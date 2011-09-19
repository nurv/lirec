
/*
Virtual laser
Sends a Bottle over the network, could add laser posistion in a header so we could use multiple lasers without much fuss 
*/
/********************* REMEBER TO TWEAK EXCLUSION ANGLES (ANGLES WHICH HIT THE ROBOT ETC) *******************/
#define DEBUGlaser 1
#define mindistance 0.10 // in m, we know the laser hits the robot so get rid of those readings
#define LaserWait	0


#include "SamClass.h"
#include <mrpt/slam.h>
#include <mrpt/hwdrivers/CHokuyoURG.h>
#include <mrpt/hwdrivers/CSerialPort.h>
#include <mrpt/gui.h>

using namespace mrpt;
using namespace mrpt::hwdrivers;
using namespace mrpt::slam;
using namespace mrpt::gui;
using namespace mrpt::utils;
using namespace std;


class Vlaser: public SamClass
{
private:
BufferedPort<Bottle> myfirst; // its gonna be process local connection, so i can send the whole class :)


Network yarp;
public:
CHokuyoURG      laser;


	void SamInit(void)
	{
	puts("in laser");
	laser.setSerialPort("COM10");
	
	puts("set serial \n Starting laser, this may take a minute");

	bool TurnedOn = false;

	while(!TurnedOn) // sometimes the coms are busy, wait untill we get what we want 
	{
		try{TurnedOn = laser.turnOn();}
		catch (...){}
	}
	puts("laser turned on");
	RecognisePort("Out");
	StartModule("/Laser");
	myfirst.open("/Laser_Out"); //myPortStatus
	myfirst.setReporter(myPortStatus);
	

	}

	void Disconnect()
	{
		laser.turnOff();
	
	}
	
	void SamIter(void)
	{
	bool thereIsObservation,hardError;	
	CObservation2DRangeScan myscan;
	
	
    laser.doProcessSimple( thereIsObservation, myscan, hardError );
	Bottle &Send = myfirst.prepare();
	Send.clear();

	if(thereIsObservation )
		{
			//scan only 512 points for 180 deg, ignore the first,last 85 points
			for(int x=0;x<myscan.scan.size()-85;x++)
			{
				//if(myscan.scan[x]<mindistance){myscan.scan[x]=0;}
				if(x>84) 
				{
					Send.addDouble(myscan.scan[x]);

					if(myscan.scan[x]==1.0)
						std::cout << "obst sensed at " << x <<std::endl;
					
					//if(myscan.scan[x]<mindistance){myscan.scan[x]=0;}
				}
				//std::cout << "Scan size Vlaser" << myscan.scan.size() <<std::endl;
			}
		myfirst.writeStrict();
		}
	//std::cout<< " laser size " <<Send.size() <<std::endl;
	}
};


int main(void) 
{
	Vlaser lsr;
	lsr.SamInit();

	
    while(1)
	{
	
		lsr.SamIter();
		
		yarp::os::Time::delay(1.0);
	}

	lsr.Disconnect();

  return 0;
}