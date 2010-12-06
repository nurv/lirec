/***************************************************************************
 *   Copyright (C) 2005 by Nicola Bellotto                                 *
 *   nbello@essex.ac.uk                                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
#ifndef LEGSDETECTOR_H
#define LEGSDETECTOR_H

#include "Utility.h"

#include <string>
#include <vector>
#include <math.h>

#include "cv.h"
#include "highgui.h"

using std::vector;

class LegsDetector 
{
   typedef struct
   {
      double distance;  // [m]
      double bearing;   // [rad]
      char pattern;
   } target_t;

   class point_t {
   public:
      point_t(double x, double y, double r, double th)
         { this->x = x; this->y = y; this->r = r; this->th = th; }
      point_t(double a, double b, bool cart = true)
         {  if (cart)
            { x = a; y = b; r = sqrt(x*x + y*y); th = atan2(y, x); }
            else
            { x = a * cos(b); y = a * sin(b); r = a; th = b; }
         }
   public:
      double x;
      double y;
      double r;
      double th;
   };
   //inline point_t Point(double x, double y) { point_t p(x, y); return p; }
   inline point_t Point(double x, double y, double r, double th) { point_t p(x, y, r, th); return p; }
   
   template <typename Point>
   struct edge_t { Point p1; Point p2; char type; };

   inline double dist(const point_t& p1, const point_t& p2)
      { return sqrt(sqr(p1.x - p2.x) + sqr(p1.y - p2.y)); }

public:
   /**
   * Constructor
   * Selectivity can be used to filter @p O ('1' = strict) and @p P ('2' = very strict) patterns
   * @param bufferLength Length of the buffer
   * @param selectivity Selectivity of the legs detection ('0' = allow everything (default), '1' = strict, '2' = very strict)
   * @param filter Filter threshold (default 0.5deg)
   */
   LegsDetector(int bufferLength, int selectivity = 0, double filter = 0.5);

   /**
   * Destructor
   * @return 
   */
   ~LegsDetector();

   /**
    * Perform a new detection on the current laser data
    * @param laserBuffer Vector of laser scans
    */
   void update(const vector< laser_t >& laserBuffer);

   /**
    * Reset detector
    */
   void reset() {
      _howMany = 0;
      _target.clear();
   }
   
   /**
    * Returns the number of detected persons (legs pairs)
    * @return Number of persons
    */
   int getHowMany();
    
   /**
    * Get the distance of a particular person (legs pair)
    * @param personID Person ID number, between "0" and "N = getHowMany()"
    * @return Distance in [m]
    */
   double getDistance(int personID); //throw (LegsDetectorException);
    
   /**
    * Get the direction of a particular person (legs pair)
    * @param personID Person ID number, between "0" and "N = getHowMany()"
    * @return Bearing in [rad], +PI on the left and -PI on the right
    */
   double getBearing(int personID); //throw (LegsDetectorException);

   /**
    * Get the direction of a particular person (legs pair)
    * @param personID Person ID number, between "0" and "N = getHowMany()"
    * @return -3, -2, -1, +1, +2, +3 corresponding to Bearing angle 
    */
   int getLocation(int personID); //throw (LegsDetectorException);

   /**
    * DEPRECATED - Sunstituted by @p getBearing
    */
   double getDirection(int personID) //throw (LegsDetectorException) 
   { return getBearing(personID); }
   
   /**
    * Get the legs pattern (U, P or O type) of a particular person
    * @param personID Person ID number, between "0" and "N = getHowMany()"
    * @return Pattern type
    */
   char getPattern(int personID); //throw (LegsDetectorException);
   
   /**
    * Get the ID of the closest person
    * @return Person ID
    */
   int getClosest();
   
   /**
    * Get the ID of the farthest person
    * @return Person ID
    */
   int getFarthest();
   
   /**
    * Enable/disable debugging window
    * @param enable Enable if "true", disable if "false"
    * @param delay Time delay in [ms] for @p OpenCV to handle signals ('0' to skip)
    */
   void setDebug(bool enable, int delay = 0);

   /**
    * When debug active, save snapshot of current detection
    * @param filename Image file
    * @return FALSE if any error occured
    */
   bool saveSnapshot(const char* filename);

private:
   int _howMany;
   vector< target_t > _target;
   
   int _bufferLength;
   IplImage* _debugImage;
   IplImage* _tmpImg;
   int _delta;

   int _selectivity;
   
   bool _debug;
   bool _delay;
   IplImage* _mapImg;
   FILE* _dataFile;

private:
   template <class Point>
   inline double getDistance(Point p1, Point p2)
      { return sqrt(sqr(p1.x - p2.x) + sqr(p1.y - p2.y)); }
      
   int getUpattern(vector< edge_t<point_t> >& input,
                    vector< edge_t<point_t> >& output);
                    
   int getPpattern(vector< edge_t<point_t> >& input,
                    vector< edge_t<point_t> >& output);
                    
   int getOpattern(vector< edge_t<point_t> >& input,
                    vector< edge_t<point_t> >& output);
};

#endif
