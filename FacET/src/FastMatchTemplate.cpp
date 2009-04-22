/*! $Id: FastMatchTemplate.cpp 5 2009-03-12 22:30:56Z mw $
 *  \file FastMatchTemplate.cpp
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

/***************************************************************************
 *            FastMatchTemplate.cc
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
 */

#include <iostream>
#include <cmath>
#include "FastMatchTemplate.h"

using std::cout;
using std::cerr;

//=============================================================================
// Assumes that source image exists and numDownPyrs > 1, no ROIs for either 
//  image, and both images have the same depth and number of channels
bool
FastMatchTemplate( const IplImage&  source, 
                   const IplImage&  target,
                   vector<CvPoint>* foundPointsList,
                   vector<double>*  confidencesList,
                   int              matchPercentage,
                   bool             findMultipleTargets,
                   int              numMaxima,
                   int              numDownPyrs,
                   int              searchExpansion )
{
  // make sure that the template image is smaller than the source
  if( target.width > source.width || target.height > source.height )
  {
    // Source image must be larger than target image.
    return false;
  }
  
  if( source.depth != target.depth )
  {
    // Source image and target image must have same depth.
    return false;
  }
  
  if( source.nChannels != target.nChannels )
  {
    // Source image and target image must have same number of channels.
    return false;
  }
  
  CvSize sourceSize = cvGetSize( &source );
  CvSize targetSize = cvGetSize( &target );
  
  int depth = source.depth;
  int numChannels = source.nChannels;
  
  // create copies of the images to modify
  IplImage* copyOfSource = cvCloneImage( &source );
  IplImage* copyOfTarget = cvCloneImage( &target );
  
  // down pyramid the images
  for( int ii = 0; ii < numDownPyrs; ii++ )
  {
    // start with the source image
    sourceSize.width /= 2;
    sourceSize.height /= 2;
    
    IplImage* smallSource = NULL;
    smallSource = cvCreateImage( sourceSize, depth, numChannels );
    cvPyrDown( copyOfSource, smallSource, CV_GAUSSIAN_5x5 );
    
    // prepare for next loop, if any
    cvReleaseImage( &copyOfSource );
    copyOfSource = cvCloneImage( smallSource );
    cvReleaseImage( &smallSource );
    
    // next, do the target    
    targetSize.width /= 2;
    targetSize.height /= 2;
    
    IplImage* smallTarget = NULL;
    smallTarget = cvCreateImage( targetSize, depth, numChannels );
    cvPyrDown( copyOfTarget, smallTarget, CV_GAUSSIAN_5x5 );
    
    // prepare for next loop, if any
    cvReleaseImage( &copyOfTarget );
    copyOfTarget = cvCloneImage( smallTarget );
    cvReleaseImage( &smallTarget );
  }
  
  // perform the match on the shrunken images
  CvSize smallTargetSize = cvGetSize( copyOfTarget );
  CvSize smallSourceSize = cvGetSize( copyOfSource );
  
  CvSize resultSize;
  resultSize.width = smallSourceSize.width - smallTargetSize.width + 1;
  resultSize.height = smallSourceSize.height - smallTargetSize.height + 1;
  
  IplImage* result = cvCreateImage( resultSize, IPL_DEPTH_32F, 1 );
  
  cvMatchTemplate( copyOfSource, copyOfTarget, result, CV_TM_CCORR_NORMED );

  // release memory we don't need anymore
  cvReleaseImage( &copyOfSource );
  cvReleaseImage( &copyOfTarget );
  
  // find the top match locations
  CvPoint* locations = NULL;
  MultipleMaxLoc( *result, &locations, numMaxima );
  
  cvReleaseImage( &result );
  
  // search the large images at the returned locations
  sourceSize = cvGetSize( &source );
  targetSize = cvGetSize( &target );
  
  // create a copy of the source in order to adjust its ROI for searching
  IplImage* searchImage = cvCloneImage( &source );
  for( int currMax = 0; currMax < numMaxima; currMax++ )
  { 
    // transform the point to its corresponding point in the larger image

    locations[currMax].x *= ( int )pow( (float) 2, (int) numDownPyrs ); // GC(?)
    locations[currMax].y *= ( int )pow( (float) 2, (int) numDownPyrs );
    // locations[currMax].x *= ( int )pow( 2, numDownPyrs );
    // locations[currMax].y *= ( int )pow( 2, numDownPyrs );

    locations[currMax].x += targetSize.width / 2;
    locations[currMax].y += targetSize.height / 2;
    
    const CvPoint& searchPoint = locations[currMax];
    
    // if we are searching for multiple targets and we have found a target or 
    //  multiple targets, we don't want to search in the same location(s) again
    if( findMultipleTargets && !foundPointsList->empty() )
    {
      bool thisTargetFound = false;
      
      int numPoints = foundPointsList->size();
      for( int currPoint = 0; currPoint < numPoints; currPoint++ )
      {
        const CvPoint& foundPoint = ( *foundPointsList )[currPoint];
        if( abs( searchPoint.x - foundPoint.x ) <= searchExpansion * 2 && 
            abs( searchPoint.y - foundPoint.y ) <= searchExpansion * 2 )
        {
          thisTargetFound = true;
          break;
        }
      }
      
      // if the current target has been found, continue onto the next point
      if( thisTargetFound )
      {
        continue;
      }
    }
    
    // set the source image's ROI to slightly larger than the target image, 
    //  centred at the current point
    CvRect searchRoi;
    searchRoi.x = searchPoint.x - ( target.width ) / 2 - searchExpansion;
    searchRoi.y = searchPoint.y - ( target.height ) / 2 - searchExpansion;
    searchRoi.width = target.width + searchExpansion * 2;
    searchRoi.height = target.height + searchExpansion * 2;
    
    // make sure ROI doesn't extend outside of image
    if( searchRoi.x < 0 )
    {
      searchRoi.x = 0;
    }
    if( searchRoi.y < 0 )
    {
      searchRoi.y = 0;
    }
    if( ( searchRoi.x + searchRoi.width ) > ( sourceSize.width - 1 ) )
    {
      int numPixelsOver 
        = ( searchRoi.x + searchRoi.width ) - ( sourceSize.width - 1 );
      
      searchRoi.width -= numPixelsOver;
    }
    if( ( searchRoi.y + searchRoi.height ) > ( sourceSize.height - 1 ) )
    {
      int numPixelsOver 
        = ( searchRoi.y + searchRoi.height ) - ( sourceSize.height - 1 );
      
      searchRoi.height -= numPixelsOver;
    }
    
    cvSetImageROI( searchImage, searchRoi );
    
    // perform the search on the large images
    resultSize.width = searchRoi.width - target.width + 1;
    resultSize.height = searchRoi.height - target.height + 1;
    
    result = cvCreateImage( resultSize, IPL_DEPTH_32F, 1 );
    
    cvMatchTemplate( searchImage, &target, result, CV_TM_CCORR_NORMED );
    cvResetImageROI( searchImage );
    
    // find the best match location
    double minValue, maxValue;
    CvPoint minLoc, maxLoc;
    cvMinMaxLoc( result, &minValue, &maxValue, &minLoc, &maxLoc );
    maxValue *= 100;
    
    // transform point back to original image
    maxLoc.x += searchRoi.x + target.width / 2;
    maxLoc.y += searchRoi.y + target.height / 2;
    
    cvReleaseImage( &result );
    
    if( maxValue >= matchPercentage )
    {
      // add the point to the list
      foundPointsList->push_back( maxLoc );
      confidencesList->push_back( maxValue );
      
      // if we are only looking for a single target, we have found it, so we
      //  can return
      if( !findMultipleTargets )
      {
        break;
      }
    }
  }
  
  if( foundPointsList->empty() )
  {
    // Target was not found to required confidence of "matchPercentage".
  }

  delete [] locations;
  cvReleaseImage( &searchImage );
  
  return true;
}

//=============================================================================

void MultipleMaxLoc( const IplImage& image, 
                     CvPoint**       locations,
                     int             numMaxima )
{
  // initialize input variable locations
  *locations = new CvPoint[numMaxima];
  
  // create array for tracking maxima

  double *maxima = new double[numMaxima]; // GC
  // double maxima[numMaxima];

  for( int i = 0; i < numMaxima; i++ )
  {
    maxima[i] = 0.0;
  }
  
  // extract the raw data for analysis
  float* data;
  int step;
  CvSize size;
  
  cvGetRawData( &image, ( uchar** )&data, &step, &size );
  
  step /= sizeof( data[0] );
  
  for( int y = 0; y < size.height; y++, data += step )
  {
    for( int x = 0; x < size.width; x++ )
    {
      // insert the data value into the arry if it is greater than any of the
      //  other array values, and bump the other values below it, down
      for( int j = 0; j < numMaxima; j++ )
      {
        if( data[x] > maxima[j] )
        {
          // move the maxima down
          for( int k = numMaxima - 1; k > j; k-- )
          {
            maxima[k] = maxima[k-1];
            ( *locations )[k] = ( *locations )[k-1];
          }
          
          // insert the value
          maxima[j] = ( double )data[x];
          ( *locations )[j].x = x;
          ( *locations )[j].y = y;
          break;
        }
      }
    }
  }
  delete [] maxima;  // GC
}

//=============================================================================

void
DrawFoundTargets( IplImage*              image,
                  const CvSize&          size,
                  const vector<CvPoint>& pointsList,
                  const vector<double>&  confidencesList,
                  int                    red,
                  int                    green,
                  int                    blue )
{
  int numPoints = pointsList.size();
  for( int currPoint = 0; currPoint < numPoints; currPoint++ )
  {
    const CvPoint& point = pointsList[currPoint];
    
    // draw a circle at the center
    cvCircle( image, point, 2, CV_RGB( red, green, blue ) );
    
    // draw a rectangle around the found target
    CvPoint topLeft;
    topLeft.x = point.x - size.width / 2;
    topLeft.y = point.y - size.height / 2;
    
    CvPoint bottomRight;
    bottomRight.x = point.x + size.width / 2;
    bottomRight.y = point.y + size.height / 2;
    
    cvRectangle( image, topLeft, bottomRight, CV_RGB( red, green, blue ) );
  }
}
