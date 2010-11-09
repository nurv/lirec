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

#include "cv.h"
#include "Vector.h"
#include "Geometry.h"
#include <string>
#include <iostream>

#ifndef IMAGE
#define IMAGE

class Image
{
public:
	Image() : m_Image(NULL) {}
	Image(int w, int h, int d, int c);
	Image(const std::string &filename);
	Image(const Image &other);
	Image(const IplImage *other); // copies the given image
	Image(int w, int h, int c, const Vector<float> &v, float gain=1.0f);
	~Image();

	void Clear();

    float Get(int x, int y, int c) 
    {
        if (x>=0 && x<m_Image->width &&
            y>=0 && y<m_Image->height)
        {
            return cvGet2D(m_Image,y,x).val[c]; 
        }
        else
        {
            return 0;
        } 
    }

	Image operator-(const Image &other);
	Image operator+(const Image &other);
	Image &operator=(const Image &other);

	void PrintInfo();
	
	void Crop(int x, int y, int w, int h);
	Image Scale(int w, int h);
	Image SubImage(int x, int y, int w, int h);
	Image SubImage(Rect r);

	// Paste an image into this one
	void Blit(const Image &image, int x, int y);
	
	// Return a sum of squared differences, for giving a similarity metric 
	float SSD(Image &other);
	
	// Subtract the mean - this is useful for accounting for global lighting changes
	void SubMean();
	
	// Convert the image into a local binary patterns image
	void LBP();
	
	// Convert to different colour spaces
	Image GRAY2RGB();
	Image RGB2GRAY();
	Image BayerGB2RGB();
	
	// Calculate a histogram for a given channel
	unsigned int *Hist(int channel);
	
	Vector<float> ToFloatVector();
	unsigned int NumElements() { return m_Image->width*m_Image->height*m_Image->nChannels; }
	
	void Save(const std::string &filename);

    void GetBB(int thresh, int &minx, int &miny, int &maxx, int &maxy);
	
	IplImage *m_Image;
	
private:
	unsigned char SafeGet2D(int y, int x, int c);
};

#endif
