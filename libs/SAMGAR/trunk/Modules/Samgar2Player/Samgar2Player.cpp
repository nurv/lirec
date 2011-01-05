/** \file Samgar2Player.cpp
 *  This file contains example of two Samgar V2 modules running in the same
 *  file. First module is a Writer and it sends increasing numbers. The second
 *  module is a Reader that is capable of obtaining data from other module. 
 */

#include "Player2SamgarModule.h"

int main(void) 
{
  Player2SamgarModule translator;
  std::list<PlayerDriver_t> proxies;
  int prox_count;
  translator.display();
  translator.start();
  translator.join_threads();
  return 0;
}


	
