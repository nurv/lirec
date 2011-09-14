//----------------------------------------------
// Heriot-Watt University
// MACS 
// www.lirec.eu
// author: Amol Deshmukh
// Date: 19/04/2011
//-----------------------------------------------

/* This is a standalone program. Pass an image name as a first parameter
of the program.  Switch between standard and probabilistic Hough transform
by changing "#if 1" to "#if 0" and back */
#include <cv.h>
#include <highgui.h>
#include <iostream>
#include <math.h>

double Rad2Deg(double x)
{
	return x*57.2957795;
};
double Deg2Rad(double x)
{
	return x/57.2957795;
};

int main(int argc, char** argv)
{
    IplImage* src;
    if( argc == 2 && (src=cvLoadImage(argv[1], 0))!= 0)
    {
        IplImage* dst = cvCreateImage( cvGetSize(src), 8, 1 );
        IplImage* color_dst = cvCreateImage( cvGetSize(src), 8, 3 );
        CvMemStorage* storage = cvCreateMemStorage(0);
        CvSeq* lines = 0;
        int i;
		// Run the edge detector algorithm on grey (set threshold to 120)
		cvCanny(src, dst, 500, 500, 3);
        //cvCanny( src, dst, 50, 200, 3 );
        cvCvtColor( dst, color_dst, CV_GRAY2BGR );
/*
#if 0
        lines = cvHoughLines2( dst,
                               storage,
                               CV_HOUGH_STANDARD,
                               1,
                               CV_PI/180,
                               100,
                               0,
                               0 );

        for( i = 0; i < MIN(lines->total,100); i++ )
        {
            float* line = (float*)cvGetSeqElem(lines,i);
            float rho = line[0];
            float theta = line[1];
            CvPoint pt1, pt2;
            double a = cos(theta), b = sin(theta);
            double x0 = a*rho, y0 = b*rho;
            pt1.x = cvRound(x0 + 1000*(-b));
            pt1.y = cvRound(y0 + 1000*(a));
            pt2.x = cvRound(x0 - 1000*(-b));
            pt2.y = cvRound(y0 - 1000*(a));
            cvLine( color_dst, pt1, pt2, CV_RGB(255,0,0), 3, 8 );
        }
#else*/
        /*lines = cvHoughLines2( dst,
                               storage,
                               CV_HOUGH_PROBABILISTIC,
                               1,
                               CV_PI/180,
                               80,
                               30,
                               10 );*/
/*
	rho – 	Distance resolution in pixel-related units
theta – Angle resolution measured in radians
threshold – Threshold parameter. A line is returned by the function if the corresponding accumulator value is greater than threshold

param1 –

The first method-dependent parameter:

    For the classical Hough transform it is not used (0).
    For the probabilistic Hough transform it is the minimum line length.
    For the multi-scale Hough transform it is the divisor for the distance resolution \rho . (The coarse distance resolution will be \rho and the accurate resolution will be (\rho / \texttt{param1}) ).

param2 –

The second method-dependent parameter:

    For the classical Hough transform it is not used (0).
    For the probabilistic Hough transform it is the maximum gap between line segments lying on the same line to treat them as a single line segment (i.e. to join them).
    For the multi-scale Hough transform it is the divisor for the angle resolution \theta . (The coarse angle resolution will be \theta and the accurate resolution will be (\theta / \texttt{param2}) ).

*/
		//10, CV_PI/180, 100, 100, 1000);

		lines = cvHoughLines2( dst, storage,CV_HOUGH_PROBABILISTIC,1, CV_PI/180, 20, 70, 10 );
        for( i = 0; i < lines->total; i++ )
        {
            CvPoint* line = (CvPoint*)cvGetSeqElem(lines,i);
            cvLine(color_dst, line[0], line[1], CV_RGB(255,0,0), 3, 8 );
			double X1 = (double)line[0].x;
			double Y1 = (double)line[0].y;

			double X2 = (double)line[1].x;
			double Y2 = (double)line[1].y;

			double ang = atan2(Y2-Y1,X2-X1);
			//calculate_distance(dX, dY, GoalPose.getX(), GoalPose.getY());//ar.findDistanceTo(ArPose(-0.75,3.0,4.5));
			//ang-=dTh;

			//while(ang>3.14159265){ang-=6.28318531;}  // normalise PI ie if over 180, then minus 360
			//while(ang<-3.14159265){ang+=6.28318531;} 

			
			//atan2(GoalPose.getY() - CurrentPose.getY(), GoalPose.getX() - CurrentPose.getX());
			std::cout << "ang rad " << ang << "deg ang " << Rad2Deg(ang) <<std::endl;
			
        }
//#endif
        cvNamedWindow( "Source", 1 );
        cvShowImage( "Source", src );

        cvNamedWindow( "Hough", 1 );
        cvShowImage( "Hough", color_dst );

		std::cout << "total lines " << lines->total << std::endl;

        cvWaitKey(0);
    }
}
/*
#include "SamClass.h"

#include <memory>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <math.h>
#include <float.h>
#include <limits.h>
#include <time.h>
#include <ctype.h>
#include "cv.h"
#include "highgui.h"
#include "cvaux.h"
#include "cxcore.h"
#include <iostream>



int Faceloop=0;
int Loopcnt=0;
static CvMemStorage* storage = 0;
static CvHaarClassifierCascade* cascade = 0;

const char* cascade_name;
CvCapture* capture = 0;
IplImage *frame, *frame_copy = 0;
IplImage *image = 0;

bool m_bflagShowResult = true;
int m_dMidX = 0;
int m_dMidY = 0;
int m_iNumFaces = 0;
bool m_bFaceDetected = false;
int m_bUserProximicFlag = 0;
double m_dScale = 1.2;
int DetectSubFace(IplImage* cvTempimage);
void DetectAndDraw(IplImage* img, double m_dScale);
void rotate(IplImage* image, IplImage* rotatedImage, float angle);
using namespace std;

class FaceTrack : public SamClass 
{
	private:
	BufferedPort<Bottle> bTracksender; // create buffered ports for bottles like this
	Network yarp;						   // make sure the network is ready
	

	public:

	void SamInit(void)
	{
	
		RecognisePort("TrackOut");				// name the port to be shown in the gui
		StartModule("/Face");	
		bTracksender.open("/Face_TrackOut");		// open the port
	
		bTracksender.setReporter(myPortStatus);	// set reporter, this is important

    	puts("started face tracker");
	}

	void SamIter(void)
	{
		Bottle& B = bTracksender.prepare();		// prepare the bottle/port
		B.clear();
		B.addInt(m_dMidX );
		B.addInt(m_dMidY);
		B.addInt(m_bUserProximicFlag);
		bTracksender.writeStrict();				// add stuff then send
	}
	
};

int main() 
{
	
	FaceTrack fTrack;
	fTrack.SamInit();
	  //ShowWindow( GetConsoleWindow(), SW_HIDE );

	  cvNamedWindow( "result", CV_WINDOW_AUTOSIZE );
    
	  cascade_name = "haarcascade_frontalface_alt.xml";
	  cascade = (CvHaarClassifierCascade*)cvLoad( cascade_name, 0, 0, 0 );
	  storage = cvCreateMemStorage(0);
			    
	if( !cascade )
	{
		   fprintf( stderr, "ERROR: Could not load classifier cascade\n" );
		   fprintf( stderr,"Usage: facedetect --cascade=\"<cascade_path>\" [filename|camera_index]\n" );
	}

	// OCV get a device
	CvCapture* capture = cvCaptureFromCAM( CV_CAP_ANY );

	// OCV make sure its there
	if( !capture ) {fprintf( stderr, "ERROR: capture is NULL \n" );getchar();return -1;}


	  while( 1 ) 
	  {
	 	Loopcnt++;
		IplImage* frame = cvQueryFrame( capture );
		if( !frame ){fprintf( stderr, "ERROR: frame is null...\n" );getchar();break;}
	
		
		if(frame!=false) // if there is no image available (one hasn't been sent) then the image will be false
		{
			
			if( !frame_copy )
				frame_copy = cvCreateImage(cvSize(frame->width,frame->height) ,
											IPL_DEPTH_8U, frame->nChannels );//frame->nChannels

			//if( frame->origin == IPL_ORIGIN_TL )
			//	cvCopy( frame, frame_copy, 0 );
			//else
			//	cvFlip( frame, frame_copy, 0 );
			

		    rotate(frame, frame_copy, 90);
			//cvCopy( frame, frame_copy, 0 );

			Sleep(100);


			DetectAndDraw( frame_copy, m_dScale );
			//cvShowImage( "result", frame_copy );

			//check if 5 seconds have elapsed
			if(Loopcnt>50 || Faceloop>5)
			{
				//face detected more than 5 times
				//if(Faceloop>5)
					fTrack.SamIter();

				Faceloop=0;
				Loopcnt=0;
				
			}
			
		
			if( cvWaitKey( 10 ) >= 0 )
				goto _cleanup_;

			
		}

		if( (cvWaitKey(10) & 255) == 27 ) break;

		
	
	 }//end while
		
	  cvWaitKey(0);
	  _cleanup_:
	  cvReleaseImage( &frame_copy );
	  cvReleaseImage( &frame );
	  cvReleaseCapture( &capture );
	  cvDestroyWindow( "result" );
	  return 0;
}


void DetectAndDraw(IplImage* img, double m_dScale)
{
	m_bFaceDetected = false;
	m_bUserProximicFlag = 0;
	m_dMidX = 0;
	m_dMidY = 0;

    IplImage *gray, *small_img;
    int i, j;

    gray = cvCreateImage( cvSize(img->width,img->height), 8, 1 );
    small_img = cvCreateImage( cvSize( cvRound (img->width/m_dScale),
                         cvRound (img->height/m_dScale)), 8, 1 );

    cvCvtColor( img, gray, CV_BGR2GRAY );
    cvResize( gray, small_img, CV_INTER_LINEAR );
    cvEqualizeHist( small_img, small_img );
    cvClearMemStorage( storage );

	double t = (double)cvGetTickCount();
	    
    if( cascade )
    {
        double t = (double)cvGetTickCount();
        CvSeq* faces = cvHaarDetectObjects( small_img, cascade, storage,
                                            m_dScale, 3, 0
                                            |CV_HAAR_FIND_BIGGEST_OBJECT
                                            //|CV_HAAR_DO_ROUGH_SEARCH
                                            //|CV_HAAR_DO_CANNY_PRUNING
                                            //|CV_HAAR_SCALE_IMAGE
                                            , cvSize(20, 20) );

        t = (double)cvGetTickCount() - t;
       	//printf( "detection time = %gms\n", t/((double)cvGetTickFrequency()*1000.) );

     	if(faces->total > 0)
	    {
			CvRect* r = (CvRect*)cvGetSeqElem( faces, 0 );
			int radius;
			CvPoint center;
			center.x = cvRound((r->x + r->width*0.5)*m_dScale);
			center.y = cvRound((r->y + r->height*0.5)*m_dScale);
			radius = cvRound((r->width + r->height)*0.25*m_dScale);

			//cutout the portion which is detected as face 
         
			cvSetImageROI(img, cvRect(cvRound(r->x*m_dScale),
	    	cvRound(r->y*m_dScale),cvRound(r->width*m_dScale),
	    	cvRound(r->height*m_dScale)));
	    		
           
			// redetect face for more accuracy
			if(DetectSubFace(img))
			{
				Faceloop++;
				cvResetImageROI(img);
				
				//face detected x,y positions on image
				m_dMidX = (img->width/2) - center.x;
				m_dMidY = (img->height/2) - center.y;
			
				//face detect flag set to true
				m_bFaceDetected = true;  

				CvPoint pt1, pt2; //Create a point to represent the face locations 
				pt1.x = r->x * m_dScale;
				pt2.x = (r->x + r->width) * m_dScale;
				pt1.y = r->y * m_dScale;
				pt2.y = (r->y + r->height) * m_dScale;

				cvRectangle( img, pt1, pt2, CV_RGB(255,0,0), 3, 8, 0 );

				// Calculate the area of the face bounding box
				int areaFace = 0;
				int areaImage = 0;
				
				//calculate difference in area of face bounding box and image size
				areaFace = r->height * r->width;  
				areaImage = img->width * img->height;  
				int areaDiff = areaImage - areaFace;
				int facepercentage =  (areaFace * 100) / areaImage;
	  
				//printf("Face Percentage %d \n",facepercentage);

				// set flag (m_bUserProximicFlag) if face  detected at threshold distance 80cm-100 cm 
				//(camera and resolution dependent) may have to change the facepercentage according to camera

				if(facepercentage > 12)
				   m_bUserProximicFlag = 1;
				else
				   m_bUserProximicFlag = 0;

				//printf("Face proxemics %d \n",m_bUserProximicFlag);
				std::cout << " X " << m_dMidX << " Y " << m_dMidY <<std::endl;
					
				
			}
			
		
		cvResetImageROI(img); // ... and remove the ROI 

		}
	 
    }

	cvShowImage( "result", img );

    cvReleaseImage( &gray );
    cvReleaseImage( &small_img );
	
}

//-------------------------------------------------------------

int DetectSubFace(IplImage* cvTempimage)
{
	//detect face in sub image, to have more confidence in detected face
	CvSeq* SubFaces = cvHaarDetectObjects( cvTempimage, cascade, storage,
					    m_dScale, 3, 0 |CV_HAAR_DO_CANNY_PRUNING,
					    cvSize(0, 0) );

	//if face found                          
	if(SubFaces->total == 1)
	   return 1;
	else
	   return 0;

}

void rotate(IplImage* image, IplImage* rotatedImage, float angle) {

//IplImage *rotatedImage = cvCreateImage(cvSize(image->width,image->height) , IPL_DEPTH_8U,image->nChannels);

CvPoint2D32f center;
center.x = image->width/2.0;
center.y = image->height/2.0;
CvMat *mapMatrix = cvCreateMat( 2, 3, CV_32FC1 );

cv2DRotationMatrix(center, angle, 1.0, mapMatrix);
cvWarpAffine(image, rotatedImage, mapMatrix, CV_INTER_LINEAR + CV_WARP_FILL_OUTLIERS, cvScalarAll(0));

//cvReleaseImage(&image);
cvReleaseMat(&mapMatrix);

//return rotatedImage;
}
*/