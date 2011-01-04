/** \file PlayerSamgarThread.h
 */
#include "PlayerSamgarThread.h"
#include <libplayerc++/playerc++.h>

void* Position2dThread(void * param){
  bool active=true;
  PlayerCc::PlayerClient* player;
  PlayerDriver_t * data = static_cast< PlayerDriver_t*>(param);

  // player configuration
  try
    {
      // Connect to Player server
      player = new  PlayerCc::PlayerClient(data->player_hostname, data->player_port);
    }
  catch (PlayerCc::PlayerError e)
    {
      std::cerr << e << std::endl;
      throw e;
      //return -1;
    }

  PlayerCc::Position2dProxy pp(player,data->index);
  
  while(active)
    {
      // read data from Samgar
      yarp::os::Bottle *input = data->samgarPort->read(true); // blocking read
      // analize data from samgar
      if (input->get(TYPE).asInt() == Position2d)
	{
	  // read state from Player
	  player->Read();
	  
	  // perform Samgar command
	  if (input->get(CMD).asInt() == Position2d_SET_SPEED)
	    {
	      pp.SetSpeed(input->get(2).asDouble(), input->get(3).asDouble());
	    }
	  if (input->get(CMD).asInt() == Position2d_SET_GOAL)
	    {
	      pp.GoTo(input->get(2).asDouble(),
		      input->get(3).asDouble(),
		      input->get(4).asDouble());
	    }
	  if (input->get(CMD).asInt() == Position2d_SET_ODOM)
	    {
	      pp.SetOdometry(input->get(2).asDouble(),
			     input->get(3).asDouble(),
			     input->get(4).asDouble());
	    }
	  if (input->get(CMD).asInt() == Position2d_SET_MOTOR)
	    {
	      pp.SetMotorEnable(input->get(2).asInt());
	    }
	  if (input->get(CMD).asInt() == SET_REQ)
	    {
	      if (input->get(Position2d_SPEED).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  B.clear();
		  B.addInt(Position2d);
		  B.addInt(Position2d_SPEED);
		  B.addDouble(pp.GetXSpeed());
		  B.addDouble(pp.GetYawSpeed());
		  data->samgarPort->write();
		}
	      if (input->get(Position2d_POSITION).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  B.clear();
		  B.addInt(Position2d);
		  B.addInt(Position2d_POSITION);
		  B.addDouble(pp.GetXPos());
		  B.addDouble(pp.GetYPos());
		  B.addDouble(pp.GetYaw());
		  data->samgarPort->write();
		}
	      if (input->get(Position2d_SIZE).asInt())
		{
		  player_bbox_t size = pp.GetSize();
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  B.clear();
		  B.addInt(Position2d);
		  B.addInt(Position2d_SIZE);
		  B.addDouble(size.sw);
		  B.addDouble(size.sl);
		  data->samgarPort->write();
		}
	      if (input->get(Position2d_STALL).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  B.clear();
		  B.addInt(Position2d);
		  B.addInt(Position2d_STALL);
		  B.addInt(pp.GetStall());
		  data->samgarPort->write();
		}		  
	    }
	  
	  // send data to Samgar
	  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
	  B.clear();
	  B.addInt(Position2d);
	  B.addInt(Ack);
	  data->samgarPort->write();
	    
	}
      else{
	yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
	B.clear();
	B.addInt(Position2d);
	B.addInt(Error);
	B.addString("Wrong device");
	data->samgarPort->write();
      }
    } // while(1)
  return NULL;
}


void* LocalizeThread(void * param){
  bool active=true;
  PlayerCc::PlayerClient* player;
  PlayerDriver_t * data = static_cast< PlayerDriver_t*>(param);

  // player configuration
  try
    {
      // Connect to Player server
      player = new  PlayerCc::PlayerClient(data->player_hostname, data->player_port);
    }
  catch (PlayerCc::PlayerError e)
    {
      std::cerr << e << std::endl;
      throw e;
      //return -1;
    }

  PlayerCc::LocalizeProxy pp(player,data->index);
  
  while(active)
    {
      // read data from Samgar
      yarp::os::Bottle *input = data->samgarPort->read(true); // blocking read
      // analize data from samgar
      if (input->get(TYPE).asInt() == Localize)
	{
	  // read state from Player
	  player->Read();
	  
	  // perform Samgar command
	  if (input->get(CMD).asInt() == Localize_SET_POSE)
	    {
	      double pose[3]={input->get(2).asDouble(),
			      input->get(3).asDouble(),
			      input->get(4).asDouble()};
	      double cov[3]={1,1,1};
	      pp.SetPose(pose,cov);
	    }
	  if (input->get(CMD).asInt() == SET_REQ)
	    {
	      if (input->get(Localize_MAP_INFO).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  B.clear();
		  B.addInt(Localize);
		  B.addInt(Localize_MAP_INFO);
		  B.addInt(pp.GetMapSizeX());
		  B.addInt(pp.GetMapSizeY());
		  B.addInt(pp.GetMapTileX());
		  B.addInt(pp.GetMapTileY());
		  B.addDouble(pp.GetMapScale());
		  data->samgarPort->write();
		}
	      if (input->get(Localize_HYPOTHS).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  B.clear();
		  B.addInt(Localize);
		  B.addInt(Localize_HYPOTHS);
		  int hypothsCount = pp.GetHypothCount();
		  player_localize_hypoth_t hypoth;
		  B.addInt(hypothsCount);
		  for (int i=0;i<hypothsCount; i++){
		    hypoth = pp.GetHypoth(i);
		    B.addDouble(hypoth.mean.px);
		    B.addDouble(hypoth.mean.py);
		    B.addDouble(hypoth.mean.pa);
		    B.addDouble(hypoth.alpha);
		  }
		  data->samgarPort->write();
		}
	    }
	  
	  // send data to Samgar
	  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
	  B.clear();
	  B.addInt(Localize);
	  B.addInt(Ack);
	  data->samgarPort->write();
	    
	}
      else{
	yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
	B.clear();
	B.addInt(Localize);
	B.addInt(Error);
	B.addString("Wrong device");
	data->samgarPort->write();
      }
    } // while(1)
  return NULL;
}

void* PlannerThread(void * param){
  bool active=true;
  PlayerCc::PlayerClient* player;
  PlayerDriver_t * data = static_cast< PlayerDriver_t*>(param);

  // player configuration
  try
    {
      // Connect to Player server
      player = new  PlayerCc::PlayerClient(data->player_hostname, data->player_port);
    }
  catch (PlayerCc::PlayerError e)
    {
      std::cerr << e << std::endl;
      throw e;
      //return -1;
    }

  PlayerCc::PlannerProxy pp(player,data->index);
  
  while(active)
    {
      // read data from Samgar
      yarp::os::Bottle *input = data->samgarPort->read(true); // blocking read
      // analize data from samgar
      if (input->get(TYPE).asInt() == Planner)
	{
	  // read state from Player
	  player->Read();
	  
	  // perform Samgar command
	  if (input->get(CMD).asInt() == Planner_SET_GOAL)
	    {
	      pp.SetGoalPose(input->get(2).asDouble(),
			     input->get(3).asDouble(),
			     input->get(4).asDouble());
	    }
	  if (input->get(1).asInt() == Planner_SET_ENABLE)
	    {
	      pp.SetEnable(input->get(2).asDouble());
	    }

	  if (input->get(1).asInt() == SET_REQ)
	    {
	      if (input->get(Planner_GOAL).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  B.clear();
		  B.addInt(Planner);
		  B.addInt(Planner_GOAL);
		  B.addDouble(pp.GetGx());
		  B.addDouble(pp.GetGy());
		  B.addDouble(pp.GetGa());
		  data->samgarPort->write();
		}
	      if (input->get(Planner_POSE).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  B.clear();
		  B.addInt(Planner);
		  B.addInt(Planner_POSE);
		  B.addDouble(pp.GetPx());
		  B.addDouble(pp.GetPy());
		  B.addDouble(pp.GetPa());
		  data->samgarPort->write();
		}
	      if (input->get(Planner_CURR_WAYPOINT).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  B.clear();
		  B.addInt(Planner);
		  B.addInt(Planner_CURR_WAYPOINT);
		  B.addDouble(pp.GetWx());
		  B.addDouble(pp.GetWy());
		  B.addDouble(pp.GetWa());
		  data->samgarPort->write();
		}
	      if (input->get(Planner_PATH).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  B.clear();
		  B.addInt(Planner);
		  B.addInt(Planner_PATH);
		  B.addInt(pp.GetPathValid());
		  B.addInt(pp.GetPathDone());
		  data->samgarPort->write();
		}
	      if (input->get(Planner_WAYPOINTS).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  B.clear();
		  B.addInt(Planner);
		  B.addInt(Planner_WAYPOINTS);
		  int waypointsCount = pp.GetWaypointCount();
		  player_pose_t pose;
		  B.addInt(waypointsCount);
		  for (int i=0;i<waypointsCount; i++){
		    pose = pp.GetWaypoint(i);
		    B.addDouble(pose.px);
		    B.addDouble(pose.py);
		    B.addDouble(pose.pa);
		  }
		  data->samgarPort->write();
		}
	    }
	  
	  // send data to Samgar
	  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
	  B.clear();
	  B.addInt(Planner);
	  B.addInt(Ack);
	  data->samgarPort->write();
	    
	}
      else{
	yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
	B.clear();
	B.addInt(Planner);
	B.addInt(Error);
	B.addString("Wrong device");
	data->samgarPort->write();
      }
    } // while(1)
  return NULL;
}


void* LaserThread(void * param){
  bool active=true;
  PlayerCc::PlayerClient* player;
  PlayerDriver_t * data = static_cast< PlayerDriver_t*>(param);

  // player configuration
  try
    {
      // Connect to Player server
      player = new  PlayerCc::PlayerClient(data->player_hostname, data->player_port);
    }
  catch (PlayerCc::PlayerError e)
    {
      std::cerr << e << std::endl;
      throw e;
      //return -1;
    }

  PlayerCc::LaserProxy pp(player,data->index);
  
  while(active)
    {
      // read data from Samgar
      yarp::os::Bottle *input = data->samgarPort->read(true); // blocking read
      // analize data from samgar
      if (input->get(TYPE).asInt() == Laser)
	{
	  // read state from Player
	  player->Read();
	  
	  // perform Samgar command
	  if (input->get(CMD).asInt() == Laser_SET_CONFIG)
	    {
	      pp.Configure(input->get(2).asDouble(),
			   input->get(3).asDouble(),
			   input->get(4).asDouble(),
			   input->get(5).asDouble(),
			   false);
	    }
	  if (input->get(CMD).asInt() == SET_REQ)
	    {
	      if (input->get(Laser_CONFIG).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  B.clear();
		  B.addInt(Laser);
		  B.addInt(Laser_CONFIG);
		  B.addDouble(pp.GetMinAngle());
		  B.addDouble(pp.GetMaxAngle());
		  B.addDouble(pp.GetScanRes());
		  B.addDouble(pp.GetRangeRes());
		  B.addDouble(pp.GetMaxRange());
		  data->samgarPort->write();
		}
	      if (input->get(Laser_SIZE).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  player_bbox_t geom=pp.GetSize();
		  B.clear();
		  B.addInt(Laser);
		  B.addInt(Laser_SIZE);
		  B.addDouble(geom.sw);
		  B.addDouble(geom.sl);
		  data->samgarPort->write();
		}
	      if (input->get(Laser_POSE).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  player_pose_t pose=pp.GetPose();
		  B.clear();
		  B.addInt(Laser);
		  B.addInt(Laser_POSE);
		  B.addDouble(pose.px);
		  B.addDouble(pose.py);
		  B.addDouble(pose.pa);
		  data->samgarPort->write();
		}
	      if (input->get(Laser_ROBOT_POSE).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  player_pose_t pose=pp.GetRobotPose();
		  B.clear();
		  B.addInt(Laser);
		  B.addInt(Laser_ROBOT_POSE);
		  B.addDouble(pose.px);
		  B.addDouble(pose.py);
		  B.addDouble(pose.pa);
		  data->samgarPort->write();
		}
	      if (input->get(Laser_POINTS).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  B.clear();
		  B.addInt(Laser);
		  B.addInt(Laser_POINTS);
		  int pointsCount = pp.GetCount();
		  player_point_2d_t point;
		  B.addInt(pointsCount);
		  for (int i=0;i<pointsCount; i++)
		    {
		      point = pp.GetPoint(i);
		      B.addDouble(point.px);
		      B.addDouble(point.py);
		    }
		  data->samgarPort->write();
		}
	      if (input->get(Laser_RANGES).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  B.clear();
		  B.addInt(Laser);
		  B.addInt(Laser_RANGES);
		  int pointsCount = pp.GetCount();
		  B.addInt(pointsCount);
		  for (int i=0;i<pointsCount; i++){
		    B.addDouble(pp.GetRange(i));
		    B.addDouble(pp.GetBearing(i));
		  }
		  data->samgarPort->write();
		}

	    }
	  
	  // send data to Samgar
	  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
	  B.clear();
	  B.addInt(Laser);
	  B.addInt(Ack);
	  data->samgarPort->write();
	    
	}
      else{
	yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
	B.clear();
	B.addInt(Laser);
	B.addInt(Error);
	B.addString("Wrong device");
	data->samgarPort->write();
      }
    } // while(1)
  return NULL;
}


void* MapThread(void * param){
  bool active=true;
  PlayerCc::PlayerClient* player;
  PlayerDriver_t * data = static_cast< PlayerDriver_t*>(param);

  // player configuration
  try
    {
      // Connect to Player server
      player = new  PlayerCc::PlayerClient(data->player_hostname, data->player_port);
    }
  catch (PlayerCc::PlayerError e)
    {
      std::cerr << e << std::endl;
      throw e;
      //return -1;
    }

  PlayerCc::MapProxy pp(player,data->index);
  
  while(active)
    {
      // read data from Samgar
      yarp::os::Bottle *input = data->samgarPort->read(true); // blocking read
      // analize data from samgar
      if (input->get(TYPE).asInt() == Map)
	{
	  // read state from Player
	  player->Read();
	  
	  // perform Samgar command
	  if (input->get(CMD).asInt() == SET_REQ)
	    {
	      if (input->get(Map_MAP).asInt())
		{
		  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
		  B.clear();
		  B.addInt(Map);
		  B.addInt(Map_MAP);
		  int width = pp.GetWidth();
		  int height = pp.GetHeight();		 
		  B.addInt(width);
		  B.addInt(height);
		  B.addDouble(pp.GetResolution());
		  for (int h=0;h<height; h++)
		    for (int w=0;w<width; w++)
		      B.addInt(pp.GetCellIndex(w,h));
		  data->samgarPort->write();
		}

	    }
	  
	  // send data to Samgar
	  yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
	  B.clear();
	  B.addInt(Map);
	  B.addInt(Ack);
	  data->samgarPort->write();
	    
	}
      else{
	yarp::os::Bottle& B = data->samgarPort->prepare();	  // prepare the bottle/port
	B.clear();
	B.addInt(Map);
	B.addInt(Error);
	B.addString("Wrong device");
	data->samgarPort->write();
      }
    } // while(1)
  return NULL;
}
