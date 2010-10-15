#include "ExampleOne.h"

int main(void) 
{
   ExampleOneWrite myfirstmodule;
   myfirstmodule.SamInit();

   ExampleTwoRead mysecondmodule;
   mysecondmodule.SamInit();

   while(1)
   {
      myfirstmodule.SamIter();
      mysecondmodule.SamIter();
      yarp::os::Time::delay(2);
   }

   return 0;
}


	
