#include "SamModules.h"

int main(void) 
{
   //ExampleOneWrite myfirstmodule("/Writer");
   //myfirstmodule.SamInit();
   
   //ExampleTwoRead mysecondmodule("/Reader");
   //mysecondmodule.SamInit();
   
   while(1)
   {
      //myfirstmodule.SamIter();
      //mysecondmodule.SamIter();
      yarp::os::Time::delay(2);
   }
   
   return 0;
}


	
