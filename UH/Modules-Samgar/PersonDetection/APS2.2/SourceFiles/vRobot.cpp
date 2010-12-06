
#include "Aria.h"
#include <ariaUtil.h>
//#include "KeyFobReader.h"
//#include "ActionApproach.h"
#include "ActionApproach.h"
#include "SamModules.h"

int main(int argc, char** argv)
{

	//*********** Yarp, SAMGAR init

	samDataIn robotDataIn("/vRobot");
	robotDataIn.PamInit("PDPoseIn");

	double myAngle = 0;
	int myHowMany = 0;
	int myOnlyTurn = 0;
	int myEStop = 0;
	double defaultHRP = 570;
	double currentHRP = defaultHRP;
	double finalHRP = defaultHRP;

	Aria::init();
	ArArgumentParser parser(&argc, argv);

	parser.loadDefaultArguments();
	parser.addDefaultArgument(" -rp com4 ");

	ArRobot robot;
	ArRobotConnector robotConnector(&parser, &robot);
	ArAnalogGyro gyro(&robot);

	if (!robotConnector.connectRobot())
	{
		if (!parser.checkHelpAndWarnUnparsed())
		{
			ArLog::log(ArLog::Terse, "Could not connect to robot, will not have parameter file so options displayed later may not include everything");
		}
		else
		{
			ArLog::log(ArLog::Terse, "Error, could not connect to robot.");
			Aria::logOptions();
			Aria::exit(1);
		}
	}

	ArLaserConnector laserConnector(&parser, &robot, &robotConnector);
	ArCompassConnector compassConnector(&parser);
	ArSonarDevice sonarDev;

	if (!Aria::parseArgs() || !parser.checkHelpAndWarnUnparsed())
	{    
		Aria::logOptions();
		exit(1);
	}

	ArKeyHandler keyHandler;
	Aria::setKeyHandler(&keyHandler);
	printf("You may press escape to exit\n");

	robot.addRangeDevice(&sonarDev);
	if (!laserConnector.connectLasers(false, false, true))
	{
		printf("Could not connect to lasers... exiting\n");
		Aria::exit(2);
	}

	//ArTCM2 *compass = compassConnector.create(&robot);
	//if(compass && !compass->blockingConnect()) {compass = NULL;}
  
	//ArUtil::sleep(1000);
	//robot.lock();

	// now add all the modes for this demo
	// these classes are defined in ArModes.cpp in ARIA's source code.
	//ArModeLaser laser(&robot, "laser", 'l', 'L');
	//ArModeTeleop teleop(&robot, "teleop", 't', 'T');
	//ArModeUnguardedTeleop unguardedTeleop(&robot, "unguarded teleop", 'u', 'U');
	//ArModeWander wander(&robot, "wander", 'w', 'W');
	//ArModeGripper gripper(&robot, "gripper", 'g', 'G');
	//ArModeCamera camera(&robot, "camera", 'c', 'C');
	//ArModeSonar sonar(&robot, "sonar", 's', 'S');
	//ArModeBumps bumps(&robot, "bumps", 'b', 'B');
	//ArModePosition position(&robot, "position", 'p', 'P', &gyro);
	//ArModeIO io(&robot, "io", 'i', 'I');
	//ArModeActs actsMode(&robot, "acts", 'a', 'A');
	//ArModeCommand command(&robot, "command", 'd', 'D');
	//ArModeTCM2 tcm2(&robot, "tcm2", 'm', 'M', compass);


	// activate the default mode
	//teleop.activate();
	//sonar.activate();

	// Collision avoidance actions at higher priority
	ArActionBumpers bumpers;
	ActionStop stop(250);
	robot.addAction(&bumpers, 100);
	robot.addAction(&stop, 95);

	// the joydrive action (drive from joystick)
	ArActionJoydrive joydriveAct("joydrive", 200, 15);
	robot.addAction(&joydriveAct, 35);
	joydriveAct.deactivate();

	// the keydrive action (drive from joystick)
	ArActionKeydrive keydriveAct("keydrive", 200, 15);
	robot.addAction(&keydriveAct, 30);
	keydriveAct.deactivate();

	// Approach action at lower priority
	ActionApproach approachAct(&currentHRP, &myHowMany, &myAngle, &myOnlyTurn, &myEStop);
	robot.addAction(&approachAct, 55);
	approachAct.deactivate();

	// Use the key to activate/deactivate Actions
	keyHandler.addKeyHandler('a', new ArGlobalFunctor1<ArAction*>(&toggleAction, &approachAct));
	keyHandler.addKeyHandler('j', new ArGlobalFunctor1<ArAction*>(&toggleAction, &joydriveAct));
	keyHandler.addKeyHandler('k', new ArGlobalFunctor1<ArAction*>(&toggleAction, &keydriveAct));
	keyHandler.addKeyHandler('A', new ArGlobalFunctor1<ArAction*>(&toggleAction, &approachAct));
	keyHandler.addKeyHandler('J', new ArGlobalFunctor1<ArAction*>(&toggleAction, &joydriveAct));
	keyHandler.addKeyHandler('K', new ArGlobalFunctor1<ArAction*>(&toggleAction, &keydriveAct));
	robot.attachKeyHandler(&keyHandler);

	robot.setCycleWarningTime(1000);
	robot.setCycleChained(false);
	robot.setCycleTime(100);
	robot.runAsync(true);
	robot.comInt(ArCommands::ENABLE, 1);
	//robot.unlock();

	int n = 0, ic = 0;

	while (Aria::getRunning())
	{
		//printf(" x %.3lf ", robot.getTh() );
		printf("\n Robot %d ", ic++);
		double data[15];
		robotDataIn.PamIter(&n, data);
		if ( n>0 )
		{
			myAngle = data[1];
			myHowMany = (data[0]>0.70)?1:0;
			printf("  %d   %.0lf ", myHowMany, myAngle );
		}
		Sleep(400);
	}
  robot.waitForRunExit();
  Aria::shutdown();
  return 0;
}

