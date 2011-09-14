//include files

#include "cv.h"

#include "highgui.h"

#include "math.h"

#include <iostream>

#include <stdio.h>

#include <math.h>

#include <string.h>

#include <conio.h>
#include "SamClass.h"

//http://www.andol.info/wp-content/uploads/2009/02/circledetect.cpp

using namespace std;



//declarations

int thresh = 50;

double dMidX =0;

int bProximicFlag = 0;

IplImage* img = 0;

IplImage* frame = 0;


IplImage *src = 0; IplImage *frame_copy=0;


int px[2], py[2], pr[2];
int edge_thresh = 1;
CvMemStorage* cstorage = 0;



void processImage(IplImage* img);


//main function start


int main() 
{
	
		cstorage = cvCreateMemStorage(0);
		cvNamedWindow("src", CV_WINDOW_AUTOSIZE );

  while( 1 ) 
  {
	IplImage *frame2 = CircleDetect.RecivePictureOCVNative();
	bot1.clear();
	bProximicFlag = 0;
	dMidX = 0;

	if(frame2!=false) // if there is no image available (one hasn't been sent) then the image will be false
	{
    
		processImage(frame2);
		//cvShowImage( "copy", frame2 ); // if its not false then display it.

		bot1.addInt(bProximicFlag);
		bot1.addDouble(dMidX);

		if(dMidX!=0)
			CircleDetect.SendBottleData("CircleOut",bot1);

		yarp::os::Time::delay(0.01);
	}

    if( (cvWaitKey(10) & 255) == 27 ) break;
  }


  cvDestroyWindow( "copy" );
  return 0;
}

/*

int main (int argc, char** argv ){

		Network yarp;
		SamgarModule CircleDetect("CircleDetect","CircleDetect","CircleDetect",running); // Cant have spaces or underscores
		CircleDetect.SetupImagePort("VideoIn");
		CircleDetect.AddPortS("CircleOut");

        cstorage = cvCreateMemStorage(0);

        //fstorage = cvCreateMemStorage(0);

      

        //get the video from webcam

        //CvCapture* capture=cvCaptureFromCAM(0);

        //cvNamedWindow("csrc",1);

        cvNamedWindow("src",1);

    

		Bottle bot1;

        //loop start here

        while(1)
		{
			bot1.clear();
			bProximicFlag = 0;
			dMidX = 0;
			frame = CircleDetect.RecivePictureOCVNative();

			if(frame!=false) // if there is no image available (one hasn't been sent) then the image will be false
			{
				frame_copy = cvCreateImage(cvSize(frame->width,frame->height) ,
											IPL_DEPTH_8U, frame->nChannels );
				
				if( frame->origin == IPL_ORIGIN_TL )
					cvCopy( frame, frame_copy, 0 );
				else
					cvFlip( frame, frame_copy, 0 );

				processImage(frame);
				//cvShowImage("src",frame);
				
				//ready to exit loop
				

				bot1.addInt(bProximicFlag);
				bot1.addDouble(dMidX);

				CircleDetect.SendBottleData("CircleOut",bot1);

				yarp::os::Time::delay(0.1);
				//if( cvWaitKey( 10 ) >= 0 )
				//goto _cleanup_;
       
			}    
			//if( (cvWaitKey(10) & 255) == 27 ) break;

	
        }

     

		//cvReleaseImage(frame);

        //release video capture

       // cvReleaseCapture( &capture);

        //release all windows

		// cvWaitKey(0);
		//_cleanup_:
		//cvReleaseImage( &frame );

        cvDestroyAllWindows();

		return 0;

}
*/

void processImage(IplImage* img)
{
		
		src = img;
        //csrc=cvCloneImage(src);
		//cvShowImage("csrc",src);

        //convert video image color
	

        IplImage *edge = cvCreateImage( cvSize(640,480), 8, 1 );

		IplImage *gray = cvCreateImage( cvSize(640,480), 8, 1 );

        cvCvtColor(src,gray,CV_BGR2GRAY);

        //set the converted image's origin

        gray->origin=1;

        //color threshold

        cvThreshold(gray,gray,100,255,CV_THRESH_BINARY);

        //smooth the image to reduce unneccesary results

        cvSmooth( gray, gray, CV_GAUSSIAN, 11, 11 );

	

        //get edges

        cvCanny(gray, edge, (float)edge_thresh, (float)edge_thresh*3, 5);

		//cvShowImage("csrc",gray);

        //get circles http://www.emgu.com/wiki/files/1.3.0.0/html/0ac8f298-48bc-3eee-88b7-d2deed2a285d.htm
		//CvSeq* circles =  cvHoughCircles( gray, cstorage, CV_HOUGH_GRADIENT, 2, gray->height/50, 5, 35 );


		CvSeq* circles =  cvHoughCircles( gray, cstorage, CV_HOUGH_GRADIENT, 1, gray->width/20, 5, 40 );

        //output all the circle detected

        cout << circles->total <<endl;

        //start drawing all the circles

        int i;
		//circles->total>=2?i<2:i < circles->total
		float* p;
        for( i = 0; i<2; i++ ){ //just make a filter to limit only <=2 ciecles to draw

             p = (float*)cvGetSeqElem( circles, i );                 

             px[i]=cvRound(p[0]); py[i]=cvRound(p[1]); pr[i] = cvRound(p[2]);

        }

       //start drawing a rectangle between circles detected
		
		if(abs(pr[0] - pr[1]) < 20 && abs(py[0] - py[1]) < 25 && circles->total>=2)
		{
			cvCircle( src, cvPoint(cvRound(p[0]),cvRound(p[1])), 3, CV_RGB(255,0,0), -1, 8, 0 );
            cvCircle( src, cvPoint(cvRound(p[0]),cvRound(p[1])), cvRound(p[2]), CV_RGB(200,0,0), 1, 8, 0 );

			cvRectangle( src, cvPoint(px[0],py[0]), cvPoint(px[1],py[1]), cvScalar( (0), (0), (201) ), -1, CV_AA, 0 );
			
			//output two circles' center position

			cout <<"px0"<<px[0]<<" py0 "<<py[0] <<" pr0 "<<pr[0] <<endl;
			cout <<"px1"<<px[1]<<" py1 "<<py[1] <<" pr1 "<<pr[1] <<endl;
			
			dMidX = (src->width/2) - ((px[1]+px[0])/2);
			int avgRadius = (pr[1] + pr[0])/2; 

			cout <<"mid point "<< dMidX  <<endl;
			cout <<"radius "<< avgRadius  <<endl;

			if(avgRadius<=80)
				bProximicFlag = 150;
			else
				bProximicFlag = 0;

		}
        
        cvShowImage("src",src);

}

