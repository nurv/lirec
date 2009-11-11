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

#include <vector>
#include "cv.h"
#include "Geometry.h"
#include "Image.h"

#ifndef FOAM_FACEFINDER
#define FOAM_FACEFINDER

// a wrapper for the opencv facefinder
// todo - make generic so we can swap in different algorithms
class FaceFinder
{
public:
	FaceFinder();
	~FaceFinder();
	
	std::vector<Rect> Find(const Image &im, bool largest);
	
private:
	CvHaarClassifierCascade* m_Cascade;
	CvMemStorage* m_Storage;

};

#endif
