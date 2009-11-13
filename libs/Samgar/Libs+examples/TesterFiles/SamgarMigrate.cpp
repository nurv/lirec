#include <SamgarMainClass.h>
using namespace std;

list<ModuleStruct>::iterator ItMod;
list<string>      ::iterator ItPlat;


int main() {

int ccc =0;
Network yarp;
SamgarModule Control("Control","Something","Something",run); // Cant have spaces or underscores
Control.TurnOnModuleListener();			// this allows it to accept messages about the status of other modules and platforms
Control.GetAvailPlatforms();			// send out a msg to the network to get other places that can be migrated to
											// have to carefull with this command, use it rapidly and it could bring the whole network down
while(1)
{

	yarp::os::Time::delay(8); // a small delay 
//Control.GetAvailPlatforms();
	// this will list out all the modules available to it
	printf(" \n \n The available modules are : \n");
	// the ListOfKnownModules is updated everytime the GUI finds or loses a module, although there can be a small delay.
	for ( ItMod=Control.ListOfKnownModules.begin() ; ItMod != Control.ListOfKnownModules.end(); ItMod++ )
		{
			string name = ItMod->name;
			string cat1 = ItMod->catagory;
			string cat2 = ItMod->subcatagory;
			string Combi = name + " " + cat1 + " " + cat2 + "\n";
			printf(Combi.c_str());
		}

	printf(" \n \n The available platforms are : \n");
		for ( ItPlat=Control.ListOfKnownPlatforms.begin() ; ItPlat != Control.ListOfKnownPlatforms.end(); ItPlat++ )
		{
			string name = ItPlat->c_str();// + "\n";
			name = name +"\n";
			printf(name.c_str());
		}
	Control.SucceedFail(true,888);
}
  return 0;
}