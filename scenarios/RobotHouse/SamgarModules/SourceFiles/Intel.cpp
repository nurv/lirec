
/*
 programmer : K . Du Casse 
 e-mail     : k.du-casse@herts.ac.uk
 Date of original : 07 april 2010
 Date of last mod : 07 april 2010

 Version 1.0
*/
#include <iostream>
#include <cmath>
#include <string>
#include "SamgarMainClass.h"

using namespace std;
using namespace yarp;



//FILE * PFile;
Bottle A;
string hello = "AIBO";
int main () 
{
	Network yarp;
	SamgarModule AI("Inteligence","Statemachine","Simple",run); // Cant have spaces or underscores
	AI.AddPortS("UserPosIN");
	AI.AddPortS("PosAchived");
	AI.AddPortS("GotoPos");
	AI.AddPortS("Migrate");

	bool didimigrate;
	Bottle WantedPos;
	Bottle BB;
	Bottle Out;
	bool compleate;
	int stage =0;
	string fridge = "Fridge";
	while(1)
	{
		switch(stage)
		{
		case 0:// just wait for a signal for where to move
			if(true==AI.GetBottleData("UserPosIN",&BB))
			{
				WantedPos.clear();
		//		if(BB.get(0).asString().c_str()==fridge)
		//		{
					puts("taken command goto fridge");
					WantedPos.addDouble(0.535);//x
					WantedPos.addDouble(0.21);//y
		//		}
				stage++;
				puts("going to fridge");
				AI.SendBottleData("GotoPos",WantedPos);		
				//OutMessage.addDouble(0.535);OutMessage.addDouble(0.21);
				//Fgeo.SendBottleData("FakeGeoOut",OutMessage);
			}
		break;
		case 1: // moved to loc and start to wait
			if(true==AI.GetBottleData("PosAchived",&BB))
			{
			puts("got to fridge");
			stage++;
			}
		break;
		case 2: 
			if(true==AI.GetBottleData("UserPosIn",&BB))
			{
				WantedPos.clear();
				if(BB.get(0).asString().c_str()=="Table"){WantedPos.addDouble(-1.00);WantedPos.addDouble(-1.00);}
				stage++;
				puts("going to laptop");
				AI.SendBottleData("GotoPos",WantedPos);
			}
		break;
		case 3: // moved to loc and start to wait
			if(true==AI.GetBottleData("PosAchived",&BB))
			{
			puts("got to laptop");
			stage++;
			}
		break;
		case 4:
			
			A.addInt(1);
			AI.SendBottleData("Migrate",A);
			yarp::os::Time::delay(1);// delay anything for a couple of secs			
			stage++;
		 break;
		case 5:
			if(true==AI.GetBottleData("Migrate",&BB))
				{
				puts("Back From migrate");
				stage=0;
				}
		break;
		}
	}
 return 0;
}
