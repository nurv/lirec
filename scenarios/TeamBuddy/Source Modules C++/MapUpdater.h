
/* Module : Navigation 
   Author : K.Du Casse
   Libs	  : Using MRPT

   The concept is that working with a whole map can be computationally high, so we only add what can presently see into the map, this solves problem of out of date
   memory, scince getting the robot to do a investigation routine could be tiresome.
   Havn't used any interupts, becouse we got to make sure certain timings are maintained, so it startsup,runs and shutsdown properly and in order
   It'll get the robot to move as fast as the closest obsticle(or endpoint if thats nearer)
   and replan the route every half a secound. more than enough time to deal with realtime changes (upto a point) but not to be too computatoinally ineffiecent.

*/

#include "SamClass.h"
#include <mrpt/slam.h>
#include <mrpt/gui.h>
#include <mrpt/base.h>
#include <mrpt/utils/CTicTac.h>
#include <mrpt/hwdrivers/CSerialPort.h>
#include <mrpt/slam.h>
#include <math.h>
#include <time.h>


using namespace mrpt;
using namespace mrpt::hwdrivers;
using namespace mrpt::slam;
using namespace mrpt::gui;
using namespace mrpt::utils;
using namespace std;

#define MyRes 0.05      // this is the most important varible of all!!
						// this controls the resolution of the grid, so 0.05 is cm grid, the less res means less memmory useage and quicker responce
						// also the higher the resolution the closer it'll try to get to a object for the shortest path, which allows less error in movement
#define RobotRad 0.40

CPose2D MyRobotPose(0,0,0);  // this is the current pos of the robot is kept
CPose2D WantedLoc(0,0,0);	 // this is where the target is kept
CPose2D OldPose(0,0,0);  // usefull for debugging, dont use

COccupancyGridMap2D *MyMap;  // the map which is used
COccupancyGridMap2D *CopyMyMap; // for debugging

CMetricMap *MyMapMetric; // debugging

mrpt::gui::CDisplayWindow			Window("Map"); // a window to show the robot and the map
CImage		Scanimg,Pathimg,Mapp;				  // just a couple of images, one to put in the window and anouther to load from a mapfile
bool InTransit = false;								// a flag to show if it should be moving or not

CPathPlanningCircularRobot		pathPlanning;		// least mean squared planning algorythem
std::deque<poses::TPoint2D>		Path;				// a future path to get to the wanted location
//std::deque<poses::CPose2D>	PathHistory;		// the past path to show where it has been 
//std::deque<poses::CPose2D, Eigen::aligned_allocator<poses::CPose2D> > PathHistory;  // OK, or alternatively
//std::vector<poses::CPose2D>	PathHistory;
//mrpt::aligned_containers<poses::CPose2D>::deque_t PathHistory;
mrpt::aligned_containers<poses::CPose2D>::vector_t PathHistory;
bool ThereIsNoPath=true;							// a flag to show if a path is possible
double MaxLaser =10000;								// Debug

CICP MyICP;											// ICP method for laser localisation (as well as map building)

//// vector
//std::vector<TYPE> v;  // ERROR.
//std::vector<TYPE, Eigen::aligned_allocator<TYPE> > v;  // OK, or alternatively
//mrpt::aligned_containers<TYPE>::vector_t v;  // this version is a short-cut.

int iCntr=0;
double dAvgSpeed=0;
double dDistance = 0;
time_t start,end;


class MapUpdater: public SamClass
{
private:
Network yarp;

BufferedPort<Bottle> LaserIn;
BufferedPort<Bottle> OdoIn;
BufferedPort<Bottle> WheelOut;
BufferedPort<Bottle> Ion;
BufferedPort<Bottle> PowerOut;


public:

	
	void SamInit(void)
	{
	RecognisePort("LaserIn");
	RecognisePort("OdoIn");
	RecognisePort("ROut");
	RecognisePort("Ion");
	RecognisePort("PhidgetOut");
	
	StartModule("/MAP");


	LaserIn.open("/MAP_LaserIn"); //myPortStatus
	OdoIn.open("/MAP_OdoIn");
	Ion.open("/MAP_Ion");
	WheelOut.open("/MAP_ROut");
	PowerOut.open("/MAP_PhidgetOut");

	// Set ICP optoins here, theres a few options
	
	/*
	MyICP.options.ICP_algorithm = icpClassic;
	MyICP.options.maxIterations			= 100;
	MyICP.options.thresholdAng			= DEG2RAD(10.0f);
	MyICP.options.thresholdDist			= 0.75f;
	MyICP.options.ALFA					= 0.5f;
	MyICP.options.smallestThresholdDist	= 0.05f;
	MyICP.options.doRANSAC = false;

	MyICP.options.dumpToConsole();*/


	MyMap = new COccupancyGridMap2D(-10,10,-10,10,MyRes);		// the map is -10m to +10m in steps of 5cm
	//CopyMyMap = new COccupancyGridMap2D(-10,10,-10,10,MyRes);

	MyMap->insertionOptions.maxDistanceInsertion=3;				// the max laser range, so it doesn't detect obsticles further than this

	Mapp.loadFromFile("LabMap.bmp");							    // if you have a map then put it in here

	pathPlanning.robotRadius = RobotRad;// + RobotRadError;    // radius of the robot
	pathPlanning.minStepInReturnedPath=1.0;						// how many steps (dont worry about changing this)
	pathPlanning.occupancyThreshold=0.01;						// this is defunct unless you use icp then its the the probability of free spaces its allowed to plan paths in
	
	}
	
	
	void SamIter(void)
	{
	UpdateOdo();		// get odo
	CheckForCommand();  // see if we have a new target
	UpdateLaser();
	PlanPath();
	
	if(InTransit && ThereIsNoPath){InTransit=false;SendReplyIon();puts("no path available sorry");}// if we havn't got a path then we got to give up
	if(InTransit && MyRobotPose.distance2DTo(WantedLoc.x(),WantedLoc.y())<0.5)	
	{
		InTransit=false;
		SendReplyIon();
		puts("reached location");
		time (&end);
		double dif;
		dif = difftime (end,start);

		std::cout << "total distance m " << dDistance/100.0 << " avg speed m/s " << dAvgSpeed/iCntr << " time s " << dif << std::endl;
		Bottle& B = PowerOut.prepare();		// prepare the bottle/port
		B.clear();
		B.addInt(2);
		PowerOut.writeStrict();			
		dAvgSpeed =0;
		dDistance =0;
		iCntr=0;

			
		Bottle &Vels=WheelOut.prepare();
		Vels.clear();
		Vels.addDouble(5);//priority: if this is sent and a expressive behaviour then this will work and the express wont
		Vels.addDouble(1);//time: give everything at least one secound, cant hit anything and it wont overload the robot with data
		Vels.addDouble(0);//lin speed, will always take 2 secs or more to get to closest object
		Vels.addDouble(0);   //rot speed
		WheelOut.writeStrict();
	
	}// if we are there then we give up
	if(InTransit){SendWheelCommands();}
	ShowMap();
	yarp::os::Time::delay(0.2);
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
	iCntr++;
	
	
	//double Angle =atan2((Path.front().y-MyRobotPose.y())*1000,(Path.front().x-MyRobotPose.x())*1000); // get angle to the nearest waypoint
	double Angle =atan2((WantedLoc.y()-MyRobotPose.y())*1000,(WantedLoc.x()-MyRobotPose.x())*1000); // get angle to the nearest waypoint
	Angle-=MyRobotPose.phi();   // take into consideratoin the current angle of the robot
	CPose2D FF;
	FF = MyRobotPose + CPose2D(RobotRad/2,0,0); // get the point that is half the robots rad infront

	double clearance = MyMap->computeClearance(FF.x(),FF.y(),1); // compute clearance for that point (so we know the distance to closest object)

	std::cout << "clearance" << clearance <<std::endl;
	//MaxLaser =2;// worked well with 1
	while(Angle>3.14159265){Angle-=6.28318531;}  // normalise PI ie if over 180, then minus 360
	while(Angle<-3.14159265){Angle+=6.28318531;} //


	// we get the lowest value, the distance of a object or to the end point, smallest value is used
	double dist = MyRobotPose.distance2DTo(WantedLoc.x(),WantedLoc.y());
	dDistance+=dist;
	if(dist<clearance)
	{
		Speed = dist/2;
	}
	else	
	{
		Speed = clearance/3;
	}
	dAvgSpeed+=Speed;
	Angle/=2; // half the angle to account for delay in processing
	if(Rad2Deg(Angle)> 40){/*Angle/=2*/;Speed=0;} // if the angle is too much then just set speed to zero
	if(Rad2Deg(Angle)<-40){/*Angle/=2*/;Speed=0;} // 

	Bottle &Vels=WheelOut.prepare();
	Vels.clear();
	Vels.addDouble(5);//priority: if this is sent and a expressive behaviour then this will work and the express wont
	Vels.addDouble(1);//time: give everything at least one secound, cant hit anything and it wont overload the robot with data
	Vels.addDouble(Speed);//lin speed, will always take 2 secs or more to get to closest object
	Vels.addDouble(Rad2Deg(Angle));   //rot speed
	WheelOut.writeStrict();

	// stuff for debugging
	//printf("speed mm %f \n",Speed);	
	//printf("current loc %s \n",MyRobotPose.asString().c_str());
	//printf("target loc %f %f \n",Path.front().x,Path.front().y);
	//printf("angle %f \n",Rad2Deg(Angle));


	}

	void UpdateOdo(void)
	{
		Bottle *BOdo = OdoIn.read(false);
		if(BOdo!=NULL)	{MyRobotPose=CPose2D(BOdo->get(0).asDouble(),BOdo->get(1).asDouble(),BOdo->get(2).asDouble());}
	}

	void CheckForCommand(void)
	{
	//	puts("checking for command");
		Bottle *BIon = Ion.read(false);
		if(BIon!=NULL)	
		{
			puts("command found planning path");
			Bottle& B = PowerOut.prepare();		// prepare the bottle/port
			B.clear();
			B.addInt(1);
			PowerOut.writeStrict();	

			time (&start);
			WantedLoc=CPose2D(BIon->get(0).asDouble(),BIon->get(1).asDouble(),BIon->get(2).asDouble());	
			std::cout << "Distance from final target " << MyRobotPose.distance2DTo(WantedLoc.x(),WantedLoc.y()) << std::endl;
			InTransit=true;
		}

	}

	void SendReplyIon(void)
	{
		Bottle &Bion2 = Ion.prepare();
		Bion2.clear();
		Bion2.addDouble(MyRobotPose.x());
		Bion2.addDouble(MyRobotPose.y());
		Ion.writeStrict();
		Bottle &Vel=WheelOut.prepare();
		Vel.clear();
		Vel.addDouble(5);//priority
		Vel.addDouble(1);//time
		Vel.addDouble(0);//lin speed, will always take 2 secs to get to closest object
		if(WantedLoc.phi()!=0){Vel.addDouble(Rad2Deg(WantedLoc.phi()-MyRobotPose.phi()));} //rot speed
		else				  {Vel.addDouble(0);}
		WheelOut.writeStrict();
	}
	

	void PlanPath(void)
	{
	/* you could change the map here, by altering then using the gridmap, maybe A* search, use your own path planner */
	
	/* Ie the commands needid to search and alter 

		MyMap->getCell(x,y);
		MyMap->setCell(x,y,0.5);
	*/

	/*	incase the validity of movement is broken we use multiple points of innitation.so if its front 
		is non valid becouse of feet, we imagine we are behind ourselves and plan path					*/

			
	if(!InTransit){return;}
	try{pathPlanning.computePath(*MyMap,MyRobotPose,WantedLoc,Path,ThereIsNoPath,1);} catch(...){}//-1.0f
	if(ThereIsNoPath){try{pathPlanning.computePath(*MyMap,MyRobotPose+CPose2D(-RobotRad,0,0),WantedLoc,Path,ThereIsNoPath,-1.0f);} catch(...){}}
	if(ThereIsNoPath){try{pathPlanning.computePath(*MyMap,MyRobotPose+CPose2D(0,RobotRad,0),WantedLoc,Path,ThereIsNoPath,-1.0f);} catch(...){}}
	if(ThereIsNoPath){try{pathPlanning.computePath(*MyMap,MyRobotPose+CPose2D(0,-RobotRad,0),WantedLoc,Path,ThereIsNoPath,-1.0f);} catch(...){}}
	if(ThereIsNoPath){try{pathPlanning.computePath(*MyMap,MyRobotPose+CPose2D(RobotRad,0,0),WantedLoc,Path,ThereIsNoPath,-1.0f);} catch(...){}}
	if(ThereIsNoPath){try{pathPlanning.computePath(*MyMap,MyRobotPose+CPose2D(RobotRad,RobotRad,0),WantedLoc,Path,ThereIsNoPath,-1.0f);} catch(...){}}
	if(ThereIsNoPath){try{pathPlanning.computePath(*MyMap,MyRobotPose+CPose2D(-RobotRad,-RobotRad,0),WantedLoc,Path,ThereIsNoPath,-1.0f);} catch(...){}}
	
	}

	void UpdateLaser(void)
	{
		Bottle *Blaser = LaserIn.read(false);
		if(Blaser!=NULL)
		{
			/*
		//CObservationRange	scan;
		CObservation2DRangeScan scan;
		//scan.validRange.resize(600);
		scan.validRange.assign(8,'1');//19
		scan.aperture=3.3;//6.1887902;// 240 degree scan range in radians
		scan.maxRange=4;//(m)
		//scan.beamAperture = 0.35;//20 deg 
		scan.rightToLeft=false;
		scan.scan.resize(8);
		CPose3D  SensorPos(0.0,0.0,0.2,0);//4.7 for 16 sonars
		scan.setSensorPose(SensorPos);

		//std::cout << "sonar data " << Blaser->size() << std::endl;
		for(int xx=0;xx<Blaser->size();xx++)//682//Blaser->size()
		{
			scan.scan[xx]=Blaser->get(xx).asDouble();
			//scan.sensedData[xx].sensorID = xx;
			//scan.sensedData[xx].sensedDistance = 0.3;//Blaser->get(xx).asDouble();
		}
		MyRobotPose.normalizePhi(); */
		
		CObservation2DRangeScan scan;
		scan.validRange.resize(512);//682
		scan.validRange.assign(512,'1');//19
		scan.aperture=3.1;//4.1887902;// 240 degree scan range in radians
		scan.maxRange=4;//(m)
		scan.scan.resize(512);
		CPose3D  SensorPos(0.3,0.2,0.3);//(0.175,0.0,0,0,0);//21 // used this for most the demo
		scan.setSensorPose(SensorPos);
		/*
		std::vector<pair<double,double> >  ignoreangles;
		std::pair<double,double> range1, range2;

		range1.first = 0.0;
		range1.second = 1.5;

		range2.first = 2.65;
		range2.second = 4.2;

		

	
		ignoreangles.push_back(range1);
		ignoreangles.push_back(range2);

		//ignoreangles.at(0).first = 3.85;
		//ignoreangles.at(0).second = 4.17;

		//ignoreangles.at(1).first = 0.1;
		//ignoreangles.at(1).second = 0.35;

		scan.filterByExclusionAngles(ignoreangles);*/
		MaxLaser =1000000;
		for(int xx=0;xx<512;xx++)//682
		{
			scan.scan[xx]=Blaser->get(xx).asDouble();
		}
		MyRobotPose.normalizePhi();
		
		/* this resets the map, do this if you dont use a offline map or you are using ICP */
		MyMap->fill();		
		/* If you have a offline map uncomment this line , comment out fill above */
		MyMap->loadFromBitmap(Mapp,MyRes,Mapp.getWidth()/2,Mapp.getHeight()/2);
		/* if you want to use ICP then uncomment below and comment load bitmap and fill above */
		// if you want icp you must do it here
		//CPointsMap *m1;
		//m1->clear();
		//m1->insertObservation(&scan,&CPose3D(MyRobotPose));
		//CPosePDFPtr offset = MyICP.Align(MyMap,m1,CPose2D(0.5,0.5,Deg2Rad(90)));
		//MyRobotPose = MyRobotPose - offset->getEstimatedPose();


		MyMap->insertObservation(&scan,&CPose3D(MyRobotPose));
	
		}
	}

	// display only //
	void ShowMap(void)
	{
		if(!MyMap->isEmpty())
		{
			if(PathHistory.size()>100){PathHistory.pop_back();}
		
			if(OldPose.distance2DTo(MyRobotPose.x(),MyRobotPose.y())>0.15)
			{
			PathHistory.push_back(MyRobotPose);
			//PathHistory.insert(PathHistory.size(),MyRobotPose);
			OldPose = MyRobotPose;
			}

			MyMap->getAsImage(Pathimg);
			Pathimg.colorImage(Pathimg);

	for(int vv=1;vv<PathHistory.size();vv++)
					{
						Pathimg.drawCircle(MyMap->x2idx(PathHistory[vv].x()),MyMap->getSizeY()-1-MyMap->y2idx(PathHistory[vv].y()),RobotRad/ MyMap->getResolution(),TColor(50,150-vv,50));
						//Pathimg.line(MyMap->x2idx(PathHistory[vv-1].x()),MyMap->getSizeY()-1-MyMap->x2idx(PathHistory[vv-1].y()),MyMap->x2idx(PathHistory[vv].x()),MyMap->getSizeY()-1-MyMap->x2idx(PathHistory[vv].y()),TColor(50,150,50));
					}

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

