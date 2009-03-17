/* 
AUTHOR: Ginevra Castellano
Queen Mary University of London
DATE: 03/2009
VERSION: 1.0
*/

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