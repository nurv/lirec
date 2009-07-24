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
#include "Classifier.h"
#include "Image.h"

#ifndef FACE_BANK
#define FACE_BANK

class FaceBank
{
public:
	// FaceWidth and FaceHeight are the size for the internal stored image of the face for 
	// comparison, ErrorThresh is the threshold at which a face will be considered as recognised,
	// NewImageThresh is a threshold greater than which a suggested face will be stored as 
	// a new image.
	FaceBank(unsigned int FaceWidth, unsigned int FaceHeight, float ErrorThresh, float NewImageThresh, Classifier *c);
	~FaceBank();

	void Clear();
	
	void SetErrorThresh(float s) { m_ErrorThresh=s; }
	void SetNewImageThresh(float s) { m_NewImageThresh=s; }
	
	// Learn this face, the face may be a false positive, so we'll discard the 
	// suggestion if we've seen it before, and the error is greater than ErrorThresh
	float Suggest(Image face, unsigned int ID);

	// Gives the id, given a face, and returns the confidence
	float Identify(Image face, unsigned int &ID, int &imagenum);
		
	std::map<unsigned int, Image> &GetFaceMap() { return m_FaceMap; }
	
	unsigned int GetFaceWidth() { return m_FaceWidth; }
	unsigned int GetFaceHeight() { return m_FaceHeight; }

	void Save(const std::string &filename) const;
	void Load(const std::string &filename);
	
	Classifier *GetClassifier() { return m_Classifier; }

private:	

	unsigned int m_FaceWidth;
	unsigned int m_FaceHeight;
	float m_ErrorThresh;
	float m_NewImageThresh;
	
	std::map<unsigned int, Image> m_FaceMap;
	
	Classifier *m_Classifier;
};

#endif
