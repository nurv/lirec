
//#include <stdio.h>
//#include <conio.h>
//#include <windows.h>
//#include <math.h>
//#include <iostream>
#include "cv.h"
#include "highgui.h"
#include "SamModules.h"

int main(void) {

	//*********** Yarp, SAMGAR init

	samVideoIn fdVideoIn("/vFD1");
	fdVideoIn.PamInit("FDvideoIn"); 

	samDataOut fdDataOut("/vFD2");
	fdDataOut.PamInit("FDPoseOut");

	//*********** Face Detection init
	int faceHowMany;
	int level = 2;
	int minsize = 30;
	double scale = 2;
	double x = 0.25;
	double y = 0.05;
	double xLocation[10];
	double yLocation[10];

	CvHaarClassifierCascade* cascade1;
	CvHaarClassifierCascade* cascade2;
	CvHaarClassifierCascade* cascade3;
	const char* cascade_name1 = "classifier\\haarcascade_frontalface_alt.xml";
	cascade1 = (CvHaarClassifierCascade*)cvLoad( cascade_name1, 0, 0, 0 );
	if( level>0 && !cascade1 ) printf("\n ERROR: Could not load the classifier 1 ");

	const char* cascade_name2 = "classifier\\haarcascade_frontalface_default.xml";
	cascade2 = (CvHaarClassifierCascade*)cvLoad( cascade_name2, 0, 0, 0 );
	if( level>2 && !cascade2 ) printf("\n ERROR: Could not load the classifier 2 ");

	const char* cascade_name3 = "classifier\\haarcascade_upperbody.xml";
	cascade3 = (CvHaarClassifierCascade*)cvLoad( cascade_name3, 0, 0, 0 );
	if( level>1 && !cascade3 ) printf("\n ERROR: Could not load the classifier 3 ");

	CvMemStorage* storage1;
	CvMemStorage* storage2;
	CvMemStorage* storage3;
	storage1 = cvCreateMemStorage(0);
	storage2 = cvCreateMemStorage(0);
	storage3 = cvCreateMemStorage(0);

	IplImage *frame = NULL;
	IplImage *img = NULL;
	IplImage *gray;
	IplImage *scaled_gray;
	IplImage *scaled_gray_Flip;

	int f1, f3, f2 = 0;
	CvPoint center1, center2, center3;
	CvSeq* faces1;
	CvSeq* faces2;
	CvSeq* faces3;

	cvNamedWindow( "FaceDetection", CV_WINDOW_AUTOSIZE );

	int ic = 0;

	while( 1 )
	{
		//IplImage *frame = NULL;
		printf("\n  FD   %d  ", ic++);
		faceHowMany = 0;
		yarp::sig::ImageOf<yarp::sig::PixelBgr> *frame1 = fdVideoIn.getImagePtr();
		frame = (IplImage*)frame1->getIplImage();

		if(frame == NULL || !cascade1 ) { Sleep(50);continue; }

		if ( img == NULL )
		{
			img = cvCreateImage( cvSize(frame->width,frame->height), IPL_DEPTH_8U, frame->nChannels );
			gray = cvCreateImage( cvSize(img->width,img->height), 8, 1 );
			scaled_gray = cvCreateImage( cvSize( cvRound (img->width/scale), cvRound (img->height/scale)), 8, 1 );
			scaled_gray_Flip = cvCreateImage( cvSize( cvRound (img->width/scale), cvRound (img->height/scale)), 8, 1 );
		}

		if( frame->origin == IPL_ORIGIN_TL ) cvCopy( frame, img, 0 );
		else  cvFlip( frame, img, 0 );

		cvCvtColor( img, gray, CV_BGR2GRAY );
		cvResize( gray, scaled_gray, CV_INTER_LINEAR );
		cvEqualizeHist( scaled_gray, scaled_gray );

		cvClearMemStorage( storage1 );
		faces1 = cvHaarDetectObjects( scaled_gray, cascade1, storage1, 1.1, 2, 0, cvSize(minsize, minsize) );
		f1 = (faces1 ? faces1->total : 0);
		if ( level > 1 && cascade3 ) //&& f1 < 1)
		{
			cvClearMemStorage( storage3 );
			faces3 = cvHaarDetectObjects( scaled_gray, cascade3, storage3, 1.1, 2, 0, cvSize(minsize, minsize) );
			f3 = (faces3 ? faces3->total : 0);
		}
		if ( level > 2 && cascade2 ) //&& f1 < 1 && f3 < 1)
		{
			cvClearMemStorage( storage2 );
			faces2 = cvHaarDetectObjects( scaled_gray, cascade2, storage2, 1.1, 2, 0, cvSize(minsize, minsize) );
			f2 = (faces2 ? faces2->total : 0);
			if ( f2 == 0 )
			{
				cvFlip( scaled_gray, scaled_gray_Flip, 1);
				faces2 = cvHaarDetectObjects( scaled_gray_Flip, cascade2, storage2, 1.1, 2, 0, cvSize(minsize, minsize) );
				f2 = (faces2 ? faces2->total : 0);
			}
		}

		for(int i = 0; i < f1; i++ )
		{
			CvRect* r1 = (CvRect*)cvGetSeqElem( faces1, i );
			center1.x = cvRound((r1->x + r1->width*0.5)*scale);
			center1.y = cvRound((r1->y + r1->height*0.5)*scale);
			int radius1 = cvRound((r1->width + r1->height)*0.25*scale);
			cvCircle( img, center1, radius1, cvScalar(0,0,255), 3, 8, 0 );

			xLocation[faceHowMany] = (double)(2*center1.x - img->width) / (img->width);
			yLocation[faceHowMany] = (double)(2*center1.y - img->height) / (img->height);
			faceHowMany++;
		}

		for(int i = 0; i < f3; i++ )
		{
			CvRect* r3 = (CvRect*)cvGetSeqElem( faces3, i );
			center3.x = cvRound((r3->x + r3->width*0.5)*scale);
			center3.y = cvRound((r3->y + r3->height*0.5)*scale);
			int radius3 = cvRound((r3->width + r3->height)*0.25*scale);
			cvCircle( img, center3, radius3, cvScalar(255,0,0), 3, 8, 0 );

			xLocation[faceHowMany] = (double)(2*center1.x - img->width) / (img->width);
			yLocation[faceHowMany] = (double)(2*center1.y - img->height) / (img->height);
			faceHowMany++;
		}

		for(int i = 0; i < f2; i++ )
		{
			CvRect* r2 = (CvRect*)cvGetSeqElem( faces2, i );
			center2.x = cvRound((r2->x + r2->width*0.5)*scale);
			center2.y = cvRound((r2->y + r2->height*0.5)*scale);
			int radius2 = cvRound((r2->width + r2->height)*0.25*scale);
			cvCircle( img, center2, radius2, cvScalar(0,255,0), 3, 8, 0 );

			xLocation[faceHowMany] = (double)(2*center1.x - img->width) / (img->width);
			yLocation[faceHowMany] = (double)(2*center1.y - img->height) / (img->height);
			faceHowMany++;
		}

		cvRectangle( img, cvPoint(cvRound(x * img->width), 0), cvPoint(cvRound((1-x) * img->width), img->height), CV_RGB(0,255,0), 1, 8, 0 );
		cvRectangle( img, cvPoint(0, cvRound(y * img->height)), cvPoint(img->width, cvRound((1-y) * img->height)), CV_RGB(0,255,0), 1, 8, 0 );
		cvShowImage( "FaceDetection", img );

		double data[15];
		data[0] = faceHowMany;
		for (int i=0; i<faceHowMany; i++)  
		{
			printf(" %.1lf ", xLocation[i]);
			data[2*i+1] = xLocation[i];
			data[2*i+2] = yLocation[i];
		}
		fdDataOut.PamIter(2*faceHowMany+1, data);
		if( cvWaitKey(20) == 27 ) break;
		//Sleep(50);
		//if( _kbhit() > 0 ) { if( _getch() == 0x1b ) break; } 
	}

	cvDestroyWindow( "FaceDetection" );
	cvReleaseImage( &frame );
	cvReleaseImage( &img );
	cvReleaseImage( &gray );
	cvReleaseImage( &scaled_gray );
	cvReleaseImage( &scaled_gray_Flip );
	return 0;
}


