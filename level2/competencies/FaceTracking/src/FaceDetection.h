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



#ifndef _FACE_DETECTION
#define _FACE_DETECTION

class FaceDetection
{
	public:
	FaceDetection();
	~FaceDetection();

	CvRect *detectFace(IplImage *src);
	void InitFaceDetection(void);

    
	private:
	CvMemStorage* storage; // memory for calculations
	
	CvHaarClassifierCascade* cascade; // Haar classifier
	CvHaarClassifierCascade* cascade_lateral; // Haar classifier

	const char* cascade_name; //string that contains the cascade name
	const char* cascade_name_lateral; //string that contains the cascade name

	CvRect* rect;
};

#endif
