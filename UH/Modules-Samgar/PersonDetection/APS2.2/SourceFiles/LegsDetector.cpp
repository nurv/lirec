/***************************************************************************
 *   Copyright (C) 2005 by Nicola Bellotto (first edition)                 *
 *   nbello@essex.ac.uk                                                    *
 *   Copyright (C) 2010 by Mohammadreza Asghari Oskoei (second edition)    *
 *   m.asghari-oskoei@herts.ac.uk                                          *
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

#include "LegsDetector.h"

#include <iostream>
#include <errno.h>
#include <vector>

#ifndef max
#define max(a,b)            (((a) > (b)) ? (a) : (b))
#endif

#ifndef min
#define min(a,b)            (((a) < (b)) ? (a) : (b))
#endif

#define MAXIMUM_RANGE  4 //8                    // maximum detection range, in [m]
#define DELTA(k, max) ((int)(k*max/180. + .5))  // minimization filter (k = bound [deg], max = aperture [deg])

#define GRID_RESOLUTION 0.01 //0.02   // [m]
#define METER2PIXEL(m) ((int)((float)(m) / (GRID_RESOLUTION) + 0.5))
#define PIXEL2METER(p) ((float)(p) * (GRID_RESOLUTION))
#define DEBUG_SCALE 1   // debug image size scaling
#define DEBUG_WINDOW_WIDTH (2 * METER2PIXEL(MAXIMUM_RANGE)) // 480
#define DEBUG_WINDOW_HEIGHT (METER2PIXEL(MAXIMUM_RANGE))	// 240

// parameters of update
#define MIN_LONG_EDGE   0.30  // [m] basically, minimum distance from obstacles
#define MIN_SHORT_EDGE  0.10  // [m] basically, minimum step length
#define MIN_EDGE_DIST   0.05  // [m] minimum distance between two right or two left vertical edges
#define MAX_LEG_WIDTH   0.25
#define MIN_LEG_WIDTH   0.05  // .10
#define MAX_LEGS_GAP    0.35
#define MAX_RANGE_DIFF  0.10  // [m] (maximum) horizontal edge gradient
#define MAX_GRADIENT    0.707 //0.50  // sin(pi/6) or 0.707 // sin(pi/4)

#define BUFFERLENGTH 512
#define INFINITY 50000
#ifndef M_PI
#define M_PI 3.1415927
#endif // of M_PI, windows has a function call instead of a define


LegsDetector::LegsDetector(int bufferLength, int selectivity, double filter)
{
   _bufferLength = bufferLength;
   //_delta = DELTA(filter, _bufferLength);
   _delta = (int) filter;

   _selectivity = selectivity;

   _howMany = 0;

   _debug = false;
}


LegsDetector::~LegsDetector()
{
   if (_debug)
   {
      cvReleaseImage(&_mapImg);
      cvReleaseImage(&_debugImage);
      cvReleaseImage(&_tmpImg);
      cvDestroyWindow("Legs detector");
      cvDestroyWindow("Old legs detector");
   }
}



int LegsDetector::getHowMany()
{
   return _howMany;
}


double LegsDetector::getDistance(int personID) //throw (LegsDetectorException)
{
   if ((personID >= 0) && (personID < _howMany))
      return _target[personID].distance;
   //throw LegsDetectorException("Index out of boundaries in LegsDetector::getDistance(int personID)", EINVAL);
   return -1;
}


double LegsDetector::getBearing(int personID) //throw (LegsDetectorException)
{
   if ((personID >= 0) && (personID < _howMany))
      return _target[personID].bearing;
   //throw LegsDetectorException("Index out of boundaries in LegsDetector::getDirection(int personID)", EINVAL);
   return -1;
}

int LegsDetector::getLocation(int personID) //throw (LegsDetectorException)
{
	if ((personID >= 0) && (personID < _howMany))
	{
		if      ( RTOD(_target[personID].bearing) <-45 ) return +3;
		else if ( RTOD(_target[personID].bearing) <-15 ) return +2;
		else if ( RTOD(_target[personID].bearing) <  0 ) return +1;
		else if ( RTOD(_target[personID].bearing) <+15 ) return -1;
		else if ( RTOD(_target[personID].bearing) <+45 ) return -2;
		else                                             return -3;
	}
   //throw LegsDetectorException("Index out of boundaries in LegsDetector::getDirection(int personID)", EINVAL);
   return 0;
}

int LegsDetector::getClosest()
{
   int personID = -1;
   double minDist = INFINITY;
   int maxId = getHowMany();
   for (int id = 0; id < maxId; id++)
      if (getDistance(id) < minDist)
         minDist = getDistance(personID = id);
   return personID;
}


int LegsDetector::getFarthest()
{
   int personID = -1;
   double maxDist = -INFINITY;
   int maxId = getHowMany();
   for (int id = 0; id < maxId; id++)
      if (getDistance(id) > maxDist)
         maxDist = getDistance(personID = id);
   return personID;
}


void LegsDetector::setDebug(bool enable, int delay)
{
   if (!_debug && (_debug = enable))
   {
      _mapImg = cvCreateImage(cvSize(600, 600), IPL_DEPTH_8U, 3);
      _debugImage = cvCreateImage(cvSize((int)(DEBUG_WINDOW_WIDTH * DEBUG_SCALE),
                                         (int)(DEBUG_WINDOW_HEIGHT * DEBUG_SCALE)),
                                  IPL_DEPTH_8U, 3);
      _tmpImg = cvCreateImage(cvSize(DEBUG_WINDOW_WIDTH, DEBUG_WINDOW_HEIGHT), IPL_DEPTH_8U, 3);
      cvNamedWindow("Legs detector", 1);
      //cvNamedWindow("Old legs detector", 1);
   }
   _delay = delay;
}

void LegsDetector::update(const std::vector< laser_t >& laserBuffer)
{
   // first remove high peaks due to absorving materials
   laser_t laser[BUFFERLENGTH];
   for (int i = 0; i < _bufferLength; i++)
   {
      laser[i].range = DBL_MAX;
      double angle = laser[i].angle = laserBuffer[i].angle;
	  for (int k = max(0, i-_delta); k <= min( _bufferLength-1, i+_delta); k++)
      {
         double range;
         if (laserBuffer[k].range < laser[i].range)
         {
            range = laser[i].range = laserBuffer[k].range;
            laser[i].x = range * cos(angle);
            laser[i].y = range * sin(angle);
         }
      }
   }
   //                       (0)
   //                        |
   //                        |
   //                        |
   // (+90)------------------|-------------------(-90)
   // reading from right to left i.e. from -90 to +90
   //
   // start extracting all the vertical edges of interest
   // remembering the scan goes from right (-PI/2) to left (+PI/2)
   // left and right edges correspond to the robot's point of view
   //
   //                 -(p1)             (p1)-
   //                   |    (p1)-(p1)   |
   //                   |     |    |     |
   //                   |     |   l|     |r
   //                   |     |    |     |
   //                  L|     |R  (p2)--(p2)
   //                   |     |
   //                   |     |
   //                  (p2)--(p2)
   //
   vector< edge_t<point_t> > vEdge;
   double prevRange = laser[0].range;
   for (int id = 1; id < _bufferLength; id++)
   {
      double range = laser[id].range;

      //if ( range == MAXIMUM_RANGE  || prevRange == MAXIMUM_RANGE ) ;
	  if ((prevRange - range) > MIN_LONG_EDGE)      // possible left long edge
      {
		  edge_t<point_t> e = {Point(laser[id-1].x, laser[id-1].y, laser[id-1].range, laser[id-1].angle),
                              Point(laser[id].x, laser[id].y, laser[id].range, laser[id].angle), 'R'};
         vEdge.push_back(e);
      }
      else if ((range - prevRange) > MIN_LONG_EDGE) // possible right long edge
      {
         edge_t<point_t> e = {Point(laser[id].x, laser[id].y, laser[id].range, laser[id].angle),
                              Point(laser[id-1].x, laser[id-1].y, laser[id-1].range, laser[id-1].angle), 'L'};
         vEdge.push_back(e);
      }
      else if ((prevRange - range) > MIN_SHORT_EDGE) // possible left short edge
      {
         edge_t<point_t> e = {Point(laser[id-1].x, laser[id-1].y, laser[id-1].range, laser[id-1].angle),
                              Point(laser[id].x, laser[id].y, laser[id].range, laser[id].angle), 'r'};
         vEdge.push_back(e);
      }
      else if ((range - prevRange) > MIN_SHORT_EDGE) // possible right short edge
      {
         edge_t<point_t> e = {Point(laser[id].x, laser[id].y, laser[id].range, laser[id].angle),
                              Point(laser[id-1].x, laser[id-1].y, laser[id-1].range, laser[id-1].angle), 'l'};
         vEdge.push_back(e);
      }

      prevRange = range;
   }
   // remove edges too close to each other
   if ( vEdge.empty() ) return;
   vector<edge_t<point_t> >::iterator first = vEdge.begin();
   vector<edge_t<point_t> >::iterator second = first + 1;
   double d1, d2;
   char t1, t2;
   while (second < vEdge.end())
   {
	   t1 = toupper(first->type);
       t2 = toupper(second->type);
	   d1 = getDistance(second->p1, first->p2);
	   d2 = getDistance(first->p1, second->p2);
       if ( t1 == 'R' && t2 == 'R' && d1 < MIN_EDGE_DIST )
       {
		   first->p2 = second->p2;
           first->type = 'R';
           second = vEdge.erase(second);
        }
        else if ( t1 == 'L' && t2 == 'L' && d2 < MIN_EDGE_DIST )
        {
			first->p1 = second->p1;
            first->type = 'L';
            second = vEdge.erase(second);
	   }
       else
       {
		   first++;
           second++;
       }
   }
   if ( vEdge.empty() ) return;
   // draw some stuff for debugging... (must be done now, before vEdge is modified)
   if (_debug)
   {
      CvPoint start;
	  cvSet(_tmpImg, cvScalar(255,255,255));

	  start = cvPoint(DEBUG_WINDOW_WIDTH/2, 0);
	  cvCircle(_tmpImg, start, 1*DEBUG_WINDOW_WIDTH/80, cvScalar(255,0,0));
	  cvCircle(_tmpImg, start, 1*DEBUG_WINDOW_WIDTH/16, cvScalar(255,0,0));
	  cvCircle(_tmpImg, start, 2*DEBUG_WINDOW_WIDTH/16, cvScalar(255,0,0));
	  cvCircle(_tmpImg, start, 3*DEBUG_WINDOW_WIDTH/16, cvScalar(255,0,0));
	  cvCircle(_tmpImg, start, 4*DEBUG_WINDOW_WIDTH/16, cvScalar(255,0,0));
	  cvCircle(_tmpImg, start, 5*DEBUG_WINDOW_WIDTH/16, cvScalar(255,0,0));
	  cvCircle(_tmpImg, start, 6*DEBUG_WINDOW_WIDTH/16, cvScalar(255,0,0));
	  cvCircle(_tmpImg, start, 7*DEBUG_WINDOW_WIDTH/16, cvScalar(255,0,0));
	  cvCircle(_tmpImg, start, 8*DEBUG_WINDOW_WIDTH/16, cvScalar(255,0,0));

      start = cvPoint(METER2PIXEL(laser[0].y) + DEBUG_WINDOW_WIDTH/2,
                              METER2PIXEL(laser[0].x));
      // draw the laser data
      for (int i = 1; i < _bufferLength; i++)
      {
         CvPoint end = cvPoint(METER2PIXEL(laser[i].y) + DEBUG_WINDOW_WIDTH/2,
                               METER2PIXEL(laser[i].x));

		 if (laser[i].range == MAXIMUM_RANGE && laser[i-1].range == MAXIMUM_RANGE)
			 cvLine(_tmpImg, start, end, cvScalar(0,0,0));
		 if (laser[i].range <  MAXIMUM_RANGE && laser[i-1].range <  MAXIMUM_RANGE)
			 cvLine(_tmpImg, start, end, cvScalar(0,0,0));

		 start = end;
      }
      // draw the extremes
      for (unsigned int i = 0; i < vEdge.size(); i++)
      {
         CvScalar color;
		 switch (vEdge[i].type)
         {
            case 'R':
               color = cvScalar(0,0,255); // red
               break;
            case 'L':
               color = cvScalar(255,0,0); // blue
               break;
            case 'r':
               color = cvScalar(0,196,255);  // yellow
               break;
            case 'l':
               color = cvScalar(64,255,0);  // green
               break;
         }
		 // draw min extremes
		 CvPoint center = cvPoint(METER2PIXEL(vEdge[i].p1.y) + DEBUG_WINDOW_WIDTH/2,
                                  METER2PIXEL(vEdge[i].p1.x));
         cvCircle(_tmpImg, center, 2, color);
         // draw max extremes
         CvPoint c1 = cvPoint(METER2PIXEL(vEdge[i].p2.y) - 3 + DEBUG_WINDOW_WIDTH/2,
                              METER2PIXEL(vEdge[i].p2.x) - 3);
         CvPoint c2 = cvPoint(METER2PIXEL(vEdge[i].p2.y) + 3 + DEBUG_WINDOW_WIDTH/2,
                              METER2PIXEL(vEdge[i].p2.x) + 3);
         cvRectangle(_tmpImg, c1, c2, color);
      }
   }

   // extract the horizontal lines of interest
   vector< edge_t<point_t> > hEdge;
   int temp = 1;
   while ( temp > 0 ) { temp = getUpattern(vEdge, hEdge); }
   temp = 1;
   while ( _selectivity < 2 && temp > 0 ) { temp = getPpattern(vEdge, hEdge);}
   temp = 1;
   while ( _selectivity < 1 && temp > 0 ) { temp = getOpattern(vEdge, hEdge);}

   // finally calculate distance and direction of each horizontal line
   _target.clear();
   vector< edge_t<point_t> >::iterator itend = hEdge.end();
   for (vector< edge_t<point_t> >::iterator it = hEdge.begin(); it < itend; it++)
   {
      target_t t;
      // the distance is an average between the two points
      double xm = ((it->p1).x + (it->p2).x) / 2;
      double ym = ((it->p1).y + (it->p2).y) / 2;
      t.distance = sqrt(sqr(xm) + sqr(ym));
      // left PI/2, right -PI/2
      t.bearing = atan2(ym, xm);
      // no height information of course...
      t.pattern = it->type;
      _target.push_back(t);
   }
   // final number of detected people
   _howMany = _target.size();
   // draw the last things for debugging
   if (_debug)
   {
      // draw horizontal edges
      for (unsigned int i = 0; i < hEdge.size(); i++)
      {
         CvPoint p1 = cvPoint(METER2PIXEL(hEdge[i].p1.y) + DEBUG_WINDOW_WIDTH/2,
                              METER2PIXEL(hEdge[i].p1.x));
         CvPoint p2 = cvPoint(METER2PIXEL(hEdge[i].p2.y) + DEBUG_WINDOW_WIDTH/2,
                              METER2PIXEL(hEdge[i].p2.x));
//          cvLine(_tmpImg, p1, p2, cvScalar(0,128,255), 2);
         CvPoint pm = cvPoint((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
         int thick;
         if (hEdge[i].type == 'U')
            thick = 3;
         else if (hEdge[i].type == 'P')
            thick = 2;
         else
            thick = 1;
         cvLine(_tmpImg, cvPoint(DEBUG_WINDOW_WIDTH/2, 0), pm, cvScalar(0,128,255), thick);
      }

      cvFlip(_tmpImg, NULL, -1);
      cvResize(_tmpImg, _debugImage, CV_INTER_NN);
      cvShowImage("Legs detector", _debugImage);
 	  if (_delay)
        cvWaitKey(_delay);  // handles event processing of HIGHGUI library
   }
   return;
}

// get all the 'U' patterns
//
//   |  ||  |       |  ||  |
//   |  ||  |       |  ||  |
//   |  ||  |  and  |  |+--+
//   |  ||  |       |  |
//   +--++--+       +--+
//
int LegsDetector::getUpattern(vector< edge_t<point_t> >& input,
                               vector< edge_t<point_t> >& output)
{
   //#warning Correct so to distinguish 2 adjacent persons
   vector< edge_t<point_t> >::iterator first = input.begin();
   while (first < input.end())
   {
	   // look for the left edge
	   if (first->type == 'R')
	   {
		   vector< edge_t<point_t> >::iterator second = first + 1;
		   double d, g;
		   if ( second < input.end() && second->type == 'L' )
		   {
			   d = dist(second->p2, first->p2);
			   g = fabs(second->p1.r - first->p1.r)/d;
			   if ( d < MAX_LEG_WIDTH && d > MIN_LEG_WIDTH )//&& g < MAX_GRADIENT )
			   {
				   // first leg detected, look for the second
				   vector< edge_t<point_t> >::iterator third = second + 1;
				   while ( third < input.end() )
				   {
					   // look for the left edge of the second leg
					   if ( third->type == 'R' )
					   {
					       d = dist(third->p2, second->p2);
					       if ( d > MAX_LEGS_GAP ) break;
						   vector< edge_t<point_t> >::iterator forth = third + 1;
						   if ( forth < input.end() && forth->type == 'L' )
						   {
							   d = dist(forth->p2, third->p2);
							   g = fabs(forth->p1.r - third->p1.r)/d;
							   if ( d < MAX_LEG_WIDTH && d > MIN_LEG_WIDTH )//&& g < MAX_GRADIENT )
							   {
								   // add new horizontal edge
								   edge_t<point_t> e = {first->p2, forth->p2, 'U'};
								   output.push_back(e);
								   // remove current legs edges
								   input.erase(first, forth+1);
								   return 1;
							   }
						   }
					   }
					   third++;
				   }
			   }
		   }
	   }
	   first++;
   }
   return 0;
}


// get all the 'P' patterns
//
//   |     |       |     |
//   |     |       |     |
//   |  |--+  and  +--|  |
//   |  |             |  |
//   +--+             +--+
//
int LegsDetector::getPpattern(vector< edge_t<point_t> >& input,
                               vector< edge_t<point_t> >& output)
{
   vector< edge_t<point_t> >::iterator first = input.begin();
   double d1, d2, d3;
   while ( first < input.end()-1 )
   {
      // look for the left edge
      if (first->type == 'R')
      {
         // look for the next two edges
         vector< edge_t<point_t> >::iterator second = first + 1;
         vector< edge_t<point_t> >::iterator third  = first + 2;
		 if ( second->type == 'l' && third->type == 'l' )
		 {
			 d1 = dist(first->p2, second->p2);
			 d2 = dist(second->p1, third->p2);
			 d3 = dist(second->p1, second->p2);
			 if ( d1 < MAX_LEG_WIDTH && d1 > MIN_LEG_WIDTH && d2 < MAX_LEG_WIDTH && d2 > MIN_LEG_WIDTH && d3 < MAX_LEGS_GAP )
			 {
                     // add new horizontal edge (well, not really horizontal...)
                     edge_t<point_t> e = {first->p2, third->p2, 'P'};
                     output.push_back(e);
                     // remove current pattern edges
                     input.erase(first, third+1);
					 return 1;
			 }
		 }
	  }
      // look for the left edge
      else if (first->type == 'r')
      {
         // look for the next two edges
         vector< edge_t<point_t> >::iterator second = first + 1;
         vector< edge_t<point_t> >::iterator third  = first + 2;
		 if ( second->type == 'r' && third->type == 'L' )
		 {
			 d1 = dist(first->p2, second->p1);
			 d2 = dist(second->p2, third->p2);
			 d3 = dist(second->p1, second->p2);
			 if ( d1 < MAX_LEG_WIDTH && d1 > MIN_LEG_WIDTH && d2 < MAX_LEG_WIDTH && d2 > MIN_LEG_WIDTH && d3 < MAX_LEGS_GAP )
			 {
                     // add new horizontal edge (well, not really horizontal...)
                     edge_t<point_t> e = {first->p2, third->p2, 'P'};
                     output.push_back(e);
                     // remove current pattern edges
                     input.erase(first, third+1);
					 return 1;
			 }
		 }
	  }
	  first++;
   }
   return 0;
}


// get all the 'O' patterns
//
//   |      |
//   |      |
//   |      |
//   |      |
//   +------+
//
int LegsDetector::getOpattern(vector< edge_t<point_t> >& input,
                               vector< edge_t<point_t> >& output)
{
   vector< edge_t<point_t> >::iterator first = input.begin();
   double d, g;
   while (first < input.end())
   {
      // look for the left edge
      if (first->type == 'R')
      {
         vector< edge_t<point_t> >::iterator second = first + 1;
         if ( second < input.end() && second->type == 'L' )
		 {
			 d = dist(second->p2, first->p2);
			 g = fabs(second->p1.r - first->p1.r)/d;
			 if ( d < 2*MAX_LEG_WIDTH && d > 2*MIN_LEG_WIDTH) // && g < MAX_GRADIENT )
			 {
				 // add new horizontal edge
				 edge_t<point_t> e = {first->p2, second->p2, 'O'};
				 output.push_back(e);
				 // remove current left and right edges
				 input.erase(first, second+1);
				 return 1;
			 }
		 }
	  }
	  first++;
   }
   return 0;
}


char LegsDetector::getPattern(int personID) //throw (LegsDetectorException)
{
   if ((personID >= 0) && (personID < _howMany))
      return _target[personID].pattern;
   //throw LegsDetectorException("Index out of boundaries in LegsDetector::getPattern(int personID)", EINVAL);
   return -1;
}


bool LegsDetector::saveSnapshot(const char* filename)
{
   if (_debug && !cvSaveImage(filename, _debugImage))
      return true;
   return false;
}


