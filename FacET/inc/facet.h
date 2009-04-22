/* $Id: facet.h 10 2009-03-24 21:45:48Z mw $
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

/*! \file facet.h
 *
 * File defines the classes for face features detection, parameterisation 
 * and displaying them in the image.
 * 
 */

#ifndef _FACET_H_
#define _FACET_H_

#include <iomanip>
#include <iostream>
#include <fstream>
#include <bitset>
#include <list>

#include <highgui.h>
#include <cvaux.h>

#include "FastMatchTemplate.h"

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::max;
using std::min;
using std::bitset;
using std::string;
using std::list;
using std::setprecision;
using std::noshowpos;
using std::showpos;


/*! \class FaceParameters
 *
 *  Defines the face parameters (eyebrows and lips proportions and 
 *  face region height. Provides methods for parameters reading and setting.
 */
class FaceParameters 
{
 protected:
  
  /*! \brief Parameter defining eyebrows proportions
   * 
   * Used in procedure for histogram-based finding of the threshold
   * in eyebrows detection
   */
  int eyebrow_proportions;
  
  /*! \brief Parameter defining lips proportions
   * 
   * Used in procedure for histogram-based finding of the threshold
   * in lips detection
   */
  int lips_proportions;
  
 public:
  
  /*! Parameterless constructor 
   *
   *  Sets the default values of the components
   */  
  FaceParameters(): 
    eyebrow_proportions(25), lips_proportions(25) {}
    
    /*! Sets the eyebrows proportions
     *
     *  \param val - eyebrows proportions value
     */
    void setEyebrowsRatio(int val) { eyebrow_proportions = val; }
    
    /*! Sets the lips proportions
     *
     *  \param val - lips proportions value
     */
    void setLipsRatio(int val) { lips_proportions = val; }  
    
    /*! Reads the eyebrow proportions
     *
     *  \return eyebrow proportions value
     */
    int readEyebrowsRatio() const { return eyebrow_proportions; }
    
    /*! Reads the lips proportions
     *
     *  \return lips proportions value
     */
    int readLipsRatio() const { return lips_proportions; }
};


/*! \class HaarParameters
 *
 *  Defines parameters for Haar classifier (image scaling for face detection,
 *  Haar cascades read from corresponding files for face, mouth, nose and eyes).
 *  Provides methods for parameters reading and setting.
 */
class HaarParameters 
{
 protected:
  
  /*! The value of image downscaling ratio for face detection with Haar 
   * classifier. 
   */
  double face_detection_scale;
  
  /*! Memory storage for face detection with Haar classifier.
   */
  static CvMemStorage* face_storage;
  
  /*! Haar cascade for face */
  CvHaarClassifierCascade* cascade_face;
  
  /*! Haar cascade for mouth */
  CvHaarClassifierCascade* cascade_mouth;
  
  /*! Haar cascade for nose */
  CvHaarClassifierCascade* cascade_nose;
  
  /*! Haar cascade for left eye */
  CvHaarClassifierCascade* cascade_leftEye;
  
  /*! Haar cascade for right eye */
  CvHaarClassifierCascade* cascade_rightEye;
  
 public:
  
  /*! Parameterless constructor 
   *
   *  Sets the default values of the components
   */  
  HaarParameters(): face_detection_scale(4) 
    {
      cascade_face = cascade_mouth = cascade_nose = cascade_leftEye = cascade_rightEye = 0;
    }
    
    /*! Sets image scaling for face detection with Haar classifier
     *
     * \param val - new image scaling value
     */
    void setFaceScale(double val) { 
      face_detection_scale = val;
    }
    
    /*! Reads image scaling for face detection with Haar classifier
     *
     * \return image scaling value
     */
    double readFaceScale() const { 
      return face_detection_scale; 
    }
    
    /*! Reads Haar cascade for face */
    void readFaceCascade(std::string face) 
    {
      cascade_face = 
	(CvHaarClassifierCascade*)cvLoad(face.c_str(), 0, 0, 0 );
    }
    
    /*! Reads Haar cascade for mouth */
    void readMouthCascade(std::string mouth) 
    {
      cascade_mouth = 
	(CvHaarClassifierCascade*)cvLoad(mouth.c_str(), 0, 0, 0 );
    }
    
    /*! Reads Haar cascade for nose */
    void readNoseCascade(std::string nose) 
    {
      cascade_nose = 
	(CvHaarClassifierCascade*)cvLoad(nose.c_str(), 0, 0, 0 );
    }
    
    /*! Reads Haar cascade for left eye */
    void readLeftEyeCascade(std::string eye_left) 
    {
      cascade_leftEye = 
	(CvHaarClassifierCascade*)cvLoad(eye_left.c_str(), 0, 0, 0 );
    }
    
    /*! Reads Haar cascade for right eye */
    void readRightEyeCascade(std::string eye_right) 
    {
      cascade_rightEye = 
	(CvHaarClassifierCascade*)cvLoad(eye_right.c_str(), 0, 0, 0 );
    }
};


/*! Max vertical and horizontal size of face image */
#define MAX_SIZE 2048

/*! Selects data averaging type */
typedef enum mean_type {MEAN_NORMAL, MEAN_MAX}; 

/*! Selects projection direction */
typedef enum proj_type {PROJ_VERTICAL, PROJ_HORIZONTAL};

/*! Selects counting mode */
typedef enum count_mode {CNT_MODE_NORMAL, CNT_MODE_BINARY};

/*! Selects option mode in selectRegion() function */
typedef enum select_mode {MODE_CUT, MODE_COPY, MODE_CLEAR};

/*! \class FaceComponent
 *
 * Describes the corners of face subregion rectangle.
 */
class FaceComponent {
public:
  /*! A point of face component */
  CvPoint p1;
  /*! A point of face component */
  CvPoint p2;
  /*! A point of face component */
  CvPoint p3;
  /*! A point of face component */
  CvPoint p4;

  /*! Parameterless constructor 
   * 
   *  Sets the default values of the components
   */
  FaceComponent() {}

  /*! Constructor
   *
   * \param pt1 - first point of face component
   * \param pt2 - second point of face component
   */
  FaceComponent(CvPoint pt1, CvPoint pt2) : 
    p1(pt1), p2(pt2), p3(cvPoint(0,0)) {}

  /*! Constructor
   *
   * \param pt1 - first point of face component
   * \param pt2 - second point of face component
   * \param pt3 - third point of face component
   */
  FaceComponent(CvPoint pt1, CvPoint pt2, CvPoint pt3): 
    p1(pt1),p2(pt2),p3(pt3) {}

  /*! Constructor
   *
   * \param pt1 - first point of face component
   * \param pt2 - second point of face component
   * \param pt3 - third point of face component
   * \param pt4 - fourth point of face component
   */
  FaceComponent(CvPoint pt1, CvPoint pt2, CvPoint pt3, CvPoint pt4): 
    p1(pt1),p2(pt2),p3(pt3),p4(pt4) {}

  /*! Overloading operator of subtraction of two objects of FaceComponent class 
   *  
   * \param el - input component
   *
   * \return difference of input component and the object
   */
  FaceComponent operator-(FaceComponent el){
    return FaceComponent(cvPoint(abs(p1.x - el.p1.x), abs(p1.y - el.p1.y)),
			 cvPoint(abs(p2.x - el.p1.x), abs(p2.y - el.p1.y)),
			 cvPoint(abs(p3.x - el.p1.x), abs(p3.y - el.p1.y)),
			 cvPoint(abs(p4.x - el.p1.x), abs(p4.y - el.p1.y)));
  }

  /*! Overloading operator of addition of two objects of FaceComponent class 
   *
   * \param el - input component
   *
   * \return sum of input component and the object
   */
  FaceComponent operator+(FaceComponent el){
    return FaceComponent(cvPoint(abs(p1.x + el.p1.x), abs(p1.y + el.p1.y)),
			 cvPoint(abs(p2.x + el.p1.x), abs(p2.y + el.p1.y)),
			 cvPoint(abs(p3.x + el.p1.x), abs(p3.y + el.p1.y)),
			 cvPoint(abs(p4.x + el.p1.x), abs(p4.y + el.p1.y)));
  }

  /*! Overloading operator of accumulation of two objects of FaceComponent class  
   *
   * \param el - input component
   */
  void operator+=(FaceComponent el) {
    *this = (*this + el);
  }

  /*! Resets the fields values */
  void clear(){ p1=p2=p3=p4=cvPoint(0,0); }
};


/*! \class FFace
 *
 * Describes the face components. Contains attributes of selected components.
 */
class FFace: public FaceComponent {
public:

  /*! Component describing mouth */
  FaceComponent mouth;
  /*! Component describing left eye */
  FaceComponent left_eye;
  /*! Component describing right eye */
  FaceComponent right_eye;
  /*! Component describing left iris */
  FaceComponent left_iris;
  /*! Component describing right iris */
  FaceComponent right_iris;  
  /*! Component describing left eyelid */
  FaceComponent left_lid;
  /*! Component describing right eyelid */
  FaceComponent right_lid;
  /*! Component describing left eyebrow */
  FaceComponent left_brow;
  /*! Component describing right eyebrow */
  FaceComponent right_brow;
  /*! Component describing lips (p1,p2 - mouth rectangle, p3,p4 - lips corners */
  FaceComponent lip;
  /*! Component describing forehead */
  FaceComponent forehead;
  /*! Component describing nose */
  FaceComponent nose;
  /*! Face width */
  int width;

  /*! Resets the fields values */
  void clearElements();
};


/*! Counts the elements of the input matrix
 *
 * \param matrix - input image matrix
 * \param tab    - data array
 * \param type   - direction of projection counting (horizontal/vertical)
 * \param mode   - counting mode (normal/binary)
 * \param start  - starting point of counting
 * \param end    - ending point of counting
 */
void counting(CvMat *matrix, int tab[MAX_SIZE], proj_type type, 
	       count_mode mode, int start, int end);


/*! Draws vertical or horizontal projection.
 *
 * \param img   - output image
 * \param tab   - input data array
 * \param start - starting point
 * \param end   - ending point
 * \param type  - direction of projection (horizontal/vertical)
 * \param color - line colour
 */
void drawProjection(IplImage *img, int tab[MAX_SIZE], int start, int end,
		    proj_type type, CvScalar color);


/*! Calculate horizontal eyes position
 *
 * \param tab    - input data array
 * \param upperb - upper limit for maximum searching
 * \param lowerb - lower limit for maximum searching
 *
 * \return - horizontal eyes position value
 */
int horizEyePosition(int tab[MAX_SIZE], int upperb, int lowerb);


/*! Calculate horizontal mouth position
 *
 * \param tab    - input data array
 * \param img_height - image height
 * \param upperb - upper limit for maximum searching
 *
 * \return horizontal mouth position value
 */
int horizMouthPosition(int tab[MAX_SIZE], int img_height, int upperb);


/*! Calculate external vertical eyes boundaries
 *
 * \param tab       - input data array
 * \param img_width - image width
 * \param ind_b     - variable for left vertical eyes boundary 
 * \param ind_e     - variable for right vertical eyes boundary 
 */
void vertEyeBorder(int tab[MAX_SIZE], int img_width, int &ind_b, int &ind_e);


/*! Calculate vertical center of eyes region
 *
 * \param tab       - input data array
 * \param img_width - image width
 * \param offset    - offset
 *
 * \return position of vertical center of eyes region
 */
int vertEyeCenter(int tab[MAX_SIZE], int img_width, int offset);


/*! Calculates vertical mouth region boundaries
 *
 * Left boundary is set on the left side of the leftmost projection range
 * of sufficient length, right boundary - on the right side of the rightmost 
 * projection range of the sufficient length.
 *
 * \param tab          - input data array
 * \param width        - image width
 * \param start        - starting point
 * \param end          - ending point
 * \param lower_border - variable for left vertical mouth boundary 
 * \param upper_border - variable for right vertical mouth boundary
 *
 */
void vertMouthBorder(int tab[MAX_SIZE], int width, int start, int end, 
		     int &lower_border, int &upper_border);


/*! Detects eyes in coarse eye region by template matching
 * 
 * \param img     - input (coarse eye region) image
 * \param templ   - eye template image
 * \param topLeft - upper left corner of the matched eye subregion
 *
 * \return template match accuracy ([0,100] range)
 */
double eyeMatching(IplImage *img, IplImage *templ, CvPoint &topLeft);


/*! Detects eyes and mouth regions in face image
 *
 * \param src       - input (face) image
 * \param eye_left  - left eye face component info
 * \param eye_right - right eye face component info
 * \param mouth     - mouth face component info
 * \param width     - face width
 * \param detect_mouth - mouth detection flag
 */ 
void detectFacialRegions(IplImage *src,  FaceComponent &eye_left,
			 FaceComponent &eye_right, FaceComponent &mouth, 
			 int &width, bool detect_mouth);


/*! Detects fine eye subregion in coarse eye region
 *
 * \param eye_img  - input (coarse eye region) image
 * \param templ    - eye template image
 * \param correction - Y coordinate offset value (to be subtracted from the 
 *                     found pupil center position)
 * \param iris     - component for the pupil position in coarse eye region
 *
 * \return template match accuracy ([0,100] range)
 */
double eyeDetection(IplImage *eye_img, IplImage *templ, int correction,
		    FaceComponent &iris);


/*! Finds locations of face components subregions in face image
 *
 * \param source_img - input (face) image
 * \param face - face describing structure to be filled
 * \param detect_mouth - mouth detection flag
 * \param detect_eyeballs - eyeballs detection flag
 */
void findFaceRegions(IplImage *source_img, FFace &face, 
			   bool detect_mouth=false, bool detect_eyeballs=false);


/*! Example usage of function for coarse face subregions detection
 * 
 *  \param face - face image file name
 */
void example_usage(char *face);



/*! \class ImgProcMethods
 *
 * Implements selected image processing methods.
 */
class ImgProcMethods 
{
protected:

  /*! Memory storage for contour finding operations */
  static CvMemStorage* contours_storage;

  /*! Memory storage for other operations */
  static CvMemStorage* storage;

public:

  /*! Counts connected areas in input image
   *
   * \param src  - input image
   * \param draw - contours drawing flag
   * \param dst  - output image for contours
   *
   * \return count of connected areas in input image
   */
  int findConnectedRegions(IplImage *src, bool draw=false, 
			       IplImage *dst = 0);  


  /*! Hysteresis thresholding 
   *
   * \param src - input image
   * \param dst - output image
   * \param R - lower (radical) threshold
   * \param L - upper (liberal) threshold
   */
  void hysteresisThresholding(IplImage *src, IplImage *dst, int R, int L);


  /*! Checks the neighbourhood of (x,y) for the presence of pixels darker
   * than R threshold.
   * 
   * \param src - input image
   * \param y   - Y coordinate of pixel
   * \param x   - X coordinate of pixel
   * \param R   - lower (radical) threshold
   *
   * \retval true  - if a pixel darker than R threshold was found in (x,y) 
   * neighbourhood
   * \retval false - otherwise
   */
  bool checkPixelNeighbourhood(IplImage *src, int y, int x, int R);


  /*! Creates silhouette outline.
   *
   * Uses morphological gradient.
   *
   * \param src - input image
   * \param dst - output image
   */
  void createOutline(IplImage *src, IplImage *dst);


  /*! Removes the blobs on the image boudary.
   *
   * Uses morphological operators.
   *
   * \param src - input image
   * \param dst - output image
   * \param flag - flag deciding along which boundaries blobs will be removed; 
   *               4-bit value LRTB (left, right, top, bottom), 1 marks for
   *               removing;
   *               eg. 0011 means removing top and bottom boundary blobs.
   */
  void removeBoundaryBlobs(IplImage *src, IplImage *dst, std::bitset<4> flag);


  /*! Removes small blobs.
   *
   * Uses morphological operators.
   *
   * \param src  - input image
   * \param dst  - output image
   * \param iter - number of iterations
   */
  void removeSmallBlobs(IplImage *src, IplImage *dst, int iter);


  /*! Finds contours in the image
   *
   * \param src      - input image
   * \param contours - output contours sequence
   */
  void findContours(IplImage *src, CvSeq *&contours);


/*! Displays image histogram
 *
 * \param src - input image 
 * \param display - histogram window display flag
 * \param size - histogram image size
 *
 * \return Calculated histogram image.
 */
IplImage *showHistogram(const IplImage *src, bool display=false, 
			 CvSize size=cvSize(512,200));


/*! Transforms input image by histogram stretching
 * 
 * \param src - input image
 * \param dst - output image
 *
 * Source www:
 * http://tech.groups.yahoo.com/group/OpenCV/message/23708
 */
void stretchHistogram(const IplImage* src, IplImage *dst);


/*! Calculates threshold on the histogram basis
 *
 *  \param src - input image
 *  \param wsp - ratio (wsp/100) of white pixels after input image 
 *                thresholding ([0,100] range)
 *
 *  \return calculated threshold value
 */
int findThresholdByHist(IplImage *src, int wsp);


  /*! Hough transform (line searching)
   *
   * \param src - input image
   * \param linesList - list of detected lines
   *
   * \return number of detected lines
   */
  int doHoughTransform(IplImage *src, std::list<CvPoint> &linesList);  


  /*! Detects any object using Haar classifier
   *
   * \param src     - input image
   * \param cascade - Haar cascade for the object
   * \param element - face component of the object
   * \param scale   - result rectangle scaling ratio
   * \param scale_factor - Haar classifier scaling factor
   * \param min_neighbors - min count of neighbours
   */
  void
  detectObjectHaar(IplImage *src, CvHaarClassifierCascade* cascade,
		      FaceComponent &element, double scale = 1, 
		      double scale_factor = 1.2, int min_neighbors = 5);

};


/*! Calculates equation parameters of straight-line through two points
 *  (p1.x, p1.y),(p2.x, p2.y).
 *
 *  Variables a, b are set to values of parameters for line equation
 *  y = ax + b
 *
 *  \param p1, p2 - points
 *  \param a, b - line equation parameters
 */
void drawStrLine(CvPoint p1, CvPoint p2, double &a, double &b);


/*! Calculates equation parameters of straight-line through two points 
 *  (p1.x, p1.y),(p2.x, p2.y).
 *  Variables A, B, C are set to values of parameters for line equation
 *  Ax + By + C = 0
 *
 *  \param p1, p2 - points
 *  \param A, B, C - line equation parameters
 */
void drawStrLine(CvPoint p1,CvPoint p2,
		     double &A, double &B, double &C);


/*! Calculates line slope angle
 *
 *  a=tg(alpha); where alpha - line slope angle
 *  a= -A/B; => tg(alfa)= -A/B => alfa = arctan(-A/B)
 *
 *  \param A, B - line equation parameters Ax + By + C = 0
 * 
 *  \return line slope angle value
 */
double calcAngleOX(double A, double B);


/*! Calculates angle between two lines
 *  Lines are defined by:
 *  A1x + B1y + C1 = 0
 *  A2x + B2y + C2 = 0
 *
 *  \param A1, B1 - first line equation parameters
 *  \param A2, B2 - second line equation parameters
 *
 *  \return value of the angle between two lines
 */
double calcTwoLinesAngle(double A1, double B1, double A2, double B2);


/*! Calculates angle between two lines
 *
 *  Lines are defined by:
 *  y = a1x + b
 *  y = a2x + b
 *
 *  \param a1 - first line equation parameters
 *  \param a2 - second line equation parameters
 *
 *  \return value of the angle between two lines
 */
double calcTwoLinesAngle(double a1, double a2);


/*! Converts radians to degrees
 *
 *  \param rad - value in radians
 *
 *  \return value in degrees
 */
double rad2deg(double rad);


/*! Converts degrees to radians
 *
 *  \param deg - value in degrees
 *
 *  \return value in radians
 */
double deg2rad(double deg);


/*! Calculates distance between two points
 *
 *  \param p1, p2 - points
 *
 *  \return distance between the points
 */
double pointsDistance(CvPoint p1, CvPoint p2);



/*! Structure of face features for saving values after detection:
 *
 * roix    -- face ROI upper left corner X coordinate (pixels)
 *
 * roiy    -- face ROI upper left corner Y coordinate (pixels)
 *
 * angle   -- face declination angle (not verified, for future use)
 *
 * LEbBnd  -- left eyebrow bend angle (top)
 *
 * LEbDcl  -- left eyebrow declination angle (side)
 *
 * LEyOpn  -- distance between the right eyelids (rel. eyeball subregion)
 *
 * LEbHgt  -- distance between left pupil and eyebrow top (rel. eye subregion)
 *
 * REbBnd  -- right eyebrow bend angle (top)
 *
 * REbDcl  -- right eyebrow declination angle (side)
 *
 * REyOpn  -- distance between the right eyelids (rel. eyeball subregion)
 *
 * REbHgt  -- distance between right pupil and eyebrow top (rel. eye subregion)
 *
 * LiAspt  -- aspect ratio of the lips bounding box (percents)
 *
 * LLiCnr  -- Y position of the left corner of the lips (rel. lips bounding box)
 *
 * RLiCnr  -- Y position of the right corner of the lips (rel. lips bounding box)
 *
 * Wrnkls  -- number of horizontal wrinkles in the center of the forehead
 *
 * Nstrls  -- nostrils baseline width (rel. face width)
 *
 * TeethA  -- area of the visible teeth (rel. lips bounding box)
 */ 
typedef struct {
 double roix,
        roiy,
        angle,
        LEbBnd,
 	LEbDcl,
	LEyOpn,
	LEbHgt,
	REbBnd,
	REbDcl,
	REyOpn,
	REbHgt,
	LiAspt,
	LLiCnr,
	RLiCnr,
	Wrnkls,
	Nstrls,
	TeethA;
} facepar_t;

/*! \class Facet
 *
 * Includes methods for face features detection and parameterisation.
 */
class Facet: 
              public FaceParameters, 
	      public HaarParameters,
	      public ImgProcMethods
{
public:

  /*! Face components data */
  FFace face;

  /*! Pointer to structure with the detected values */
  static facepar_t * currfacptr;

  /*! List of the detected faces and parameters */
  std:: list<facepar_t> facesList;

  /*! Haar method for mouth detection flag */
  bool mouthHaar;

  /*! Haar method for nose detection flag */
  bool noseHaar;

  /*! Haar method for eyeballs detection flag */
  bool eyeballsHaar;

  Facet();
  ~Facet();

  /*! Read the settings from config file
   *
   * \param file_name - settings file name
   * 
   * \retval true  - succesful reading of the config file
   * \retval false - otherwise
   */
  bool readSettings(std::string file_name = "default.cfg");

  /*! Remove all the elements of the faces list (\link
   *  Facet::facesList facesList\endlink) 
   */
  void cleanFacesList();

  /*! Detect the face features in the image
   *
   * \param src - input image
   * \param dst - output image
   */
  void detectFeat(IplImage *src, IplImage *dst);

private:

  /*! Check points location 
   *
   * Checks if width and height of the rectangle spanned by p1 i p2 
   * points are positive.
   *
   * \param p1, p2 - points to be checked
   */
  void checkSize(CvPoint &p1, CvPoint &p2);

  /*! Check if the sizes of the eyes subregions are > 0. */
  void checkEyesSize();

  /*! Check if the sizes of the eyeballs subregions are > 0. */
  void checkEyeballsSize();

  /*! Check if the size of the mouth subregion is > 0. */
  void checkMouthSize();

  /*! Check if the size of the lips subregion is > 0.  */
  void checkLipsSize();

  /*! Check if the size of the nose subregion is > 0.  */
  void checkNoseSize();

  /*! Check if the size of the forehead subregion is > 0.  */
  void checkForeheadSize();

  /*! Calculate the face declination
   *  
   * Uses the location of the eyeballs.
   *
   * \return declination (in degrees) of the eyeballs baseline from the 
   * horizontal line.
   */ 
  double calcFaceDeclination();

  /*! Face detection with Haar classifier
   *
   * \param src - input image
   *
   * \return sequence containing locations of all the detected faces
   */
  CvSeq *faceDetectHaar(IplImage *src);

  /*! Face detection with skin hue method
   *
   * \param src  - input image
   * \param lips - lips detection flag
   * \param eyes - eyes detection flag
   */
  void faceDetectHue(IplImage *src, bool lips=false, bool eyes=false);

  /*! Detect eyebrows
   *
   * \param src  - input image
   * \param iris - eyball info
   * \param eye_region - eye region info
   * \param brow - eyebrow info
   * \param flaga - boundary elements removal flag for eyebrows
   */
  void detectEyebrow(IplImage *src, FaceComponent iris, 
		      FaceComponent eye_region, FaceComponent &brow, 
		      std::bitset<4> flaga);

  /*! Detect eyelids
   *
   * \param src - input image
   * \param iris - eyeball info
   * \param lid - eyelid info
   *
   * \retval eye opening relative to the eyeball subregion
   */
  double detectEyelids(IplImage *src, FaceComponent iris, FaceComponent &lid);

  /*! Detect mouth with Haar classifier 
   * 
   * \param src - input image
   */
  void detectMouthHaar(IplImage *src);

  /*! Detect nose with Haar classifier 
   * 
   * \param src - input image
   */
  void detectNoseHaar(IplImage *src);

  /*! Detect right eyeball with Haar classifier 
   * 
   * \param src - input image
   */
  void detectRightEyeballHaar(IplImage *src);

  /*! Detect left eyeball with Haar classifier 
   * 
   * \param src - input image
   */
  void detectLeftEyeballHaar(IplImage *src);

  /*! Detect nostrils
   * 
   * \param src - input image
   * \param nose - nose location info
   */
  void detectNose(IplImage *src, FaceComponent &nose);

  /*! Detect forehead and wrinkles
   * 
   * \param src - input image
   * \param linesList - list of the detected wrinkles
   */
  void detectForehead(IplImage *src, std::list<CvPoint> &linesList);

  /*! Detect lips
   *
   * \param src - input image
   * \param lip - Lips location info
   * \param lips_outline - point sequence defining the lips outline
   */
  void detectLips(IplImage *src, FaceComponent &lip, CvSeq *&lips_outline);

  /*! Detect teeth
   *
   * \param src - input image
   */
  void detectTeeth(IplImage *src);

  /*! Parameterise lips
   *
   * \param result_c - lips image
   * \param lip - lips location info
   * \param lips_outline - point sequence defining the lips outline
   */
  void parameteriseLips(IplImage *result_c, FaceComponent &lip, 
			 CvSeq *lips_outline);

  /*! Parameterise eyebrows
   *
   * \param brow - eyebrows location info
   */
  void parameteriseEyebrow(FaceComponent brow, std::bitset<4> flaga);

  /*! Define the lips bounding rectangle
   *
   * \param contour - point sequence defining the lips outline
   */
  CvRect findMouthRectangle(CvSeq *contour);

  /*! Find the left eyebrow height over the pupil */
  int calcLeftEyebrowHeight();

  /*! Find the right eyebrow height over the pupil */
  int calcRightEyebrowHeight();
  
  /*! Draw the face features 
   *
   * \param dst - output image
   * \param listOfLines - list of poins defining the wrinkles
   * \param lips - lips outline sequence
   */
  void drawFeatures(IplImage *dst, std::list<CvPoint> listOfLines,
		     CvSeq *lips);

  /*! Draw the eyebrows
   *
   * \param src - input image
   * \param eye_region - left eye region info
   * \param brow - eyebrows info
   */
  void drawEyebrows(const IplImage *src, FaceComponent eye_region, 
		     FaceComponent brow);
};

#endif 


/*! \mainpage 
  <b>
  This document was generated for FacET (Facial Expression Tracker),
  a library of image processing 
  procedures for detecting and parameterising face components (eg. eyes, 
  eyebrows, lips, forehead wrinkles).\n 
  Such face features are useful for classification of human emotions 
  based on facial expression.
 
  The current version of FacET was developed by Marek Wnuk and is
  based on the application created for Marcin Namysl's Master thesis 
  (supervised by Marek Wnuk, Wroclaw University of Technology, 
  Institute of Computer Engineering, Control and Robotics): 
  </b>\n ,,<em>Wykorzystanie systemu wizyjnego do rozpoznawania emocji 
  czlowieka</em>'' (,,<em> Vision system in human emotions recognition
  </em>'').\n 
 
    FacET is a library for detecting and parameterising face components.\n
    Copyright (C) 2009  Marek Wnuk <marek.wnuk@pwr.wroc.pl>\n

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.\n

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.\n

    You should have received a copy of the GNU General Public License
    along with this program.\n
    If not, see <http://www.gnu.org/licenses/>.
 
  \n\n
  \section sec_obsluga FacET library
 

  The main public function of the library is \link Facet::detectFeat face   
  features detection\endlink:
  \verbatim
  void detectFeat(IplImage *src, IplImage *dst);
  \endverbatim
 
  Using the src as input image, it fills the \link Facet::facesList facesList\endlink - list of \link  
  facepar_t facepar_t\endlink structures of features for all the faces  
  detected in the source image:
  \verbatim
  std:: list<facepar_t> facesList;
  \endverbatim
Structure of the type \link facepar_t facepar_t\endlink contains the face features values after detection:

  \verbatim
  roix    -- face ROI upper left corner X coordinate (pixels)
  roiy    -- face ROI upper left corner Y coordinate (pixels)
  angle   -- face declination angle (not verified, for future use)
  LEbBnd  -- left eyebrow bend angle (top)
  LEbDcl  -- left eyebrow declination angle (side)
  LEyOpn  -- distance between the right eyelids (rel. eyeball subregion)
  LEbHgt  -- distance between left pupil and eyebrow top (rel. eye subregion)
  REbBnd  -- right eyebrow bend angle (top)
  REbDcl  -- right eyebrow declination angle (side)
  REyOpn  -- distance between the right eyelids (rel. eyeball subregion)
  REbHgt  -- distance between right pupil and eyebrow top (rel. eye subregion)
  LiAspt  -- aspect ratio of the lips bounding box (percents)
  LLiCnr  -- Y position of the left corner of the lips (rel. lips bounding box)
  RLiCnr  -- Y position of the right corner of the lips (rel. lips bounding box)
  Wrnkls  -- number of horizontal wrinkles in the center of the forehead
  Nstrls  -- nostrils baseline width (rel. face width)
  TeethA  -- area of the visible teeth (rel. lips bounding box)
  \endverbatim

The dst image is used merely as an output for visualisation (a copy of input
image with face regions markers). Both src and dst can point to the same image.

Reading the settings from config file is provided by:

  \verbatim
  bool readSettings(std::string file_name = "default.cfg"),
  \endverbatim

which returns true in case of successful reading, false otherwise.

Initialisation of the \link Facet::facesList facesList\endlink is provided by:

  \verbatim
  void cleanFacesList().
  \endverbatim

The library is intended to be platform-independent. It does not contain 
any GUI components, but it makes use of OpenCV structures and image 
processing functions. 



\section ss_demo Demo application

In order to check the library functionality and to make its usage clear,
a demo application is included, as an example of FacET library usage
with a simple, text-based user interface.

It provides face features processing in the images captured from 
camera or a video/image file:

  \verbatim
 
 #----------------- FacET demo -----------------#
 |       started at: 09.03.04.22.40.46          |
 |                                              |
 | (t) - Main program (mode : camera)           |
 | (v) - Main program (mode : video file)       |
 | (z) - Main program (mode : image file)       |
 | (q) - Quit                                   |
 |                                              |
 #----------------------------------------------#

 > 
  \endverbatim

Camera interface and several image capture parameters can be set 
with command-line options (overriding the defaults, or config file 
settings):

  \verbatim
Command line options:

-b INT	set brightness
-c INT	set contrast
-s INT	set saturation
-h INT	set hue
-g INT	set gain
-n INT	set channel
-m INT	set video mode
-l INT	set mirror mode
-u INT	set rotate mode
-v 	V4L interface
-f 	1394 interface
-p 	show this help message
  \endverbatim

 
  A sample configuration file \link Facet::readSettings reading\endlink 
  is also provided:
 
  \verbatim
#===================================#
# *  Default configuration file  *  #
#                                   #
#  Author: Marek Wnuk               #
#  Creation date: 03.03.2009        #
#                                   #
#===================================#

# FacET library parameters
# ========================

# Features detection parameters
EYEBROWS_RATIO 25
LIPS_RATIO 25
FACE_SCALING 4

# Paths to Haar classifier cascades
FACE_PATH /opt/opencv/share/opencv/haarcascades/haarcascade_frontalface_alt.xml
R_EYE_PATH cascades/eyes/thing_shan_eye_cascade.xml
L_EYE_PATH cascades/eyes/thing_shan_eye_cascade.xml
MOUTH_PATH cascades/mouth/Mouth.xml
NOSE_PATH cascades/nose/Nariz.xml

# Image capture parameters
# ========================

BRIGHTNESS 50         # Brightness, contrast, saturation and hue can be
CONTRAST 13           # initialised with any value from [0,100] range
SATURATION 13
HUE 0
GAIN 30
IMAGE_WIDTH 640         
IMAGE_HEIGHT 480          
FLIP 0		      # 0 => flip= FALSE, otherwise flip=TRUE
ROTATE 0              # 0 => rotate= FALSE, otherwise rotate=TRUE

# Application parameters
# ======================

# Application options
TIMING  0             # 0 => timing on, otherwise timing off
IMGREC  1             # 0 => imgrec= FALSE, otherwise imgrec=TRUE
PARREC  1             # 0 => parrec= FALSE, otherwise parrec=TRUE
SHOW    1             # 0 => show= FALSE, otherwise show=TRUE
VERBOSE 1             # verbosity level (0 => no stderr output)

# Output path for the results of processing
IMAGE_PATH output/
 \endverbatim

The first section, "FacET library parameters" is read by \link Facet::readSettings readSettings()\endlink, 
which sets features detection parameters and paths to Haar classifier cascades. 
Two detection parameters (eyebrows proportions and lips proportions) can be 
used to trim the features detection methods in FacET library. The preset 
values are used by the histogram-based threshold calculation procedure for
eyebrows and lips detection algorithms.
 
"Image capture parameters" and "Application parameters" can be also defined,
up to the user needs. The user should provide relevant function for his own
parameters reading. In the demo, the functions are defined in
"demo/src/capture_mw.cpp":  \link Capture::readSettings readSettings(string
file_name)\endlink,
and in "demo/src/main.cpp": \link readOptions readOptions(string file_name)
\endlink.

The demo application can output the results in several, user-selectable 
forms. Application options in config file are used for switching them off 
and on.

If PARREC is set, a new parameter file "1_fea.prm", "2_fea.prm", ... "n_fea.prm", is created for each consecutive run of the detection
procedure. 
If IMGREC is set, output images/videos are saved as "n_img.png" and 
"n_vid.avi" respectively.
Setting the location of the output files is possible with 
IMAGE_PATH configuration parameter.
If SHOW is set, the output image from the detectFeat() library function 
is displayed in "Result" window (using highgui cvShowImage()).

A sample parameters registration file follows:

 \verbatim
 1; 180 72 2.02136 144 21 45 26 146 22 43 26 229 90 70 0 26.1084 0 
 2; 180 72 0.674037 141 22 46 24 142 21 45 25 233 86 76 2 24.1379 0 
 3; 184 72 1.38035 147 18 49 26 141 21 43 27 236 86 73 0 24.1379 0 
 4; 184 68 0.65106 145 19 42 25 146 17 42 26 241 86 79 2 24.1379 0 
 5; 184 72 1.99788 148 16 45 26 149 20 42 26 253 78 82 2 24.2574 0 
 6; 180 68 2.04541 143 21 46 25 146 21 41 24 237 82 79 0 25.2475 0 
 7; 184 72 1.99788 140 24 45 27 146 18 42 27 239 85 78 1 25.2475 0 
 8; 188 72 2.75911 144 18 44 26 143 20 43 25 215 65 57 1 23.7624 0 
 9; 180 68 1.99788 139 24 44 26 143 22 45 25 244 86 82 0 25.7426 0 
 \endverbatim

  The first record contains:

 \verbatim
 1;      - frame number in corresponding .avi file
 180     - face ROI upper left corner X coordinate (pixels)
 72      - face ROI upper left corner Y coordinate (pixels)
 2.02136 - face declination angle (not verified, for future use)
 144     - LEbBnd     left eyebrow bend angle (top)
 21      - LEbDcl     left eyebrow declination angle (side)
 45      - LEyOpn     distance between the right eyelids (relative to the eyeball subregion)
 26      - LEbHgt     distance between left pupil and eyebrow top (relative to the eye subregion)
 146     - REbBnd     right eyebrow bend angle (top)
 22      - REbDcl     right eyebrow declination angle (side)
 43      - REyOpn     distance between the right eyelids (relative to the eyeball subregion)
 26      - REbHgt     distance between right pupil and eyebrow top (relative to the eye subregion)
 229     - LiAspt     aspect ratio of the lips bounding box (percents)
 90      - LLiCnr     Y position of the left corner of the lips (relative to the lips bounding box)
 70      - RLiCnr     Y position of the right corner of the lips (relative to the lips bounding box)
 0       - Wrnkls     number of horizontal wrinkles in the center of the forehead
 26.1084 - Nstrls     nostrils baseline width (relative to the face width)
 0       - TeethA     area of the visible teeth (relative to the lips bounding box)
 \endverbatim

  In case of more than one face detected: face position, declination, and 14 
  parameters are appended to the same line (frame number).
 
*/

