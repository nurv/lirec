// Copyright (C) 2009 foam
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

#include <iostream>
#include <map>
#include <vector>
#include <assert.h>
#include "cv.h"
#include "Face.h"

#ifndef FACE_BANK
#define FACE_BANK

/////////////////////////////////////////////////////////////////////////////////
// A database of detected faces
//
// The facebank is mainly driven by calling Suggest() (which maps face images with 
// id numbers) or Identify() which looks in it's current set of faces (and in the 
// images contained in those faces) for the closest match. 
//
// Thresholding explanations
//
// Suggest mode:
//
//    Error ->
//    0          RelearnThresh       ErrorThresh                         1
//    I----------------I------------------I------------------------------>
//        Do nothing      Add New Image       Suggestion rejected
//
// In suggest mode, suggestions are also rejected if they are too similar
// to faces already in the bank - this is an attempt to make calibration 
// possible while other people are in shot.
//
// Identify mode:
//
//    Error ->
//    0                              ErrorThresh                         1
//    I-----------------------------------I------------------------------>
//                  Match                           No match
//
//

class FaceBank
{
public:
	// FaceWidth and FaceHeight are the size for the internal stored image of the face for 
	// comparison, ErrorThresh is the threshold at which a face will be considered as recognised,
	// NewImageThresh is a threshold greater than which a suggested face will be stored as 
	// a new image.
	FaceBank(unsigned int FaceWidth, unsigned int FaceHeight, float ErrorThresh, float NewImageThresh);
	~FaceBank();

	void Clear();
	
	void SetErrorThresh(float s) { m_ErrorThresh=s; }
	void SetNewImageThresh(float s) { m_NewImageThresh=s; }
	
	// Learn this face, the face may be a false positive, so we'll discard the 
	// suggestion if we've seen it before, and the error is greater than ErrorThresh
	float Suggest(IplImage *face, unsigned int ID);

	// Gives the id, given a face, and returns the confidence
	float Identify(IplImage *face, unsigned int &ID, int &imagenum);
		
	std::map<unsigned int, Face*> &GetFaceMap() { return m_FaceMap; }
	
	unsigned int GetFaceWidth() { return m_FaceWidth; }
	unsigned int GetFaceHeight() { return m_FaceHeight; }

	void Save(const std::string &filename) const;
	void Load(const std::string &filename);

private:	

	unsigned int m_FaceWidth;
	unsigned int m_FaceHeight;
	float m_ErrorThresh;
	float m_NewImageThresh;
	
	std::map<unsigned int, Face*> m_FaceMap;
};

#endif
