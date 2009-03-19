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
#include <assert.h>

#include "cv.h"

#ifndef FACE_BANK
#define FACE_BANK

/////////////////////////////////////////////////////////////////////////////////
// A face representation for the facebank

class Face
{
public:
	Face(IplImage *image);
	~Face();

	// Blends a newly detected face into this image,
	// an attempt at making it a little more dynamic
	// needs more testing.
	void Learn(const IplImage *image, float blend);

	IplImage *m_Image;
};

/////////////////////////////////////////////////////////////////////////////////
// A database of detected faces

class FaceBank
{
public:
	// FaceWidth and FaceHeight are the size for the internal stored image of the face for 
	// comparison, ErrorThresh is the error amount which will trigger a new face to be stored
	FaceBank(unsigned int FaceWidth, unsigned int FaceHeight, float ErrorThresh);
	
	~FaceBank();

	void Clear();
	
	// Learn this face, the face may be a false positive, so we'll discard the 
	// suggestion if we've seen it before, and the error is greater than ErrorThresh
	float Suggest(IplImage *face, unsigned int ID);

	// Gives the id, given a face, and returns the confidence
	float Identify(IplImage *face, unsigned int &ID);
	
	std::map<unsigned int, Face*> &GetFaceMap() { return m_FaceMap; }
	
	unsigned int GetFaceWidth() { return m_FaceWidth; }
	unsigned int GetFaceHeight() { return m_FaceHeight; }

private:	

	unsigned int m_FaceWidth;
	unsigned int m_FaceHeight;
	float m_ErrorThresh;
	
	std::map<unsigned int, Face*> m_FaceMap;
};

#endif
