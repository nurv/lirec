
/*
Map builder, just updates a map, thats all
is a good example of a port interupt;
*/
#define DEBUGmap 1	
#define RobotRad 0.45
#define RobotStopDistance 0.4 // the distance it should stop at, its nearly the robots rad


/* we could use a 3d map, but the focus isn't navigation, no one will see it and it'll just take more cpu */
/* the idea is we have two locations, wanted and present, if they dont equal then we goto where we are ment to go */

//#include "SamClass.h"
#include <mrpt/slam.h>
#include <mrpt/gui.h>
#include <mrpt/base.h>
#include <mrpt/utils/CTicTac.h>


using namespace mrpt;
using namespace mrpt::hwdrivers;
using namespace mrpt::slam;
using namespace mrpt::gui;
using namespace mrpt::utils;
using namespace std;


string WantedLocation =" ";
string CurrentLocation = " ";
bool CurrentlyHavePath = false;
bool IHavePermissionToMove = false;


 CMetricMapBuilderICP				*mapBuilder;		
 TSetOfMetricMapInitializers		metricMapsOpts;
 CConfigFile						iniFile("MapSetup.ini");
 CICP::TConfigParams				icpOptions;
 COccupancyGridMap2D Gridmap;
 CPose3D CurRobotPose;
 mrpt::gui::CDisplayWindow			Pathwin("Computed path");
 CImage		img;
 double LowestLaserReadings;
 double iterTimer =0;
 CPathPlanningCircularRobot	pathPlanning;
 bool ThereIsNoPath;
 CPose2D StartPose,FinnishPose;
 std::deque<poses::TPoint2D>		Path;

 class LaserPort : public BufferedPort<BinPortable<CObservation2DRangeScan>> 
{
    virtual void onRead(BinPortable<CObservation2DRangeScan> &b) 
	 { 
		 CObservation2DRangeScan scan = b.content();
		 CObservation2DRangeScanPtr scanptr(scan.duplicateGetSmartPtr());
		 mapBuilder->processObservation(scanptr);
		LowestLaserReadings = 100000; // thats very big in mtrs
		for(int x = 0;x<b.content().scan.size();x++)
			{
			if(b.content().scan[x]<LowestLaserReadings&&b.content().scan[x]!=0)
				{
				LowestLaserReadings=b.content().scan[x];
				}
			}
		if(LowestLaserReadings<RobotStopDistance){ThereIsNoPath=true;}
     }
};
 class OdoPort : public BufferedPort<BinPortable<CObservationOdometry>> 
{
    virtual void onRead(BinPortable<CObservationOdometry> &b) 
	 { 
		// CObservationOdometry odo = b.content();
		// CObservationOdometryPtr odoptr(odo.duplicateGetSmartPtr());
		// mapBuilder->processObservation(odoptr);
     }
};
 class DestinPort : public BufferedPort<Bottle> 
{
    virtual void onRead(Bottle &b) 
	 { 
		IHavePermissionToMove =true;
		WantedLocation=b.get(0).asString().c_str();
     }
};



class MapUpdater: public SamClass
{
private:
Network yarp;
LaserPort LaserIn; // its gonna be process local connection, so i can send the whole class :)
OdoPort OdoIn;
BufferedPort<BinPortable<CMultiMetricMap>> MapOut;
public:

	
	void SamInit(void)
	{
	RecognisePort("LaserIn");
	RecognisePort("OdoIn");
	RecognisePort("MapOut");
	LaserIn.open("/MAP_LaserIn"); //myPortStatus
	OdoIn.open("/MAP_OdoIn");
	MapOut.open("/MAP_MapOut");
	LaserIn.useCallback();
	LaserIn.setReporter(myPortStatus);
	OdoIn.useCallback();
	LaserIn.setReporter(myPortStatus);
	MapOut.setReporter(myPortStatus);
	StartModule("/MAP");

	pathPlanning.robotRadius = RobotRad;// + RobotRadError;
	pathPlanning.minStepInReturnedPath=0.6;
	pathPlanning.occupancyThreshold=0.01;
	ThereIsNoPath=true;

	//LaserIn
	metricMapsOpts.loadFromConfigFile(iniFile, "MappingApplication"); 
	icpOptions.loadFromConfigFile(iniFile, "ICP");
	mapBuilder = new CMetricMapBuilderICP(&metricMapsOpts,0.001,DEG2RAD(0.001),&icpOptions);
	mapBuilder->options.verbose=false;
	mapBuilder->enableMapUpdating(true);// start off with localisation only;

	/////////////////////////////////////////////////////////////// REALLY IMPORTANT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	// this line is the threshold for 1 updating the posistion, and 2 adding to the map.
	// the higher the better map becouse incorrect readings wont be put in the map
	// but if its too high it can get lost and never work at all.
	mapBuilder->ICP_options.minICPgoodnessToAccept=0.9;


	}
	
	
	void SamIter(void)
	{
		if(!mapBuilder->getCurrentlyBuiltMetricMap()->isEmpty())
		{

		Gridmap = *mapBuilder->getCurrentlyBuiltMetricMap()->m_gridMaps[0].pointer();

	//	StartPose. = mapBuilder->getCurrentPoseEstimation()->getEstimatedPose();
//		showmap();
		

		if(WantedLocation.compare(CurrentLocation)!=0 && ThereIsNoPath==true)// if in wrong location and we havn't got a path
			{
			PlanPath();

			}
		else if(WantedLocation.compare(CurrentLocation)!=0 && ThereIsNoPath==false && IHavePermissionToMove) // if in wrong location and we have a path
			{
			// work out its angle to the next waypoint

			}
		
		}
	}

	void GetCurrentLocation(void)
	{

	// got to put in here that if we are in the right loc then turn permissoin to false
	// but also send the command to stop

	// when we get to the first waypoint plan again

	}

	void PlanPath(void)
	{
			try{pathPlanning.computePath(Gridmap,StartPose,FinnishPose,Path,ThereIsNoPath,-1.0f);}
				catch(...){}
				CPose2D TempPose;
				if(ThereIsNoPath) // try imagining the robot is behind itself
				{
				TempPose = StartPose + CPose2D(-RobotRad,0,0);
				try{pathPlanning.computePath(Gridmap,TempPose,FinnishPose,Path,ThereIsNoPath,-1.0f);}
				catch(...){}
				}
				if(ThereIsNoPath)// try imagining the robot to right right of itself
				{
				TempPose = StartPose + CPose2D(0,RobotRad,0);
				try{pathPlanning.computePath(Gridmap,TempPose,FinnishPose,Path,ThereIsNoPath,-1.0f);}
				catch(...){}
				}
				if(ThereIsNoPath)// try imagining the robot to right right of itself
				{
				TempPose = StartPose + CPose2D(0,-RobotRad,0);
				try{pathPlanning.computePath(Gridmap,TempPose,FinnishPose,Path,ThereIsNoPath,-1.0f);}
				catch(...){}
				}
	}


	void showmap(void)
	{
		Gridmap.getAsImage(img,false);
		CPose3DPDFPtr posePDF = mapBuilder->getCurrentPoseEstimation();
		CPose2D RobotLoc(mapBuilder->getCurrentPoseEstimation()->getEstimatedPose());
		CPose2D Nose = RobotLoc + CPose2D(RobotRad,0,0);
		img.drawCircle(Gridmap.x2idx(RobotLoc.x()),Gridmap.getSizeY()-1-Gridmap.y2idx(RobotLoc.y()),RobotRad/ Gridmap.getResolution(),TColor(50,100,50));
		img.drawCircle(Gridmap.x2idx(Nose.x()),Gridmap.getSizeY()-1-Gridmap.y2idx(Nose.y()),(RobotRad/2)/ Gridmap.getResolution(),TColor(50,100,50));
		Pathwin.showImage(img);
	}
};

