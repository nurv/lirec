/** \file Player2SamgarModule.cpp
 */
#include "Player2SamgarModule.h"
//#include "Player2SamgarThread.h"
#include <iostream>

std::string itoa(int value, int base=10) 
{
  std::string buf;
  
  // check that the base if valid
  if (base < 2 || base > 16) return buf;
  
  enum { kMaxDigits = 35 };
  buf.reserve( kMaxDigits ); // Pre-allocate enough space.
  
  int quotient = value;
  
  // Translating number to string with base:
  do {
    buf += "0123456789abcdef"[ std::abs( quotient % base ) ];
    quotient /= base;
  } while ( quotient );
  
  // Append the negative sign
  if ( value < 0) buf += '-';
  
  std::reverse( buf.begin(), buf.end() );
  return buf;
}



Player2SamgarModule::Player2SamgarModule(std::string hostname, uint port):
  SamClass(std::string("/Player-")+hostname+"-"+itoa(port)), gHostname(hostname), gPort(port)
{
  int proxiesCount;
  // initialize data structure if needed

  // initialization of Player
  try
    {
      // Connect to Player server
      player = new  PlayerCc::PlayerClient(gHostname, gPort);
    }
  catch (PlayerCc::PlayerError e)
    {
      std::cerr << e << std::endl;
      throw e;
      //return -1;
    }
  player->StartThread();

  std::list<playerc_device_info_t > devices;
  int devicesCount;
  // obtaining list of devices available in Player
  player->RequestDeviceList();
  devices=player->GetDeviceList();

  PlayerDriver_t currDriver;
  std::string name;
  // check Player devices and add them as a ports to Samgar
  for (std::list<playerc_device_info_t>::iterator 
	 it = devices.begin(); it != devices.end(); it++) 
    {    
      currDriver.driverName=it->drivername ;
      currDriver.interfName=player->LookupName(it->addr.interf);
      currDriver.interfID=it->addr.interf;
      currDriver.index=it->addr.index ;
      currDriver.host=it->addr.host;
      currDriver.port=it->addr.robot ;
      currDriver.player_hostname=gHostname;
      currDriver.player_port=gPort;

      name = currDriver.interfName; 
      name += "-"+itoa(currDriver.index);
      
      ports.push_back(new  yarp::os::BufferedPort<yarp::os::Bottle>);
      newPort(ports.back(), name);
      currDriver.samgarPort=ports.back();

      drivers.push_back(currDriver);
    }

  StartModule();
}

Player2SamgarModule::~Player2SamgarModule()
{
  // cancell all threads
  for (std::list<pthread_t*>::iterator
	 it = threads.begin(); it != threads.end(); it++) 
    { 
      pthread_cancel(*(*it));
    }

  // join all threads
  for (std::list<pthread_t*>::iterator
	 it = threads.begin(); it != threads.end(); it++) 
    { 
      pthread_join(*(*it), NULL);
    }

  // free all data
    for (std::list<pthread_t*>::iterator
	 it = threads.begin(); it != threads.end(); it++) 
    { 
      delete (*it);
    }
    threads.clear();

    drivers.clear();

    for ( std::vector<yarp::os::BufferedPort<yarp::os::Bottle>* >::iterator
	    it = ports.begin(); it != ports.end(); it++) 
      { 
	delete (*it);
      }
    ports.clear();
       
}

void Player2SamgarModule::display()
{
  std::cout<<"Found following Player drivers:"<<std::endl;
  for (std::list<PlayerDriver_t>::const_iterator
	 it = drivers.begin(); it != drivers.end(); it++) 
    {    
      std::cout <<"-> "<< it->driverName <<" "<< it->interfName<<"("<<
	it->interfID<<")"<<":"<<it->index <<" "<<it->host<< ":"
	<<it->port << std::endl;
    }
}

void Player2SamgarModule::start()
{
  std::cout<<"Player2SamgarModule: starting device threads"<<std::endl;
  // start all threads
  for (std::list<PlayerDriver_t>::iterator
	 it = drivers.begin(); it != drivers.end(); it++) 
    { 
      std::cout <<"-> "<< it->driverName <<" "<< it->interfName<<":"<<it->index << std::endl;
      if (it->interfName.compare("position2d")==0)
	{
	  threads.push_back(new pthread_t);
	  pthread_create(threads.back(), NULL, &Position2dThread, &(*it));
	}
      if (it->interfName.compare("localize")==0)
	{
	  threads.push_back(new pthread_t);
	  pthread_create(threads.back(), NULL, &LocalizeThread, &(*it));
	}
      // wafewront has a little bug in its interface so there is a need of such a trick
      if (it->interfName.compare("unknown")==0 && it->driverName.compare("wavewront"))
	{
	  threads.push_back(new pthread_t);
	  pthread_create(threads.back(), NULL, &PlannerThread, &(*it));
	}
      if (it->interfName.compare("laser")==0)
	{
	  threads.push_back(new pthread_t);
	  pthread_create(threads.back(), NULL, &LaserThread, &(*it));
	}
      if (it->interfName.compare("map")==0)
	{
	  threads.push_back(new pthread_t);
	  pthread_create(threads.back(), NULL, &MapThread, &(*it));
	}
      if (it->interfName.compare("sonar")==0)
	{
	  threads.push_back(new pthread_t);
	  pthread_create(threads.back(), NULL, &SonarThread, &(*it));
	}
      if (it->interfName.compare("bumper")==0)
	{
	  threads.push_back(new pthread_t);
	  pthread_create(threads.back(), NULL, &BumperThread, &(*it));
	}
    }
}

void Player2SamgarModule::join_threads()
{
  for (std::list<pthread_t*>::iterator
	 it = threads.begin(); it != threads.end(); it++) 
    { 
      pthread_join(*(*it), NULL);
    }
}
