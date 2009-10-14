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

#define CV_NO_BACKWARD_COMPATIBILITY

#include "cv.h"
#include "highgui.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <math.h>
#include <float.h>
#include <limits.h>
#include <time.h>
#include <ctype.h>
#include <vector>

#ifdef _EiC
#define WIN32
#endif

#include "Image.h"
#include "Matrix.h"
#include "Vector.h"
#include "PCA.h"
#include "FileTools.h"

using namespace std;

static CvMemStorage* storage = 0;

void detect_and_draw( IplImage* image );

double scale = 1;

//int w=50;
//int h=80;
int w=20;
int h=30;

PCA pca(w*h);
Vector<float> params(100);
Image src("../data/images/faces/dave/dave.png");

void Generate(PCA &pca, const string &imagepath)
{
	vector<string> images=Glob(imagepath);
	for (vector<string>::iterator i=images.begin(); i!=images.end(); i++)
	{
		cerr<<*i<<endl;
		Image im(*i);
		Vector<float> v(im.Scale(w,h).RGB2GRAY().ToFloatVector());
		v-=v.Mean();
		pca.AddFeature(v);
	}	
	pca.Calculate(); 
}

PCA MakeSubspace(const PCA &space, const string &imagepath)
{
	PCA subspace(space.GetFeatureSize());
	
	// find the transform based on the parameters
	vector<string> images=Glob(imagepath);
	for (vector<string>::iterator i=images.begin(); i!=images.end(); i++)
	{
		cerr<<*i<<endl;
		Image im(*i);
		Vector<float> v(im.Scale(w,h).RGB2GRAY().ToFloatVector());
		v-=v.Mean();
		subspace.AddFeature(space.Project(v));
	}
	
	subspace.Calculate();

	// project back each row
	// think there must be a much much better way to do this...
	for (int i=0; i<subspace.EigenTransform().GetRows(); i++)
	{
		cerr<<"row: "<<i<<endl;
		Vector<float> row = subspace.EigenTransform().GetRowVector(i);
		subspace.EigenTransform().SetRowVector(i,
			pca.Synth(row));
	}

	return subspace; 
}

PCA LoadPCA(string filename)
{
	PCA pca(1);
	FILE *f=fopen(filename.c_str(), "rb");
	pca.Load(f);
	fclose(f);
	return pca;
}

void SavePCA(const PCA &pca, string filename)
{
	FILE *f=fopen(filename.c_str(), "wb");
	pca.Save(f);
	fclose(f);
}

void TestPCA()
{
	//Recalc();
	//FILE *f=fopen("davelight-20x30.pca", "wb");
	//pca.Save(f);
	//pca = LoadPCA("../no-redist/eigenspaces/spacek-50x80.pca");
	pca = LoadPCA("../no-redist/eigenspaces/yalefaces-expression-20x30.pca");
	//PCA subspace = LoadPCA("../data/eigenspaces/davelight-spacek-20x30.pca");
		
	//pca.EigenTransform() *= subspace.EigenTransform().Transposed();
	
	//PCA davesubspace = MakeSubspace(pca,"../data/images/faces/dave/*.png");
	//SavePCA(davesubspace,"davelight-spacek-20x30.pca");
		
	pca.Compress(0,100);
	
	src = src.Scale(w,h).RGB2GRAY();
	Vector<float> d(src.ToFloatVector());	
	params=pca.Project(d);	
	params[0]=1;

}



int main( int argc, char** argv )
{

	PCA::RunTests();
	TestPCA();

    CvCapture* capture = 0;
    IplImage *frame, *frame_copy = 0;
    IplImage *image = 0;
    const char* scale_opt = "--scale=";
    int scale_opt_len = (int)strlen(scale_opt);
    int i;
    const char* input_name = 0;

    for( i = 1; i < argc; i++ )
    {
        if( strncmp( argv[i], scale_opt, scale_opt_len ) == 0 )
        {
            if( !sscanf( argv[i] + scale_opt_len, "%lf", &scale ) || scale < 1 )
                scale = 1;
        }
        else if( argv[i][0] == '-' )
        {
            fprintf( stderr, "WARNING: Unknown option %s\n", argv[i] );
        }
        else
            input_name = argv[i];
    }

    storage = cvCreateMemStorage(0);

    if( !input_name || (isdigit(input_name[0]) && input_name[1] == '\0') )
        capture = cvCaptureFromCAM( !input_name ? 0 : input_name[0] - '0' );
    else if( input_name )
    {
        image = cvLoadImage( input_name, 1 );
        if( !image )
            capture = cvCaptureFromAVI( input_name );
    }
    else
        image = cvLoadImage( "lena.jpg", 1 );

    cvNamedWindow( "result", 1 );

    if( capture )
    {
        for(;;)
        {
            frame = cvQueryFrame( capture );
            if( !frame )
                break;
            if( !frame_copy )
                frame_copy = cvCreateImage( cvSize(frame->width,frame->height),
                                            IPL_DEPTH_8U, frame->nChannels );
            if( frame->origin == IPL_ORIGIN_TL )
                cvCopy( frame, frame_copy, 0 );
            else
                cvFlip( frame, frame_copy, 0 );

            detect_and_draw( frame_copy );

            if( cvWaitKey( 10 ) >= 0 )
                goto _cleanup_;
        }

        cvWaitKey(0);
_cleanup_:
        cvReleaseImage( &frame_copy );
        cvReleaseCapture( &capture );
    }
    else
    {
        if( image )
        {
            detect_and_draw( image );
            cvWaitKey(0);
            cvReleaseImage( &image );
        }
        else if( input_name )
        {
            /* assume it is a text file containing the
               list of the image filenames to be processed - one per line */
            FILE* f = fopen( input_name, "rt" );
            if( f )
            {
                char buf[1000+1];
                while( fgets( buf, 1000, f ) )
                {
                    int len = (int)strlen(buf), c;
                    while( len > 0 && isspace(buf[len-1]) )
                        len--;
                    buf[len] = '\0';
                    printf( "file %s\n", buf );
                    image = cvLoadImage( buf, 1 );
                    if( image )
                    {
                        detect_and_draw( image );
                        c = cvWaitKey(0);
                        if( c == 27 || c == 'q' || c == 'Q' )
                            break;
                        cvReleaseImage( &image );
                    }
                }
                fclose(f);
            }
        }
    }

    cvDestroyWindow("result");

    if (storage)
    {
        cvReleaseMemStorage(&storage);
    }

    return 0;
}
static CvScalar colors[] =
    {
        {{255,255,255}},
        {{0,128,255}},
        {{0,255,255}},
        {{0,255,0}},
        {{255,128,0}},
        {{255,255,0}},
        {{255,0,0}},
        {{255,0,255}}
    };
/*
void lpbhist(int x, int y, IplImage* img, IplImage* mainimg)
{
	IplImage* gray = cvCreateImage( cvSize(img->width,img->height), 8, 1 );
    cvCvtColor( img, gray, CV_BGR2GRAY );
    IplImage *lbp = cvCreateImage( cvSize(img->width,img->height), 8, 1 );
	LBPImage(gray, lbp);
	unsigned int *h=HistMono8Bit(lbp);
	BlitImage(lbp,mainimg,cvPoint(x,y));
	DrawHistogram8(x+img->width, y, -0.1, colors[0], h, lbp);
	delete[] h;
	cvReleaseImage( &lbp );
	cvReleaseImage( &gray );
}
*/
void detect_and_draw( IplImage* img )
{
	Image camera(img);
	CvFont font;
	cvInitFont( &font, CV_FONT_HERSHEY_PLAIN, 0.5, 0.5, 0, 1, CV_AA );

	//////////////////////////////////
	// Matrix tests
	//Matrix<float>::RunTests();

	//////////////////////////////////
	// test the debayering
	/*Image im("../data/images/bayer.pgm");
	im.Crop(300,300,320,240);
	im.RGB2GRAY();
	im.BayerGB2RGB();*/
	
	//////////////////////////////////
	// image differencing
	/*
	vector<Image> imagevec;
 	//imagevec.push_back(Image("../data/audrey.png"));
 	imagevec.push_back(Image("../data/dave-1.png"));
 	imagevec.push_back(Image("../data/dave-2.png"));
 	imagevec.push_back(Image("../data/amber-1.png"));
 	imagevec.push_back(Image("../data/amber-2.png"));
 	//imagevec.push_back(Image("../data/false.png"));

	for(unsigned int x=0; x<imagevec.size(); x++)
	{
		//cvSobel(imagevec[x].m_Image, imagevec[x].m_Image, 2, 2);
		//cvSmooth(imagevec[x].m_Image, imagevec[x].m_Image, CV_GAUSSIAN, 7);
		//imagevec[x].SubMean();
	}

	camera.Clear();

	for(unsigned int x=0; x<imagevec.size(); x++)
	{
		camera.Blit(imagevec[x],100+50*x,50);
	}

	for(unsigned int x=0; x<imagevec.size(); x++)
	{
		camera.Blit(imagevec[x],50, 100+50*x);
	}
	
	for(unsigned int x=0; x<imagevec.size(); x++)
	{
		for(unsigned int y=0; y<imagevec.size(); y++)
		{
			Image diff=imagevec[x]-imagevec[y];
			camera.Blit(diff,100+50*x,100+50*y);
			char s[32];
			sprintf(s,"%0.5f",1-imagevec[x].SSD(imagevec[y]));
			cvPutText(camera.m_Image, s, cvPoint(100+50*x,150+50*y), &font, colors[0]);		
		}
	}
	
	//camera.Blit(dave1,100,100);
	//camera.Blit(dave2,140,100);
	//camera.Blit(other,180,100);
	*/
	
	///////////////////////////////////
	// PCA display
	camera.Clear();

	//for (unsigned int i=0; i<pca.GetFeatures().size(); i++)
	//{
	//	camera.Blit(Image(30,40,1,pca.GetFeatures()[i]),(i%20)*32,(i/20)*42);
	//}
	
	static float t=0;

	for (unsigned int i=0; i<100; i++)
	{
		camera.Blit(Image(w,h,1,(pca.GetEigenTransform().GetRowVector(i)*100*sin(t))/((i+1) * 0.5)+pca.GetMean()
			),(i%12)*(w+2),0+(i/12)*(h+2));
		//camera.Blit(Image(w,h,1,pca.GetEigenTransform().GetRowVector(i)*5+pca.GetMean()
		//	),(i%10)*(w+2),0+(i/10)*(h+2));
	}
	
	t+=0.05;
	
	static int frame=0;
	char fn[256];
	snprintf(fn,256,"out-%06d.jpg",frame);
	cvSaveImage(fn,camera.m_Image);
	frame++;
	
	//camera.Blit(Image(w,h,1,pca.GetMean()),0,300);
	//camera.Blit(src,60,300);
	//camera.Blit(Image(w,h,1,pca.Synth(params)),120,300);

    cvShowImage("result", camera.m_Image);
 
}

