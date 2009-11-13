//----------------------------------------------
// Heriot-Watt University
// MACS 
// www.lirec.eu
// author: Amol Deshmukh
// Date: 17/03/2009
//-----------------------------------------------



#include "FaceDetect.h"
#include <math.h>


static CvMemStorage* storage = 0;
static CvHaarClassifierCascade* cascade = 0;

const char* cascade_name;
CvCapture* capture = 0;
IplImage *frame, *frame_copy = 0;
IplImage *image = 0;


//-------------------------------------------------------------

FaceDetect::FaceDetect()
{	
	m_dScale = 1.20;
	m_bflagShowResult = true;
	m_dMidX = 0;
	m_iNumFaces = 0;
	m_bFaceDetected = false;
	m_bUserProximicFlag = false;
	
	
	cascade_name = "haarcascade_frontalface_alt.xml";
	cascade = (CvHaarClassifierCascade*)cvLoad( cascade_name, 0, 0, 0 );
	storage = cvCreateMemStorage(0);
		    
	if( !cascade )
	{
	   fprintf( stderr, "ERROR: Could not load classifier cascade\n" );
	   fprintf( stderr,
	   "Usage: facedetect --cascade=\"<cascade_path>\" [filename|camera_index]\n" );
	}

	if(m_bflagShowResult)
           cvNamedWindow( "result", 1 );


}

//-------------------------------------------------------------

FaceDetect::~FaceDetect()
{
	cvDestroyWindow("result");
		
}

//-------------------------------------------------------------

void FaceDetect::StartFaceDetection(void)
{
	capture = cvCaptureFromCAM(0);

	

    if( capture )
    {
        for(;;)
        {
            frame = cvQueryFrame( capture );
            if( !frame )
   		break;
            if( !frame_copy )
                frame_copy = cvCreateImage( cvSize(frame->width,frame->height),
                                            IPL_DEPTH_8U, frame->nChannels );
            if( frame->origin == IPL_ORIGIN_TL )
                cvCopy( frame, frame_copy, 0 );
            else
                cvFlip( frame, frame_copy, 0 );

            DetectAndDraw( frame_copy, m_dScale );
	
            if( cvWaitKey( 10 ) >= 0 )
                goto _cleanup_;
		
        }

        cvWaitKey(0);
	_cleanup_:
        cvReleaseImage( &frame_copy );
        cvReleaseCapture( &capture );
    }
    else
    {
        printf("Failed to capture from camera\n");
    }
         
}

//-------------------------------------------------------------

void FaceDetect::DetectAndDraw(IplImage* img, double m_dScale)
{

    IplImage *gray, *small_img;
    int i, j;

    gray = cvCreateImage( cvSize(img->width,img->height), 8, 1 );
    small_img = cvCreateImage( cvSize( cvRound (img->width/m_dScale),
                         cvRound (img->height/m_dScale)), 8, 1 );

    cvCvtColor( img, gray, CV_BGR2GRAY );
    cvResize( gray, small_img, CV_INTER_LINEAR );
    cvEqualizeHist( small_img, small_img );
    cvClearMemStorage( storage );

  

    
    if( cascade )
    {
        double t = (double)cvGetTickCount();
        CvSeq* faces = cvHaarDetectObjects( small_img, cascade, storage,
                                            m_dScale, 3, 0
                                            |CV_HAAR_FIND_BIGGEST_OBJECT
                                            //|CV_HAAR_DO_ROUGH_SEARCH
                                            //|CV_HAAR_DO_CANNY_PRUNING
                                            //|CV_HAAR_SCALE_IMAGE
                                            ,
                                            cvSize(20, 20) );
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
					
					
			m_dMidX = (img->width/2) - center.x;
			m_dMidY = (img->height/2) - center.y;
		
			//face detect flag set to true
			m_bFaceDetected = true;  

			//calculate angles from detected face
			m_dAngleX = m_dMidX * (180.0/img->width);
			m_dAngleY = m_dMidY * (180.0/img->height);

			//std::cout << "Face Angle X " << m_dAngleX  << std::endl;
			//std::cout << "Face Angle Y " << m_dAngleY << std::endl;
			
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
  
                        //std::cout << "Area difference "<< areaDiff << std::endl;
			
			// set flag if face  detected at threshold distance 80cm-100 cm (camera and resolution dependent)
			// resolution required 640 * 480 (can be modified acc to requirement)
			if(areaDiff < 297000)
			{			
			   m_bUserProximicFlag = true;
			   std::cout << "User proximity 80cm to 100 cm "<< std::endl;
			}
			else
			{
    			    m_bUserProximicFlag = false;
			   
			}
			
			
		}
		

		cvResetImageROI(img); // ... and remove the ROI 
		m_iNumFaces = faces->total;

        }
	
	 
    }

  if(m_bflagShowResult)
   cvShowImage( "result", img );

    cvReleaseImage( &gray );
    cvReleaseImage( &small_img );
}

//-------------------------------------------------------------

int FaceDetect::DetectSubFace(IplImage* cvTempimage)
{
	//detect face in sub image
	CvSeq* SubFaces = cvHaarDetectObjects( cvTempimage, cascade, storage,
					    m_dScale, 3, 0 |CV_HAAR_DO_CANNY_PRUNING,
					    cvSize(0, 0) );

	//if face found                          
	if(SubFaces->total == 1)
	   return 1;
	else
	   return 0;

}
//-------------------------------------------------------------

int main( int argc, char** argv )
{
  
    FaceDetect face;
   
    //call face detection 
    face.StartFaceDetection();


    printf("program working\n");
    return 0;
    
}

