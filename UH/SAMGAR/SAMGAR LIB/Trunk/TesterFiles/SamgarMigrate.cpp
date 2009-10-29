#include <SamgarMainClass.h>
using namespace std;

list<ModuleStruct>::iterator ItMod;
list<string>      ::iterator ItPlat;


int main() {

int ccc =0;
Network yarp;
SamgarModule MyFirstTest("Module2","behaviour","happy",run); // Cant have spaces or underscores
MyFirstTest.AddPortS("num100");
MyFirstTest.AddPortS("num200");
MyFirstTest.TurnOnModuleListener();			// this allows it to accept messages about the status of other modules and platforms
MyFirstTest.GetAvailPlatforms();			// send out a msg to the network to get other places that can be migrated to
											// have to carefull with this command, use it rapidly and it could bring the whole network down
while(1)
{

	yarp::os::Time::delay(8); // a small delay 

	// this will list out all the modules available to it
	printf(" \n \n The available modules are : \n");
	// the ListOfKnownModules is updated everytime the GUI finds or loses a module, although there can be a small delay.
	for ( ItMod=MyFirstTest.ListOfKnownModules.begin() ; ItMod != MyFirstTest.ListOfKnownModules.end(); ItMod++ )
		{
			string name = ItMod->name;
			string cat1 = ItMod->catagory;
			string cat2 = ItMod->subcatagory;
			string Combi = name + " " + cat1 + " " + cat2 + "\n";
			printf(Combi.c_str());
		}

	printf(" \n \n The available platforms are : \n");
	// this command sends a message to the GUI to ask for new data to which companion shells there are to migrate to
	MyFirstTest.GetAvailPlatforms();
		for ( ItPlat=MyFirstTest.ListOfKnownPlatforms.begin() ; ItPlat != MyFirstTest.ListOfKnownPlatforms.end(); ItPlat++ )
		{
			string name = ItPlat->c_str();// + "\n";
			name = name +"\n";
			printf(name.c_str());
		}
	MyFirstTest.SucceedFail(true,888);
}
  return 0;
}