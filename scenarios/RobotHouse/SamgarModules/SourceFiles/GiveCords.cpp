/*
 programmer : K . Du Casse 
 e-mail     : k.du-casse@herts.ac.uk
 Date of original : 07 april 2010
 Date of last mod : 07 april 2010

 Version 1.0
*/
//// translator for abstract places to real cords

#include "SamgarMainClass.h"
using namespace std;
using namespace yarp;


string InputString;
double X,Y;

int main (void)
{

	Network yarp;
//	SamgarModule Mapper("Cords","cord","Map",interupt); // Cant have spaces or underscores
	SamgarModule Mapper("Cords","cord","Map",interupt);
	Mapper.AddPortS("PlaceIn");
	Mapper.AddPortS("CordsOut");
	Bottle InMessage;
	Bottle OutMessage;


	while(1)
	{

	// put something here so we can manually set the destination.
	//	InputString
	//	if(
	//	if(Mapper.GetBottleData("PlaceIn",&OutMessage)==true)
	//	{
	//		InputString==OutMessage.get(0).asString().c_str();

			 if(InputString=="tv")	{X=-752;Y=-282;}
		else if(InputString=="sofa"){X=-671;Y=-340;}
		else if(InputString=="middle"){X=-511;Y=-304;}
		else if(InputString=="kitchen"){X=0;Y=0;}
		else if(InputString=="kitchen"){X=0;Y=0;}
		else if(InputString=="kitchen"){X=0;Y=0;}
		else if(InputString=="kitchen"){X=0;Y=0;}
		else if(InputString=="kitchen"){X=0;Y=0;}
		else if(InputString=="kitchen"){X=0;Y=0;}
		else if(InputString=="kitchen"){X=0;Y=0;}
		else if(InputString=="kitchen"){X=0;Y=0;}
		else if(InputString=="kitchen"){X=0;Y=0;}
		else if(InputString=="kitchen"){X=0;Y=0;}
		else if(InputString=="kitchen"){X=0;Y=0;}
		else if(InputString=="kitchen"){X=0;Y=0;}
		else {/* we should send a error signal here */}
		
		OutMessage.clear();
		OutMessage.addDouble(X);
		OutMessage.addDouble(Y);

		Mapper.SendBottleData("CordsOut",OutMessage);
		}		
		Mapper.SucceedFail(true,100);
	}
}