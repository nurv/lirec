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

#include "Image.h"

#include <iostream>
#include "highgui.h"

using namespace std;

Image::Image(int w, int h, int d, int c)
{
	m_Image=cvCreateImage(cvSize(w, h), d, c);
}

Image::Image(const string &filename)
{
	m_Image=cvLoadImage(filename.c_str());
	if (m_Image==NULL) cerr<<"Could not open image: "<<filename<<endl;
	assert(m_Image);
cerr<<filename<<" "<<m_Image->nChannels<<endl;

}

Image::Image(const Image &other)
{
	m_Image=cvCloneImage(other.m_Image);
}

Image::Image(const IplImage *other)
{
	m_Image=cvCloneImage(other);
}

Image::Image(int w, int h, int c, const Vector<float> &v, float gain)
{
	m_Image=cvCreateImage(cvSize(w, h), 8, c);
	unsigned int pos=0;
	for(int y=0; y<m_Image->height; y++)
	{
        for(int x=0; x<m_Image->width; x++)
		{
			CvScalar s;
			for (int c=0; c<m_Image->nChannels; c++)
			{
				s.val[c]=(0.5+v[pos++]*gain)*127.0f;
			}
			cvSet2D(m_Image,y,x,s);
		}
	}
}

Image::~Image()
{
	cvReleaseImage(&m_Image);
}

void Image::Clear()
{
    for(int y=0; y<m_Image->height; y++)
	{
        for(int x=0; x<m_Image->width; x++)
		{
			CvScalar v;
			
 			for (int c=0; c<m_Image->nChannels; c++)
			{
				v.val[c]=0;
			}
						
			cvSet2D(m_Image,y,x,v);
		}
	}
}

Image Image::operator-(const Image &other)
{
	assert(other.m_Image->width == m_Image->width);
	assert(other.m_Image->height == m_Image->height);
	assert(other.m_Image->nChannels == m_Image->nChannels);

	Image ret(*this);

    for(int y=0; y<m_Image->height; y++)
	{
        for(int x=0; x<m_Image->width; x++)
		{
			CvScalar v;
			v.val[0]=0;v.val[1]=0;v.val[2]=0;v.val[3]=0;
 			for (int c=0; c<m_Image->nChannels; c++)
			{
				v.val[c]=abs((int)(cvGet2D(m_Image,y,x).val[c] - 
				                   cvGet2D(other.m_Image,y,x).val[c]));		
			}
			cvSet2D(ret.m_Image,y,x,v);
		}
	}
	return ret;
}

Image Image::operator+(const Image &other)
{
	assert(other.m_Image->width == m_Image->width);
	assert(other.m_Image->height == m_Image->height);
	assert(other.m_Image->nChannels == m_Image->nChannels);

	Image ret(*this);

    for(int y=0; y<m_Image->height; y++)
	{
        for(int x=0; x<m_Image->width; x++)
		{
			CvScalar v;
 			for (int c=0; c<m_Image->nChannels; c++)
			{
				v.val[c]=cvGet2D(m_Image,y,x).val[c] + 
				         cvGet2D(other.m_Image,y,x).val[c];		
			}
			cvSet2D(&ret,y,x,v);
		}
	}
	
	return ret;
}

Image &Image::operator=(const Image &other)
{
	if (m_Image!=NULL) cvReleaseImage(&m_Image);
	m_Image=cvCloneImage(other.m_Image);
	return *this;
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

Image Image::SubImage(Rect r)
{
	return SubImage(r.x, r.y, r.w, r.h);
}

Image Image::SubImage(int x, int y, int w, int h)
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
	cvResetImageROI(m_Image);
	Image ret(newimage);
    cvReleaseImage(&newimage);
    return ret;
}

Image Image::GRAY2RGB()
{
	IplImage *newimage = cvCreateImage(cvGetSize(m_Image), 8, 3);
    cvCvtColor(m_Image, newimage, CV_GRAY2RGB);
	Image ret(newimage);
    cvReleaseImage(&newimage);
    return ret;
}

Image Image::RGB2GRAY()
{
	IplImage *newimage = cvCreateImage(cvGetSize(m_Image), 8, 1);
    cvCvtColor(m_Image, newimage, CV_RGB2GRAY);
	Image ret(newimage);
    cvReleaseImage(&newimage);
    return ret;
}

Image Image::BayerGB2RGB()
{
	IplImage *newimage = cvCreateImage(cvGetSize(m_Image), 8, 3);
    cvCvtColor(m_Image, newimage, CV_BayerGB2RGB);
	Image ret(newimage);
    cvReleaseImage(&newimage);
    return ret;
}

Image Image::Scale(int w, int h)
{
	IplImage *newimage = cvCreateImage(cvSize(w,h), m_Image->depth, m_Image->nChannels);
	cvResize( m_Image, newimage, CV_INTER_LINEAR );
	Image ret(newimage);
    cvReleaseImage(&newimage);
    return ret;
}

void Image::Blit(const Image &image, int px, int py)
{	
	for(int y=0; y<image.m_Image->height; y++)
	{
        for(int x=0; x<image.m_Image->width; x++)
		{
			if (x+px>0 && x+px<m_Image->width &&
				y+py>0 && y+py<m_Image->height)
			{
            	if (image.m_Image->nChannels==1) 
				{
					CvScalar v;
					v.val[0]=cvGet2D(image.m_Image,y,x).val[0];
					v.val[1]=cvGet2D(image.m_Image,y,x).val[0];
					v.val[2]=cvGet2D(image.m_Image,y,x).val[0];
            		cvSet2D(m_Image,y+py,x+px,v);
				}
				else 
				{
					cvSet2D(m_Image,y+py,x+px,cvGet2D(image.m_Image,y,x));
				}
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
		
	CvScalar o;
	
	// now subtract it from each pixel
	for(int y=0; y<m_Image->height; y++)
	{
        for(int x=0; x<m_Image->width; x++)
		{			
			for (int c=0; c<m_Image->nChannels; c++)
			{
				// force the average to be 127
				o.val[c]=127+(cvGet2D(m_Image,y,x).val[c] - v.val[c]*256.0f);
			}
				
            cvSet2D(m_Image,y,x,o);
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

Vector<float> Image::ToFloatVector()
{
	Vector<float> v(m_Image->width*m_Image->height*m_Image->nChannels);
    // for each pixel
	unsigned int pos=0;
    for(int y=0; y<m_Image->height; y++)
	{
        for(int x=0; x<m_Image->width; x++)
		{
			for (int c=0; c<m_Image->nChannels; c++)
			{
				v[pos++]=cvGet2D(m_Image,y,x).val[c]/256.0f;
			}
		}
	}
	return v;
}

void Image::Save(const string &filename)
{
	cvSaveImage(filename.c_str(),m_Image);
}

void Image::GetBB(int thresh, int &minx, int &miny, int &maxx, int &maxy)
{
    minx=m_Image->width;
    miny=m_Image->height;
    maxx=0;
    maxy=0;

    for(int y=0; y<m_Image->height; y++)
	{
        for(int x=0; x<m_Image->width; x++)
		{
            if (cvGet2D(m_Image,y,x).val[0]>thresh)
            {
                if (x<minx) minx=x;           
                if (y<miny) miny=y;
                if (x>maxx) maxx=x;           
                if (y>maxy) maxy=y;
            }
        }
    }
}
