
/*
	MRPT MapBuilding and Path planning

	I have tryed to make the code as excessable to users as possible, So everything is method based and the only slightly tricky thing is the 
	externs and multhreading, although I have not used semiphores but thread delays instead (critical sections based)

	builds a map from odometry and laser data
	plan a path using min distance
	sends the command to vrobot

	Can crash rarely, is being updated to new versoin soon (new version will have many improvments)


*/





#define CreateVars
#include <MapHelper.h>

bool KyronSaysWeAreThere=false;  
int main (void)
{
	OnEXIT=false;

/* created the samgar module */
Network yarp;
SamgarModule Mapper("MapBuilder","Navigation","Map",SamgarModule::run);
			 Mapper.AddPortS("GlobalPosIn");  // the global odometry from level 1
			 Mapper.AddPortS("LaserIn");	  // laser readings from level 1
			 Mapper.AddPortS("TargetIn");	  // target posistion from cmion
			 Mapper.AddPortS("LocationReached"); // confirmation to level three
			 Mapper.AddPortS("VrobotOut");

puts("stated mod");
//LoadMapBuilder();      // mapinnit;
puts("loaded map");
LoadMovementParams();  // guassian distribution on posistion beleif innit
puts("loading move");
LoadLaserParams();     // laser innit
puts("loaded laser");
LoadColors();          // define some colors
LoadPathPlanner();     // innit for the path planner

LoadMapBuilder();

TThreadHandle GuiThread  = createThread(ShowMap);    // create a thread using the showmap method, only GUI stuff
TThreadHandle PlanThread = createThread(PlanPath);   // create a thread using the planpath method, only for path planning


						
while(1)
{
	try	{

	yarp::os::Time::delay(MainWait);     // stop the main loop using the whole cpu

	if (kbhit())
	{
		char mychar = getch(); 
		if(mychar=='q'){break;}
		if(mychar=='1')
		{
		PrimayTarget.x(5);
		PrimayTarget.y(0);
		PrimayTarget.phi(90);
		HaveTarget=true;
		Thereisnopath.set(false);//=false;
		}
		if(mychar=='2')
		{
		PrimayTarget.x(0);
		PrimayTarget.y(0);
		PrimayTarget.phi(0);
		HaveTarget=true;
		Thereisnopath.set(false);//=false;
		}
		if(mychar=='w')
		{
		Bottle ConfirmWithFatima;
		ConfirmWithFatima.clear();
		ConfirmWithFatima.addDouble(0);
		ConfirmWithFatima.addDouble(0);
		Mapper.SendBottleData("LocationReached",ConfirmWithFatima);
		JustReachedTarget=false;
		HaveTarget=false;
		}

	}  // quit

	GetLatestLaserData(&Mapper);         // pass on the samgarmodule and update the laser
	GetLatestOdoData(&Mapper);			 // pass on the samgarmodule and update the odo
	GetTarget(&Mapper);					 // pass on the samgarmodule and see if theres a new target


// get the occupancy gridmap and the current estimated pose from map
const CMultiMetricMap* mostLikMap =  mapBuilder->getCurrentlyBuiltMetricMap();	
CPose3DPDFPtr posePDF = mapBuilder->getCurrentPoseEstimation();	// get current pose pointer of robot
const COccupancyGridMap2D gridmap = *mostLikMap->m_gridMaps[0 ].pointer();	


if(HaveTarget==true) // if i have a target
{
//	puts("have target");
//	BotVelAngError.clear();
//	if(!HaveReachedDestination(posePDF->getEstimatedPose())) // this works out the action if its close to the target, if false its not near
//	{
//		GetAngleAndSetSpeed (posePDF->getEstimatedPose());   // this works out the action if its far away 
//	}

//	if(BotVelAngError.size()>0)
//	{
//	printf("sending data : %s \n",BotVelAngError.toString().c_str());
//	Mapper.SendBottleData("VrobotOut",BotVelAngError);       // send the action to the robot
//	}
//	if(KyronSaysWeAreThere==true)
//	if(JustReachedTarget==true)  // if it is on the target this iteration send its current posistion back to Cmion/Fatima
//	{
/*
		printf("sening out confirmation that we have reached location \n");
	Bottle ConfirmWithFatima;
	ConfirmWithFatima.clear();
	ConfirmWithFatima.addDouble(posePDF->getEstimatedPose().x());
	ConfirmWithFatima.addDouble(posePDF->getEstimatedPose().y());
	Mapper.SendBottleData("LocationReached",ConfirmWithFatima);
	JustReachedTarget=false;
	HaveTarget=false;
	KyronSaysWeAreThere=false;
*/
//	}
}
csCounter.enter();   // pause all threads and update varibles they will acess
PrimayTarget2=PrimayTarget;
PathWhereImGoing2 = PathWhereImGoing;
PathWhereImGoingMap = PathWhereImGoing;
GuiGridmap = gridmap;
GuicurRobotPose = posePDF->getEstimatedPose();
PathGridmap = gridmap;
PathcurRobotPose = posePDF->getEstimatedPose();
csCounter.leave();
	}
catch(exception& e)	
	{
		int ii;

		cout << "except:" << e.what()<<endl;
		cin>>ii;

	}
}

EXIT();  // deconstuct everything


return 0;
}


// if its not near the target then work out the angle needid to the closet waypoint and go towards it
void GetAngleAndSetSpeed (CPose3D RobotCurrent)
{
	if(PathWhereImGoing2.size()>0)
	{
	double wantedangle = atan2(PathWhereImGoing2.front().y - RobotCurrent.y(),PathWhereImGoing2.front().x - RobotCurrent.x());

			BotVelAngError.clear();
			BotVelAngError.addDouble(1); // keep movement
			BotVelAngError.addDouble(Speed); // speed
			BotVelAngError.addDouble(RAD2DEG(wantedangle)); // angle
			BotVelAngError.addDouble(ErrorBox); // accuracy of 1 cm
			//printf(" angle wanted %f \n",RAD2DEG(wantedangle));
	}
}

// work out if its reached destination and if it has then make sure angle is right before closing up
bool HaveReachedDestination(CPose3D RobotCurrent)
{
	static int errorcount =0;
	BotVelAngError.clear();


if(Thereisnopath)	
		{
			BotVelAngError.addDouble(0); // stop all movement from us
			BotVelAngError.addDouble(0);
			BotVelAngError.addDouble(0);
			BotVelAngError.addDouble(0);
		//	HaveTarget=false;
			errorcount=0;
			return true;
		}
else
{
	distoobjective=PrimayTarget.distance2DTo(RobotCurrent.x(),RobotCurrent.y());

if(distoobjective<0.1&&HaveTarget==true)
	{
		if(abs(PrimayTarget.phi()-RAD2DEG(RobotCurrent.yaw()))<10)// if the rotation needid is less than 5 deg
		{
			BotVelAngError.addDouble(0); // stop all movement from us
			BotVelAngError.addDouble(0);
			BotVelAngError.addDouble(0);
			BotVelAngError.addDouble(0);
			HaveTarget=false;
			JustReachedTarget=true;
			PathWhereImGoing2.clear();
		}
		else
		{
			// just rotate
			BotVelAngError.addDouble(1); // keep movement
			BotVelAngError.addDouble(0);//Speed); // speed
			BotVelAngError.addDouble(PrimayTarget.phi()); // angle
			BotVelAngError.addDouble(0.01); // accuracy of 1 cm
		}
	return true;
	}
}
return false;
}

void GetTarget(SamgarModule *tempmodule)
{
	Bottle TargetData;

	if(tempmodule->GetBottleData("TargetIn",&TargetData,SamgarModule::NoStep)) // if there is data.
	{
		puts("got target");
		PrimayTarget.x(TargetData.get(0).asDouble());
		PrimayTarget.y(TargetData.get(1).asDouble());
		PrimayTarget.z(TargetData.get(2).asDouble());
		printf("location wanted : %s \n",TargetData.get(3).asString().c_str());
		HaveTarget=true;
		Thereisnopath.set(false);// assume there is a path to begin with
	}
}

// deconstruct everything
void EXIT(void)
{
OnEXIT=true;
yarp::os::Network::fini();
yarp::os::Time::delay(1);
//mapBuilder->saveCurrentMapToFile("Robothousemap.simplemap",false);
img.~CImage();
mapBuilder->~CMetricMapBuilderICP();
yarp::os::Time::delay(1);
Pathwin.~CDisplayWindow();
MyRobotAction.~CActionRobotMovement2D();
action->~CActionCollection();
observations->~CSensoryFrame();
scan->~CObservation2DRangeScan();
csCounter.~CCriticalSection();
iniFile.~CConfigFile();
PathWhereImGoing.~deque();
PathWhereIveBeen.~deque();
pathPlanning.~CPathPlanningCircularRobot();
GuiGridmap.~COccupancyGridMap2D();
PathGridmap.~COccupancyGridMap2D();




}


// for the gui, get the new posistion if far enough away from the old one then update a list with old movements
void CalcTraveledPath(CPose3D MCP)
{
	if(PathWhereIveBeen.empty())
		{
		TPoint2D MyPos;
		MyPos.x=MCP.x();
		MyPos.y=MCP.y();
		PathWhereIveBeen.push_front(MyPos);
		}
		else
		{
//
			if(PathWhereIveBeen.size()>ShadowPathLength){PathWhereIveBeen.resize(ShadowPathLength);}
			TPoint2D OldPos=	PathWhereIveBeen.front();
		if(MCP.distance2DTo(OldPos.x,OldPos.y)>RobotRad*2)
			{
			TPoint2D NewPos;
			NewPos.x=MCP.x();
			NewPos.y=MCP.y();
			PathWhereIveBeen.push_front(NewPos);
			}

		}


}

// if there is a target then plan the path to it
void PlanPath(void)
{
static int numberpathtrys = 0;
bool internalpath=false;
while(1)
{
try	{
	if(OnEXIT==true){break;}
	yarp::os::Time::delay(PlanWait);

	if(HaveTarget)
	{
		if(!PathGridmap.isEmpty() && PrimayTarget2.x()< PathGridmap.getXMax() && PrimayTarget2.x()< PathGridmap.getYMax() && PrimayTarget2.x()> PathGridmap.getXMin() && PrimayTarget2.y()> PathGridmap.getYMin())
				{
					try	
						{
						pathPlanning.computePath(PathGridmap,PathcurRobotPose,PrimayTarget2,PathWhereImGoing,internalpath, -1.0f  );
						Thereisnopath.set(internalpath);
					}
					catch(exception& e)	{cout << "except:" << e.what()<<endl;}
		}
		
	}
}
catch(exception& e)	{cout << "except:" << e.what()<<endl;}
}



puts("exited PathPlan");
}


void LoadPathPlanner(void)
{
pathPlanning.robotRadius = RobotRad;// + RobotRadError;
pathPlanning.minStepInReturnedPath=0.6;
pathPlanning.occupancyThreshold=0.01;

}

void ClearProcessedData(void)
{
action->clear();		// clear the last obs and action
observations->clear();
//printf("The cleard obs size is %i \n",observations->size());
}


void LoadColors(void)
{
Red			= TColor(255,0,0);
Blue		= TColor(0,0,255);
Green		= TColor(0,255,0);
OffSetColor	= TColor(2,2,2);

TConsoleColor  Ccolor;
Ccolor = TConsoleColor::CONCOL_GREEN;

setConsoleColor(Ccolor,false);
}

// just show the map and add some lines etc to make it easer for the user to understand what the robots doing
// usefull for debug but otherwise window dressing
void ShowMap(void)
{

while(1)
{
	try{
	if(OnEXIT==true){break;}
	yarp::os::Time::delay(GuiWait);

	if(!GuiGridmap.isEmpty())
			{

			CPose2D Rightset(GuicurRobotPose);
			CPose2D Leftset(GuicurRobotPose);

			CPose2D RobotLeft(GuicurRobotPose);
			CPose2D RobotTop(GuicurRobotPose);
			CPose2D RobotRight(GuicurRobotPose);

			RobotLeft  = RobotLeft + CPose2D(0,-RobotRad,0);
			RobotRight = RobotRight + CPose2D(0,RobotRad,0);
			RobotTop   = RobotRight + CPose2D(RobotRad*2,-RobotRad,0);



			Rightset = Rightset + CPose2D(0,0,DEG2RAD(120));
			Rightset = Rightset + CPose2D(0.7,0,0);

			Leftset = Leftset + CPose2D(0,0,DEG2RAD(-120));
			Leftset = Leftset + CPose2D(0.7,0,0);

			
			GuiGridmap.getAsImage(img,false, true);

			CalcTraveledPath(GuicurRobotPose);
			DrawMyTraveledPath(GuicurRobotPose);
			if((!Thereisnopath) && HaveTarget){DrawMyFuturePath(GuicurRobotPose);}


			img.drawCircle( GuiGridmap.x2idx(GuicurRobotPose.x()),GuiGridmap.getSizeY()-1-GuiGridmap.y2idx(GuicurRobotPose.y()),RobotRad/ GuiGridmap.getResolution(),Green);
			img.drawCircle( GuiGridmap.x2idx(Rightset.x()),GuiGridmap.getSizeY()-1-GuiGridmap.y2idx(Rightset.y()),(RobotRad/2)/ GuiGridmap.getResolution(),Green);
			img.drawCircle( GuiGridmap.x2idx(Leftset.x()),GuiGridmap.getSizeY()-1-GuiGridmap.y2idx(Leftset.y()),(RobotRad/2)/ GuiGridmap.getResolution(),Green);

			DrawPointy(RobotLeft,RobotRight,RobotTop);

			Pathwin.showImage(img.scaleHalf());

			//printf("X %f Y %f rot %f \n",GuicurRobotPose.x(),GuicurRobotPose.x(),RAD2DEG(GuicurRobotPose.yaw()));
			}
}
catch(exception& e)	{cout << "except:" << e.what()<<endl;}
}
puts("exited GUI");
}


// draw the path the robot has taken
void DrawMyTraveledPath(CPose3D mycurrentpos)
{

	if(!PathWhereIveBeen.empty())
	{
		std::deque<poses::TPoint2D>::const_iterator it2=PathWhereIveBeen.begin();
		for (std::deque<poses::TPoint2D>::const_iterator it=PathWhereIveBeen.begin();it!=PathWhereIveBeen.end();++it)
						{
						img.drawCircle( GuiGridmap.x2idx(it->x),GuiGridmap.getSizeY()-1-GuiGridmap.y2idx(it->y),(RobotRad)/ GuiGridmap.getResolution(),Blue );

						if(it!=PathWhereIveBeen.begin())
							{
								it2=it;
								--it2;
							    DrawMyLine(it->x,it->y,it2->x,it2->y,Blue);
							}
						}
	}
}
// draw the future path
void DrawMyFuturePath(CPose3D mycurrentpos)
{

	if(!PathWhereImGoingMap.empty())
	{
		std::deque<poses::TPoint2D>::const_iterator it2=PathWhereImGoingMap.begin();
		for (std::deque<poses::TPoint2D>::const_iterator it=PathWhereImGoingMap.begin();it!=PathWhereImGoingMap.end();++it)
						{
						img.drawCircle( GuiGridmap.x2idx(it->x),GuiGridmap.getSizeY()-1-GuiGridmap.y2idx(it->y),(RobotRad)/ GuiGridmap.getResolution(),Red );

						if(it!=PathWhereImGoingMap.begin())
							{
								it2=it;
								--it2;
							    DrawMyLine(it->x,it->y,it2->x,it2->y,Red);
							}
						}
	}

}
// draw the point for the robot
void DrawPointy(CPose2D left,CPose2D right,CPose2D top)
{
	DrawMyLine(left.x(),left.y(),top.x(),top.y(),Green);
	DrawMyLine(right.x(),right.y(),top.x(),top.y(),Green);
}

// just draw a line
void DrawMyLine(double x1,double y1,double x2,double y2,TColor color)
{
x1=GuiGridmap.x2idx(x1);
y1=GuiGridmap.getSizeY()-1-GuiGridmap.y2idx(y1);
x2=GuiGridmap.x2idx(x2);
y2=GuiGridmap.getSizeY()-1-GuiGridmap.y2idx(y2);
img.line(x1,y1,x2,y2,color,1);

}

void GetLatestLaserData(SamgarModule *tempmodule)
{

	if(HasGotInitialPosistion==true)
	{

	Bottle LaserData;
	if(tempmodule->GetBottleData("LaserIn",&LaserData,SamgarModule::Step)) // if there is data.
	{
		Speed    = 1000;
		ErrorBox = 1000;
		LoadLaserParams();
	//	float distancetoobjective = PrimayTarget.distance2DTo(RobotCurrent.x(),RobotCurrent.y())
		//scan = CObservation2DRangeScan::Create();
	for(int hh =0 ; hh< validrangesize ;hh++)
				{
		//			scan->scan[hh] = 3.9;//hh/100;

					scan->scan[hh]=LaserData.get(hh).asDouble()/1000;

					if((scan->scan[hh]/4)<Speed && scan->scan[hh]>0)
					{
						Speed     = scan->scan[hh]/4; // go at a speed where the closest object is always 2 secounds away
						ErrorBox  = scan->scan[hh]/2; // make sure the robot gets to a angle in a 1/4 of the distance before the object
						if(Speed>distoobjective/2){Speed=distoobjective/2;}
					}
					
				}
	mapBuilder->processObservation(scan);
	}
	}
}

void GetLatestOdoData(SamgarModule *tempmodule)
{
	Bottle OdoData;
	static double rot =0;


	if(tempmodule->GetBottleData("GlobalPosIn",&OdoData,SamgarModule::Step)) // if there is data.
	{
		
		CPose2D newPose(OdoData.get(0).asDouble(),OdoData.get(1).asDouble(),DEG2RAD(OdoData.get(2).asDouble()));
	   // static CPose2D newPose(0,0,DEG2RAD(0));
		CPose2D xx(0,0,DEG2RAD(10));

	//	newPose+=xx;
		newPose.normalizePhi();
	//	CPose2D DiffInOdo = GetDiffOdo(newPose);

		
		// update initial posistion , one time only;
		if(HasGotInitialPosistion == false)
		{
		CPosePDFPtr MyFirstPose;
		MyFirstPose =  	CPosePDFGaussian::Create();
			
		MyFirstPose->changeCoordinatesReference(CPose3D(OdoData.get(0).asDouble(),OdoData.get(1).asDouble(),DEG2RAD(OdoData.get(2).asDouble()),0,0));
		MyMap = CSimpleMap::Create();
		mapBuilder->getCurrentlyBuiltMap(*MyMap);

		mapBuilder->initialize(*MyMap,&*MyFirstPose);
		//	mapBuilder->
		}

		
	    GlobalOdo = CObservationOdometry::Create();
		GlobalOdo->odometry = newPose;

		try				   {mapBuilder->processObservation(GlobalOdo);}
	    catch(exception& e){cout << "except:" << e.what()<<endl;}

		HasGotInitialPosistion=true;
		
	}
}


/*
Seems to work great, all is good;
*/
CPose2D GetDiffOdo(CPose2D NewPose)
{
 static CPose2D OldOdoPose(0,0,0);
 CPose2D OdoDiff(0.0,0.0,DEG2RAD(0)); 

/// experimental 
OldOdoPose.x(GuicurRobotPose.x());
OldOdoPose.y(GuicurRobotPose.y());
OldOdoPose.phi(GuicurRobotPose.yaw());



 ////

 OdoDiff = NewPose - OldOdoPose;
 OldOdoPose = NewPose;

return OdoDiff;

}

void LoadMapBuilder(void)
{
	metricMapsOpts.loadFromConfigFile(iniFile, "MappingApplication"); // load params
	icpOptions.loadFromConfigFile(iniFile, "ICP");					  // load params
	mapBuilder = new CMetricMapBuilderICP(&metricMapsOpts,1.0,DEG2RAD(30),&icpOptions);
	
	mapBuilder->ICP_options.matchAgainstTheGrid = true;
	mapBuilder->options.verbose					= false;
	mapBuilder->options.enableMapUpdating		= true;
	mapBuilder->options.debugForceInsertion		= false;
	mapBuilder->options.insertImagesAlways		= true;
	mapBuilder->enableMapUpdating(true);

	mapBuilder->setCurrentMapFile("Robothousemap.simplemap");

	
}

void LoadMovementParams(void)
{
	MyOptions.gausianModel.a1=0.500002;//002;	
	MyOptions.gausianModel.a2=0.500002;//002;		
	MyOptions.gausianModel.a3=0.500002;//002;
	MyOptions.gausianModel.a4=0.500002;//002;	
	MyOptions.gausianModel.minStdPHI=0.500002;//002;	
	MyOptions.gausianModel.minStdXY=0.5000002;//02;

	action=CActionCollection::Create();
	
	odoupdate = false;

	GlobalOdo = CObservationOdometry::Create();
	
//	Mapper->AddPortS("GlobalPosIn");
}

void LoadLaserParams(void)
{

	

	observations =CSensoryFrame::Create();
	scan = CObservation2DRangeScan::Create();

	scan->validRange.resize(validrangesize);
	scan->validRange.assign(validrangesize,'1');//19
	

	scan->aperture=4.1887902;// 240 degree scan range in radians
	scan->maxRange=4;//(m)

	scan->scan.resize(682);
	
	CPose3D  SensorPos(0.175,0.0,0,0,0);//21 // used this for most the demo
	scan->setSensorPose(SensorPos);

}