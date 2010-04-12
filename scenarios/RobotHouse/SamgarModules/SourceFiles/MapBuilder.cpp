
/*
 Simple Navigation Using MRPT

 programmer : K . Du Casse 
 e-mail     : k.du-casse@herts.ac.uk
 Date of original : 07 april 2010
 Date of last mod : 07 april 2010

 Version 1.0

 ALOT of this is MRPT wrapped in samgar. For full details goto the MRPT website where this is lots of usefull info, else e-mail me

 The quick runthrough :

 First part gets the laser and global cords/angle data and puts them into the map
 secound part if a destination is given it attempts to plan a path to it and gives out the first waypoint
 to be used by motion control

 
 
 
 */

/*
TODO :
1. Doesn't exit the program well
2. if it cant get to end location should replan to get as close as it can


*/




/* a few windows only commands present, should be able to deal with these though to make it cross platform*/


#include <mrpt/core.h>
#include <iostream>
#include <fstream>
#include <conio.h>
#include <windows.h>

#include "SamgarMainClass.h"

using namespace mrpt;
using namespace mrpt::slam;
using namespace mrpt::opengl;
using namespace mrpt::gui;
using namespace mrpt::system;
using namespace mrpt::math;
using namespace mrpt::utils;
using namespace std;
using namespace yarp;


/*********************************** THE VARIBLES TO CHANGE THE BEHAVIOUR *********************************************/
/******************/					static double fullspeederror =	10.00;						/******************/
/******************/					static double lowspeederror	 =  00.50;						/******************/
/******************/					static double fullspeed		 =  16.00;						/******************/
/******************/					static double lowspeed		 =	08.00;						/******************/
/******************/					static double secstoturn	 =	02.00;						/******************/
/**********************************************************************************************************************/

	mrpt::gui::CDisplayWindow		Pathwin("Computed path");
	TSetOfMetricMapInitializers		metricMapsOpts;
	CICP::TConfigParams				icpOptions;
	CConfigFile						iniFile( "icp-slam_demo.ini"); // open the file
	static double x=0,y=0,rot=0;
	static double TheAngleTheRobotThinks=0.0;
	static double RealRobotRad=0.24;
	CImage		img;     // image for the path

	Bottle BVelRot;
	Bottle Btarget;

	static CPose2D  DifOdoPose(0,0,0);  // the movement of the robot known
	static CPose2D  OldOdoPose(0,0,0);  // the old pose 
	static CPose2D  StarPose(0,0,0);	// new pose from stargazer
	static CPose2D  UpdatePose(0,0,0);
	static CPose2D  Fake(0,0,0);
	static CPose2D	target(0,0,0);
	static CPose2D  TempPose(0,0,0);
	bool HaveTarget = false;
	//	static Cpose2D  SavedPose(0,0,0);
	CPose3DPDFPtr posePDF;				// MapBuilder estimation
	CPose3D  curRobotPose;				// MapBuilder estimation
	static int bytes;
	static int errorC;

int main(void)
{
	/* yarp stuff */
	Network yarp;
	SamgarModule Mapper("MapBuilder","Navigation","Map",run);
	Mapper.AddPortS("PosIn");
	Mapper.AddPortS("LaserIn");
	Mapper.AddPortS("TargetIn");
	Mapper.AddPortS("VelRotOut");
	Mapper.AddPortS("goalAchived");

	/* map building and updating init */
	puts("loading map stuff");
	metricMapsOpts.loadFromConfigFile(iniFile, "MappingApplication"); // load params
	icpOptions.loadFromConfigFile(iniFile, "ICP");					  // load params
	CMetricMapBuilderICP mapBuilder(&metricMapsOpts,1.0,DEG2RAD(30),&icpOptions);
	mapBuilder.ICP_options.matchAgainstTheGrid	= true;
	mapBuilder.options.verbose					= true;
	mapBuilder.options.enableMapUpdating		= true;
    mapBuilder.options.debugForceInsertion		= false;
	mapBuilder.options.insertImagesAlways		= true;

	/* motion model stuff set to zero so pos isn't estimated it's real (near enough) */
	puts("motion model stuff");
	/* seems to work quite well when rotating on spot*/
	
	CActionRobotMovement2D ::TMotionModelOptions MyOptions;
	MyOptions.gausianModel.a1=0.0000002;	
	MyOptions.gausianModel.a2=0.0000002;		
	MyOptions.gausianModel.a3=0.0000002;
	MyOptions.gausianModel.a4=0.0000002;	
	MyOptions.gausianModel.minStdPHI=0.0000002;	
	MyOptions.gausianModel.minStdXY=0.0000002;


	/* stuff for the robot action */
	puts("robot action stuff");
	CActionRobotMovement2D	MyRobotAction;
	CActionCollectionPtr	action;
	action=CActionCollection::Create();

	/* stuff for the sensor */
	puts("sensor model stuff");
	CSensoryFramePtr		observations;
	CObservation2DRangeScanPtr scan = CObservation2DRangeScan::Create();
	observations =CSensoryFrame::Create();
	scan->aperture=4.1887902;// 240 degree scan range in radians
	scan->maxRange=5.6;//(m)
	scan->scan.resize(682);
	scan->validRange.assign(682,1);//19
	CPose3D  SensorPos(0.175,0.0,0,0,0);//21 // used this for most the demo
	scan->setSensorPose(SensorPos);

	
	puts("checking for last odopose");
	if(system::fileExists("LastOdoPose.odo")==true)	{CFileInputStream("LastOdoPose.odo")>>OldOdoPose;puts("loaded");}

	mapBuilder.loadCurrentMapFromFile("Robothousemap.simplemap");

	
	CPathPlanningCircularRobot	pathPlanning;
	pathPlanning.robotRadius = RealRobotRad;
	pathPlanning.minStepInReturnedPath=0.6;
	pathPlanning.occupancyThreshold=0.01;//worked well with 0.1 //// brill its the threshold of how much probability it is to take into consideration for path planning
	/// occThres 0.99 didn't go through anything 
	/// occThres 0.00 went through everything


	Bottle BBcc;
	Bottle PosMessage;
	
	std::deque<poses::TPoint2D>		thePath;
	bool	Thereisnopath;
	
	
	
		
	puts("getting inital poses");
	posePDF =  mapBuilder.getCurrentPoseEstimation();
	curRobotPose = posePDF->getEstimatedPose();	
	printf("it thinks old pose is X%f Y%f R%f \n",OldOdoPose.x(),OldOdoPose.y(),OldOdoPose.phi());
	printf("it thinks pose     is X%f Y%f R%f \n",curRobotPose.x(),curRobotPose.y(),curRobotPose.yaw());
	printf("diff               is X%f Y%f R%f \n",OldOdoPose.x()-curRobotPose.x(),OldOdoPose.y()-curRobotPose.y(),OldOdoPose.phi()-curRobotPose.yaw());

	


	static int Counter=0;
	bool FirstTimeOnly=true;
	Mapper.GetBottleData("PosIn",&PosMessage);
	Mapper.GetBottleData("LaserIn",&BBcc);
	for (;;)
	{
		//mrpt::system::sleep(50);// was 250// worked great with local server on the lappy// worked well with 700 but few dots
	
		Counter++;

		if (kbhit()){if(_getch()=='q'){break;}}

		action->clear();		// clear the last obs and action
		observations->clear();
		if(Mapper.GetBottleData("PosIn",&PosMessage)==true)
		{
			if(Mapper.GetBottleData("LaserIn",&BBcc)==true)//&&FirstTimeOnly==true)
			{
				x	= PosMessage.get(2).asDouble()/100;//.asDouble()/100;
				y	= PosMessage.get(3).asDouble()/100;//.asDouble()/100;
				rot	= PosMessage.get(1).asDouble();///57.2957795;
				rot = DEG2RAD(rot*-1);

			StarPose.x(x);StarPose.y(y);StarPose.phi(rot);
			StarPose.normalizePhi();
			DifOdoPose=StarPose-OldOdoPose;
			OldOdoPose=StarPose;

			for(int hh =0 ; hh< 681/*BB.size()*/;hh++)
				{
					scan->scan[hh]=BBcc.get(hh).asDouble()/1000;
				}

			MyRobotAction.computeFromOdometry(DifOdoPose,MyOptions);
			action->insert(MyRobotAction);
			observations->insert(scan);
			if(observations->size()>0&&action->size()>0)
				{
				try
					{
					mapBuilder.processActionObservation( *action, *observations );
					}
				catch(exception& e){cout << "except:" << e.what()<<endl;}
				}
			PosMessage.clear();
			BBcc.clear();
			}
		}
	

	
	
		if(Mapper.GetBottleData("TargetIn",&Btarget)==true)
		{
		HaveTarget=true;
		target.x(Btarget.get(0).asDouble());
		target.y(Btarget.get(1).asDouble());
		target.z(Btarget.get(2).asDouble());
		BVelRot.clear();
		BVelRot.addDouble(0);// stop the robot
		BVelRot.addDouble(0);
		Mapper.SendBottleData("VelRotOut",BVelRot);
		Thereisnopath==true;
		thePath.clear();
		errorC=0;
		}

			const CMultiMetricMap* mostLikMap =  mapBuilder.getCurrentlyBuiltMetricMap();					
			posePDF =  mapBuilder.getCurrentPoseEstimation();	// get current pose pointer of robot
			curRobotPose = posePDF->getEstimatedPose();		
			if(mostLikMap->m_gridMaps.size()!=0 )
			{
				thePath.clear();
				const COccupancyGridMap2D gridmap = *mostLikMap->m_gridMaps[0].pointer();			//gridmap.
				double R2 = round(RealRobotRad/ gridmap.getResolution() );
			
				gridmap.getAsImage(img,false, true);
				img.drawCircle(gridmap.x2idx(curRobotPose.x()),gridmap.y2idx(curRobotPose.y()*-1),R2,TColor(0,200,0),1 );
				img.drawCircle(gridmap.x2idx(target.x()),gridmap.y2idx(target.y()*-1),R2,TColor(200,0,0),1 );

				if(target.distance2DTo(curRobotPose.x(),curRobotPose.y())<0.1&&HaveTarget==true)	// if the distance is less than 10cm
				{
				Bottle BBB;
				BBB.clear();
				BBB.addDouble(target.x());
				BBB.addDouble(target.y());
				BVelRot.clear();
				HaveTarget=false;
				BVelRot.addDouble(secstoturn);         // speed of rot, take three secounds to geto right angle          
				BVelRot.addDouble(target.z());// angle wanted
				BVelRot.addDouble(0);		  // error is zero (so it turns on the spot	
				Mapper.SendBottleData("goalAchived",BBB);
				Mapper.SendBottleData("VelRotOut",BVelRot);
				}

				if(HaveTarget==true)//&&Counter%5==1)
				{
				Thereisnopath=true;
				pathPlanning.robotRadius=RealRobotRad*2.0;//}
					
				try	{pathPlanning.computePath(gridmap,curRobotPose,target, thePath,Thereisnopath, 1000.0f  );}catch(exception& e)	{cout << "except:" << e.what()<<endl;}
				
				if(Thereisnopath==true)
				{
				pathPlanning.robotRadius=RealRobotRad*1.2;
				try	{pathPlanning.computePath(gridmap,curRobotPose,target, thePath,Thereisnopath, 1000.0f  );}catch(exception& e)	{cout << "except:" << e.what()<<endl;}
				}
				if(Thereisnopath==true)
				{
				//	errorC=0;
				puts("UNABLE TO REACH TARGET WITH KNOWN ENVIROMENT, EITHER INPUT ERROR OR SEND ROBOT TO MIDWAY POINTS");

				// sends back the current robot pose instead of target to let it know its failed.
				Bottle BBB;
				BBB.clear();
				BBB.addDouble(curRobotPose.x());
				BBB.addDouble(curRobotPose.y());
				BVelRot.clear();
				HaveTarget=false;
				BVelRot.addDouble(0);
				BVelRot.addDouble(0);
				BVelRot.addDouble(0);
				Mapper.SendBottleData("goalAchived",BBB);
				Mapper.SendBottleData("VelRotOut",BVelRot);
				}
		

				CPose2D temptarget;
				temptarget = target;
				double Xmis;
				double Ymis;
				Xmis =(target.x()-curRobotPose.x())/10;
				Ymis =(target.y()-curRobotPose.y())/10;
				int Pathcounter =0;
				Pathcounter=0;
			}
			if(Thereisnopath==false&&HaveTarget==true)
				{
					int Wp =0,WX=0,WY=0;
					Wp =0;

					for (std::deque<poses::TPoint2D>::const_iterator it=thePath.begin();it!=thePath.end();++it)
						{
						if(Wp==0){WX=it->x;WY=it->y;}Wp++;
						img.drawCircle( gridmap.x2idx(it->x),gridmap.getSizeY()-1-gridmap.y2idx(it->y),(pathPlanning.robotRadius)/ gridmap.getResolution(),TColor(0,0,255) );
						}

				BVelRot.clear();
				
				double housewantedangle = atan2(WY-curRobotPose.y(),WX-curRobotPose.x());
				double copyofrobotrot = curRobotPose.yaw();
				
				if(copyofrobotrot<0)  {copyofrobotrot  +=DEG2RAD(360);}
				if(housewantedangle<0){housewantedangle+=DEG2RAD(360);}

				double angletosend=0;
	
				if(pathPlanning.robotRadius==RealRobotRad*1.2) // tight path, go slow
				{
				BVelRot.addDouble(lowspeed); // speed// wokred well but slowly with 20 works ok with 80
				BVelRot.addDouble(RAD2DEG(housewantedangle));//angle to send
				BVelRot.addDouble(lowspeederror);//angle to send
				}
				else
				{
				BVelRot.addDouble(fullspeed); // speed// wokred well but slowly with 20 works ok with 80
				BVelRot.addDouble(RAD2DEG(housewantedangle));//angle to send
				BVelRot.addDouble(fullspeederror);//angle to send
				}
				
				Mapper.SendBottleData("VelRotOut",BVelRot);
				}
			else if(HaveTarget==true)
				{
				BVelRot.clear();			BVelRot.addDouble(0);			BVelRot.addDouble(0);		Mapper.SendBottleData("VelRotOut",BVelRot);
				}
			}

			if(Counter>10){Counter=0;errorC=0;}
				try
					{
						Pathwin.showImage(img.scaleHalf());
					}
				catch(exception& e){cout << "except:" << e.what()<<endl;}
	}


	puts("system shutdown");
	//mrpt::system::sleep(2000);
	puts("saving file");
	mapBuilder.saveCurrentMapToFile("Robothousemap.simplemap",false);
	//action.clear_unique();
	//observations.clear_unique();
	puts("saved file");
	mapBuilder.~CMetricMapBuilderICP();
	Pathwin.~CDisplayWindow();
	pathPlanning.~CPathPlanningCircularRobot();

	//mrpt::system::sleep(1000);
	//yarp.fini();

	return 0;
}
