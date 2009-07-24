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
#include "highgui.h"
#include "tinyxml.h"

#include <vector>

#ifdef WIN32
#include <string>
#define snprintf _snprintf 
#endif

using namespace std;

/////////////////////////////////////////////////////////////////////////////////
	
FaceBank::FaceBank(unsigned int FaceWidth, unsigned int FaceHeight, float ErrorThresh, float NewImageThresh, Classifier *c) : 
m_FaceWidth(FaceWidth),
m_FaceHeight(FaceHeight),
m_ErrorThresh(ErrorThresh),
m_NewImageThresh(NewImageThresh),
m_Classifier(c)
{
}

FaceBank::~FaceBank() 
{	
	Clear();
}

void FaceBank::Clear()
{
}

float FaceBank::Suggest(Image face, unsigned int ID)
{
	// Subtract the mean as an attempt to deal with global lighting changes
	//face.SubMean();
	
	m_Classifier->AddFeature(ID,face.Scale(m_FaceWidth,m_FaceHeight).RGB2GRAY().ToFloatVector());
	//m_FaceMap[ID]=face;
	
	return 1;
}

float FaceBank::Identify(Image face, unsigned int &ID, int &imagenum)
{
	// Subtract the mean as an attempt to deal with global lighting changes
	//face.SubMean();
	
	float error;
	ID = m_Classifier->Classify(face.Scale(m_FaceWidth,m_FaceHeight).RGB2GRAY().ToFloatVector(),error);

	// if the error is less than the threshold, return the id
	//if (error<m_ErrorThresh)
	{
		return 1-error;
	}
	
	cerr<<"unrecognised face"<<endl;
	ID=-1;
	return 0;
}

void FaceBank::Save(const std::string &filename) const
{
	
}

void FaceBank::Load(const std::string &filename)
{
	
}
