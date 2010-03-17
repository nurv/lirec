
/* ************************************************************************************************************

Program : example sending images with SAMGAR
Date	: 24 Aug 2009
Author  : K Du Casse

A simple program that shows how to send images with SAMGAR

************************************************************************************************************ */



// To use samgar you only need to attach the SamgarLib and the SamgarMainClass header
#include "cv.h"
#include "highgui.h"
#include <SamgarMainClass.h>
using namespace std;

int main() {
Network yarp;
	// Creates a module with a name , catagory , sub catagory , typeofmodule 
	SamgarModule MyFirstTest("CameraSend","behaviour","happy",run); // Cant have spaces or underscores

	// then we setup the image port
	MyFirstTest.SetupImagePort("VideoOut");

 // OCV get a device
  CvCapture* capture = cvCaptureFromCAM( CV_CAP_ANY );
  // OCV make sure its there
  if( !capture ) {fprintf( stderr, "ERROR: capture is NULL \n" );getchar();return -1;}

  
  cvNamedWindow("original", CV_WINDOW_AUTOSIZE );

  while( 1 ) 
  {
    IplImage* frame = cvQueryFrame( capture );
    if( !frame ){fprintf( stderr, "ERROR: frame is null...\n" );getchar();break;}
	
   cvShowImage( "original", frame );
	// send the frame
	MyFirstTest.SendPictureOCVNative(frame);
	MyFirstTest.SucceedFail(true,888);
    if( (cvWaitKey(10) & 255) == 27 ) break;

	//yarp::os::Time::delay(0.1);
  }


  cvReleaseCapture( &capture );
  cvDestroyWindow( "original" );
  return 0;
}






























































/*

#include "cv.h"
#include "highgui.h"


#include <stdio.h>
#include <SamgarMainClass.h>
using namespace std;


using namespace yarp::sig::draw;

int main() 
{
SamgarModule MyFirstTest1("Module1","behaviour","happy","Interupt"); // Cant have spaces or underscores
MyFirstTest1.SetupImagePort("VideoOut");

SamgarModule MyFirstTest2("Module2","behaviour","happy","Interupt"); // Cant have spaces or underscores
MyFirstTest2.SetupImagePort("VideoIn");

Network::connect("/Port_Module2_VideoIn_OCV","/Port_Module1_VideoOut_OCV","udp");
Network::connect("/Port_Module1_VideoOut_OCV","/Port_Module2_VideoIn_OCV","udp");
 ImageOf<PixelBgr> yarpImage;
		 yarpImage.resize(300,200);
		 addCircle(yarpImage,PixelBgr(255,0,0),
         yarpImage.width()/2,yarpImage.height()/2,
         yarpImage.height()/4);


while(1)
	{
	//	MyFirstTest1.SendPictureYarpNative

 	    
		 MyFirstTest1.SendPictureYarpNative(yarpImage);

	}

}



/*

#include "cv.h"
#include "highgui.h"
#include <stdio.h>



//#include <SamgarMainClass.h>
//using namespace std;

int main (void)
{
//SamgarModule MyFirstTest("Module1","behaviour","happy","Interupt"); // Cant have spaces or underscores
//MyFirstTest.AddPort("output1");
//MyFirstTest.AddPort("output2");
//MyFirstTest.AddPort("output3");
//MyFirstTest.AddPort("output4");
//MyFirstTest.AddPort("output5");
//MyFirstTest.AddPort("output6");
//MyFirstTest.AddPort("output7");
//Network::connect("/Port_Module1_output7","/Port_Module2_output7");
//Network::connect("/Port_Module2_output7","/Port_Module1_output7");

//static double xx = 1;
//string yy;

  cvNamedWindow( "mywindow", CV_WINDOW_AUTOSIZE );

  CvCapture* capture = cvCaptureFromCAM( CV_CAP_ANY );
  if( !capture ) {   fprintf( stderr, "ERROR: capture is NULL \n" ); getchar();return -1;}

 // MyFirstTest.SetupImagePort("VideoOut");


 


while(1)
{
    IplImage* frame = cvQueryFrame( capture );
    if( !frame ) {fprintf( stderr, "ERROR: frame is null...\n" );getchar();break;}
	cvShowImage( "mywindow", frame);
}


//sleep(10);
return 0;
}*/