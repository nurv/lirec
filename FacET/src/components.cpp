/* $Id: components.cpp 5 2009-03-12 22:30:56Z mw $
 * +-------------------------------------------------------------------+
 * | This file contains parts of code from the application created for |
 * | the Master thesis supervised by Marek Wnuk (Wroclaw University of |
 * | Technology):  "Wykorzystanie systemu wizyjnego do rozpoznawania   |
 * | emocji czlowieka" ("Vision system in human emotions recognition") |
 * | by Marcin Namysl in June 2008.                                    |
 * +-------------------------------------------------------------------+
 *
 * \author Marek Wnuk
 * \date 2009.02.26
 * \version 1.00.00
 */

/*
    FacET is a library for detecting and parameterising face components.
    Copyright (C) 2009  Marek Wnuk <marek.wnuk@pwr.wroc.pl>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*! \file components.cpp
 *
 * File contains auxiliary functions for equation of straight-line through 
 * two points, calculation of \link ::calcTwoLinesAngle angle between two 
 * lines\endlink, \link ::pointsDistance distance between two points\endlink 
 * and rad/deg conversion.
 * 
 */

#include "facet.h"

#ifndef M_PI 
# define M_PI           3.14159265358979323846  // pi for windows
#endif // M_PI

void drawStrLine(CvPoint p1, CvPoint p2, double &a, double &b)
{
  a=(p2.y-p1.y)/(p2.x-p1.x);
  b=p1.y-a*p1.x;
}

void drawStrLine(CvPoint p1,CvPoint p2,
		     double &A, double &B, double &C)
{
  float dx = p2.x-p1.x;
  float dy = p2.y-p1.y;
  float M = sqrt(dx*dx + dy*dy); // distance between two points
  A=-dy/M; 
  B=dx/M;
  C=-A*p1.x-B*p1.y;
}

double calcAngleOX(double A, double B)
{
  return atan2(-A,B);
}

double calcTwoLinesAngle(double A1, double B1, double A2, double B2)
{
  //return atan2((A1*B2-A2*B1),(A1*A2+B1*B2));
  //return (((A1>0 && A2>0)) || ((A1<0) && (A2<0))) ? 
  //atan(A1)-atan(A2) : atan(A1)+atan(A2);
  return acos((A1*A2 + B1*B2)/(sqrt(A1*A1+B1*B1)*sqrt(A2*A2+B2*B2)));
}

double calcTwoLinesAngle(double a1, double a2)
{
  return atan2(a1-a2, 1+a1*a2);
}


double rad2deg(double rad)
{
  return rad*180/M_PI;
}

double deg2rad(double deg)
{
  return deg*M_PI/180;
}

double pointsDistance(CvPoint p1, CvPoint p2)
{
  double dx = p2.x - p1.x;
  double dy = p2.y - p1.y;
  return sqrt(dx*dx+dy*dy);
}
