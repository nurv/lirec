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

#include "ImageUtils.h"

using namespace std;

/////////////////////////////////////////////////////////////
// deep copy a sub image

IplImage* SubImage(IplImage *image, CvRect roi)
{
	IplImage *result;
	cvSetImageROI(image,roi);
	result = cvCreateImage( cvSize(roi.width, roi.height), image->depth, image->nChannels );
	cvCopy(image,result);
	cvResetImageROI(image);
	return result;
}

/////////////////////////////////////////////////////////////
// subtract the mean (RGB)

void SubMean(IplImage *image)
{
	CvSize size = cvGetSize(image);

	float r=0;
	float g=0;
	float b=0;

	float s=size.width*size.height;

    for(int y=0; y<size.height; y++)
	{
        for(int x=0; x<size.width; x++)
		{
            r+=cvGet2D(image,y,x).val[0]/256.0f;
            g+=cvGet2D(image,y,x).val[1]/256.0f;
            b+=cvGet2D(image,y,x).val[2]/256.0f;
		}
	}
	
	r/=s;
	g/=s;
	b/=s;
		
	for(int y=0; y<size.height; y++)
	{
        for(int x=0; x<size.width; x++)
		{
            cvSet2D(image,y,x,cvScalar(127+(cvGet2D(image,y,x).val[0] - r*256.0f),
									   127+(cvGet2D(image,y,x).val[1] - g*256.0f),
									   127+(cvGet2D(image,y,x).val[2] - b*256.0f)));
		}
	}
}

/////////////////////////////////////////////////////////////
// return a diff metric between two images (works in RGB)

float Diff(IplImage *imagea, IplImage *imageb)
{
	CvSize sizea = cvGetSize(imagea);
	CvSize sizeb = cvGetSize(imageb);
	
	assert(sizea.width == sizeb.width);
	assert(sizea.height == sizeb.height);
	
	float ret=0;

    for(int y=0; y<sizea.height; y++)
	{
        for(int x=0; x<sizea.width; x++)
		{
            ret+=fabs((cvGet2D(imagea,y,x).val[0]/256.0f)-(cvGet2D(imageb,y,x).val[0]/256.0f));
            ret+=fabs((cvGet2D(imagea,y,x).val[1]/256.0f)-(cvGet2D(imageb,y,x).val[1]/256.0f));
            ret+=fabs((cvGet2D(imagea,y,x).val[2]/256.0f)-(cvGet2D(imageb,y,x).val[2]/256.0f));
		}
	}
	ret/=sizea.width*sizea.height*3;
	return ret;
}
