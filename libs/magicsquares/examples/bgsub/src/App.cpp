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

#include <assert.h>
#include "App.h"
#include "FileTools.h"
#include "PCA.h"

//#define SAVE_FRAMES

using namespace std;

//int w=50;
//int h=80;

int w=20;
int h=30;

App::App(const string &filename) :
m_Capture(NULL),
m_Frame(NULL),
m_FrameCopy(NULL),
m_FrameNum(0),
m_BG("bg.jpg")
{
	if (filename=="")
	{
		m_Capture = cvCaptureFromCAM(0);
	}
	else
	{
		m_Capture = cvCaptureFromAVI(filename.c_str());
	}
	
	assert(m_Capture);
	cvInitFont( &m_Font, CV_FONT_HERSHEY_PLAIN, 0.5, 0.5, 0, 1, CV_AA );
	cvInitFont( &m_LargeFont, CV_FONT_HERSHEY_PLAIN, 5, 5, 0, 10, CV_AA );    
	cvNamedWindow( "motion detection", 1 );
}

App::~App()
{
}

static CvScalar colors[] =
    {
        {{255,0,0}},
        {{0,0,0}},
        {{0,255,255}},
        {{0,255,0}},
        {{255,128,0}},
        {{255,255,0}},
        {{255,0,0}},
        {{255,0,255}},
    };

void App::Run()
{
	m_Frame = cvQueryFrame( m_Capture );
    if( !m_Frame ) 
	{
		cerr<<"no frame captured"<<endl;
		return;
	}
	
	if( !m_FrameCopy )
        m_FrameCopy = cvCreateImage( cvSize(m_Frame->width/2,m_Frame->height/2),
                                    IPL_DEPTH_8U, m_Frame->nChannels );
    if( m_Frame->origin == IPL_ORIGIN_TL )
    {
        cvResize(m_Frame, m_FrameCopy);
        //cvCopy(m_Frame, m_FrameCopy, 0);
    }
    else
    {
        cvFlip(m_Frame, m_FrameCopy, 0);
	}

	Image camera(m_FrameCopy);
	Update(camera);
	
	m_FrameNum++;
#ifdef SAVE_FRAMES
	char name[256];
	sprintf(name,"out-%0.4d.jpg",m_FrameNum);
	cerr<<"saving "<<name<<endl;
	cvSaveImage(name,camera.m_Image);
#endif

	cvShowImage("motion detection", camera.m_Image);
}

void App::Update(Image &camera)
{	
	int key=cvWaitKey(10);
    
    static int t=50;
    static bool ready=false;
	static bool bg=false;
    
    Image tmp(camera);
   
	switch (key)
	{
    case 'o': t++; break;
    case 'p': t--; break;
    case 'b': bg=!bg; break;
    case 'c': m_LastImage=tmp.RGB2GRAY(); ready=true; break;
        //case 'd': dir=!dir; break;
	}			

    Image thresh=camera.RGB2GRAY();
    if (bg) cvResize(m_BG.m_Image,camera.m_Image);
    else cvSet(camera.m_Image, cvScalar(0,0,0));
    //cerr<<m_BG.m_Image->width<<" "<<m_BG.m_Image->height<<endl;
    //cerr<<camera.m_Image->width<<" "<<camera.m_Image->height<<endl;

    if (ready) 
    {
        thresh=m_LastImage-thresh;
        for(int y=0; y<camera.m_Image->height; y++)
        {
            for(int x=0; x<camera.m_Image->width; x++)
            {
                if (cvGet2D(thresh.m_Image,y,x).val[0]>t)
                {
                    cvSet2D(camera.m_Image,y,x,cvGet2D(tmp.m_Image,y,x));
                }
            }
        }
    }
}

