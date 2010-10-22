
/*
	An example on how to get video data over a network, 
	1. Uses yarp image over the network, has many advantages (in yarp documentation)
	2. If you dont use lossy connections could easily hog bandwidth.


*/


#define CV_NO_BACKWARD_COMPATIBILITY

#include "cv.h"
#include "highgui.h"
#include "VideoGet.h"

#include <iostream>
#include <cstdio>

#ifdef _EiC
#define WIN32
#endif

using namespace std;
using namespace cv;



int main( void)
{
	Vget MyGetMod;     // create instance
	MyGetMod.innit();  // do samgar only stuff in its own innit, keeps the rest of the code clear from clutter

	char Vid2[] = "After";

	IplImage * frm;


	
	int k =0;
	int flag =0;
	while(k!='q')  // doesn't work properly but can be modified
	{
		ImageOf<yarp::sig::PixelBgr> *yarpImage2 = MyGetMod.VideoRecive.read(true); // true means it'll wait for image
		if(yarpImage2!=NULL)   // doesn't work as expected, you will have to check the validity of the IplImage with ->getIplImage
		{
		if(flag=0)
			{
			cvNamedWindow (Vid2); // only create the window to display if we have a input
			}

		flag=1;
		puts("got img");
		cvShowImage(Vid2,yarpImage2->getIplImage());  // show the image in the window that we have recived
		int k = cvWaitKey( 50 ); // is a time delay, works well better with the opencv window than sleep
		}


	}
cvDestroyWindow( Vid2 );


}