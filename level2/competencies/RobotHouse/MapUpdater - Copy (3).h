
#define validrangesize 682
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



CICP    icp;
CSimplePointsMap    M,T;
CDisplayWindowPlots		win("Laser scans");
CDisplayWindowPlots		win2("Main map");
CPose2D            RobotPose(0,0,0);

class MapUpdater: public SamClass
{
private:
Network yarp;
BufferedPort <Bottle> LaserIn;
BufferedPort <Bottle> OdoIn;

public:
	void CheckOdo(void)
	{
	
	}

	void CheckLaser(void)
	{
		Bottle *Bscan = LaserIn.read(false);
		if(Bscan!=NULL)
		{
		CObservation2DRangeScan scan;
		scan.validRange.resize(validrangesize);
		scan.validRange.assign(validrangesize,'1');//19
		scan.aperture=4.1887902;// 240 degree scan range in radians
		scan.maxRange=4;//(m)
		scan.scan.resize(682);
		CPose3D  SensorPos(0,0,0,0,0,0);//(0.175,0.0,0,0,0);//21 // used this for most the demo
		scan.setSensorPose(SensorPos);	
		for(int x=0;x<Bscan->size();x++)
			{
				scan.scan[x]=Bscan->get(x).asDouble();
			}
		T.clear();
		T.insertObservation(&scan);

		if(M.isEmpty()&&!T.isEmpty()){M.insertObservation(&scan);}

		CPosePDFPtr pdf =  icp.Align(&M,&T,CPose2D(0,0,0));
		RobotPose = pdf->getEstimatedPose(); 

		printf("robots loc %s \n",RobotPose.asString().c_str());

		CPose3D RobotPose3D(RobotPose);
		M.insertObservation(&scan,&RobotPose3D);

		

		vector_float	xs,ys,zs,xa,ya,za;
		T.getAllPoints(xs,ys,zs);
		M.getAllPoints(xa,ya,za);

		win.plot(xs,ys,".b3");
		win.axis_equal();

		win2.plot(xa,ya,".b3");
		win2.axis_equal();

		}


	}


	
	void SamInit(void)
	{
	RecognisePort("LaserIn");
	RecognisePort("OdoIn");
	LaserIn.open("/MAP_LaserIn"); //myPortStatus
	OdoIn.open("/MAP_OdoIn");
	LaserIn.setReporter(myPortStatus);
	OdoIn.setReporter(myPortStatus);
	StartModule("/MAP");

	
	icp.options.maxIterations = 250;

//	ICPer.options.maxIterations = 250;
	}
	
	
	void SamIter(void)
	{
		CheckOdo();
		CheckLaser();





	
	}




	
};

