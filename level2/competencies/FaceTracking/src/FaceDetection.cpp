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

// FaceTracking uses the Camshift wrapper program (see camshift_wrapper.cpp and camshift_wrapper.h):
// Copyright (c) 2007, Robin Hewitt (http://www.robin-hewitt.com)
// See License.txt, in the Camshift wrapper folder, for details.



#include "cv.h"
#include "highgui.h"

#include <stdio.h>

#include "FaceDetection.h"


FaceDetection::FaceDetection()
{
    this->storage = NULL; // Create memory for calculations
	this->cascade = NULL; // Create a new Haar classifier
	this->cascade_lateral = NULL; // Create a new Haar classifier
	
	this->cascade_name = "haarcascade_frontalface_alt.xml"; // Create a string that contains the cascade name
	this->cascade_name_lateral = "haarcascade_profileface.xml"; // Create a string that contains the cascade name

	this->rect = NULL;
}


FaceDetection::~FaceDetection()
{
	if(this->cascade) cvReleaseHaarClassifierCascade(&(this->cascade));
	if(this->cascade_lateral) cvReleaseHaarClassifierCascade(&(this->cascade_lateral));
	if(this->storage) cvReleaseMemStorage(&(this->storage));
}


void FaceDetection::InitFaceDetection(void)
{
	this->cascade_name = "haarcascade_frontalface_alt.xml";
	this->cascade_name_lateral = "haarcascade_profileface.xml";
	
	this->storage = cvCreateMemStorage(0); // Allocate the memory storage

	// Load the HaarClassifierCascade
    this->cascade = (CvHaarClassifierCascade*)cvLoad(this->cascade_name, 0, 0, 0 );
	this->cascade_lateral = (CvHaarClassifierCascade*)cvLoad(this->cascade_name_lateral, 0, 0, 0 );

	// Check whether the cascade has loaded successfully, otherwise report and error and quit
    if(!this->cascade)
    {
     fprintf( stderr, "ERROR: Could not load classifier cascade\n" );
     fprintf( stderr,
     "Usage: facedetect --cascade=\"<cascade_path>\" [filename|camera_index]\n" );
    }

	 if(!this->cascade_lateral)
    {
     fprintf( stderr, "ERROR: Could not load classifier cascade lateral\n" );
     fprintf( stderr,
     "Usage: facedetect --cascade=\"<cascade_path>\" [filename|camera_index]\n" );
	}
}


CvRect *FaceDetection::detectFace(IplImage *src)
{
    int minSize = src->width / 4; // Size of the smallest face to search for

	// Clear the memory storage which was used before
	cvClearMemStorage(this->storage);

	// If the cascade is loaded, then:
    if(this->cascade && this->cascade_lateral)
    {
     double t = (double)cvGetTickCount();

	 // There can be more than one face in an image; detect faces and store them in a growable sequence
        
	 CvSeq* faces = cvHaarDetectObjects(src, this->cascade, this->storage,
                                            1.1, 3, CV_HAAR_FIND_BIGGEST_OBJECT,
                                            cvSize(minSize, minSize)); 

     t = (double)cvGetTickCount() - t;

     printf( "\nDetection time = %gms\n", t/((double)cvGetTickFrequency()*1000.));

	 if(faces->total < 1)  // If no face is detected using the frontal classifier, use the lateral Haar classifier

	 faces = cvHaarDetectObjects(src, this->cascade_lateral, this->storage,
                                            1.1, 3, CV_HAAR_FIND_BIGGEST_OBJECT,
                                            cvSize(minSize, minSize));
		
	 
	 if(faces)
	 {
		// If one or more faces are detected, return the first one
		 if (faces->total > 0)
		 { 
			 printf("\nDetected a face");
			 this->rect = (CvRect*)cvGetSeqElem(faces, 0);
		 }
		 //otherwise, reset the face rectangle to be NULL
		 else 
		 {
			 printf("\nDidn't detect a face");
			 this->rect = NULL;
		 }
	 }
	 else this->rect = NULL;

	 return this->rect;
    }
}
