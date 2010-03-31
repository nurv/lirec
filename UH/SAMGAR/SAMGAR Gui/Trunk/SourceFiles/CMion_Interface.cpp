

#include <yarp/sig/all.h>
#include <yarp/os/all.h>
#include <iostream>
#include <list>
#include <fstream>
#include <string>

using namespace yarp::os;
using namespace std;

int main (void)
{
Network yarp; // might need to initalise the network 
BufferedPort<Bottle> ThePort1,ThePort2;
Bottle DataToSend,DataRecived;
int i;
ConstString NameOfServer=Network::getNameServerName();
Network::setNameServerName(NameOfServer);
ThePort1.open("/Main_test");
yarp::os::Time::delay(5);
Network::setNameServerName("/Global");
ThePort2.open("/Main_Migrate");
yarp::os::Time::delay(5);
Network::setNameServerName(NameOfServer);
yarp::os::Time::delay(5);

	while(1)
	{
		puts(" 1 . Register migration port");
		puts(" 2 . Ask for current platforms");
		puts(" 3 . Migrate out (gui out)");
		puts(" 4 . Migrate in  (gui in)");
	cin >> i;	
	DataToSend.clear();
	DataRecived.clear();

	Bottle& cc = ThePort1.prepare();
	Bottle *b;
		switch (i)
		{
		case 1:	// register with migration port

			puts("sending data to register migration port");
			cc.clear();
			cc.addInt(4004);
			ThePort1.write();
		break;
		case 2:
			puts("sending data to get current platforms");
			cc.clear();
			cc.addInt(40);
			ThePort1.write();
			b = ThePort1.read(true);
			puts(b->toString().c_str());
		break;
		case 3:
			puts("sending migration data and command");
			cc.clear();
			cc.addInt(36);
			cc.addString("/fotis_Migration");
			cc.addString("whos");
			cc.addString("a");
			cc.addString("duck");
			cc.addString(",whos");
			cc.addString("a");
			cc.addString("little");
			cc.addString("duck");
			ThePort1.write();
			b = ThePort1.read(true);
			if(b->get(1).asInt()==1){puts("Sucsesfull migrated");}
			else					{puts("unsuc migration");}

			b = ThePort2.read(true);
			puts(b->toString().c_str());
		break;
		case 4:
			Network::setNameServerName("/Global");
			puts("changed to global");
			Time::delay(5);
			puts("setting cons");
			while(!Network::connect("/Main_Migrate"   ,"/local_Migration","tcp")){;}
		//	while(!Network::connect("/local_Migration","/Main_Migrate","tcp")){;}

			yarp::os::Time::delay(5);
			puts("finnished cons changing to local");
			
			Network::setNameServerName(NameOfServer);
			
			//cc.addInt(36);
			cc.addString("/Main_Migrate");
			cc.addString("whos");
			cc.addString("a");
			cc.addString("duck");
			cc.addString(",whos");
			cc.addString("a");
			cc.addString("little");
			cc.addString("duck");
			ThePort2.write();
			puts("wrote data");
			b = ThePort1.read(true);
			puts(b->toString().c_str());

		break;
		}

		


	}

}








/*#include <yarp/sig/all.h>
#include <yarp/os/all.h>
#include <iostream>
#include <list>
#include <fstream>
#include <string>

using namespace yarp::os;
using namespace std;

// Create a class that inherts from a bufferd port of type bottle
 class MainCmionPort : public BufferedPort<Bottle> 
{
public:

	MainCmionPort(void)
	{
		useCallback();      // set the port to use onRead
		while(Network::getNameServerName()=="/global"){;}   // a noop to wait untill the network is on the local and not global server
		open("/Main_CMion"); // open the port, this will be the main module port you'll get updates on
		while(getInputCount()<1)
		{;} 
		// you might want a line like this to know that the samgar gui has actully connected to the maim module port
	}


	  virtual void onRead(Bottle& b) 
	 {
		 int choice = b.get(0).asInt();
		puts("got something on port");
		string vv = "The bottle as a string :";
		vv = vv + b.toString().c_str();

		int FF =0;
		// the first int tell you what type of data will be comming across
		switch (choice)
		{
		case 105:
			printf(" got into 105");
			// this gives you data on what modules exist on the system so
			while(FF<=b.size())
			{
			string name        =b.get(FF).asString().c_str();FF++;
			string catagory    =b.get(FF).asString().c_str();FF++;
			string subcatagory =b.get(FF).asString().c_str();FF++;
			}
		break;

		case 50:
			// this will give you more data on what platforms can be migrated to
			while(FF<=b.size())
			{
			string PlacesToMigrateto =b.get(FF).asString().c_str();FF++;
			}
		break;
		}
     }

};
 class LittleCmionPort : public BufferedPort<Bottle> 
{
public:

	LittleCmionPort(string name)
	{
	useCallback();
	name="/Port_CMion_"+name;
	open(name.c_str()); // needs this naming scheme to be recognised
	}

  // get your data here to do whatever you wish!!!
  virtual void onRead(Bottle& b) 
	 {
		int something = b.get(0).asInt();
	 }
 };




int main (void)
{
Network yarp; // might need to initalise the network 

MainCmionPort MyMainPort; // create instance of the class
yarp::os::Time::delay(2);
LittleCmionPort PortForSomething("Port1"); // only a-z 0-9 no spaces or anything!!!!
yarp::os::Time::delay(2);
LittleCmionPort PortForSomethingelse("Port2");

while(1){;}

}
*/



