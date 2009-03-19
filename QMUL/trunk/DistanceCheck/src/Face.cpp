/* 
AUTHOR: Ginevra Castellano
Queen Mary University of London
DATE: 03/2009
VERSION: 1.0
*/

// This code is based on the face detection code provided by OpenCV

#include "stdafx.h"

#include "cv.h"
#include "highgui.h"

#include <stdio.h>

#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <math.h>
#include <float.h>
#include <limits.h>
#include <time.h>
#include <ctype.h>

#include <vector> 

#include "distanceCheck.h"

#ifdef _EiC
#define WIN32
#endif

// Create memory for calculations
static CvMemStorage* storage = 0;

// Create a new Haar classifier
static CvHaarClassifierCascade* cascade = 0;

// Function prototype for detecting and drawing an object from an image 
// and returning the area of the bounding box

int detect_and_draw(IplImage* image); 

// Create a string that contains the cascade name
const char* cascade_name =
    "haarcascade_frontalface_alt.xml";

// Main function, defines the entry point for the program


int main( int argc, char** argv )
{
	// Structure for getting video from camera or avi
	CvCapture* capture = 0;

	// Images to capture the frame from video or camera or from file
    IplImage *frame, *frame_copy = 0;

	// Used for calculations
    int optlen = strlen("--cascade=");

	// Input file name for avi or image file
    const char* input_name;

	// Check for the correct usage of the command line
	if( argc > 1 && strncmp( argv[1], "--cascade=", optlen ) == 0 )
    {
        cascade_name = argv[1] + optlen;
        input_name = argc > 2 ? argv[2] : 0;
    }
    else
    {
        cascade_name = "C:/Program Files/OpenCV/data/haarcascades/haarcascade_frontalface_alt.xml";
       input_name = argc > 1 ? argv[1] : 0;
    }

	// Load the HaarClassifierCascade
    cascade = (CvHaarClassifierCascade*)cvLoad( cascade_name, 0, 0, 0 );

	 // Check whether the cascade has loaded successfully. Else report an error and quit
    if( !cascade )
    {
        fprintf( stderr, "ERROR: Could not load classifier cascade\n" );
        fprintf( stderr,
        "Usage: facedetect --cascade=\"<cascade_path>\" [filename|camera_index]\n" );
        return -1;
    }

	// Allocate the memory storage
    storage = cvCreateMemStorage(0);
    
	// Find whether to detect the object from file or from camera
    if( !input_name || (isdigit(input_name[0]) && input_name[1] == '\0') )
        capture = cvCaptureFromCAM( !input_name ? 0 : input_name[0] - '0' );
    else
        capture = cvCaptureFromAVI( input_name ); 

	// Create a new named window with title: result
    cvNamedWindow( "result", 1 );

	// Object distanceCheck of the class "DistanceCheck"
    DistanceCheck *distanceCheck = new DistanceCheck(4); 
	
	int area = 0; // Area of the face bounding box
	
    // Find if the capture is loaded successfully or not
	// If loaded succesfully, then:

    if( capture )
    {
		// Capture from the camera (or avi file)
        for(;;)
        {

			// Capture the frame and load it in IplImage
            if( !cvGrabFrame( capture ))
                break;
            frame = cvRetrieveFrame( capture );

            
			// If the frame does not exist, quit the loop
			if( !frame )
                break;

			// Allocate framecopy as the same size of the frame
            if( !frame_copy )
                frame_copy = cvCreateImage( cvSize(frame->width,frame->height),
                                            IPL_DEPTH_8U, frame->nChannels );

			// Check the origin of image. If top left, copy the image frame to frame_copy. 
            if( frame->origin == IPL_ORIGIN_TL )
                cvCopy( frame, frame_copy, 0 );
            
			// Else flip and copy the image
			else
                cvFlip( frame, frame_copy, 0 );
            
            // Call the function to detect and draw the face and return the area of the face bounding box
            area = detect_and_draw(frame_copy);

            // Call the function "addFaceVal" of the class "DistanceCheck" to store the values of the area of the bounding box in a vector
			distanceCheck->addFaceVal(area); 

			// Call the function "getMovementType" of the class "DistanceCheck" to predict whether the user is staying still, approaching the camera or withdrawing
			switch (distanceCheck->getMovementType()) 
			{	
				case UNDEFINED:
					printf("\nUndefined");
					break;
			    case STAYING_STILL:
					printf("\nStaying still");
					break;
			    case APPROACHING: 
					printf("\nApproaching");
					break;
				case WITHDRAWING:
					printf("\nWithdrawing");
					break;
			}


			// Wait for a while before proceeding to the next frame
            if( cvWaitKey( 10 ) >= 0 )
                break;
        }

		// Release the images, and capture memory
        cvReleaseImage( &frame_copy );
        cvReleaseCapture( &capture );
    }

	delete distanceCheck; 

    // Destroy the window previously created with filename: "result"
    cvDestroyWindow("result");

    return 0;
}



// Function to detect and draw any faces that is present in an image
// It returns the area of the face bounding box 

int detect_and_draw( IplImage* img ) 

{
    int area = 0; 

    static CvScalar colors[] = 
    {
        {{0,0,255}},
        {{0,128,255}},
        {{0,255,255}},
        {{0,255,0}},
        {{255,128,0}},
        {{255,255,0}},
        {{255,0,0}},
        {{255,0,255}}
    };

    double scale = 1.3;

	
	// Create a new image based on the input image

    IplImage* gray = cvCreateImage( cvSize(img->width,img->height), 8, 1 );
    IplImage* small_img = cvCreateImage( cvSize( cvRound (img->width/scale),
                         cvRound (img->height/scale)), 8, 1 );
    int i;

    cvCvtColor( img, gray, CV_BGR2GRAY );
    cvResize( gray, small_img, CV_INTER_LINEAR );
    cvEqualizeHist( small_img, small_img );
    
	
	// Clear the memory storage which was used before
	cvClearMemStorage( storage );


	// Find whether the cascade is loaded, to find the faces. If yes, then:
    if( cascade )
    {
        double t = (double)cvGetTickCount();


		// There can be more than one face in an image. So create a growable sequence of faces.
        // Detect the objects and store them in the sequence
       CvSeq* faces = cvHaarDetectObjects( small_img, cascade, storage,
                                            1.1, 2, CV_HAAR_FIND_BIGGEST_OBJECT,
                                            cvSize(30, 30) );
		


        t = (double)cvGetTickCount() - t;
		int radius = 0;
        printf( "detection time = %gms\n", t/((double)cvGetTickFrequency()*1000.) );

		// Loop the number of faces found.
        for( i = 0; i < (faces ? faces->total : 0); i++ )
        {
			// Create a new rectangle for drawing the face
            CvRect* r = (CvRect*)cvGetSeqElem( faces, i );  
            
			// Create a point to represent the face locations 
			CvPoint center;
            radius = 0;

			CvPoint pt1, pt2; //Create a point to represent the face locations 

			// Find the dimensions of the face,and scale it if necessary
        

			pt1.x = r->x * scale;
            pt2.x = (r->x + r->width) * scale;
            pt1.y = r->y * scale;
            pt2.y = (r->y + r->height) * scale;

            cvRectangle( img, pt1, pt2, CV_RGB(255,0,0), 3, 8, 0 );

			// Calculate the area of the face bounding box

			area = abs(pt2.x-pt1.x)*abs(pt2.y-pt1.x);  
        }
    }

	// Show the image in the window named "result"
    cvShowImage( "result", img );
    
	// Release the images created.
	cvReleaseImage( &gray );
    cvReleaseImage( &small_img );
	
	return area;  
}
