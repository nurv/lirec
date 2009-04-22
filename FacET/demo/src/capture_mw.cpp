/* $Id: capture_mw.cpp 10 2009-03-24 21:45:48Z mw $
*/
/*! \file capture_mw.cpp
 *
 * This file contains camera access functions for 
 * setting and checking a certain subset of common 
 * parameters for different camera interface types 
 * (actually, 1394 and v4l).
 * \author Marek Wnuk
 * \date 2009.03.13
 * \version 1.00.00
*/
/*  Camera setup function with some useful settings
    Copyright (C) 2009  Marek Wnuk <marek.wnuk@pwr.wroc.pl>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
  Some experiments with dc1394 (FW camera control)           07.03.09 MW
  Testing channel number and video mode settings (Hauppauge) 13.03.09 MW 
*/

#include <cv.h>
#include <highgui.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/ioctl.h>
#include <unistd.h>

#include "capture_mw.h"



Capture::Capture() {

  if (!readSettings()) 
    std::cerr << "ERROR: reading the default parameters (default.cfg)\n";

  camera_if = MY_CAM_IF;
  device_no = 0;
  channel_no = MY_CHANNEL_NUMBER;
  video_norm = MY_VIDEO_MODE;
}

Capture::~Capture() {

}

/*! Read default camera settings */
bool Capture::readSettings(string file_name)
{
  std::ifstream IStrm(file_name.c_str());
  
  string parameter = "";
  string valu = "";
  double value = 0;
  char bufc[200];
  bool ok;

  void (ImageParameters::*wfun_image)(int) = 0;

  cerr << "------------------------------------\n" 
       << "Capture parameters from config file:\n"
       << "- File name: [" << file_name << "]:\n"
       << "- Settings:\n";

  while ( !IStrm.eof() && !IStrm.bad() ) {
    if (( IStrm >> parameter).good() ) {  

      if (parameter.at(0) == '#') {
	IStrm.getline(bufc, 199);
	IStrm.unget();
      }
      
      else { // Capture parameters
	ok = true;
	if (!parameter.compare("BRIGHTNESS"))
	  wfun_image = &ImageParameters::setBrightness;
	else if (!parameter.compare("CONTRAST"))
	  wfun_image = &ImageParameters::setContrast;
	else if (!parameter.compare("SATURATION")) 
	  wfun_image = &ImageParameters::setSaturation;
	else if (!parameter.compare("HUE"))
	  wfun_image = &ImageParameters::setHue;
	else if (!parameter.compare("GAIN"))
	  wfun_image = &ImageParameters::setGain;
	else if (!parameter.compare("FLIP"))
	  wfun_image = &ImageParameters::setFlip;
	else if (!parameter.compare("ROTATE"))
	  wfun_image = &ImageParameters::setRotate;
	else if (!parameter.compare("IMAGE_WIDTH"))
	  wfun_image = &ImageParameters::setWidth;
	else if (!parameter.compare("IMAGE_HEIGHT"))
	  wfun_image = &ImageParameters::setHeight;

	else { cerr << ""; ok = false; }
	
        if( ok ) {
	  if ( (IStrm >> value).good() ){
	    if (wfun_image) (this->*wfun_image)(static_cast<int>(value));
	    cerr << "  " << parameter << " " << value << endl;
	    wfun_image = NULL;
	  }
	  else {
	    IStrm.clear(); IStrm.ignore(); IStrm.unget();
          }
	}
        else { wfun_image = NULL; }
      }
    }
    else IStrm.clear(); IStrm.ignore(); IStrm.unget();
  }
  cerr << "----------------------------------\n" ;
  return true;
}


/* Setup and check camera options */
void Capture::camSetup(
              CvCapture* capture,
              int cam_if,  // camera interface (0-auto, 2-v4l, 3-1394)
              int device,  // device number
              int channel, // channel number (if applicable)
              int norm,    // video standard (0-AUTO, 1-NTSC, 2-PAL, 3-SECAM)
              int quiet) {

  struct video_channel selectedChannel;

  if(cam_if == CAM_IF_V4L) {
    const char *norm_name [] = {"AUTO", "NTSC", "PAL", "SECAM"};

  /* Set the channel of the video input. For a typical V4L TV capture card, 
     this is usually 1. 
     Set the video norm to NTSC, PAL or SECAM.
  */
    if(!quiet) fprintf(stdout,"Interface: V4L\n");
    /* try to select input channel */ 
    selectedChannel.channel=channel;
    if (ioctl(((CvCaptureCAM_V4L *)capture)->deviceHandle, 
	      VIDIOCGCHAN ,
	      &selectedChannel) != -1) {
      /* try to set the video norm to ( IDEO_MODE_PAL, VIDEO_MODE_NTSC, 
	 VIDEO_MODE_SECAM) */
      selectedChannel.norm = norm;
      if (ioctl(((CvCaptureCAM_V4L *)capture)->deviceHandle, 
		VIDIOCSCHAN , 
		&selectedChannel) == -1) {
	/* Could not set selected norm */
	if(!quiet) fprintf(stdout, "%d, %s not %s norm capable.\n",
                           selectedChannel.channel,
                           selectedChannel.name, 
                           norm_name[selectedChannel.norm]);
      }
      else {
        if(!quiet) fprintf(stdout, "Channel %d (%s) set to %s norm.\n",
                           selectedChannel.channel,
                           selectedChannel.name, 
                           norm_name[selectedChannel.norm]);
      }
    }
    else {
      /* Could not set selected channel */
      if(!quiet) fprintf(stdout, "Cannot select channel.\n");
    }
  }

  if(cam_if == CAM_IF_ANY) { // BTW, how to guess actual camera IF?
    if(!quiet) fprintf(stdout,"Interface: Auto (unknown)\n"); 
  }

  if(cam_if == CAM_IF_DC1394) {
    if(!quiet) fprintf(stdout,"Interface: DC1394\n");

    // get camera info 
    dc1394_camerainfo cameraInfo;
    dc1394_get_camera_info(((CvCaptureCAM_DC1394*)capture)->handle,
                           ((CvCaptureCAM_DC1394*)capture)->camera->node,
                           &cameraInfo);
    if(!quiet) fprintf(stdout, "Firewire camera: %s (%s) on node %d\n", 
                       cameraInfo.model, 
                       cameraInfo.vendor, 
                       cameraInfo.id);
  }

  /* This is a hack. If we don't call this first then getting capture
   * properties (below) won't work right. This is an OpenCV bug. We
   * ignore the return value here. But it's actually a video frame.
   */
  cvQueryFrame( capture );
  
  if(cam_if == CAM_IF_DC1394) {
    /* Read the frame rate */
    double cfps = cvGetCaptureProperty(capture, CV_CAP_PROP_FPS);
    if(!quiet) fprintf(stdout, "FPS: %f \n", cfps);
  }

  /* Read the frame size */
  CvSize frame_size;
  frame_size.height =
    (int) cvGetCaptureProperty( capture, CV_CAP_PROP_FRAME_HEIGHT );
  frame_size.width =
    (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_WIDTH);
  if(!quiet) fprintf(stdout, "Size: %d x %d cam: %d\n", frame_size.width,
                     frame_size.height, 100*cam_if+device);

  /* MW test: set a new frame size (fails so far) 
  int rw = cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_WIDTH,
                                frame_size.width/2);
  int rh = cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_HEIGHT,
                                frame_size.height/2);
  */

  double bri, con, sat, hue, gai = -1;
  
  /* Get and show common camera settings */
  bri = (cvGetCaptureProperty( capture, CV_CAP_PROP_BRIGHTNESS ));
  con = (cvGetCaptureProperty( capture, CV_CAP_PROP_CONTRAST ));
  sat = (cvGetCaptureProperty( capture, CV_CAP_PROP_SATURATION ));
  hue = (cvGetCaptureProperty( capture, CV_CAP_PROP_HUE ));
  if(cam_if == CAM_IF_DC1394)
    gai = (cvGetCaptureProperty( capture, CV_CAP_PROP_GAIN ));
  if(!quiet) fprintf(stdout, " B = %4.0f, C = %4.0f, S = %4.0f, H = %4.0f, G = %4.0f\n",
          100*bri, 100*con, 100*sat, 100*hue, 100*gai);
  
  if(cam_if == CAM_IF_DC1394) {
    /* MW test: get and show 1394-specific properties */
    int mod, fmt, rgb;
    rgb = (int) (cvGetCaptureProperty( capture, CV_CAP_PROP_CONVERT_RGB ));
    mod = (int) (cvGetCaptureProperty( capture, CV_CAP_PROP_MODE ));
    fmt = (int) (cvGetCaptureProperty( capture, CV_CAP_PROP_FORMAT ));
    if(!quiet) fprintf(stdout, " M = 0x%04x, F = 0x%04x, R = %d\n", mod, fmt, rgb);
  }
}

/* Modify camera image parameters */
void Capture::setImageParameters(CvCapture* capture, Capture* cam) {

  if(cam->ImageParameters::readBrightness() != -2) 
    cvSetCaptureProperty(capture, CV_CAP_PROP_BRIGHTNESS, 
                         ((double)(cam->ImageParameters::readBrightness()))/100);
  if(cam->ImageParameters::readContrast() != -2) 
    cvSetCaptureProperty(capture, CV_CAP_PROP_CONTRAST,  
                         ((double)(cam->ImageParameters::readContrast()))/100);
  if(cam->ImageParameters::readSaturation() != -2) 
    cvSetCaptureProperty(capture, CV_CAP_PROP_SATURATION,  
                         ((double)(cam->ImageParameters::readSaturation()))/100);
  if(cam->ImageParameters::readHue() != -2) 
    cvSetCaptureProperty(capture, CV_CAP_PROP_HUE,  
                         ((double)(cam->ImageParameters::readHue()))/100);
  if(cam->ImageParameters::readGain() != -2) 
    cvSetCaptureProperty(capture, CV_CAP_PROP_GAIN,  
                         ((double)(cam->ImageParameters::readGain()))/100);
  cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_WIDTH, 
                       cam->ImageParameters::readWidth()); //?
  cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_HEIGHT, 
                       cam->ImageParameters::readHeight());//?

}


