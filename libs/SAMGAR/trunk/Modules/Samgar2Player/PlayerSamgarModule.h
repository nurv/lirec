/** \file PlayerSamgarModule.h
 */

#ifndef PLAYERSAMGARMODULE_H
#define PLAYERSAMGARMODULE_H

#include <libplayerc++/playerc++.h>
#include <yarp/os/all.h>
#include <list>
#include <string>
#include <pthread.h>
#include "SamClass.h"
#include "PlayerSamgarThread.h"




class PlayerSamgarModule: public SamClass 
{
public:
  PlayerSamgarModule(std::string hostname=PlayerCc::PLAYER_HOSTNAME, uint port=PlayerCc::PLAYER_PORTNUM);
  ~PlayerSamgarModule();
  void start();
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

#endif
