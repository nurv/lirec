/** \file Samgar2Player.cpp
 *  This file contains example of two Samgar V2 modules running in the same
 *  file. First module is a Writer and it sends increasing numbers. The second
 *  module is a Reader that is capable of obtaining data from other module. 
 */

#include "PlayerSamgarModule.h"

int main(void) 
{
  PlayerSamgarModule translator;
  std::list<PlayerDriver_t> proxies;
  int prox_count;
  translator.display();
  translator.start();
  //sleep(10);
  while(1)
    sleep(1);
  
  return 0;
}


	
