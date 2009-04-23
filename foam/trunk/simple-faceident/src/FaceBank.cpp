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
#include "highgui.h"
#include "tinyxml.h"

#include <vector>

#ifdef WIN32
#include <string>
#define snprintf _snprintf 
#endif

using namespace std;

/////////////////////////////////////////////////////////////////////////////////
	
FaceBank::FaceBank(unsigned int FaceWidth, unsigned int FaceHeight, float ErrorThresh) : 
m_FaceWidth(FaceWidth),
m_FaceHeight(FaceHeight),
m_ErrorThresh(ErrorThresh),
m_MultiFaceImages(false)
{
}

FaceBank::~FaceBank() 
{	
	Clear();
}

void FaceBank::Clear()
{
	for(map<unsigned int,Face*>::iterator i=m_FaceMap.begin(); i!=m_FaceMap.end(); ++i)
	{
		delete i->second;
	}
	m_FaceMap.clear();
}

float FaceBank::Suggest(IplImage *face, unsigned int ID)
{
	IplImage *faceresized = cvCreateImage(cvSize(m_FaceWidth,m_FaceHeight),IPL_DEPTH_8U, face->nChannels);
	cvResize(face, faceresized, CV_INTER_LINEAR );
	// Subtract the mean as an attempt to deal with global lighting changes
	SubMean(faceresized);
	
	// Get this face
	map<unsigned int,Face*>::iterator i=m_FaceMap.find(ID);
	
	// If it's the first time we've seen this face then record it
	if (i==m_FaceMap.end())
	{		
		// Check it doesn't look like any we have already recorded
		unsigned int checkid=0;
		int imagenum=-1;
		if (Identify(faceresized,checkid,imagenum)>0)
		{
			cerr<<"We've already seen this face: "<<checkid<<":"<<imagenum<<endl;	
			return 0;
		}

		cerr<<"new face "<<ID<<endl;
		m_FaceMap[ID]=new Face(faceresized);
		return 1;
	}
	
	// Does this face look like the one we already have for this id?
	int facenum;
	float error = i->second->FindSimilar(faceresized,facenum);

	if (error<m_ErrorThresh) 
	{
		//cerr<<"adding to face:"<<ID<<" image:"<<facenum<<endl;
		// Blend this face into the one we have already
		i->second->Learn(faceresized,0.2,facenum);
		cvReleaseImage(&faceresized);
		ID=i->first;
		return 1-error;
	}
	
	if (m_MultiFaceImages)
	{	
		// Does it look like any we have already recorded for any face?
		unsigned int checkid=0;
		int imagenum=-1;
		if (Identify(faceresized,checkid,imagenum)>0)
		{
			cerr<<"We've already seen this face: "<<checkid<<":"<<imagenum<<endl;	
			return 0;
		}	
		
		cerr<<"too different - adding new image to face "<<error<<" "<<ID<<endl;
		i->second->AddImage(faceresized);
	}
	
	return 0;
}

float FaceBank::Identify(IplImage *face, unsigned int &ID, int &imagenum)
{
	IplImage *faceresized = cvCreateImage(cvSize(m_FaceWidth,m_FaceHeight),IPL_DEPTH_8U, face->nChannels);
	cvResize(face, faceresized, CV_INTER_LINEAR );
	// Subtract the mean as an attempt to deal with global lighting changes
	SubMean(faceresized);

	// No faces recorded?
	if (m_FaceMap.empty())
	{
		return 0;
	}
	
	float error=FLT_MAX;
	unsigned int best=0;
	Face* bestface=NULL;
	imagenum=-1;
	
	// look for the lowest error in the map of faces
	for(map<unsigned int,Face*>::iterator i=m_FaceMap.begin(); i!=m_FaceMap.end(); ++i)
	{
		int similarfacenum;
		float tmp = i->second->FindSimilar(faceresized,similarfacenum);
		if (tmp<error)
		{
			error=tmp;
			best=i->first;
			bestface=i->second;
			imagenum=similarfacenum;
		}
	}
	
	// if the error is less than the threshold, return the id
	if (error<m_ErrorThresh)
	{
		// blend this face into the one we have already
		bestface->Learn(faceresized,0,imagenum);
		cvReleaseImage(&faceresized);
		ID=best;
		return 1-error;
	}
	
	cerr<<"unrecognised face"<<endl;
	
	return 0;
}

void FaceBank::Save(const std::string &filename) const
{
	char fn[256];
	snprintf(fn,256,"%s.xml",filename.c_str());
	FILE *f=fopen(fn,"w");
	
	if (f==NULL)
	{
		cerr<<"could not open file for saving: "<<filename<<endl;
		return;
	}
	
	TiXmlElement facebank("facebank");
	facebank.SetAttribute("version", 1);

	for(map<unsigned int,Face*>::const_iterator i=m_FaceMap.begin(); i!=m_FaceMap.end(); ++i)
	{
		TiXmlElement face("face");
		face.SetAttribute("id", i->first);
		int imagenum=0;
		
		for(vector<IplImage *>::iterator im=i->second->m_ImageVec.begin();
			im!=i->second->m_ImageVec.end(); im++)
		{
			TiXmlElement image("image");
			char fn[256];
			snprintf(fn,256,"%s-id%04d-image%04d.png",filename.c_str(),i->first,imagenum);
			
			cvSaveImage(fn,*im);
			
			image.SetAttribute("filename", fn);
			face.InsertEndChild(image);
			imagenum++;
		}
		
		facebank.InsertEndChild(face);
	}

	facebank.Print(f,0);
	fclose(f);
}

void FaceBank::Load(const std::string &filename)
{
	Clear();
	char fn[256];
	snprintf(fn,256,"%s.xml",filename.c_str());
	
	TiXmlDocument doc(fn);
	if (!doc.LoadFile())
	{
		cerr<<"could not load "<<fn<<" error:"<<doc.ErrorDesc()<<endl;
		return;
	}
	
	TiXmlNode* root = doc.FirstChild("facebank");
	if(root!=NULL)
	{
		// loop over faces
		TiXmlNode* face = root->FirstChild();
		while(face!=NULL)
		{	
			TiXmlElement* faceelem = face->ToElement();
			if(faceelem)
			{
				unsigned int ID = atoi(faceelem->Attribute("id"));
				int count=0;
				// loop over images
				TiXmlNode* image = face->FirstChild();
				while(image!=NULL)
				{
					TiXmlElement* imageelem = image->ToElement();
					if(imageelem)
					{
						string filename=imageelem->Attribute("filename");
						if(count==0) m_FaceMap[ID]=new Face(cvLoadImage(filename.c_str()));
						else m_FaceMap[ID]->AddImage(cvLoadImage(filename.c_str()));
						count++;
						image = image->NextSibling();
					}
				}
				face = face->NextSibling();
			}
		}
	}
	else
	{
		cerr<<"error parsing xml in "<<fn<<endl;
	}
}
