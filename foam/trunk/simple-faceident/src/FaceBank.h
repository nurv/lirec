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
#include <list>
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
	// MaxFaces is the number of faces we want to restrict the facebank to storing, 
	// FaceWidth and FaceHeight are the size for the internal stored image of the face for 
	// comparison, ErrorThresh is the error amount which will trigger a new face to be stored
	FaceBank(unsigned int MaxFaces, unsigned int FaceWidth, unsigned int FaceHeight, float ErrorThresh);
	
	~FaceBank();
	
	// gives the id, given a new face, and returns the confidence
	// if it's a new face it returns 1 and stores the id, copying the face image
	float Identify(IplImage *face, unsigned int &ID, bool learn);
	
	std::list<Face*> &GetFaceList() { return m_FaceList; }
	
private:	

	unsigned int m_MaxFaces;
	unsigned int m_FaceWidth;
	unsigned int m_FaceHeight;
	float m_ErrorThresh;
	
	std::list<Face*> m_FaceList;
};

#endif
