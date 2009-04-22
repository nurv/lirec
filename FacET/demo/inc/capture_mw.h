/* $Id: capture_mw.h 10 2009-03-24 21:45:48Z mw $
*/
/*! \file capture_mw.h
 *
 * File defines the class Capture for image capturing in OpenCV.
 * 
 * \author Marek Wnuk
 * \date 2009.03.13
 * \version 1.00.00
*/
/*  Definitions and class Capture for image capturing in OpenCV
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


#ifndef _CAPTURE_MW_H_
#define _CAPTURE_MW_H_

#include <iostream>
#include <iomanip>
#include <iostream>
#include <fstream>
#include <linux/videodev.h>
#include <libraw1394/raw1394.h>
#include <libdc1394/dc1394_control.h>

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

/* MW for cvFlip() */
#define FLIP_HORIZONTAL 1
#define FLIP_VERTICAL 0
#define FLIP_BOTH -1
#define ROTATE FLIP_BOTH

/*
 * These are the hidden data structures which reveal the file descriptor
 * of an OpenCV capturing device. CvCaptureCAM_V4L and CvCaptureCAM_DC1394 
 * can be used by explicit typecasting an CvCapture structure. 
 * The file descriptor is needed to access the extended functions of cameras. 
 * Note that these structures are copied from OpenCV, version 1.0.0. 
 * If you want to use future versions you will perhaps have to copy them 
 * again from cvcap_v4l.cpp, cvcap_DC1394.cpp and _highgui.h (from 
 * otherlibs/highgui/).
 */
typedef void         (CV_CDECL* CvCaptureCloseFunc)         ( CvCapture* capture );
typedef int          (CV_CDECL* CvCaptureGrabFrameFunc)     ( CvCapture* capture );
typedef IplImage   * (CV_CDECL* CvCaptureRetrieveFrameFunc) ( CvCapture* capture );
typedef double       (CV_CDECL* CvCaptureGetPropertyFunc)   ( CvCapture* capture, int id );
typedef int          (CV_CDECL* CvCaptureSetPropertyFunc)   ( CvCapture* capture, int id, double value );
typedef const char * (CV_CDECL* CvCaptureGetDescriptionFunc)( CvCapture* capture );

typedef struct CvCaptureVTable
{
  int                           count;
  CvCaptureCloseFunc            close;
  CvCaptureGrabFrameFunc        grab_frame;
  CvCaptureRetrieveFrameFunc    retrieve_frame;
  CvCaptureGetPropertyFunc      get_property;
  CvCaptureSetPropertyFunc      set_property;
  CvCaptureGetDescriptionFunc   get_description;
}
  CvCaptureVTable;

typedef struct CvCaptureCAM_DC1394
{
  CvCaptureVTable* vtable;
  raw1394handle_t handle;
  nodeid_t  camera_node;
  dc1394_cameracapture* camera;
  int format;
  int mode;
  int color_mode;
  int frame_rate;
  char * device_name;
  IplImage frame;
  int convert;
  int buffer_is_writeable;  // indicates whether frame.imageData is allocated by OpenCV or DC1394
}
  CvCaptureCAM_DC1394; // MW from cvcap_dc1394.cpp

typedef struct CvCaptureCAM_V4L
{
    CvCaptureVTable* vtable;
    int deviceHandle;
    int bufferIndex;
    int FirstCapture;
    struct video_capability capability;
    struct video_window     captureWindow;
    struct video_picture    imageProperties; 
    struct video_mbuf       memoryBuffer;
    struct video_mmap       *mmaps;
    char *memoryMap;
    IplImage frame;
    #ifdef HAVE_CAMV4L2
    buffer buffers[10];
    struct v4l2_capability cap;
    struct v4l2_input inp;
    struct v4l2_format form;
    struct v4l2_crop crop;
    struct v4l2_cropcap cropcap;
    struct v4l2_requestbuffers req;
    struct v4l2_jpegcompression compr;
    struct v4l2_control control;
    enum v4l2_buf_type type;
    struct v4l2_queryctrl queryctrl;
    struct v4l2_querymenu querymenu;
    int v4l2_brightness, v4l2_brightness_min, v4l2_brightness_max;
    int v4l2_contrast, v4l2_contrast_min, v4l2_contrast_max;
    int v4l2_saturation, v4l2_saturation_min, v4l2_saturation_max;
    int v4l2_hue, v4l2_hue_min, v4l2_hue_max;
    int v4l2_gain, v4l2_gain_min, v4l2_gain_max;
    #endif
}
  CvCaptureCAM_V4L;

/*!
  Definitions and prototypes for  camera settings
  MW 13.03.09 
*/

//#define MY_CHANNEL_NUMBER 0  // Hauppauge WinTV USB2 Tuner
#define MY_CHANNEL_NUMBER 1  // Hauppauge WinTV USB2 S_Video (Canon)

#define MY_VIDEO_MODE VIDEO_MODE_NTSC // (Canon)
//#define MY_VIDEO_MODE VIDEO_MODE_PAL
//#define MY_VIDEO_MODE VIDEO_MODE_SECAM

#define CAM_IF_ANY    (CV_CAP_ANY/100)
#define CAM_IF_V4L    (CV_CAP_V4L/100)
#define CAM_IF_DC1394 (CV_CAP_DC1394/100)

#define MY_CAM_IF CAM_IF_ANY

/*! \class ImageParameters
 *
 *  Defines the image parameters (brightness, contrast, saturation and
 *  hue). Provides methods for parameters reading and setting.
 */
class ImageParameters
{
 protected:

  /*! Image width */
  int width;

  /*! Image height */
  int height;

  /*! Image brightness */
  int brightness;

  /*! Image contrast */
  int contrast;

  /*! Image saturation */
  int saturation;

  /*! Image hue */
  int hue;

  /*! Gain */
  int gain;

  /*! Horizontal image flipping
   *
   *  True - mirror-like image
   *  False - normal image
   */
  int flip;

  /*! Image rotation by 180 deg.
   *
   *  True - seascape image
   *  False - normal image
   */
  int rotate;

public:

  /*! Parameterless constructor
   * 
   *  Sets the default values of the components
   */
  ImageParameters(): width(640), height(480), brightness(-2), contrast(-2), 
    saturation(-2), hue(-2), gain(-2), flip(0), rotate(0) {
  }
    
    /*! Constructor
     *
     *  Sets the provided values of the components
     *
     * \param wdth - image width value
     * \param hght - image height value
     * \param b - brightness value
     * \param c - contrast value
     * \param s - saturation value
     * \param h - hue value
     * \param g - gain value
     * \param f - flip value
     * \param r - rotate value
     */
    ImageParameters(int wdth, int hght, int b, int c, int s, int h, int g, 
      bool f, bool r): 
      width(wdth), height(hght), brightness(b), contrast(c), saturation(s), 
      hue(h), gain(g), flip(f), rotate(r) {}
      
      /*! Sets the image parameters 
       *  
       * \param wdth - new image width value
       * \param hght - new image height value
       * \param b - new brightness value
       * \param c - new contrast value
       * \param s - new saturation value
       * \param h - new hue value
       * \param g - new gain value
       * \param f - new flip value
       * \param r - new rotate value
       */
      void setParameters(int wdth, int hght, int b, int c, int s, int h, int g, 
        int f, int r) {
	height=hght; width=wdth; brightness=b; contrast=c; saturation=s; 
	hue=h; gain=g; flip=f; rotate=r;
      }
      
      /*! Sets the image width value
       *
       *  \param val - new image width value
       */
      void setWidth(int val) { width = val; }
      
      /*! Reads the image width value
       *
       *  \return image width value
       */
      int readWidth() const { return width; }
      
      /*! Sets the image height value
       *
       *  \param val - new image height value
       */
      void setHeight(int val) { height = val; }
      
      /*! Reads the image height value
       *
       *  \return image height value
       */
      int readHeight() const { return height; }
      
      /*! Sets the brightness value
       *
       *  \param val - new brightness value
       */
      void setBrightness(int val) { brightness = val; }
      
      /*! Reads the brightness value
       *
       *  \return brightness value
       */
      int readBrightness() const { return brightness; }
      
      /*! Sets the contrast value
       *
       *  \param val - new contrast value
       */
      void setContrast(int val) { contrast = val; }
      
      /*! Reads the contrast value
       *
       *  \return contrast value
       */
      int readContrast() const { return contrast; }
      
      /*! Sets the saturation value
       *
       *  \param val - new saturation value
       */
      void setSaturation(int val) { saturation = val; }
      
      /*! Reads the saturation value
       *
       *  \return saturation value
       */
      int readSaturation() const { return saturation; }
      
      /*! Sets the hue value
       *
       *  \param val - new hue value
       */
      void setHue(int val) { hue = val; }
      
      /*! Reads the hue value
       *
       *  \return hue value
       */
      int readHue() const { return hue; }
      
      /*! Sets the hue value
       *
       *  \param val - new hue value
       */
      void setGain(int val) { gain = val; }
      
      /*! Reads the hue value
       *
       *  \return hue value
       */
      int readGain() const { return gain; }
      
      /*! Sets the value of \link ImageParameters::flip flip\endlink flag
       * 
       * \param val - if 0 then flip is set to FALSE, otherwise
       *              flip is set to TRUE
       */
      void setFlip(int val) { flip=val; }
      
      /*! Reads the value of flip flag
       *
       * \return value of flip flag
       */
      int readFlip() { return flip; }
      
      /*! Sets the value of \link ImageParameters::rotate rotate\endlink 
       * flag
       * 
       * \param val - if 0 then rotate is set to FALSE, otherwise
       *              rotate is set to TRUE
       */
      void setRotate(int val) { rotate=val; }
      
      /*! Reads the value of rotate flag
       *
       * \return value of rotate flag
       */
      int readRotate() { return rotate; }

};

/*! \class Capture
 *
 * Includes methods for camera image capture setup.
 */
class Capture: 
              public ImageParameters 
{

public:

  /* Camera interface (0-auto, 2-v4l, 3-1394) */
  int camera_if;

  /* Device number */
  int device_no;

  /* Channel number (if applicable) */
  int channel_no;

  /* Video standard (0-AUTO, 1-NTSC, 2-PAL, 3-SECAM) */
  int video_norm;


  Capture();
  ~Capture();

  /*! Read the settings from config file
   *
   * \param file_name - settings file name
   * 
   * \retval true  - succesful reading of the config file
   * \retval false - otherwise
   */
  bool readSettings(std::string file_name = "default.cfg");

  /*! Modify camera image parameters 
   *
   * \param capture - camera descriptor
   * \param ipar - camera settings
   *
   */
  void setImageParameters(CvCapture* capture, Capture* cam);

  /*! Setup and check camera options
   *
   * \param file_name - settings file name
   * \param cam_if - camera interface (0-auto, 2-v4l, 3-1394)
   * \param device - device number
   * \param channel - channel number (if applicable)
   * \param norm - video standard (0-AUTO, 1-NTSC, 2-PAL, 3-SECAM)
   * \param quiet - 1 - be quiet, 0 - be verbose
   * 
   */
  void camSetup(CvCapture* capture,
              int cam_if,  // camera interface (0-auto, 2-v4l, 3-1394)
              int device,  // device number
              int channel, // channel number (if applicable)
              int norm,    // video standard (0-AUTO, 1-NTSC, 2-PAL, 3-SECAM)
              int quiet);
};

#endif

