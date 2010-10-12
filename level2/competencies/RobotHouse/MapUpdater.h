
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

CPose2D MyRobotPose(0,0,0);  // this is the current pos of the robot is kept
CPose2D WantedLoc  (5,5,0);	 // this is where the target is kept
CPose2D OldPose	   (0,0,0);  // usefull for debugging, dont use

COccupancyGridMap2D *MyMap;  // the map which is used
COccupancyGridMap2D *CopyMyMap; // for debugging




CMetricMap *MyMapMetric; // debugging

mrpt::gui::CDisplayWindow			Window("Map"); // a window to show the robot and the map
CImage		Scanimg,Pathimg,Mapp;				  // just a couple of images, one to put in the window and anouther to load from a mapfile
bool InTransit = false;								// a flag to show if it should be moving or not

CPathPlanningCircularRobot		pathPlanning;		// least mean squared planning algorythem
std::deque<poses::TPoint2D>		Path;				// a future path to get to the wanted location
std::deque<poses::CPose2D>		PathHistory;		// the past path to show where it has been 
bool ThereIsNoPath=true;							// a flag to show if a path is possible
double MaxLaser =10000;								// Debug


CICP MyICP;											// ICP method for laser localisation (as well as map building)

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
	StartModule("/MAP");


	LaserIn.open("/MAP_LaserIn"); //myPortStatus
	OdoIn.open("/MAP_OdoIn");
	Ion.open("/MAP_Ion");
	WheelOut.open("/MAP_ROut");

	// Set ICP optoins here, theres a few options
	//MyICP.options.ALFA=0.2;


	MyMap = new COccupancyGridMap2D(-10,10,-10,10,MyRes);		// the map is -10m to +10m in steps of 5cm
	CopyMyMap = new COccupancyGridMap2D(-10,10,-10,10,MyRes);

	MyMap->insertionOptions.maxDistanceInsertion=3;				// the max laser range, so it doesn't detect obsticles further than this


	Mapp.loadFromFile("myMap.bmp");							    // if you have a map then put it in here
	


	pathPlanning.robotRadius = RobotRad;// + RobotRadError;    // radius of the robot
	pathPlanning.minStepInReturnedPath=0.6;						// how many steps (dont worry about changing this)
	pathPlanning.occupancyThreshold=0.01;						// this is defunct unless you use icp then its the the probability of free spaces its allowed to plan paths in
	}
	
	
	void SamIter(void)
	{
	UpdateOdo();		// get odo
	CheckForCommand();  // see if we have a new target
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
	
	

	
	
	double Angle =atan2((Path.front().y-MyRobotPose.y())*1000,(Path.front().x-MyRobotPose.x())*1000); // get angle to the nearest waypoint
	Angle-=MyRobotPose.phi();   // take into consideratoin the current angle of the robot
	CPose2D FF;
	FF = MyRobotPose + CPose2D(RobotRad/2,0,0); // get the point that is half the robots rad infront

	double clearance = MyMap->computeClearance(FF.x(),FF.y(),1); // compute clearance for that point (so we know the distance to closest object)

	//MaxLaser =2;// worked well with 1
	while(Angle>3.14159265){Angle-=6.28318531;}  // normalise PI ie if over 180, then minus 360
	while(Angle<-3.14159265){Angle+=6.28318531;} //



	// we get the lowest value, the distance of a object or to the end point, smallest value is used
	if(MyRobotPose.distance2DTo(WantedLoc.x(),WantedLoc.y())<clearance){Speed = MyRobotPose.distance2DTo(WantedLoc.x(),WantedLoc.y())/4;}
	else															   {Speed = clearance/5;}
	Angle/=2; // half the angle to account for delay in processing
	if(Rad2Deg(Angle)> 40){/*Angle/=2;*/Speed=0;} // if the angle is too much then just set speed to zero
	if(Rad2Deg(Angle)<-40){/*Angle/=2;*/Speed=0;} // 




	Vels.clear();
	Vels.addDouble(5);//priority: if this is sent and a expressive behaviour then this will work and the express wont
	Vels.addDouble(1);//time: give everything at least one secound, cant hit anything and it wont overload the robot with data
	Vels.addDouble(Speed);//lin speed, will always take 2 secs or more to get to closest object
	Vels.addDouble(Rad2Deg(Angle));   //rot speed
	WheelOut.write();

	// stuff for debugging
	//printf("speed mm %f \n",Speed*1000);	
	//printf("current loc %s \n",MyRobotPose.asString().c_str());
	//printf("target loc %f %f \n",Path.front().x,Path.front().y);
	//printf("angle %f \n",Rad2Deg(Angle));

	// to debug gets it to stop here
	int uu;
//	cin >> uu;
	}

	void UpdateOdo(void)
	{
		Bottle *BOdo = OdoIn.read(false);
		if(BOdo!=NULL)	{MyRobotPose=CPose2D(BOdo->get(0).asDouble(),BOdo->get(1).asDouble(),BOdo->get(2).asDouble());puts("got odo data");	}
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

	//if we have reached location or have no path send current cords to CMion and set the robot to point the right way
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
		if(WantedLoc.phi()!=0){Vel.addDouble(Rad2Deg(WantedLoc.phi()-MyRobotPose.phi()));} //rot speed
		else				  {Vel.addDouble(0);}
		WheelOut.write();
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
	try{pathPlanning.computePath(*MyMap,MyRobotPose,WantedLoc,Path,ThereIsNoPath,-1.0f);} catch(...){}
	if(ThereIsNoPath){try{pathPlanning.computePath(*MyMap,MyRobotPose+CPose2D(-RobotRad,0,0),WantedLoc,Path,ThereIsNoPath,-1.0f);} catch(...){}}
	if(ThereIsNoPath){try{pathPlanning.computePath(*MyMap,MyRobotPose+CPose2D(0,RobotRad,0),WantedLoc,Path,ThereIsNoPath,-1.0f);} catch(...){}}
	if(ThereIsNoPath){try{pathPlanning.computePath(*MyMap,MyRobotPose+CPose2D(0,-RobotRad,0),WantedLoc,Path,ThereIsNoPath,-1.0f);} catch(...){}}

	
	}

	void UpdateLaser(void)
	{
		Bottle *Blaser = LaserIn.read(false);
		if(Blaser!=NULL)
		{
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
				scan.scan[xx]=0;//Blaser->get(xx).asDouble();
			}
		MyRobotPose.normalizePhi();
	
		/* this resets the map do this if you dont use a offline map or you are using ICP */
		MyMap->fill();		
		/* If you have a offline map uncomment this line , comment out fill above */
		//MyMap->loadFromBitmap(Mapp,MyRes,Mapp.getWidth()/2,Mapp.getHeight()/2);
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
			PathHistory.push_front(MyRobotPose);
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

