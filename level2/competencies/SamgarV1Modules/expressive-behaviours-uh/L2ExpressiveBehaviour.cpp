#include <Windows.h>
#include <iostream>
#include "SamgarMainClass.h"

using namespace std;
using namespace yarp;

int main(void)
{
	Network yarp;			//name					//Category		//subcategory	
	SamgarModule Behaviour(	"BehaviourExpression",	"Behaviours",	"PhysicalDisplaySound", SamgarModule::run);
	Behaviour.AddPortS("BehaviourOut");
	Bottle ChosenBehaviour;

	int userChoice=0;

	while (userChoice<=6)
	{
		cout<<"Input Behaviour Choice\n";
		cin>>userChoice;
		cout<<"choice"<<userChoice;
		ChosenBehaviour.clear();
		ChosenBehaviour.addInt(userChoice); 
		Behaviour.SendBottleData("BehaviourOut",ChosenBehaviour);
		//ChosenBehaviour.clear();

		//Behaviour.SucceedFail(true,0);
	}
}
