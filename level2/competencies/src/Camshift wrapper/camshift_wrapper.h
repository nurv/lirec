// camshift_wrapper.h - by Robin Hewitt, 2007
// http://www.cognotics.com/opencv/downloads/camshift_wrapper
// This is free software. See License.txt, in the download
// package, for details.
// Copyright (c) 2007, Robin Hewitt (http://www.robin-hewitt.com).
// Third party copyrights are property of their respective owners.
//
// Slight modifications by Ginevra Castellano, October 2009

// Public interface for the Simple Camshift Wrapper

#ifndef __SIMPLE_CAMSHIFT_WRAPPER_H
#define __SIMPLE_CAMSHIFT_WRAPPER_H

// Main Control functions
int     createTracker(const IplImage * pImg);
void    releaseTracker();
void    startTracking(IplImage * pImg, CvRect * pRect);

CvRect track(IplImage *);
//CvBox2D track(IplImage *);

// Parameter settings
void setVmin(int vmin);
void setSmin(int smin);

#endif
