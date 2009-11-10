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

#ifndef FACE
#define FACE

/////////////////////////////////////////////////////////////////////////////////
// A face representation
//
// Consists of a set of images representing an identity. 

class Face
{
public:
	Face(IplImage *image);
	~Face();
	
	void AddImage(IplImage *image);
	float FindSimilar(const IplImage *image, int &imagenum);

	// Blends a newly detected face into this image,
	// an attempt at making it a little more dynamic
	// needs more testing.
	void Learn(const IplImage *image, float blend, int imagenum);

	std::vector<IplImage *> m_ImageVec;
};

#endif

