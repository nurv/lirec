/* $Id: face_regions.cpp 5 2009-03-12 22:30:56Z mw $
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

/*! \file face_regions.cpp
 *
 * The file defines a set of functions for face subregions (mouth and eyes) 
 * detection using gradient projection method.
 *
 * It contains FaceComponent class definition, which can be used to describe 
 * a single face component (mouth, eyes, eyebrows, nose, forehead). 
 * The FFace class is defined, which contains objects of type FaceComponent  
 * (mouth, eyes, eyebrows, forehead, eyelids). 
 * 
 */

#include <math.h> 

#include "facet.h"

void FFace::clearElements()
{
  mouth.clear();
  left_eye.clear();
  right_eye.clear();
  left_iris.clear();
  right_iris.clear();
  left_brow.clear();
  right_brow.clear();
  left_lid.clear();
  right_lid.clear();
  lip.clear();
  nose.clear();
  forehead.clear();
}



void counting(CvMat *matrix, int tab[MAX_SIZE], proj_type type, 
	       count_mode mode, int start, int end)
{
  if (type==PROJ_HORIZONTAL)
    for (int i=0; i < matrix->rows; i++){
      tab[i]=0;
      for (int j=start; j < end ; j++){
	if (mode==CNT_MODE_BINARY){
	  tab[i]+= (cvGetAt(matrix,i,j).val[0]>0) ? 1 : 0;
	}
	else if (mode==CNT_MODE_NORMAL){
	  tab[i]+=(int)cvGetAt(matrix,i,j).val[0];	  
	}
      }
    }
  else if (type==PROJ_VERTICAL)
    for (int i=0; i < matrix->cols; i++){
      tab[i]=0;
      for (int j=start; j < end ; j++){
	if (mode==CNT_MODE_BINARY){
	  tab[i]+= (cvGetAt(matrix,j,i).val[0]>0) ? 1 : 0;
	}
	else if (mode==CNT_MODE_NORMAL){
	  tab[i]+=(int)cvGetAt(matrix,j,i).val[0];	  
	}
      }
    }
}


void drawProjection(IplImage *img, int tab[MAX_SIZE], int start, int end,
		    proj_type type, CvScalar color)
{
  CvPoint pt1 = {0,0};
  CvPoint pt2 = {0,0};
  for (int i=start+1; i < end; i++){
    if (type==PROJ_VERTICAL){
      pt1 = cvPoint( i-1, img->height - (tab[i-1] - 1)/70 );//300
    pt2 = cvPoint( i, img->height - (tab[i] - 1)/70 );
    }
    else if (type==PROJ_HORIZONTAL){
      pt1 = cvPoint((tab[i-1]-1)/50,i-1);
      pt2 = cvPoint((tab[i]-1)/50,i);
    }
    else {
      cerr << "Error in function 'drawProjection()\n";
      cerr << "(Illegal projection type).\n";
    }
    cvLine(img,pt1,pt2,color,1,8);
  }
}


int horizEyePosition(int tab[MAX_SIZE], int upperb, int lowerb)
{
  int max_horiz=0;
  int max_val;

  for (int i=lowerb; i < upperb; i++){
    if (tab[i]>max_horiz){
      max_horiz = tab[i];
      max_val = i;
    } 
  }

  int second_max=0;
  int max_val2;

  for (int i=lowerb; i < upperb; i++) {
    if ( (tab[i]>second_max) && (tab[i]<=max_horiz) && 
	 (abs(max_val-i)>(upperb-lowerb)/4) ) {
      second_max = tab[i];
      max_val2 = i;
    } 
  }
  return max(max_val, max_val2) - cvRound(abs(max_val2-max_val))/2;
}


int horizMouthPosition(int tab[MAX_SIZE], int img_height, int upperb)
{
  int max_horiz=0;
  int ret_val = 0;
  for (int i=upperb; i < img_height; i++){
    if (tab[i]>max_horiz){
      max_horiz = tab[i];
      ret_val = i;
    }
  }
  return ret_val;
}


void vertEyeBorder(int tab[MAX_SIZE], int img_width, int &ind_b, int &ind_e)
{
  int max_b, max_e;
  max_b=max_e=0;

  for (int i=0; i < img_width; i++){
    if ( (i<=img_width/2) && (tab[i]>max_b) ){
      max_b = tab[i];
      ind_b = i;
    }
    if ( (i>img_width/2) && (tab[i]>max_e) ){
      max_e = tab[i];
      ind_e = i;
    }    
  }
}

double rint(double x) //CHANGE added rint function GC
{
  //middle value point test
  if (ceil(x+0.5) == floor(x+0.5))
    {
      int a = (int)ceil(x);
      if (a%2 == 0)
	{return ceil(x);}
      else
	{return floor(x);}
    }
  
  else return floor(x+0.5);
}


int vertEyeCenter(int tab[MAX_SIZE], int img_width, int offset)
{
  int max_vert=0;
  int ret_val = 0;
  for (int i=0; i < img_width; i++){

    tab[i]=(int)rint(tab[i]/(2*offset)); //CHANGE round function GC
    //tab[i]=(int)round(tab[i]/(2*offset));

    if ( (tab[i]>max_vert) && (i>img_width/4) && (i<3*img_width/4) ){
      max_vert = tab[i];
      ret_val = i;
    }
  }
  return ret_val;
}


void vertMouthBorder(int tab[MAX_SIZE], int width, int start, int end, 
		     int &lower_border, int &upper_border)
{
  int cntr=start;         // tab elements counter
  int count=0;            // range elements counter
  bool find_start=true;   // start of range searching mode
  bool find_end=false;    // end of range searching mode

  int range=(int)rint(width/10); //CHANGE round GC
  //int range=(int)round(width/10); 

  // lower bound of the possible vertical mouth projection

  upper_border=lower_border=start;

  while (cntr++ < end){
    if (find_start){
      if (tab[cntr]>=100) count++;
      else 
	if (count<=range) count = 0;
	else { 
	  lower_border=cntr-count;
	  upper_border=cntr;
	  find_end=true;
	  find_start=false;
	  count=0;
	}
    }
    if (find_end){
      if (tab[cntr]>=100) count++;
      else 
	if (count<=range) count = 0;
	else {
	  upper_border=cntr;
	  count=0;
	}
    }
  }
}


double eyeMatching(IplImage *img, IplImage *templ, CvPoint &topLeft)
{
  vector<CvPoint> foundPointsList;
  vector<double> confidencesList;

  if( (templ->width <= img->width) || (templ->height <= img->height) ) {
    topLeft.x = 0;
    topLeft.y = 0;
    cerr << "ERROR: Fast match template failed (size(temp)>size(img))\n";
    return 0;
  }

  else if(!FastMatchTemplate(*img, *templ, &foundPointsList, &confidencesList,
			90, false))
    cerr << "\nERROR: Fast match template failed (< 90%).\n";
  
  int numPoints = foundPointsList.size();

  double max_confidence = 90;


  for( int currPoint = 0; currPoint < numPoints; currPoint++ )
  {
    const CvPoint& point = foundPointsList[currPoint];
    
    if ( confidencesList[currPoint] >= max_confidence ) { //*****

      max_confidence = confidencesList[currPoint];
      
      CvSize size = cvGetSize( templ );

      topLeft.x = point.x - size.width / 2;
      topLeft.y = point.y - size.height / 2;
    }
  }

  if (numPoints==0){
    topLeft.x = 0;
    topLeft.y = 0;
  }

  return max_confidence;
}


void detectFacialRegions(IplImage *src,  FaceComponent &eye_left,
			 FaceComponent &eye_right, FaceComponent &mouth, 
			 int &width, bool detect_mouth)
{
  IplImage *tmp  = 0;  // tmp image
  IplImage *grad = 0;  // gradient image
  IplImage *img  = 0;  // gradient with projections and boudaries

  int* ver_val = new int [src->width];  //CHANGE GC
  // int ver_val[src->width];//values for vertical projection
  int* hor_val = new int [src->height];
  // int hor_val[src->height];//values for horizontal projection

  int x[9] = {0,1,0,1,1,1,0,1,0}; 

  int EHC, FC, MHC;
  int EV1, EV2, EH1, EH2;
  int MV1, MV2, MH1, MH2;

  int lowerbound=1*src->height/4; // eyes search region boudaries
  int upperbound=3*src->height/6;

  int offset=src->height/4;

  int off=(int)rint(min(src->width,src->height)/50); //CHANGE round GC
  // int off=(int)round(min(src->width,src->height)/50);

  int sum,count,average;
  
  tmp  = cvCreateImage(cvGetSize(src),src->depth,src->nChannels);
  cvCopy(src,tmp);

  img  = cvCreateImage(cvGetSize(src),src->depth,src->nChannels);
  grad = cvCreateImage(cvGetSize(src), src->depth, 1);
  
  // calculate gradient of the source image
  cvSmooth(src,src,3,3,1,1);
  
  IplConvKernel *kernel = cvCreateStructuringElementEx(3, 3, 1, 1, 
						       CV_SHAPE_CUSTOM, x);
  cvMorphologyEx(src, img, tmp, kernel, CV_MOP_GRADIENT, 1); //gradient image
  cvReleaseStructuringElement( &kernel );

  cvCvtColor(img, grad, CV_RGB2GRAY);

  //CvMat *matc1 = 0; // gradient image matrix
  //CvMat *matc3 = 0; // source image matrix
  CvMat mat_temp;

  //matc1 = cvCreateMat(grad->height, grad->width, CV_8UC1);
  //matc3 = cvCreateMat(src->height, src->width, CV_8UC3);

  cvGetMat(grad,&mat_temp);//get gradient image matrix //***

  /////////////// Operators for finding the eyes region //////////////////
    
  counting(&mat_temp,ver_val,PROJ_VERTICAL,CNT_MODE_NORMAL,0,src->height); 
  
  // vertical eyes boudaries
  vertEyeBorder(ver_val,src->width,EV1,EV2);

  width = EV2 - EV1; // eststimated face width

  if (EV1<1) EV1=1;
  if (EV2>src->height) EV2=src->height;
    
  counting(&mat_temp,hor_val,PROJ_HORIZONTAL,CNT_MODE_NORMAL,EV1+2*off,EV2-2*off);

  // horizontal eyes location
  EHC = horizEyePosition(hor_val,upperbound,lowerbound);

  if (EHC<1) EHC=1;
  if (EHC>img->height) EHC=img->height;

  EH1 = EHC-(int)rint(0.5*offset); //CHANGE round GC
  EH2 = EHC+(int)rint(0.8*offset);
  // EH1 = EHC-(int)round(0.5*offset);
  // EH2 = EHC+(int)round(0.8*offset);

  if (EH1<1) EH1=1;
  if (EH2>src->height) EH2 = src->height;
  
  cvGetMat(src, &mat_temp); // get source image matrix

  counting(&mat_temp,ver_val,PROJ_VERTICAL,CNT_MODE_NORMAL,
  	    EH1,EH2);

  // find vertical eye center
  FC = vertEyeCenter(ver_val,src->width,offset);

  if (FC<1) FC=1;
  if (FC>img->height) FC = img->height;

  // vertical projection - find the face center
  // coordinates of eyes bounding rectangles 
  eye_left.p1=cvPoint(EV1, EH1);
  eye_left.p2=cvPoint(FC-off, EH2);

  eye_right.p1=cvPoint(FC+off, EH1-(int)rint(0.3*offset)); //CHANGE round GC
  // eye_right.p1=cvPoint(FC+off, EH1-(int)round(0.3*offset));

  eye_right.p2=cvPoint(EV2, EH2);
  
  if (detect_mouth) {
    /////////////// Operators for finding the mouth region /////////////////

    int mouthbound = 3*src->height/4;
    MHC = horizMouthPosition(hor_val, src->height, mouthbound);

    if (MHC<1) MHC=1;
    if (MHC>img->height) MHC = img->height;

    MH1 = MHC-(int)(0.4*offset);
    //MH2 = MHC+(int)(0.8*offset);
    MH2 = src->height;

    if (MH1<1) MH1 = 1;
    if (MH2>src->height) MH2 = src->height;

    cvGetMat(grad,&mat_temp,0); // get gradient image matrix
  
    // vertical projection in the mouth region
    counting(&mat_temp,ver_val,PROJ_VERTICAL,CNT_MODE_NORMAL,
	      MH1,MH2);

    sum=count=average=0;

    for (int i=eye_left.p1.x; i < eye_right.p2.x; i++) sum+=ver_val[i];
    count = eye_right.p2.x - eye_left.p1.x;

    average = (int)rint(sum/count); //CHANGE round GC
    // average = (int)round(sum/count);

    for (int i=eye_left.p1.x; i < eye_right.p2.x; i++){
      if ((ver_val[i]>average/2) && (i>src->width/4) && (i<3*src->width/4)){
	ver_val[i]=100;
      }
      else ver_val[i]=10;
    }
  
    // vertical mouth projection drawing
    // vertical boundaries for the mouth region
    vertMouthBorder(ver_val, src->width, eye_left.p1.x, eye_right.p2.x, 
		    MV1, MV2);

    // mouth limits relaxation
    if (MV1<=0) MV1=FC;
    if (MV2>=src->width) MV2=FC;
    
    MV1-=offset/5;
    MV2+=offset/5;

    // coordinates of mouth bounding box
    mouth.p1 = cvPoint(MV1,MH1);  
    mouth.p2 = cvPoint(MV2,MH2);
  }

  cvReleaseImage( &img );
  cvReleaseImage( &tmp );
  cvReleaseImage( &grad );

  //  cvReleaseMat( &matc1 );
  //  cvReleaseMat( &matc3 );

  delete [] ver_val;	//CHANGE delete allocated memory GC
  delete [] hor_val;

}

double eyeDetection(IplImage *eye_img, IplImage *templ, int correction, 
		    FaceComponent &iris)
{
  int w,h;
  /*
  h=(int)ceil(width/10);
  w=2*h;
  */
  
  h=static_cast<int>(ceil((float)eye_img->width/14));
  w=static_cast<int>(ceil((float)eye_img->height/6));

  if (eye_img->width <= w ) w++;
  if (eye_img->height <= h ) h++;
  
  IplImage *scale = cvCreateImage(cvSize(w, h),
				  IPL_DEPTH_8U,1 );
  
  cvResize(templ,scale);
  //IplImage *scale = cvCreateImage(cvGetSize(templ),8,1);
  //cvCopy(templ,scale);

  if ((eye_img->width) <= 0) {
    iris = FaceComponent(cvPoint(0,0),cvPoint(0,0));
    return 0;
  }

  IplImage *img = cvCreateImage(cvGetSize(eye_img), 8, 1);

  cvCvtColor(eye_img, img, CV_RGB2GRAY);

  CvPoint iris_pt1;

  double confidence = eyeMatching(img, scale, iris_pt1);

  iris_pt1 = cvPoint(iris_pt1.x,iris_pt1.y-correction);

  CvPoint iris_pt2 = cvPoint(iris_pt1.x+scale->width-1,
			     iris_pt1.y+scale->height-1);

  iris = FaceComponent(iris_pt1,iris_pt2);

  cvReleaseImage( &img );
  cvReleaseImage( &scale );

  return confidence;
}


void checkSizeOczu(FaceComponent &el1, FaceComponent &el2)
{
  // Check the eye regions size constraints
  
  if (el1.p1.x > el1.p2.x) {
    int tmp = 0;
    tmp = el1.p1.x;
    el1.p1.x = el1.p2.x;
    el1.p2.x = tmp;
  }
  else if (el1.p1.x == el1.p2.x)
    el1.p2.x += 1;
  
  if (el1.p1.y > el1.p2.y) {
    int tmp = 0;
    tmp = el1.p1.y;
    el1.p1.y = el1.p2.y;
    el1.p2.y = tmp;
  }
  else if (el1.p1.y == el1.p2.y)
    el1.p2.y += 1;
  
  if (el2.p1.x > el2.p2.x) {
    int tmp = 0;
    tmp = el2.p1.x;
    el2.p1.x = el2.p2.x;
    el2.p2.x = tmp;
  }
  else if (el2.p1.x == el2.p2.x)
    el2.p2.x += 1;
  
  if (el2.p1.y > el2.p2.y) {
    int tmp = 0;
    tmp = el2.p1.y;
    el2.p1.y = el2.p2.y;
    el2.p2.y = tmp;
  }
  else if (el2.p1.y == el2.p2.y)
    el2.p2.y += 1;
}

void findFaceRegions(IplImage *source_img, FFace &face, bool detect_mouth,
			   bool detect_eyeballs)
{
  FaceComponent eye_right, eye_left, mouth;

  IplImage *result_img = cvCreateImage(cvGetSize(source_img),source_img->depth,
				       source_img->nChannels);
  cvCopy(source_img,result_img);

  detectFacialRegions(result_img, eye_left, eye_right, mouth, face.width, 
		      detect_mouth);

  checkSizeOczu(eye_left, eye_right);

  FaceComponent liris, riris;

  int lo_p1y=eye_left.p1.y;
  int po_p1y=eye_right.p1.y;
  int correction = 0;

  if (detect_eyeballs) {
    IplImage *templ_l = 0;
    IplImage *templ_r = 0;
    IplImage *leye_img = NULL;
    IplImage *reye_img = NULL;

    // eye_left.p1.y contains lower value of Y coord. (???)
    // eye_right.p1.y greater (???)
    
    int diff_leye_x = eye_left.p2.x - eye_left.p1.x;
    int diff_peye_x = eye_right.p2.x - eye_right.p1.x;

    if (diff_leye_x<0) eye_left.p1.x = eye_left.p2.x;
    if (diff_peye_x<0) eye_right.p1.x = eye_right.p2.x;
    
    correction = po_p1y - lo_p1y;

    double confid_l, confid_r;
  
    templ_l = cvLoadImage("input/leye.jpg", 0); // MW: to be improved
    templ_r = cvLoadImage("input/peye.jpg", 0);

    cvSetImageROI(result_img, cvRect(eye_left.p1.x, eye_left.p1.y,
				     eye_left.p2.x-eye_left.p1.x,
				     eye_left.p2.y-eye_left.p1.y));
    confid_l = eyeDetection(result_img, templ_l, correction, liris);
    cvResetImageROI(result_img);

    cvSetImageROI(result_img, cvRect(eye_right.p1.x, eye_right.p1.y,
				     eye_right.p2.x-eye_right.p1.x,
				     eye_right.p2.y-eye_right.p1.y));
    confid_r = eyeDetection(result_img, templ_r, correction, riris);
    cvResetImageROI(result_img);

    cvReleaseImage( &templ_r );
    cvReleaseImage( &templ_l );
    cvReleaseImage( &leye_img );
    cvReleaseImage( &reye_img );
  }

  cvReleaseImage( &result_img );

  eye_left.p1.y = po_p1y;

  face.left_eye = eye_left;
  face.right_eye = eye_right;
  
  if (detect_mouth)  face.mouth = mouth;  

  if (detect_eyeballs) {

    riris.p1.y += correction;
    riris.p2.y += correction;

    face.left_iris = liris;
    face.right_iris = riris;

    face.left_iris.p1 = cvPoint(face.left_iris.p1.x+face.left_eye.p1.x,
				face.left_iris.p1.y+face.left_eye.p1.y);
    face.left_iris.p2 = cvPoint(face.left_iris.p2.x+face.left_eye.p1.x,
				face.left_iris.p2.y+face.left_eye.p1.y);
    face.right_iris.p1=cvPoint(face.right_iris.p1.x+face.right_eye.p1.x,
			       face.right_iris.p1.y+face.right_eye.p1.y);
    face.right_iris.p2=cvPoint(face.right_iris.p2.x+face.right_eye.p1.x,
			       face.right_iris.p2.y+face.right_eye.p1.y);
  }
}

/*
void example_usage(char *twarz)
{
  IplImage *input_img = NULL;

  FFace face;

  input_img = cvLoadImage( twarz, -1 );

  findFaceRegions(input_img, face);
  
  //I5 cvNamedWindow( "Source", 1 );
  //I5 cvShowImage( "Source", input_img );
  //I5 cvDestroyWindow("Source");
  cvWaitKey(0);

  cvReleaseImage( &input_img );
}
*/
