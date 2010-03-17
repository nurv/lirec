#include <SamgarMainClass.h>
using namespace std;

int main() {

Network yarp;
int myint;

myint = 0;

SamgarModule MyFirstTest("Reciver","FaceDetect","Camera",interupt); 
MyFirstTest.AddPortS("in");
MyFirstTest.AddPortS("example");


while( 1 ) 
  {
  // in this module we accept int data from the specified port. When we call a port for data a true or false returns specifying whether it is new or old data, 
  // and the data reference we pass gets changed if true
  if(MyFirstTest.GetIntData("in",&myint)==true)
    {
     printf("got data %d \n",myint);
 // in this module we also update the log with this command.
     if(myint%5==0){MyFirstTest.SendToLog("this has come from a port",3);}
    }
  else
   {
// this will never be printed proving the module sleeps after each new input
    printf("got no data \n");
    }
 // in an interrupt module this function call has added significance, the primary being the true or false whether it has accomplished its task, 
 // and secondly a variable which could be how well it has acheived the task. This data is sent on to the GUI so the success rate of the modules can be deduced
  MyFirstTest.SucceedFail(true,myint);
  }
  return 0;
}
