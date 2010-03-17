//----------------------------------------------
// Heriot-Watt University
// MACS 
// www.lirec.eu
// author: Amol Deshmukh
// Date: 10/03/2010
//-----------------------------------------------

#include <memory>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <math.h>
#include <float.h>
#include <limits.h>
#include <time.h>
#include <ctype.h>
#include <iostream>
#include "cv.h"
#include "highgui.h"
#include "cvaux.h"
#include <SamgarMainClass.h>

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


int main() 
{
	  Network yarp;
	  SamgarModule CameraRecive("FaceFinder","FaceDetect","FaceDetect",run); // Cant have spaces or underscores
	  CameraRecive.SetupImagePort("VideoIn");
	  CameraRecive.AddPortS("Out");


	  cvNamedWindow( "result", CV_WINDOW_AUTOSIZE );
    
	  cascade_name = "C:\\Program Files\\OpenCV\\data\\haarcascades\\haarcascade_frontalface_alt.xml";
	  cascade = (CvHaarClassifierCascade*)cvLoad( cascade_name, 0, 0, 0 );
	  storage = cvCreateMemStorage(0);
			    
	if( !cascade )
	{
		   fprintf( stderr, "ERROR: Could not load classifier cascade\n" );
		   fprintf( stderr,"Usage: facedetect --cascade=\"<cascade_path>\" [filename|camera_index]\n" );
	}

	Bottle bot1;


	  while( 1 ) 
	  {
		IplImage *frame = CameraRecive.RecivePictureOCVNative();

		if(frame!=false) // if there is no image available (one hasn't been sent) then the image will be false
		{
			
			if( !frame_copy )
				frame_copy = cvCreateImage(cvSize(frame->width,frame->height) ,
											IPL_DEPTH_8U, frame->nChannels );
			if( frame->origin == IPL_ORIGIN_TL )
				cvCopy( frame, frame_copy, 0 );
			else
				cvFlip( frame, frame_copy, 0 );

			DetectAndDraw( frame_copy, m_dScale );

			
				bot1.addInt(m_bFaceDetected);
				bot1.addInt(m_bUserProximicFlag);
				bot1.addDouble(m_dMidX);
				CameraRecive.SendBottleData("Out",bot1);
			
			if( cvWaitKey( 10 ) >= 0 )
				goto _cleanup_;

			bot1.clear();
		}

		if( (cvWaitKey(10) & 255) == 27 ) break;
	
	
	 }
		
	  cvWaitKey(0);
	  _cleanup_:
	  cvReleaseImage( &frame_copy );

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
	  
				printf("Face Percentage %d \n",facepercentage);

				// set flag (m_bUserProximicFlag) if face  detected at threshold distance 80cm-100 cm 
				//(camera and resolution dependent) may have to change the facepercentage according to camera

				if(facepercentage > 12)
				   m_bUserProximicFlag = 1;
				else
				   m_bUserProximicFlag = 0;

				printf("Face proxemics %d \n",m_bUserProximicFlag);
				printf("Face side %d \n",m_dMidX);
					
				
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





















/*
if(facepercentage > 12 && facepercentage <= 12)
{
   m_bUserProximicFlag = 1;
}
else if (facepercentage > 12)
{   
	m_bUserProximicFlag = -1;
}
else
	m_bUserProximicFlag = 0;

*/