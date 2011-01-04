/** \file SamExample.cpp
 *  This file contains example of two Samgar V2 modules running in the same
 *  file. First module is a Writer and it sends increasing numbers. The second
 *  module is a Reader that is capable of obtaining data from other module. 
 */
#include "ExampleOne.h"

int main(void) 
{
   ExampleOneWrite myfirstmodule("/Writer");
   myfirstmodule.SamInit();
   
   ExampleTwoRead mysecondmodule("/Reader");
   mysecondmodule.SamInit();
   
   while(1)
   {
      myfirstmodule.SamIter();
      mysecondmodule.SamIter();
      yarp::os::Time::delay(2);
   }
   
   return 0;
}


	
