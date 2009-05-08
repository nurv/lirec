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
#include "highgui.h"

using namespace std;

Image::Image(int w, int h, int d, int c)
{
	cvCreateImage(cvSize(w, h), d, c);
}

Image::Image(const string &filename)
{
	m_Image=cvLoadImage(filename.c_str());
}

Image::Image(const Image *other)
{
	m_Image=cvCloneImage(other->m_Image);
}

Image::~Image()
{
	cvReleaseImage(&m_Image);
}

// safe accessor, which returns 0 if out of range
unsigned char Image::SafeGet2D(int y, int x, int c)
{
	if (x<0 || x>=m_Image->width || y<0 || y>=m_Image->height)
	{
		return 0;
	}
	
	return cvGet2D(m_Image,y,x).val[c];
}

void Image::PrintInfo()
{
	cerr<<m_Image->width<<"x"<<m_Image->height<<"x"<<m_Image->nChannels
		<<" @ "<<m_Image->depth<<" bpp"<<endl;
}

void Image::Crop(int x, int y, int w, int h)
{
	CvRect roi;
	roi.x=x;
	roi.y=y;
	roi.width=w;
	roi.height=h;
	IplImage *newimage;
	cvSetImageROI(m_Image,roi);
	newimage = cvCreateImage( cvSize(roi.width, roi.height), m_Image->depth, m_Image->nChannels );
	cvCopy(m_Image,newimage);
	cvReleaseImage(&m_Image);
	m_Image=newimage;
}

void Image::GRAY2RGB()
{
	IplImage *newimage = cvCreateImage(cvGetSize(m_Image), 8, 3);
    cvCvtColor(m_Image, newimage, CV_GRAY2RGB);
	cvReleaseImage(&m_Image);
	m_Image=newimage;
}

void Image::RGB2GRAY()
{
	IplImage *newimage = cvCreateImage(cvGetSize(m_Image), 8, 1);
    cvCvtColor(m_Image, newimage, CV_RGB2GRAY);
	cvReleaseImage(&m_Image);
	m_Image=newimage;
}

void Image::BayerGB2RGB()
{
	IplImage *newimage = cvCreateImage(cvGetSize(m_Image), 8, 3);
    cvCvtColor(m_Image, newimage, CV_BayerGB2RGB);
	cvReleaseImage(&m_Image);
	m_Image=newimage;
}

void Image::Scale(int w, int h)
{
	IplImage *newimage = cvCreateImage(cvSize(w,h), m_Image->depth, m_Image->nChannels);
	cvResize( m_Image, newimage, CV_INTER_LINEAR );
	cvReleaseImage(&m_Image);
	m_Image=newimage;
}

void Image::Blit(const Image &image, CvPoint pos)
{	
	for(int y=0; y<image.m_Image->height; y++)
	{
        for(int x=0; x<image.m_Image->width; x++)
		{
			if (x+pos.x>0 && x+pos.x<m_Image->width &&
				y+pos.y>0 && y+pos.y<m_Image->height)
			{
            	cvSet2D(m_Image,y+pos.y,x+pos.x,cvGet2D(image.m_Image,y,x));
			}
		}
	}
}

void Image::SubMean()
{	
	// get the mean of each channel
	CvScalar v;
	for (int c=0; c<m_Image->nChannels; c++)
	{
		v.val[c]=0;
	}

	float s=m_Image->width*m_Image->height;

    for(int y=0; y<m_Image->height; y++)
	{
        for(int x=0; x<m_Image->width; x++)
		{
 			for (int c=0; c<m_Image->nChannels; c++)
			{
				v.val[c]+=cvGet2D(m_Image,y,x).val[c]/256.0f;
			}
		}
	}
	
	for (int c=0; c<m_Image->nChannels; c++)
	{
		v.val[c]/=s;
	}
	
	// now subtract it from each pixel
	for(int y=0; y<m_Image->height; y++)
	{
        for(int x=0; x<m_Image->width; x++)
		{
			for (int c=0; c<m_Image->nChannels; c++)
			{
				// force the average to be 127
				v.val[c]=127+(cvGet2D(m_Image,y,x).val[c] - v.val[c]*256.0f);
			}
		
            cvSet2D(m_Image,y,x,v);
		}
	}
}

float Image::SSD(Image &other)
{
	assert(other.m_Image->width == m_Image->width);
	assert(other.m_Image->height == m_Image->height);
	
	float ret=0;
	float dif=0;
	
    for(int y=0; y<m_Image->height; y++)
	{
        for(int x=0; x<m_Image->width; x++)
		{
			for (int c=0; c<m_Image->nChannels; c++)
			{
				dif = (cvGet2D(m_Image,y,x).val[c]/256.0f) - 
				      (cvGet2D(other.m_Image,y,x).val[c]/256.0f);
            	ret+=dif*dif;
            }
		}
	}
	
	// not sure strictly whether the difference should be
	// invariant to the number of pixels, but it helps
	ret/=m_Image->width*m_Image->height*m_Image->nChannels;
	
	return ret;
}

void Image::LBP()
{
	IplImage *newimage;
	newimage = cvCreateImage(cvGetSize(m_Image), m_Image->depth, m_Image->nChannels);
		
    // for each pixel
    for(int y=0; y<m_Image->height; y++)
	{
        for(int x=0; x<m_Image->width; x++)
		{
			CvScalar sc;
			
			// for each channel
			for(int c=0; c<m_Image->nChannels; c++)
			{
				unsigned char v=0;
				unsigned char o=cvGet2D(m_Image,y,x).val[c];
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
							if (o>SafeGet2D(y+ky,x+kx,c))
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
			cvSet2D(newimage,y,x,sc);
		}
	}
	
	cvReleaseImage(&m_Image);
	m_Image=newimage;
}

unsigned int *Image::Hist(int channel)
{
	assert(channel<m_Image->nChannels);
	assert(m_Image->depth == 8);
	
	unsigned int *h=new unsigned int[256];

	for(int i=0; i<256; i++)
	{
		h[i]=0;
	}

    // for each pixel
    for(int y=0; y<m_Image->height; y++)
	{
        for(int x=0; x<m_Image->width; x++)
		{
			
			h[(unsigned char)cvGet2D(m_Image,y,x).val[channel]]++;
		}
	}
	
	return h;
}

