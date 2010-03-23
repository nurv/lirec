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


#include "cv.h"
#include "highgui.h"

#include <time.h>
#include <math.h>
#include <ctype.h>
#include <stdio.h>

#include "MotionDirection.h"


MotionDirection::MotionDirection(): MHI_DURATION(1), MIN_TIME_DELTA(0.05), MAX_TIME_DELTA(0.5)
{
	this->motion = NULL;
	this->image = NULL; 
	this->frame_background = NULL;
	this->silhouette = NULL;
	this->motionOrient = 0.0;

	this->mhi = NULL; 
	this->orient = NULL; 
	this->mask = NULL; 
	this->storage = NULL; 

    this->count = 0;
	this->threshold = 1000;
    this->angle = 0;
    this->magnitude = 100;          
    this->color = CV_RGB(255,255,255);

	ms = new MessageStorage(100);
}


MotionDirection::~MotionDirection()
{
	if(this->storage) cvReleaseMemStorage(&this->storage);

	delete ms;
}


int MotionDirection::execute(CaptureType c, char* aviname)
{
	cvNamedWindow( "Silhouette", 1 );
	cvNamedWindow( "Motion", 1 );

	if (!this->captureFrame.StartCapture(c, aviname)) 
	{
		printf("\nFatal error: capture not started successfully");
		//big error occurred;  
		while(1);
	}

	this->finished = this->captureFrame.CaptureNextFrame(); // capture into frameCopy  
													        // 1st call (i.e. 1st frame, contains no colour data)
	if (this->finished) // if video is finished  
	 {
	   this->captureFrame.DeallocateFrames();
	   return 0;
	 }

	this->finished = this->captureFrame.CaptureNextFrame();  // 2nd call (i.e. 2nd frame, is the first to contain colour data)

	if (this->finished) // if video is finished  
	 {
	   this->captureFrame.DeallocateFrames();
	   return 0;
	 }

	this->image = this->captureFrame.getFrameCopy(); // get the first valid frame

	if(!this->frame_background)
       this->frame_background = cvCreateImage(cvSize(this->image->width,this->image->height),IPL_DEPTH_8U, this->image->nChannels); 

	cvCopy(this->image, this->frame_background, 0); // store the first valid frame as the background 


	while(1) 
	{ 

	 this->finished = this->captureFrame.CaptureNextFrame();

	 if (this->finished) 
	  {
	    this->captureFrame.DeallocateFrames();
	    return 0;
	  }

	 if(!this->motion)
      {
        this->motion = cvCreateImage(cvSize(this->image->width,this->image->height), 8, 3);
        cvZero(this->motion);
        this->motion->origin = this->image->origin;
      }
	 
	 if(!this->silhouette)
      {
        this->silhouette = cvCreateImage(cvSize(this->image->width,this->image->height), 8, 3);
        cvZero(this->silhouette);
        this->silhouette->origin = this->image->origin;
      }

	 // extract silhouette from background 
	 this->silhouette = this->backgroundSubtr.extractBackground(this->captureFrame.getFrameCopy(), this->frame_background); 
      
      cvShowImage("Silhouette", this->silhouette);
	  
	  // build motion template and compute overall motion direction
	  this->motionOrient = this->update_mhi();
	  cvShowImage("Motion", this->motion);

	  switch (this->getMovementType()) 
		{	
			case NO_MOTION:
				printf("\nNO MOTION");
				this->ms->addMessage(NO_MOTION, (double)clock()/CLOCKS_PER_SEC);  // store current message code and timestamp
				break;
			case ENTERING_THE_ROOM:
				printf("\nENTERING THE ROOM");
				this->ms->addMessage(ENTERING_THE_ROOM, (double)clock()/CLOCKS_PER_SEC);
				break;
			case LEAVING_THE_ROOM: 
				printf("\nLEAVING THE ROOM");
				this->ms->addMessage(LEAVING_THE_ROOM, (double)clock()/CLOCKS_PER_SEC);
				break;
			}
		
	 if( cvWaitKey(10) >= 0 )
     break;
	} //end while


	cvDestroyWindow( "Motion" );
	cvDestroyWindow( "Background" );
    captureFrame.DeallocateFrames();

	return -1; 
}


double MotionDirection::update_mhi(void)

{
	this->timestamp = (double)clock()/CLOCKS_PER_SEC;  // get current system time in seconds
	this->size = cvSize(this->image->width,this->image->height); // get current frame size

	// allocate images at the beginning or
    // reallocate them if the frame size is changed
    if( !this->mhi || this->mhi->width != this->size.width || this->mhi->height != this->size.height ) 
	 {
        cvReleaseImage(&this->mhi);
        cvReleaseImage(&this->orient);
        cvReleaseImage(&this->mask);
        
        this->mhi = cvCreateImage(this->size, IPL_DEPTH_32F, 1);
        cvZero(this->mhi); // clear mhi at the beginning
        this->orient = cvCreateImage(this->size, IPL_DEPTH_32F, 1);
        this->mask = cvCreateImage(this->size, IPL_DEPTH_8U, 1);
     }

	cvUpdateMotionHistory(this->silhouette, this->mhi, this->timestamp, this->MHI_DURATION); // update mhi  

    // convert MHI to blue 8u image
    cvCvtScale(this->mhi, this->mask, 255./this->MHI_DURATION,
                (this->MHI_DURATION - this->timestamp)*255./this->MHI_DURATION);
    cvZero(this->motion);
    cvCvtPlaneToPix(this->mask, 0, 0, 0, this->motion);  

    // calculate motion gradient orientation and valid orientation mask
    cvCalcMotionGradient(this->mhi, this->mask, this->orient, this->MIN_TIME_DELTA, this->MAX_TIME_DELTA, 3);

	if(!this->storage )
        this->storage = cvCreateMemStorage(0);
    else
        cvClearMemStorage(this->storage);

	// calculate orientation
    this->angle = cvCalcGlobalOrientation(this->orient, this->mask, this->mhi, this->timestamp, this->MHI_DURATION);
    this->angle = 360 - this->angle;  // adjust for images with top-left origin

    this->count = (cvNorm(this->silhouette, 0, CV_L1, 0 )/1000); // calculate a measure of motion 

	if (this->count > this->threshold)  // disregard little motion
	 {
		// draw a clock with arrow indicating the direction
		this->center = cvPoint((this->image->width/2),(this->image->height/2));

        cvCircle(this->motion, this->center, cvRound(this->magnitude*1.2), this->color, 3, CV_AA, 0);
        cvLine(this->motion, this->center, cvPoint(cvRound(this->center.x + this->magnitude*cos(this->angle*CV_PI/180)),
                cvRound(this->center.y - this->magnitude*sin(this->angle*CV_PI/180))), this->color, 3, CV_AA, 0);
		printf("\nAngle is %d", this->angle);
		return this->angle;
	 }

	else
	 {
		this->angle= -1;
		printf("\nAngle is %d", this->angle);
		return this->angle;
	 }

}


int MotionDirection::getMovementType(void)

{
	if (this->motionOrient > 90 && this->motionOrient <= 270)
	 {
		return ENTERING_THE_ROOM;
	 }
   
	else if ((this->motionOrient >= 0 && this->motionOrient <= 90) ||
		(this->motionOrient > 270 && this->motionOrient <= 360))
	 {
		return LEAVING_THE_ROOM;
	 }
	
	else if (this->motionOrient == -1) 
	 {
		return NO_MOTION;
	 }

	return -1;
}