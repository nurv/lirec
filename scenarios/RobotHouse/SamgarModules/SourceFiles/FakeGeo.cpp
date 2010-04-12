/*
 programmer : K . Du Casse 
 e-mail     : k.du-casse@herts.ac.uk
 Date of original : 07 april 2010
 Date of last mod : 07 april 2010

 Version 1.0
*/

#include <iostream>
#include <cmath>
#include "SamgarMainClass.h"

using namespace std;
using namespace yarp;

static int i=0;
Bottle OutMessage;
int main () 
{
	Network yarp;
	SamgarModule Fgeo("FakeGeo","FakeGeo","Simple",run); // Cant have spaces or underscores
	Fgeo.AddPortS("FakeGeoOut");
	OutMessage.clear();
	while(1)
	{
		OutMessage.clear();
		puts("optoin 1 : Kitchen");
		puts("optoin 2 : Table");
		puts("optoin 3 : Charge");
		puts("optoin 4 : TV");
		puts("optoin 4 : choose your own");
		cin >> i;
		if(i==1) // kitchen
		{
		OutMessage.addDouble(-0.580);OutMessage.addDouble(-0.444);
		Fgeo.SendBottleData("FakeGeoOut",OutMessage);
		}
		if(i==2) // table
		{
		OutMessage.addDouble(-1.525);OutMessage.addDouble(-3.721);
		Fgeo.SendBottleData("FakeGeoOut",OutMessage);
		}
		if(i==3) // charge
		{
		OutMessage.addDouble(-5.364);OutMessage.addDouble(-4.006);
		Fgeo.SendBottleData("FakeGeoOut",OutMessage);
		}
		if(i==4) // TV
		{
		OutMessage.addDouble(-5.995);OutMessage.addDouble(-2.713);
		Fgeo.SendBottleData("FakeGeoOut",OutMessage);
		}
		if(i==5)
		{
			double Ix,Iy;
			cin>>Ix;
			cin>>Iy;

		OutMessage.addDouble(Ix);OutMessage.addDouble(Iy);
		Fgeo.SendBottleData("FakeGeoOut",OutMessage);
		}
	}
	
 return 0;
}
/*
			img.drawCircle(gridmap.x2idx(0.0)		,gridmap.y2idx(0.0),R2,TColor(0,200,200),1 ); //kitch
			img.drawCircle(gridmap.x2idx(0.535)		,gridmap.y2idx(0.0),R2,TColor(100,0,200),1 );//kitch door
			img.drawCircle(gridmap.x2idx(-0.70)		,gridmap.y2idx(-0.25),R2,TColor(100,0,200),1 );//between kitch and stairs
			img.drawCircle(gridmap.x2idx(-1.6712)	,gridmap.y2idx(0.1687),R2,TColor(100,0,200),1 );
			img.drawCircle(gridmap.x2idx(-1.9734)	,gridmap.y2idx(3.07),R2,TColor(100,0,200),1 ); // 
			img.drawCircle(gridmap.x2idx(-6.3394)	,gridmap.y2idx(2.490),R2,TColor(100,0,200),1 ); // TV
			img.drawCircle(gridmap.x2idx(-5.2394)	,gridmap.y2idx(3.90),R2,TColor(100,0,200),1 ); // small table charging
*/

		//	img.drawCircle(gridmap.x2idx(0.0)		,gridmap.y2idx(0.0),R2,TColor(0,200,200),1 ); //kitch
		//	img.drawCircle(gridmap.x2idx(0.535)		,gridmap.y2idx(0.21),R2,TColor(100,0,200),1 );//kitch door
		//	img.drawCircle(gridmap.x2idx(-0.30)		,gridmap.y2idx(0.18),R2,TColor(100,0,200),1 );//between kitch and stairs
		//	img.drawCircle(gridmap.x2idx(-1.6712)	,gridmap.y2idx(0.1687),R2,TColor(100,0,200),1 );
		//	img.drawCircle(gridmap.x2idx(-1.9734)	,gridmap.y2idx(3.07),R2,TColor(100,0,200),1 );
		//	img.drawCircle(gridmap.x2idx(-6.3394)	,gridmap.y2idx(2.490),R2,TColor(100,0,200),1 );