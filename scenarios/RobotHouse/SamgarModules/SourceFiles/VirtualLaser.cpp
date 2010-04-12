/*
 programmer : K . Du Casse 
 e-mail     : k.du-casse@herts.ac.uk
 Date of original : 07 april 2010
 Date of last mod : 07 april 2010

 Version 1.0
*/
#include "SamgarMainClass.h"
#include <stdio.h>
#include <math.h>
#include <iostream>
using namespace std;
using namespace yarp;


#include <src/hokuyo_aist/hokuyo_aist.h>



//int main(int argc, char **argv)
int main(void)
{
Network yarp;
	string portOptions = "type=serial,device=/dev/ttyACM0,timeout=1";
	double startAngle = 0.0, endAngle = 0.0;
	int firstStep = -1, lastStep = -1;
	unsigned int baud = 19200, speed = 0, clusterCount = 1;
	bool getIntensities = false, getNew = false, verbose = false;
 SamgarModule Laser("Obj","something","Laser",run); // Cant have spaces or underscores
	
	Laser.AddPortS("Output");
	Bottle PosMessage;

	portOptions = "type=serial,device=COM4,timeout=1";

	puts("started the main bit");

	try
	{
		hokuyo_aist::HokuyoLaser laser; // Laser scanner object

		puts("created main laser");
		// Set the laser to verbose mode (so we see more information in the console)
		if (verbose)
		{
			laser.SetVerbose (true);
		}
		puts("set verbose");
		// Open the laser
		laser.Open (portOptions);

		puts("opened laser");
		// Turn the laser on
		laser.SetPower (true);
		// Set the baud rate

		puts("set power and stuff");
		try
		{
			laser.SetBaud (baud);
		}
		catch (hokuyo_aist::HokuyoError e)
		{
			cerr << "Failed to change baud rate: (" << e.Code () << ") " << e.what () << endl;
		}
		// Set the motor speed
		try
		{
			laser.SetMotorSpeed (speed);
		}
		catch (hokuyo_aist::HokuyoError e)
		{
			cerr << "Failed to set motor speed: (" << e.Code () << ") " << e.what () << endl;
		}

		// Get some laser info
		cout << "Laser sensor information:" << endl;
		hokuyo_aist::HokuyoSensorInfo info;
		laser.GetSensorInfo (&info);
		cout << info.AsString ();

		// Get range data
		hokuyo_aist::HokuyoData data; 
		while(1)
		{
			yarp::os::Time::delay(0.5);
		if ((firstStep == -1 && lastStep == -1) &&
			(startAngle == 0.0 && endAngle == 0.0))
		{
			// Get all ranges
			if (getNew)
				laser.GetNewRanges (&data, -1, -1, clusterCount);
			else if (getIntensities)
				laser.GetNewRangesAndIntensities (&data, -1, -1, clusterCount);
			else
				laser.GetRanges (&data, -1, -1, clusterCount);
		}
		else if (firstStep != -1 || lastStep != -1)
		{
			// Get by step
			if (getNew)
				laser.GetNewRanges (&data, firstStep, lastStep, clusterCount);
			else if (getIntensities)
				laser.GetNewRangesAndIntensities (&data, firstStep, lastStep, clusterCount);
			else
				laser.GetRanges (&data, firstStep, lastStep, clusterCount);
		}
		else
		{
			// Get by angle
			if (getNew)
				laser.GetNewRangesByAngle (&data, startAngle, endAngle, clusterCount);
			else if (getIntensities)
				laser.GetNewRangesAndIntensitiesByAngle (&data, startAngle, endAngle, clusterCount);
			else
				laser.GetRangesByAngle (&data, startAngle, endAngle, clusterCount);
		}

	//	cout << "Laser range data:" << endl;
	//	cout << data.AsString ();


		const uint32_t * ArrayOfRange = data.Ranges(); // so get the pointer to the data
int i;
		PosMessage.clear();
		/// this looks like it works to get the raw data!!
		for(int hh =0; hh< data.Length();hh++) // for the data length
		{
		double xx = ArrayOfRange[hh];   // get each string and put it into a int.
		if(xx<300){xx=0;}	
		PosMessage.addDouble(xx);
	//	printf("I think its this long : %f \n",xx);
		}
		Laser.SendBottleData("Output",PosMessage);
	
		Laser.SucceedFail(true,100);
		
	}
		// Close the laser
		laser.Close ();
	}
	catch (hokuyo_aist::HokuyoError e)
	{
		puts("big bloody error");
		puts("could be wroung com port! Com port set to 4");
		puts("is driver installed for the laser??");
		cerr << "Caught exception: (" << e.Code () << ") " << e.what () << endl;
		return 1;
	}

	return 0;
}
