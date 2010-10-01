/*
	PROGRAM:	Level 2 of Pioneer's Physical Expression Module 
	Author :	K . L. Koay
	Date   :    05 May 2010

*/

#include <Windows.h>
#include "SamgarMainClass.h"

//int AbsoluteMaxRotAccel = 300;		//Sets the robot's absolute maximum rotational acceleration. 
//int AbsoluteMaxRotDecel = 300;		//Sets the robot's absolute maximum rotational deceleration. 
//int AbsoluteMaxRotVel = 1500;		//Sets the robot's absolute maximum rotational velocity. 

//int AbsoluteMaxTransAccel = 2000;	//Sets the robot's absolute maximum translational acceleration. 
//int AbsoluteMaxTransDecel = 2000;//Sets the robot's absolute maximum translational deceleration. 
//int AbsoluteMaxTransVel = 1500; //Sets the robot's absolute maximum translational velocity. 


int CAM_Init=-99999;
int ROBOT_Stop=-88888;
int ROBOT_FinishedTransMovement=-77777;
int ROBOT_Wait=60000;
int CAM_Wait=30000;

int ROBOT_Move=17367;
int ROBOT_DeltaHeading=6986;//
int ROBOT_AbsoluteMaxTransVel=5096;
int ROBOT_AbsoluteMaxTransAccel=3086;	//Sets the robot's absolute maximum translational acceleration. 
int ROBOT_AbsoluteMaxTransDecel=1075;	//Sets the robot's absolute maximum translational deceleration. 
int ROBOT_AbsoluteMaxRotVel = 954;		//Sets the robot's absolute maximum rotational velocity. 
int ROBOT_AbsoluteMaxRotAccel = 644;		//Sets the robot's absolute maximum rotational acceleration. 
int ROBOT_AbsoluteMaxRotDecel = 333;		//Sets the robot's absolute maximum rotational deceleration. 
int CAM_Pan=231;	//
int CAM_Tilt=39;	//



int camWait(int wait)
{
	if (wait>20000){
		wait=20000;
		printf("\n Max waiting time is 20sec");
	}
	return wait+=CAM_Wait;
}
int robotWait(int wait)
{
	if (wait>20000){
		wait=20000;
		printf("\n Max waiting time is 20sec");
	}
	return wait+=ROBOT_Wait;
}

int robotMove(int moveCommand)
{
return moveCommand+=ROBOT_Move;
}

int robotDeltaHeading(int deltaHeadingCommand)
{
return deltaHeadingCommand+=ROBOT_DeltaHeading;
}

int setMaxTransVel(int MaxTransVel)
{
return MaxTransVel+=ROBOT_AbsoluteMaxTransVel;
}

int setMaxTransAccel(int MaxTransAccel)
{
return MaxTransAccel+=ROBOT_AbsoluteMaxTransAccel;
}

int setMaxTransDecel(int MaxTransDecel)
{
return MaxTransDecel+=ROBOT_AbsoluteMaxTransDecel;
}
int setMaxRotVel(int MaxRotVel)
{
return MaxRotVel+=ROBOT_AbsoluteMaxRotVel;
}
int setMaxRotAccel(int MaxRotAccel)
{
return MaxRotAccel+=ROBOT_AbsoluteMaxRotAccel;
}
int setMaxRotDecel(int MaxRotDecel)
{
return MaxRotDecel+=ROBOT_AbsoluteMaxRotDecel;
}
int camPan(int pan)
{
	return pan = CAM_Pan + pan;
}
int camTilt(int tilt)
{
	return tilt = CAM_Tilt + tilt;
}




//2147483648 to +2147483647



void behaviour_Physical(SamgarModule &PhysicalRef, int behaviour)
{
Bottle BehaviourBaseOut;
Bottle BehaviourCamOut;
int ii, jj;
switch (behaviour) {
    case  0:	//MigrationOut - Done
		//Camera stats looking down
		/* for V1 of Vrobot
		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(0));
		BehaviourOut.addInt(camWait(18500));
		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(-30));
		//send data to virtual robot 
		PhysicalRef.SendBottleData("L2PBOut", BehaviourOut); //all physical movement share port i.e. cam and base movement
		BehaviourOut.clear();
		*/
		//New Vrobot - v2
		BehaviourCamOut.addDouble(0); BehaviourCamOut.addDouble(0);	BehaviourCamOut.addDouble(18500);
		BehaviourCamOut.addDouble(0); BehaviourCamOut.addDouble(-30);	BehaviourCamOut.addDouble(0);
		PhysicalRef.SendBottleData("L2PB_COut", BehaviourCamOut);	//send cam data to cam port
		BehaviourCamOut.clear();
		printf("Migration Out\n");

		break;

	case  1:	//'MigrationInto - Done	
		/*for V1 of Vrobot
		BehaviourOut.addInt(camWait(21500));
		BehaviourOut.addInt(CAM_Init);	//Init the Camera and then start looking forward
		BehaviourOut.addInt(camWait(5000));
		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(20));
		*/
		//New Vrobot -v2
		BehaviourCamOut.addDouble(-666);	BehaviourCamOut.addDouble(-666);	BehaviourCamOut.addDouble(21500); //do nothing to camera just wait
		BehaviourCamOut.addDouble(999);	BehaviourCamOut.addDouble(999);	BehaviourCamOut.addDouble(5000);	//init the came and wait for 5sec
		BehaviourCamOut.addDouble(0);		BehaviourCamOut.addDouble(20);		BehaviourCamOut.addDouble(0);	//init the came and wait for 5sec
		printf("Migration Into\n");
		//send data to virtual robot 
		//for V1 of Vrobot
		//PhysicalRef.SendBottleData("L2PBOut", BehaviourOut); //all physical movement share port i.e. cam and base movement
		//BehaviourOut.clear();
		//for Vrobot - v2
		PhysicalRef.SendBottleData("L2PB_COut", BehaviourCamOut);	//send cam data to cam port
		BehaviourCamOut.clear();
	  break;

    case  2:	//'Happy':
		/*for V1 of Vrobot
		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(20));

		BehaviourOut.addInt(robotDeltaHeading(-15)); //turn right
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire movemyRobot->lock();

		BehaviourOut.addInt(robotDeltaHeading(30)); //turn left
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire movemyRobot->lock();

		BehaviourOut.addInt(robotDeltaHeading(-30)); //turn right
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire movemyRobot->lock();

		BehaviourOut.addInt(robotDeltaHeading(30)); //turn left
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire movemyRobot->lock();

		BehaviourOut.addInt(robotDeltaHeading(-15)); //turn right
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire movemyRobot->lock();

		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(20));
		BehaviourOut.addInt(camWait(1000));
		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(-20));
		BehaviourOut.addInt(camWait(1000));
		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(20));
		BehaviourOut.addInt(camWait(1000));
		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(-20));
		BehaviourOut.addInt(camWait(1000));
		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(20));

		PhysicalRef.SendBottleData("L2PBOut", BehaviourOut);
		BehaviourOut.clear();
		*/
		//for Vrobot - v2
		//Base
		BehaviourBaseOut.addDouble(-1); 	//setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);		//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);		//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);		//setMaxRotVel
		BehaviourBaseOut.addDouble(0);		//translation
		BehaviourBaseOut.addDouble(-15);	//rotation - turn right
		
		BehaviourBaseOut.addDouble(-1); 	//setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);		//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);		//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);		//setMaxRotVel
		BehaviourBaseOut.addDouble(0); 		//translation
		BehaviourBaseOut.addDouble(30); 	//rotation - turn left
		
		BehaviourBaseOut.addDouble(-1); 	//setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);		//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);		//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);		//setMaxRotVel
		BehaviourBaseOut.addDouble(0); 		//translation
		BehaviourBaseOut.addDouble(-30);	//turn right
		
		BehaviourBaseOut.addDouble(-1); 	//setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);		//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);		//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);		//setMaxRotVel
		BehaviourBaseOut.addDouble(0); 		//translation
		BehaviourBaseOut.addDouble(30); 	//turn left
		
		BehaviourBaseOut.addDouble(-1);		//setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);		//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);		//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);		//setMaxRotVel
		BehaviourBaseOut.addDouble(0); 		//translation
		BehaviourBaseOut.addDouble(-15);	//turn right
		
		//Cam
		BehaviourCamOut.addDouble(0);	BehaviourCamOut.addDouble(20);	BehaviourCamOut.addDouble(1000);
		BehaviourCamOut.addDouble(0);	BehaviourCamOut.addDouble(-20);	BehaviourCamOut.addDouble(1000);
		BehaviourCamOut.addDouble(0);	BehaviourCamOut.addDouble(20);	BehaviourCamOut.addDouble(1000);
		BehaviourCamOut.addDouble(0);	BehaviourCamOut.addDouble(-20);	BehaviourCamOut.addDouble(1000);
		BehaviourCamOut.addDouble(0);	BehaviourCamOut.addDouble(20);	BehaviourCamOut.addDouble(0);
		
		
		PhysicalRef.SendBottleData("L2PB_BOut", BehaviourBaseOut);	//send Base data to Base port
		PhysicalRef.SendBottleData("L2PB_COut", BehaviourCamOut);	//send cam data to cam port
		BehaviourBaseOut.clear();
		BehaviourCamOut.clear();
	  break;

    case  3:	//Excited':
		/*for V1 of Vrobot
		BehaviourOut.addInt(setMaxTransAccel(250)); 
		BehaviourOut.addInt(setMaxTransDecel(250));

		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(20));

		BehaviourOut.addInt(robotMove(40)); //move forward
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire move
		
		BehaviourOut.addInt(robotMove(-80)); //move backward
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire move
		
		BehaviourOut.addInt(robotMove(80)); //move forward
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire move
		  
		BehaviourOut.addInt(robotMove(-80)); //move backward
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire move
		  
		BehaviourOut.addInt(robotMove(80)); //move forward
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire move
		  
		BehaviourOut.addInt(robotMove(-80)); //move backwardward
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire move
		 
		BehaviourOut.addInt(robotMove(80)); //move forward
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire move
		  
		BehaviourOut.addInt(robotMove(-40)); //move backward
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire move

		BehaviourOut.addInt(ROBOT_Stop); //stop the robot if it reach the desire move
		BehaviourOut.addInt(robotWait(200)); //wait for the robot to stop 
		  
		BehaviourOut.addInt(camPan(-10)); BehaviourOut.addInt(camTilt(25));
		BehaviourOut.addInt(camWait(800));
		BehaviourOut.addInt(camPan(10)); BehaviourOut.addInt(camTilt(25));
		BehaviourOut.addInt(camWait(800));
		BehaviourOut.addInt(camPan(-10)); BehaviourOut.addInt(camTilt(25));
		BehaviourOut.addInt(camWait(800));
		BehaviourOut.addInt(camPan(10)); BehaviourOut.addInt(camTilt(25));
		BehaviourOut.addInt(camWait(800));
		BehaviourOut.addInt(camPan(-10)); BehaviourOut.addInt(camTilt(25));
		BehaviourOut.addInt(camWait(800));
		BehaviourOut.addInt(camPan(10)); BehaviourOut.addInt(camTilt(25));
		BehaviourOut.addInt(camWait(800));
		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(25));
		BehaviourOut.addInt(camWait(200));

		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(20));
		PhysicalRef.SendBottleData("L2PBOut", BehaviourOut);
		BehaviourOut.clear();
		*/
		BehaviourBaseOut.addDouble(250); //setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotVel
		BehaviourBaseOut.addDouble(40); //move forward
		BehaviourBaseOut.addDouble(0);	//delta 
		
		BehaviourBaseOut.addDouble(250); //setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotVel
		BehaviourBaseOut.addDouble(-80); //move backward
		BehaviourBaseOut.addDouble(0);	//delta 
		
		BehaviourBaseOut.addDouble(250); //setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotVel
		BehaviourBaseOut.addDouble(80); //move forward
		BehaviourBaseOut.addDouble(0); 	//rotation - delta heading
		
		
		BehaviourBaseOut.addDouble(250); //setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotVel
		BehaviourBaseOut.addDouble(-80); //move backward
		BehaviourBaseOut.addDouble(0); 	//rotation - delta heading
		
		BehaviourBaseOut.addDouble(250); //setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotVel
		BehaviourBaseOut.addDouble(80); 	//move forward
		BehaviourBaseOut.addDouble(0); 	//rotation - delta heading
		
		BehaviourBaseOut.addDouble(250); //setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotVel
		BehaviourBaseOut.addDouble(-80); //move backwardward
		BehaviourBaseOut.addDouble(0); 	//rotation - delta heading
		
		BehaviourBaseOut.addDouble(250); //setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotVel
		BehaviourBaseOut.addDouble(80); //move forward
		BehaviourBaseOut.addDouble(0); 	//rotation - delta heading
		
		BehaviourBaseOut.addDouble(250); //setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotVel
		BehaviourBaseOut.addDouble(-40); //move backward
		BehaviourBaseOut.addDouble(0); 	//rotation - delta heading
		
		//Cam
		//BehaviourCamOut.addDouble(0); BehaviourCamOut.addDouble(20);  	BehaviourCamOut.addDouble(800);
		BehaviourCamOut.addDouble(-10);	BehaviourCamOut.addDouble(25);		BehaviourCamOut.addDouble(800);
		BehaviourCamOut.addDouble(10); 	BehaviourCamOut.addDouble(25);		BehaviourCamOut.addDouble(800);
		BehaviourCamOut.addDouble(-10); BehaviourCamOut.addDouble(25);		BehaviourCamOut.addDouble(800);
		BehaviourCamOut.addDouble(10); 	BehaviourCamOut.addDouble(25);		BehaviourCamOut.addDouble(800);
		BehaviourCamOut.addDouble(-10); BehaviourCamOut.addDouble(25);		BehaviourCamOut.addDouble(800);
		BehaviourCamOut.addDouble(10); 	BehaviourCamOut.addDouble(25);		BehaviourCamOut.addDouble(800);
		BehaviourCamOut.addDouble(0); 	BehaviourCamOut.addDouble(25);		BehaviourCamOut.addDouble(200);
		BehaviourCamOut.addDouble(0); 	BehaviourCamOut.addDouble(20);		BehaviourCamOut.addDouble(0);
		
		PhysicalRef.SendBottleData("L2PB_BOut", BehaviourBaseOut);
		PhysicalRef.SendBottleData("L2PB_COut", BehaviourCamOut);
		BehaviourBaseOut.clear();
		BehaviourCamOut.clear();
		
	  break;

    case  4:	//'Bored':
    /*for V1 of Vrobot
		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(20));

		BehaviourOut.addInt(setMaxRotAccel(10)); 
		BehaviourOut.addInt(setMaxRotDecel(10));

		BehaviourOut.addInt(robotDeltaHeading(-35)); //turn right
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire move
		BehaviourOut.addInt(ROBOT_Stop); //stop the robot if it reach the desire move
		BehaviourOut.addInt(robotWait(800)); //wait for the robot to stop FinishedRotMovement(myRobot);

		BehaviourOut.addInt(robotDeltaHeading(70)); //turn left
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire move
		BehaviourOut.addInt(ROBOT_Stop); //stop the robot if it reach the desire move
		BehaviourOut.addInt(robotWait(800)); //wait for the robot to stop FinishedRotMovement(myRobot);

		BehaviourOut.addInt(robotDeltaHeading(-35)); //turn right
		//BehaviourOut.addInt(robotWait(100)); //wait for command to get to robot 
		//BehaviourOut.addInt(ROBOT_FinishedTransMovement); //then activate this to check if robot reach the desire move
		BehaviourOut.addInt(ROBOT_Stop); //stop the robot if it reach the desire move
		BehaviourOut.addInt(robotWait(800)); //wait for the robot to stop FinishedRotMovement(myRobot);

		//camera down and up randomly.//moves slowly or try to get attention //boring colour/orange on
		//restored Max Rot Acceel and Rot Decel

		jj=0;  
		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(-20));
		BehaviourOut.addInt(camWait(200));

		/*for (ii=0; ii<10; ii++) {
			//random select one
			BehaviourOut.addInt(camPan(2*ii)); BehaviourOut.addInt(camTilt(-20));
			BehaviourOut.addInt(camWait(500));
		}
		BehaviourOut.addInt(camWait(2000));

		for (ii=0; ii<10; ii++) {
			BehaviourOut.addInt(camPan(-4*ii)); BehaviourOut.addInt(camTilt(-20));
			BehaviourOut.addInt(camWait(500));
			//random select one
		} */
		/*BehaviourOut.addInt(camPan(45)); BehaviourOut.addInt(camTilt(45));
		BehaviourOut.addInt(camWait(1000));

		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(-30));
		BehaviourOut.addInt(camWait(2000));

		BehaviourOut.addInt(camPan(-45)); BehaviourOut.addInt(camTilt(45));
		BehaviourOut.addInt(camWait(1000));

		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(-30));
		BehaviourOut.addInt(camWait(2000));

		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(20));
		BehaviourOut.addInt(camWait(200));

		PhysicalRef.SendBottleData("L2PBOut", BehaviourOut);
		BehaviourOut.clear();
		
		*/
		

		BehaviourBaseOut.addDouble(-1); 	//setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxTransVel
		BehaviourBaseOut.addDouble(10);	//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotVel
		BehaviourBaseOut.addDouble(0); 	//move backward
		BehaviourBaseOut.addDouble(-35); //turn right
		
		BehaviourBaseOut.addDouble(-1); 	//setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxTransVel
		BehaviourBaseOut.addDouble(10);	//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotVel
		BehaviourBaseOut.addDouble(0); 	//move backward
		BehaviourBaseOut.addDouble(70); //turn left
		
		BehaviourBaseOut.addDouble(-1); 	//setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxTransVel
		BehaviourBaseOut.addDouble(10);	//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotVel
		BehaviourBaseOut.addDouble(0); 	//move backward
		BehaviourBaseOut.addDouble(-35); //turn right
		
		//camera down and up randomly.//moves slowly or try to get attention //boring colour/orange on
		//restored Max Rot Acceel and Rot Decel
		
		BehaviourCamOut.addDouble(0); BehaviourCamOut.addDouble(20);	BehaviourCamOut.addDouble(200);
		BehaviourCamOut.addDouble(0); BehaviourCamOut.addDouble(-20);	BehaviourCamOut.addDouble(2000);
		BehaviourCamOut.addDouble(45); BehaviourCamOut.addDouble(45);	BehaviourCamOut.addDouble(2000);
		BehaviourCamOut.addDouble(0); BehaviourCamOut.addDouble(-30);	BehaviourCamOut.addDouble(2000);
		BehaviourCamOut.addDouble(-45); BehaviourCamOut.addDouble(45);	BehaviourCamOut.addDouble(2000);
		BehaviourCamOut.addDouble(0); BehaviourCamOut.addDouble(-30);	BehaviourCamOut.addDouble(2000);
		BehaviourCamOut.addDouble(0); BehaviourCamOut.addDouble(20);	BehaviourCamOut.addDouble(200);
		

		PhysicalRef.SendBottleData("L2PB_BOut", BehaviourBaseOut);
		PhysicalRef.SendBottleData("L2PB_COut", BehaviourCamOut);
		BehaviourBaseOut.clear();
		BehaviourCamOut.clear();
		
	  break;

    case  5:	//'Tired' 
		/*
		//camera down
		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(-10));
		//BehaviourOut.addInt(camWait(2000));
		//Set the Max Trans Vel for tired
		//BehaviourOut.addInt(setMaxTransVel(50)); 
		BehaviourOut.addInt(setMaxTransAccel(10)); 
		BehaviourOut.addInt(setMaxTransDecel(10));

		
		//Performing the forward backward motion
		BehaviourOut.addInt(robotMove(20)); //move forward
		BehaviourOut.addInt(ROBOT_Stop); //stop the robot if it reach the desire move
		BehaviourOut.addInt(robotWait(200)); //wait for the robot to stop 	
		
		BehaviourOut.addInt(robotMove(-40)); //moves backward
		BehaviourOut.addInt(ROBOT_Stop); //stop the robot if it reach the desire move
		BehaviourOut.addInt(robotWait(200)); //wait for the robot to stop 
	 
		BehaviourOut.addInt(robotMove(40)); //moves forward
		BehaviourOut.addInt(ROBOT_Stop); //stop the robot if it reach the desire move
		BehaviourOut.addInt(robotWait(200)); //wait for the robot to stop 
	 
		BehaviourOut.addInt(robotMove(-20)); //move forward
		BehaviourOut.addInt(ROBOT_Stop); //stop the robot if it reach the desire move
		BehaviourOut.addInt(robotWait(200)); //wait for the robot to stop 

		//Restored the Max Trans Vel for tired
		/*
		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(20));
		BehaviourOut.addInt(camWait(2000));
		for (ii=0; ii<10; ii++) {
			BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(20-(ii*5)));
			//need to set a random select one action at some point
			BehaviourOut.addInt(camWait(500-ii*20));
		}*/
/*
		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(20));
		BehaviourOut.addInt(camWait(1000));

		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(-30));
		BehaviourOut.addInt(camWait(2000));

		BehaviourOut.addInt(camPan(0)); BehaviourOut.addInt(camTilt(20));
		BehaviourOut.addInt(camWait(200));

		PhysicalRef.SendBottleData("L2PBOut", BehaviourOut);
		BehaviourOut.clear();
		*/
		
		//camera down

		
		//Performing the forward backward motion
		BehaviourBaseOut.addDouble(10); 	//setMaxTransAccel, setMaxTransDecel -1: use vrobot value
		BehaviourBaseOut.addDouble(-1);	//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotVel
		BehaviourBaseOut.addDouble(20); 	//move backward
		BehaviourBaseOut.addDouble(0); 	//delta rot

		BehaviourBaseOut.addDouble(10); 	//setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotVel
		BehaviourBaseOut.addDouble(-40); 	//move backward
		BehaviourBaseOut.addDouble(0); 	//delta rot

		BehaviourBaseOut.addDouble(10); 	//setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotVel
		BehaviourBaseOut.addDouble(40); 	//move backward
		BehaviourBaseOut.addDouble(0); 	//delta rot
	 
 		BehaviourBaseOut.addDouble(10); 	//setMaxTransAccel, setMaxTransDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxTransVel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotAccel, setMaxRotDecel
		BehaviourBaseOut.addDouble(-1);	//setMaxRotVel
		BehaviourBaseOut.addDouble(-20); //move backward
		BehaviourBaseOut.addDouble(0); 	//delta rot
	 
	 	BehaviourCamOut.addDouble(0); 		BehaviourCamOut.addDouble(-10);		BehaviourCamOut.addDouble(2000); //might need bigger delay so it only do the below movement later - check with video
		BehaviourCamOut.addDouble(0); 		BehaviourCamOut.addDouble(20);		BehaviourCamOut.addDouble(1000);
		BehaviourCamOut.addDouble(0); 		BehaviourCamOut.addDouble(-30);		BehaviourCamOut.addDouble(2000);
		BehaviourCamOut.addDouble(0); 		BehaviourCamOut.addDouble(20);		BehaviourCamOut.addDouble(0);
		

		PhysicalRef.SendBottleData("L2PB_BOut", BehaviourBaseOut);
		PhysicalRef.SendBottleData("L2PB_COut", BehaviourCamOut);
		BehaviourBaseOut.clear();
		BehaviourCamOut.clear();
		
	  break;
	  
	  
	default:
		printf("\nNo Such Behaviour, I haven't learn that behaviour yet");
      break;


  }
}
int main(void)
{
	Network yarp;		//name			//Category //subcategory	
	SamgarModule Physical("PioneerPhysical", "Physical", "BaseAndCam", SamgarModule::interupt);
	Physical.AddPortS("L2PBIn");
	Physical.AddPortS("L2PB_BOut");
	Physical.AddPortS("L2PB_COut");
	
	Bottle BehaviourIn;
	while (1){
		if (Physical.GetBottleData("L2PBIn", &BehaviourIn, SamgarModule::NoStep)==true)
		{
			//printf("\n ---- I got something %d", BehaviourIn.get(0).asInt());
			behaviour_Physical(Physical, BehaviourIn.get(0).asInt());
			//printf("\n ---- Done sending Behaviour %d", BehaviourIn.get(0).asInt());
			BehaviourIn.clear();
		}
		Physical.SucceedFail(true,100);
	}
}