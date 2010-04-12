/*
 programmer : K . Du Casse 
 e-mail     : k.du-casse@herts.ac.uk
 Date of original : 07 april 2010
 Date of last mod : 07 april 2010

 Version 1.0
*/
#include <mrpt/core.h>

#include <iostream>

using namespace mrpt;
using namespace mrpt::utils;
using namespace mrpt::slam;
using namespace std;


// ------------------------------------------------------
//				TestPathPlanning
// ------------------------------------------------------
COccupancyGridMap2D		gridmap;

int main()
{
	//	CSensoryFramePtr		observations;
	//	CSensoryFrame			SF;
	//	CActionRobotMovement2D	MyRobotAction;
	//	CActionCollectionPtr	action;
	//	CActionCollection       actionsave;
	
		
		
		CPose3D TheRobotPose;
		TheRobotPose.x(0);
		TheRobotPose.y(0);
	//	TheRobotPose.yaw(0);


		if (mrpt::system::fileExists("MyGrid.Mapp"))
		{
			CFileGZInputStream("MyGrid.mapp") >> gridmap;
		}

	// Load the gridmap:
	while(1)
	{

		gridmap.insertObservation(*scan,*TheRobotPose)
	
	}
}


