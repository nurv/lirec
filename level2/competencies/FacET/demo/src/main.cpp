/* $Id: main.cpp 10 2009-03-24 21:45:48Z mw $
 * +-------------------------------------------------------------------+
 * | This file contains parts of code from the application created for |
 * | the Master thesis supervised by Marek Wnuk (Wroclaw University of |
 * | Technology):  "Wykorzystanie systemu wizyjnego do rozpoznawania   |
 * | emocji czlowieka" ("Vision system in human emotions recognition") |
 * | by Marcin Namysl in June 2008.                                    |
 * +-------------------------------------------------------------------+
 *
 * \author Marek Wnuk
 * \date 2009.03.03
 * \version 1.00.00
 */

/*
    FacET is a library for detecting and parameterising face components.
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

/*! \file main.cpp
 *
 * The file contains the sources of the FacET demo application. 
 * 
 */


#include <sstream>
#include <sys/stat.h>

#include "timer.h"
#include "facet.h"

#include "options.h"
#include "capture_mw.h"

/*
int channel = MY_CHANNEL_NUMBER;
int norm = MY_VIDEO_MODE;
int cam = 0;
int cam_if = 0;
double br = -1.0;
double co = -1.0;
double sa = -1.0;
double hu = -1.0;
double ga = -1.0;
*/

Facet emo;
DetectionOptions dopt;
//ImageParameters capt;
Capture camera;

/*! Verbosity suppresion flag */
int quiet = 0;

/*! The number of features detection runs */
int detection_runs_counter = 0;

/*! Detection in progress flag */
bool detrun = false;

/*! Procedures execution time counter */
Timer uclock;

/*! Path name for output file */
std:: string out_path;

/*! Path name for input file */
std:: string in_path;

/*! Camera image capturing object */
CvCapture *capture;

/*! Resulting video file writer object */
CvVideoWriter *writer;

/*! Read demo options from the config file
 *
 * \param file_name - config file name
 */

bool readOptions(string file_name)
{
  std::ifstream IStrm(file_name.c_str());
  
  string parameter = "";
  string valu = "";
  double value = 0;
  char bufc[200];
  bool ok;

  void (DetectionOptions::*wfun_opt)(int) = 0;

  cerr << "----------------------------------\n" 
       << "Options from config file:\n"
       << "- File name: [" << file_name << "]:\n"
       << "- Settings:\n";

  while ( !IStrm.eof() && !IStrm.bad() ) {
    if (( IStrm >> parameter).good() ) {  

      if (parameter.at(0) == '#') {
	IStrm.getline(bufc, 199);
	IStrm.unget();
      }
      
      else { // parameters
	ok = true;
	if (!parameter.compare("TIMING"))
	  wfun_opt = &DetectionOptions::setTiming;
	else if (!parameter.compare("IMGREC"))
	  wfun_opt = &DetectionOptions::setImgrec;
	else if (!parameter.compare("PARREC"))
	  wfun_opt = &DetectionOptions::setParrec;
	else if (!parameter.compare("SHOW"))
	  wfun_opt = &DetectionOptions::setShow;
	else if (!parameter.compare("VERBOSE"))
	  wfun_opt = &DetectionOptions::setVerbose;

	else { cerr << ""; ok = false; }
	
	if ( ok & ((IStrm >> value).good()) ){
	  if (wfun_opt) (dopt.*wfun_opt)(static_cast<int>(value));
	  cerr << "  " << parameter << " " << value << endl;
	  wfun_opt = NULL;
	}
	else {
	  IStrm.clear(); IStrm.ignore(); IStrm.unget();
	}
      }
    }
    else {IStrm.clear(); IStrm.ignore(); IStrm.unget();}
  }
  cerr << "----------------------------------\n" ;
  return true;
}


/*! Write the parameters to the output file
 *
 * \param file_name - output file name
 */

void writeDataToFile(string file_name, int frame_no)
{
  std::ofstream OStrm(file_name.c_str(), std::ios::app);
  
  if (OStrm.is_open()){
    OStrm << frame_no << "; ";
    
    for (std::list<facepar_t>::iterator iter = emo.facesList.begin();
	 iter != emo.facesList.end(); ++iter)
      OStrm << 
	iter->roix << " " <<
	iter->roiy << " " <<
	iter->angle << " " <<
	iter->LEbBnd << " " <<
	iter->LEbDcl << " " <<
	iter->LEyOpn << " " <<
	iter->LEbHgt << " " <<
	iter->REbBnd << " " <<
	iter->REbDcl << " " <<
	iter->REyOpn << " " <<
	iter->REbHgt << " " <<
	iter->LiAspt << " " <<
	iter->LLiCnr << " " <<
	iter->RLiCnr << " " <<
	iter->Wrnkls << " " <<
	iter->Nstrls << " " <<
	iter->TeethA << " " ;
    
    OStrm << endl;
  }
}


/*! Face features detection from video/camera */

void detectFromStream(int device)
{
  if (device == 0) {

    capture = cvCaptureFromCAM(100*camera.camera_if+camera.device_no);

    if( !capture )
    {
      if(dopt.readVerbose() > 0) cerr << "Could not initialize capturing...\n";
      return;
    }

    camera.setImageParameters(capture, & camera);

    camera.camSetup(capture, camera.camera_if, camera.device_no, 
                    camera.channel_no, camera.video_norm, quiet);
  }
  else if (device == 1) {
    if (in_path.c_str())
      capture = cvCaptureFromFile(in_path.c_str());
    else { 
      cout << "ERROR: Empty video file name\n\n";
      return;
    }
  }
  
  if( capture ) {
    detection_runs_counter++;

    cvQueryFrame( capture );
    /* Read the frame size */
    CvSize frame_size;
    frame_size.height =
      (int) cvGetCaptureProperty( capture, CV_CAP_PROP_FRAME_HEIGHT );
    frame_size.width =
      (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_WIDTH);
  
    if(dopt.readImgrec()) {
      std::string file_name;
      std::stringstream ss;
      ss << detection_runs_counter;
      file_name = out_path;
      file_name += ss.str();
      file_name += "_vid.avi"; 
      writer = cvCreateVideoWriter(file_name.c_str(), 
                                   0, 
                                   5, 
                                   frame_size,
                                   1);
    }

    std::string par_name;

    if(dopt.readParrec()) {
      std::stringstream ss;
      ss << detection_runs_counter;
      par_name = out_path;
      par_name += ss.str();
      par_name += "_fea.prm"; // parameters file
    }
    
    int frame_counter=1;
    detrun = true;
    
    while(detrun) {
      IplImage *frame = 0;
      IplImage *frame_copy = 0;
      
      if( !cvGrabFrame( capture )) break;
      frame = cvRetrieveFrame( capture );
      if( !frame ) break;
      
      frame_copy=cvCreateImage(cvSize(frame->width,frame->height),
			       IPL_DEPTH_8U, frame->nChannels );

/*
      if( frame->origin == IPL_ORIGIN_TL )
	cvCopy( frame, frame_copy, 0 );
      else
	cvFlip( frame, frame_copy, 0 );
*/
      
      if((camera.ImageParameters::readRotate()) && (camera.ImageParameters::readFlip())) 
        cvFlip(frame, frame_copy, FLIP_VERTICAL); 
      else if(camera.ImageParameters::readRotate()) 
        cvFlip(frame, frame_copy, ROTATE); 
      else if(camera.ImageParameters::readFlip())
        cvFlip(frame, frame_copy, FLIP_HORIZONTAL);
      else cvCopy(frame, frame_copy, 0);

      emo.face.clearElements();
      
      //emo.checkImageParameters(); // this confuses FW camera (sets mono mode ?)
      
      if(dopt.readTiming()) {
        uclock.start();
	
        emo.detectFeat(frame_copy,frame_copy);
	
        if(dopt.readVerbose() > 0) cerr << "[Time] = " 
                                               << uclock.readMSecSinceStart()
	                                       << "ms\n";
        uclock.stop();
      }
      else emo.detectFeat(frame_copy,frame_copy);
      
      if(dopt.readParrec()) writeDataToFile(par_name, frame_counter);
      if(dopt.readImgrec()) cvWriteFrame( writer, frame_copy );

      emo.cleanFacesList();
      
      frame_counter++;
      
      if(dopt.readShow()) {
        cvNamedWindow("Result",1);
        cvShowImage("Result", frame_copy);
      }
      
      //    cvReleaseImage( &frame );
      if( cvWaitKey( 10 ) >= 0 ) detrun = false;
      cvReleaseImage( &frame_copy );
    }
    cvReleaseCapture( &capture );
    if(dopt.readImgrec()) cvReleaseVideoWriter( &writer );
  }
}

/*! Face features detection from still image */

void detectFromImage()
{
  detection_runs_counter++;
  std::string file_name = out_path;
  
  if(dopt.readImgrec()) {
    std::stringstream ss;
    ss << detection_runs_counter;
    file_name += ss.str();
    file_name += "_img.png";
  } 

  std::string par_name;

  if(dopt.readParrec()) {
    std::stringstream ss;
    ss << detection_runs_counter;
    par_name = out_path;
    par_name += ss.str();
    par_name += "_fea.prm"; // parameters file
  }
  
  IplImage *src = cvLoadImage((const char *)in_path.c_str());

  if(src == NULL){
    cout << "No input file:" << in_path <<endl;
    return;
  }
  
  emo.face.clearElements(); 

  if(dopt.readTiming()) {
    uclock.start();
    
    emo.detectFeat(src, src);
    
    if(dopt.readVerbose() > 0) cerr << "[Time] = " 
					   << uclock.readMSecSinceStart()
					   << "ms\n";
    uclock.stop();
  }
  else emo.detectFeat(src, src);
  
  if(dopt.readParrec()) writeDataToFile(par_name, 1);
  if(dopt.readImgrec()) cvSaveImage(file_name.c_str(),src);

  emo.cleanFacesList();
  
  if(dopt.readShow()) {
    cvNamedWindow("Result",1);
    cvShowImage("Result", src);
  }
  
  cvWaitKey();
}




/*! Selector function for menu example */

void selectMenuOption(const char option)
{

  string name;

  switch (option){

  case 't': // Main function for camera
    detectFromStream(0);
    break;
    
  case 'v': // Main function for video file

    cout << " Video name :\n";
    cout << "  (file name extention must be .avi)\n";
    cout << "  (provide file basename, no extension";
    cout << " face.avi -> face):";
    cout << "\n >> ";
    cin >> name;

    in_path = "input/video/";
    in_path += name;
    in_path += ".avi";

    detectFromStream(1);
    break;
    
  case 'z': // Main function for image file

    cout << " Image name :\n";
    cout << "  (file name extention must be .jpg)\n";
    cout << "  (provide file basename, no extension";
    cout << " face.jpg -> face):";
    cout << "\n >> ";
    cin >> name;

    in_path = "input/img/";
    in_path += name;
    in_path += ".jpg";

    detectFromImage();
    break;

  case 'q':
    cout << "exit\n";
    break;
    
  default:
    cout << "Illegal option. Try again.\n";

#ifdef WIN32

    //usleep() takes time in microseconds, Window's sleep() needs milliseconds
    Sleep(800); //CHANGE GC

#else  // WIN32

    usleep(800000);

#endif // WIN32

  }
}

/*! Menu function of an application example */

void showMenu()
{
  char ans = 'a';
  time_t stim = time(NULL);
  struct tm * st = localtime(&stim);
  char tbuf[18];
  sprintf(tbuf, "%02d.%02d.%02d.%02d.%02d.%02d", 
          st->tm_year%100, 
          st->tm_mon+1, 
          st->tm_mday, 
          st->tm_hour, 
          st->tm_min, 
          st->tm_sec
         );
 
  out_path = dopt.readOutputImagePath();
  out_path += tbuf;
  if(-1 == mkdir(out_path.c_str(), 01777)) exit(-1);
  out_path += "/";
    
  cerr << "Output path:  " << out_path << " \n\n"; 

  while (ans != 'q'){
    cout << " \n\n";
    cout << " #----------------- FacET demo -----------------#\n";
    cout << " |       started at: " << tbuf << "          |\n"; 
    cout << " |                                              |\n";
    cout << " | (t) - Main program (mode : camera)           |\n";
    cout << " | (v) - Main program (mode : video file)       |\n";
    cout << " | (z) - Main program (mode : image file)       |\n";
    cout << " | (q) - Quit                                   |\n";
    cout << " |                                              |\n";
    cout << " #----------------------------------------------#\n";
    cout << endl;
    cout << " > " ;
    cin  >> ans;
    cout << endl;

    selectMenuOption(ans);
  }
}

/*! Main function of an application example */

int main( int argc, char** argv )
{

  readOptions("default.cfg");

  /* parse the command line */
  int c;
  opterr = 0;
  while ((c = getopt (argc, argv, "b:c:s:h:g:n:m:l:u:qpvf")) != -1) {
    switch (c) {
      case 'b':
          camera.ImageParameters::setBrightness(atoi(optarg));
          break;
      case 'c':
          camera.ImageParameters::setContrast(atoi(optarg));
          break;
      case 's':
          camera.ImageParameters::setSaturation(atoi(optarg));
          break;
      case 'h':
          camera.ImageParameters::setHue(atoi(optarg));
          break;
      case 'g':
          camera.ImageParameters::setGain(atoi(optarg));
          break;
      case 'n':
          camera.channel_no = atoi(optarg);
          break;
      case 'm':
          camera.video_norm = atoi(optarg);
          break;
      case 'l':
          camera.ImageParameters::setFlip(atoi(optarg));
          break;
      case 'u':
          camera.ImageParameters::setRotate(atoi(optarg));
          break;
      case 'v':
          camera.camera_if = CAM_IF_V4L;
          break;
      case 'f':
          camera.camera_if = CAM_IF_DC1394;
          break;
      case 'q':
          quiet = 1;
          break;
      case '?':
          if (isprint (optopt))
          fprintf (stdout, "\nUnknown command line option `-%c'.\n", optopt);
          else
          fprintf (stdout,
                   "\nUnknown command line option character `\\x%x'.\n",
                   optopt);
      case 'p':
          printf("\nCommand line options:\n\n"
              "-b INT\tset brightness\n"
              "-c INT\tset contrast\n"
              "-s INT\tset saturation\n"
              "-h INT\tset hue\n"
              "-g INT\tset gain\n"
              "-n INT\tset channel\n"
              "-m INT\tset video mode\n"
              "-l INT\tset mirror mode\n"
              "-u INT\tset rotate mode\n"
              "-v \tV4L interface\n"
              "-f \t1394 interface\n"
              "-p \tshow this help message\n");
          return 1;
      default:
          abort ();
    }
    // fprintf(stderr, "optind %d, argc %d\n", optind, argc);
    if (optind<argc)
    {
        camera.device_no = atoi(argv[optind]);
    }
    else
    {
        printf("Usage: %s [options] camera_number\n"
               "or     %s -p\tfor help on options\n", argv[0], argv[0]);
        return 1;
    }
  }

  showMenu();
}
