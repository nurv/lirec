/*
 programmer : K . Du Casse & Khenglee Koay
 e-mail     : k.du-casse@herts.ac.uk
 Date of original : 07 april 2010
 Date of last mod : 07 april 2010

 Version 1.0
*/

#include <iostream>
#include <cmath>

	float Target_Pos[2], Robot_Pos[2];
	float WP[20+1][2];
	float Wjk[20][2];//input weight
	float Wij[20][2];//output weight
	float Phi[20];
	float PhiDenominator_Total=0, PhiNumerator_TotalX=0, PhiNumerator_TotalY=0;
	float W_Gaussian =0.5;
	int K=2, J=4, k, j; //K: numb of input/output nodes; J: Numb. of hidden nodes (waypoints).	


void WorkOutWaypoints(void)
{
	
	
//	WP[0][0]=1; WP[0][1]=4;
	// get bottle data to input into waypoints
	// also get the size of the list so we can set J;

	Robot_Pos[0]=2.5; Robot_Pos[1]=15;
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
			/// if phi 
			
			PhiDenominator_Total = PhiDenominator_Total + Phi[j];

			if(PhiDenominator_Total==0){puts("devide by zero");break;}

			PhiNumerator_TotalX = PhiNumerator_TotalX + Wij[j][0]*Phi[j];
			PhiNumerator_TotalY = PhiNumerator_TotalY + Wij[j][1]*Phi[j];
		}
	//Calculating the output
	Target_Pos[0] = PhiNumerator_TotalX/PhiDenominator_Total;
	Target_Pos[1] = PhiNumerator_TotalY/PhiDenominator_Total;
		

 std::cout << Target_Pos[0]<<" "<< Target_Pos[1]<< "\\n";	


}


int main (int argc, char * const argv[]) 
{

 return 0;
}
