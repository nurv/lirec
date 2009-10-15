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



#ifndef _CAPTURE_FRAME
#define _CAPTURE_FRAME

class CaptureFrame
{
	public:
	CaptureFrame(void);
	~CaptureFrame(void);

	int StartCapture(void);
	bool CaptureNextFrame(void);

	void AllocateFrames(int width, int height, int channels);
	void DeallocateFrames(void);

	IplImage* getFrameCopy(void);
	
	private:
	IplImage* frame;
    IplImage* frameCopy;
	CvCapture* capture;  // Structure for getting video from camera or avi
};

#endif
