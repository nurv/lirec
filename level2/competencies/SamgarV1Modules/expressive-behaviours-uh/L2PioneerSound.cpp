/*
	PROGRAM:	Level 2 of Pioneer's Sound Expression Module 
	Author :	K . L. Koay
	Date   :    05 May 2010
*/

#include <Windows.h>
#include "SamgarMainClass.h"

using namespace std;
using namespace yarp;

void behaviour_Sound(SamgarModule &SoundRef, int behaviour)
{
char tune[40];
Bottle BehaviourOut;

  switch (behaviour) {
	case  0:	//'MigrationOutof':
	  break;

    case  1:	//'MigrationInto':
	  break;
   
    case  2:	//'Happy':
		//duration in ms num.*25	//tone
		BehaviourOut.addInt(20);	BehaviourOut.addInt(77);
		BehaviourOut.addInt(10);	BehaviourOut.addInt(62);
		BehaviourOut.addInt(20);	BehaviourOut.addInt(70);
		BehaviourOut.addInt(20);	BehaviourOut.addInt(77);
		BehaviourOut.addInt(10);	BehaviourOut.addInt(62);
		BehaviourOut.addInt(20);	BehaviourOut.addInt(70);
		SoundRef.SendBottleData("L2SBOut", BehaviourOut);
		BehaviourOut.clear();
	  break;
    case  3:	//Excited':
		//duration in ms num.*25	//tone
		BehaviourOut.addInt(10);	BehaviourOut.addInt(78);
		BehaviourOut.addInt(10);	BehaviourOut.addInt(80);
		BehaviourOut.addInt(10);	BehaviourOut.addInt(83);
		BehaviourOut.addInt(10);	BehaviourOut.addInt(78);
		BehaviourOut.addInt(10);	BehaviourOut.addInt(80);
		BehaviourOut.addInt(10);	BehaviourOut.addInt(80);
		SoundRef.SendBottleData("L2SBOut", BehaviourOut);
		BehaviourOut.clear();
	  break;
    case  4:	//'Bored':
		//duration in ms num.*25	//tone
		BehaviourOut.addInt(20);	BehaviourOut.addInt(70);
		BehaviourOut.addInt(30);	BehaviourOut.addInt(66);
		BehaviourOut.addInt(100);	BehaviourOut.addInt(60);
		SoundRef.SendBottleData("L2SBOut", BehaviourOut);
		BehaviourOut.clear();
	  break;
    case  5:	//'Tired':
		//duration in ms num.*25	//tone
		BehaviourOut.addInt(45);	BehaviourOut.addInt(130);
		BehaviourOut.addInt(40);	BehaviourOut.addInt(64);
		BehaviourOut.addInt(50);	BehaviourOut.addInt(129);
		BehaviourOut.addInt(60);	BehaviourOut.addInt(128);
		SoundRef.SendBottleData("L2SBOut", BehaviourOut);
		BehaviourOut.clear();
	  break;
	 default:
		printf("\nNo Such Behaviour, I haven't learn that behaviour yet");
	break;
  }
}

int main(void)
{
	Network yarp;		//name			//Category //subcategory	
	SamgarModule Sound("PioneerSound", "Sound", "BasePiezoBuzzer", SamgarModule::interupt);
	Sound.AddPortS("L2SBIn");
	Sound.AddPortS("L2SBOut");
	
	Bottle BehaviourIn;
	while (1){
		if (Sound.GetBottleData("L2SBIn", &BehaviourIn, SamgarModule::NoStep)==true)
		{
			//printf("\n ---- I got something %d", BehaviourIn.get(0).asInt());
			behaviour_Sound(Sound, BehaviourIn.get(0).asInt());
			//printf("\n ---- Done sending Behaviour %d", BehaviourIn.get(0).asInt());
			BehaviourIn.clear();
		}
		Sound.SucceedFail(true,100);
	}
}