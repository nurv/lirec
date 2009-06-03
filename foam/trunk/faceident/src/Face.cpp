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
#include "highgui.h"

#include "ImageUtils.h"
#include "Face.h"

#ifdef WIN32
#include <string>
#define snprintf _snprintf 
#endif

using namespace std;

/////////////////////////////////////////////////////////////////////////////////

Face::Face(IplImage *image)
{
	AddImage(image);
}

Face::~Face() 
{
	for(vector<IplImage *>::iterator i=m_ImageVec.begin(); i!=m_ImageVec.end(); i++)
	{
		cvReleaseImage(&(*i));
	}
}

void Face::Learn(const IplImage *image, float blend, int imagenum)
{
	CvSize sizea = cvGetSize(image);
	CvSize sizeb = cvGetSize(m_ImageVec[0]);

	assert(sizea.width == sizeb.width);
	assert(sizea.height == sizeb.height);
	assert(imagenum < (int)m_ImageVec.size());
	
	float ret=0;

    for(int y=0; y<sizea.height; y++)
	{
        for(int x=0; x<sizea.width; x++)
		{
			cvSet2D(m_ImageVec[imagenum],y,x,cvScalar(
						cvGet2D(m_ImageVec[imagenum],y,x).val[0]*(1 - blend) + 
							cvGet2D(image,y,x).val[0]*blend,
						cvGet2D(m_ImageVec[imagenum],y,x).val[1]*(1 - blend) + 
							cvGet2D(image,y,x).val[1]*blend,
						cvGet2D(m_ImageVec[imagenum],y,x).val[2]*(1 - blend) + 
							cvGet2D(image,y,x).val[2]*blend));
		}
	}
}

void Face::AddImage(IplImage *image)
{
	m_ImageVec.push_back(image);
}

float Face::FindSimilar(const IplImage *image, int &imagenum)
{
	float error=FLT_MAX;
	imagenum=-1;
	unsigned int count=0;
	
	for(vector<IplImage *>::iterator i=m_ImageVec.begin(); i!=m_ImageVec.end(); i++)
	{
		float tmp = Diff(image,*i);
		if (tmp<error)
		{
			error=tmp;
			imagenum=count;
		}
		count++;
	}
	
	return error;
}
