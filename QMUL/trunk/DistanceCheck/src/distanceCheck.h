// AUTHOR: Ginevra Castellano
// Queen Mary University of London
// DATE: 03/2009
// VERSION: 1.0

// Copyright (C) 2009 Ginevra Castellano

// This file is part of the DistanceCheck program.

// DistanceCheck is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

// DistanceCheck is based on the face detection code provided by OpenCV.
// OpenCV: Copyright (C) 2000-2006, Intel Corporation, all rights reserved.
// Third party copyrights are property of their respective owners.
// See OpenCV_license.txt, in the program folder, for details.



#ifndef _DISTANCE_CHECK
#define _DISTANCE_CHECK


#include <vector>

enum MovementType {
	
	UNDEFINED = 0,
	STAYING_STILL = 1,
	APPROACHING = 2,
	WITHDRAWING = 3
};

class DistanceCheck
{
	public:
	DistanceCheck(int windowSize);
	~DistanceCheck();

	double getAverageVal(int startValIndex, int endValIndex); // Calculate the average of the areas of the face bounding box
	double getAverageValInWindow(void);     // Calculate the average of the areas of the face bounding box in a temporal window
	int getMovementType(void);         // Detect the type of movement 

	void addFaceVal(int val);  // Store the values of the area of the face bounding box in a vector
	void setFaceVal(int index, int val);   // Set a specific value for the area of the face bounding box

	int getNumElements(void);  // Get the number of element in a vector

	private:
		
		int windowSize;
		std::vector<int> faceAreaVals;
};

#endif