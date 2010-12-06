
#include <stdio.h>
#include <conio.h>
#include <windows.h>
#include "cv.h"
#include "highgui.h"
#include "SamModules.h"
using namespace std;
using namespace cv;

int main() {

	samVideoOut cameraOut("/vCamera");
	cameraOut.PamInit("videoOut"); 

	CvCapture* capture = cvCaptureFromCAM( 0 );
	if( !capture ) {fprintf( stderr, "ERROR: capture is NULL \n" );return -1;}

	//cvNamedWindow( "Camera", CV_WINDOW_AUTOSIZE );
	int ic = 0;

	while( 1 ) 
	{
		printf("\n  vCamera %d  ", ic++);
		IplImage* frame = cvQueryFrame( capture );
		if( !frame ){Sleep(50);continue;}

		//cvShowImage( "Camera", frame );
		yarp::sig::ImageOf<yarp::sig::PixelBgr> yarpImage;
  		yarpImage.wrapIplImage(frame);
		cameraOut.setImagePtr(yarpImage);

		Sleep(20);
		if( _kbhit() > 0 ) { if( _getch() == 0x1b ) break; } 
		//if( (cvWaitKey(30) & 255) == 27 ) break;
	}
	//cvDestroyWindow( "Camera" );
	cvReleaseCapture( &capture );
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

