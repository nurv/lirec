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
#include "BackgroundSubtr.h"
#include <stdio.h>


BackgroundSubtr::BackgroundSubtr(void)
{
	this->storage = NULL; 

	this->backgrImgGray = NULL; 
	this->srcGray = NULL; 
	this->frameForeground = NULL; 
	this->ForegroundEroded = NULL; 
}

BackgroundSubtr::~BackgroundSubtr(void)
{
	if(this->storage) cvReleaseMemStorage(&this->storage);
}


IplImage* BackgroundSubtr::extractBackground(IplImage *src, IplImage *backgrImg)

{	
	if(!this->storage)
        this->storage = cvCreateMemStorage(0);
    else
        cvClearMemStorage(this->storage);

	if(!backgrImgGray)
       backgrImgGray = cvCreateImage(cvSize(src->width,src->height),
                                            IPL_DEPTH_8U, 1); 
	if(!srcGray)
       srcGray = cvCreateImage(cvSize(src->width,src->height),
                                            IPL_DEPTH_8U, 1); 
	if(!frameForeground)
       frameForeground = cvCreateImage(cvSize(src->width,src->height),
                                            IPL_DEPTH_8U, 1); 
	if(!ForegroundEroded)
       ForegroundEroded = cvCreateImage(cvSize(src->width,src->height),
                                            IPL_DEPTH_8U, 1);

	cvCvtColor(backgrImg, backgrImgGray, CV_RGB2GRAY);  // convert background image to grayscale
	cvCvtColor(src, srcGray, CV_RGB2GRAY); // convert current frame to grayscale
	cvAbsDiff(backgrImgGray, srcGray, frameForeground);  // subtract background grayscale image from grayscale current frame
	cvThreshold(frameForeground, frameForeground, 50, 255, CV_THRESH_BINARY); // threshold to get a black and white image
	cvErode(frameForeground, frameForeground, NULL, 1); // eliminate noise

	return frameForeground;
}

