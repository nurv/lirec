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


// This class includes functions that allow one to predict whether the user is staying still, approaching the camera or
// withdrawing by comparing the area of the face bounding box in the current frame with the values of the area in
// a temporal window of size N preceding it

#include "distanceCheck.h"


DistanceCheck::DistanceCheck(int windowSize)
{
	this->windowSize = windowSize; 
	if (this->windowSize < 0) this->windowSize = 1;
}

DistanceCheck::~DistanceCheck()
{
	this->faceAreaVals.clear();

}

double DistanceCheck::getAverageVal(int startValIndex, int endValIndex)
{
	int valsSize = this->faceAreaVals.size();    // Calculate the size of faceAreaVals (the vector that stores the areas)
	double average = 0.0; 
	int sumArea = 0;

	int j= 0;
	for (j = startValIndex; j < endValIndex+1; j++)  
	{ 
		sumArea += this->faceAreaVals[j];     // Sum of the areas 
	}
	int num = (endValIndex - startValIndex) + 1;
	average = (double)((double)sumArea/(double)num);  // Calculate the average area
	return average;
}

double DistanceCheck::getAverageValInWindow(void)
{
	int valsSize = this->faceAreaVals.size();
	if (valsSize <= this->windowSize) return 0.0;  
	int currentIndex = valsSize - 1;
	double average = 0.0;
	average = this->getAverageVal(currentIndex - this->windowSize, currentIndex - 1); // Calculate the average area over a temporal window of size "windowSize" preceding the current frame
	printf("\nAverage is %lf", average);
	return average;
}

void DistanceCheck::addFaceVal(int val)
{
	this->faceAreaVals.push_back(val);  // Store the values of the area in a vector
}

void DistanceCheck::setFaceVal(int index, int val)
{
	if ((index > this->faceAreaVals.size() -1) || (index < 0)) return;  
	this->faceAreaVals[index] = val;
}

int DistanceCheck::getNumElements(void)  
{
	return this->faceAreaVals.size();
}


// Compare the area of the face bounding box in the current frame with the average value of the area over a set 
// temporal window preceding it

int DistanceCheck::getMovementType(void)
{

    int valsSize = this->faceAreaVals.size();
	int currentIndex = valsSize - 1;
	double average = this->getAverageValInWindow();

	if (valsSize <= this->windowSize) return UNDEFINED;

	double percent = 0.1;  // Percentage of increase/decrease of the area of the face bounding box
    double extra = faceAreaVals[currentIndex - this->windowSize] * percent;  // Increase/decrease of the area of the face bounding box in the first frame of the window
    double newBiggerArea = faceAreaVals[currentIndex - this->windowSize] + extra; // Area in the first frame of the window increased of a certain percentage
    double newSmallerArea = faceAreaVals[currentIndex - this->windowSize] - extra;  // Area in the first frame of the window decreased of a certain percentage

	// If the area of the face bounding box in the current frame is not bigger than the area of the starting frame of the window
	// increased of a certain percentage and is not smaller than the area at the starting frame of the window decreased of a 
	// of a certain percentage the user is staying still

	if (faceAreaVals[this->faceAreaVals.size() - 1] <= newBiggerArea &&
		faceAreaVals[this->faceAreaVals.size() - 1]   >= newSmallerArea)
	{
		return STAYING_STILL;
	}
   
	// If the area of the face bounding box in the current frame is bigger than the average area over the preceding temporal window
	// the user is approaching the camera
	
	else if (this->faceAreaVals[this->faceAreaVals.size() - 1] > average)
	{
		return APPROACHING;
	}
	
	// If the area of the face bounding box in the current frame is smaller than the average area over the preceding temporal window
	// the user is withdrawing 
	
	else
	{
		return WITHDRAWING;
	}
}

