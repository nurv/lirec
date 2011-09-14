#define maxmodules 50
#define maxconns   100
#define maxports   10
#define maxmigraionplatforms 10

#include "InteruptOverlay.h"


class SamClass
{
private:
MainPortOverlay MainPort;
string myName;
string ports[maxports];
int portcounter;
Network yarp;

string PossibleMigrationPlatforms[maxmigraionplatforms];

public:
myPortReport myPortStatus;
	SamClass()
	{
	portcounter =0;
	myPortStatus.CopyMainPort=&MainPort;
	}

	void StartModule(string NameOfModule)
	{
	
	myName=NameOfModule;
	MainPort.SetUp(NameOfModule);
	MainPort.addOutput("/CONTROL","tcp");

	while(MainPort.isWriting()){}
	Bottle& B = MainPort.prepare();
	B.addString("Add_Module");
	B.addString(NameOfModule.c_str());
	for(int uu =0;uu<portcounter;uu++)
		{
		B.addString(ports[uu].c_str());
		}
	MainPort.write();

	}
	void RecognisePort(string name)
	{
		ports[portcounter]=name.c_str();
		portcounter++;
		if(portcounter>maxports){portcounter=0;}
	}

	void RegisterForMigration(void)
	{
	// switch namespaces
	// open port
	// switch namespace back
	}

	void UpdateMigratoinPlatforms(void)
	{
	// switch to global namespace
	// get a list of possible platforms 
	//string PossibleMigrationPlatforms[maxmigraionplatforms];
	}
	bool ConnectToPlatform(string platform)
	{
	//switch namespaces
	// uses yarp.connect(thisname,platformname)
	}
};
