
/////**** VROBOT *****////

/* Authors	: K.Du Casse , Khenglee Koay
   Date		: 29/09/2010

   Vrobot is designed to take realtime movement commands from a navigation module in lin vel, rot vel. And also take commands for behaviours
   has keyboard input for testing and manual driving. 

   Also has a stop/rotate function for bumpers and sonar (within tolerance)
   Also updates its own odometry for outside source, such as stargazer

   /// This code will be replaced by a new versoin soon

*/





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

//variable to store the original value before modification and can be used to restore it
double robotAbsoluteMaxAccel=0;
double robotAbsoluteMaxRotDecel=0;
double robotAbsoluteMaxRotVel=0;

double robotRotAccel=0;
double robotRotDecel=0;
double robotRotVelMax=0;

double robotAbsoluteMaxTransAccel=0;
double robotAbsoluteMaxTransDecel=0;
double robotAbsoluteMaxTransVel=0;

double robotTransAccel=0;
double robotTransDecel=0;
double robotTransVelMax=0;

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
		myRobot->lock();
		if (kbhit())
		{
		//	myRobot->unlock();
		//	myRobot->lock();
			char mychar = getch();
			if(mychar=='q'){myRobot->disconnect();Aria::shutdown();}
			if(mychar=='w'){myRobot->setVel(+200);}
			if(mychar=='s'){myRobot->setVel(-200);}
			if(mychar=='a'){myRobot->setDeltaHeading(+10);}
			if(mychar=='d'){myRobot->setDeltaHeading(-10);}
			BangBang=true;
		//	myRobot->unlock();
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
				myRobot->setDeltaHeading(0);
				myRobot->setVel(0);
				myDesired.reset();
		//		myRobot->unlock();
			}
			BangBang=false;
		}
		myRobot->unlock();
		
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

	virtual void setRobot(ArRobot *robot) {	ArAction::setRobot(robot);}
	virtual ~PlaySounder(void){}

	virtual ArActionDesired *fire (ArActionDesired currentDesired)
	{
	Bottle SoundBottle;
	//myRobot->lock();
	if(Mycopyofmodule->GetBottleData("SOUNDin",&SoundBottle,SamgarModule::NoStep)==true)
	{	printf("Received sound data---------------------------\n");
		char tune[40];

		for(int uu=0; uu<SoundBottle.size(); uu++)
		{
			tune[uu]=SoundBottle.get(uu).asInt();
		}
		printf("sound here!!!!\n");
		myRobot->lock();
		robot.comStrN(ArCommands::SAY,tune,SoundBottle.size());
		myRobot->unlock();
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
	temp=0;
	int i=0;
	}
	virtual void setRobot(ArRobot *robot)	{	ArAction::setRobot(robot);}
	virtual ~BehaveMove(void){}

	virtual ArActionDesired *fire (ArActionDesired currentDesired)
	{
	Bottle MoveBottle;
	NewData = Mycopyofmodule->GetBottleData("BEHAVEin",&MoveBottle,SamgarModule::NoStep);
	/*	
	if (temp==150)//happy
	{
		PrecBottle.addDouble(myRobot->getTransVelMax());
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

		printf("temp = %d\n",temp);
		
	}
	temp+=1;*/

	if(NewData){
		NewData=false; 
		i=0;
		WhereInList=0;PrecBottle=MoveBottle;
		
		setTransAccel=PrecBottle.get(WhereInList).asDouble();		//set()Accel and Decel are the same value
		setTransDecel=PrecBottle.get(WhereInList).asDouble();		WhereInList++;	
		setTransVelMax=PrecBottle.get(WhereInList).asDouble();		WhereInList++;	
				
		setRotAccel=PrecBottle.get(WhereInList).asDouble();			//set()Accel and Decel are the same value
		setRotDecel=PrecBottle.get(WhereInList).asDouble();			WhereInList++;	
		setRotVelMax=PrecBottle.get(WhereInList).asDouble();		WhereInList++;	

	//	myRobot->unlock();
		myRobot->lock();

		myRobot->setRotAccel(robotRotAccel);
		myRobot->setRotDecel(robotRotDecel);
		myRobot->setRotVelMax(robotRotVelMax);

		myRobot->setTransAccel(robotTransAccel);
		myRobot->setTransDecel(robotTransDecel);
		myRobot->setTransVelMax(robotTransVelMax);

		if (setTransAccel>0) {	//if not requesting defult value then set 
			myRobot->setTransAccel(setTransAccel);
			myRobot->setTransDecel(setTransDecel);
		}
				
		if (setTransVelMax>0)	{	//if not requesting defult value then set 
			myRobot->setTransVelMax(setTransVelMax);
		}
				
		if (setRotAccel>0)	{	//if not requesting defult value then set 
			myRobot->setRotAccel(setRotAccel);
			myRobot->setRotDecel(setRotDecel);
		}
					
		if (setRotVelMax>0)	{	//if not requesting defult value then set 
			myRobot->setRotVelMax(setRotVelMax);
		}
				
		myRobot->move(PrecBottle.get(WhereInList).asDouble());				WhereInList++;
		myRobot->setDeltaHeading(PrecBottle.get(WhereInList).asDouble());	WhereInList++;
		i+=1;
		printf("i = %d\n",i);
		myRobot->unlock();
		
	}

	//I change the order from the original now they work as below
	//PrecBottle.clear();
	//PrecBottle.addDouble(0); // trans accel and Decel - they are the same
	//PrecBottle.addDouble(0); // max trans vel
	//PrecBottle.addDouble(0); // rot accel and Decel - they are the same
	//PrecBottle.addDouble(0); // max rot vel
	//PrecBottle.addDouble(0); // distance
	//PrecBottle.addDouble(0); // rot (delta angle)
			
	
	myRobot->lock();
			if(myRobot->isMoveDone(5) && myRobot->isHeadingDone(1) && WhereInList<PrecBottle.size() && InTransit == false)
			{

				printf("heading done,loading new\n");
				//-1 = no change, use original one
				
				setTransAccel=PrecBottle.get(WhereInList).asDouble();		//set()Accel and Decel are the same value
				setTransDecel=PrecBottle.get(WhereInList).asDouble();		WhereInList++;	
				setTransVelMax=PrecBottle.get(WhereInList).asDouble();		WhereInList++;	
				
				setRotAccel=PrecBottle.get(WhereInList).asDouble();			//set()Accel and Decel are the same value
				setRotDecel=PrecBottle.get(WhereInList).asDouble();			WhereInList++;	
				setRotVelMax=PrecBottle.get(WhereInList).asDouble();		WhereInList++;	
						
				if (setTransAccel>0) {	//if not requesting defult value then set 
					myRobot->setTransAccel(setTransAccel);
					myRobot->setTransDecel(setTransDecel);
				}
				
				if (setTransVelMax>0)	{	//if not requesting defult value then set 
					myRobot->setTransVelMax(setTransVelMax);
				}
				
				if (setRotAccel>0)	{	//if not requesting defult value then set 
					myRobot->setRotAccel(setRotAccel);
					myRobot->setRotDecel(setRotDecel);
				}
					
				if (setRotVelMax>0)	{	//if not requesting defult value then set 
					myRobot->setRotVelMax(setRotVelMax);
				}
				
				myRobot->move(PrecBottle.get(WhereInList).asDouble());				WhereInList++;
				myRobot->setDeltaHeading(PrecBottle.get(WhereInList).asDouble());	WhereInList++;
				i+=1;
				//printf("i = %d, ins left = %d",i, PrecBottle.size()-WhereInList);

			}
			else if (InTransit == true)
							{	// If the robot is in transit then we ignore all incoming action request
								WhereInList=PrecBottle.size();
							}
			myRobot->unlock();

			printf("M = %d, D = %d, ins left = %d\n",myRobot->isMoveDone(1), myRobot->isHeadingDone(1),  PrecBottle.size()-WhereInList);
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
		int temp;
		int setTransAccel,	setTransDecel,	setTransVelMax;
		int setRotAccel,	setRotDecel,	setRotVelMax;
		int i;
};

class MoveCAM : public ArAction
{
public :

	MoveCAM(SamgarModule *copyofmodule) : ArAction("MoveCAM")
	{
	Mycopyofmodule = copyofmodule;
	Mycopyofmodule->AddPortS("CAMin");
	NewData = false;
	WhereInList =9999;
	pan =0;
	tilt=0;
	time=0;
	camTimeRef=0;
	temp=0;
	}

	virtual void setRobot(ArRobot *robot)					{	ArAction::setRobot(robot);}
	virtual ~MoveCAM(void){}

	virtual ArActionDesired *fire (ArActionDesired currentDesired)
	{
	Bottle CAMBottle;
	NewData = Mycopyofmodule->GetBottleData("CAMin",&CAMBottle,SamgarModule::NoStep);
		
	if(NewData){NewData=false;WhereInList=0;PrecBottle=CAMBottle;} // if theres new data reset the system

	//PrecBottle.clear();
	//PrecBottle.addDouble(0); // pan
	//PrecBottle.addDouble(0); // tilt
	//PrecBottle.addDouble(100); // wait (iteration)


if (time<(ArUtil::getTime()-camTimeRef))  //delay have more priority, if future maybe only need either or target
{
	if ((ptz.getPan()==pan) && (ptz.getTilt()==tilt)  && (WhereInList<PrecBottle.size())) //reached target
	{	pan  = PrecBottle.get(WhereInList).asDouble();WhereInList++;
		tilt = PrecBottle.get(WhereInList).asDouble();WhereInList++;			
		time = (int)PrecBottle.get(WhereInList).asDouble();WhereInList++;

		if ((pan==-666) && (tilt==-666)) //do not move the camera
		{	pan=ptz.getPan();
			tilt=ptz.getTilt();
			//printf("doing -666, storing p=%f, t=%f, ti=%d\n", pan, tilt, time);
		}
		else if ((pan==999) && (tilt==999))	//init the camera
				{	ptz.init();
					pan=0;
					tilt=0;
					//printf("doing 999, setting p=%f, t=%f, ti=%d\n", pan, tilt, time);
				}
		else	{	
				ptz.panTilt(pan,tilt); 
				//printf("pan=%f, tilt=%f \n",pan, tilt);
				}
		//printf("time pass = %u\n", ArUtil::getTime()-camTimeRef);
		camTimeRef=ArUtil::getTime(); //set the timer for cases where delay has more priority thatn the angles of cam
	}
}		

ptz.panTilt(pan,tilt); 
//printf("calling Cam p=%f, t=%f, ti=%f, WL=%d, PB=%d\n", pan, tilt, time, WhereInList, PrecBottle.size());
//printf("cam now on p=%f, t=%f, timepass=%u\n",ptz.getPan(), ptz.getTilt(), ArUtil::getTime()-camTimeRef);
	return &myDesired;
	}

	protected :
		Bottle PrecBottle;
		int WhereInList;
		bool NewData;
		double pan,tilt,time;
		unsigned int camTimeRef;
		SamgarModule *Mycopyofmodule;
		ArActionDesired myDesired;
		int temp;
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
	UpdateMap		UpdMap(&VR);
	UpOdo			OdoUp(&VR);
	Transit			TransitIn(&VR);
	PlaySounder		SoundPlayer(&VR);
	BehaveMove		MoveBehave(&VR);
	MoveCAM			CAMMove(&VR);

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

	printf("done Init");

//	Aria::setKeyHandler(&keyHandler);
//	robot.attachKeyHandler(&keyHandler);

	if(!robot.blockingConnect()){puts("not connected to robot");Aria::shutdown();}
		robot.addRangeDevice(&sonarDev);
		robot.addRangeDevice(&bumpers);
		robot.enableMotors();
		robot.enableSonar();
		robot.requestEncoderPackets();
		robot.setCycleChained(false);
		robot.setAbsoluteMaxRotVel(robot.getAbsoluteMaxRotVel()/10);
//	robot.setRotVelMax(robot.getRotVelMax());

robotAbsoluteMaxAccel=robot.getAbsoluteMaxRotAccel();
robotAbsoluteMaxRotDecel=robot.getAbsoluteMaxRotDecel();
robotAbsoluteMaxRotVel=robot.getAbsoluteMaxRotVel();

robotRotAccel=robot.getRotAccel();
robotRotDecel=robot.getRotDecel();
robotRotVelMax=robot.getRotVelMax();

robotAbsoluteMaxTransAccel=robot.getAbsoluteMaxTransAccel();
robotAbsoluteMaxTransDecel=robot.getAbsoluteMaxTransDecel();
robotAbsoluteMaxTransVel=robot.getAbsoluteMaxTransVel();

robotTransAccel=robot.getTransAccel();
robotTransDecel=robot.getTransDecel();
robotTransVelMax=robot.getTransVelMax();


printf("ARotA= %f, ARotD= %f, ARotVelM= %f\n",robot.getAbsoluteMaxRotAccel(),robot.getAbsoluteMaxRotDecel(),robot.getAbsoluteMaxRotVel());
printf("RotA= %f, RotD= %f, RotVelM= %f\n",robot.getRotAccel(),robot.getRotDecel(),robot.getRotVelMax());
printf("ATransA= %f, ATransD= %f, ATransVelM= %f\n",robot.getAbsoluteMaxTransAccel(),robot.getAbsoluteMaxTransDecel(),robot.getAbsoluteMaxTransVel());
printf("TransA= %f, TransD= %f, TransVelM= %f\n",robot.getTransAccel(),robot.getTransDecel(),robot.getTransVelMax());
//stored these value then reset it everytime we got data in.
}