// AUTHOR: Ginevra Castellano
// Queen Mary University of London
// DATE: 03/2010
// VERSION: 1.0

// Copyright (C) 2009 Ginevra Castellano
// Queen Mary University of London

// This file is part of the MotionDirectionDetection program

// MotionDirectionDetection is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

// MotionDirectionDetection uses the OpenCV library
// Copyright (C) 2000-2006, Intel Corporation, all rights reserved.
// Third party copyrights are property of their respective owners.
// See OpenCV_license.txt, in the program folder, for details.


#ifndef _MOTION_DIRECTION
#define _MOTION_DIRECTION


#include "CaptureFrame.h"  
#include "MessageStorage.h"
#include "BackgroundSubtr.h"

#include "cv.h"
#include "highgui.h"

enum MovementType {
	
	NO_MOTION = 0,
	ENTERING_THE_ROOM = 1,
	LEAVING_THE_ROOM = 2
};

class MotionDirection
{
	public:
	MotionDirection(void);
	~MotionDirection(void);

	int execute(CaptureType c, char* aviname = NULL); // process video, detect type of movement and store this information in a list
	double update_mhi(void); // detect overall motion direction
	int getMovementType(void); // detect type of movement based on overall motion direction

	MessageStorage* ms;

	private:
	CaptureFrame captureFrame; 
	BackgroundSubtr backgroundSubtr;

	IplImage* motion;
	IplImage* image; 
	IplImage* frame_background;
	IplImage* silhouette;
	double motionOrient;

	bool finished;

	// tracking parameters (in seconds)
	const double MHI_DURATION; // how long motion history pixels are allowed to remain in the mhi 
	const double MIN_TIME_DELTA; // minimum gradient magnitude allowed 
	const double MAX_TIME_DELTA;  // maximum gradient magnitude allowed
	
	// temporary images
	IplImage *mhi; // motion history image (mhi)
	IplImage *orient; // orientation
	IplImage *mask; // valid orientation mask
	CvMemStorage* storage; // temporary storage

	double timestamp; // current time in seconds
    CvSize size; // current frame size
    double count; // a measure of motion
	int threshold; // threshold to disregard little motion
    int angle; // motion orientation
    CvPoint center;
    double magnitude;          
    CvScalar color;
};

#endif