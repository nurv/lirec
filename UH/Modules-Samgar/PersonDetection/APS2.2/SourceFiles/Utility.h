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

#ifndef UTILITY_H
#define UTILITY_H

#ifndef M_PI					// Oskoei
#define M_PI 3.1415927			// Oskoei
#endif // of M_PI, windows has a function call instead of a define

#ifndef RTOD
/** Convert radians to degrees */
#define RTOD(r) ((r) * 180 / M_PI)
#endif

#ifndef DTOR
/** Convert degrees to radians */
#define DTOR(d) ((d) * M_PI / 180)
#endif

#ifndef NORMALIZE
/** Normalize angle to domain -pi, pi */
#define NORMALIZE(z) atan2(sin(z), cos(z))
#endif

// Square
template <class scalar>
inline scalar sqr(scalar x)
{
   return x*x;
}

// Sign
template <class scalar>
inline scalar sign(scalar x)
{
   return x < 0 ? -1. : x > 0 ? 1. : 0;
}


typedef struct
{
   double x;         // [m]
   double y;         // [m]
   double th;        // [rad]
   double pan;       // [rad]
   double tilt;      // [rad]
} odometry_t;


typedef struct
{
   double range;     // [m]
   double angle;     // [rad]
   double x;         // [m]
   double y;         // [m]
   int intensity;    // 0-7
} laser_t;


typedef struct
{
   double range;     // [m]
   double angle;     // [rad]
} sonar_t;


#endif
