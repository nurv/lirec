//----------------------------------------------
// Heriot-Watt University
// MACS 
// www.lirec.eu
// author: Amol Deshmukh
// Date: 10/03/2010
//-----------------------------------------------

#define ARIA 0
#if ARIA
#include "Aria.h"
#else
typedef void* ArRobot;
#endif
#include <Samgar.h>

/*
 * This is a connection handler class, to demonstrate how to run code in
 * response to events such as the program connecting an disconnecting
 * from the robot.
 */
class ConnHandler
{
public:
  // Constructor
  ConnHandler(ArRobot *robot);
  // Destructor, its just empty
  ~ConnHandler(void) {}
  // to be called if the connection was made
  void connected(void);
  // to call if the connection failed
  void connFail(void);
  // to be called if the connection was lost
  void disconnected(void);
protected:
#if ARIA
  // robot pointer
  ArRobot *myRobot;
  // the functor callbacks
  ArFunctorC<ConnHandler> myConnectedCB;
  ArFunctorC<ConnHandler> myConnFailCB;
  ArFunctorC<ConnHandler> myDisconnectedCB;
#endif
};

ConnHandler::ConnHandler(ArRobot *robot) 
#if ARIA
:
  myConnectedCB(this, &ConnHandler::connected),  
  myConnFailCB(this, &ConnHandler::connFail),
  myDisconnectedCB(this, &ConnHandler::disconnected)
#endif

{
#if ARIA
  myRobot = robot;
  myRobot->addConnectCB(&myConnectedCB, ArListPos::FIRST);
  myRobot->addFailedConnectCB(&myConnFailCB, ArListPos::FIRST);
  myRobot->addDisconnectNormallyCB(&myDisconnectedCB, ArListPos::FIRST);
  myRobot->addDisconnectOnErrorCB(&myDisconnectedCB, ArListPos::FIRST);
#endif
}

// just exit if the connection failed
void ConnHandler::connFail(void)
{
  printf("directMotionDemo connection handler: Failed to connect.\n");
#if ARIA
  myRobot->stopRunning();
  Aria::shutdown();
#endif
  return;
}

// turn on motors, and off sonar, and off amigobot sounds, when connected
void ConnHandler::connected(void)
{
  printf("directMotionDemo connection handler: Connected\n");
#if ARIA
  myRobot->comInt(ArCommands::SONAR, 0);
  myRobot->comInt(ArCommands::ENABLE, 1);
  myRobot->comInt(ArCommands::SOUNDTOG, 1);
#endif
}

// lost connection, so just exit
void ConnHandler::disconnected(void)
{
  printf("directMotionDemo connection handler: Lost connection, exiting program.\n");
  exit(0);
}

Bottle bot; 


int main(int argc, char** argv)
{
  //setting yarp connection
  Network yarp;
  Samgar::SamgarModule IntReciver("Move","MoveRobot","Motion",Samgar::ModeInterupt); 
  IntReciver.AddPortS("In"); //adding in port for recieving motion commands from CMION
  int myint = 0;
  //Please not there is no obstacle avoidance in this program so be careful

#if ARIA
  //Aria configuration
  Aria::init();
  
  ArArgumentParser argParser(&argc, argv);

  ArSimpleConnector con(&argParser);

  argParser.loadDefaultArguments();

  ArRobot robot;

  // the connection handler from class
  ConnHandler ch(&robot);

  if(!Aria::parseArgs())
    {
      Aria::logOptions();
      Aria::shutdown();
      return 1;
    }

  if(!con.connectRobot(&robot))
    {
      ArLog::log(ArLog::Normal, "MotionExample: Could not connect to the robot. Exiting.");
      return 1;
    }

  yarp::os::Time::delay(2);
  ArLog::log(ArLog::Normal, "MotionExample: Connected.");

  
  robot.setAbsoluteMaxRotVel(10);
  robot.setAbsoluteMaxTransVel(200);
  robot.runAsync(false);

#endif

  while( 1 )
  {
      if (IntReciver.getCurrentState() == Samgar::StateRunning)
      {
      //robot.lock();

          if(IntReciver.GetBottleData("In",&bot)==true)
          {
              int vel = bot.get(0).asInt();
              double turn = bot.get(1).asInt()/5; //divide by 5 to avoid large turning angle
              printf("data %s \n",bot.toString().c_str());
              std::cout<< "turn " << turn << std::endl;
#if ARIA
              robot.setVel(vel); //set velocity
              robot.setDeltaHeading(turn);
#endif
          }
          else
          {
              // this will never be printed proving the module sleeps after each new input
              //printf("no data \n");
              //robot.setVel(0);
              //robot.setDeltaHeading(0);
          }
          // Make sure you unlock before any sleep() call or any other
          // code that will take some time; if the robot remains locked
          // during that time, then ArRobot's background thread will be
          // blocked and unable to communicate with the robot, call
          // tasks, etc.
		  
          //robot.unlock();

          // in an interrupt module this function call has added
          // significance, the primary being the true or false whether
          // it has accomplished its task,  and secondly a variable
          // which could be how well it has acheived the task. This data
          // is sent on to the GUI so the success rate of the modules
          // can be deduced

      }
      IntReciver.SucceedFail(true,myint);

      //    bot.clear();
      //    yarp::os::Time::delay(0.2);
    }

#if ARIA
  robot.stopRunning();

  // wait for the thread to stop
  robot.waitForRunExit();

  Aria::shutdown();
#endif
  return 0;
}

