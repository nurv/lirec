/** \file Player2SamgarModule.h
 */

#ifndef PLAYERSAMGARMODULE_H
#define PLAYERSAMGARMODULE_H

#include <libplayerc++/playerc++.h>
#include <yarp/os/all.h>
#include <list>
#include <string>
#include <pthread.h>
#include "SamClass.h"


enum PlayerProxy_TYPE {Position2d=0, Localize, Planner, Laser, Map, Sonar, Bumper};

enum PlayerProxy_FIELDS {TYPE=0, CMD};

enum PlayerProxy_CMD {SET_REQ=0};
enum PlayerProxy_Position2d_CMD {Position2d_SET_SPEED=1, Position2d_SET_GOAL, Position2d_SET_ODOM, Position2d_SET_MOTOR};
enum PlayerProxy_Localize_CMD {Localize_SET_POSE=1};
enum PlayerProxy_Planner_CMD {Planner_SET_GOAL=1, Planner_SET_ENABLE};
enum PlayerProxy_Laser_CMD {Laser_SET_CONFIG=1};

enum PlayerProxy_DATA {Error=0, Ack=1};

enum PlayerProxy_Position2d_DATA {Position2d_SPEED=2, Position2d_POSE, Position2d_SIZE, Position2d_STALL};
enum PlayerProxy_Localize_DATA {Localize_MAP_INFO=2, Localize_HYPOTHS};
enum PlayerProxy_Planner_DATA {Planner_GOAL=2, Planner_POSE, Planner_CURR_WAYPOINT, Planner_PATH, Planner_WAYPOINTS};
enum PlayerProxy_Laser_DATA {Laser_CONFIG=2, Laser_SIZE, Laser_POSE, Laser_ROBOT_POSE, Laser_POINTS, Laser_RANGES};
enum PlayerProxy_Map_DATA   {Map_MAP=2};
enum PlayerProxy_Sonar_DATA {Sonar_POSES=2, Sonar_RANGES};
enum PlayerProxy_Bumper_DATA {Bumper_POSES=2, Bumper_BUMPED};

class PlayerDriver_t 
{
public:
  std::string driverName;
  std::string interfName;
  int interfID;
  int index;
  long host; //IP addres of host writtedn in HEX in oposite dirrection and
	     //transfered to dec. i.e. 127.0.0.1 -> 1 00 00 7F -> 16777343
  long port;
  std::string player_hostname;
  int player_port;
  yarp::os::BufferedPort<yarp::os::Bottle>* samgarPort;
};

class Player2SamgarModule: public SamClass 
{
public:
  Player2SamgarModule(std::string hostname=PlayerCc::PLAYER_HOSTNAME, uint port=PlayerCc::PLAYER_PORTNUM);
  ~Player2SamgarModule();
  void start();
  void join_threads();
  void display();
  virtual void SamInit(){};
  virtual void SamIter(){};


private:
  PlayerCc::PlayerClient* player;
  std::string  gHostname;
  uint         gPort;
  std::list<PlayerDriver_t> drivers;
  std::vector<yarp::os::BufferedPort<yarp::os::Bottle>* > ports;
  std::list<pthread_t*> threads;
};

/** \brief Thread for communication with Position2d Player devices
 *
 * This function is design to work as a POSIX thread responsible for
 * communication with Position2dProxy in Player and allow acces to this device
 * in Samgar network.
 * 
 * \param param should be of PlayerDriver_t
 */
void* Position2dThread(void * param);

/** \brief Thread for communication with Localize Player devices
 */
void* LocalizeThread(void * param);

/** \brief Thread for communication with Planner Player devices
 */
void* PlannerThread(void * param);

/** \brief Thread for communication with Laser Player devices
 */
void* LaserThread(void * param);

/** \brief Thread for communication with Map Player devices
 */
void* MapThread(void * param);

/** \brief Thread for communication with Bumper Player devices
 */
void* BumperThread(void * param);

/** \brief Thread for communication with Map Player devices
 */
void* SonarThread(void * param);

#endif
