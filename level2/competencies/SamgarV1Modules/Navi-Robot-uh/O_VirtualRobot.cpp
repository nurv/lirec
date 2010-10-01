

#include <Aria.h>
#include <time.h>
#include <string>
#include <ArBumpers.h>
#include <ArVCC4.h>
#include <ariaUtil.h>
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <string>
#include <conio.h>
#include "SamgarMainClass.h"


/*
	Remember to put some stuff in so it waits for stargazer to update it before it starts pumping out data



*/

#define usestar false


using namespace std;
using namespace yarp;

/*** Robot Defs ***/

ArRobot robot(NULL,false);
ArVCC4 ptz(&robot, false, ArVCC4::COMM_UNKNOWN,true, false, ArVCC4::CAMERA_C50I);
ArSerialConnection RobotConnectoin;
ArSerialConnection CameraConnectoin;
ArSonarDevice sonarDev;
ArBumpers bumpers;


ArKeyHandler keyHandler;
unsigned int camTimeRef;

bool InTransit = false;
bool Emergency = false;
bool BeenCorrectedByStar = false;



class ActionEmergencyControl : public ArAction
{
public :
	double SonarThreshold;
	ArRangeDevice *mysonar;
	// constructor
	ActionEmergencyControl() : ArAction("EmergencyControl")	{	SonarThreshold = 500;	}
	virtual void setRobot(ArRobot *robot)					{	ArAction::setRobot(robot);	mysonar = robot->findRangeDevice("sonar");	}
	virtual ~ActionEmergencyControl(void){}
	virtual ArActionDesired *fire (ArActionDesired currentDesired)
	{
		static bool BangBang = false;
		double LeftFrontSonar  = mysonar->currentReadingPolar(0,90);
		double LeftBackSonar   = mysonar->currentReadingPolar(90,180);
		double RightBackSonar  = mysonar->currentReadingPolar(180,270);
		double RightFrontSonar = mysonar->currentReadingPolar(270,360);//- myRobot->getRobotRadius();// - robot.getRobotRadius();
		double leftwheel =0,rightwheel =0;
		double turnleft=0,turnright=0;
		double forward=0,backward=0;	

		Emergency=false;
//		if(LeftFrontSonar<SonarThreshold)	{turnleft-=2;backward-=100;Emergency=true;}
//		if(RightFrontSonar<SonarThreshold)	{turnright+=2;backward-=100;Emergency=true;}
//		if(LeftBackSonar<SonarThreshold)	{turnright+=2;forward+=100;Emergency=true;}
//		if(RightBackSonar<SonarThreshold)	{turnleft-=2;forward+=100;Emergency=true;}
	
		Emergency=false;
		if (kbhit())
		{
			char mychar = getch();
			if(mychar=='q'){myRobot->disconnect();Aria::shutdown();}
			if(mychar=='w'){myDesired.setVel(+200);}
			if(mychar=='s'){myDesired.setVel(-200);}
			if(mychar=='a'){myDesired.setDeltaHeading(+10);}
			if(mychar=='d'){myDesired.setDeltaHeading(-10);}
			BangBang=true;
		}
		else if(Emergency==true)
		{
		//	myDesired.setDeltaHeading(turnleft+turnright,1);
		//	myDesired.setVel(forward+backward);
		//	myDesired.setVel(0);
		}
		else 
		{
			if(BangBang==true)
			{
				puts("resetting to zero");
			myDesired.setDeltaHeading(0,1);
			myDesired.setVel(0);
			myDesired.reset();
			myRobot->unlock();
			}
			BangBang=false;
		}
		
		
	return &myDesired;
	}
protected :
	ArActionDesired myDesired;
};

class UpdateMap : public ArAction
{
public :
	

	UpdateMap(SamgarModule *copyofmodule) : ArAction("UpdateMap")
	{
	Mycopyofmodule = copyofmodule;
	Mycopyofmodule->AddPortS("MAPout");
	}
	virtual void setRobot(ArRobot *robot)					{	ArAction::setRobot(robot);}
	virtual ~UpdateMap(void){}
	virtual ArActionDesired *fire (ArActionDesired currentDesired)
	{
		if(BeenCorrectedByStar || !usestar) // if we have updated odo from star or is star isn't being used
		{
		 ArPose MyPose;
		 MyPose =	myRobot->getPose();
		Bottle PoseBottle;
		
		PoseBottle.addDouble(MyPose.getX()/1000);
		PoseBottle.addDouble(MyPose.getY()/1000);
		PoseBottle.addDouble(MyPose.getTh());
		Mycopyofmodule->SendBottleData("MAPout",PoseBottle);
		//printf("my x:%f y:%f rot:%f \n",MyPose.getX()/1000,MyPose.getY()/1000,MyPose.getTh());
		}
	return &myDesired;
	}
	protected :
	SamgarModule *Mycopyofmodule;
	ArActionDesired myDesired;
};
class PlaySounder : public ArAction
{
public :

	PlaySounder(SamgarModule *copyofmodule) : ArAction("PlaySounder")
	{
	Mycopyofmodule = copyofmodule;
	Mycopyofmodule->AddPortS("SOUNDin");
	}
	virtual void setRobot(ArRobot *robot)					{	ArAction::setRobot(robot);}
	virtual ~PlaySounder(void){}
	virtual ArActionDesired *fire (ArActionDesired currentDesired)
	{
	Bottle SoundBottle;
	if(Mycopyofmodule->GetBottleData("SOUNDin",&SoundBottle,SamgarModule::NoStep)==true)
		{
			char tune[40];
			for(int uu = 0;uu<SoundBottle.size();uu++)
			{
				tune[uu]=SoundBottle.get(uu).asInt();
			}
			robot.comStrN(ArCommands::SAY,tune,SoundBottle.size());
		}
	return &myDesired;
	}
	protected :
	SamgarModule *Mycopyofmodule;
	ArActionDesired myDesired;
};
class BehaveMove : public ArAction
{
public :

	BehaveMove(SamgarModule *copyofmodule) : ArAction("BehaveMove")
	{
	Mycopyofmodule = copyofmodule;
	Mycopyofmodule->AddPortS("BEHAVEin");
	NewData = false;
	WhereInList =0;
	}
	virtual void setRobot(ArRobot *robot)					{	ArAction::setRobot(robot);}
	virtual ~BehaveMove(void){}

	virtual ArActionDesired *fire (ArActionDesired currentDesired)
	{
	Bottle MoveBottle;
	NewData = Mycopyofmodule->GetBottleData("BEHAVEin",&MoveBottle,SamgarModule::NoStep);
		//happy
/*		PrecBottle.addDouble(myRobot->getTransVelMax());
		PrecBottle.addDouble(myRobot->getRotVelMax());
		PrecBottle.addDouble(0); //linear vel = 0
		PrecBottle.addDouble(-15); //turn right
		
		PrecBottle.addDouble(myRobot->getTransVelMax());
		PrecBottle.addDouble(myRobot->getRotVelMax());
		PrecBottle.addDouble(0); //linear vel = 0
		PrecBottle.addDouble(30); //turn left
		
		PrecBottle.addDouble(myRobot->getTransVelMax());
		PrecBottle.addDouble(myRobot->getRotVelMax());
		PrecBottle.addDouble(0); //linear vel = 0
		PrecBottle.addDouble(-30); //turn right
		
		PrecBottle.addDouble(myRobot->getTransVelMax());
		PrecBottle.addDouble(myRobot->getRotVelMax());
		PrecBottle.addDouble(0); //linear vel = 0
		PrecBottle.addDouble(30); //turn left
		
		PrecBottle.addDouble(myRobot->getTransVelMax());
		PrecBottle.addDouble(myRobot->getRotVelMax());
		PrecBottle.addDouble(-0); //linear vel = 0
		PrecBottle.addDouble(-15); //turn right
*/	
	if(NewData){WhereInList=0;PrecBottle=MoveBottle;}


	//I change the order from the original now they work as below
	//PrecBottle.clear();
	//PrecBottle.addDouble(0); // maxtrans
	//PrecBottle.addDouble(0); // max rot
	//PrecBottle.addDouble(0); // distance
	//PrecBottle.addDouble(0); // rot
			
			if(myRobot->isHeadingDone(1) && WhereInList<PrecBottle.size() && InTransit == false)
			{
				printf("heading done,loading new");
				//myRobot->setAbsoluteMaxLatVel(PrecBottle.get(WhereInList).asDouble());
				myRobot->setAbsoluteMaxTransVel(PrecBottle.get(WhereInList).asDouble());
				WhereInList++;
				myRobot->setAbsoluteMaxRotVel(PrecBottle.get(WhereInList).asDouble());
				WhereInList++;
				myRobot->move(PrecBottle.get(WhereInList).asDouble());
				WhereInList++;
				myRobot->setDeltaHeading(PrecBottle.get(WhereInList).asDouble());
				WhereInList++;
			}

			if(WhereInList>=PrecBottle.size())
			{
		//		myDesired.reset();
			}
		//	myDesired.setHeading(0,1);
	return &myDesired;
	}

	protected :
		Bottle PrecBottle;
		int WhereInList;
		bool NewData;
		SamgarModule *Mycopyofmodule;
		ArActionDesired myDesired;
};

class MoveCAM : public ArAction
{
public :

	MoveCAM(SamgarModule *copyofmodule) : ArAction("MoveCAM")
	{
	Mycopyofmodule = copyofmodule;
	Mycopyofmodule->AddPortS("CAMin");
	NewData = false;
	WhereInList =0;
	pan =0;
	tilt=0;
	time=0;
	}

	virtual void setRobot(ArRobot *robot)					{	ArAction::setRobot(robot);}
	virtual ~MoveCAM(void){}

	virtual ArActionDesired *fire (ArActionDesired currentDesired)
	{
	Bottle CAMBottle;
	NewData = Mycopyofmodule->GetBottleData("CAMin",&CAMBottle,SamgarModule::NoStep);

		
	if(NewData){WhereInList=0;PrecBottle=CAMBottle;} // if theres new data reset the system

	//PrecBottle.clear();
	//PrecBottle.addDouble(0); // pan
	//PrecBottle.addDouble(0); // tilt
	//PrecBottle.addDouble(100); // wait (iteration)

	PrecBottle.addDouble(0); PrecBottle.addDouble(20);	PrecBottle.addDouble(1000);
	PrecBottle.addDouble(0); PrecBottle.addDouble(-20);	PrecBottle.addDouble(1000);
	PrecBottle.addDouble(0); PrecBottle.addDouble(20);	PrecBottle.addDouble(1000);
	PrecBottle.addDouble(0); PrecBottle.addDouble(-20);	PrecBottle.addDouble(1000);
	PrecBottle.addDouble(0); PrecBottle.addDouble(20);	PrecBottle.addDouble(100);
	time=-1;

	// if its reached the desired tilt/pan get the next lot of data if there is more data in the list
	if(ptz.getPan()==pan && ptz.getTilt() == tilt && WhereInList<PrecBottle.size()/*&& ((time==-1) || ((time!=-1)&& (time < (ArUtil::getTime()-camTimeRef))) )*/)   
		{
		pan  = PrecBottle.get(WhereInList).asDouble();WhereInList++;
		tilt = PrecBottle.get(WhereInList).asDouble();WhereInList++;			
		time = PrecBottle.get(WhereInList).asDouble();WhereInList++;
		printf("if - Pan %d=%d, Tilt %d=%d, Timer %d=%d\n",pan,ptz.getPan(), tilt,ptz.getTilt(), time, (ArUtil::getTime()-camTimeRef));
		}
	else  // if its not reached the desired pan/tilt send the command again.
		{
		ptz.pan(pan);
		ptz.tilt(tilt);
		camTimeRef=ArUtil::getTime();
		printf("else - Pan %d=%d, Tilt %d=%d, Timer %d=%d\n",pan,ptz.getPan(), tilt,ptz.getTilt(), time, (ArUtil::getTime()-camTimeRef));
		
		}
	//time--;
		return &myDesired;
	}

	protected :
		Bottle PrecBottle;
		int WhereInList;
		bool NewData;
		double pan,tilt,time;
		SamgarModule *Mycopyofmodule;
		ArActionDesired myDesired;
};

class UpOdo : public ArAction
{
public :
		// corrects odo from stargazer
	UpOdo(SamgarModule *copyofmodule) : ArAction("UpOdo")
	{
	Mycopyofmodule = copyofmodule;
	Mycopyofmodule->AddPortS("STARin");
	}
	virtual void setRobot(ArRobot *robot)					{	ArAction::setRobot(robot);}
	virtual ~UpOdo(void){}

	virtual ArActionDesired *fire (ArActionDesired currentDesired)
	{
	Bottle OdoBottle;
	
	if(Mycopyofmodule->GetBottleData("STARin",&OdoBottle,SamgarModule::NoStep))
	{
		ArPose MyPose;
		MyPose.setX (OdoBottle.get(0).asDouble());
		MyPose.setY (OdoBottle.get(1).asDouble());
		MyPose.setTh(OdoBottle.get(2).asDouble());
		myRobot->moveTo(MyPose,true);//update in mm
		BeenCorrectedByStar=true;
	}
	
	return &myDesired;
	}

	protected :
		SamgarModule *Mycopyofmodule;
		ArActionDesired myDesired;
};

class Transit : public ArAction
{
public :

	Transit(SamgarModule *copyofmodule) : ArAction("TransitIn")
	{
	Mycopyofmodule = copyofmodule;
	
	PI= 3.14159265;
	}
	virtual void setRobot(ArRobot *robot)					{	ArAction::setRobot(robot);}
	virtual ~Transit(void){}

	virtual ArActionDesired *fire (ArActionDesired currentDesired)
	{
	Bottle TransitBottle;
	static bool wasIntransit=false;
	//puts("in transit method");
	if(Mycopyofmodule->GetBottleData("TransitIn",&TransitBottle,SamgarModule::NoStep))
	{
		puts("got data");
		//printf("recived data %s \n",TransitBottle.toString().c_str());
		if(TransitBottle.get(0).asDouble()==1)
		{

		
		
		speed    = TransitBottle.get(1).asDouble()*1000;
		angle    = TransitBottle.get(2).asDouble();
		accuracy = TransitBottle.get(3).asDouble()*1000;





		if(InTransit==false){myDesired.setHeading(angle);
		yarp::os::Time::delay(0.1);}
		InTransit=true;
	//	speed=speed/2;
		if(speed<20){speed=0;}   // its in mm
		}

		else 
		{
		speed    = 0;//TransitBottle.get(1).asDouble()*1000;
		angle    = 0;//TransitBottle.get(2).asDouble();
		accuracy = 0;//TransitBottle.get(3).asDouble()*1000;


			InTransit=false;
			myDesired.reset();
		
		}
	}


		if(InTransit==true)
		{
						
			//	float currentheadingchange = abs(myDesired.getHeading()-angle);
				printf("angle wanted %f \n",angle);
			//	myDesired.setDeltaHeading(currentheadingchange/4);
				myDesired.setHeading(angle,1);
				myDesired.setVel(speed,1);
		}
		else
		{
			myDesired.reset();
		}
			

	
	return &myDesired;
	}

	void FigureoutWheelsKyronsAlgorythem(void)
	{
		// all in mm and in one timestep (sec)
		
		double RobotCircum = (myRobot->getRobotRadius()*2)*PI;
		double WheelRobotRatio = RobotCircum / 360 ;// Very important, the wheel move needid to move 1 deg
		double errortospeedratio = speed/accuracy;
		double angleneedid = angle - myRobot->getPose().getTh();

		
		// degree a secound myDesired.setRotVel
	//	RotLeftWheel = (WheelRobotRatio*angleneedid)*errortospeedratio;
	//	RotRightWheel = RotLeftWheel*-1;
	//	myDesired.setRotVel(angle/2,1);
		//or
	//	myDesired.setRotVel(RotLeftWheel,1);

	//	myDesired.setVel(speed,1);

//		puts("working out");
//		printf("wheel ratio %f angleneedid %f errortospeedration %f \n",WheelRobotRatio,angleneedid,errortospeedratio);
//		int hh;
	//	cin >> hh;
			
//		if(abs(RotLeftWheel)>=speed)
//		{
//			if(RotLeftWheel>0)	{RotLeftWheel=   speed; RotRightWheel = - speed;}
//			else				{RotLeftWheel= - speed; RotRightWheel =   speed;}
//		}
//		else
//		{
		//double leftoverspeed = speed - abs(RotLeftWheel);
//		RotLeftWheel+=leftoverspeed;
//		RotRightWheel+=leftoverspeed;
//		}

		
	}

	protected :
		double speed,angle, accuracy;
		 SamgarModule *Mycopyofmodule;
		ArActionDesired myDesired;
		double PI;
		double RotLeftWheel,RotRightWheel;
};

void SetupRobot(void);

int main()
{

/*
	TODO
	Check that the Starclass is updating the global pose right	- not done
	Check that the MapClass is giving the right pose			- not done
	check khenglee can use the behaviours						- not done
	check emergency control										- not done

*/
	Network yarp;

	SamgarModule VR("Vrobot","Loco","wheel",SamgarModule::run); // Cant have spaces or underscores
	VR.AddPortS("TransitIn");
	SetupRobot();


	ActionEmergencyControl EmergencyControl;
	UpdateMap		UpdMap		(&VR);
	UpOdo           OdoUp       (&VR);
	Transit         TransitIn   (&VR);
	PlaySounder		SoundPlayer	(&VR);
	BehaveMove		MoveBehave	(&VR);
	MoveCAM			CAMMove		(&VR);

	// lowest priority might actully be highest becouse coms direct to robot and not through desiredaction.
	robot.addAction(&EmergencyControl,99); // need to check this works
	robot.addAction(&UpdMap,99);
	robot.addAction(&OdoUp,99);
	robot.addAction(&TransitIn,70);
	robot.addAction(&SoundPlayer,100);
	robot.addAction(&MoveBehave,10);
	robot.addAction(&CAMMove,100);
	robot.run(true);

	robot.disconnect();
	Aria::shutdown();
	return 0;
}

void SetupRobot(void)
{

	puts("attempting to connect to robot");
	RobotConnectoin.setPort("COM2");
	RobotConnectoin.setBaud(9600);
	robot.setDeviceConnection(&RobotConnectoin);
	CameraConnectoin.open("COM9");
	ptz.setDeviceConnection(&CameraConnectoin);
	ptz.init();
	ArUtil::sleep(15000);
	printf("done Init");
	Aria::setKeyHandler(&keyHandler);
	robot.attachKeyHandler(&keyHandler);

	if(!robot.blockingConnect()){puts("not connected to robot");Aria::shutdown();}
	robot.addRangeDevice(&sonarDev);
	robot.addRangeDevice(&bumpers);
	robot.enableMotors();
	robot.enableSonar();
	robot.requestEncoderPackets();
	robot.setCycleChained(false);
	robot.setAbsoluteMaxRotVel(robot.getAbsoluteMaxRotVel()/10);
//	robot.setRotVelMax(robot.getRotVelMax());
}





