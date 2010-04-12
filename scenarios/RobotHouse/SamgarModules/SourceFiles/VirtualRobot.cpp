/*
	PROGRAM:	motion profile for pionner robot
	Author :	K . Du Casse
	Date   :    08 April 2010


	A quick and easy motion profile to allow the user to specify the error in path following, small error makes the robot more accurate but less smooth
	a big error makes it alot more smooth but less accurate, this is impacted by overall speed, if maxvel is high and error is low is REALLY differnt to Maxvel low error low

*/
/* 
it should be noted, that the max values are often used if the error tol is low, becouse it cant do it in one timestep and keep within the tolerances of MAXSPEED
so it will just try to spin right and take more timesteps, on the next timestep the vel needied to turn the angle and keep the tol might be within the
max tol so then it'll start to move forward
*/
/*
IMPORTANT :::: When error > 1, the robot will always go forward untill told to stop, with max the max vel given within tolerance of the error
			   When error = 0. The robot will rotate, and maxvel is time wanted to compleate the rotation
*/

#define wheelbase 32

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

void WorkOutWheelSpeed(void);
void JustRotate(void);
void SetupRobot(void);

using namespace std;
using namespace yarp;

/*** Robot Defs ***/

ArRobot robot(NULL,false);
ArVCC4 ptz(&robot, false, ArVCC4::COMM_UNKNOWN,true, false, ArVCC4::CAMERA_C50I);
ArSerialConnection RobotConnectoin;
ArSerialConnection CameraConnectoin;
ArSonarDevice sonarDev;
ArBumpers bumpers;



/*** formula defs ***/
	double CircumWheel  = wheelbase*M_PI;   // the circum with the wheel diameter
	double AngleToCM    = CircumWheel/360;  // break that down to how far the wheel has to travel to get rotate 1 deg (spinning on spot)
	double AllowedError = 0;				// how far the robot can go off corse, higher = smoother = less accurate
	double AllowedSpeed	= 0;				// the wanted speed also is the max for both rotational and liner speed
	double RatioErrorSpeed =0;				// the ratio between error and speed, it makes sure even with high vel the turn is always within error
	double MaxAngleChangeOneTSpeed =0;      // the max angle that can be made in one timestep with full speed on both wheels
	double MaxAngleChangeOneTError =0;      // the max angle that can be made in one timestep within full error on both wheels
	double LeftWheel =0;				
	double RightWheel =0;				
	double WantedAngle=0;
	double CurrentAngle=0;
	double DiffernceAngle=0;
	double WheelDiffAngle =0;				// the diff in wheel speed to get to the disired rotation within error tol
	double LeftOverSpeed =0;

/*** Robot defs ***/
	Bottle CurrentRotVal;
	Bottle WantedRotVelError;
	Bottle SonarState;

int main()
{
	Network yarp;
	SamgarModule VR("Vrobot","Loco","wheel",run); // Cant have spaces or underscores
	VR.AddPortS("VAEin");
	VR.AddPortS("ANGin");
	VR.AddPortS("SONin");
	SetupRobot();

	while(1)
	{
	if(VR.GetBottleData("ANGin",&CurrentRotVal)==true){	CurrentAngle = (CurrentRotVal.get(0).asDouble())*-1;} // from stargazer
	if(VR.GetBottleData("VAEin",&WantedRotVelError)==true)
		{
			AllowedSpeed =WantedRotVelError.get(0).asDouble();
			WantedAngle  =WantedRotVelError.get(1).asDouble();
			AllowedError =WantedRotVelError.get(1).asDouble();
		}
	cin >> AllowedSpeed;
	cin >> WantedAngle;
	cin >> AllowedError;

	if(AllowedError!=0){WorkOutWheelSpeed();}
	else			   {JustRotate();}

	robot.setVel2(LeftWheel*100,RightWheel*100);

	if(VR.GetBottleData("SONin",&SonarState)==true)
		{
		if(SonarState.get(0).asDouble()==1)		{	robot.enableSonar() ;}
		else								    {	robot.disableSonar();}
		}

	}
	robot.disconnect();
	Aria::shutdown();
	return 0;
}

void SetupRobot(void)
{
	RobotConnectoin.setPort("COM8");
	RobotConnectoin.setBaud(9600);
	robot.setDeviceConnection(&RobotConnectoin);
	if(!robot.blockingConnect()){puts("not connected to robot");Aria::shutdown();}
	robot.runAsync(false);
	robot.addRangeDevice(&sonarDev);
	robot.addRangeDevice(&bumpers);
	robot.enableMotors();
	robot.enableSonar();

}

void JustRotate(void)
{
	DiffernceAngle    =   WantedAngle-CurrentAngle;
	WheelDiffAngle    =   (AngleToCM*abs(DiffernceAngle))/AllowedSpeed;
	
	 if(WantedAngle>0)
	 {
		RightWheel = 0 - WheelDiffAngle;
		LeftWheel  = 0 + WheelDiffAngle; // should always be the wanted speed.
	 }
	 else
	 {
		RightWheel = 0 + WheelDiffAngle;// should always be the wanted speed.
		LeftWheel  = 0 - WheelDiffAngle; 
	 }
	if(WheelDiffAngle!=AllowedSpeed&&WheelDiffAngle!=-AllowedSpeed)
	{
	 printf("ang: %+4f Left: %+4f  Right: %+4f \t wheeldif:%+4f \n",WantedAngle,LeftWheel,RightWheel,WheelDiffAngle);
	}
}



void WorkOutWheelSpeed(void)
{
	 
	 DiffernceAngle    =   WantedAngle-CurrentAngle;
	 RatioErrorSpeed   =   AllowedError/AllowedSpeed;
	 WheelDiffAngle    =   (AngleToCM*abs(DiffernceAngle))/RatioErrorSpeed; // actully its the speed the wheels need to go at to achive angle in 1 time step
	
	 if(WheelDiffAngle >  AllowedSpeed ){WheelDiffAngle=AllowedSpeed;}
	 
	 LeftOverSpeed     =   AllowedSpeed-abs(WheelDiffAngle);

	 if(WantedAngle>0)
	 {
		RightWheel =LeftOverSpeed - WheelDiffAngle;
		LeftWheel  =LeftOverSpeed + WheelDiffAngle; // should always be the wanted speed.
	 }
	 else
	 {
		RightWheel =LeftOverSpeed + WheelDiffAngle;// should always be the wanted speed.
		LeftWheel  =LeftOverSpeed - WheelDiffAngle; 
	 }
	if(WheelDiffAngle!=AllowedSpeed&&WheelDiffAngle!=-AllowedSpeed)
	{
	 printf("ang: %+4f Left: %+4f  Right: %+4f \t wheeldif:%+4f \n",WantedAngle,LeftWheel,RightWheel,WheelDiffAngle);
	}
}



