
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

VideoSend::VideoSend(std::string name): SamClass(name)
{
}

void VideoSend::SamInit()
{
   newPort(&videoOutput, "Out"); // add new port  
   StartModule();	
   puts("started video send");
}

void VideoSend::SamIter()
{
}

void VideoSend::setImagePtr(const yarp::sig::ImageOf<yarp::sig::PixelBgr>& image)
{
  yarp::sig::ImageOf<yarp::sig::PixelBgr> &yarpImage = this->videoOutput.prepare();
  yarpImage.copy(image);
  this->videoOutput.write();
}


int main( void)
{
	VideoSend MySendMod("/VideoS");   // create instance of the mod
	MySendMod.SamInit(); // start the module up, samgar only code

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
		yarp::sig::ImageOf<yarp::sig::PixelBgr> yarpImage;
  		yarpImage.wrapIplImage(frm);
		MySendMod.setImagePtr(yarpImage);
/*
		ImageOf<yarp::sig::PixelBgr> &yarpImage = MySendMod.VideoSend.prepare();
		yarpImage.wrapIplImage(frm);
		MySendMod.VideoSend.write();
*/
		int k = cvWaitKey( 50 );
		}
	}


cvDestroyWindow( Vid1 ); // Destroy the window
cvReleaseCapture( &capture );
}

else
puts( "Can not Open the Webcam" );

}
