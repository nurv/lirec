// AUTHOR: Ginevra Castellano
// Queen Mary University of London
// DATE: 10/2009
// VERSION: 1.0

// Copyright (C) 2009 Ginevra Castellano

// This file is part of the FaceTracking program

// FaceTracking is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

// FaceTracking uses the OpenCV library
// Copyright (C) 2000-2006, Intel Corporation, all rights reserved.
// Third party copyrights are property of their respective owners.
// See OpenCV_license.txt, in the program folder, for details.

// FaceTracking uses the Camshift wrapper program (see camshift_wrapper.cpp and camshift_wrapper.h)
// Copyright (c) 2007, Robin Hewitt (http://www.robin-hewitt.com)
// See License.txt, in the Camshift wrapper folder, for details.


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

#include "camshift_wrapper.h"

#include "CaptureFrame.h"
#include "FaceDetection.h"


#ifdef _EiC
#define WIN32
#endif


// Function definition
CvRect* waitForFaceDetect(FaceDetection* fD, CaptureFrame* cF); 



int main( int argc, char** argv )
{
	// Store the first detected face
	CvRect *pFaceRect = NULL;

	// Store the tracked face
	CvRect nextFaceRect;
	//CvBox2D faceBox; 

	// Points to draw the face rectangle
	CvPoint pt1 = cvPoint(0,0);
	CvPoint pt2 = cvPoint(0,0);
	
	char c = 0;  

	// Object faceDetection of the class "FaceDetection"
    FaceDetection faceDetection;

	// Object captureFrame of the class "CaptureFrame"
	CaptureFrame captureFrame; 

	// Create a new window 
    cvNamedWindow("tracked face", 1);

	printf("\nPress r to re-initialise tracking");

	// Capture from the camera
	captureFrame.StartCapture();

	bool finished = captureFrame.CaptureNextFrame(); // capture into frameCopy
	if (finished) // if video is finished
	 {
	   captureFrame.DeallocateFrames();
	   releaseTracker();
	   cvDestroyWindow("tracked face");
	   return 0;
	 }
		      
    // Create the tracker
    if(!createTracker(captureFrame.getFrameCopy())) 
	   fprintf( stderr, "ERROR: tracking initialisation\n" );

	// Set Camshift parameters
	setVmin(30);
	setSmin(20);

	// Capture video until a face is detected
	pFaceRect = waitForFaceDetect(&faceDetection, &captureFrame);
	// Start tracking
	if (pFaceRect == NULL)
	{
	 captureFrame.DeallocateFrames();
	 releaseTracker();
	 // Destroy the window previously created
	 cvDestroyWindow("tracked face");
	 return 0;
	}
	// Start tracking
	startTracking(captureFrame.getFrameCopy(), pFaceRect);


	// Track the detected face using CamShift
	while(1)
	{
		finished = captureFrame.CaptureNextFrame(); //capture to frameCopy
		
		if (finished) 
		{
	      captureFrame.DeallocateFrames();
		  releaseTracker();
		  cvDestroyWindow("tracked face");
		  return 0;
		}
			 
		// Track the face in the new video frame
		nextFaceRect = track(captureFrame.getFrameCopy());
		//faceBox = track(captureFrame.getFrameCopy());

		pt1.x = nextFaceRect.x;
        pt1.y = nextFaceRect.y;
        pt2.x = pt1.x + nextFaceRect.width;
        pt2.y = pt1.y + nextFaceRect.height;

		// Draw face rectangle
		cvRectangle(captureFrame.getFrameCopy(), pt1, pt2, CV_RGB(255,0,0), 3, 8, 0 );

		// Draw face ellipse
		//cvEllipseBox(captureFrame.getFrameCopy(), faceBox,
		             //CV_RGB(255,0,0), 3, CV_AA, 0 );

		cvShowImage("tracked face", captureFrame.getFrameCopy());
			
		c = cvWaitKey(100);
		switch(c)
		{		
			case 27: break;
				break;
			case 'r': printf("\nKey pressed for re-initialisation");
				// Capture video until a face is detected
				pFaceRect = waitForFaceDetect(&faceDetection, &captureFrame);
				
				if (pFaceRect == NULL) 
				{
				 captureFrame.DeallocateFrames();
				 releaseTracker();
				 // Destroy the window previously created
				 cvDestroyWindow("tracked face");
			     return 0;
				}
				releaseTracker();
				// Start tracking
				startTracking(captureFrame.getFrameCopy(), pFaceRect);
				break;
		}
	}

	// Release the image and tracker
  	captureFrame.DeallocateFrames();
    releaseTracker();

    // Destroy the window previously created
    cvDestroyWindow("tracked face");
    return 0;
}



//////////////////////
//waitForFaceDetect
/////////////////////

CvRect* waitForFaceDetect(FaceDetection* fD, CaptureFrame* cF)
{
	CvRect *pFaceRect = NULL;
	bool finished = false;

	// Load face cascades
	fD->InitFaceDetection();
	
	while(1)
	{
	  finished = cF->CaptureNextFrame(); //capture to frameCopy
	  if (finished) 
      {
        cF->DeallocateFrames();
        releaseTracker();
        cvDestroyWindow("tracked face");
        return NULL;
      }
	  
	  // Detect face
	  pFaceRect = fD->detectFace(cF->getFrameCopy());			

	  // Wait for a while before proceeding to the next frame
      if(cvWaitKey(10) >= 0);

      // When a face is found, quit the loop
      if(pFaceRect) 
	  {
	    printf("\nFound a face rectangle data %d, %d", pFaceRect->height, pFaceRect->width);
		return pFaceRect;
	  }
	}
}