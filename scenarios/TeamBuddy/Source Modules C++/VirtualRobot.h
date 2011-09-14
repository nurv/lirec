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
//#include <mrpt/hwdrivers/src/aria/include/Aria.h>
#include <mrpt/gui.h>



using namespace mrpt::hwdrivers;
using namespace mrpt::utils;
using namespace mrpt::math;
using namespace mrpt::slam;
using namespace mrpt::poses;
using namespace std;

CPose2D OldPoseStar (0,0,0);
int iStarCntr=0;
double calculate_distance(double x1,double y1,double x2 ,double y2);
bool bWheelCommand=false;


class VRobot: public SamClass
{
private:
BufferedPort<Bottle> PortIn;				
BufferedPort<Bottle> OdoOut;
BufferedPort<Bottle> StarIn;
BufferedPort<Bottle> SonarOut;

double TimeNeedid;
CActivMediaRobotBase::TRobotDescription  robInfo;
CActivMediaRobotBase	*robot;
Bottle ListOfMoves;
int WhereInList;
Network yarp;
int currentpriority;

public:

	bool firstStarReading;

	void SamInit(void)
	{
		robot = new CActivMediaRobotBase();
		robot->setSerialPortConfig("COM9",9600);
		robot->enableSonars();
		robot->initialize();
		firstStarReading = false;
		
		
		
		robot->getRobotInformation(robInfo);
		cout << "Robot # front bumpers : " << robInfo.nFrontBumpers << endl;
		cout << "Robot # rear bumpers  : " << robInfo.nRearBumpers << endl;
		cout << "Robot # sonars        : " << robInfo.nSonars << endl;

		ListOfMoves.addInt(0); // set the current priority to nothing
		WhereInList=1;


		RecognisePort("CMDin");
		RecognisePort("SonarOut");
		RecognisePort("ODOout");
		RecognisePort("StarIn");
		StartModule("/Robot");


		
		PortIn.open("/Robot_CMDin");				
		OdoOut.open("/Robot_ODOout");
		StarIn.open("/Robot_StarIn");
		//SonarOut.open("/Robot_SonarOut");

		PortIn.setReporter(myPortStatus);				
		OdoOut.setReporter(myPortStatus);
		StarIn.setReporter(myPortStatus);
		//SonarOut.setReporter(myPortStatus);
		
	}
	
	void Disconnect()
	{
		robot->disableSonars();
	}


	void SamIter(void)
	{
		
		//robot->setVelocities(200,0);
		robot->doProcess();

		Bottle *b = PortIn.read(false);
		if(b!=NULL) // if the new bottle has higher priority then 
		{
			//puts("robot got new commands");
			if(b->get(0).asInt()>=currentpriority)
			{
				ListOfMoves.clear();
				ListOfMoves = *b;
				currentpriority = b->get(0).asInt();
				WhereInList=1;
				bWheelCommand=true;
			}
		}
		//else
		//{
		//	bWheelCommand=false;
		//}

		Bottle *c = StarIn.read(false);
	
		if(c!=NULL)
		{	
			double starID = c->get(0).asDouble();
			//std::cout << "star id " << starID <<  std::endl;
			double starX = c->get(1).asDouble();
			double starY = c->get(2).asDouble();
			double starAng = c->get(3).asDouble();
			CPose2D newpose(starX,starY,starAng);
		
			//we add the ppfset of stargazer sensor position on the robot
			//double distOffset = newpose.distance2DTo(starX,starY+0.30);
			//newpose = newpose + CPose2D(0,distOffset,0);

			//printf("current loc %s \n",newpose.asString().c_str());

			
			if(firstStarReading==false)
			{
				robot->changeOdometry(newpose);
				firstStarReading = true;
				OldPoseStar = newpose;
				std::cout << " inside first reading " << std::endl;
				yarp::os::Time::delay(0.2);

			}
			
			iStarCntr++;
			CPose2D oldpose;
			
			robot->getOdometry(oldpose);
			double distOld = oldpose.distance2DTo(newpose.x(), newpose.y());


			//calculate_distance(OldPoseStar.x(),OldPoseStar.y(),newpose.x(), newpose.y());
			//std::cout << " new pose X, Y " << newpose.x() << " "  << newpose.y() << std::endl;
			//std::cout << " old pose X, Y " << OldPoseStar.x() << " "  << OldPoseStar.y() <<  " old dist " << distOld << std::endl;
			//if dictsnce is too large due to error reading then ignore, but also take the new reading if it repeats
			if(((distOld<1.5 && distOld>0.5)|| iStarCntr>3) && starID!=0)
			{
				robot->changeOdometry(newpose);
				OldPoseStar = newpose;
				iStarCntr = 0;
			}
			/*
			else if(starID==0)
			{
				puts("data corrupt, putting robot in old position");
				robot->changeOdometry(OldPoseStar);
			}
			else
			{
				puts("the distance changed too much ignoring");
				robot->changeOdometry(OldPoseStar);
			}*/

			
		}
		
		
		

		if(yarp::os::Time::now()>TimeNeedid && WhereInList<ListOfMoves.size()) // if its reached the time and theres still more commands
		{
			 
		TimeNeedid = yarp::os::Time::now() + ListOfMoves.get(WhereInList).asDouble(); // set the amount of time
		robot->setVelocities(ListOfMoves.get(WhereInList+1).asDouble(),DEG2RAD(ListOfMoves.get(WhereInList+2).asDouble()));////(WhereInList+1,WhereInList+2);
		//printf("command recived speed %f rot %f \n",ListOfMoves.get(WhereInList+1).asDouble(),ListOfMoves.get(WhereInList+2).asDouble());
		//dSpeed+=ListOfMoves.get(WhereInList+1).asDouble();
		WhereInList+=3;
		//bWheelCommand = true;
		
		}
		else if( WhereInList>=ListOfMoves.size() && currentpriority!=0) // gone through the whole list then stop currentpriority!=0)//WhereInList>=ListOfMoves.size()
		{
			puts("stopping robot");
			robot->setVelocities(0,0);
			robot->GetRuntimeClass();
			currentpriority=0;
			//ListOfMoves.clear();
			//ListOfMoves.addInt(0);
		}
	
		
		//mrpt::system::sleep(20); // has to sleep a little bit so the onboard interupts doesn't keep on interupting itself.
		yarp::os::Time::delay(0.2);
		CPose2D odopose;
		robot->getOdometry(odopose);

		Bottle &odo = OdoOut.prepare();
		odo.clear();
		odo.addDouble(odopose.x());
		odo.addDouble(odopose.y());
		odo.addDouble(odopose.phi());
		OdoOut.writeStrict();
		vector_bool bumps;
		robot->getBumpers(bumps);
/*
		CObservationRange	obs;
		
		bool Observation;
		robot->getSonarsReadings(Observation, obs);

	if(Observation)
	{
		Bottle &Send = SonarOut.prepare();
		Send.clear();
	
		for (int i = 0;i<8;i++)//obs.sensedData.size()//front sonars only
		{
			//printf("[ID:%i]=%15f \n",obs.sensedData[i].sensorID,obs.sensedData[i].sensedDistance);
			Send.addDouble(obs.sensedData[i].sensedDistance);
			//cntSonar--;
		//	if(obs.sensedData[i].sensedDistance < 0.2)
		//		distflag = true;
		}
		
		SonarOut.writeStrict();
	}

	*/
		double battery_volts ;
		robot->getBatteryCharge(battery_volts);
		//std::cout << "Voltage level " << battery_volts <<std::endl;

		
		for(int y=0;y<(robInfo.nFrontBumpers+robInfo.nRearBumpers);y++)
		{
			if(bumps[y]==1)//&& distflag==true
			{
				if(y>=4 && WhereInList<ListOfMoves.size())//bumper hit and robot is moving
					robot->setVelocities(-20,0);
				else if (y<5 && WhereInList<ListOfMoves.size())
					robot->setVelocities(20,0);
				else
					robot->setVelocities(0,0);

				yarp::os::Time::delay(2);
				puts("bumper hit or too close from sonar");
				break;
			}

		}
	}
};

double calculate_distance (double x1,double y1,double x2 ,double y2)
{

double distance;

double distance_x = x1-x2;

double distance_y = y1- y2;

distance = sqrt( (distance_x * distance_x) + (distance_y * distance_y));

return distance;

}