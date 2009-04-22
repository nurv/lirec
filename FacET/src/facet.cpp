/* $Id: facet.cpp 5 2009-03-12 22:30:56Z mw $
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

/*! \file facet.cpp
 *
 * File defines the class for face features detection, parameterisation 
 * and displaying them in the image.
 * 
*/

#include "facet.h"

facepar_t *Facet::currfacptr = NULL;

CvMemStorage *HaarParameters::face_storage = cvCreateMemStorage(0);
CvMemStorage *ImgProcMethods::contours_storage = cvCreateMemStorage(0);
CvMemStorage *ImgProcMethods::storage = cvCreateMemStorage(0);

/***************************** EMOTIONS ******************************/

Facet::Facet() {

  if (!readSettings()) 
    std::cerr << "ERROR: reading the default parameters (default.cfg)\n";

  mouthHaar = true; 
  noseHaar = false; 
  eyeballsHaar = true;
}

Facet::~Facet() { 
  cvDestroyAllWindows();
  cvReleaseHaarClassifierCascade( &cascade_face );
  cvReleaseHaarClassifierCascade( &cascade_leftEye );
  cvReleaseHaarClassifierCascade( &cascade_rightEye );
  cvReleaseHaarClassifierCascade( &cascade_mouth );
  cvReleaseHaarClassifierCascade( &cascade_nose );
  cvReleaseMemStorage( &face_storage );
  cvReleaseMemStorage( &contours_storage );
  cvReleaseMemStorage( &storage );
}


bool Facet::readSettings(string file_name)
{
  std::ifstream IStrm(file_name.c_str());
  
  string parameter = "";
  string valu = "";
  double value = 0;
  char bufc[200];
  bool ok;

  void (FaceParameters::*wfun_face)(int) = 0;
  void (HaarParameters::*wfun_haar)(double) = 0;
  void (HaarParameters::*wfun_haar_s)(string) = 0;

  cerr << "----------------------------------\n" 
       << "FacET parameters from config file:\n"
       << "- File name: [" << file_name << "]:\n"
       << "- Settings:\n";

  while ( !IStrm.eof() && !IStrm.bad() ) {
    if (( IStrm >> parameter).good() ) {  

      if (parameter.at(0) == '#') {
	IStrm.getline(bufc, 199);
	IStrm.unget();
      }
      
      else { // FacET parameters
	ok = true;
	if (!parameter.compare("EYEBROWS_RATIO"))
	  wfun_face = &FaceParameters::setEyebrowsRatio;
	else if (!parameter.compare("LIPS_RATIO"))
	  wfun_face = &FaceParameters::setLipsRatio;
	else if (!parameter.compare("FACE_SCALING")) 
	  wfun_haar = &HaarParameters::setFaceScale;
	else if (!parameter.compare("FACE_PATH"))
	  wfun_haar_s = &HaarParameters::readFaceCascade;
	else if (!parameter.compare("R_EYE_PATH"))
	  wfun_haar_s = &HaarParameters::readRightEyeCascade;
	else if (!parameter.compare("L_EYE_PATH"))
	  wfun_haar_s = &HaarParameters::readLeftEyeCascade;
	else if (!parameter.compare("MOUTH_PATH"))
	  wfun_haar_s = &HaarParameters::readMouthCascade;
	else if (!parameter.compare("NOSE_PATH"))
	  wfun_haar_s = &HaarParameters::readNoseCascade;

	else { cerr << ""; ok = false; }
	
        if( ok ) {
	  if ( (IStrm >> value).good() ){
	    if (wfun_haar) (this->*wfun_haar)(static_cast<double>(value));
	    else if (wfun_face) (this->*wfun_face)(static_cast<int>(value));
	    cerr << "  " << parameter << " " << value << endl;
	    wfun_haar = NULL; wfun_face = NULL;
	  }
	  else {
	    IStrm.clear(); IStrm.ignore(); IStrm.unget();
	    if ( (IStrm >> valu).good() ){
	      if (wfun_haar_s) (this->*wfun_haar_s)(valu);
	    
	      cerr << "  " << parameter << " " << valu << endl;
	      wfun_haar_s = NULL;
	    }
	    else {IStrm.clear(); IStrm.ignore(); IStrm.unget();} 
          }
	}
        else { wfun_haar = NULL; wfun_face = NULL; wfun_haar_s = NULL; }
      }
    }
    else IStrm.clear(); IStrm.ignore(); IStrm.unget();
  }
  cerr << "----------------------------------\n" ;
  return true;
}

/*! Returns the sequence with the detected faces */ 
CvSeq *Facet::faceDetectHaar(IplImage *src)
{
  if( !cascade_face ) {
    cerr << "ERROR: Loading the face cascade file failed\n";
    return NULL;
  }
  
  IplImage* gray = cvCreateImage(cvSize(src->width,src->height), 8, 1 );
  IplImage* small_img = 
    cvCreateImage(cvSize(cvRound (src->width/face_detection_scale),
			 cvRound (src->height/face_detection_scale)), 8, 1);
  
  cvCvtColor( src, gray, CV_BGR2GRAY );
  cvResize( gray, small_img, CV_INTER_LINEAR );
  cvEqualizeHist( small_img, small_img );
  
  cvClearMemStorage( face_storage );
  
  CvSeq *faces;
  
  if( cascade_face ) {
    faces = cvHaarDetectObjects(small_img, cascade_face, face_storage, 1.1, 
				3, CV_HAAR_DO_CANNY_PRUNING, cvSize(30, 30));
    cvReleaseImage( &gray );
    cvReleaseImage( &small_img );
    
    return faces;
  }
  else {
    cvReleaseImage( &gray );
    cvReleaseImage( &small_img );
    
    return NULL;
  }
}

void Facet::detectFeat(IplImage *src, IplImage *dst)
{ 
  CvSeq *faces = faceDetectHaar(src);
  /*
    faceDetectHue(src,true,true);
  */
  if ( !faces ) return;
  if ( !faces->total) return;
  
  
  
  for(int i = 0; i < (faces ? faces->total : 0); i++ )
    {
      
      currfacptr = new facepar_t;
      
      CvRect* r = (CvRect*)cvGetSeqElem( faces, i );
      
      // face rectangle scaling (for Haar-based method)
      face.p1.x = cvRound(r->x*(face_detection_scale));
      face.p2.x = cvRound((r->x+r->width)*(face_detection_scale));
      face.p1.y = cvRound(r->y*(face_detection_scale));
      face.p2.y = cvRound((r->y+r->height)*(face_detection_scale));
      
      
      CvRect faceRectangle = cvRect(face.p1.x,face.p1.y,face.p2.x-face.p1.x,
				    face.p2.y-face.p1.y);
      
      cvSetImageROI(src, faceRectangle);
      findFaceRegions(src, face, !mouthHaar, !eyeballsHaar);
      cvResetImageROI(src);
      
      checkEyesSize();
      
      cvSetImageROI(src, cvRect(face.p1.x + face.left_eye.p1.x,
				face.p1.y + face.left_eye.p1.y,
				face.left_eye.p2.x - face.left_eye.p1.x,
				face.left_eye.p2.y - face.left_eye.p1.y));
      if (eyeballsHaar) detectLeftEyeballHaar(src);
      cvResetImageROI(src);
      cvSetImageROI(src, cvRect(face.p1.x + face.right_eye.p1.x,
				face.p1.y + face.right_eye.p1.y,
				face.right_eye.p2.x - face.right_eye.p1.x,
				face.right_eye.p2.y - face.right_eye.p1.y));
      if (eyeballsHaar) detectRightEyeballHaar(src);
      cvResetImageROI(src);
//      cvResetImageROI(src);
      
      if (mouthHaar) {
	cvSetImageROI(src, cvRect(face.p1.x, face.p1.y + 
				  cvRound(2*faceRectangle.height/3), //2,3
				  faceRectangle.width,
				  3*faceRectangle.height/7)); // 3,7
	detectMouthHaar(src);
	cvResetImageROI(src);
      }
      
      if (noseHaar) {
	cvSetImageROI(src, cvRect(face.p1.x, face.p1.y + 
				  faceRectangle.height/2, 
				  faceRectangle.width,
				  1*faceRectangle.height/2));
	detectNoseHaar(src);
	cvResetImageROI(src);
      }
      
      checkMouthSize();
      checkNoseSize();
      checkEyeballsSize();
      
      calcFaceDeclination();
      
      
      // left eyebrow transform
      bitset<4> flaga = 8;
      cvSetImageROI(src, cvRect(face.p1.x + face.left_eye.p1.x,
				face.p1.y + face.left_eye.p1.y,
				face.left_eye.p2.x - face.left_eye.p1.x, 
				face.left_eye.p2.y - face.left_eye.p1.y));
      detectEyebrow(src, face.left_eye, face.left_iris, face.left_brow,flaga);
      cvResetImageROI(src);
      
      cvSetImageROI(src,cvRect(face.p1.x + face.left_iris.p1.x, 
			       face.p1.y + face.left_iris.p1.y - 
			       cvRound((face.left_iris.p2.y - 
					face.left_iris.p1.y)/3),
			       face.left_iris.p2.x-face.left_iris.p1.x, 
			       face.left_iris.p2.y-face.left_iris.p1.y+
			       cvRound(2*(face.left_iris.p2.y - 
					  face.left_iris.p1.y)/3)));
      
      
      currfacptr->LEyOpn = detectEyelids(src, face.left_iris, face.left_lid);    
      cvResetImageROI(src);
      
      currfacptr->LEbHgt = calcLeftEyebrowHeight();
      
      // right eyebrow transform
      flaga = 4;
      cvSetImageROI(src, cvRect(face.p1.x + face.right_eye.p1.x,
				face.p1.y + face.right_eye.p1.y,
				face.right_eye.p2.x-face.right_eye.p1.x, 
				face.right_eye.p2.y-face.right_eye.p1.y));
      
      
      detectEyebrow(src,face.right_eye,face.right_iris,face.right_brow,flaga);
      cvResetImageROI(src);

      cvSetImageROI(src,cvRect(face.p1.x + face.right_iris.p1.x, 
			       face.p1.y + face.right_iris.p1.y - 
			       cvRound((face.right_iris.p2.y - 
					face.right_iris.p1.y)/3),
			       face.right_iris.p2.x-face.right_iris.p1.x, 
			       face.right_iris.p2.y-face.right_iris.p1.y+
			       cvRound(2*(face.right_iris.p2.y - 
					  face.right_iris.p1.y)/3)));
      
      currfacptr->REyOpn = detectEyelids(src, face.right_iris, face.right_lid);
      cvResetImageROI(src);
//      cvResetImageROI(src);
      currfacptr->REbHgt = calcRightEyebrowHeight();

      cvSetImageROI(src, cvRect(face.p1.x + face.mouth.p1.x, 
				face.p1.y + face.mouth.p1.y,
				face.mouth.p2.x - face.mouth.p1.x,
				face.mouth.p2.y - face.mouth.p1.y));

    
      CvSeq *lips = 0;
      detectLips(src, face.lip, lips);
      cvResetImageROI(src);
      
      list<CvPoint> wrinklesList;
      int forehead_y = min(face.left_brow.p2.y, face.right_brow.p2.y)/3;
      if (forehead_y <= 0) forehead_y = cvRound((face.p2.y-face.p1.y)/3);
      face.forehead.p1 = cvPoint((face.p2.x-face.p1.x)/4, forehead_y);
      face.forehead.p2 = cvPoint(face.forehead.p1.x + 
				 (face.p2.x-face.p1.x)/2,
				 face.forehead.p1.y + 2*forehead_y);
      
      checkForeheadSize();
      
      cvSetImageROI(src, cvRect(face.p1.x + face.forehead.p1.x,
				face.p1.y + face.forehead.p1.y, 
				(face.p2.x-face.p1.x)/2, 2*forehead_y));
      
      detectForehead(src, wrinklesList);
      cvResetImageROI(src);
      
      if (!noseHaar) {
	face.nose.p1 = cvPoint(face.mouth.p1.x, face.left_eye.p2.y);
	face.nose.p2 = cvPoint(face.mouth.p2.x, face.mouth.p1.y);
	checkNoseSize();
      }
      
      cvSetImageROI(src, cvRect(face.p1.x + face.nose.p1.x, 
				face.p1.y + face.nose.p1.y,
				face.nose.p2.x - face.nose.p1.x,
				face.nose.p2.y - face.nose.p1.y));

      detectNose(src, face.nose);
      cvResetImageROI(src);

      cvSetImageROI(src, cvRect(face.lip.p1.x, face.lip.p1.y, 
				face.lip.p2.x - face.lip.p1.x,
				face.lip.p2.y - face.lip.p1.y));

      checkLipsSize();

      detectTeeth(src);
      cvResetImageROI(src);
          
      drawFeatures(dst, wrinklesList, lips);
      
      facesList.push_back(*currfacptr);
      delete currfacptr;
    }
}

double Facet::calcFaceDeclination()
{
  CvPoint p1 = cvPoint(cvRound((face.left_iris.p2.x - face.left_iris.p1.x)*0.5)
		       + face.left_iris.p1.x, 
		       cvRound((face.left_iris.p2.y - face.left_iris.p1.y)*0.5)
		       + face.left_iris.p1.y);
  
  CvPoint p2 = cvPoint(cvRound((face.right_iris.p2.x-face.right_iris.p1.x)*0.5)
		       + face.right_iris.p1.x, 
		       cvRound((face.right_iris.p2.y-face.right_iris.p1.y)*0.5)
		       + face.right_iris.p1.y); 
  
  double A, B, C;

  drawStrLine(p1, p2, A, B, C);
  
  double param = rad2deg(calcAngleOX(-A, B));
	
  currfacptr->roix = face.p1.x;
  currfacptr->roiy = face.p1.y;
  currfacptr->angle = param;
  return param;
}


void Facet::checkSize(CvPoint &p1, CvPoint &p2)
{
  if (p1.x > p2.x) {
    int tmp = 0;
    tmp = p1.x;
    p1.x = p2.x;
    p2.x = tmp;
  }
  else if (p1.x == p2.x)
    p2.x += 1;
  
  if (p1.y > p2.y) {
    int tmp = 0;
    tmp = p1.y;
    p1.y = p2.y;
    p2.y = tmp;
  }
  else if (p1.y == p2.y)
    p2.y += 1;
}


void Facet::checkEyesSize()
{
  checkSize(face.left_eye.p1, face.left_eye.p2);
  checkSize(face.right_eye.p1, face.right_eye.p2);
}

void Facet::checkEyeballsSize()
{
  checkSize(face.left_iris.p1, face.left_iris.p2);
  checkSize(face.right_iris.p1, face.right_iris.p2);
}

void Facet::checkMouthSize()
{
  checkSize(face.mouth.p1, face.mouth.p2);
}

void Facet::checkLipsSize()
{
  checkSize(face.lip.p1, face.lip.p2);
}

void Facet::checkNoseSize()
{
  checkSize(face.nose.p1, face.nose.p2);
}

void Facet::checkForeheadSize()
{
  checkSize(face.forehead.p1, face.forehead.p2);
}

int Facet::calcRightEyebrowHeight()
{
  return (face.right_iris.p1.y+(face.right_iris.p2.y-face.right_iris.p1.y)/2 -
	  face.right_brow.p2.y)*100/(face.right_eye.p2.y-face.right_eye.p1.y);
}     

int Facet::calcLeftEyebrowHeight()
{
  return (face.left_iris.p1.y + (face.left_iris.p2.y - face.left_iris.p1.y)/2 -
	  face.left_brow.p2.y) * 100/(face.left_eye.p2.y-face.left_eye.p1.y);
}

void Facet::detectLeftEyeballHaar(IplImage *src){

  detectObjectHaar(src, cascade_leftEye, face.left_iris,.6,1.2,5);
  face.left_iris.p1.x += face.left_eye.p1.x;
  face.left_iris.p1.y += face.left_eye.p1.y;
  face.left_iris.p2.x += face.left_eye.p1.x;
  face.left_iris.p2.y += face.left_eye.p1.y;
}

void Facet::detectRightEyeballHaar(IplImage *src){
  detectObjectHaar(src, cascade_rightEye, face.right_iris,.6,1.2,5);
  face.right_iris.p1.x += face.right_eye.p1.x;
  face.right_iris.p1.y += face.right_eye.p1.y;
  face.right_iris.p2.x += face.right_eye.p1.x;
  face.right_iris.p2.y += face.right_eye.p1.y; 
}

void Facet::detectNoseHaar(IplImage *src)
{      
  detectObjectHaar(src, cascade_nose, face.nose, .9, 1.1, 2);
      
  face.nose.p1.y += cvRound((face.p2.y-face.p1.y)/3);
  face.nose.p2.y += cvRound((face.p2.y-face.p1.y)/3);
}

void Facet::detectMouthHaar(IplImage *src)
{ 
  detectObjectHaar(src, cascade_mouth, face.mouth, 1.2, 1.1, 5);
	
  face.mouth.p1.y += cvRound(2*(face.p2.y-face.p1.y)/3);
  face.mouth.p2.y += cvRound(2*(face.p2.y-face.p1.y)/3);
}


CvRect Facet::findMouthRectangle(CvSeq *contour)
{
  CvRect b_rect = cvRect(0,0,1,1);
  CvRect b_rect_small = cvRect(0,0,1,1);
  CvRect b_rect_max = cvRect(0,0,1,1);
  
  double outer_area = 0;
  double small_area = 0;
  double max_area = 0;
  
  for( ; contour != 0; contour = contour->h_next ){
    CvSeqReader reader;
    cvStartReadSeq( contour, &reader, 0 );
    
    outer_area =  fabs(cvContourArea(contour, CV_WHOLE_SEQ ));
    
    if (outer_area > max_area){
      if (small_area > outer_area/6){
	b_rect_small = b_rect;
	small_area = max_area;
      }
      max_area = outer_area;
      b_rect = cvContourBoundingRect(contour, 0);		  
    }
    else if ( (max_area>0) & (max_area/6 < outer_area) & 
	      (outer_area < max_area) ) {
      b_rect_small = cvContourBoundingRect(contour, 0);
      small_area = outer_area;
    }
  }
  if ( (b_rect_small.x > 0) && (b_rect_small.y > 0) ) {
    b_rect_max = cvMaxRect( &b_rect, &b_rect_small );
    return b_rect_max;
  }
  else return b_rect;
}


void Facet::drawFeatures(IplImage *dst, list<CvPoint> listOfLines, 
			    CvSeq *lips)
{
  CvPoint pt1 = face.p1;
  CvPoint pt2 = face.p2;
  
  int pt_size = 2; // size of points
  
  CvScalar inner_rect_color = CV_RGB(255,0,0);
  CvScalar outer_rect_color = CV_RGB(255,255,0);
  CvScalar second_lip_color = CV_RGB(255,0,255);
  CvScalar b_rect_max_color = CV_RGB(255,255,0);
  
  // face region rectangle
  cvRectangle(dst, pt1, pt2, CV_RGB(255,0,0),pt_size+1,8);
  
  // wrinkles
  for (list<CvPoint>::iterator iter=listOfLines.begin(); 
       iter != listOfLines.end(); ++iter){
    CvPoint p1 = *iter;
    CvPoint p2 = *(++iter);
    p1.x += face.p1.x + face.forehead.p1.x;
    p1.y += face.p1.y + face.forehead.p1.y;
    p2.x += face.p1.x + face.forehead.p1.x;
    p2.y += face.p1.y + face.forehead.p1.y;

    cvLine(dst,p1,p2,CV_RGB(255,0,0),1,8);
  }
  
  for( ; lips != 0; lips = lips->h_next ){
    CvPoint pt_old = cvPoint(0,0);
    CvPoint pt = cvPoint(0,0);
    CvSeqReader reader;
    cvStartReadSeq( lips, &reader, 0 );
    
    for( int i = 0; i < lips->total; i++ ){
      CV_READ_SEQ_ELEM( pt, reader );
      
      CvPoint p1 = cvPoint(pt1.x + face.mouth.p1.x + pt.x,
			   pt1.y + face.mouth.p1.y + pt.y);
      CvPoint p2 = cvPoint(pt1.x + face.mouth.p1.x + pt_old.x,
			   pt1.y + face.mouth.p1.y + pt_old.y);
      
      if (i>0) cvLine(dst, p1, p2, CV_RGB(0,255,0), pt_size-1, 8);
      cvLine(dst, p1, p1, CV_RGB(0,0,255), pt_size-1, 8);
      
      pt_old = pt;
    }
  }
  
  // eyelids boundary drawing
  cvLine(dst, 
	 cvPoint(pt1.x+face.left_lid.p1.x, pt1.y+face.left_lid.p1.y),
	 cvPoint(pt1.x+face.left_lid.p2.x, pt1.y+face.left_lid.p2.y),
	 CV_RGB(255,0,0), pt_size, 4);
  
  cvLine(dst, 
	 cvPoint(pt1.x+face.left_lid.p3.x, pt1.y+face.left_lid.p3.y),
	 cvPoint(pt1.x+face.left_lid.p4.x, pt1.y+face.left_lid.p4.y),
	 CV_RGB(255,0,0), pt_size, 4);
  
  cvLine(dst, 
	 cvPoint(pt1.x+face.right_lid.p1.x, pt1.y+face.right_lid.p1.y),
	 cvPoint(pt1.x+face.right_lid.p2.x, pt1.y+face.right_lid.p2.y),
	 CV_RGB(255,0,0), pt_size, 4);

  cvLine(dst, 
	 cvPoint(pt1.x+face.right_lid.p3.x, pt1.y+face.right_lid.p3.y),
	 cvPoint(pt1.x+face.right_lid.p4.x, pt1.y+face.right_lid.p4.y),
	 CV_RGB(255,0,0), pt_size, 4);

  // nostrils points
  cvLine(dst,cvPoint(face.nose.p3.x+pt1.x, face.nose.p3.y+pt1.y),
	 cvPoint(face.nose.p3.x+pt1.x, face.nose.p3.y+pt1.y),
	 CV_RGB(0,0,255),pt_size+1,8);
  
  cvLine(dst,cvPoint(face.nose.p4.x+pt1.x, face.nose.p4.y+pt1.y),
	 cvPoint(face.nose.p4.x+pt1.x, face.nose.p4.y+pt1.y),
	 CV_RGB(0,0,255),pt_size+1,8);
  
  cvLine(dst,cvPoint(face.nose.p3.x+pt1.x, face.nose.p3.y+pt1.y),
	 cvPoint(face.nose.p4.x+pt1.x, face.nose.p4.y+pt1.y),
	 CV_RGB(0,255,0),pt_size,8);
  
  // mouth region rectangle
  cvRectangle(dst,cvPoint(face.lip.p1.x, face.lip.p1.y),
	      cvPoint(face.lip.p2.x, face.lip.p2.y), 
	      outer_rect_color,pt_size-1,8);	  
  
  // lips corners
  cvLine(dst, face.lip.p3, face.lip.p3, CV_RGB(255,0,0), pt_size+5, 8);
  cvLine(dst, face.lip.p4, face.lip.p4, CV_RGB(255,0,0), pt_size+5, 8); 
  
  // eyes rectangles
  cvRectangle(dst,
	      cvPoint(face.left_eye.p1.x+pt1.x,
		      face.left_eye.p1.y+pt1.y),
	      cvPoint(face.left_eye.p2.x+pt1.x, 
		      face.left_eye.p2.y+pt1.y), 
	      CV_RGB(0,255,0),pt_size,8);
  
  cvRectangle(dst,
	      cvPoint(face.right_eye.p1.x+pt1.x,
		      face.right_eye.p1.y+pt1.y),
	      cvPoint(face.right_eye.p2.x+pt1.x, 
		      face.right_eye.p2.y+pt1.y), 
	      CV_RGB(0,255,0),pt_size,8);
  
  // mouth rectangle
  cvRectangle(dst,cvPoint(face.mouth.p1.x+pt1.x,face.mouth.p1.y+pt1.y),
	      cvPoint(face.mouth.p2.x+pt1.x,face.mouth.p2.y+pt1.y),
	      CV_RGB(0,0,255),pt_size,8);
  
  
  // nose rectangle
  cvRectangle(dst,
	      cvPoint(face.nose.p1.x+pt1.x, face.nose.p1.y+pt1.y),
	      cvPoint(face.nose.p2.x+pt1.x, face.nose.p2.y+pt1.y), 
	      CV_RGB(255,0,0),pt_size,8);
  
  // forehead rectangle
  cvRectangle(dst,
	      cvPoint(face.forehead.p1.x+pt1.x, 
		      face.forehead.p1.y+pt1.y),
	      cvPoint(face.forehead.p2.x+pt1.x, 
		      face.forehead.p2.y+pt1.y), 
	      CV_RGB(255,255,0),pt_size,8);
  
  // left eyebrow lines
  
  cvLine(dst,
	 cvPoint(pt1.x+face.left_brow.p1.x,
		 pt1.y+face.left_brow.p1.y),
	 cvPoint(pt1.x+face.left_brow.p2.x,
		 pt1.y+face.left_brow.p2.y),
	 CV_RGB(0,200,255),pt_size+1,8);
  
  cvLine(dst,
	 cvPoint(pt1.x+face.left_brow.p2.x,
		 pt1.y+face.left_brow.p2.y),
	 cvPoint(pt1.x+face.left_brow.p3.x,
		 pt1.y+face.left_brow.p3.y),
	 CV_RGB(0,200,255),pt_size+1,8);
  
  cvLine(dst,
	 cvPoint(pt1.x+face.left_brow.p3.x,
		 pt1.y+face.left_brow.p3.y),
	 cvPoint(pt1.x+face.left_brow.p3.x,
		 pt1.y+face.left_brow.p3.y),
	 CV_RGB(255,255,0),pt_size+1,8);
  cvLine(dst,
	 cvPoint(pt1.x+face.left_brow.p2.x,
		 pt1.y+face.left_brow.p2.y),
	 cvPoint(pt1.x+face.left_brow.p2.x,
		 pt1.y+face.left_brow.p2.y),
	 CV_RGB(255,255,0),pt_size+1,8);
  
  cvLine(dst,
	 cvPoint(pt1.x+face.left_brow.p1.x,
		 pt1.y+face.left_brow.p1.y),
	 cvPoint(pt1.x+face.left_brow.p1.x,
		 pt1.y+face.left_brow.p1.y),
	 CV_RGB(255,255,0),pt_size+1,8);
  
  // right eyebrow lines
  
  cvLine(dst,
	 cvPoint(pt1.x+face.right_brow.p1.x,
		 pt1.y+face.right_brow.p1.y),
	 cvPoint(pt1.x+face.right_brow.p2.x,
		 pt1.y+face.right_brow.p2.y),
	 CV_RGB(0,200,255),pt_size+1,8);
  
  cvLine(dst,
	 cvPoint(pt1.x+face.right_brow.p2.x,
		 pt1.y+face.right_brow.p2.y),
	 cvPoint(pt1.x+face.right_brow.p3.x,
		 pt1.y+face.right_brow.p3.y),
	 CV_RGB(0,200,255),pt_size+1,8);
  
  cvLine(dst,
	 cvPoint(pt1.x+face.right_brow.p3.x,
		 pt1.y+face.right_brow.p3.y),
	 cvPoint(pt1.x+face.right_brow.p3.x,
		 pt1.y+face.right_brow.p3.y),
	 CV_RGB(255,255,0),pt_size+1,8);
  cvLine(dst,
	 cvPoint(pt1.x+face.right_brow.p2.x,
		 pt1.y+face.right_brow.p2.y),
	 cvPoint(pt1.x+face.right_brow.p2.x,
		 pt1.y+face.right_brow.p2.y),
	 CV_RGB(255,255,0),pt_size+1,8);
  
  cvLine(dst,
	 cvPoint(pt1.x+face.right_brow.p1.x,
		 pt1.y+face.right_brow.p1.y),
	 cvPoint(pt1.x+face.right_brow.p1.x,
		 pt1.y+face.right_brow.p1.y),
	 CV_RGB(255,255,0),pt_size+1,8);
	  
  // eye pupil rectangles
  cvRectangle(dst, 
	      cvPoint(pt1.x+face.left_iris.p1.x,
		      pt1.y+face.left_iris.p1.y),
	      cvPoint(pt1.x+face.left_iris.p2.x,
		      pt1.y+face.left_iris.p2.y),
	      CV_RGB(255,255,0),pt_size-1,8);
  cvRectangle(dst, 
	      cvPoint(pt1.x+face.right_iris.p1.x,
		      pt1.y+face.right_iris.p1.y),
	      cvPoint(pt1.x+face.right_iris.p2.x,
		      pt1.y+face.right_iris.p2.y),
	      CV_RGB(255,255,0),pt_size-1,8);
  
}


/*! Function for nose detection
 *
 *
 * - maybe gradient projection will work fine?
 */
void Facet::detectNose(IplImage *src, FaceComponent &nose)
{ 
  IplImage *nose_img = cvCreateImage(cvGetSize(src), 8, src->nChannels);
  cvCopy(src, nose_img);

  IplImage *gray = cvCreateImage(cvGetSize(nose_img),8,1);
  IplImage *grad = cvCreateImage(cvGetSize(nose_img),8,1);
  IplImage *tmp = cvCreateImage(cvGetSize(nose_img),8,1);

  for (int i = 0; i < 1; i++) cvSmooth(nose_img, nose_img, CV_MEDIAN, 3, 3);
  cvCvtColor(nose_img, gray,CV_RGB2GRAY);

  IplConvKernel* element = 
    cvCreateStructuringElementEx(3, 3, 1, 1, CV_SHAPE_ELLIPSE);
  
  cvMorphologyEx(gray, grad, tmp, element, CV_MOP_GRADIENT, 1);

  cvReleaseStructuringElement( &element );

  stretchHistogram(grad, grad);
  
  int* ver_val = new int[grad->width];	//CHANGE GC
  int* hor_val = new int[grad->height];
  // int ver_val[grad->width];   
  // int hor_val[grad->height];
  
  //CvMat *mat_grad = cvCreateMat(grad->height, grad->width, CV_8UC1);
  CvMat mat_grad;
  
  cvGetMat(grad, &mat_grad);
  
  counting(&mat_grad,ver_val,PROJ_VERTICAL,CNT_MODE_NORMAL,0,gray->height); 
  counting(&mat_grad,hor_val,PROJ_HORIZONTAL,CNT_MODE_NORMAL,0,gray->width); 
  
  int sum = 0;
  for (int i=0; i<gray->width; i++){
    sum += ver_val[i];
  }
  int average = cvRound(sum/(gray->width));
  average = cvRound(average*1.2);
  
  bool startup = true;
  
  int max_h = 0;
  int hor_pos =0 , ver_pos_l = 0, ver_pos_r = 0;
  
  int range = 0;
  
  for (int i=0; i<gray->width; i++){
    if (startup & (range >= 2)){
      startup = false;
      ver_pos_l = i - range;
    }
    else if (!startup && (range >= 2)) {
      ver_pos_r = i;
    }
    if (ver_val[i]>average){
      ++range;
    }
    else range = 0;
  }
  
  for (int i=0; i<gray->height; i++){
    if (hor_val[i]>max_h){
      max_h=hor_val[i];
      hor_pos=i;
    }
  }
  
  nose.p3 = cvPoint(face.nose.p1.x + ver_pos_l, face.nose.p1.y + hor_pos);
  nose.p4 = cvPoint(face.nose.p1.x + ver_pos_r, face.nose.p1.y + hor_pos);
  
  float roz_noz = (ver_pos_r-ver_pos_l)*100.0/face.width;

  currfacptr->Nstrls = roz_noz;
  cvReleaseImage( &nose_img );
  cvReleaseImage( &gray );
  cvReleaseImage( &grad );
  cvReleaseImage( &tmp );
  //  cvReleaseMat( &mat_grad );

  delete [] ver_val;	//CHANGE delete memory GC
  delete [] hor_val;

}


/*! Function for the forehead detection with wrinkles in its central part
 *
 * it's better to resize the forehead to learn the wrinkles size
 *
 * check how the wrinkles appear in different colour spaces
 *
 * test the adaptive threshold
 *
 * Multiple sobel - acceptable results up to the 2nd derivative
 * Adaptive threshold - poor results
 *
 */

void Facet::detectForehead(IplImage *src, list<CvPoint> &linesList)
{
  IplImage *gray = cvCreateImage(cvGetSize(src),8,1);
  IplImage *sobel = cvCreateImage(cvGetSize(src),8,1);
  
  cvCvtColor(src, gray, CV_RGB2GRAY);
  for (int i = 0; i < 5; i++)  cvSmooth(gray, gray,CV_MEDIAN,3,3);

  cvSobel(gray, sobel,0,1,3);
  cvSobel(sobel, sobel,0,1,3);

  int wrinkles_counter = doHoughTransform(sobel, linesList);

  currfacptr->Wrnkls = wrinkles_counter;

  cvReleaseImage( &gray );
  cvReleaseImage( &sobel );
}


/*! Function for teeth detection in the mouth area
 *
 */
void Facet::detectTeeth(IplImage *src)
{ 
  IplImage *scale = cvCreateImage(cvSize(150,100), 8, 3);
  IplImage *gray = cvCreateImage(cvGetSize(scale), 8, 1);
  IplImage *binary = cvCreateImage(cvGetSize(scale), 8, 1);
  cvResize(src,scale);
  cvCvtColor(scale, gray, CV_RGB2GRAY);
  
  for (int i = 0; i < 1; i++) cvSmooth(gray,gray,CV_MEDIAN ,3,1,1);  
  cvNot(gray,gray);
  
  int average = cvRound(cvAvg(gray).val[0]);
    
  hysteresisThresholding(gray, binary, cvRound(0.9*average), 
		     cvRound(average*1.4));
  
  bitset<4> flag = 15;
  removeBoundaryBlobs(binary, gray, flag);

  int white_cnt = cvCountNonZero(gray);
  int total_cnt = binary->width * binary->height;

  double param = white_cnt*100/total_cnt;
  currfacptr->TeethA = param;

  cvReleaseImage( &scale );
  cvReleaseImage( &gray );
  cvReleaseImage( &binary );
}


/*! Function for mouth transforms (lips detection)
 *
 *  An example of HSV transform saved in file: ->
 *                                      backup/program_mouth_hsv.cpp
 */
void Facet::detectLips(IplImage *src, FaceComponent &lip, 
			    CvSeq *&lips_outline)
{  
  IplImage *mouth = cvCreateImage(cvGetSize(src), src->depth, src->nChannels);
  cvCopy(src, mouth);

  IplImage *L  = cvCreateImage(cvGetSize(mouth), 8, 1);
  IplImage *r  = cvCreateImage(cvGetSize(mouth), 8, 1);
  IplImage *g  = cvCreateImage(cvGetSize(mouth), 8, 1);
  IplImage *b  = cvCreateImage(cvGetSize(mouth), 8, 1);
  IplImage *luv  = cvCreateImage(cvGetSize(mouth), 8, 3);
  IplImage *plane  = cvCreateImage(cvGetSize(mouth), 8, 1);

  for (int i = 0; i < 10; i++)  cvSmooth(mouth, mouth,CV_BLUR,3,3);

  cvCvtColor(mouth, luv, CV_BGR2Luv);
  cvSplit(luv, L, plane, plane, 0 );
  cvSplit(mouth, r, g, b, 0 );

  CvMat mat_R; // = cvCreateMat(mouth->height, mouth->width,CV_8UC1);
  CvMat mat_G; // = cvCreateMat(mouth->height, mouth->width,CV_8UC1);
  CvMat mat_B; //= cvCreateMat(mouth->height, mouth->width,CV_8UC1);

  cvGetMat(r,&mat_R,0);
  cvGetMat(g,&mat_G,0);
  cvGetMat(b,&mat_B,0);

  double max_h=0.0, min_h=255.0;

  double** H_values = new double*[mouth->width]; // GC
  for(int j=0; j< mouth->width; j++) {
    double* vals = new double[mouth->height];
    H_values[j] = vals;
  }
  // double H_values [mouth->width][mouth->height];
  
  for(int i=0; i< mouth->height; i++) {
    for(int j=0; j< mouth->width; j++) { 
      
      double R = cvGetAt(&mat_R,i,j).val[0];
      double G = cvGetAt(&mat_G,i,j).val[0];
      if (G==0) G+=0.0000000001;
      double H = R / ( R + G );
      H_values[j][i] = H;   
      if (H>max_h) max_h=H;
      if (H<min_h) min_h=H;
    }
  }

  IplImage *Rcor  = cvCreateImage(cvGetSize(mouth), 8, 1);
  IplImage *Gcor  = cvCreateImage(cvGetSize(mouth), 8, 1);
  IplImage *Bcor  = cvCreateImage(cvGetSize(mouth), 8, 1);
  IplImage *result = cvCreateImage(cvGetSize(mouth), 8, 1);
  IplImage *result_c = cvCreateImage(cvGetSize(mouth), 8, 1);

  CvMat mat_Rcor;// = cvCreateMat(result->height, result->width,CV_8UC1);
  CvMat mat_Gcor;// = cvCreateMat(result->height, result->width,CV_8UC1);
  CvMat mat_Bcor;// = cvCreateMat(result->height, result->width,CV_8UC1);
  CvMat mat_L;// = cvCreateMat(result->height, result->width,CV_8UC1);
  CvMat mat_Res;// = cvCreateMat(result->height, result->width,CV_8UC1);
  
  cvGetMat(Rcor,&mat_Rcor,0);
  cvGetMat(Gcor,&mat_Gcor,0);
  cvGetMat(Bcor,&mat_Bcor,0);
  cvGetMat(L,&mat_L,0);
  cvGetMat(result,&mat_Res,0);

  for(int i=0; i< result->height; i++) {
    for(int j=0; j< result->width; j++) { 
      
      double R = cvGetAt(&mat_R,i,j).val[0];
      double Lxy = cvGetAt(&mat_L,i,j).val[0];
      double r_cor = R / ( R + 0.4 * Lxy + 0.4 );
      cvSetAt(&mat_Rcor,cvScalar(cvRound(r_cor*255)),i,j);  
      
      double G = cvGetAt(&mat_G,i,j).val[0];
      double g_cor = G / ( G + 0.4 * Lxy + 0.4 );
      cvSetAt(&mat_Gcor,cvScalar(cvRound(g_cor*255)),i,j);        

      double B = cvGetAt(&mat_B,i,j).val[0];
      double b_cor = B / ( B + 0.4 * Lxy + 0.4 );
      cvSetAt(&mat_Bcor,cvScalar(cvRound(b_cor*255)),i,j);  

    }
  }
  
  cvGetImage(&mat_Res,result); 
  //stretchHistogram(result, result); cvNot(result,result);
  
  cvSub(Bcor, Gcor, result);  
  stretchHistogram(result, result);  
  cvNot(result,result);	    

  int mouth_thresh = findThresholdByHist( result, lips_proportions );
  hysteresisThresholding(result,result_c,cvRound(mouth_thresh*.8),
		     cvRound(mouth_thresh*1.2));
  cvCopy(result_c, result);
  
  bitset<4> flag = 14;
 
  if ((result->width>5) && (result->height>5))
    removeBoundaryBlobs(result,result_c,flag);
  else cvCopy(result,result_c);
  
  
  int iter = 1;
  while (  findConnectedRegions(result_c) > 2 ){
    removeSmallBlobs(result_c, result, iter++);
    cvCopy(result,result_c);
  }
  
  IplConvKernel* element = 
    cvCreateStructuringElementEx(3,3,1,1,CV_SHAPE_ELLIPSE);
  cvMorphologyEx(result, result, result_c, element, CV_MOP_CLOSE, 1);
  
  cvReleaseStructuringElement( &element );

  createOutline(result,result_c);
  findContours(result_c, lips_outline);
  
  parameteriseLips(result_c, lip, lips_outline);
  
  int H_width = mouth->width; //CHANGE2 GC
  
  cvReleaseImage( &mouth );
  cvReleaseImage( &L );
  cvReleaseImage( &r );
  cvReleaseImage( &g );
  cvReleaseImage( &b );
  cvReleaseImage( &luv );
  cvReleaseImage( &plane );
  cvReleaseImage( &Rcor );
  cvReleaseImage( &Gcor );
  cvReleaseImage( &Bcor );
  cvReleaseImage( &result );
  cvReleaseImage( &result_c );
  /*
  cvReleaseMat( &mat_R );
  cvReleaseMat( &mat_G );
  cvReleaseMat( &mat_B );
  cvReleaseMat( &mat_Rcor );
  cvReleaseMat( &mat_Gcor );
  cvReleaseMat( &mat_Bcor );
  cvReleaseMat( &mat_L );
  cvReleaseMat( &mat_Res );
  */

  for(int j=0; j< H_width; j++) { //CHANGE2 GC
    delete [] H_values[j];
  }
  delete [] H_values;
}


void Facet::parameteriseLips(IplImage *result_c, FaceComponent &lip,
				CvSeq *lips_outline){
  // aspect
  CvRect lips_rect = findMouthRectangle(lips_outline);
  double aspect_ratio;
  (lips_rect.height != 0) ? 
    aspect_ratio = lips_rect.width*100/lips_rect.height :
    aspect_ratio=-1;
  
  currfacptr->LiAspt = aspect_ratio;
  lip.p1 = cvPoint(lips_rect.x, lips_rect.y);
  lip.p2 = cvPoint(lips_rect.x+lips_rect.width, lips_rect.y+lips_rect.height);
  
  // corners location
  
  CvMat mat;// = cvCreateMat(result_c->height, result_c->width, CV_8UC1);
  cvGetMat(result_c, &mat);
  
  int maxi=-1, mini=-1;
  for (int i=0; i< result_c->width; i++){
    for (int j=0; j< result_c->height; j++){
      double akt=cvGetAt(&mat, j, i).val[0];
      
      if ((i>=lip.p1.x) && (i<=lip.p2.x) && (j>=lip.p1.y) && (j<=lip.p2.y))
	{
	  if ((akt>100)&&(mini<0)){
	    mini=j;
	  }
	  else if ((akt>100)&&(mini>=0))
	    maxi=j;
	}
    }
  }
  lip.p3 = cvPoint(lip.p1.x, mini); // left corner
  lip.p4 = cvPoint(lip.p2.x, maxi); // right corner

  double l_corner, r_corner;
  l_corner = ((lips_rect.y+lips_rect.height) - lip.p3.y)*100/lips_rect.height;
  r_corner = ((lips_rect.y+lips_rect.height) - lip.p4.y)*100/lips_rect.height;
  
  currfacptr->LLiCnr = l_corner;
  currfacptr->RLiCnr = r_corner;

  // mouth rectangle and lips corners in the face region coordinates
  lip += (FaceComponent(face.p1,face.p2) + face.mouth) ;
  //  cvReleaseMat( &mat );
}


// An attempt to detect the face by the skin hue
// -> based on : jap.pdf
// The same filter for lips detection
//
void Facet::faceDetectHue(IplImage *src, bool lips, bool eyes)
{
  IplImage *dst = cvCreateImage(cvGetSize(src), 8, 3);
  cvCopy(src, dst);

  IplImage *R = cvCreateImage(cvGetSize(dst), 8, 1);
  IplImage *G = cvCreateImage(cvGetSize(dst), 8, 1);
  IplImage *B = cvCreateImage(cvGetSize(dst), 8, 1);
  IplImage *r = cvCreateImage(cvGetSize(dst), 8, 1);
  IplImage *g = cvCreateImage(cvGetSize(dst), 8, 1);
  IplImage *lip = cvCreateImage(cvGetSize(dst), 8, 3);
  
  cvCvtColor(dst,dst,CV_RGB2BGR);
  cvSmooth(dst,dst,CV_MEDIAN ,3,1,1);

  // transformation into (r,g) colour space
  cvSplit(dst,R,G,B,0);
  cvZero(r);
  cvZero(g);
  cvZero(dst);
  if (lips) cvZero(lip);

  CvMat mat_R;// = cvCreateMat(dst->height, dst->width, CV_8UC1);
  CvMat mat_G;// = cvCreateMat(dst->height, dst->width, CV_8UC1);
  CvMat mat_B;// = cvCreateMat(dst->height, dst->width, CV_8UC1);
  CvMat mat_r;// = cvCreateMat(dst->height, dst->width, CV_8UC1);
  CvMat mat_g;// = cvCreateMat(dst->height, dst->width, CV_8UC1);
  CvMat mat_dst;// = cvCreateMat(dst->height, dst->width, CV_8UC3);
  CvMat mat_src;// = cvCreateMat(dst->height, dst->width, CV_8UC3);
  CvMat mat_lip;// = cvCreateMat(dst->height, dst->width, CV_8UC3);
  cvGetMat(R,&mat_R,0);
  cvGetMat(G,&mat_G,0);
  cvGetMat(B,&mat_B,0);
  cvGetMat(r,&mat_r,0);
  cvGetMat(g,&mat_g,0);
  cvGetMat(dst,&mat_dst,0);
  cvGetMat(src,&mat_src,0);
  if (lips)  cvGetMat(&lip,&mat_lip,0);

  for (int i=0; i < (&mat_R)->rows; i++)
    for (int j=0; j < (&mat_R)->cols; j++) 
      {
	double red = cvGetAt(&mat_R,i,j).val[0];
	double green = cvGetAt(&mat_G,i,j).val[0];
	double blue = cvGetAt(&mat_B,i,j).val[0];
	double rr = red/(red+green+blue);
	double gg = green/(red+green+blue);
	
	// skin detection
	double f_upper = -1.3767*rr*rr + 1.0743*rr + 0.1452;
	double f_lower = -0.7760*rr*rr + 0.5601*rr + 0.1766;
	double W = (rr - 0.33)*(rr - 0.33) + (gg - 0.33)*(gg - 0.33);
	
	if ( ((gg < f_upper) && (gg > f_lower)) && (W > 0.0004) &&
	     (red > blue) && (red > green) && 
	     ((red-green) >= 25))
	  //cvSetAt(mat_dst, cvScalar(blue, green, red), i, j);
	  cvSetAt(&mat_dst, cvScalar( 255, 255, 255 ), i, j);
	else
	  cvSetAt(&mat_dst, cvScalar ( 0,0,0 ), i, j);	  
	
	if (lips) {
	  double Lr = -0.776*rr*rr + 0.5661*rr + 0.165;

	  if ((gg < Lr) && (red >= 20) && (green >= 20) && (blue >= 20))
	    cvSetAt(&mat_lip, cvGetAt(&mat_src,i,j), i, j);
	}
	
      }

  cvGetImage(&mat_dst, dst);  
  if (lips) cvGetImage(&mat_lip, lip);
  
  IplImage *dst_copy = cvCreateImage(cvGetSize(dst),8,1);
  cvCvtColor(dst,dst_copy, CV_RGB2GRAY);
  
  IplImage *src_copy = cvCreateImage(cvGetSize(src),8,3);
  cvCopy(src,src_copy);
  
  CvSeq *contour = 0;
  CvPoint pkt = cvPoint(0,0);
  
  findContours(dst_copy, contour);
  
  if (contour != 0) {
    
    for( ; contour != 0; contour = contour->h_next ){
      
      double area =  fabs(cvContourArea(contour, CV_WHOLE_SEQ ));
      if (area >= (src->width*src->height)/100) {
	
	CvRect b_rect = cvContourBoundingRect(contour, 0);
	
	cvRectangle(src_copy, cvPoint(b_rect.x, b_rect.y), 
		    cvPoint(b_rect.x+b_rect.width,b_rect.y+b_rect.height),
		    CV_RGB(0,255,0),8,8);
      }
    }
  } 
  
  if (eyes) {
    IplImage *eye = cvCreateImage(cvGetSize(src),8,1);
    IplImage *eye2 = cvCreateImage(cvGetSize(src),8,1);
    cvCvtColor(src_copy,eye,CV_RGB2GRAY);
    cvZero(eye2);
    
    int thr = 40;
    hysteresisThresholding(eye, eye2, thr, cvRound(thr*1.4));
    
    cvReleaseImage( &eye );
    cvReleaseImage( &eye2 );
  }
  
  cvReleaseImage( &dst );
  cvReleaseImage( &dst_copy );
  cvReleaseImage( &src_copy );
  cvReleaseImage( &R );
  cvReleaseImage( &G );
  cvReleaseImage( &B );
  cvReleaseImage( &r );
  cvReleaseImage( &g );
  cvReleaseImage( &lip );
  /*
  cvReleaseMat( &mat_R );
  cvReleaseMat( &mat_G );
  cvReleaseMat( &mat_B );
  cvReleaseMat( &mat_r );
  cvReleaseMat( &mat_g );
  cvReleaseMat( &mat_dst );
  cvReleaseMat( &mat_src );
  cvReleaseMat( &mat_lip );
  */
}


// Eyelids detection
//
double Facet::detectEyelids(IplImage *src, FaceComponent iris, 
			     FaceComponent &lid) {

  IplImage *temp = cvCreateImage(cvGetSize(src), 8, 1);
  IplImage *val  = cvCreateImage(cvGetSize(src), 8, 1);
  IplImage *hsv  = cvCreateImage(cvGetSize(src), 8, 3);
  cvCvtColor(src, hsv, CV_BGR2HSV);
  cvSplit(hsv, temp, temp, val, 0 );
  
  IplImage *eye = cvCreateImage(cvSize(150,100), 8, 1);
  IplImage *eye_c = cvCreateImage(cvGetSize(eye), 8, 3);
  IplImage *eye_thresh = cvCreateImage(cvGetSize(eye), 8, 1);
  IplImage *tmpx = cvCreateImage(cvGetSize(eye), 8, 1);
  
  IplImage *eye_c1 = cvCreateImage(cvGetSize(src), 8, 1);
  cvCopy(val, eye_c1);
  IplImage *eye_c3 = cvCreateImage(cvGetSize(src), 8, 3);
  cvCopy(src, eye_c3);
  
  cvResize(eye_c1, eye);
  cvResize(eye_c3, eye_c);
  
  cvZero(tmpx);
  cvEqualizeHist(eye, eye);
  
  int thresh = findThresholdByHist(eye, 20); // 1/4
  hysteresisThresholding(eye, eye_thresh, cvRound(0.9*thresh),
			 cvRound(thresh*1.2));
  
  bitset<4> flag = 3;
  cvCopy(eye_thresh, tmpx);
  removeBoundaryBlobs( tmpx, eye_thresh, flag);
  
  CvMat mat; // = cvCreateMat(eye->height, eye->width, CV_8UC1);
  cvGetMat(eye_thresh, &mat);
  
  int* ver = new int[eye->width];	//CHANGE GC
  int* hor = new int[eye->height];
  // int ver[eye->width], hor[eye->height];
  
  counting(&mat,ver,PROJ_VERTICAL,CNT_MODE_NORMAL,0,eye->height);
  counting(&mat,hor,PROJ_HORIZONTAL,CNT_MODE_NORMAL,0,eye->width);
  
  int sum=0;
  for (int i=0; i< eye->height; ++i) {
    sum+=hor[i];
  }
  
  int average = cvRound(sum/eye->height/3);
  
  int v_i_p=0, v_i_k=0;
  for (int i=0; i< eye->height; ++i) {
    if ((hor[i]>average)&&(v_i_p==0)) {
      v_i_p = i;
    }
    else if (hor[i]>average) v_i_k = i;
  }
  
  //cvGetImage(&mat, eye_thresh);
  
  double param = (v_i_k - v_i_p)*100/eye->height;

  double scale;
  scale = eye_c1->height*100/eye->height;

  lid.p1 = cvPoint(iris.p1.x - 2, iris.p1.y - 
		   cvRound((iris.p2.y -iris.p1.y)/3)+cvRound(scale*v_i_p/100));

  lid.p2 = cvPoint(iris.p2.x + 2, iris.p1.y - 
		   cvRound((iris.p2.y -iris.p1.y)/3)+cvRound(scale*v_i_p/100));
  
  lid.p3 = cvPoint(iris.p1.x -2, iris.p1.y - 
		   cvRound((iris.p2.y -iris.p1.y)/3)+cvRound(scale*v_i_k/100));

  lid.p4 = cvPoint(iris.p2.x + 2, iris.p1.y - 
		   cvRound((iris.p2.y -iris.p1.y)/3)+cvRound(scale*v_i_k/100));
  
  cvReleaseImage( &temp );
  cvReleaseImage( &val );
  cvReleaseImage( &hsv );
  
  cvReleaseImage( &eye );
  cvReleaseImage( &eye_c );
  cvReleaseImage( &eye_c1 );
  cvReleaseImage( &eye_c3 );
  cvReleaseImage( &eye_thresh );
  cvReleaseImage( &tmpx );
  
  //cvReleaseMat( &mat );
  
  delete [] ver;	//CHANGE delete memory GC
  delete [] hor;

  return param;
}


// Eyebrow transform
// 
// Operates on HSV rather than on Grayscale image
// 
void Facet::detectEyebrow(IplImage *src, FaceComponent eye_region,
			     FaceComponent iris, FaceComponent &brow, 
			     bitset<4> flaga)
{
  IplImage *hsv  = cvCreateImage(cvGetSize(src), src->depth, src->nChannels);
  IplImage *hsv_plane  = cvCreateImage(cvGetSize(src), src->depth, 1);
  IplImage *hsv_value  = cvCreateImage(cvGetSize(src), src->depth, 1);
  IplImage *eyebrows = cvCreateImage(cvGetSize(src), src->depth, 1);
  
  cvCvtColor(src, hsv, CV_BGR2HSV);
  cvSplit(hsv, hsv_plane, hsv_plane, hsv_value, 0 );
  
  for (int i = 0; i < 1; i++) 
    cvSmooth(hsv_value, hsv_value, CV_MEDIAN , 3, 1, 1);
  
  // *** ROI SETTING ***
  
  cvCopy(hsv_value, eyebrows);
  cvSetImageROI(eyebrows, cvRect(0, 0, src->width,
				 ((iris-eye_region).p1.y) == 0 ? 1 :
				 (iris-eye_region).p1.y));
  
  // *** SCALING ***
  
  IplImage *scale = cvCreateImage( cvSize(150,100),8,1 );      // scaling
  IplImage *scale_prim = cvCreateImage(cvGetSize(eyebrows),8,1 ); // primary scale
  
  cvResize(eyebrows, scale);
  //cvSaveImage("scaling.png", scale);
  
  // **** THRESHOLDING ****
  
  int brow_thresh = findThresholdByHist(scale, eyebrow_proportions);
  hysteresisThresholding(scale, scale, cvRound(0.9*brow_thresh),
			 cvRound(brow_thresh*1.4));//60 200
  
  for (int i=0; i<3; i++) cvSmooth(scale, scale, CV_MEDIAN, 3, 3); 
  
  //cvSaveImage("thresholding.png", scale);  
  
  // **** FILTERING ****
  
  bitset<4> flag1 = 12;
  
  removeBoundaryBlobs(scale, scale, flag1);
  
  //cvSaveImage("rm_bound_blobs.png", scale);  
  
  int iter = 1;  
  // remove small blobs if total number of them is greater than 1
  while ( findConnectedRegions(scale) > 1 ) {
    removeSmallBlobs(scale, scale, iter++);
  }
  
  //cvSaveImage("rm_small_blobs.png", scale);  
  
  // **** SILHOUETTE OUTLINE ****

  createOutline(scale, scale);
  
  //cvSaveImage("outline.png", scale);  
  
  // *** SCALING (return to the original size) ***
  
  cvResize(scale, eyebrows);
  //eyebrows = cvCloneImage(scale);
  
  // **** FINDING THE CHARACTERISTIC POINTS ****
  
  CvMat matc1;// = cvCreateMat(eyebrows->height, eyebrows->width,CV_8UC1);
  cvGetMat(eyebrows, &matc1);
  bool end = false;
  int min_h=eyebrows->height;
  
  for (int j=0; j< matc1.width; j++){
    for (int i=0; i< matc1.height; i++){
      int value = (int)cvGetAt(&matc1,i,j).val[0];
      if (value>0) {
	if (!end){
	  brow.p1=cvPoint(eye_region.p1.x+j, 
			  eye_region.p1.y+i);
	  end=true;
	}
	else {
	  brow.p3=cvPoint(eye_region.p1.x+j, 
			  eye_region.p1.y+i);
	  if (min_h>i){
	    min_h = i;
	    brow.p2=cvPoint(eye_region.p1.x+j, 
			    eye_region.p1.y+i);
	  }
	}
      }
    }
  }
  
  parameteriseEyebrow(brow, flaga);
  
  //drawEyebrows(src, eye_region, brow);
  //drawEyebrows(eyebrows, eye_region, brow);
  
  cvResetImageROI(eyebrows);
  
  cvReleaseImage(&eyebrows);
  // cvReleaseImage(&temp);
  cvReleaseImage(&hsv);
  cvReleaseImage(&hsv_plane);
  cvReleaseImage(&hsv_value);
  cvReleaseImage(&scale);
  cvReleaseImage(&scale_prim);
  
  //cvReleaseMat( &matc1 );
}


void Facet::parameteriseEyebrow(FaceComponent brow, bitset<4> flaga) {
  
  // calculate the angle between two lines
  double A1,A2,B1,B2,C1,C2;
  
  drawStrLine(brow.p1, brow.p2, A1, B1, C1);
  drawStrLine(brow.p3, brow.p2, A2, B2, C2);
  
  double param;
  param = abs(cvRound(rad2deg(calcTwoLinesAngle(A1,B1,A2,B2))));
  
  if(flaga[2]) currfacptr->REbBnd = param;
  if(flaga[3]) currfacptr->LEbBnd = param;
  
  double Kat1 = abs(cvRound(rad2deg(calcAngleOX(A1,B1))));
  double Kat2 = 180 - abs(cvRound(rad2deg(calcAngleOX(A2,B2))));
  
  param = (Kat1>Kat2 ? Kat1 : Kat2);
  if(flaga[2]) currfacptr->REbDcl = param;
  if(flaga[3]) currfacptr->LEbDcl = param;

}


void Facet::drawEyebrows(const IplImage *src, FaceComponent eye_region, 
			    FaceComponent brow){
  
  IplImage *display = cvCreateImage(cvGetSize(src), 8, 3);
  
  if (src->nChannels == 3) cvCopy(src, display);
  else cvCvtColor(src, display, CV_GRAY2RGB);
  
  brow = (brow - eye_region);
  
  cvLine(display,cvPoint(brow.p2.x,brow.p2.y), cvPoint(brow.p3.x, brow.p3.y), 
	 CV_RGB(255,255,0),3,8);
  
  cvLine(display,cvPoint(brow.p1.x,brow.p1.y), cvPoint(brow.p2.x,brow.p2.y), 
	 CV_RGB(255,255,0),3,8);
  
  cvLine(display,cvPoint(brow.p1.x,brow.p1.y), cvPoint(brow.p1.x,brow.p1.y), 
	 CV_RGB(0,0,0),8,8);
  
  cvLine(display,cvPoint(brow.p2.x,brow.p2.y), cvPoint(brow.p2.x,brow.p2.y), 
	 CV_RGB(0,0,0),8,8);
  
  cvLine(display,cvPoint(brow.p3.x,brow.p3.y), cvPoint(brow.p3.x,brow.p3.y), 
	 CV_RGB(0,0,0),8,8);
  
  cvLine(display,cvPoint(brow.p1.x,brow.p1.y), cvPoint(brow.p1.x,brow.p1.y), 
	 CV_RGB(255,255,255),6,8);

  cvLine(display,cvPoint(brow.p2.x,brow.p2.y), cvPoint(brow.p2.x,brow.p2.y), 
	 CV_RGB(255,255,255),6,8);
  
  cvLine(display,cvPoint(brow.p3.x,brow.p3.y), cvPoint(brow.p3.x,brow.p3.y), 
	 CV_RGB(255,255,255),6,8);
  
  cvLine(display,cvPoint(brow.p1.x,brow.p1.y), cvPoint(brow.p1.x,brow.p1.y), 
	 CV_RGB(0,0,0),4,8);
  
  cvLine(display,cvPoint(brow.p2.x,brow.p2.y), cvPoint(brow.p2.x,brow.p2.y), 
	 CV_RGB(0,0,0),4,8);
  
  cvLine(display,cvPoint(brow.p3.x,brow.p3.y), cvPoint(brow.p3.x,brow.p3.y), 
	 CV_RGB(0,0,0),4,8);
  
  cvSaveImage("aproksymacja.png", display);  
  cvReleaseImage( &display );
}

void Facet::cleanFacesList()
{
  if(facesList.size()){
    
    int tmp = facesList.size();
    for (int i = 0; i< tmp; ++i){
      facesList.pop_front();
    }
  }
}

