//----------------------------------------------
// Heriot-Watt University
// MACS 
// www.lirec.eu
// author: Amol Deshmukh
// Date: 17/03/2009
//-----------------------------------------------

#ifndef FACEDETECT_INCLUDEDEF_H
#define FACEDETECT_INCLUDEDEF_H


#include <cv.h>
#include <cvaux.h>
#include <highgui.h>

#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <math.h>
#include <float.h>
#include <limits.h>
#include <time.h>
#include <ctype.h>
#include <iostream>
//-----------------------------------------------

/**
 * This class provides face detection capabilties
 */
class FaceDetect
{
public:

		FaceDetect();

		~FaceDetect();

		//Set scale for the image
		void SetScale( double dScale){m_dScale = dScale; };

		double GetScale(){ return m_dScale ;};

		// show output on screen
		void ShowResult( bool bflag){m_bflagShowResult = bflag; };

		// output screen flag
		bool GetShowResultFlag(){return m_bflagShowResult;};

 		void StartFaceDetection(void);

		double GetFaceMidPointX(){ return m_dMidX;};

		double GetFaceMidPointY(){ return m_dMidY;};

		double GetFaceAngleX(){ return m_dAngleX;};

		double GetFaceAngleY(){ return m_dAngleY;};
		
		// number of faces detected
		int m_iNumFaces;
		
		// boolean value if face is detected
		bool m_bFaceDetected;

		// flag to detect face at threshold distance (camera and resolution dependent)
		// resolution required 640 * 480 (can be modified acc to requirement)
		bool m_bUserProximicFlag;


		
private:
		// scale factor for image
		double m_dScale;


		//face midpoint(+ve:Face is left side, -ve:right side of camera)
		double m_dMidX;
		double m_dMidY;
		
		//angle from face midpoint
		//(if value is +ve:Face is at left side, -ve:right side of camera)
		double m_dAngleX;
		double m_dAngleY;

		//show result in a window
		bool m_bflagShowResult;
		
		//detects a face and draws rectangle around the face
		void DetectAndDraw( IplImage* cvImg, double dScale );

		//detect face in a region of an image
		int DetectSubFace(IplImage* cvTempimage);

		
};
#endif
