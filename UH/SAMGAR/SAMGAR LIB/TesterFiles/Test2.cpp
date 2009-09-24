#include <SamgarMainClass.h>
using namespace std;


int main() {

	int ccc =0;
Network yarp;
SamgarModule MyFirstTest("Module2","behaviour","happy",run); // Cant have spaces or underscores
MyFirstTest.AddPortS("num100");
MyFirstTest.AddPortS("num200");


  while( 1 ) 
  {
	  Time::delay(1);
	  printf("sending data \n");
	  MyFirstTest.SendIntData("num100",ccc++);
	  if(ccc>100){ccc=0;}
	  MyFirstTest.SucceedFail(true,888);
  }

  return 0;
}