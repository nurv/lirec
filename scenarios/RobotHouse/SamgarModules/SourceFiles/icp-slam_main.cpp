/* +---------------------------------------------------------------------------+
   |          The Mobile Robot Programming Toolkit (MRPT) C++ library          |
   |                                                                           |
   |                   http://mrpt.sourceforge.net/                            |
   |                                                                           |
   |   Copyright (C) 2005-2009  University of Malaga                           |
   |                                                                           |
   |    This software was written by the Machine Perception and Intelligent    |
   |      Robotics Lab, University of Malaga (Spain).                          |
   |    Contact: Jose-Luis Blanco  <jlblanco@ctima.uma.es>                     |
   |                                                                           |
   |  This file is part of the MRPT project.                                   |
   |                                                                           |
   |     MRPT is free software: you can redistribute it and/or modify          |
   |     it under the terms of the GNU General Public License as published by  |
   |     the Free Software Foundation, either version 3 of the License, or     |
   |     (at your option) any later version.                                   |
   |                                                                           |
   |   MRPT is distributed in the hope that it will be useful,                 |
   |     but WITHOUT ANY WARRANTY; without even the implied warranty of        |
   |     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         |
   |     GNU General Public License for more details.                          |
   |                                                                           |
   |     You should have received a copy of the GNU General Public License     |
   |     along with MRPT.  If not, see <http://www.gnu.org/licenses/>.         |
   |                                                                           |
   +---------------------------------------------------------------------------+ */

/*---------------------------------------------------------------
	APPLICATION: ICP-based SLAM
	FILE: icp-slam_main.cpp
	AUTHOR: Jose Luis Blanco Claraco <jlblanco@ctima.uma.es>

	See README.txt for instructions.
  ---------------------------------------------------------------*/

#include <mrpt/core.h>

using namespace mrpt;
using namespace mrpt::slam;
using namespace mrpt::opengl;
using namespace mrpt::gui;
using namespace mrpt::system;
using namespace mrpt::math;
using namespace mrpt::utils;
using namespace std;

/*****************************************************
			Config params
 *****************************************************/
std::string				INI_FILENAME;
std::string				RAWLOG_FILE;
unsigned int			rawlog_offset;
std::string				OUT_DIR_STD;
const char				*OUT_DIR;
int						LOG_FREQUENCY;
bool					SAVE_POSE_LOG;
bool					SAVE_3D_SCENE;
bool					CAMERA_3DSCENE_FOLLOWS_ROBOT;

bool 					SHOW_PROGRESS_3D_REAL_TIME = false;
int						SHOW_PROGRESS_3D_REAL_TIME_DELAY_MS = 0;
bool					matchAgainstTheGrid;

TSetOfMetricMapInitializers  metricMapsOpts;
float						insertionLinDistance;
float						insertionAngDistance;

CICP::TConfigParams			icpOptions;


// Forward declaration.
void MapBuilding_ICP();

// ------------------------------------------------------
//						MAIN
// ------------------------------------------------------
int main(int argc, char **argv)
{
	try
	{
		bool showHelp    = argc>1 && !os::_strcmp(argv[1],"--help");
		bool showVersion = argc>1 && !os::_strcmp(argv[1],"--version");

		printf(" icp-slam version 0.1 - Part of the MRPT\n");
		printf(" MRPT C++ Library: %s - BUILD DATE %s\n", MRPT_getVersion().c_str(), MRPT_getCompilationDate().c_str());

		if (showVersion)
			return 0;	// Program end

		printf("-------------------------------------------------------------------\n");

		// Process arguments:
		if (argc<2 || showHelp )
		{
			printf("Usage: %s <config_file.ini>\n\n",argv[0]);
			if (!showHelp)
			{
				mrpt::system::pause();
				return -1;
			}
			else	return 0;
		}

		INI_FILENAME = std::string( argv[1] );
		ASSERT_(fileExists(INI_FILENAME));

		CConfigFile		iniFile( INI_FILENAME );

		// ------------------------------------------
		//			Load config from file:
		// ------------------------------------------
		RAWLOG_FILE			 = iniFile.read_string("MappingApplication","rawlog_file","",  /*Force existence:*/ true);
		rawlog_offset		 = iniFile.read_int("MappingApplication","rawlog_offset",0,  /*Force existence:*/ true);
		OUT_DIR_STD			 = iniFile.read_string("MappingApplication","logOutput_dir","log_out",  /*Force existence:*/ true);
		LOG_FREQUENCY		 = iniFile.read_int("MappingApplication","LOG_FREQUENCY",5,  /*Force existence:*/ true);
		SAVE_POSE_LOG		 = iniFile.read_bool("MappingApplication","SAVE_POSE_LOG", false,  /*Force existence:*/ true);
		SAVE_3D_SCENE        = iniFile.read_bool("MappingApplication","SAVE_3D_SCENE", false,  /*Force existence:*/ true);
		CAMERA_3DSCENE_FOLLOWS_ROBOT = iniFile.read_bool("MappingApplication","CAMERA_3DSCENE_FOLLOWS_ROBOT", true,  /*Force existence:*/ true);

		MRPT_LOAD_CONFIG_VAR( SHOW_PROGRESS_3D_REAL_TIME, bool,  iniFile, "MappingApplication");
		MRPT_LOAD_CONFIG_VAR( SHOW_PROGRESS_3D_REAL_TIME_DELAY_MS, int, iniFile, "MappingApplication");

		OUT_DIR = OUT_DIR_STD.c_str();

		MRPT_LOAD_CONFIG_VAR( insertionLinDistance, 	float,		iniFile, "MappingApplication");
		MRPT_LOAD_CONFIG_VAR_DEGREES( insertionAngDistance,		iniFile, "MappingApplication");

		metricMapsOpts.loadFromConfigFile(iniFile, "MappingApplication");
		icpOptions.loadFromConfigFile(iniFile, "ICP");
		matchAgainstTheGrid = iniFile.read_bool("MappingApplication","matchAgainstTheGrid", true );

		// Print params:
		printf("Running with the following parameters:\n");
		printf(" RAWLOG file:'%s'\n", RAWLOG_FILE.c_str());
		printf(" Output directory:\t\t\t'%s'\n",OUT_DIR);
		printf(" matchAgainstTheGrid:\t\t\t%c\n", matchAgainstTheGrid ? 'Y':'N');
		printf(" Log record freq:\t\t\t%u\n",LOG_FREQUENCY);
		printf("  SAVE_3D_SCENE:\t\t\t%c\n", SAVE_3D_SCENE ? 'Y':'N');
		printf("  SAVE_POSE_LOG:\t\t\t%c\n", SAVE_POSE_LOG ? 'Y':'N');
		printf("  CAMERA_3DSCENE_FOLLOWS_ROBOT:\t%c\n",CAMERA_3DSCENE_FOLLOWS_ROBOT ? 'Y':'N');

		printf("\n");

		metricMapsOpts.dumpToConsole();
		icpOptions.dumpToConsole();



		// Checks:
		ASSERT_(RAWLOG_FILE.size()>0);
		ASSERT_(fileExists(RAWLOG_FILE));

		// Run:
		MapBuilding_ICP();

		//pause();
		return 0;
	} catch (std::exception &e)
	{
		std::cerr << e.what() << std::endl << "Program finished for an exception!!" << std::endl;
		mrpt::system::pause();
		return -1;
	}
	catch (...)
	{
		std::cerr << "Untyped exception!!" << std::endl;
		mrpt::system::pause();
		return -1;
	}
}



// ------------------------------------------------------
//				MapBuilding_ICP
// ------------------------------------------------------
void MapBuilding_ICP()
{
	MRPT_TRY_START

	CTicTac								tictac,tictacGlobal,tictac_JH;
	int									step = 0;
	std::string							str;
	FILE								*f_log,*f_path,*f_pathOdo;
	CSensFrameProbSequence				finalMap;
	float								t_exec;
	COccupancyGridMap2D::TEntropyInfo	entropy;

	char								strFil[1000];

	size_t						rawlogEntry = 0;
	CFileGZInputStream					rawlogFile( RAWLOG_FILE.c_str() );

	// ---------------------------------
	//		Constructor
	// ---------------------------------
	CMetricMapBuilderICP mapBuilder(
		&metricMapsOpts,
		insertionLinDistance,
		insertionAngDistance,
		&icpOptions
		);

	mapBuilder.ICP_options.matchAgainstTheGrid = matchAgainstTheGrid;

	// ---------------------------------
	//   CMetricMapBuilder::TOptions
	// ---------------------------------
	mapBuilder.options.verbose					= true;
	mapBuilder.options.enableMapUpdating		= true;
    mapBuilder.options.debugForceInsertion		= false;
	mapBuilder.options.insertImagesAlways		= false;

//	mrpt::registerAllClasses();

	// Prepare output directory:
	// --------------------------------
	deleteFilesInDirectory(OUT_DIR);
	createDirectory(OUT_DIR);

	// Open log files:
	// ----------------------------------
	os::sprintf(strFil,1000,"%s/log_times.txt",OUT_DIR);
	f_log=os::fopen(strFil,"wt");

	os::sprintf(strFil,1000,"%s/log_estimated_path.txt",OUT_DIR);
	f_path=os::fopen(strFil,"wt");

	os::sprintf(strFil,1000,"%s/log_odometry_path.txt",OUT_DIR);
	f_pathOdo=os::fopen(strFil,"wt");


	ASSERT_(f_log!=NULL);
	ASSERT_(f_path!=NULL);
	ASSERT_(f_pathOdo!=NULL);


	// Create 3D window if requested:
	CDisplayWindow3D	*win3D = NULL;
#if MRPT_HAS_WXWIDGETS
	if (SHOW_PROGRESS_3D_REAL_TIME)
	{
		win3D = new CDisplayWindow3D("ICP-SLAM @ MRPT C++ Library (C) 2004-2008", 600, 500);
		win3D->setCameraZoom(20);
		win3D->setCameraAzimuthDeg(-45);
	}
#endif

	// ----------------------------------------------------------
	//						Map Building
	// ----------------------------------------------------------
	CActionCollectionPtr	action;
	CSensoryFramePtr		observations;
	CPose2D					odoPose(0,0,0);

	tictacGlobal.Tic();
	for (;;)
	{
		if (os::kbhit())
		{
			char c = os::getch();
			if (c==27)
				break;
		}

		// Load action/observation pair from the rawlog:
		// --------------------------------------------------
		if (! CRawlog::readActionObservationPair( rawlogFile, action, observations, rawlogEntry) )
			break; // file EOF


		if (rawlogEntry>=rawlog_offset)
		{
			// Update odometry:
			{
				CActionRobotMovement2DPtr act= action->getBestMovementEstimation();
				if (act)
					odoPose = odoPose + act->poseChange->getEstimatedPose();
			}

			// Execute:
			// ----------------------------------------
			tictac.Tic();
				mapBuilder.processActionObservation( *action, *observations );
			t_exec = tictac.Tac();
			printf("Map building executed in %.03fms\n", 1000.0f*t_exec );


			// Info log:
			// -----------
			os::fprintf(f_log,"%f %i\n",1000.0f*t_exec,mapBuilder.getCurrentlyBuiltMapSize() );

			const CMultiMetricMap* mostLikMap =  mapBuilder.getCurrentlyBuiltMetricMap();

			if (0==(step % LOG_FREQUENCY))
			{
				// Pose log:
				// -------------
				if (SAVE_POSE_LOG)
				{
					printf("Saving pose log information...");
					mapBuilder.getCurrentPoseEstimation()->saveToTextFile( format("%s/mapbuild_posepdf_%03u.txt",OUT_DIR,step) );
					printf("Ok\n");
				}
			}

			// Save a 3D scene view of the mapping process:
			if (0==(step % LOG_FREQUENCY) || (SAVE_3D_SCENE || win3D!=NULL))
			{
                CPose2D				robotPose( mapBuilder.getCurrentPoseEstimation()->getEstimatedPose() );
				COpenGLScenePtr		scene = COpenGLScenePtr( new COpenGLScene() );

                COpenGLViewportPtr view=scene->getViewport("main");
                ASSERT_(view);

                COpenGLViewportPtr view_map = scene->createViewport("mini-map");
                view_map->setBorderSize(2);
                view_map->setViewportPosition(0.01,0.01,0.35,0.35);
                view_map->setTransparent(false);

				{
					mrpt::opengl::CCamera &cam = view_map->getCamera();
					cam.setAzimuthDegrees(-90);
					cam.setElevationDegrees(90);
					cam.setPointingAt(robotPose.x(),robotPose.y(),robotPose.z());
					cam.setZoomDistance(20);
					cam.setOrthogonal();
				}

				// The ground:
				mrpt::opengl::CGridPlaneXYPtr groundPlane = mrpt::opengl::CGridPlaneXY::Create(-200,200,-200,200,0,5);
				groundPlane->setColor(0.4,0.4,0.4);
				view->insert( groundPlane );
				view_map->insert( CRenderizablePtr( groundPlane) ); // A copy

				// The camera pointing to the current robot pose:
				if (CAMERA_3DSCENE_FOLLOWS_ROBOT)
				{
				    scene->enableFollowCamera(true);

					mrpt::opengl::CCamera &cam = view_map->getCamera();
					cam.setAzimuthDegrees(-45);
					cam.setElevationDegrees(45);
					cam.setPointingAt(robotPose.x(),robotPose.y(),robotPose.z());
				}

				// The maps:
				{
					opengl::CSetOfObjectsPtr obj = opengl::CSetOfObjects::Create();
					mostLikMap->getAs3DObject( obj );
					view->insert(obj);

					// Only the point map:
					opengl::CSetOfObjectsPtr ptsMap = opengl::CSetOfObjects::Create();
					if (mostLikMap->m_pointsMaps.size())
					{
                        mostLikMap->m_pointsMaps[0]->getAs3DObject(ptsMap);
                        view_map->insert( ptsMap );
					}
				}

				// Draw the robot path:
				CPose3DPDFPtr posePDF =  mapBuilder.getCurrentPoseEstimation();
				CPose3D  curRobotPose( posePDF->getEstimatedPose() );
				{
					opengl::CSetOfObjectsPtr obj = opengl::stock_objects::RobotPioneer();
					obj->setPose( curRobotPose );
					view->insert(obj);
				}
				{
					opengl::CSetOfObjectsPtr obj = opengl::stock_objects::RobotPioneer();
					obj->setPose( curRobotPose );
					view_map->insert( obj );
				}

				// Save as file:
				if (0==(step % LOG_FREQUENCY) && SAVE_3D_SCENE)
				{
					CFileGZOutputStream	f( format( "%s/buildingmap_%05u.3Dscene",OUT_DIR,step ));
					f << *scene;
				}

				// Show 3D?
				if (win3D)
				{
					opengl::COpenGLScenePtr &ptrScene = win3D->get3DSceneAndLock();
					ptrScene = scene;

					win3D->unlockAccess3DScene();

					// Move camera:
					win3D->setCameraPointingToPoint( curRobotPose.x(), curRobotPose.y(), curRobotPose.z() );

					// Update:
					win3D->forceRepaint();

					sleep( SHOW_PROGRESS_3D_REAL_TIME_DELAY_MS );
				}
			}


			// Save the memory usage:
			// ------------------------------------------------------------------
			{
				printf("Saving memory usage...");
				unsigned long	memUsage = getMemoryUsage();
				FILE		*f=os::fopen( format("%s/log_MemoryUsage.txt",OUT_DIR).c_str() ,"at");
				if (f)
				{
					os::fprintf(f,"%u\t%lu\n",step,memUsage);
					os::fclose(f);
				}
				printf("Ok! (%.04fMb)\n", ((float)memUsage)/(1024*1024) );
			}

			// Save the robot estimated pose for each step:
			os::fprintf(f_path,"%i %f %f %f\n",
				step,
				mapBuilder.getCurrentPoseEstimation()->getEstimatedPose().x(),
				mapBuilder.getCurrentPoseEstimation()->getEstimatedPose().y(),
				mapBuilder.getCurrentPoseEstimation()->getEstimatedPose().yaw() );


			os::fprintf(f_pathOdo,"%i %f %f %f\n",step,odoPose.x(),odoPose.y(),odoPose.phi());

		} // end of if "rawlog_offset"...

		step++;
		printf("\n---------------- STEP %u | RAWLOG ENTRY %u ----------------\n",step, (unsigned)rawlogEntry);

		// Free memory:
		action.clear_unique();
		observations.clear_unique();
	};

	printf("\n---------------- END!! (total time: %.03f sec) ----------------\n",tictacGlobal.Tac());

	os::fclose(f_log);
	os::fclose(f_path);
	os::fclose(f_pathOdo);

	// Save map:
	mapBuilder.getCurrentlyBuiltMap(finalMap);

	str = format("%s/_finalmap_.simplemap",OUT_DIR);
	printf("Dumping final map in binary format to: %s\n", str.c_str() );
	CFileGZOutputStream		filOut(str);
	filOut << finalMap;


	CMultiMetricMap  *finalPointsMap = mapBuilder.getCurrentlyBuiltMetricMap();
	str = format("%s/_finalmaps_.txt",OUT_DIR);
	printf("Dumping final metric maps to %s_XXX\n", str.c_str() );
	finalPointsMap->saveMetricMapRepresentationToFile( str );

	if (win3D)
	{
		mrpt::system::pause();
		delete win3D;
	}

	MRPT_TRY_END
}
