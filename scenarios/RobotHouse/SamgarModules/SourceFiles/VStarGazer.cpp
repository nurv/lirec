/*
 programmer : K . Du Casse 
 e-mail     : k.du-casse@herts.ac.uk
 Date of original : 07 april 2010
 Date of last mod : 07 april 2010

 Version 1.0
*/


#include "windows.h"
#include "SamgarMainClass.h"
#include <stdio.h>
#include <iostream>
#include <string>
#include <list>
using namespace std;
using namespace yarp;

#define M_PI2 3.14159265358979323846264338327950288

void StartupComm(void);
bool writeCom(char* outputData,const unsigned int& sizeBuffer,unsigned long& length);
bool readCom(char* inputData2,const unsigned int& sizeBuffer2,unsigned long& length2);
void CloseComm(void);
 double DEG2RAD(double x);// { return x*M_PI/180.0;	}


HANDLE hComm;
DCB dcb = {0};
COMMTIMEOUTS comTimeOut;   
int Bad =0;
int counter=0;

double DEG2RAD(double x) { return x*M_PI2/180.0;	}


int main (void)
{
Network yarp;
Bottle TheMessage;
Bottle TheAngMessage;

StartupComm();

SamgarModule StarGazer("StarGazer","Navigation","Camera",run); // Cant have spaces or underscores
StarGazer.AddPortS("LocOut");
StarGazer.AddPortS("AngOut");



//yarp::os::Time::delay(2);
char mess[255];//was 255
unsigned int lenBuff = 255;//was 255;
unsigned long lenMessage;
string MyData;
int Mylist[5];
bool CorrectData;
int x;
bool istheredata;
string IDnum;// = MyData.substr(3,Mylist[0]-3);
string angle;// = MyData.substr(Mylist[0],(Mylist[1]-Mylist[0])-1);
string Myx  ;// = MyData.substr(Mylist[1],(Mylist[2]-Mylist[1])-1);
string Myy  ;// = MyData.substr(Mylist[2],(Mylist[3]-Mylist[2])-1);
puts("going into while loop");
int count=0;
static double oldAng=0;
static double OldX=0;
static double OldY=0;
static double OldId=0;
double ID=0;
yarp::os::Time::delay(2);

int xx=0;
while(readCom(mess,lenBuff,lenMessage)&&xx<20)
{
	xx++;
	istheredata = readCom(mess,lenBuff,lenMessage);
}

while(1)
{
	//yarp::os::Time::delay(0.5);
	try
	{
	istheredata = readCom(mess,lenBuff,lenMessage);
	MyData = mess;
	TheMessage.clear();
	TheAngMessage.clear();
	x=0;
	CorrectData=true;
	for(int yy =0;yy < MyData.length();yy++)
		{
		if(MyData[yy]=='|')
			{
			Mylist[x]=yy+1;
			x++;
			}
		}
	if(x==4&&MyData[0]=='~'&&istheredata==true)
		{
		IDnum = MyData.substr(3,Mylist[0]-3);
		angle = MyData.substr(Mylist[0],(Mylist[1]-Mylist[0])-1);
		Myx   = MyData.substr(Mylist[1],(Mylist[2]-Mylist[1])-1);
		Myy   = MyData.substr(Mylist[2],(Mylist[3]-Mylist[2])-1);
		if(IDnum.length()>0&&angle.length()>0&&Myx.length()>0&&Myy.length()>0)
		{
			if(IDnum.length()<7)
			{
				ID = atof(IDnum.c_str());
				double Ang	=	atof(angle.c_str());
				double X	=	atof(Myx.c_str());
				double Y	=	atof(Myy.c_str());
					//woz 5
					if(abs(X)-abs(OldX)<100&&abs(Y)-abs(OldY)<100 && OldId==ID)
						{
						if(ID!=HUGE_VAL&&Ang!=HUGE_VAL&&X!=HUGE_VAL&&Y!=HUGE_VAL)
							{
							// ok so got to put silly rotation here with y being the front of the robot and x being side to side
							TheMessage.addDouble(ID);
							TheMessage.addDouble(Ang);
							TheMessage.addDouble(X);
							TheMessage.addDouble(Y);
							OldX=X;
							OldY=Y;
							TheAngMessage.addDouble(Ang);//"AngOut"

							StarGazer.SendBottleData("LocOut",TheMessage);
							StarGazer.SendBottleData("AngOut",TheAngMessage);

							counter=0;
							count++;
							//if(count>10){count=0;printf("Data is Fine ID:%s Angle:%s X:%s Y:%s\n",IDnum.c_str(),angle.c_str(),Myx.c_str(),Myy.c_str());}
							//if(count>10){count=0;printf("Data is Fine ID:%f Angle:%f X:%f Y:%f\n",ID,Ang,X,Y);}	
							}
						else
							{
							puts("data is corrupt");
							}
					}//abs end
					else
						{
						puts("the distance changed to quickly ignoring");
						float Xdiff = abs(OldX)-abs(X);
						float Ydiff = abs(OldY)-abs(Y);
						if(X<OldX){OldX=OldX-(Xdiff/5);}
						else	  {OldX=OldX+(Xdiff/5);}
						if(Y<OldY){OldY=OldY-(Ydiff/5);}
						else	  {OldY=OldY+(Ydiff/5);}	
						}
			}
				OldId=ID;
				StarGazer.SucceedFail(true,000);
			}
			else{CorrectData=false;}
		} 
		else{CorrectData=false;}//length


	if(CorrectData==false)
		{
		StarGazer.SucceedFail(false,000);
		printf("there is no data or its corrupted \n");
		}

	MyData.clear();
}
	catch(exception& e)
			{
				cout << "except:" << e.what()<<endl;
			}

}

CloseComm();

return 0;
}


void CloseComm(void)
{
if(CloseHandle(hComm) == 0)    // Call this function to close port.
    {
		printf("cant close port \n");
    }    
}

void StartupComm(void)
{
hComm = CreateFile("COM1", GENERIC_READ | GENERIC_WRITE,0,NULL,OPEN_EXISTING,0,NULL);   

if (hComm == INVALID_HANDLE_VALUE)	{ Bad =1;printf("Cant open handler \n");}

if (GetCommState(hComm,&dcb) == 0)	{ Bad =1;printf("Cant Get data for handler \n");}

dcb.BaudRate=115200;
dcb.ByteSize=8;
dcb.StopBits=1;
dcb.Parity=0;

 if (SetCommState(hComm, &dcb)) // update the new info
 {
	 printf("Cant give new data for handler \n");
	Bad= 1;
 }



comTimeOut.ReadIntervalTimeout = 3;
comTimeOut.ReadTotalTimeoutMultiplier = 3;
comTimeOut.ReadTotalTimeoutConstant = 2;
comTimeOut.WriteTotalTimeoutMultiplier = 3;
comTimeOut.WriteTotalTimeoutConstant = 2;
SetCommTimeouts(hComm,&comTimeOut);

puts("finnished starting up serial for use with star"); 
}

bool readCom(char* inputData2,const unsigned int& sizeBuffer2,unsigned long& length55)
{
 if (ReadFile(hComm,inputData2,sizeBuffer2,&length55,NULL) == 0)              // pointer to structure for data
   {
	//  printf("couldn't read from port \n");
    return FALSE;
  }
  if (length55 > 0)
  {
    inputData2[length55] = NULL; // Assign end flag of message.
	return TRUE;  
  } 
  return TRUE;
}

bool writeCom(char* outputData,const unsigned int& sizeBuffer,unsigned long& length)
{
 // if (length > 0)
 // {
  //  if (WriteFile(hComm,  outputData,    sizeBuffer,              &length,NULL) == 0)      
  //  {
//		printf("couldn't write to port \n");
  //    return FALSE;
 //   }
 //   return TRUE;
 // }
  printf("length is zero so nothing to send on port \n");
  return FALSE;
}