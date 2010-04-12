

#include <mrpt/core.h>
#include <iostream>
#include <fstream>
#include <conio.h>
//#include <stdio.h>
//#include <stdlib>
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


	CDisplayWindow3D				win("Delayed time map",640,480);
	mrpt::gui::CDisplayWindow		Pathwin("Computed path");

	TSetOfMetricMapInitializers		metricMapsOpts;
	CICP::TConfigParams				icpOptions;
	CConfigFile						iniFile( "icp-slam_demo.ini"); // open the file
	static double x=0,y=0,rot=0;

	CImage		img;     // image for the path

	Bottle PosMessage;

	static CPose2D  DifOdoPose(0,0,0);  // the movement of the robot known
	static CPose2D  OldOdoPose(0,0,0);  // the old pose 
	static CPose2D  StarPose(0,0,0);	// new pose from stargazer
	static CPose2D  UpdatePose(0,0,0);
	static CPose2D  Fake(0,0,0);

	//	static Cpose2D  SavedPose(0,0,0);
	CPose3DPDFPtr posePDF;				// MapBuilder estimation
	CPose3D  curRobotPose;				// MapBuilder estimation
	static int bytes;

int main(void)
{
	/* yarp stuff */
	Network yarp;
	SamgarModule Mapper("MapBuilder","Navigation","Map",run);
	Mapper.AddPortS("PosIn");
	Mapper.AddPortS("LaserIn");
	Mapper.AddPortS("TargetIn");
	Mapper.AddPortS("WaypointOut");

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

	/* painting thing */
	puts("loading 3d window stuff");
	COpenGLScenePtr &theScene    = win.get3DSceneAndLock();							// create the scene
	opengl::CGridPlaneXYPtr obj1 = opengl::CGridPlaneXY::Create(-20,20,-20,20,0,1);	// chuck grid into it
	obj1->setColor(0.4,0.4,0.4);													// set its color
	theScene->insert( obj1 );														// chuck the rid it	
	win.unlockAccess3DScene();														// unlock
	win.setCameraZoom(20);
	win.setCameraAzimuthDeg(-45);

	
	/* motion model stuff set to zero so pos isn't estimated it's real (near enough) */
	puts("motion model stuff");
	/* seems to work quite well when rotating on spot
	CActionRobotMovement2D ::TMotionModelOptions MyOptions;
	MyOptions.gausianModel.a1=0.002;	
	MyOptions.gausianModel.a2=0.002;		
	MyOptions.gausianModel.a3=0.002;
	MyOptions.gausianModel.a4=0.002;	
	MyOptions.gausianModel.minStdPHI=0.002;	
	MyOptions.gausianModel.minStdXY=0.002;
	*/
	/* works very well
	CActionRobotMovement2D ::TMotionModelOptions MyOptions;
	MyOptions.gausianModel.a1=0.0002;	
	MyOptions.gausianModel.a2=0.0002;		
	MyOptions.gausianModel.a3=0.0002;
	MyOptions.gausianModel.a4=0.0002;	
	MyOptions.gausianModel.minStdPHI=0.0002;	
	MyOptions.gausianModel.minStdXY=0.0002;
	*/
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
	CPose3D  SensorPos(0.175,0.0,0,0,0);//21

	//40//7
// 44 and 17 worked out before
	scan->setSensorPose(SensorPos);

	puts("checking for last odopose");
	if(system::fileExists("LastOdoPose.odo")==true)
	{
	puts("exists");
	CFileInputStream("LastOdoPose.odo")>>OldOdoPose; // make sures there a odopose there otherwise it will crash
	puts("loaded");
	}
	int NumberofReadings =0;
	puts("checking for fake");
	if(system::fileExists("Fake.int")==true)
	{
	CFileInputStream("Fake.int")>>Fake; // make sures there a odopose there otherwise it will crash
	}
	
	/* changing files */
	puts("changing files");
	if(remove("OldData.rawlog")!=0)					{puts("couldn't deleate file");}
	if(rename("NewData.rawlog","OldData.rawlog")!=0){puts("couldn't rename file");}
	yarp::os::Time::delay(2);
	mrpt::utils::CFileGZInputStream MapReader("OldData.rawlog");
	mrpt::utils::CFileGZOutputStream MapUpdater("NewData.rawlog");


	size_t rawlogEntry =0;
	puts("opening rawlog");
	int totalnumberofinputs =0;
	if(MapReader.fileOpenCorrectly())// found file and opening
		{
			puts("checking size");
			if(MapReader.getTotalBytesCount()>0)
			{
			puts("file opened processing data");
			while(totalnumberofinputs<Fake.x())
				{				
				mrpt::slam::CRawlog::readActionObservationPair(MapReader, action, observations, rawlogEntry);
				mapBuilder.processActionObservation( *action, *observations );
				MapUpdater<<*action<<*observations ;
				totalnumberofinputs++;
				}
			puts("closing file");
			MapReader.close();
			}
			else	{OldOdoPose.x(0);OldOdoPose.y(0);OldOdoPose.phi(0);	}
		}
	else	{OldOdoPose.x(0);OldOdoPose.y(0);OldOdoPose.phi(0);	}
	
	printf("number of entrys read %i \n",totalnumberofinputs);
	puts("getting inital poses");
	posePDF =  mapBuilder.getCurrentPoseEstimation();
	curRobotPose = posePDF->getEstimatedPose();	
	printf("it thinks old pose is X%f Y%f R%f \n",OldOdoPose.x(),OldOdoPose.y(),OldOdoPose.phi());
	printf("it thinks pose     is X%f Y%f R%f \n",curRobotPose.x(),curRobotPose.y(),curRobotPose.yaw());
	printf("diff               is X%f Y%f R%f \n",OldOdoPose.x()-curRobotPose.x(),OldOdoPose.y()-curRobotPose.y(),OldOdoPose.phi()-curRobotPose.yaw());
int xc =0;
int sizeofmap=0;
int oldsizeofmap=0;
	bool FirstTimeOnly=true;
	for (;;)
	{
		mrpt::system::sleep(1000);
		if (kbhit())//mrpt::system::os::kbhit())//			os::kbhit())
		{
			if(_getch()=='q')
			{
				break;
			}
		}
		
		action->clear();		// clear the last obs and action
		observations->clear();
		PosMessage.clear();

		if(Mapper.GetBottleData("PosIn",&PosMessage)==true)//&&FirstTimeOnly==true)
			{
			puts("updating posistoin");
			x	= PosMessage.get(2).asDouble()/100;//.asDouble()/100;
			y	= PosMessage.get(3).asDouble()/100;//.asDouble()/100;
			rot	= PosMessage.get(1).asDouble();///57.2957795;
			rot = DEG2RAD(rot*-1);

			StarPose.x(x);StarPose.y(y);StarPose.phi(rot);
			StarPose.normalizePhi();
			DifOdoPose=StarPose-OldOdoPose;

			OldOdoPose=StarPose;
			MyRobotAction.computeFromOdometry(DifOdoPose,MyOptions);
			action->insert(MyRobotAction);
			}
	
	

								
			Bottle BB;
			if(true==Mapper.GetBottleData("LaserIn",&BB))
			{
			puts("updating laser");
			for(int hh =0 ; hh< BB.size();hh++)
				{
				scan->scan[hh]=BB.get(hh).asDouble()/1000;
				}
			observations->insert(scan);
			}


		if(observations->size()>0&&action->size()>0)
		{
		puts("updating map");
		FirstTimeOnly=false;
		try
			{
			Fake.x(Fake.x()+1);
			mapBuilder.processActionObservation( *action, *observations );
			MapUpdater<<*action<<*observations;
			CFileStream("LastOdoPose.odo",fomWrite)<<StarPose;
			CFileStream("Fake.int",fomWrite)<<Fake;
			}
		
		catch(exception& e){cout << "except:" << e.what()<<endl;}
		}
		
			puts("creating 3d map");
			 const CMultiMetricMap* mostLikMap =  mapBuilder.getCurrentlyBuiltMetricMap();
						
			posePDF =  mapBuilder.getCurrentPoseEstimation();	// get current pose pointer of robot
			curRobotPose = posePDF->getEstimatedPose();		
			if(mostLikMap->m_gridMaps.size()!=0 )
			{
			const COccupancyGridMap2D gridmap = *mostLikMap->m_gridMaps[0].pointer();
			float CX = curRobotPose.x();
			float CY = curRobotPose.y();
			float CR = RAD2DEG(curRobotPose.yaw());
			gridmap.getAsImage(img,false, true);
			img.drawCircle(gridmap.x2idx(0.0),gridmap.y2idx(0.0),10,TColor(100,0,200),1 );

			printf("the current loc X:%f Y:%f R:%f \n",CX*100,CY*100,CR);

			Pathwin.showImage(img);
			}

			int Counter=0;
			Counter++;
			if(Counter%5==1)
			{
			Counter =0;
			theScene->clear();
			theScene->insert( obj1 );
			opengl::CSetOfObjectsPtr obj = opengl::CSetOfObjects::Create(); // get a empty objecy
			mostLikMap->getAs3DObject(obj);									// get the map as a object
			theScene->insert(obj);											// chuck it in the scene
			opengl::CSetOfObjectsPtr obj3 = opengl::stock_objects::RobotPioneer(); // get a object and call it obj3
			obj3->setPose(curRobotPose);									// set its pose to the robot pose										
			theScene->insert(obj3);											// chuck it in the scene
			COpenGLScenePtr &theScene = win.get3DSceneAndLock();			// lock the scene
			win.unlockAccess3DScene();										// unlock it
			win.forceRepaint();												// refresh the view
			win.setCameraPointingToPoint(curRobotPose.x(),curRobotPose.y(),1);
					
		}
	}
	puts("system shutdown");
	mrpt::system::sleep(2000);
	puts("saving file");
	MapUpdater.close();// does it actully get here?
	MapUpdater.~CFileGZOutputStream();
	MapReader.~CFileGZInputStream();
	action.clear_unique();
	observations.clear_unique();
	puts("saved file");
	mrpt::system::sleep(1000);

	return 0;
}
/*
 	CPathPlanningCircularRobot	pathPlanning;
	pathPlanning.robotRadius = RobotDiameter;
	std::deque<poses::TPoint2D>		thePath;
	bool	Thereisnopath;
	// image for the path planner
	 // create a window to show path
	static int TimeUntillMapUpdating = 20;
	bool continuepath = false;
*/


////************************ path planning ***********************///
/*
			Bottle Btarget;
			if(mostLikMap->m_gridMaps.size()!=0 && (Mapper.GetBottleData("TargetIn",&Btarget)==true||continuepath==true))
				{
				CPose2D  wantedRobotPose(-2.0,-0);
				puts(" accepted target");
				static const COccupancyGridMap2D gridmap = *mostLikMap->m_gridMaps[0].pointer();
				int R2 = round(pathPlanning.robotRadius / gridmap.getResolution() );
				gridmap.getAsImage(img,false, true);
//				wantedRobotPose.x(Btarget.get(0).asDouble());//.asDouble()/100;
//				wantedRobotPose.y(Btarget.get(1).asDouble());
				img.drawCircle(gridmap.x2idx(curRobotPose.x()),gridmap.y2idx(curRobotPose.y()),R2,TColor(255,0,255),1 );
//				img.drawCircle(gridmap.x2idx(wantedRobotPose.x()),gridmap.y2idx(wantedRobotPose.y()),R2,TColor(255,0,255),1 );
//				printf("current pos X%f Y%f wanted pos X%f Y%f",curRobotPose.x(),curRobotPose.y(),wantedRobotPose.x(),wantedRobotPose.y());
				
//				try					{pathPlanning.computePath(gridmap,curRobotPose,wantedRobotPose, thePath,Thereisnopath, 1000.0f  );}
//				catch(exception& e)	{cout << "except:" << e.what()<<endl;}

				Bottle Bwaypoint;
				Bwaypoint.clear();

				if(Thereisnopath==false)
				{
					puts("there is path");
					for (std::deque<poses::TPoint2D>::const_iterator it=thePath.begin();it!=thePath.end();++it)
					{
					img.drawCircle( gridmap.x2idx(it->x),gridmap.getSizeY()-1-gridmap.y2idx(it->y),R2, TColor(255,0,255) );
					printf(" the waypoints imagined are (cell) X:%f Y:%f \n",it->x,it->y);
					Bwaypoint.addDouble(gridmap.idx2x(it->x));
					Bwaypoint.addDouble(gridmap.idx2y(it->y));
					}
				Mapper.SendBottleData("WaypointOut",Bwaypoint);
				}
				else
				{
				puts("there is no path");
				}
				Pathwin.showImage(img);
				}
*/

/************************* a stupied attempt to save and get the odo readings, the serialise class makes this moot ****/
/*	if(FirstTimeOnly==true)
			{
			string line;
			ifstream myfile ("SavedOriginalOdo.txt");
			if(myfile.is_open())
			{
			puts("file exists");
			getline(myfile,line);
			SavedPose.x(atof(line.c_str()));
			getline(myfile,line);
			SavedPose.y(atof(line.c_str()));
			getline(myfile,line);
			SavedPose.phi(atof(line.c_str()));
			printf("the saved cords are X%f Y%f PHI%f \n",SavedPose.x(),SavedPose.y(),SavedPose.phi());
			myfile.close();
			}
			else
			{
			puts("file does not exist creating");
			ofstream myfile2 ("SavedOriginalOdo.txt");
				if(myfile2.is_open())
				{
				myfile2<<SavedPose.x();
				myfile2<<SavedPose.y();
				myfile2<<SavedPose.phi();
				myfile2.close();
				}
			}
			FirstTimeOnly=false;
			}
			NewOdoPose = NewOdoPose-SavedPose;
		*/




/************************ this was for loading the map through the mapbuilder and then giving it a innit pos *********/


//mrpt::slam::CSensFrameProbSequence something;	 // empty var for seq
//	mapBuilder.loadCurrentMapFromFile("robothouse.simplemap"); // load maps
	//mapBuilder.

//	puts("loaded proper map");
//	mapBuilder.getCurrentlyBuiltMap(something);		 // put map into seq
//	mapBuilder.clear();								 // clear the map
	
//	Bottle FirstLoc;
	//while(Mapper.GetBottleData("PosIn",&FirstLoc)==false){}
	//		x	= FirstLoc.get(2).asInt()/100;//.asDouble()/100;
	//		y	= FirstLoc.get(3).asInt()/100;//.asDouble()/100;
	//		rot	= DEG2RAD(FirstLoc.get(1).asDouble());//57.2957795;
		//	rot = rot*-1;
		//	rot = rot -4;
	//		static CPose2D StartPos;
	//		StartPos.x(x);
	//		StartPos.y(y);
	//		StartPos.phi(rot*-1);

//	CPosePDFGaussian dummypose(CPose2D(x,y,rot),CMatrixDouble33(3,3));
//	mapBuilder.initialize(something,&dummypose);