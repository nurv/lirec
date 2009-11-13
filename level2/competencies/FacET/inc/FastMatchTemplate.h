/*! $Id: FastMatchTemplate.h 5 2009-03-12 22:30:56Z mw $
 *  \file FastMatchTemplate.h
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 * \author Tristen Georgiou (Copyright \f$\copyright \f$ 2005 Tristen Georgiou)
 *
 * minor changes by Marcin Namysl
 * 2008.06.28
 */

#ifndef FastMatchTemplate_h
#define FastMatchTemplate_h
/***************************************************************************
 *            FastMatchTemplate.h
 *
 *  
 *  Copyright  2005  Tristen Georgiou
 *  tristen_georgiou@hotmail.com
 ****************************************************************************/

/*
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 
 
    How fast match template works:
    1. Both target and source image are down sampled numDownPyrs times
    2. cvMatchTemplate() function is called on shrunken images 
       (uses CCORR_NORMED algorithm)
    3. The numMaxima best locations are found
    4. For each point where a maxima was located:
       a) Original source image is searched at point +/- searchExpansion
          pixels in both x and y direction
    5. If match score is above matchPercentage, a positive ID is found and a
       rectangle is drawn on the source image, where the positive ID was made.
    6. If findMultipleTargets is true, an attempt will be made to find up to
       numMaxima targets
 */

#include <vector>
#include "cv.h"

using std::vector;

//=============================================================================
// Performs a fast match template
bool 
FastMatchTemplate( const IplImage&  source, 
                   const IplImage&  target,
                   vector<CvPoint>* foundPointsList,
                   vector<double>*  confidencesList,
                   int              matchPercentage = 70,
                   bool             findMultipleTargets = true,
                   int              numMaxima = 5,
                   int              numDownPyrs = 2,
                   int              searchExpansion = 15 );

//=============================================================================
// Searches an image for multiple maxima
// Assumes a single channel, floating point image
void 
MultipleMaxLoc( const IplImage& image, 
                CvPoint**       locations,
                int             numMaxima );

//=============================================================================
// Draws a rectangle of dimension size, at the given positions in the list,
//  in the given RGB color space
void
DrawFoundTargets( IplImage*              image,
                  const CvSize&          size,
                  const vector<CvPoint>& pointsList,
                  const vector<double>&  confidencesList,
                  int                    red   = 0,
                  int                    green = 255,
                  int                    blue  = 0 );

#endif
