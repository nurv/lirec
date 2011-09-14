/*
 programmer : Amol Deshmukh
 19 Jan 2011

 Version 1.0
*/
#pragma once

#include "SamClass.h"
#include <time.h>
#include <ctype.h>
#include <fstream>
#include "windows.h"
#include <stdio.h>
#include <iostream>
#include <string>
#include <list>
#include <vector>
#include <stdlib.h>
#include <math.h>


#define PI 3.14159265

//Direction Angles in regard to stargazer
#define O 1 //origin
#define N 0
#define NW 315
#define W 270
#define SW 225
#define S 180
#define SE 135
#define E 90
#define NE 45




double DEG2RAD(double x) { return x/57.2957795;}//return x*M_PI/180.0;	}

using namespace std;
using namespace yarp;

class cCell
{
	public:
		cCell(){m_iIndex=0, m_iLandMarkId=0, m_sLabel="", m_dX =0, m_dY=0;};
		cCell(int Indx, int Lid, string sLbl){m_iIndex=Indx, m_iLandMarkId=Lid, m_sLabel=sLbl;};
		int m_iIndex;
		int m_iLandMarkId;
		double m_dX;
		double m_dY;
		string	m_sLabel;
		void setValues(int Indx, int Lid, string sLbl, double x, double y){m_iIndex=Indx, m_iLandMarkId=Lid, m_sLabel=sLbl, m_dX=x, m_dY=y;};
};


class NavigationMap : public SamClass 
{
	private:
	BufferedPort<Bottle> bStarSend; // create buffered ports for bottles like this
	Network yarp;						   // make sure the network is ready

	public:
		//NavigationMap(){};
		//NavigationMap(
		cCell m_cFrom;
		cCell m_cTo;
		double m_iAngleDir;
		double i_X;
		double i_Y;
		double i_ID;
		double i_Angle;

		void SetParam(cCell cFrom, cCell cTo, int AngleDir){m_cFrom = cFrom, m_cTo = cTo, m_iAngleDir=AngleDir;};
		

	void SamInit(void)
	{
	
		RecognisePort("Out");				// name the port to be shown in the gui
		StartModule("/Star");	
		bStarSend.open("/Star_Out");		// open the port
		bStarSend.setReporter(myPortStatus);	// set reporter, this is important

		puts("started stargazer");
	}

	void SamIter(void)
	{
		Bottle& B = bStarSend.prepare();		// prepare the bottle/port
		B.clear();
		B.addInt(i_ID);
		B.addDouble(i_X);
		B.addDouble(i_Y);
		B.addDouble(i_Angle);
		bStarSend.writeStrict();					// add stuff then send
	}
	
};


std::vector<cCell> cellList;
std::vector<NavigationMap> mapList;
cCell cC0, cC1, cC2, cC3, cC4, cC5, cC6, cC7, cC8;
cCell cC9, cC10, cC11, cC12, cC13, cC14, cC15, cC16;
cCell cC17, cC18, cC19, cC20, cC21, cC22, cC23, cC24;
static const int m_iCellCnt=23;

void initCellList();
//void initMapList();
void printCellList();
void printNavMapList();
int getDirAngle(int frmCell, int toCell);
int findIndexfrmLabel(string sLbl);
int findIndexfrmLandmarkID(int Lid);
void convCordXY(double &X, double &Y);
double calAnglesFrmXY(int ang, int Xi, int Yi);
void convGlobalCords(double &X, double &Y, int iLid);
double getX(int iLid);
double getY(int iLid);
int counter=0;


NavigationMap *Nav;


int main (void)
{

ifstream myfile;
initCellList();
//initMapList();

//yarp::os::Time::delay(2);

Nav = new NavigationMap();
Nav->SamInit();


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

//system("StarGazerSample.exe");
yarp::os::Time::delay(2);

int xx=0;


while(1)
{
	double RotAngle=0;
	double LandMarkAngle =0;
	bool flag = false;

	yarp::os::Time::delay(0.2);
	
	MyData = "";
	//myfile.open("C:\\Documents and Settings\\LirecUser\\Desktop\\sarah\\navigation\\StarGazerSample\\log.txt");
	
	myfile.open("log.txt");
	getline(myfile,MyData);

	myfile.clear();
	myfile.close();
	
	
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

	if(x==4&&MyData!="")
	{
		IDnum = MyData.substr(MyData.length()-3,3);
		angle = MyData.substr(Mylist[0],(Mylist[1]-Mylist[0])-1);
		Myx   = MyData.substr(Mylist[1],(Mylist[2]-Mylist[1])-1);
		Myy   = MyData.substr(Mylist[2],(Mylist[3]-Mylist[2])-1);

		//std::cout << " ID found " << IDnum;
		if(IDnum.length()>0&&angle.length()>0&&Myx.length()>0&&Myy.length()>0)
		{
			if(IDnum.length()<7)
			{
				ID = atof(IDnum.c_str());
				double Ang	=	atof(angle.c_str());
				double X	=	atof(Myx.c_str());
				double Y	=	atof(Myy.c_str());
				int id= findIndexfrmLandmarkID(ID);

				//std::cout << " ID found " << id;


				// add the offset of SG position on robot
				//X = X + 30;
				
				//woz 5
					//if(abs(X)-abs(OldX)<20&&abs(Y)-abs(OldY)<20 && OldId==ID)
					//	{
						if(ID!=HUGE_VAL&&Ang!=HUGE_VAL&&X!=HUGE_VAL&&Y!=HUGE_VAL&&id!=-1)
						{
							// ok so got to put silly rotation here with y being the front of the robot and x being side to side
							
							convGlobalCords( X, Y, ID);
							Nav->i_X = X;
							Nav->i_Y = Y;
							Nav->i_ID = ID;

							//to conv landmark angle in range 0-360

							//std::cout << " Angle before" << Ang  <<std::endl;

							if(Ang  < 0 && abs(Ang) <=90)
								Ang = abs(Ang) + 270;
							else if (Ang  < 0 && abs(Ang) > 90)
								Ang = abs(Ang)- 90;
							else if(Ang >= 0)
								Ang = 270 - Ang;


							Nav->i_Angle = DEG2RAD(Ang);

												
							OldX=X;
							OldY=Y;

							//Nav->SamIter();
							
							
							
							//if(count>10){count=0;printf("Data is Fine ID:%s Angle:%s X:%s Y:%s\n",IDnum.c_str(),angle.c_str(),Myx.c_str(),Myy.c_str());}
							//if(count>10){count=0;printf("Data is Fine ID:%f Angle:%f X:%f Y:%f\n",ID,Ang,X,Y);}	
							}
						else
						{
							puts("data is corrupt");
							Nav->i_ID = 0;
							//Nav->i_X = 0;
							//Nav->i_Y = 0;
							//Nav->i_Angle = 0;
						}
						count++;
						if(count>=3)
						{
							Nav->SamIter();
							std::cout << " X,Y,ID, Angle " << Nav->i_X << " " << Nav->i_Y << " " << Nav->i_ID << " " << Nav->i_Angle << endl;
							count=0;
						}
					/*}
					else
						{
						puts("the distance changed to quickly ignoring");
						float Xdiff = abs(OldX)-abs(X);
						float Ydiff = abs(OldY)-abs(Y);
						if(X<OldX){OldX=OldX-(Xdiff/5);}
						else	  {OldX=OldX+(Xdiff/5);}
						if(Y<OldY){OldY=OldY-(Ydiff/5);}
						else	  {OldY=OldY+(Ydiff/5);}	
						}//abs end*/
			
			
			
			}//
			
			
		}
		OldId=ID;				
	}					
					
	MyData.clear();
	//Nav->SamIter();
}//end while
	

//system("pause")

return 0;
}



void initCellList()
{ 
	/*cC0.setValues(0,214,"home",3.0,-4.0);
	cC1.setValues(1,212,"", 1.5, -4.0);
	cC2.setValues(2,226,"", 0.0,-4.0);
	cC3.setValues(3,224,"table1", -1.5, -4.0);
	cC4.setValues(4,258,"door",3.0 ,-2.5);
	cC5.setValues(5,230,"", 1.5,-2.5);
	cC6.setValues(6,292,"", 0.0, -2.5);
	cC7.setValues(7,244,"table2", -1.5, -2.5);
	cC8.setValues(8,256,"",1.5,-1.0);
	cC9.setValues(9,288,"",0.0,-1.0);
	cC10.setValues(10,210,"",-1.5, -1.0);
	cC11.setValues(11,260,"", 0.0, 0.5);
	cC12.setValues(12,240,"table3",-1.5, 0.5);
	cC13.setValues(13,228,"lab1", 1.5, 2.0);
	cC14.setValues(14,242,"lab2",0.0, 2.0);
	cC15.setValues(15,278,"table4", -1.5, 2.0);
	cC16.setValues(16,276,"table5", -1.5, 3.5);*/

	cC0.setValues(0,214,"home", -3.25, 3.75);
	cC1.setValues(1,212,"", -2.0, 3.75);
	cC2.setValues(2,226,"", -0.75, 3.75);
	cC3.setValues(3,224,"table1", 1.25, 3.75);
	cC4.setValues(4,258,"door",-3.25, 2.25);
	cC5.setValues(5,230,"", -2.0, 2.25);
	cC6.setValues(6,292,"", -0.75, 2.25);
	cC7.setValues(7,386,"", 1.25, 2.25);
	cC8.setValues(8,358,"", -2.0,0.75);
	cC9.setValues(9,288,"", -0.75, 0.75);
	cC10.setValues(10,210,"table3",1.25, 0.75);
	cC11.setValues(11,260,"",-0.75,-0.75);
	cC12.setValues(12,352,"",1.25,-0.75);
	cC13.setValues(13,228,"lab1",-2.0,-2.25);
	cC14.setValues(14,242,"lab2",-0.75, -2.25);
	cC15.setValues(15,278,"", 1.25, -2.25);
	cC16.setValues(16,276,"", 1.25,-3.75);

	cC17.setValues(17,384,"", 1.35, 3.0);
	cC18.setValues(18,356,"table2", 2.0, 1.50);
	cC19.setValues(19,374,"table4", 2.0,0);
	cC20.setValues(20,354,"table5", 2.0,-1.50);
	cC21.setValues(21,342,"table6", 2.0 ,-3.0);
	cC22.setValues(22,370,"", -2.4,3.0);
	cC23.setValues(23,306,"", 0.0 ,3.0);
	cC24.setValues(24,164,"", 0.0 ,1.00);



	cellList.push_back(cC0);
	cellList.push_back(cC1);
	cellList.push_back(cC2);
	cellList.push_back(cC3);
	cellList.push_back(cC4);
	cellList.push_back(cC5);
	cellList.push_back(cC6);
	cellList.push_back(cC7);
	cellList.push_back(cC8);
	cellList.push_back(cC9);
	cellList.push_back(cC10);
	cellList.push_back(cC11);
	cellList.push_back(cC12);
	cellList.push_back(cC13);
	cellList.push_back(cC14);
	cellList.push_back(cC15);
	cellList.push_back(cC16);

	cellList.push_back(cC17);
	cellList.push_back(cC18);
	cellList.push_back(cC19);
	cellList.push_back(cC20);
	cellList.push_back(cC21);
	cellList.push_back(cC22);
	cellList.push_back(cC23);
	cellList.push_back(cC24);
	

}

void printCellList()
{
	
	for(int i=0; i<cellList.size();i++ )
		std::cout << "Cell Index " << cellList[i].m_iIndex << " Cell ID " << cellList[i].m_iLandMarkId << "Cell Lable " << cellList[i].m_sLabel << std::endl;

}

void printNavMapList()
{
	for(int i=0; i<mapList.size();i++ )
		std::cout << "Cell from " << mapList[i].m_cFrom.m_iIndex << " Cell to " << mapList[i].m_cTo.m_iIndex << " Dir Angle " << mapList[i].m_iAngleDir << std::endl;

}

int getDirAngle(int frmCell, int toCell)
{
	int cellFound=0;
	cellFound = (frmCell * m_iCellCnt) + toCell;

	if(cellFound >= mapList.size() || cellFound < 0)
		return -1;
	else
		return mapList[cellFound].m_iAngleDir;

}


int findIndexfrmLabel(string sLbl)
{

	for(int i=0; i<cellList.size();i++ )
	{
		if(cellList[i].m_sLabel == sLbl)
			return i;
	}
	return -1;

}


int findIndexfrmLandmarkID(int Lid)
{

	for(int i=0; i<cellList.size();i++ )
	{
		if(cellList[i].m_iLandMarkId == Lid)
			return i;
	}
	return -1;

}

double getX(int Lid)
{

	for(int i=0; i<cellList.size();i++ )
	{
		if(cellList[i].m_iLandMarkId == Lid)
			return cellList[i].m_dX;
	}
	return 0;

}

double getY(int Lid)
{

	for(int i=0; i<cellList.size();i++ )
	{
		if(cellList[i].m_iLandMarkId == Lid)
			return cellList[i].m_dY;
	}
	return 0;

}


double calAnglesFrmXY(int ang, int Xi, int Yi)
{
	int X1 = Xi;
	int Y1 = Yi;

	int X2=0, Y2=0;
	int angle = ang;
	double AngleXY=0, dx, dy;
	
	//finding quadrant values
	switch (angle)
	{
		case 1: X2 = 0; //O
				Y2 = 0;
		break; 

		case 0: X2 = 0; //N
				Y2 = 150;
		break; 

		case 45: X2 = 150; //NE
				 Y2 = 150;
		break; 

		case 90: X2 = 150; //E
				 Y2 = 0;
		break; 
	
		case 135: X2 = 150; //SE
				  Y2 = -150;
		break; 

		case 180: X2 = 0; //S
				  Y2 = -150;
		break; 
	
		case 225: X2 = -150; //SW
				  Y2 = -150;
		break; 

		case 270: X2 = -150; //W
				  Y2 = 0;
		break; 

		case 315: X2 = -150; //NW
				  Y2 = 150;
		break; 

		default: X2 = 0; //O
				 Y2 = 0;
		break;
	
	}

	dx = X2 - X1;
	dy = Y2 - Y1;

	AngleXY = atan2(dx, dy); 
	AngleXY = AngleXY * (180.0/PI); 

	if(AngleXY   < 0 )
		AngleXY  = AngleXY + 360;

	return AngleXY;


	
}


void convCordXY(double &X, double &Y)
{
	int CX = X;
	int CY = Y;
	int temp;
	
	//need to swap X, Y for corrected orientation of sensor
	temp = CY; 
	CY = CX;
	CX = -(temp);

/*
	//keeping variables in bounds 150,-150, 
	//this basically keeps the range for X, Y cordinates within +/- 150cm from sensor
	if(CX>=0 && abs(CX)>=150)
		CX = 150;

	if(CX<=0 && abs(CX)>=150)
		CX = -150;

	if(CY>=0 && abs(CY)>=150)
		CY = 150;

	if(CY<=0 && abs(CY)>=150)
		CY = -150;
*/
	//final coversion of X & Y
	X = CX;
	Y = CY;


	//std::cout<< " ID " << State::getInstance()->LandMarkID;
	//std::cout<< " X, Y " << State::getInstance()->LandMarkX << " " << State::getInstance()->LandMarkY;// <<std::endl;
	//std::cout<< " Conv X, Y " << State::getInstance()->ConvX << " " << State::getInstance()->ConvY <<std::endl;
	
}

void convGlobalCords(double &X, double &Y, int iLid)
{
	convCordXY(X, Y);//just swap x,y
	double dx = getX(iLid);
	double dy = getY(iLid);

	X = dx - (X/100.0);
	Y = dy - (Y/100.0);

}