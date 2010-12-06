
//#include <stdio.h>
#include <conio.h>
#include <windows.h>
#include "SamModules.h"
#include "hokuyo_aist/hokuyo_aist.h"

using namespace std;
using namespace yarp;

int main(int argc, char **argv)
{

	string portOptions = "type=serial,device=COM8,timeout=1";
	unsigned int baud = 19200;
	unsigned int speed = 0;
	unsigned int clusterCount = 1;

	samDataOut laserDataOut("/vLaser");
	laserDataOut.PamInit("laserOut");

	hokuyo_aist::HokuyoLaser laser;
	try
	{
		laser.Open(portOptions);
		laser.SetPower(true);
		laser.SetBaud(baud);
		laser.SetMotorSpeed(speed);
	}
	catch (hokuyo_aist::HokuyoError e)
	{
		cerr << "Failed to initiate laser: (" << e.Code () << ") " << e.what () << endl;
	}
	int ic = 0;
	printf("\n vLaser ....\n\n");

	hokuyo_aist::HokuyoData data;
	while(1)
	{
		printf("\n vLaser  %d  ", ic++);
		Sleep(50);
		if( _kbhit() > 0 ) { if( _getch() == 0x1b ) break; } 

		data.CleanUp();
		unsigned int t = laser.GetRanges (&data, -1, -1, clusterCount);
		if ( t<598 ) continue;
		const uint32_t * ArrayOfRange = data.Ranges(); 

		double data1[700];
		for(unsigned int hh =0; hh< data.Length();hh++)	data1[hh] = ArrayOfRange[hh]; 
		laserDataOut.PamIter(data.Length(), data1);
	}
	laser.Close ();
	return 0;
}
