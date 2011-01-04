#include <SamgarMainClass.h>
using namespace std;

int main() {

int ccc =0;
Network yarp; // setup the network, always needed
//Create a module, with a given name,category and subcategory all of which must only contain a-z 0-9
SamgarModule IntSender("Sender","Distance","Sonar",run);
//The last option must be run or interrupt both of which have different effects on the module. 
//Run will create a module which runs continuously until a stop command is sent from the GUI or other module. 
//Interrupt will only enable the module to run whilst there is new data on the port, so if no infomation is sent it is then impossible to run the module.
IntSender.AddPortS("Out");
IntSender.AddPortS("OutExample");
// these two commands start two ports that are children of the module
  while( 1 ) 
  {
		  yarp::os::Time::delay(5);
		  // this function sends int data on the specified port 
          IntSender.SendIntData("Out",ccc++);
          // in a run module the data given in this function is unimportant but still needs to be called for the module to work properly
          IntSender.SucceedFail(true,888);
  }
 return 0;
}
