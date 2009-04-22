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
#include "highgui.h"

#ifndef IMAGE_UTILS
#define IMAGE_UTILS

// I need to sort this out before it grows too much...

IplImage* SubImage(IplImage *image, CvRect roi);
void BlitImage(IplImage *srcimage, IplImage *dstimage, CvPoint pos);
void SubMean(IplImage *image);
float Diff(IplImage *imagea, IplImage *imageb);
void LBPImage(IplImage *srcimage, IplImage *dstimage);
unsigned int *HistMono8Bit(IplImage *image);

void DrawHistogram8(int x, int y, float scale, CvScalar colour, unsigned int *h, IplImage *img);

#endif
