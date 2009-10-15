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
// (Copyright (c) 2007, Robin Hewitt (http://www.robin-hewitt.com)
// See License.txt, in the Camshift wrapper folder, for details.



#include "cv.h"
#include "highgui.h"

#include <stdio.h>

#include "CaptureFrame.h"



CaptureFrame::CaptureFrame()
{
    this->capture = NULL;
	this->frame = NULL;   
	this->frameCopy = NULL;
}


CaptureFrame::~CaptureFrame()
{
	cvReleaseCapture(&(this->capture));
}


void CaptureFrame::AllocateFrames(int width, int height, int channels)
{
	this->frameCopy = cvCreateImage(cvSize(width,height),
                                            IPL_DEPTH_8U, channels);
}


void CaptureFrame::DeallocateFrames(void)
{
	cvReleaseImage(&(this->frameCopy));
}


int CaptureFrame::StartCapture(void)
{
	this->capture = cvCaptureFromCAM(0);
	
	// If capture is not loaded successfully
	if(!this->capture)
    {
	 printf("\nFatal error: capture not loaded successfully");
	 return 0;
	}
}


bool CaptureFrame::CaptureNextFrame(void)
//Capture the next frame into frameCopy
//Return whether video is finished
{
    this->frame = cvQueryFrame(capture);

	if (this->frame == NULL) 
	{
		return true;	//finished
	}

	if (this->frameCopy == NULL) 
	  this->AllocateFrames(this->frame->width,this->frame->height,this->frame->nChannels);

	// If the origin of the image is top left, copy frame to frameCopy
	if(this->frame->origin == IPL_ORIGIN_TL)
	  cvCopy(this->frame, this->frameCopy, 0);
		
	// Else flip and copy the image
	else
	  cvFlip(this->frame, this->frameCopy, 0);

	return false;
}


IplImage* CaptureFrame::getFrameCopy(void)

{
	return this->frameCopy;
}