/* $Id: image_processing.cpp 5 2009-03-12 22:30:56Z mw $
 * +-------------------------------------------------------------------+
 * | This file contains parts of code from the application created for |
 * | the Master thesis supervised by Marek Wnuk (Wroclaw University of |
 * | Technology):  "Wykorzystanie systemu wizyjnego do rozpoznawania   |
 * | emocji czlowieka" ("Vision system in human emotions recognition") |
 * | by Marcin Namysl in June 2008.                                    |
 * +-------------------------------------------------------------------+
 *
 * \author Marek Wnuk
 * \date 2009.03.03
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

/*! \file image_processing.cpp
 *
 * File defines class implementing image processing methods:
 * \link ImgProcMethods::hysteresisThresholding hysteresis thresholding\endlink,
 * \link ImgProcMethods::createOutline silhouette outline creation\endlink,
 * \link ImgProcMethods::doHoughTransform Hough transform\endlink, etc.
 * 
 */

#include "facet.h"

using std::bitset;
using std::list;
using std::cerr;


/********************** IMAGE PROCESSING METHODS *************************/

int ImgProcMethods::findConnectedRegions(IplImage *src, bool draw, 
						  IplImage *dst)
{
  CvSeq* contour = 0;
  IplImage *src_copy = cvCreateImage(cvGetSize(src),src->depth,src->nChannels);
  cvCopy(src, src_copy);
  
  cvClearMemStorage( contours_storage );

  cvFindContours( src_copy, contours_storage, &contour, sizeof(CvContour), 
		  CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE );

  int cont_ctr = 0;
  for( ; contour != 0; contour = contour->h_next ){

    if (draw) {
      cvZero(dst);
      cvDrawContours(dst,contour,CV_RGB(rand()&255,rand()&255,rand()&255),
		     CV_RGB(rand()&255,rand()&255,rand()&255),-1,CV_FILLED,8);
    }
    cont_ctr++;
  }
  cvReleaseImage( &src_copy );
  return cont_ctr;
}

void 
ImgProcMethods::detectObjectHaar(IplImage *src, 
					 CvHaarClassifierCascade* cascade,
					 FaceComponent &element, double scale,
					 double scale_factor, 
					 int min_neighbors)
{

  CvSize object_size = cvSize(cvRound(cvGetSize(src).width/3),
			      cvRound(cvGetSize(src).height/4));
  
  element = FaceComponent(cvPoint(1,1),cvPoint(2,2));
  if( !cascade )
    {
      cerr << "ERROR: Loading the object cascade file failed\n";
      return;
    }

  cvClearMemStorage( storage );
  

  if( cascade )
    {
      CvSeq* objects = 
	cvHaarDetectObjects(src, cascade, storage, scale_factor, min_neighbors,
			    CV_HAAR_DO_CANNY_PRUNING, object_size);

      for(int i = 0; i < (objects ? objects->total : 0); i++ )
        {
	  CvRect* r = (CvRect*)cvGetSeqElem( objects, i );

	  double W = r->width - r->width*scale;
	  double H = r->height - r->height*scale;

	  CvPoint pt1, pt2;

	  pt1.x = (r->x + cvRound(W/2));
	  pt2.x = (r->x + r->width - cvRound(W/2));
	  pt1.y = (r->y + cvRound(H/2));
	  pt2.y = (r->y + r->height - cvRound(H/2));
	
	  element = FaceComponent(pt1, pt2);
        }
    }
  return;
}

/* left = 1000 (8)
 * right = 0100 (4)
 * top = 0010 (2)
 * bottom = 0001 (1)
 *
 * top&bottom = 0011 (3)
 * left&right = 1100 (12)
 * top&left = 1010 (10)
 * top&right = 0110 (6)
 * top&left = 1001 (9)
 * bottom&right = 0101 (5)
 *
 * left&right&top = 1110 (14)
 * left&right&bottom = 1101 (13)
 * top&bottom&left = 1011 (11)
 * top&bottom&right = 0111 (7)
 *
 * all = 1111 (15)
 */
void ImgProcMethods::removeBoundaryBlobs(IplImage *src, IplImage *dst, 
					       bitset<4> flag)
{
  int border = 1;

  int left   = flag[3]*border;
  int right  = flag[2]*border;
  int up     = flag[1]*border;
  int bottom = flag[0]*border;

  IplImage *temp = cvCreateImage(cvGetSize(src), 8, 1);
  IplImage *tmp = cvCreateImage(cvGetSize(src), 8, 1);
  cvSet(temp, cvScalar(255));

  cvSetImageROI(temp, cvRect(left, up, temp->width-right-left, 
			     temp->height-bottom-up));
  cvZero(temp);
  cvResetImageROI(temp);

  IplImage *dst_copy=cvCreateImage(cvGetSize(dst), dst->depth, dst->nChannels);

  do {
    cvDilate(temp, dst_copy);
    cvAnd(dst_copy, src, dst_copy);
    cvXor(dst_copy, temp, tmp);
    cvCopy(dst_copy, temp);
  } while (cvCountNonZero(tmp));
  cvSub(src, dst_copy, dst_copy);
  cvCopy(dst_copy,dst);
  cvReleaseImage( &dst_copy );
  cvReleaseImage( &temp );
  cvReleaseImage( &tmp );
}

void ImgProcMethods::removeSmallBlobs(IplImage *src, IplImage *dst, 
					   int iter)
{
  IplImage *tmp = cvCreateImage(cvGetSize(src), 8, 1);
  cvCopy(src, tmp);

  //remove small objects 
  for (int i=0;i<iter;i++) cvErode(tmp,tmp);
  for (int i=0;i<iter;i++){
    cvDilate(tmp,tmp);
    //cvAnd(src,dst,src);
  }
  cvCopy(tmp,dst);
  cvReleaseImage( &tmp );
}


void ImgProcMethods::createOutline(IplImage *src, IplImage *dst)
{
  IplImage *dst_copy = cvCreateImage(cvGetSize(dst),dst->depth,dst->nChannels);
  cvCopy(dst, dst_copy);

  cvErode(src, dst_copy);
  cvSub(src, dst_copy, dst);				

  cvReleaseImage( &dst_copy);
}


void ImgProcMethods::findContours(IplImage *src, CvSeq *&contours)
{   
  cvClearMemStorage( contours_storage );

  cvFindContours( src, contours_storage, &contours, sizeof(CvContour),
		  CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE );
}

int ImgProcMethods::doHoughTransform(IplImage *src, 
					     list<CvPoint> &linesList) { 
  IplImage *image = cvCreateImage(cvGetSize(src),8,1);
  
  if (image->nChannels > 1) cvCvtColor(src, image, CV_BGR2GRAY);
  else cvCopy(src, image);
    
  cvClearMemStorage( storage );
  stretchHistogram(image, image);
  int average = cvRound(cvAvg(image).val[0]);
  if (!average) average=1; 

  int width = image->width/3;
  if (!width) average=1;

  CvSeq* lines = cvHoughLines2( image, storage, CV_HOUGH_PROBABILISTIC, 1, 
				CV_PI/360, average, image->width/3, 1 );

  int wrinkles_counter = 0;

  for(int i = 0; i < lines->total; i++ ){
    
    CvPoint* line = (CvPoint*)cvGetSeqElem(lines,i);

    double A,B,C;
    drawStrLine(line[0],line[1],A,B,C);
    double kat = -rad2deg(calcAngleOX(A,B));

    if ((kat < 30) & (kat > -30)){
      linesList.push_front(line[0]);
      linesList.push_front(line[1]);
      wrinkles_counter++;
    }
  } 
  cvReleaseImage( &image );
  return wrinkles_counter;
}

bool ImgProcMethods::checkPixelNeighbourhood(IplImage *src, int y, int x,
						  int R)
{
  for (int i=y-1; i<=y+1; i++)
    for (int j=x-1; j<=x+1; j++)
      if ((i>=0) & (j>=0) & (i<src->height) & (j<src->width))
	if ((int)CV_IMAGE_ELEM(src,uchar,i,j)<R)
	  return true;
  return false;
}

void ImgProcMethods::hysteresisThresholding(IplImage *src, IplImage *dst, 
					     int R, int L)
{
  IplImage *temp = cvCreateImage(cvGetSize(dst), dst->depth, dst->nChannels);
  cvZero(temp);

  for (int i=0; i<(src->height); i++){
    for (int j=0; j<(src->width); j++){
      int w = (int)CV_IMAGE_ELEM(src,uchar,i,j); 
      if (w<R)
	CV_IMAGE_ELEM(temp,uchar,i,j)= 255;
      else if (w<L){
	if (checkPixelNeighbourhood(src,i,j,R))
	  CV_IMAGE_ELEM(temp,uchar,i,j)= 255;
	else
	  CV_IMAGE_ELEM(temp,uchar,i,j)= 0;
      }
      else
	CV_IMAGE_ELEM(temp,uchar,i,j)= 0;
    }
  }
  cvCopy(temp,dst);
  cvReleaseImage( &temp );
}

IplImage *ImgProcMethods::showHistogram(const IplImage *src, bool display, CvSize size)
{
  IplImage *dst=cvCloneImage(src);
  IplImage *hist_image;
  CvHistogram *hist;

  int hist_size = 256;
  float range_0[]={0,256};
  float* ranges[] = { range_0 };
  CvMat *lut_mat;
  uchar lut[256];
  float max_value = 0.f, min_value = 0.f;
  int bin_w;
  CvFont font;
  double hScale = 0.8;
  double vScale = 0.8;
  int lineWidth = 1;
  int offset=80;
  int xsize=size.width;
  int ysize=size.height;
  char buffer[4];

  hist_image=cvCreateImage(cvSize(xsize+offset,ysize+2*offset), 8, 3);
  hist = cvCreateHist(1, &hist_size, CV_HIST_ARRAY, ranges, 1);
  lut_mat = cvCreateMatHeader( 1, 256, CV_8UC1 );
  cvSetData( lut_mat, lut, 0 );

  cvCalcHist(&dst,hist,0);
  cvGetMinMaxHistValue(hist,&min_value,&max_value);
  cvScale( hist->bins, hist->bins, ((double)ysize)/max_value, 0 );

  cvSet( hist_image, cvScalarAll(255), 0 );
  bin_w = cvRound((double)xsize/hist_size);

  cvInitFont(&font, CV_FONT_HERSHEY_PLAIN, hScale, vScale,
	     0, lineWidth);

  cvPutText (hist_image, "some text", cvPoint(90, 90),
	     &font, cvScalar(0,0,0));

  cvRectangle(hist_image,cvPoint(offset/2,offset),
	      cvPoint(xsize+offset/2,ysize+offset),cvScalar(255,248,240),-1);
  
  for (int i=0; i<ysize; i+=20){
    if ((i%100==0)&(i!=0)){
      cvLine(hist_image,cvPoint(offset/2-5,ysize+offset-i),
	     cvPoint(xsize+offset/2,ysize+offset-i),cvScalar(0,0,0),1);

      sprintf (buffer, "%d", i); // i -> char buffer

      cvPutText(hist_image,buffer,cvPoint(offset/2-15*bin_w,ysize+offset-i),
		&font , cvScalar(0,0,0));
      
    }
    else{
      cvLine(hist_image,cvPoint(offset/2,ysize+offset-i),
	     cvPoint(xsize+offset/2,ysize+offset-i),cvScalar(0,0,0),1);
    }
  }

  for (int i=0; i<ysize; i+=100){
    cvLine(hist_image,cvPoint(offset/2-5,ysize+offset-i),
	   cvPoint(xsize+offset/2,ysize+offset-i),cvScalar(0,0,0),1);
  }

  for(int i = 0; i < hist_size; i++ ){

    int y= cvRound(cvGetReal1D(hist->bins,i));
    int x=bin_w;

    cvRectangle( hist_image, cvPoint(i*x+offset/2, ysize+offset),
		 cvPoint((i+1)*x+offset/2, ysize - y+offset),
		 cvScalar(131,139,139), -1, 8, 0 );

    sprintf (buffer, "%d", i); // i -> char buffer

    if (i%50==0){
  
      cvPutText(hist_image,buffer,cvPoint(i*x+offset/2-5*x,
					  cvRound(ysize+offset*1.8)),
		&font, cvScalar(0,0,0));
      cvLine(hist_image,cvPoint(i*x+offset/2,cvRound(ysize+offset*1.3)),
	     cvPoint(i*x+offset/2,offset),cvScalar(255,255,255),2,CV_AA);
      
      cvLine(hist_image,cvPoint(i*x+offset/2,cvRound(ysize+offset*1.3)),
	     cvPoint(i*x+offset/2,offset),cvScalar(0,0,255),1);
      
    }
    else if (i%10==0){
      cvLine(hist_image,cvPoint(i*x+offset/2,cvRound(ysize+offset*1.3)),
	     cvPoint(i*x+offset/2,offset),cvScalar(0,0,0));

    }
  }
	  
  cvLine(hist_image,cvPoint(offset/2,offset),
	 cvPoint(xsize+offset/2,offset), cvScalar(0,0,0));

  // display histogram image
  if (display){
    cvNamedWindow("histogram", 1);
    cvShowImage( "histogram", hist_image );
    cvWaitKey(0);
  }

  cvReleaseImage( &dst );
  cvReleaseMat( &lut_mat );

  return hist_image;
}

void ImgProcMethods::stretchHistogram(const IplImage* src, IplImage *dst)
{
  double min,max;
  
  cvMinMaxLoc ( src, &min, &max );

  if( min != max )
    cvConvertScaleAbs ( src, dst, 255/(max-min), -min );
  else
    cvZero ( dst );
}

int ImgProcMethods::findThresholdByHist(IplImage *src, int factor)
{
  int size=256;
  float range[2] = {0, size};
  float *ranges[] = {range};
  
  CvHistogram *histogram = cvCreateHist(1, &size, CV_HIST_ARRAY, ranges, 1);
  
  cvCalcHist(&src, histogram, 0);
  
  int intensity_sum = 0;
  int current_sum = 0;

  intensity_sum = src->width * src->height;

  for(int i = 0; i < size; i++ ){

    current_sum += cvRound(cvGetReal1D(histogram->bins,i));

    if (current_sum >= ((intensity_sum * factor)/100) ) {
      cvReleaseHist( &histogram );
      return i;
    }
  }

  cvReleaseHist( &histogram );
  std::cerr << "Error in function 'findThresholdByHist()' \n";

  return -1;
}

