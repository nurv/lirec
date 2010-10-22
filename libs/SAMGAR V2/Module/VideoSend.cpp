
/*
A example of sending a image over the network, has been tested for long runs, uses yarp image for the network.


*/


#define CV_NO_BACKWARD_COMPATIBILITY

#include "cv.h"
#include "highgui.h"
#include "VideoSend.h"

#include <iostream>
#include <cstdio>

#ifdef _EiC
#define WIN32
#endif

using namespace std;
using namespace cv;



int main( void)
{
	Vsend MySendMod;   // create instance of the mod
	MySendMod.innit(); // start the module up, samgar only code

	char Vid1[] = "Before";

	IplImage * frm;
	CvCapture * capture;
	capture = cvCaptureFromCAM(0); // the parameter for a cam

if( capture ) 
	{
	cvNamedWindow (Vid1);

	while(1)
	{
	frm = cvQueryFrame( capture );
	if(frm)
		{
		cvShowImage (Vid1, frm);

		//printf("%s \n",frm->colorModel); // probably rgb
		ImageOf<yarp::sig::PixelBgr> &yarpImage = MySendMod.VideoSend.prepare();
		yarpImage.wrapIplImage(frm);
		MySendMod.VideoSend.write();
		int k = cvWaitKey( 50 );
		}
	}


cvDestroyWindow( Vid1 ); // Destroy the window
cvReleaseCapture( &capture );
}

else
puts( "Can not Open the Webcam" );

}