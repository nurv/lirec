
/* Module : Navigation 
   Author : K.Du Casse
   Libs	  : Using MRPT

   The concept is that working with a whole map can be computationally high, so we only add what can presently see into the map, this solves problem of out of date
   memory, scince getting the robot to do a investigation routine could be tiresome.
   Havn't used any interupts, becouse we got to make sure certain timings are maintained, so it startsup,runs and shutsdown properly and in order
   It'll get the robot to move as fast as the closest obsticle(or endpoint if thats nearer)
   and replan the route every half a secound. more than enough time to deal with realtime changes (upto a point) but not to be too computatoinally ineffiecent.

*/


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

#define MyRes 0.05      // this is the most important varible of all!!
						// this controls the resolution of the grid, so 0.05 is cm grid, the less res means less memmory useage and quicker responce
						// also the higher the resolution the closer it'll try to get to a object for the shortest path, which allows less error in movement
#define RobotRad 0.45

CPose2D MyRobotPose(0,0,0);
CPose2D WantedLoc  (5,5,0);

COccupancyGridMap2D *MyMap; 
COccupancyGridMap2D *CopyMyMap;



CMetricMap *MyMapMetric;

mrpt::gui::CDisplayWindow			Window("Map");
CImage		Scanimg,Pathimg;
bool InTransit = false;

CPathPlanningCircularRobot		pathPlanning;
std::deque<poses::TPoint2D>		Path;
bool ThereIsNoPath=true;
double MaxLaser =10000;


CICP MyICP;
//ICP MyICP;

class MapUpdater: public SamClass
{
private:
Network yarp;

BufferedPort<Bottle> LaserIn;
BufferedPort<Bottle> OdoIn;
BufferedPort<Bottle> WheelOut;
BufferedPort<Bottle> Ion;


public:

	


	
	void SamInit(void)
	{
	RecognisePort("LaserIn");
	RecognisePort("OdoIn");
	RecognisePort("ROut");
	RecognisePort("Ion");
	
	LaserIn.open("/MAP_LaserIn"); //myPortStatus
	OdoIn.open("/MAP_OdoIn");
	Ion.open("/MAP_Ion");
	WheelOut.open("/MAP_ROut");

	StartModule("/MAP");



	MyMap = new COccupancyGridMap2D(-10,10,-10,10,MyRes);
	CopyMyMap = new COccupancyGridMap2D(-10,10,-10,10,MyRes);

	MyMap->insertionOptions.maxDistanceInsertion=3;

	pathPlanning.robotRadius = RobotRad;// + RobotRadError;
	pathPlanning.minStepInReturnedPath=0.6;
	pathPlanning.occupancyThreshold=0.01;
	}
	
	
	void SamIter(void)
	{
	UpdateOdo();
	CheckForCommand();
	UpdateLaser();
	PlanPath();
	if(InTransit && ThereIsNoPath)												{InTransit=false;SendReplyIon();puts("no path available sorry");}// if we havn't got a path then we got to give up
	if(InTransit && MyRobotPose.distance2DTo(WantedLoc.x(),WantedLoc.y())<0.2)	{InTransit=false;SendReplyIon();puts("reached location");}// if we are there then we give up
	if(InTransit){SendWheelCommands();}
	ShowMap();
	yarp::os::Time::delay(0.15);
	}

	double Rad2Deg(double x)
	{
	return x*57.2957795;
	}
	double Deg2Rad(double x)
	{
	return x/57.2957795;
	}


	void SendWheelCommands(void)
	{
	double Speed;
	Bottle &Vels=WheelOut.prepare();
	
	

	
	//double Angle =RAD2DEG(atan2(MyRobotPose.y()-Path.front().y,MyRobotPose.x()-Path.front().x));
	
	double Angle =atan2((Path.front().y-MyRobotPose.y())*1000,(Path.front().x-MyRobotPose.x())*1000);
	Angle-=MyRobotPose.phi();

	while(Angle>2){Angle-=1;}
	while(Angle<-2){Angle+=1;}
	if(MyRobotPose.distance2DTo(WantedLoc.x(),WantedLoc.y())<MaxLaser){Speed = MyRobotPose.distance2DTo(WantedLoc.x(),WantedLoc.y())/3;}
	else															  {Speed = MaxLaser/3;}

	Vels.clear();
	Vels.addDouble(5);//priority
	Vels.addDouble(1);//time
	Vels.addDouble(Speed);//lin speed, will always take 2 secs to get to closest object
	Vels.addDouble(Rad2Deg(Angle));   //rot speed
	WheelOut.write();

		
	printf("current loc %s \n",MyRobotPose.asString().c_str());
	printf("target loc %f %f \n",Path.front().x,Path.front().y);
	printf("angle %f \n",Rad2Deg(Angle));

	int uu;
//	cin >> uu;
	}

	void UpdateOdo(void)
	{
		Bottle *BOdo = OdoIn.read(false);
		if(BOdo!=NULL)	{MyRobotPose=CPose2D(BOdo->get(0).asDouble(),BOdo->get(1).asDouble(),BOdo->get(2).asDouble());	}
	}

	void CheckForCommand(void)
	{
	//	puts("checking for command");
		Bottle *BIon = Ion.read(false);
		if(BIon!=NULL)	
		{
			puts("command found planning path");
			WantedLoc=CPose2D(BIon->get(0).asDouble(),BIon->get(1).asDouble(),BIon->get(2).asDouble());	
			InTransit=true;
		}

	}
	void SendReplyIon(void)
	{
		Bottle &Bion2 = Ion.prepare();
		Bion2.clear();
		Bion2.addDouble(MyRobotPose.x());
		Bion2.addDouble(MyRobotPose.y());
		Ion.write();
		Bottle &Vel=WheelOut.prepare();
		Vel.clear();
		Vel.addDouble(5);//priority
		Vel.addDouble(1);//time
		Vel.addDouble(0);//lin speed, will always take 2 secs to get to closest object
		Vel.addDouble(Rad2Deg(WantedLoc.phi()-MyRobotPose.phi())); //rot speed
		WheelOut.write();
	}
	

	void PlanPath(void)
	{
	/* you could change the map here, by altering then using the gridmap, maybe A* search, use your own path planner */


	/*	incase the validity of movement is broken we use multiple points of innitation.so if its front 
		is non valid becouse of feet, we imagine we are behind ourselves and plan path					*/
	
	if(!InTransit){return;}
	try{pathPlanning.computePath(*MyMap,MyRobotPose,WantedLoc,Path,ThereIsNoPath,-1.0f);} catch(...){}
	if(ThereIsNoPath){try{pathPlanning.computePath(*MyMap,MyRobotPose+CPose2D(-RobotRad,0,0),WantedLoc,Path,ThereIsNoPath,-1.0f);} catch(...){}}
	if(ThereIsNoPath){try{pathPlanning.computePath(*MyMap,MyRobotPose+CPose2D(0,RobotRad,0),WantedLoc,Path,ThereIsNoPath,-1.0f);} catch(...){}}
	if(ThereIsNoPath){try{pathPlanning.computePath(*MyMap,MyRobotPose+CPose2D(0,-RobotRad,0),WantedLoc,Path,ThereIsNoPath,-1.0f);} catch(...){}}

	
	}

	void UpdateLaser(void)
	{
	//	Bottle *Blaser = LaserIn.read(false);
	//	if(Blaser!=NULL)
	//	{
		CObservation2DRangeScan scan;
		scan.validRange.resize(682);
		scan.validRange.assign(682,'1');//19
		scan.aperture=4.1887902;// 240 degree scan range in radians
		scan.maxRange=4;//(m)
		scan.scan.resize(682);
		CPose3D  SensorPos(0,0,0);//(0.175,0.0,0,0,0);//21 // used this for most the demo
		scan.setSensorPose(SensorPos);
		MaxLaser =1000000;
		for(int xx=0;xx<682/*xx<Blaser->size()*/;xx++)
			{
				scan.scan[xx]=5;//Blaser->get(xx).asDouble();
				MaxLaser=3;
				//if(scan.scan[xx]!=0&&scan.scan[xx]<MaxLaser){MaxLaser=scan.scan[xx];}
			}
		MyRobotPose.normalizePhi();
	
		MyMap->fill(); // this line gets rid of the last of the scan,comment out if your sure your odo is upto the task	
		MyMap->insertObservation(&scan,&CPose3D(MyRobotPose));
	//	}
	}

	void ShowMap(void)
	{
		if(!MyMap->isEmpty())
		{
			MyMap->getAsImage(Pathimg);
			Pathimg.colorImage(Pathimg);
			CPose2D nose = MyRobotPose + CPose2D(RobotRad/2,0,0);
			Pathimg.drawCircle(MyMap->x2idx(MyRobotPose.x()),MyMap->getSizeY()-1-MyMap->y2idx(MyRobotPose.y()),RobotRad/ MyMap->getResolution(),TColor(150,50,50));
			Pathimg.drawCircle(MyMap->x2idx(nose.x()),MyMap->getSizeY()-1-MyMap->y2idx(nose.y()),(RobotRad/2)/ MyMap->getResolution(),TColor(150,50,50));

			if(!Path.empty())
				{
				for(int uu =0;uu<Path.size();uu++)
					{
					Pathimg.drawCircle(MyMap->x2idx(Path[uu].x),MyMap->getSizeY()-1-MyMap->y2idx(Path[uu].y),RobotRad/ MyMap->getResolution(),TColor(50,50,150));
					}
				}
			Window.showImage(Pathimg.scaleDouble());
		}
	}

};

