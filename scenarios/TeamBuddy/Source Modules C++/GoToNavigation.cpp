#include "SamClass.h"
#include "Aria.h"
#include <cstdlib>
#include <iostream>
#include <fstream>



/** 
*/

double calculate_distance (double x1,double y1,double x2 ,double y2);
double calcAngle(double p1_x, double p1_y, double p2_x, double p2_y);
void SetGoalPos(int userPose);
int GetGoalFromString(std::string str );
double Rad2Deg(double x)
{
	return x*57.2957795;
};
double Deg2Rad(double x)
{
	return x/57.2957795;
};

ArPose GoalPose(0,0,0);
ArPose CurrentPose(0,0,0);
ArPose OldPose(0,0,0);

ArPose RechargePose(0,0,0);//8
ArPose VisitorPose(-0.75,2.0,4.5);//9
ArPose HomePose(-0.75,3.0,4.5);//0
ArPose DoorPose(-1.8,1.8,3.1);//7
ArPose Table1Pose(0.85,3.3,0.5);//1
ArPose Table2Pose(0.85,2.3,0);//2
ArPose Table3Pose(0.85,0.75,0);//3
ArPose Table4Pose(1.1,-1.3,6.0);//4
ArPose Table5Pose(1.1,-2.5,6.0);//5
ArPose Table6Pose(1.1,-3.5,5.0);//6

int userPose=-1;
bool bReached=true;

//The number of mm away from obstacle to begin turnning
double myTurnThreshold=550;
// amount to turn when turning is needed
double myTurnAmount=6;
// remember which turn direction we requested, to help keep turns smooth
int myTurning=0; // -1 == left, 1 == right, 0 == none
//Maximum speed to go
double myMaxSpeed=220;
// Distance at which to stop from obstacle.
double myStopDistance=450;
// Current robot speed.
double speed=0;

#define PI 3.14159265;

ArRobot *robot=new ArRobot();

bool ObstacleAvoidance(void);
int iStarCntr=0;
bool firstStarReading=false;
ifstream myfile;
string MyData;
double OldDist=0;
double DiffDist=0;
int iStuckCount=0;
int iGoalPostion=0;
int loopCnt=0;
class GotNav: public SamClass
{
private:
//BufferedPort<Bottle> CommandIn;				
BufferedPort<Bottle> StarIn;
BufferedPort<Bottle> GoalIn;
//BufferedPort<Bottle> LaserIn;
//BufferedPort<Bottle> PwrOut;

Network yarp;


public:
	double dX;
	double dY;
	double dTh;
	double dStarId;

	void SamInit(void)
	{
	RecognisePort("StarIn");
	RecognisePort("GoalIn");
	//RecognisePort("PwrOut");
	//RecognisePort("LaserIn");
	StartModule("/GotoNav");
	StarIn.open("/GotoNav_StarIn");
	GoalIn.open("/GotoNav_GoalIn");
	//PwrOut.open("/GotoNav_PwrOut");
	
	//LaserIn.open("/GotoNav_LaserIn");

	StarIn.setStrict(true);
	GoalIn.setStrict(true);
	//PwrOut.setStrict(true);
	//LaserIn.setStrict(true);

	StarIn.setReporter(myPortStatus);
	GoalIn.setReporter(myPortStatus);
	//PwrOut.setReporter(myPortStatus);
	//LaserIn.setReporter(myPortStatus);

	
	//myfile.open ("motion.txt");
	//if (myfile.is_open())
	//{
	//	myfile << 0 << "|" << 0 << "\n";
	//	myfile.close();
	//}

	//system("move.exe");
	yarp::os::Time::delay(0.5);


	puts("started nav go to");
	}

	void SamIter(void)
	{
		Bottle *goal = GoalIn.read(false);
		//puts("running reader");
		//while (GoalIn.getPendingReads() > 0)
		//	goal = GoalIn.read(false);

		if(goal!=NULL)						 // check theres data
		{
		
			std::cout << "size of bottle is " << goal->size() << " data " << goal->toString().c_str() << std::endl; 

			if(goal->size()>0)
			{
				//iGoalPostion =GetGoalFromString(goal->get(1).asString().c_str());
				iGoalPostion=atoi(goal->get(1).asString().c_str());
				SetGoalPos(iGoalPostion);
				bReached=false;
				std::cout<< "got a goal, location " <<  iGoalPostion <<std::endl;
				iStuckCount=0;
				loopCnt=0;
				robot->enableMotors();
				robot->enableSonar();
			}
		}
		
		//// send back a bottle with current voltage value
		//Bottle& b2 = PwrOut.prepare();	  // prepare the bottle/port
		//b2.clear();
		//b2.addDouble(robot->getRealBatteryVoltage()); // indicates robot voltage avg		
		//PwrOut.writeStrict();
		
		
	
		Bottle *input = NULL;
		//puts("running reader");
		while (StarIn.getPendingReads() > 0)
			input = StarIn.read(false);

		if(input!=NULL)						 // check theres data
		{
			
			dStarId = input->get(0).asDouble();
			//std::cout << "star id " << starID <<  std::endl;
			dX = input->get(1).asDouble();
			dY = input->get(2).asDouble();
			dTh = input->get(3).asDouble();

			

			if(firstStarReading==false)
			{
				CurrentPose.setX(dX);
				CurrentPose.setY(dY);
				CurrentPose.setTh(dTh);
				OldPose = CurrentPose;
				firstStarReading=true;
			}


			iStarCntr++;
			

			if((OldPose.findDistanceTo(CurrentPose)<1.5 || iStarCntr>3) && dStarId!=0)
			{
				CurrentPose.setX(dX);
				CurrentPose.setY(dY);
				CurrentPose.setTh(dTh);
				OldPose = CurrentPose;
				iStarCntr=0;
				
			}
			//puts("got a msg");
			//puts(input->toString());
			//std::cout << " robot->getRealBatteryVoltageNow() "  << " " << robot->getRealBatteryVoltageNow()<<std::endl;
			
		}
		
		
    

		 if(bReached==false)
		 {
			
			  loopCnt++;
			  double ang = atan2(GoalPose.getY() - CurrentPose.getY(), GoalPose.getX() - CurrentPose.getX());
			  double dist =  GoalPose.findDistanceTo(CurrentPose);

			  //check if the robot is stuck while navigating
			  if(dist>0)
			  {
				  //first distance reading
				  if(iStuckCount==0)
					OldDist=dist;
	
				 DiffDist=OldDist-dist;
				
				  
				 if(loopCnt%5==0 && abs(DiffDist)<0.1)
				 {
					iStuckCount++;
					std::cout << "diff dist, dist, stuck count  " << DiffDist << " " << dist << " " << iStuckCount <<std::endl;
				 }
				 else
					OldDist=dist;

				 

					//recover motion
					if(iStuckCount>6)
					{
						robot->lock();
						robot->setVel(-100);//move back
						
						yarp::os::Time::delay(5);
						robot->unlock();
						//robot->setVel(0);//move back
						
						iStuckCount=0;
	 
					}
					
			  }
			 
			  //calculate_distance(dX, dY, GoalPose.getX(), GoalPose.getY());//ar.findDistanceTo(ArPose(-0.75,3.0,4.5));
			  ang-=dTh;

			  while(ang>3.14159265){ang-=6.28318531;}  // normalise PI ie if over 180, then minus 360
			  while(ang<-3.14159265){ang+=6.28318531;} 

			  ang = Rad2Deg(ang);
			  //std::cout << "required angle, dist  " << ang << " " << dist <<std::endl;

			  //if the robot has to turn more then turn threshold for obst avoidance is less and speed is 0
			  if(abs(ang)>55)
			  {
				myTurnThreshold = 250;
				myStopDistance = 200;
				speed=0;
			  }
			  else
			  {
				 speed=220;
				 myTurnThreshold = 550; 
				 myStopDistance = 450;
			  }


			  bool obst= ObstacleAvoidance();

			  if(dist>=0.7 && obst==false)//&& abs(ang)>10
			  {
				robot->lock();
				std::cout <<"navigating to goal " << iGoalPostion<<std::endl;
				
			  /*	if(abs(ang)<45)
					speed=200;	
				else
					speed=0;*/

			   robot->setDeltaHeading(ang/2.0);
			   robot->setVel(speed);
			   robot->unlock();
			  }
			  else if(dist<0.7)
			  {
				robot->lock();

				double rotFinal = GoalPose.getTh();
				rotFinal-=dTh;
				std::cout <<"goal rot " << rotFinal <<std::endl;
				
				while(rotFinal>3.14159265){rotFinal-=6.28318531;}  // normalise PI ie if over 180, then minus 360
				while(rotFinal<-3.14159265){rotFinal+=6.28318531;} 

				rotFinal = Rad2Deg(rotFinal);
				
				robot->setDeltaHeading(rotFinal);
				robot->setVel(0);

	
				std::cout <<"final rot " << rotFinal <<std::endl;
				//robot->setDeltaHeading(0);
				
			    ang=0;
				speed=0;
				puts("reached ");
				bReached=true;
				robot->unlock();
				

				// send back a bottle with goal position reached
				Bottle& b2 = GoalIn.prepare();	  // prepare the bottle/port
				b2.clear();
				b2.addInt(iGoalPostion); // indicates robot reached goal location
				GoalIn.writeStrict();

				//wait for some time for rotation to complete
				yarp::os::Time::delay(5);
				robot->disableMotors();
				robot->disableSonar();
				
			  }
			  
			
			/*myfile.open ("motion.txt");
			if (myfile.is_open())
			{
				myfile << ang << "|" << speed << "\n";
				myfile.close();
			}*/
		 }
		//else{puts("didn't get a msg");}
	} 
};

GotNav *nav;
using namespace std;
int main(int argc, char **argv)
{
  nav = new GotNav ();
  nav->SamInit();
  
  Aria::init();
  ArArgumentParser parser(&argc, argv);
  parser.loadDefaultArguments();
  ArSimpleConnector simpleConnector(&parser);

  ArSonarDevice sonar;
  ArAnalogGyro gyro(robot);
  robot->addRangeDevice(&sonar);

  // Make a key handler, so that escape will shut down the program
  // cleanly
  ArKeyHandler keyHandler;
  Aria::setKeyHandler(&keyHandler);
  robot->attachKeyHandler(&keyHandler);
  printf("You may press escape to exit\n");

  // Parse all command line arguments
  if (!Aria::parseArgs() || !parser.checkHelpAndWarnUnparsed())
  {    
    Aria::logOptions();
    exit(1);
  }
  
  // Connect to the robot
  if (!simpleConnector.connectRobot(robot))
  {
    printf("Could not connect to robot... exiting\n");
    Aria::exit(1);
  }
 // robot.runAsync(true);

  robot->runAsync(false);

  // turn off the motors, turn off amigobot sounds
  robot->disableMotors();
  robot->disableSonar();
  robot->comInt(ArCommands::SOUNDTOG, 0);
  robot->setAbsoluteMaxTransVel(myMaxSpeed);
  robot->setAbsoluteMaxRotVel(40);
 
  yarp::os::Time::delay(2);

  bool first = true;
  int goalNum = 0;
  ArTime start;
  start.setToNow();
  
 
  //GoalPose = Table4Pose;
  while (1) 
  {
	 
	/*
	if(bReached==true)
	{
		bReached=false;
	 
	  puts("Press 0 to go to HomePose(-0.75,3.0,1.5)");
	  puts("Press 1 to go to Table1Pose(1.3,2.8,0.1)");
	  puts("Press 4 to go to Table4Pose(1.4,-1.3,0.1)");
	  puts("Press 6 to go to DoorPose(-2.0,1.8,3.1)");
						

				cin >> userPose;
				

				switch ( userPose ) 
				{

					case 0 : 	GoalPose = HomePose;
					break;

					case 1 : 	GoalPose = Table1Pose;
					break;

					case 4 : 	GoalPose = Table4Pose;
					break;

					case 6 : 	GoalPose = DoorPose;
					break;
					
					default : 
					GoalPose = HomePose;
					break;
				}
	  }
	  */
	  nav->SamIter();

	  yarp::os::Time::delay(1);
	  
	
  }
  
  // Robot disconnected or time elapsed, shut down
  Aria::shutdown();
  return 0;
}

double calcAngle(double p1_x, double p1_y, double p2_x, double p2_y)
{
double a_x = p2_x - p1_x;
double a_y = p2_y - p1_y;

double b_x = 1.0;
double b_y = 0.0;

return acos((a_x*b_x+a_y*b_y)/sqrt(a_x*a_x+a_y*a_y));
}

double calculate_distance (double x1,double y1,double x2 ,double y2)
{

double distance;

double distance_x = x1-x2;

double distance_y = y1- y2;

distance = sqrt( (distance_x * distance_x) + (distance_y * distance_y));

return distance;

}


//  This is for obsracle avoidance

bool ObstacleAvoidance()
{
  double leftRange, rightRange;
  double range;
  bool obstacleflag=false;
 
  int BumperValue = robot->getStallValue();
  robot->lock();

  //check first if bumper is triggered
  if( BumperValue >0)
  {
	  std::cout << "stall value "  << robot->getStallValue() <<std::endl;
    //front bumper triggered
	if(BumperValue>32)
	{
		 robot->setVel(-100);//move back
	}
	else 
		 robot->setVel(100);//move front

	
	 robot->unlock();
	 yarp::os::Time::delay(4);
	 //robot->setVel(0);//stop
	 
	 return true;
  
  }
/*
	MyData="";
	myfile.open ("laser.txt");

	std::getline(myfile,MyData); // Saves the line in STRING.
     
	int iLaser = atoi(MyData.c_str());//read laser reading 0=front, -1=right, 1=left

	myfile.clear();
	myfile.close();
 */
  // Get the left readings and right readings off of the sonar, first check for front obstacles 
  range =  robot->getClosestSonarRange(-30,30);//mySonar->currentReadingPolar(-70, 70) - robot.getRobotRadius();

  
  if (range < myStopDistance)// || iLaser==0 )
  {
    // the range was less than the stop distance, so request stop
    robot->setVel(0);
	obstacleflag=true;
  }
  /*
  else
  {
    // just an arbitrary speed based on the range
    speed = range * .3;
    // if that speed is greater than our max, cap it
    if (speed > myMaxSpeed)
      speed = myMaxSpeed;
    // now set the velocity
    robot->setVel(speed);
   
  }*/

	//now check for side obstacles
  leftRange = robot->getClosestSonarRange(0, 100);

  rightRange = robot->getClosestSonarRange(-100, 0);
  // if neither left nor right range is within the turn threshold,
  // reset the turning variable and don't turn
  if (leftRange > myTurnThreshold && rightRange > myTurnThreshold)// && iLaser==2)
  {
    myTurning = 0;
    robot->setDeltaHeading(0);
  }
  // if we're already turning some direction, keep turning that direction
  else if (myTurning )
  {
    robot->setDeltaHeading(myTurnAmount * myTurning);
	obstacleflag=true;
  }
  // if we're not turning already, but need to, and left is closer, turn right
  // and set the turning variable so we turn the same direction for as long as
  // we need to
  else if (leftRange < rightRange )// || iLaser==1)
  {
    myTurning = -1;
    robot->setDeltaHeading(myTurnAmount * myTurning);
	obstacleflag=true;
	
  }
  // if we're not turning already, but need to, and right is closer, turn left
  // and set the turning variable so we turn the same direction for as long as
  // we need to
  else if (leftRange > rightRange )// || iLaser==-1)
  {
    myTurning = 1;
    robot->setDeltaHeading(myTurnAmount * myTurning);
	obstacleflag=true;
	
  }
  else
  {
    myTurning = 1;
    robot->setDeltaHeading(myTurnAmount * myTurning);
	obstacleflag=true;
  }

  if(obstacleflag)
  	  std::cout << "ranges: front, left, right " << range << " " << leftRange << " " << leftRange << std::endl;
	
   
  
 // if(obstacleflag)
//	robot->forceTryingToMove();

   robot->unlock();
  return obstacleflag;
  
}

void SetGoalPos(int userPose)
{
	switch ( userPose ) 
	{

			case 0 : 	GoalPose = HomePose;
			break;

			case 1 : 	GoalPose = Table1Pose;
			break;

			case 2 : 	GoalPose = Table2Pose;
			break;

			case 3 : 	GoalPose = Table3Pose;
			break;

			case 4 : 	GoalPose = Table4Pose;
			break;

			case 5 : 	GoalPose = Table5Pose;
			break;

			case 6 : 	GoalPose = Table6Pose;
			break;

			case 7 : 	GoalPose = DoorPose;
			break;

			case 8 : 	GoalPose = RechargePose;
			break;

			case 9 : 	GoalPose = VisitorPose;
			break;
		
			default : 
			GoalPose = HomePose;
			break;
	}

	

}

int GetGoalFromString(std::string str )
{

	if(str=="Amol")
		return 1;
	else if(str=="Iain")
		return 2;
	else if(str=="Asad")
		return 3;
	else if(str=="Christopher")
		return 4;
	else if(str=="Michael")
		return 5;		
	else if(str=="Matthias")
		return 6;
	else if(str=="Home")
		return 0;


}