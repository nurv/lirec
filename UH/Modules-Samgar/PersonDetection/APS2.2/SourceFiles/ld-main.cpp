
//#include <stdio.h>
//#include <math.h>
#include <conio.h>
#include <windows.h>
#include "LegsDetector.h"
#include "Utility.h"
#include "SamModules.h"

int main(void) {

	//*********** Yarp, SAMGAR init
	samDataIn laserDataIn("/vLD1");
	laserDataIn.PamInit("LDlaserIn");

	samDataOut ldDataOut("/vLD2");
	ldDataOut.PamInit("LDPoseOut");

	//*********** Leg Detection init
	double rangeDim = 512;
	double rangeResolution = (double)360/1024;
	double maxrange = 4.0, minrange = 0.32;
	laser_t ta;
	vector< laser_t > laserVector;
	int legHowMany;

	LegsDetector legs(rangeDim, 2, 3);   // 3-close 1-far distances
	legs.setDebug(true, 20);
	int ic = 0;

	while( 1 )
	{
		legHowMany = 0;
		printf("\n  LD   %d  ", ic++);
		double data1[700];
		int n;
		laserDataIn.PamIter(&n, data1);

		if( n>598 )
		{
			laserVector.clear();
			for (int i = 0; i < 512; i++ )
			{
				ta.angle = DTOR( i*rangeResolution-90 );
				ta.range = data1[i+85]/1000;
				if ( ta.range < minrange || ta.range > maxrange ) ta.range = maxrange;
				ta.x = 0; ta.y = 0;	ta.intensity = 4;
				laserVector.push_back(ta);
			}
			legs.update(laserVector);
			legHowMany = legs.getHowMany();
		}

		double data[15];
		data[0] = legHowMany;
		for (int i=0; i<legHowMany; i++)  
		{
			data[2*i+1] = -RTOD(legs.getBearing(i));
			data[2*i+2] = legs.getDistance(i);
			printf(" %.0lf ", data[2*i+1]);
		}
		ldDataOut.PamIter(2*legHowMany+1, data);

		Sleep(50);
		if( _kbhit() > 0 ) { if( _getch() == 0x1b ) break; } 
	}
	return 0;
}


