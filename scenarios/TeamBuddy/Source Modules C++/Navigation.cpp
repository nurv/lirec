#pragma once

#include <vector>
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <math.h>
#include <string>

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

using namespace std;

class cCell
{
	public:
		cCell(){m_iIndex=0, m_iLandMarkId=0, m_sLabel="";};
		cCell(int Indx, int Lid, string sLbl, int X, int Y)
		{
		 m_iIndex=Indx; 
		 m_iLandMarkId=Lid;
		 m_sLabel=sLbl;
		 i_X = X;
		 i_Y = Y;
		};

		int m_iIndex;
		int m_iLandMarkId;
		string	m_sLabel;
		int i_X;
		int i_Y;
		void setValues(int Indx, int Lid, string sLbl, int X, int Y)
		{
		 m_iIndex=Indx; 
		 m_iLandMarkId=Lid;
		 m_sLabel=sLbl;
		 i_X = X;
		 i_Y = Y;
		};
};

class NavigationMap
{
	public:
		NavigationMap(cCell cFrom, cCell cTo, int AngleDir){m_cFrom = cFrom, m_cTo = cTo, m_iAngleDir=AngleDir;};
		cCell m_cFrom;
		cCell m_cTo;
		int m_iAngleDir;
};




class Navigation 
{
	public:
		Navigation();
		~Navigation();
		
		std::vector<cCell> cellList;
		std::vector<NavigationMap> mapList;
		cCell cC0, cC1, cC2, cC3, cC4, cC5, cC6, cC7, cC8;
		cCell cC9, cC10, cC11, cC12, cC13, cC14, cC15, cC16;
		static const int m_iCellCnt=17;
	
		void initCellList();
		void initMapList();
		void printCellList();
		void printNavMapList();
		int getDirAngle(int frmCell, int toCell);
		int findIndexfrmLabel(string sLbl);
		int findIndexfrmLandmarkID(int Lid);
		void convCordXY(int &X, int &Y);
		double calAnglesFrmXY(int ang, int Xi, int Yi);
		

};




//double DEG2RAD(double x);// { return x*M_PI/180.0;	}
Navigation::Navigation()
{
	initCellList();
	initMapList();
}

Navigation::~Navigation()
{


}

void Navigation::initCellList()
{ 
	cC0.setValues(0,214,"home", -3.25, 3.75);
	cC1.setValues(1,212,"", -2.25, 3.75);
	cC2.setValues(2,226,"", -0.75, 3.75);
	cC3.setValues(3,224,"table1", 1.25, 3.75);
	cC4.setValues(4,258,"door",-3.25, 2.25);
	cC5.setValues(5,230,"", -2.25, 2.25);
	cC6.setValues(6,292,"", -0.75, 2.25);
	cC7.setValues(7,386,"table2". 1.25, 2.25);//changed from 244
	cC8.setValues(8,256,"", -2.25,0.75);
	cC9.setValues(9,288,"", -0.75, 0.75);
	cC10.setValues(10,210,"table3",1.25, 0.75);
	cC11.setValues(11,260,"",-0.75,-0.75);
	cC12.setValues(12,240,"table4",1.25,-0.75);
	cC13.setValues(13,228,"lab1",-2.25,-2.25);
	cC14.setValues(14,242,"lab2",-0.75, -2.25);
	cC15.setValues(15,278,"table5", 1.25, -2.25);
	cC16.setValues(16,276,"table6", 1.25,-3.75);

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
	

}


void Navigation::initMapList()
{
	
	//Cell cC0
	mapList.push_back(NavigationMap(cC0,cC0,O));
	mapList.push_back(NavigationMap(cC0,cC1,W));
	mapList.push_back(NavigationMap(cC0,cC2,W));
	mapList.push_back(NavigationMap(cC0,cC3,W));
	mapList.push_back(NavigationMap(cC0,cC4,N));
	mapList.push_back(NavigationMap(cC0,cC5,NW));
	mapList.push_back(NavigationMap(cC0,cC6,NW));
	mapList.push_back(NavigationMap(cC0,cC7,NW));
	mapList.push_back(NavigationMap(cC0,cC8,NW));
	mapList.push_back(NavigationMap(cC0,cC9,NW));
	mapList.push_back(NavigationMap(cC0,cC10,NW));
	mapList.push_back(NavigationMap(cC0,cC11,NW));
	mapList.push_back(NavigationMap(cC0,cC12,NW));
	mapList.push_back(NavigationMap(cC0,cC13,NW));
	mapList.push_back(NavigationMap(cC0,cC14,NW));
	mapList.push_back(NavigationMap(cC0,cC15,NW));
	mapList.push_back(NavigationMap(cC0,cC16,NW));
	
	//Cell cC1
	mapList.push_back(NavigationMap(cC1,cC0,E));
	mapList.push_back(NavigationMap(cC1,cC1,O));
	mapList.push_back(NavigationMap(cC1,cC2,W));
	mapList.push_back(NavigationMap(cC1,cC3,W));
	mapList.push_back(NavigationMap(cC1,cC4,NE));
	mapList.push_back(NavigationMap(cC1,cC5,N));
	mapList.push_back(NavigationMap(cC1,cC6,NW));
	mapList.push_back(NavigationMap(cC1,cC7,NW));
	mapList.push_back(NavigationMap(cC1,cC8,N));
	mapList.push_back(NavigationMap(cC1,cC9,NW));
	mapList.push_back(NavigationMap(cC1,cC10,NW));
	mapList.push_back(NavigationMap(cC1,cC11,NW));
	mapList.push_back(NavigationMap(cC1,cC12,NW));
	mapList.push_back(NavigationMap(cC1,cC13,N));
	mapList.push_back(NavigationMap(cC1,cC14,NW));
	mapList.push_back(NavigationMap(cC1,cC15,NW));
	mapList.push_back(NavigationMap(cC1,cC16,NW));
	
	//Cell cC2
	mapList.push_back(NavigationMap(cC2,cC0,E));
	mapList.push_back(NavigationMap(cC2,cC1,E));
	mapList.push_back(NavigationMap(cC2,cC2,O));
	mapList.push_back(NavigationMap(cC2,cC3,W));
	mapList.push_back(NavigationMap(cC2,cC4,NE));
	mapList.push_back(NavigationMap(cC2,cC5,NE));
	mapList.push_back(NavigationMap(cC2,cC6,N));
	mapList.push_back(NavigationMap(cC2,cC7,NW));
	mapList.push_back(NavigationMap(cC2,cC8,NE));
	mapList.push_back(NavigationMap(cC2,cC9,N));
	mapList.push_back(NavigationMap(cC2,cC10,NW));
	mapList.push_back(NavigationMap(cC2,cC11,N));
	mapList.push_back(NavigationMap(cC2,cC12,NW));
	mapList.push_back(NavigationMap(cC2,cC13,NE));
	mapList.push_back(NavigationMap(cC2,cC14,N));
	mapList.push_back(NavigationMap(cC2,cC15,NW));
	mapList.push_back(NavigationMap(cC2,cC16,NW));
	
	//Cell cC3
	mapList.push_back(NavigationMap(cC3,cC0,E));
	mapList.push_back(NavigationMap(cC3,cC1,E));
	mapList.push_back(NavigationMap(cC3,cC2,E));
	mapList.push_back(NavigationMap(cC3,cC3,O));
	mapList.push_back(NavigationMap(cC3,cC4,NE));
	mapList.push_back(NavigationMap(cC3,cC5,NE));
	mapList.push_back(NavigationMap(cC3,cC6,NE));
	mapList.push_back(NavigationMap(cC3,cC7,N));
	mapList.push_back(NavigationMap(cC3,cC8,NE));
	mapList.push_back(NavigationMap(cC3,cC9,NE));
	mapList.push_back(NavigationMap(cC3,cC10,N));
	mapList.push_back(NavigationMap(cC3,cC11,NE));
	mapList.push_back(NavigationMap(cC3,cC12,N));
	mapList.push_back(NavigationMap(cC3,cC13,NE));
	mapList.push_back(NavigationMap(cC3,cC14,NE));
	mapList.push_back(NavigationMap(cC3,cC15,N));
	mapList.push_back(NavigationMap(cC3,cC16,N));

	//Cell cC4
	mapList.push_back(NavigationMap(cC4,cC0,S));
	mapList.push_back(NavigationMap(cC4,cC1,SW));
	mapList.push_back(NavigationMap(cC4,cC2,SW));
	mapList.push_back(NavigationMap(cC4,cC3,SW));
	mapList.push_back(NavigationMap(cC4,cC4,O));
	mapList.push_back(NavigationMap(cC4,cC5,W));
	mapList.push_back(NavigationMap(cC4,cC6,W));
	mapList.push_back(NavigationMap(cC4,cC7,W));
	mapList.push_back(NavigationMap(cC4,cC8,W));//avoid going to obst
	mapList.push_back(NavigationMap(cC4,cC9,W));
	mapList.push_back(NavigationMap(cC4,cC10,NW));
	mapList.push_back(NavigationMap(cC4,cC11,W));
	mapList.push_back(NavigationMap(cC4,cC12,W));
	mapList.push_back(NavigationMap(cC4,cC13,W));
	mapList.push_back(NavigationMap(cC4,cC14,W));
	mapList.push_back(NavigationMap(cC4,cC15,W));
	mapList.push_back(NavigationMap(cC4,cC16,W));

	//Cell cC5
	mapList.push_back(NavigationMap(cC5,cC0,SE));
	mapList.push_back(NavigationMap(cC5,cC1,S));
	mapList.push_back(NavigationMap(cC5,cC2,SW));
	mapList.push_back(NavigationMap(cC5,cC3,SW));
	mapList.push_back(NavigationMap(cC5,cC4,E));
	mapList.push_back(NavigationMap(cC5,cC5,O));
	mapList.push_back(NavigationMap(cC5,cC6,W));
	mapList.push_back(NavigationMap(cC5,cC7,W));
	mapList.push_back(NavigationMap(cC5,cC8,W));//avoid going to obst
	mapList.push_back(NavigationMap(cC5,cC9,W));
	mapList.push_back(NavigationMap(cC5,cC10,NW));
	mapList.push_back(NavigationMap(cC5,cC11,W));
	mapList.push_back(NavigationMap(cC5,cC12,NW));
	mapList.push_back(NavigationMap(cC5,cC13,W));
	mapList.push_back(NavigationMap(cC5,cC14,NW));
	mapList.push_back(NavigationMap(cC5,cC15,NW));
	mapList.push_back(NavigationMap(cC5,cC16,NW));

	//Cell cC6
	mapList.push_back(NavigationMap(cC6,cC0,SE));
	mapList.push_back(NavigationMap(cC6,cC1,SE));
	mapList.push_back(NavigationMap(cC6,cC2,S));
	mapList.push_back(NavigationMap(cC6,cC3,SW));
	mapList.push_back(NavigationMap(cC6,cC4,E));
	mapList.push_back(NavigationMap(cC6,cC5,E));
	mapList.push_back(NavigationMap(cC6,cC6,O));
	mapList.push_back(NavigationMap(cC6,cC7,W));
	mapList.push_back(NavigationMap(cC6,cC8,W));//avoid going to obst
	mapList.push_back(NavigationMap(cC6,cC9,W));
	mapList.push_back(NavigationMap(cC6,cC10,NW));
	mapList.push_back(NavigationMap(cC6,cC11,W));
	mapList.push_back(NavigationMap(cC6,cC12,NW));
	mapList.push_back(NavigationMap(cC6,cC13,W));
	mapList.push_back(NavigationMap(cC6,cC14,NW));
	mapList.push_back(NavigationMap(cC6,cC15,NW));
	mapList.push_back(NavigationMap(cC6,cC16,NW));

	//Cell cC7
	mapList.push_back(NavigationMap(cC7,cC0,SE));
	mapList.push_back(NavigationMap(cC7,cC1,SE));
	mapList.push_back(NavigationMap(cC7,cC2,SE));
	mapList.push_back(NavigationMap(cC7,cC3,S));
	mapList.push_back(NavigationMap(cC7,cC4,E));
	mapList.push_back(NavigationMap(cC7,cC5,E));
	mapList.push_back(NavigationMap(cC7,cC6,E));
	mapList.push_back(NavigationMap(cC7,cC7,O));
	mapList.push_back(NavigationMap(cC7,cC8,N));//avoid going to obst
	mapList.push_back(NavigationMap(cC7,cC9,N));
	mapList.push_back(NavigationMap(cC7,cC10,N));
	mapList.push_back(NavigationMap(cC7,cC11,N));
	mapList.push_back(NavigationMap(cC7,cC12,N));
	mapList.push_back(NavigationMap(cC7,cC13,NE));
	mapList.push_back(NavigationMap(cC7,cC14,NE));
	mapList.push_back(NavigationMap(cC7,cC15,N));
	mapList.push_back(NavigationMap(cC7,cC16,N));
	
	//Cell cC8
	mapList.push_back(NavigationMap(cC8,cC0,SE));
	mapList.push_back(NavigationMap(cC8,cC1,S));
	mapList.push_back(NavigationMap(cC8,cC2,SW));
	mapList.push_back(NavigationMap(cC8,cC3,SW));
	mapList.push_back(NavigationMap(cC8,cC4,SE));
	mapList.push_back(NavigationMap(cC8,cC5,S));
	mapList.push_back(NavigationMap(cC8,cC6,SW));
	mapList.push_back(NavigationMap(cC8,cC7,SW));
	mapList.push_back(NavigationMap(cC8,cC8,O));//avoid going to obst
	mapList.push_back(NavigationMap(cC8,cC9,W));
	mapList.push_back(NavigationMap(cC8,cC10,W));
	mapList.push_back(NavigationMap(cC8,cC11,W));
	mapList.push_back(NavigationMap(cC8,cC12,W));
	mapList.push_back(NavigationMap(cC8,cC13,W));
	mapList.push_back(NavigationMap(cC8,cC14,W));
	mapList.push_back(NavigationMap(cC8,cC15,W));
	mapList.push_back(NavigationMap(cC8,cC16,W));

	//Cell cC9
	mapList.push_back(NavigationMap(cC9,cC0,SE));
	mapList.push_back(NavigationMap(cC9,cC1,SE));
	mapList.push_back(NavigationMap(cC9,cC2,S));
	mapList.push_back(NavigationMap(cC9,cC3,SW));
	mapList.push_back(NavigationMap(cC9,cC4,SE));
	mapList.push_back(NavigationMap(cC9,cC5,SE));
	mapList.push_back(NavigationMap(cC9,cC6,S));
	mapList.push_back(NavigationMap(cC9,cC7,SW));
	mapList.push_back(NavigationMap(cC9,cC8,E));//avoid going to obst
	mapList.push_back(NavigationMap(cC9,cC9,O));
	mapList.push_back(NavigationMap(cC9,cC10,W));
	mapList.push_back(NavigationMap(cC9,cC11,W));
	mapList.push_back(NavigationMap(cC9,cC12,W));
	mapList.push_back(NavigationMap(cC9,cC13,W));
	mapList.push_back(NavigationMap(cC9,cC14,W));
	mapList.push_back(NavigationMap(cC9,cC15,W));
	mapList.push_back(NavigationMap(cC9,cC16,W));

	//Cell cC10
	mapList.push_back(NavigationMap(cC10,cC0,SE));
	mapList.push_back(NavigationMap(cC10,cC1,SE));
	mapList.push_back(NavigationMap(cC10,cC2,SE));
	mapList.push_back(NavigationMap(cC10,cC3,S));
	mapList.push_back(NavigationMap(cC10,cC4,SE));
	mapList.push_back(NavigationMap(cC10,cC5,SE));
	mapList.push_back(NavigationMap(cC10,cC6,SE));
	mapList.push_back(NavigationMap(cC10,cC7,S));
	mapList.push_back(NavigationMap(cC10,cC8,E));//avoid going to obst
	mapList.push_back(NavigationMap(cC10,cC9,E));
	mapList.push_back(NavigationMap(cC10,cC10,O));
	mapList.push_back(NavigationMap(cC10,cC11,NE));
	mapList.push_back(NavigationMap(cC10,cC12,N));
	mapList.push_back(NavigationMap(cC10,cC13,NE));
	mapList.push_back(NavigationMap(cC10,cC14,NE));
	mapList.push_back(NavigationMap(cC10,cC15,N));
	mapList.push_back(NavigationMap(cC10,cC16,N));

	//Cell cC11
	mapList.push_back(NavigationMap(cC11,cC0,SW));
	mapList.push_back(NavigationMap(cC11,cC1,SW));
	mapList.push_back(NavigationMap(cC11,cC2,SW));
	mapList.push_back(NavigationMap(cC11,cC3,SW));
	mapList.push_back(NavigationMap(cC11,cC4,SW));
	mapList.push_back(NavigationMap(cC11,cC5,SW));
	mapList.push_back(NavigationMap(cC11,cC6,SW));
	mapList.push_back(NavigationMap(cC11,cC7,SW));
	mapList.push_back(NavigationMap(cC11,cC8,SW));//avoid going to obst
	mapList.push_back(NavigationMap(cC11,cC9,SW));
	mapList.push_back(NavigationMap(cC11,cC10,SW));
	mapList.push_back(NavigationMap(cC11,cC11,O));
	mapList.push_back(NavigationMap(cC11,cC12,W));
	mapList.push_back(NavigationMap(cC11,cC13,NE));
	mapList.push_back(NavigationMap(cC11,cC14,N));
	mapList.push_back(NavigationMap(cC11,cC15,NW));
	mapList.push_back(NavigationMap(cC11,cC16,NW));

	//Cell cC12
	mapList.push_back(NavigationMap(cC12,cC0,S));
	mapList.push_back(NavigationMap(cC12,cC1,S));
	mapList.push_back(NavigationMap(cC12,cC2,S));
	mapList.push_back(NavigationMap(cC12,cC3,S));
	mapList.push_back(NavigationMap(cC12,cC4,S));
	mapList.push_back(NavigationMap(cC12,cC5,S));
	mapList.push_back(NavigationMap(cC12,cC6,S));
	mapList.push_back(NavigationMap(cC12,cC7,S));
	mapList.push_back(NavigationMap(cC12,cC8,S));//avoid going to obst
	mapList.push_back(NavigationMap(cC12,cC9,S));
	mapList.push_back(NavigationMap(cC12,cC10,S));
	mapList.push_back(NavigationMap(cC12,cC11,E));
	mapList.push_back(NavigationMap(cC12,cC12,O));
	mapList.push_back(NavigationMap(cC12,cC13,NE));
	mapList.push_back(NavigationMap(cC12,cC14,NE));
	mapList.push_back(NavigationMap(cC12,cC15,N));
	mapList.push_back(NavigationMap(cC12,cC16,N));

	//Cell cC13
	mapList.push_back(NavigationMap(cC13,cC0,W));
	mapList.push_back(NavigationMap(cC13,cC1,W));
	mapList.push_back(NavigationMap(cC13,cC2,W));
	mapList.push_back(NavigationMap(cC13,cC3,W));
	mapList.push_back(NavigationMap(cC13,cC4,W));
	mapList.push_back(NavigationMap(cC13,cC5,W));
	mapList.push_back(NavigationMap(cC13,cC6,W));
	mapList.push_back(NavigationMap(cC13,cC7,W));
	mapList.push_back(NavigationMap(cC13,cC8,W));//avoid going to obst
	mapList.push_back(NavigationMap(cC13,cC9,W));
	mapList.push_back(NavigationMap(cC13,cC10,W));
	mapList.push_back(NavigationMap(cC13,cC11,W));
	mapList.push_back(NavigationMap(cC13,cC12,W));
	mapList.push_back(NavigationMap(cC13,cC13,O));
	mapList.push_back(NavigationMap(cC13,cC14,W));
	mapList.push_back(NavigationMap(cC13,cC15,W));
	mapList.push_back(NavigationMap(cC13,cC16,W));

	//Cell cC14
	mapList.push_back(NavigationMap(cC14,cC0,SW));
	mapList.push_back(NavigationMap(cC14,cC1,SW));
	mapList.push_back(NavigationMap(cC14,cC2,SW));
	mapList.push_back(NavigationMap(cC14,cC3,SW));
	mapList.push_back(NavigationMap(cC14,cC4,SW));
	mapList.push_back(NavigationMap(cC14,cC5,SW));
	mapList.push_back(NavigationMap(cC14,cC6,SW));
	mapList.push_back(NavigationMap(cC14,cC7,SW));
	mapList.push_back(NavigationMap(cC14,cC8,SW));//avoid going to obst
	mapList.push_back(NavigationMap(cC14,cC9,SW));
	mapList.push_back(NavigationMap(cC14,cC10,SW));
	mapList.push_back(NavigationMap(cC14,cC11,S));
	mapList.push_back(NavigationMap(cC14,cC12,SW));
	mapList.push_back(NavigationMap(cC14,cC13,E));
	mapList.push_back(NavigationMap(cC14,cC14,O));
	mapList.push_back(NavigationMap(cC14,cC15,W));
	mapList.push_back(NavigationMap(cC14,cC16,W));


	//Cell cC15
	mapList.push_back(NavigationMap(cC15,cC0,S));
	mapList.push_back(NavigationMap(cC15,cC1,S));
	mapList.push_back(NavigationMap(cC15,cC2,S));
	mapList.push_back(NavigationMap(cC15,cC3,S));
	mapList.push_back(NavigationMap(cC15,cC4,S));
	mapList.push_back(NavigationMap(cC15,cC5,S));
	mapList.push_back(NavigationMap(cC15,cC6,S));
	mapList.push_back(NavigationMap(cC15,cC7,S));
	mapList.push_back(NavigationMap(cC15,cC8,S));//avoid going to obst
	mapList.push_back(NavigationMap(cC15,cC9,S));
	mapList.push_back(NavigationMap(cC15,cC10,S));
	mapList.push_back(NavigationMap(cC15,cC11,SE));
	mapList.push_back(NavigationMap(cC15,cC12,S));
	mapList.push_back(NavigationMap(cC15,cC13,E));
	mapList.push_back(NavigationMap(cC15,cC14,E));
	mapList.push_back(NavigationMap(cC15,cC15,O));
	mapList.push_back(NavigationMap(cC15,cC16,N));

	//Cell cC16
	mapList.push_back(NavigationMap(cC16,cC0,S));
	mapList.push_back(NavigationMap(cC16,cC1,S));
	mapList.push_back(NavigationMap(cC16,cC2,S));
	mapList.push_back(NavigationMap(cC16,cC3,S));
	mapList.push_back(NavigationMap(cC16,cC4,S));
	mapList.push_back(NavigationMap(cC16,cC5,S));
	mapList.push_back(NavigationMap(cC16,cC6,S));
	mapList.push_back(NavigationMap(cC16,cC7,S));
	mapList.push_back(NavigationMap(cC16,cC8,S));//avoid going to obst
	mapList.push_back(NavigationMap(cC16,cC9,S));
	mapList.push_back(NavigationMap(cC16,cC10,S));
	mapList.push_back(NavigationMap(cC16,cC11,S));
	mapList.push_back(NavigationMap(cC16,cC12,S));
	mapList.push_back(NavigationMap(cC16,cC13,S));
	mapList.push_back(NavigationMap(cC16,cC14,S));
	mapList.push_back(NavigationMap(cC16,cC15,S));
	mapList.push_back(NavigationMap(cC16,cC16,O));
	
}


void Navigation::printCellList()
{
	
	for(int i=0; i<cellList.size();i++ )
		std::cout << "Cell Index " << cellList[i].m_iIndex << " Cell ID " << cellList[i].m_iLandMarkId << "Cell Lable " << cellList[i].m_sLabel << std::endl;

}

void Navigation::printNavMapList()
{
	for(int i=0; i<mapList.size();i++ )
		std::cout << "Cell from " << mapList[i].m_cFrom.m_iIndex << " Cell to " << mapList[i].m_cTo.m_iIndex << " Dir Angle " << mapList[i].m_iAngleDir << std::endl;

}

int Navigation::getDirAngle(int frmCell, int toCell)
{
	int cellFound=0;
	cellFound = (frmCell * m_iCellCnt) + toCell;

	if(cellFound >= mapList.size() || cellFound < 0)
		return -1;
	else
		return mapList[cellFound].m_iAngleDir;

}


int Navigation::findIndexfrmLabel(string sLbl)
{

	for(int i=0; i<cellList.size();i++ )
	{
		if(cellList[i].m_sLabel == sLbl)
			return i;
	}
	return -1;

}


int Navigation::findIndexfrmLandmarkID(int Lid)
{

	for(int i=0; i<cellList.size();i++ )
	{
		if(cellList[i].m_iLandMarkId == Lid)
			return i;
	}
	return -1;

}


double Navigation::calAnglesFrmXY(int ang, int Xi, int Yi)
{
	int X1 = Xi;
	int Y1 = Yi;

	int X2=0, Y2=0;
	int angle = ang;//goal angle
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


void Navigation::convCordXY(int &X, int &Y)
{
	int CX = X;
	int CY = Y;
	int temp;
	
	//need to swap X, Y for corrected orientation of sensor
	temp = CY; 
	CY = CX;
	CX = -temp;


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

	//final coversion of X & Y
	X = CX;
	Y = CY;


	//std::cout<< " ID " << State::getInstance()->LandMarkID;
	//std::cout<< " X, Y " << State::getInstance()->LandMarkX << " " << State::getInstance()->LandMarkY;// <<std::endl;
	//std::cout<< " Conv X, Y " << State::getInstance()->ConvX << " " << State::getInstance()->ConvY <<std::endl;
	
}

