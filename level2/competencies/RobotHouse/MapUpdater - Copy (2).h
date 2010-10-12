
/*
Map builder, just updates a map, thats all
is a good example of a port interupt;
*/


/* we dont need to explisitly stop the robot, it'll always time out after a secound in the virtual robot */

#define DEBUGmap 1	
#define RobotRad 0.45
#define RobotStopDistance 0.4 // the distance it should stop at if something is on laser, its nearly the robots rad
#define HowCloseToPointUntillStop 0.2 // the distance it should accept its in the right posistion
#define HowLongToWaitWithoutPath 30 // how long the robot should wait if it cant plan a path

/* we could use a 3d map, but the focus isn't navigation, no one will see it and it'll just take more cpu */
/* the idea is we have two locations, wanted and present, if they dont equal then we goto where we are ment to go */

//#include "SamClass.h"
#include <mrpt/slam.h>
#include <mrpt/gui.h>
#include <mrpt/base.h>
#include <mrpt/utils/CTicTac.h>
#include <math.h>

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
 bool ignorenext;
//CObservation2DRangeScanPtr scanptr;
 bool laserin = false;
 bool odoin = false;

static CObservationOdometryPtr odoptr;
//static CObservation2DRangeScanPtr scanptr;
/*
 class LaserPort : public BufferedPort<BinPortable<CObservation2DRangeScan>> 
{
    virtual void onRead(BinPortable<CObservation2DRangeScan> &b) 
	 { 
		 laserin = true;
		 if(odoin==true){puts("interupted odo");}
		b.content().maxRange=1;
		LowestLaserReadings = 100000; // thats very big in mtrs
		for(int x = 0;x<b.content().scan.size();x++)
			{
				b.content().scan[x]=0;
			if(b.content().scan[x]<LowestLaserReadings&&b.content().scan[x]!=0)
				{
				LowestLaserReadings=b.content().scan[x];
				}
			}
		CObservation2DRangeScan scan = b.content();
		CObservation2DRangeScanPtr scanptr(scan.duplicateGetSmartPtr());
		scanptr->maxRange=5;
		 mapBuilder->processObservation(scanptr);
		 laserin = false;
     }
};
*/
/*
 class OdoPort : public BufferedPort<Bottle> 
{
    virtual void onRead(Bottle &b) 
	 { 
		 odoin = true;
		 if(laserin==true){puts("interupted laser");}
		 try
		 {
		 odoptr = CObservationOdometry::Create();
		 CPose2D odo(b.get(0).asDouble(),b.get(1).asDouble(),b.get(2).asDouble());
		 odoptr->odometry = odo;
		 mapBuilder->processObservation(odoptr);
		 }
		 catch(exception& e){cout << "except:" << e.what()<<endl;}
		odoin = false; 
	}
};
*/
 class DestinPort : public BufferedPort<Bottle> 
{
    virtual void onRead(Bottle &b) 
	 { 
		IHavePermissionToMove =true;
		FinnishPose.x(b.get(0).asDouble());
		FinnishPose.y(b.get(1).asDouble());
		FinnishPose.phi(b.get(2).asDouble());
     }
};



class MapUpdater: public SamClass
{
private:
Network yarp;
//LaserPort LaserIn; // its gonna be process local connection, so i can send the whole class :)
//OdoPort OdoIn;
BufferedPort <BinPortable<CObservation2DRangeScan>> LaserIn;
BufferedPort<Bottle> OdoIn;

DestinPort Destination;
BufferedPort <Bottle> MoveOut;
BufferedPort <Bottle> Reached;

bool WhereWeThereLastTime;
double TimeToStop;

double MTimeNeedid;
double RobotSpeed;
public:

	void CheckLaser(void)
	{
		BinPortable<CObservation2DRangeScan> *LaserData = LaserIn.read(false);
		if(LaserData!=NULL)
		{
			for(int x = 0;x<LaserData->content().scan.size();x++)
			{
				LaserData->content().scan[x] = 0;
			}
			CObservation2DRangeScan scan(LaserData->content());
			CObservation2DRangeScanPtr scanptr(scan.duplicateGetSmartPtr());
			mapBuilder->processObservation(scanptr);
			printf("laser time %u \n",scanptr->timestamp);
		}

	}
	void CheckOdo(void)
	{
		Bottle *CC = OdoIn.read(false);
		if(CC!=NULL)
		{
		odoptr = CObservationOdometry::Create();
		CPose2D odo(CC->get(0).asDouble(),CC->get(1).asDouble(),CC->get(2).asDouble());
		odoptr->odometry = odo;
		mapBuilder->processObservation(odoptr);
		}
	}

	
	void SamInit(void)
	{
	RecognisePort("LaserIn");
	RecognisePort("OdoIn");
	RecognisePort("MoveOut");
	RecognisePort("TargetIn");
	RecognisePort("CMion");
	Destination.open("/MAP_TargetIn");
	LaserIn.open("/MAP_LaserIn"); //myPortStatus
	OdoIn.open("/MAP_OdoIn");
	MoveOut.open("/MAP_MoveOut");
	Reached.open("/MAP_CMion");

	LaserIn.useCallback();
	OdoIn.useCallback();
	Destination.useCallback();

	LaserIn.setReporter(myPortStatus);
	OdoIn.setReporter(myPortStatus);
	MoveOut.setReporter(myPortStatus);
	Destination.setReporter(myPortStatus);
	Reached.setReporter(myPortStatus);

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
	mapBuilder->setCurrentMapFile("Robothousemap.simplemap");

	mapBuilder->ICP_options.matchAgainstTheGrid = true;
//	mapBuilder->options.verbose					= false;
//	mapBuilder->options.enableMapUpdating		= true;
	mapBuilder->options.debugForceInsertion		= false;
	mapBuilder->options.insertImagesAlways		= true;
	/////////////////////////////////////////////////////////////// REALLY IMPORTANT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	// this line is the threshold for 1 updating the posistion, and 2 adding to the map.
	// the higher the better map becouse incorrect readings wont be put in the map
	// but if its too high it can get lost and never work at all.
	//mapBuilder->ICP_options.minICPgoodnessToAccept=0.9;

	MTimeNeedid =0;
	WhereWeThereLastTime=false;
	TimeToStop=0;
	}
	
	
	void SamIter(void)
	{
		CheckOdo();
		CheckLaser();
		
		if(!mapBuilder->getCurrentlyBuiltMetricMap()->isEmpty())
		{

		Gridmap = *mapBuilder->getCurrentlyBuiltMetricMap()->m_gridMaps[0].pointer();

		StartPose.x(mapBuilder->getCurrentPoseEstimation()->getEstimatedPose().x());
		StartPose.y(mapBuilder->getCurrentPoseEstimation()->getEstimatedPose().y());
		StartPose.phi(mapBuilder->getCurrentPoseEstimation()->getEstimatedPose().yaw());
		showmap();
		

		// if we are not in the right location then every secound replan route
		if(!AreWeThere() && yarp::os::Time::now()>MTimeNeedid && IHavePermissionToMove)// if in wrong location and we havn't got a path
			{
			PlanPath();
			if(!Path.empty())
				{
				MTimeNeedid = yarp::os::Time::now() + 1;	
				Bottle &bb = MoveOut.prepare();
				bb.clear();
				bb.addInt(5);//priority
				bb.addDouble(1);//time
				bb.addDouble(RobotSpeed);// liner vel

				double Angle =RAD2DEG(atan2(Path.front().y - StartPose.y(),Path.front().x - StartPose.x()));
				bb.addDouble(RAD2DEG(Angle)); //rot vel
				MoveOut.write();
				}

			}
			
		}
	}

	bool AreWeThere(void)
	{
		
		double distanceTofinalObjective = StartPose.distance2DTo(FinnishPose.x(),FinnishPose.y());
		if(distanceTofinalObjective<LowestLaserReadings/2){RobotSpeed=distanceTofinalObjective*1000;} // needs to put in mm
		else											  {RobotSpeed=(LowestLaserReadings/2)*1000;}		// needs to put in mm
			
		if(distanceTofinalObjective < HowCloseToPointUntillStop)
		{
			IHavePermissionToMove=false;
			Path.clear();
			if(WhereWeThereLastTime==false)// so we only just got into pos
			{
				Bottle &BBB2 = Reached.prepare();
				BBB2.clear();
				BBB2.addDouble(StartPose.x());
				BBB2.addDouble(StartPose.y());
				Reached.write();
			}

			WhereWeThereLastTime=true;
			return true;
		}
		WhereWeThereLastTime=false;
		return false;

	}

	void PlanPath(void)
	{

	//	puts("planning path");
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


				if(!ThereIsNoPath){TimeToStop=0;}
				else			  {TimeToStop++;}

			if(TimeToStop>HowLongToWaitWithoutPath)// so we only just got into pos
			{
				Bottle &BBB3 = Reached.prepare();
				BBB3.clear();
				BBB3.addDouble(StartPose.x());
				BBB3.addDouble(StartPose.y());
				Reached.write();
				IHavePermissionToMove=false;
				TimeToStop=0;
			}

			//	puts("stoped planning path");

				// need to do something so if it cant find it for 10 secs then stop
	}


	void showmap(void)
	{
		Gridmap.getAsImage(img,false);
		CPose3DPDFPtr posePDF = mapBuilder->getCurrentPoseEstimation();
		CPose2D RobotLoc(mapBuilder->getCurrentPoseEstimation()->getEstimatedPose());
		CPose2D Nose = RobotLoc + CPose2D(RobotRad,0,0);
		img.drawCircle(Gridmap.x2idx(RobotLoc.x()),Gridmap.getSizeY()-1-Gridmap.y2idx(RobotLoc.y()),RobotRad/ Gridmap.getResolution(),TColor(50,100,50));
		img.drawCircle(Gridmap.x2idx(Nose.x()),Gridmap.getSizeY()-1-Gridmap.y2idx(Nose.y()),(RobotRad/2)/ Gridmap.getResolution(),TColor(50,100,50));
		
		
		if(!Path.empty())
		{
			for(int uu =0;uu<Path.size();uu++)
			{
				img.drawCircle(Gridmap.x2idx(Path[uu].x),Gridmap.getSizeY()-1-Gridmap.y2idx(Path[uu].y),RobotRad/ Gridmap.getResolution(),TColor(50,50,150));
			}
		}
	
		
		Pathwin.showImage(img);
	}
};

