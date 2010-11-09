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

#include "FaceFinder.h"
	
using namespace std;

FaceFinder::FaceFinder()
{
	m_Cascade = (CvHaarClassifierCascade*)cvLoad("haarcascade_frontalface_alt.xml", 0, 0, 0);
	assert(m_Cascade);
	m_Storage = cvCreateMemStorage(0);
}

FaceFinder::~FaceFinder()
{
	
}

vector<Rect> FaceFinder::Find(const Image &in, bool largest)
{
	cvClearMemStorage(m_Storage);

	int flags=0;
	//if (largest) flags|=CV_HAAR_FIND_BIGGEST_OBJECT;

	CvSeq* faces = cvHaarDetectObjects( in.m_Image, m_Cascade, m_Storage,
			1.1, 2, flags
			//|CV_HAAR_FIND_BIGGEST_OBJECT
			//|CV_HAAR_DO_ROUGH_SEARCH
			//|CV_HAAR_DO_CANNY_PRUNING
			//|CV_HAAR_SCALE_IMAGE
			,
			cvSize(30, 30) );
			
	vector<Rect> ret;
	for(int i = 0; i < (faces ? faces->total : 0); i++ )
	{
		CvRect* r = (CvRect*)cvGetSeqElem( faces, i );
		ret.push_back(Rect(r->x,r->y,r->width,r->height));
	}
	
	return ret;
}
