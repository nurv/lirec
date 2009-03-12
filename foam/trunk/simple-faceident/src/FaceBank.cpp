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

#include "FaceBank.h"
#include "ImageUtils.h"

using namespace std;

/////////////////////////////////////////////////////////////////////////////////

Face::Face(IplImage *image) : 
m_Image(image) 
{
}

Face::~Face() 
{
	cvReleaseImage(&m_Image);
}

void Face::Learn(const IplImage *image, float blend)
{
	CvSize sizea = cvGetSize(image);
	CvSize sizeb = cvGetSize(m_Image);

	assert(sizea.width == sizeb.width);
	assert(sizea.height == sizeb.height);

	float ret=0;

    for(int y=0; y<sizea.height; y++)
	{
        for(int x=0; x<sizea.width; x++)
		{
			cvSet2D(m_Image,y,x,cvScalar(
						cvGet2D(m_Image,y,x).val[0]*(1 - blend) + cvGet2D(image,y,x).val[0]*blend,
						cvGet2D(m_Image,y,x).val[1]*(1 - blend) + cvGet2D(image,y,x).val[1]*blend,
						cvGet2D(m_Image,y,x).val[2]*(1 - blend) + cvGet2D(image,y,x).val[2]*blend));
		}
	}
}

/////////////////////////////////////////////////////////////////////////////////
	
FaceBank::FaceBank(unsigned int MaxFaces, unsigned int FaceWidth, unsigned int FaceHeight, float ErrorThresh) : 
m_MaxFaces(MaxFaces), 
m_FaceWidth(FaceWidth),
m_FaceHeight(FaceHeight),
m_ErrorThresh(ErrorThresh)
{
}

FaceBank::~FaceBank() 
{	
	for(list<Face*>::iterator i=m_FaceList.begin(); i!=m_FaceList.end(); ++i)
	{
		delete *i;
	}
}

float FaceBank::Identify(IplImage *face, unsigned int &ID, bool learn)
{
	IplImage *faceresized = cvCreateImage(cvSize(m_FaceWidth,m_FaceHeight),IPL_DEPTH_8U, face->nChannels);
	cvResize(face, faceresized, CV_INTER_LINEAR );
	// subtract the mean as an attempt to deal 
	// with global lighting changes
	SubMean(faceresized);

	// the first face found?
	if (m_FaceList.empty())
	{
		m_FaceList.push_back(new Face(faceresized));
		return 0;
	}
	
	float error=FLT_MAX;
	unsigned int best=0;
	Face* bestface=NULL;
	
	// look for the lowest error in the list of faces
	int c=0;
	for(list<Face*>::iterator i=m_FaceList.begin(); i!=m_FaceList.end(); ++i)
	{
		float tmp = Diff(faceresized,(*i)->m_Image);
		if (tmp<error)
		{
			error=tmp;
			best=c;
			bestface=*i;
		}
		c++;
	}
	
	// if the error is less than the threshold, return the id
	if (!learn || error<m_ErrorThresh) 
	{
		// blend this face into the one we have already
		bestface->Learn(faceresized,0.5);
		cvReleaseImage(&faceresized);
		ID=best;
		return error;
	}
	
	// we have a new face!
	// if there is no space, drop the oldest face
	// todo: drop least recently detected instead
	if (m_FaceList.size()>=m_MaxFaces)
	{
		delete *m_FaceList.begin();
		m_FaceList.pop_front();	
	}
		
	cerr<<"new face found"<<endl;
	m_FaceList.push_back(new Face(faceresized));
	return 1;
}
