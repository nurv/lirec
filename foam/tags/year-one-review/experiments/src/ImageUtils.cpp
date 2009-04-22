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

#include <iostream>
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
// safe accessor, which returns 0 if out of range

unsigned char SafeGet2D(IplImage *image, int y, int x, int c)
{
	CvSize size = cvGetSize(image);
	if (x<0 || x>=size.width || y<0 || y>=size.height)
	{
		return 0;
	}
	
	return cvGet2D(image,y,x).val[c];
}

/////////////////////////////////////////////////////////////
// paste an image over the top of another

void BlitImage(IplImage *srcimage, IplImage *dstimage, CvPoint pos)
{
	CvSize size = cvGetSize(srcimage);
	CvSize dstsize = cvGetSize(dstimage);
	
	for(int y=0; y<size.height; y++)
	{
        for(int x=0; x<size.width; x++)
		{
			if (x+pos.x>0 && x+pos.x<dstsize.width &&
				y+pos.y>0 && y+pos.y<dstsize.height)
			{
            	cvSet2D(dstimage,y+pos.y,x+pos.x,cvGet2D(srcimage,y,x));
			}
		}
	}
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



/////////////////////////////////////////////////////////////
// return a local binary patterns image of the src image


void LBPImage(IplImage *srcimage, IplImage *dstimage)
{
	CvSize srcsize = cvGetSize(srcimage);
	CvSize dstsize = cvGetSize(dstimage);
	
	assert(srcsize.width == dstsize.width);
	assert(srcsize.height == dstsize.height);
	assert(srcimage->nChannels == dstimage->nChannels);

    // for each pixel
    for(int y=0; y<srcsize.height; y++)
	{
        for(int x=0; x<srcsize.width; x++)
		{
			CvScalar sc;
			
			// for each channel
			for(int c=0; c<dstimage->nChannels; c++)
			{
				unsigned char v=0;
				unsigned char o=cvGet2D(srcimage,y,x).val[c];
				unsigned char b=0;

				// loop through a 3x3 kernel
				for (int kx=-1; kx<=1; kx++)
				{
					for (int ky=-1; ky<=1; ky++)
					{
						// don't compare with ourself
						if (!(kx==0 && ky==0))
						{
							// threshold
							if (o>SafeGet2D(srcimage,y+ky,x+kx,c))
							{
								// bit magic
								v|=(1<<b);
							}
							b++;
						}
					}
				}
				sc.val[c]=v;
			}
			cvSet2D(dstimage,y,x,sc);
		}
	}
}

/////////////////////////////////////////////////////////////
// calculate a histogram

unsigned int *HistMono8Bit(IplImage *image)
{
	assert(image->nChannels == 1);
	assert(image->depth == 8);
	CvSize size = cvGetSize(image);

	unsigned int *h=new unsigned int[256];

	for(int i=0; i<256; i++)
	{
		h[i]=0;
	}

    // for each pixel
    for(int y=0; y<size.height; y++)
	{
        for(int x=0; x<size.width; x++)
		{
			h[(unsigned char)cvGet2D(image,y,x).val[0]]++;
		}
	}
	
	return h;
}

/////////////////////////////////////////////////////////////
// draw a histogram

void DrawHistogram8(int x, int y, float scale, CvScalar colour, unsigned int *h, IplImage *img)
{
	for(int i=0; i<256; i++)
	{
		cvLine(img, cvPoint(x+i,y),cvPoint(x+i,y+h[i]*-scale), colour);	
	}
}
