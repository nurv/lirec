/*
 programmer : K . Du Casse & Khenglee Koay
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

void sortwaypoints(void);

	int K=2, J=4, k, j; //K: numb of input/output nodes; J: Numb. of hidden nodes (waypoints).		
	float Target_Pos[2], Robot_Pos[2];
	float WP[20+1][2];
	float Wjk[20][2];//input weight
	float Wij[20][2];//output weight
	float Phi[20];
	float PhiDenominator_Total=0, PhiNumerator_TotalX=0, PhiNumerator_TotalY=0;
	float W_Gaussian =0.2;
	double wantedangle=0;



void sortwaypoints(void)
{
	
//	Robot_Pos[0]=2.5; Robot_Pos[1]=15;
	
	//Setting up the weights of the networks
	for (k=0; k<K; k++)
		for (j=0; j<J; j++)
		{
			Wjk[j][k] = WP[j][k]; //input weight
			if (j==J-1)
					Wij[j][k] = Wjk[j][k]; //set the last output weight to point to itself
				else	
					Wij[j][k] = WP[j+1][k]; //output weight
		}
		
	//Calculating the value for the hidden nodes	
	for (j=0; j<J; j++) 
		{
			Phi[j] = exp(-(((Robot_Pos[0]-Wjk[j][0])*(Robot_Pos[0]-Wjk[j][0]))+((Robot_Pos[1]-Wjk[j][1])*(Robot_Pos[1]-Wjk[j][1])))/(2*(W_Gaussian*W_Gaussian)));
			PhiDenominator_Total = PhiDenominator_Total + Phi[j];
			PhiNumerator_TotalX = PhiNumerator_TotalX + Wij[j][0]*Phi[j];
			PhiNumerator_TotalY = PhiNumerator_TotalY + Wij[j][1]*Phi[j];
		}
		
	//Calculating the output
	Target_Pos[0] = PhiNumerator_TotalX/PhiDenominator_Total;
	Target_Pos[1] = PhiNumerator_TotalY/PhiDenominator_Total;
		

 std::cout << Target_Pos[0]<<" "<< Target_Pos[1]<< "\\n";	

}


int main () 
{
	Network yarp;
	SamgarModule Mapper("Stepper","Navigation","Map",run); // Cant have spaces or underscores
	Mapper.AddPortS("waypointsIn");
	Mapper.AddPortS("StepLocIn");
	Mapper.AddPortS("SpeedAndRotOut");
	// setup ports
	// etc etc
	Bottle Bwaypoint;
	bool compleate;
	while(1)
	{
		if(Mapper.GetBottleData("waypointsIn",&Bwaypoint)==true)
		{
			puts("recived waypoints");
				// fill the waypoints
				J=Bwaypoint.size()/2;
				int internalcounter =0;
				for(int hh =0;hh<J;hh=hh++)
				{
					WP[hh][0]=Bwaypoint.get(internalcounter).asDouble();
					internalcounter++;
					WP[hh][1]=Bwaypoint.get(internalcounter).asDouble();
					internalcounter++;
					printf("waypoint %i X:%f Y:%f",hh,WP[hh][0],WP[hh][1]);
				}
		compleate = false;
		while(compleate==false)
			{
				// get location
				Bottle Bloc;
				if(Mapper.GetBottleData("StepLocIn",&Bloc)==true)
				{
				Robot_Pos[0]	= Bloc.get(2).asDouble();							// these might need to be
				Robot_Pos[1]	= Bloc.get(3).asDouble();							// changed to X and Y 
				double rot		= Bloc.get(1).asDouble();							// match the map
				sortwaypoints();
				
				// get the desired angle

				wantedangle=atan2(Robot_Pos[1]-Target_Pos[1],Robot_Pos[0]-Target_Pos[0]);// might need to inverse this

				if(rot>wantedangle+3||rot<wantedangle-3)
					{
						Bottle BspeedRot;
						BspeedRot.clear();
						BspeedRot.addDouble(50);//speed
						BspeedRot.addDouble(wantedangle);
						Mapper.SendBottleData("SpeedAndRotOut",BspeedRot);
					}
				}
				// if its close		
			  if(abs(Robot_Pos[1])-abs(Target_Pos[1])<0.2)
			  {
				if(abs(Robot_Pos[1])-abs(Target_Pos[1])<0.2)
					{
						Bottle BspeedRot;
						BspeedRot.clear();
						BspeedRot.addDouble(00);//speed
						BspeedRot.addDouble(wantedangle);
						Mapper.SendBottleData("SpeedAndRotOut",BspeedRot);
					}
			  }
			}
	 }
		Mapper.SucceedFail(true,100);
	}
 return 0;
}
