

#include <SamgarMainClass.h>
using namespace std;

// types of module
//#define interupt 0
//#define run      1
// current mode
//#define running 0
//#define paused 1
//#define stoped 2
//#define fullstop 3


int main() {

Network yarp;
int myint;

myint = 0;

SamgarModule MyFirstTest("Module1","behaviour","happy",interupt); // Cant have spaces or underscores
MyFirstTest.AddPortS("num10");
MyFirstTest.AddPortS("num20");


while( 1 ) 
  {
	 if(MyFirstTest.GetIntData("num10",&myint)==true)
	 {
		printf("got data %d \n",myint);
	
		if(myint%5==0){MyFirstTest.SendToLog("this has come from a port",3);}
	//	if(myint>50)
	//	{
				
		//	MyFirstTest.SendAllModulesCommand(0);		
		//	MyFirstTest.SendModuleCommand("Module2",3);
	//		myint=0;
		
	//	}
	 }
	 else
	 {
		printf("got no data \n");
	 }
	 MyFirstTest.SucceedFail(true,myint);
  }

  return 0;
}









