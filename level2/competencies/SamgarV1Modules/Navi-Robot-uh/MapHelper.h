#include <mrpt/core.h>
#include <iostream>
#include <fstream>
#include <conio.h>
#include <windows.h>

#include "SamgarMainClass.h"

using namespace mrpt;
using namespace mrpt::slam;
using namespace mrpt::opengl;
using namespace mrpt::gui;
using namespace mrpt::system;
using namespace mrpt::math;
using namespace mrpt::utils;
using namespace std;
using namespace yarp;

using namespace mrpt::synch;
using namespace mrpt::random;

/* We are using multhreading

These delays are the only thing stopping the threads from trying to take over a core, if you set them to zero you will get good speeds
but they will try to occupy three/or less cores continuasly

*/

#define MainWait 0.05//0.25 worked well but want laser quicker // this is the main map update and localisation, you want this pretty fast
#define GuiWait  0.35 // this is ONLY for the user to see whats going on, helpfull but not important
#define PlanWait 0.05 // this is how often when it has a target to replan, quicker better but it will slow down near obsticles so this being high should cause crashes

#define validrangesize 682 // number of laser readings in one scan

#ifdef CreateVars
#define EXTERN
#else
#define EXTERN extern
#endif



EXTERN CMetricMapBuilderICP			*mapBuilder;
EXTERN TSetOfMetricMapInitializers		metricMapsOpts;
EXTERN CICP::TConfigParams				icpOptions;

#ifdef CreateVars
CConfigFile						iniFile( "icp-slam_demo.ini"); // open the file
mrpt::gui::CDisplayWindow		Pathwin("Computed path");
CImage		img;
#else
extern CConfigFile						iniFile;
#endif

EXTERN COccupancyGridMap2D GuiGridmap;
EXTERN CPose3D GuicurRobotPose;

EXTERN COccupancyGridMap2D PathGridmap;
EXTERN CPose3D PathcurRobotPose;

EXTERN float distoobjective;


EXTERN CActionRobotMovement2D ::TMotionModelOptions MyOptions;
EXTERN CActionRobotMovement2D	MyRobotAction;
EXTERN CActionCollectionPtr	action;

EXTERN static CSensoryFramePtr		   observations;
EXTERN static CObservation2DRangeScanPtr scan ;
EXTERN static CObservationOdometryPtr GlobalOdo;

EXTERN static CSimpleMapPtr MyMap;
//SamgarModule *Mapper;

EXTERN CCriticalSection  csCounter;

EXTERN bool HasGotInitialPosistion = false;//true;//false;// should be false to wait for odo

EXTERN bool obsupdate;
EXTERN bool odoupdate;

EXTERN bool OnEXIT;

EXTERN double Speed,ErrorBox;

EXTERN bool    HaveTarget,JustReachedTarget;
EXTERN CPose2D PrimayTarget;
EXTERN CPose2D PrimayTarget2;

#define RobotRad 0.45
#define RobotRadError 0.20
#define ShadowPathLength 20
#define Iterationsbeforegiveuppath 50

EXTERN CPathPlanningCircularRobot	pathPlanning;

//EXTERN 	CThreadSafeVariable thereisnopath = 	CThreadSafeVariable();

EXTERN  CThreadSafeVariable<bool>   Thereisnopath;




EXTERN TColor Blue;
EXTERN TColor Red;
EXTERN TColor Green;
EXTERN TColor OffSetColor;

EXTERN Bottle BotVelAngError;

EXTERN std::deque<poses::TPoint2D>		PathWhereImGoing;
EXTERN std::deque<poses::TPoint2D>		PathWhereImGoing2;
EXTERN std::deque<poses::TPoint2D>		PathWhereImGoingMap;

EXTERN std::deque<poses::TPoint2D>		PathWhereIveBeen;

void LoadMapBuilder(void);
void LoadMovementParams(void);
void LoadLaserParams(void);
CPose2D GetDiffOdo(CPose2D);
void GetLatestOdoData(SamgarModule *tempmodule);
void GetLatestLaserData(SamgarModule *tempmodule);
void GetTarget(SamgarModule *tempmodule);

void ShowMap(void);
void ClearProcessedData(void);
void LoadColors(void);
void DrawPointy(CPose2D left,CPose2D right,CPose2D top);
void DrawMyLine(double x1,double y1,double x2,double y2,TColor color);
void DrawMyTraveledPath(CPose3D mycurrentpos);
void LoadPathPlanner(void);
void PlanPath(void);
void CalcTraveledPath(CPose3D MCP);
void DrawMyFuturePath(CPose3D mycurrentpos);
void EXIT(void);
bool HaveReachedDestination(CPose3D RobotCurrent);
void GetAngleAndSetSpeed (CPose3D RobotCurrent);