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

#include <vector>

using namespace std;

/////////////////////////////////////////////////////////////////////////////////
// util for loading

int SplitString(const string& input, const string& delimiter, vector<string>& results, bool includeEmpties)
{
    int iPos = 0;
    int newPos = -1;
    int sizeS2 = (int)delimiter.size();
    int isize = (int)input.size();

    if(isize == 0 || sizeS2 == 0)
    {
        return 0;
    }

    vector<int> positions;
    newPos = input.find (delimiter, 0);

    if( newPos < 0 ) return 0; 

    int numFound = 0;

    while( newPos >= iPos )
    {
        numFound++;
        positions.push_back(newPos);
        iPos = newPos;
        newPos = input.find (delimiter, iPos+sizeS2);
    }

    if( numFound == 0 ) return 0;

    for( int i=0; i <= (int)positions.size(); ++i )
    {
        string s("");
        if( i == 0 ) 
        { 
            s = input.substr( i, positions[i] ); 
        }
		else
		{
        	int offset = positions[i-1] + sizeS2;
        	if( offset < isize )
        	{
        	    if( i == (int)positions.size() )
        	    {
        	        s = input.substr(offset);
        	    }
        	    else if( i > 0 )
        	    {
        	        s = input.substr( positions[i-1] + sizeS2, 
        	              positions[i] - positions[i-1] - sizeS2 );
        	    }
        	}
		}
		
        if( includeEmpties || ( s.size() > 0 ) )
        {
            results.push_back(s);
        }
    }
    return numFound;
}

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
	
FaceBank::FaceBank(unsigned int FaceWidth, unsigned int FaceHeight, float ErrorThresh) : 
m_FaceWidth(FaceWidth),
m_FaceHeight(FaceHeight),
m_ErrorThresh(ErrorThresh)
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

void FaceBank::Save(const std::string &filename) const
{
	char fn[256];
	snprintf(fn,256,"%s.dat",filename.c_str());
	FILE *f=fopen(fn,"w");
	
	if (f==NULL)
	{
		cerr<<"could not open file for saving: "<<filename<<endl;
		return;
	}

	char header[1024];
	snprintf(header,1024,"%d\n%d\n",0,m_FaceMap.size());
	fwrite(header,strlen(header),1,f);
	
	for(map<unsigned int,Face*>::const_iterator i=m_FaceMap.begin(); i!=m_FaceMap.end(); ++i)
	{
		char fn[256];
		snprintf(fn,256,"%d\n%s-%d.png\n",i->first,filename.c_str(),i->first);
		fwrite(fn,strlen(fn),1,f);
	}
	fclose(f);
	
	for(map<unsigned int,Face*>::const_iterator i=m_FaceMap.begin(); i!=m_FaceMap.end(); ++i)
	{
		char fn[256];
		snprintf(fn,256,"%s-%d.png",filename.c_str(),i->first);
		cvSaveImage(fn,i->second->m_Image);
	}
}

void FaceBank::Load(const std::string &filename)
{
	Clear();

	char fn[256];
	snprintf(fn,256,"%s.dat",filename.c_str());
	FILE *f=fopen(fn,"r");
	
	if (f==NULL)
	{
		cerr<<"file not found: "<<filename<<endl;
		return;
	}
	
	fseek(f,0,SEEK_END);
	unsigned int size=ftell(f);
	fseek(f,0,SEEK_SET);
	
	if (size==0)
	{
		fclose(f);
		cerr<<"empty file: "<<filename<<endl;
		return;
	}

	if (size<0)
	{
		fclose(f);
		cerr<<"error loading file: "<<filename<<" size: "<<size<<"??"<<endl;
		return;
	}

	char *data=new char[size+1];		
	fread(data,1,size,f);
	data[size]='\0';
	fclose(f);
	
	vector<string> items;
	SplitString(data,"\n",items,true);

	if (items.size()>3)
	{
		int version=(int)atoi(items[0].c_str());
		int count=(int)atoi(items[1].c_str());
		for (int i=2; i<count*2+2; i+=2)
		{
			int ID=(int)atoi(items[i].c_str());
			m_FaceMap[ID]=new Face(cvLoadImage(items[i+1].c_str()));
		}
	}
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
		if (Identify(faceresized,checkid)>0)
		{
			cerr<<"We've already seen this face: "<<checkid<<endl;	
			return 0;
		}

		cerr<<"new face "<<ID<<endl;
		m_FaceMap[ID]=new Face(faceresized);
		return 1;
	}
	
	// Does this face look like the one we already have for this id?
	float error = Diff(faceresized,i->second->m_Image);

	if (error<m_ErrorThresh) 
	{
		cerr<<"adding to face "<<ID<<endl;
		// Blend this face into the one we have already
		i->second->Learn(faceresized,0.2);
		cvReleaseImage(&faceresized);
		ID=i->first;
		return 1-error;
	}
	
	cerr<<"false positive? "<<error<<" "<<ID<<endl;
	
	return 0;
}

float FaceBank::Identify(IplImage *face, unsigned int &ID)
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
	
	// look for the lowest error in the map of faces
	for(map<unsigned int,Face*>::iterator i=m_FaceMap.begin(); i!=m_FaceMap.end(); ++i)
	{
		float tmp = Diff(faceresized,i->second->m_Image);
		if (tmp<error)
		{
			error=tmp;
			best=i->first;
			bestface=i->second;
		}
	}
	
	// if the error is less than the threshold, return the id
	if (error<m_ErrorThresh)
	{
		// blend this face into the one we have already
		bestface->Learn(faceresized,0);
		cvReleaseImage(&faceresized);
		ID=best;
		return 1-error;
	}
	
	cerr<<"unrecognised face"<<endl;
	
	return 0;
}
