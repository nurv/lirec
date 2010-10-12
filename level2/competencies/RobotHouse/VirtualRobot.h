/*

	Vrobot

	The idea here is to send the priority then in couples of three the time wanted,liner vel and rot vel,
	so if we send 1,1,500,90;
	it'll be priority 1, set forward vel to 500mm and rot vel to 90 and stay that way for one secound
	it sends the odo data out at the same time
	also has a clause that if any of the front bumpers are hit it will stop until the bumper is no longer in contact


*/


//#include "SamClass.h"
#include <mrpt/slam.h>
#include <mrpt/hwdrivers/CHokuyoURG.h>
#include <mrpt/hwdrivers/CSerialPort.h>
#include <mrpt/hwdrivers/CActivMediaRobotBase.h>
#include <mrpt/gui.h>

using namespace mrpt::hwdrivers;
using namespace mrpt::utils;
using namespace mrpt::math;
using namespace mrpt::slam;
using namespace mrpt::poses;
using namespace std;







class VRobot: public SamClass
{
private:
BufferedPort<Bottle> PortIn;				
BufferedPort<Bottle> OdoOut;
BufferedPort<Bottle> StarIn;
double TimeNeedid;
CActivMediaRobotBase::TRobotDescription  robInfo;
CActivMediaRobotBase	*robot;
Bottle ListOfMoves;
int WhereInList;
Network yarp;
int currentpriority;
public:

	void SamInit(void)
	{
		robot = new CActivMediaRobotBase;
		robot->setSerialPortConfig("COM1",9600);
		robot->enableSonars();
		robot->initialize();
		
		
		robot->getRobotInformation(robInfo);
		cout << "Robot # front bumpers : " << robInfo.nFrontBumpers << endl;
		cout << "Robot # rear bumpers  : " << robInfo.nRearBumpers << endl;
		cout << "Robot # sonars        : " << robInfo.nSonars << endl;

		ListOfMoves.addInt(0); // set the current priority to nothing
		WhereInList=1;


		RecognisePort("CMDin");
		RecognisePort("ODOout");
		RecognisePort("StarIn");
		StartModule("/Robot");


		
		PortIn.open("/Robot_CMDin");				
		OdoOut.open("/Robot_ODOout");
		StarIn.open("/Robot_StarIn");

		PortIn.setReporter(myPortStatus);				
		OdoOut.setReporter(myPortStatus);
		StarIn.setReporter(myPortStatus);
		
	}
	
	


	
	void SamIter(void)
	{
			
		//robot->setVelocities(200,0);
		robot->doProcess();

		Bottle *b = PortIn.read(false);
		if(b!=NULL) // if the new bottle has higher priority then 
		{
			puts("robot got new commands");
			if(b->get(0).asInt()>=currentpriority){ListOfMoves.clear();ListOfMoves = *b;currentpriority = b->get(0).asInt();WhereInList=1;}
		}

		Bottle *c = StarIn.read(false);
		if(c!=NULL)
		{
			robot->changeOdometry(CPose2D(c->get(0).asDouble(),c->get(1).asDouble(),c->get(2).asDouble()));
		}

		if(yarp::os::Time::now()>TimeNeedid && WhereInList<ListOfMoves.size()) // if its reached the time and theres still more commands
		{
		TimeNeedid = yarp::os::Time::now() + ListOfMoves.get(WhereInList).asDouble(); // set the amount of time
		robot->setVelocities(ListOfMoves.get(WhereInList+1).asDouble(),DEG2RAD(ListOfMoves.get(WhereInList+2).asDouble()));////(WhereInList+1,WhereInList+2);
	//	printf("command recived speed %f rot %f \n",ListOfMoves.get(WhereInList+1).asDouble(),ListOfMoves.get(WhereInList+2).asDouble());
		WhereInList+=3;
		}
		else if(currentpriority!=0)//WhereInList>=ListOfMoves.size()) // gone through the whole list then stop
		{
			puts("stopping robot");
			robot->setVelocities(0,0);
			currentpriority=0;
			//ListOfMoves.clear();
			//ListOfMoves.addInt(0);
		}
		
		//mrpt::system::sleep(20); // has to sleep a little bit so the onboard interupts doesn't keep on interupting itself.
		yarp::os::Time::delay(1);
		CPose2D odopose;
		robot->getOdometry(odopose);

		Bottle &odo = OdoOut.prepare();
		odo.clear();
		odo.addDouble(odopose.x());
		odo.addDouble(odopose.y());
		odo.addDouble(odopose.phi());
		OdoOut.write();
		vector_bool bumps;
		robot->getBumpers(bumps);
		for(int y=0;y<robInfo.nFrontBumpers;y++)
		{
			if(bumps[y]==1){robot->setVelocities(0,0);break;}
		}
	}
};
