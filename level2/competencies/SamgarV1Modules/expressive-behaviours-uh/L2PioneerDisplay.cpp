/*
	PROGRAM:	Level 2 of Pioneer's Display Expression Module 
	Author :	K . L. Koay
	Date   :    05 May 2010

*/

#include <Windows.h>
#include "SamgarMainClass.h"

using namespace std;
using namespace yarp;

void behaviour_Display(SamgarModule &DisplayRef,	int behaviour)
{
int ii, jj, kk;  

Bottle BehaviourOut;

  switch (behaviour){

	case 0:  //Migration Out
		printf("Migration Out - 0");
		for (jj=0; jj<10; jj++){ 
			for (ii=0; ii<4; ii++){
				if (ii==0){
					BehaviourOut.addInt(100);BehaviourOut.addInt(0);BehaviourOut.addInt(0);BehaviourOut.addInt(0);
					//sprintf(data1,"#12H #13L #14L #15L \r");
				}
				else if (ii==1){
						BehaviourOut.addInt(0);BehaviourOut.addInt(100);BehaviourOut.addInt(0);BehaviourOut.addInt(0);
						//sprintf(data1,"#12L #13H #14L #15L \r");
					}
					else if (ii==2){
							BehaviourOut.addInt(0);BehaviourOut.addInt(0);BehaviourOut.addInt(100);BehaviourOut.addInt(0);
							//sprintf(data1,"#12L #13L #14H #15L \r");
						}
						else if (ii==3){
								BehaviourOut.addInt(0);BehaviourOut.addInt(0);BehaviourOut.addInt(0);BehaviourOut.addInt(100);
								//sprintf(data1,"#12L #13L #14L #15H \r");
							}
				DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
				BehaviourOut.clear();
				//write(*fd,data1,sizeof(data1));
				Sleep(500);
				//ArUtil::sleep(500);
			}
		}
		BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);
		//sprintf(data1,"#12H #13H #14H #15H \r");
		DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
		BehaviourOut.clear();
		//write(*fd,data1,sizeof(data1));
		Sleep(1000);
		//ArUtil::sleep(1000);
		BehaviourOut.addInt(0);BehaviourOut.addInt(0);BehaviourOut.addInt(0);BehaviourOut.addInt(0);
		//sprintf(data1,"#12L #13L #14L #15L \r");
		DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
		BehaviourOut.clear();
		//write(*fd,data1,sizeof(data1));
		Sleep(500);
		//ArUtil::sleep(500);
		BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);
		//sprintf(data1,"#12H #13H #14H #15H \r");
		DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
		BehaviourOut.clear();
		//write(*fd,data1,sizeof(data1));
		Sleep(1000);
		//ArUtil::sleep(1000);
		BehaviourOut.addInt(0);BehaviourOut.addInt(0);BehaviourOut.addInt(0);BehaviourOut.addInt(0);
		//sprintf(data1,"#12L #13L #14L \r");
		DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
		BehaviourOut.clear();
		Sleep(1000);
		//DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
		//BehaviourOut.clear();
		//write(*fd,data1,sizeof(data1));
		break;

    case 1:  //Migration in
		printf("Migration in -1");
		for (jj=0; jj<10; jj++)
		{  
		  for (ii=0; ii<4; ii++){
			if (ii==0){
				BehaviourOut.addInt(100);BehaviourOut.addInt(0);BehaviourOut.addInt(0);BehaviourOut.addInt(0);
				//sprintf(data1,"#12H #15L #14L #13L \r");
			 }
			else if (ii==1){
					BehaviourOut.addInt(0);BehaviourOut.addInt(0);BehaviourOut.addInt(0);BehaviourOut.addInt(100);
					//sprintf(data1,"#12L #15H #14L #13L \r");
				 }	
				else if (ii==2){
						BehaviourOut.addInt(0);BehaviourOut.addInt(0);BehaviourOut.addInt(100);BehaviourOut.addInt(0);
						//sprintf(data1,"#12L #15L #14H #13L \r");
					}
					else if (ii==3){
							BehaviourOut.addInt(0);BehaviourOut.addInt(100);BehaviourOut.addInt(0);BehaviourOut.addInt(0);
							//sprintf(data1,"#12L #15L #14L #13H \r");
						}
			DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
			BehaviourOut.clear();
			Sleep(500);
			}
		} 
		BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);
		//sprintf(data1,"#12H #13H #14H #15H \r");
		DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
		BehaviourOut.clear();
		Sleep(1000);
		BehaviourOut.addInt(0);BehaviourOut.addInt(0);BehaviourOut.addInt(0);BehaviourOut.addInt(0);
		//sprintf(data1,"#12L #13L #14L #15L \r");
		DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
		BehaviourOut.clear();
		Sleep(500);
		BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);	BehaviourOut.addInt(100);
		//sprintf(data1,"#12H #13H #14H #15H \r");
		DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
		BehaviourOut.clear();
		//write(*fd,data1,sizeof(data1));  
			
      /*
      x[6]=2400;x[5]=1600;x[4]=800;x[3]=0;
      x[2]=2400;x[1]=1600;x[0]=800;
      sprintf(data1,"#12 P%d #13 P%d #14 P%d #15 P%d T100\r", 2400,    0,    0,    0);
      write(*fd,data1,sizeof(data1));
      ArUtil::sleep(500);
      sprintf(data1,"#12 P%d #13 P%d #14 P%d #15 P%d T100\r", 1600, 2400,    0,    0);
      write(*fd,data1,sizeof(data1));
      ArUtil::sleep(500);
      for (jj=0; jj<5; jj++)
      {
	for (ii=0; ii<4; ii++){
	  sprintf(data1,"#12 P%d #13 P%d #14 P%d #15 P%d T100\r", x[3-ii], x[4-ii], x[5-ii], x[6-ii]);
	  write(*fd,data1,sizeof(data1));
	  ArUtil::sleep(500);
	}
      }
      */
/*	
        sprintf(data1,"#12 P%d #13 P%d #14 P%d #15 P%d T100\r",  800, 1600, 2400,    0);
        sprintf(data1,"#12 P%d #13 P%d #14 P%d #15 P%d T100\r",    0,  800, 1600, 2400);
        sprintf(data1,"#12 P%d #13 P%d #14 P%d #15 P%d T100\r", 2400,    0,  800, 1600);
        sprintf(data1,"#12 P%d #13 P%d #14 P%d #15 P%d T100\r", 1600, 2400,    0,  800);
        sprintf(data1,"#12 P%d #13 P%d #14 P%d #15 P%d T100\r",  800, 1600, 2400,    0);
	P12=      
      #12 P2400 #13 P1600 #14 P800 #15 P0
*/        
		break;

	case 2:  //Happy
		printf(" Happy -2 \n");
		for (ii=0; ii<10; ii++){
			BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);
			//sprintf(data1,"#12H #13H #14H #15H \r");
			DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
			BehaviourOut.clear();
			Sleep(500);	
			BehaviourOut.addInt(100);BehaviourOut.addInt(0);BehaviourOut.addInt(100);BehaviourOut.addInt(0);
			//sprintf(data1,"#12H #13L #14H #15L \r");
			DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
			BehaviourOut.clear();
			Sleep(500);
		}
		BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);
		//sprintf(data1,"#12H #13H #14H #15H \r");
		DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
		BehaviourOut.clear();
		Sleep(500);	
		break;

	case 3:  //Excited 
		for (ii=0; ii<20; ii++){
			BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);
			//sprintf(data1,"#12 P%d #13 P2500 #14 P2500 #15 P2500 T100\r",2500);
			DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
			BehaviourOut.clear();
			Sleep(200);
			BehaviourOut.addInt(0);BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);
			//sprintf(data1,"#12 P%d #13 P2500 #14 P2500 #15 P2500 T100\r",0);
			DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
			BehaviourOut.clear();
			Sleep(200);
		}
		BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);BehaviourOut.addInt(100);
		//sprintf(data1,"#12H #13H #14H #15H \r");
		DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
		BehaviourOut.clear();
		Sleep(500);	
		break;    
	case 4: //Bored
			jj=100;
			BehaviourOut.addInt(jj);BehaviourOut.addInt(jj);BehaviourOut.addInt(jj);BehaviourOut.addInt(jj);
			//sprintf(data1,"#12 P%d #13 P%d #14 P%d #15 P%d T100\r", jj, jj, jj, jj );
			//F100,L100,B100, R100, T100,\r,
			DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
			BehaviourOut.clear();
		break;

    case 5:  //Tired
		for (kk=0; kk<6; kk++){
			jj=50;
			for (ii=0; ii<20; ii++){ 
				jj+=120;
				BehaviourOut.addInt((int)(jj/25));BehaviourOut.addInt((int)(jj/25));BehaviourOut.addInt((int)(jj/25));BehaviourOut.addInt((int)(jj/25));
				//sprintf(data1,"#12 P%d #13 P%d #14 P%d #15 P%d T100\r", jj, jj, jj, jj );
				DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
				BehaviourOut.clear();
				//printf("%d going bright\n", jj);
				Sleep(200);
			}
			Sleep(1000);
			    
			for (ii=0; ii<20; ii++){
				jj-=120; 
				BehaviourOut.addInt((int)(jj/25));BehaviourOut.addInt((int)(jj/25));BehaviourOut.addInt((int)(jj/25));BehaviourOut.addInt((int)(jj/25));
				//sprintf(data1,"#12 P%d #13 P%d #14 P%d #15 P%d T100\r", jj, jj, jj, jj );
				DisplayRef.SendBottleData("L2DBOut", BehaviourOut);
				BehaviourOut.clear();
				//printf("%d going dim\n", jj);
				Sleep(200);
				}
			Sleep(1000);
		}
		break;
	default:
			printf("\nNo Such Behaviour, I haven't learn that behaviour yet");
		break;

   }
}


int main(void)
{
	Network yarp;		 //name			  //Category //subcategory	
	SamgarModule Display("PioneerDisplay", "Display", "BaseLights", SamgarModule::interupt);
	Display.AddPortS("L2DBIn");
	Display.AddPortS("L2DBOut");
	
	Bottle BehaviourIn;
	while (1){
		
			if (Display.GetBottleData("L2DBIn", &BehaviourIn, SamgarModule::NoStep)==true)
			{
				//printf("\n ---- I got something %d", BehaviourIn.get(0).asInt());
				behaviour_Display(Display, BehaviourIn.get(0).asInt());
				//printf("\n ---- Done sending Behaviour %d", BehaviourIn.get(0).asInt());
				BehaviourIn.clear();
			}
			Display.SucceedFail(true,100);
			printf(".");
		}
}