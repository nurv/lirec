
//#include <stdio.h>
#include <conio.h>
#include <math.h>
#include <windows.h>
#include "SamModules.h"
#define M_PI 3.1415927

int main(void) {

	//*********** Yarp, SAMGAR init
	samDataIn fdDataIn("/vPD1");
	fdDataIn.PamInit("FDPoseIn");

	samDataIn ldDataIn("/vPD2");
	ldDataIn.PamInit("LDPoseIn");

	samDataIn ftDataIn("/vPD3");
	ftDataIn.PamInit("FTPoseIn");

	samDataOut pdDataOut("/vPD4");
	pdDataOut.PamInit("PDPoseOut");

	int ldLocation[7], fdLocation[7], ftLocation[7];
	int ldHowMany, fdHowMany, ftHowMany;
	int pZone=0, tZone;
	double tStrength, tAngle, sStrength = 0;
	bool verbose = true;
	double strength_th = 0.70;


	//	Array		0		1		2		3		4		5		6
	//	Zones		-3		-2		-1		0		+1		+2		+3
	//	FD							-.5		0		+.5
	//	FT							-15		0		+15
	//	LD					-45		-25		0		+25		+45
	//
	//
	//
	int ic = 0, in = 0;

	while( 1 )
	{
		for (int i=0; i<7; i++)	ldLocation[i] = 0;
		for (int i=0; i<7; i++)	fdLocation[i] = 0;
		for (int i=0; i<7; i++)	ftLocation[i] = 0;
		tZone = 0;
		tStrength = 0;
		tAngle = 0;

		double data1[15], data2[15], data3[15], data4[15];
		fdDataIn.PamIter(&fdHowMany, data1);
		fdHowMany = (fdHowMany>0) ? ((fdHowMany-1)/2) :0;
		for (int i=0; i<fdHowMany; i++) 
		{
			if      ( data1[2*i+1] < -0.5 ) fdLocation[1]++;
			else if ( data1[2*i+1] <  0   ) fdLocation[2]++;
			else if ( data1[2*i+1] < +0.5 ) fdLocation[4]++;
			else                            fdLocation[5]++;
		}
		
		ldDataIn.PamIter(&ldHowMany, data2);
		ldHowMany = (ldHowMany>0) ? ((ldHowMany-1)/2) :0;
		for (int i=0; i<ldHowMany; i++) 
		{
			if      ( data2[2*i+1] < -45 ) ldLocation[0]++;
			else if ( data2[2*i+1] < -25 ) ldLocation[1]++;
			else if ( data2[2*i+1] <   0 ) ldLocation[2]++;
			else if ( data2[2*i+1] < +25 ) ldLocation[4]++;
			else if ( data2[2*i+1] < +45 ) ldLocation[5]++;
			else						   ldLocation[6]++;
		}

		ftDataIn.PamIter(&ftHowMany, data3);
		ftHowMany = ( (ftHowMany>0) && (data3[0]>0) ) ? 1 : 0;
		if ( ftHowMany == 1 )
		{
			double xBearing = 180 * atan(data3[1]/data3[3])/M_PI;
			double yBearing = 180 * atan(data3[2]/data3[3])/M_PI;

			if      ( xBearing < -15 ) ftLocation[1]++;
			else if ( xBearing <   0 ) ftLocation[2]++;
			else if ( xBearing <  15 ) ftLocation[4]++;
			else                       ftLocation[5]++;
		}
		if (verbose) printf("\n  PD %d  %d  %d  %d ", ic++, fdHowMany, ldHowMany, ftHowMany );

		if ( (ftLocation[2]>0 || fdLocation[2]>0) && ldLocation[2]>0 )
		{
			tZone = -1;
			tStrength = 0.95;
			if (verbose) printf(" Face+Leg at Center Left");
		}
		else if ( (ftLocation[4]>0 || fdLocation[4]>0) && ldLocation[4]>0 )
		{
			tZone = +1;
			tStrength = 0.95;
			if (verbose) printf(" Face+Leg at Center Right");
		}
		else if ( (ftLocation[1]>0 || fdLocation[1]>0) && ldLocation[1]>0 )
		{
			tZone = -2;
			tStrength = 0.95;
			if (verbose) printf(" Face+Leg at Left");
		}
		else if ( (ftLocation[5]>0 || fdLocation[5]>0) && ldLocation[5]>0 )
		{
			tZone = +2;
			tStrength = 0.95;
			if (verbose) printf(" Face+Leg at Right");
		}
		else if ( ftLocation[2]>0 || fdLocation[2]>0 )
		{
			tZone = -1;
			tStrength = 0.75;
			if (verbose) printf(" Face at Center Left");
		}
		else if ( ftLocation[4]>0 || fdLocation[4]>0 )
		{
			tZone = +1;
			tStrength = 0.75;
			if (verbose) printf(" Face at Center Right");
		}
		else if ( ftLocation[1]>0 || fdLocation[1]>0 )
		{
			tZone = -2;
			tStrength = 0.75;
			if (verbose) printf(" Face at Left");
		}
		else if ( ftLocation[5]>0 || fdLocation[5]>0 )
		{
			tZone = +2;
			tStrength = 0.75;
			if (verbose) printf(" Face at Right");
		}
		else if ( ldLocation[2]>0 )
		{
			tZone = -1;
			tStrength = 0.20;
			if (verbose) printf(" Leg at Center Left");
		}
		else if ( ldLocation[4]>0 )
		{
			tZone = +1;
			tStrength = 0.20;
			if (verbose) printf(" Leg at Center Right");
		}
		else if ( ldLocation[1]>0 )
		{
			tZone = -2;
			tStrength = 0.20;
			if (verbose) printf(" Leg at Left");
		}
		else if ( ldLocation[5]>0 )
		{
			tZone = +2;
			tStrength = 0.20;
			if (verbose) printf(" Leg at Right");
		}
		else if ( ldLocation[0]>0 )
		{
			tZone = -3;
			tStrength = 0.20;
			if (verbose) printf(" Leg at Far Left");
		}
		else if ( ldLocation[6]>0 )
		{
			tZone = +3;
			tStrength = 0.20;
			if (verbose) printf(" Leg at Far Right");
		}

		if ( tZone == pZone ) {sStrength = sStrength + tStrength;}
		else {sStrength = tStrength;}
		pZone = tZone;

		if ( sStrength > strength_th ) 
		{
			tAngle = 15 * (1-abs(tZone)) * abs(tZone) / tZone;
			//sStrength = tStrength;
		}

		data4[0] = sStrength;		// strength of detection: F+L: 0.95, F: 0.75, L: 0.55, fL: 0.25
		data4[1] = tAngle;			// angle that should be sent to robot to turn (degree)
		data4[2] = (double)tZone;	// zone of detection: -3, -2, -1, +1, +2, +3
		pdDataOut.PamIter(3, data4);

		Sleep(200);
		if( _kbhit() > 0 ) { if( _getch() == 0x1b ) break; } 
	}

	return 0;
}


