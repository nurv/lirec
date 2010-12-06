#include "Aria.h"

class ActionApproach : public ArAction
{
protected:
  ArActionDesired myDesired;
  ArRangeDevice *mySonar;
  ArRangeDevice *myLaser;
  ArRobot *myRobot;

  double myMaxSpeed;
  double myStopAngle;
  double myTargetTh;
  double mySpeedRatio;

  double *myStopDistance;
  double *myAngle;
  int *myOnlyTurn;
  int *myHowMany;
  int *myEStop;

public:
	ActionApproach(double *proxemicDistance, int *targetHowMany, double *targetAngle, int *onlyTurn, int *eStop):ArAction("Approach")
	{
	  myMaxSpeed = 100;
	  myStopAngle = 4;
	  myStopDistance = proxemicDistance;
	  myHowMany = targetHowMany;
	  myAngle = targetAngle;
	  myOnlyTurn = onlyTurn;
	  myEStop = eStop;
	  myTargetTh = 0;
	  mySpeedRatio = 0.5;
	}

  virtual ~ActionApproach(void) {};

  virtual ArActionDesired *fire(ArActionDesired currentDesired)
	{
		myDesired.reset();
		double range, speed;

		//ArPose pose = myRobot->getPose();
		//printf(" x %.3lf ", pose.getTh() );
		//printf(" x %.3lf ", myRobot->getTh() );
		//printf(" x ");
		if ( *myHowMany > 0 && fabs(*myAngle) > myStopAngle ) 
		{
			myTargetTh = myRobot->getTh() + *myAngle;
			myDesired.setRotVel(myTargetTh * mySpeedRatio);
			*myHowMany = 0;
			printf(" s");
		}
		else if ( myTargetTh != 0 && fabs(myTargetTh - myRobot->getTh()) <= myStopAngle ) 
		{
			myDesired.setRotVel(0);
			myTargetTh = 0;
			printf(" f");
		}
		else if ( myTargetTh != 0 && *myEStop > 0 ) 
		{
			myDesired.setRotVel(0);
			myTargetTh = 0;
			*myEStop = 0;
			printf(" e");
		}
		else if ( myTargetTh != 0 ) 
		{
			range = myTargetTh - myRobot->getTh(); 
			speed = range * mySpeedRatio;
			myDesired.setRotVel( speed );
			printf(" t");
		}
		else if ( *myOnlyTurn == 0 && *myHowMany > 0 )
		{
			range = myRobot->checkRangeDevicesCurrentPolar(-25, 25) - *myStopDistance;
			speed = range * mySpeedRatio; 
			if ( speed > myMaxSpeed ) speed = myMaxSpeed;
			myDesired.setVel(speed);
			//*myHowMany = 0;
			printf(" a");
		}
	    return &myDesired;
  }
	
virtual void setRobot(ArRobot *robot)	
{
	ArAction::setRobot(robot);
	myRobot = robot;
	mySonar = robot->findRangeDevice("sonar");
	//myLaser = robot->findRangeDevice("laser");
	if (robot == NULL)
	{
		ArLog::log(ArLog::Terse, "ActionApproach: (Warning) I found no sonar, deactivating.");
		deactivate();
	}
}
};

void toggleAction(ArAction* action)
{
  if(action->isActive()) {
    action->deactivate();
    printf("\n %s is now deactivated.", action->getName());
  }
  else {
    action->activate();
    printf("\n %s is now activated.", action->getName());
  }
}






class ActionStop : public ArAction
{
public:
  ActionStop(double stopDistance);
  virtual ~ActionStop(void) {};
  virtual ArActionDesired *fire(ArActionDesired currentDesired);
  virtual void setRobot(ArRobot *robot);
protected:
  ArRangeDevice *mySonar;
  ArActionDesired myDesired;

  double myMaxSpeed;
  double myStopDistance;
};


class ActionTurn : public ArAction
{
public:
  ActionTurn(double turnThreshold, double turnAmount);
  virtual ~ActionTurn(void) {};
  virtual ArActionDesired *fire(ArActionDesired currentDesired);
  virtual void setRobot(ArRobot *robot);
protected:
  ArRangeDevice *mySonar;
  ArActionDesired myDesired;
  double myTurnThreshold;
  double myTurnAmount;
  int myTurning; // -1 == left, 1 == right, 0 == none
};

ActionStop::ActionStop(double stopDistance) :
  ArAction("Stop")
{
  mySonar = NULL;
  //myMaxSpeed = maxSpeed;
  myStopDistance = stopDistance;
  //setNextArgument(ArArg("maximum speed", &myMaxSpeed, "Maximum speed to go."));
  setNextArgument(ArArg("stop distance", &myStopDistance, "Distance at which to stop."));
}

void ActionStop::setRobot(ArRobot *robot)
{
  ArAction::setRobot(robot);
  mySonar = robot->findRangeDevice("sonar");
  if (robot == NULL)
    {
      ArLog::log(ArLog::Terse, "actionExample: ActionStop: Warning: I found no sonar, deactivating.");
      deactivate();
    }
}

ArActionDesired *ActionStop::fire(ArActionDesired currentDesired)
{
  double range;

  myDesired.reset();
  if (mySonar == NULL) {  deactivate();  return NULL; }

  range = mySonar->currentReadingPolar(-25, 25);
  if (range <= myStopDistance) { myDesired.setVel(0); }

  return &myDesired;
}


ActionTurn::ActionTurn(double turnThreshold, double turnAmount) :
  ArAction("Turn")
{
  myTurnThreshold = turnThreshold;
  myTurnAmount = turnAmount;
  setNextArgument(ArArg("turn threshold (mm)", &myTurnThreshold, "The number of mm away from obstacle to begin turnning."));
  setNextArgument(ArArg("turn amount (deg)", &myTurnAmount, "The number of degress to turn if turning."));
  myTurning = 0;
}

void ActionTurn::setRobot(ArRobot *robot)
{
  ArAction::setRobot(robot);
  mySonar = robot->findRangeDevice("sonar");
  if (mySonar == NULL)
  {
    ArLog::log(ArLog::Terse, "actionExample: ActionTurn: Warning: I found no sonar, deactivating.");
    deactivate(); 
  }
}

ArActionDesired *ActionTurn::fire(ArActionDesired currentDesired)
{
  double leftRange, rightRange;
  myDesired.reset();
  if (mySonar == NULL) { deactivate(); return NULL; }

  leftRange = (mySonar->currentReadingPolar(0, 100) - myRobot->getRobotRadius());
  rightRange = (mySonar->currentReadingPolar(-100, 0) - myRobot->getRobotRadius());

  // if neither left nor right range is within the turn threshold,
  // reset the turning variable and don't turn
  if (leftRange > myTurnThreshold && rightRange > myTurnThreshold)
  {
    myTurning = 0;
    myDesired.setDeltaHeading(0);
  }
  // if we're already turning some direction, keep turning that direction
  else if (myTurning)
  {
    myDesired.setDeltaHeading(myTurnAmount * myTurning);
  }
  // if we're not turning already, but need to, and left is closer, turn right
  // and set the turning variable so we turn the same direction for as long as
  // we need to
  else if (leftRange < rightRange)
  {
    myTurning = -1;
    myDesired.setDeltaHeading(myTurnAmount * myTurning);
  }
  // if we're not turning already, but need to, and right is closer, turn left
  // and set the turning variable so we turn the same direction for as long as
  // we need to
  else 
  {
    myTurning = 1;
    myDesired.setDeltaHeading(myTurnAmount * myTurning);
  }
  // return a pointer to the actionDesired, so resolver knows what to do
  return &myDesired;
}

