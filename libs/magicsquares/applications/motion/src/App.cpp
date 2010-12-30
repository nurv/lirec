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
m_FrameNum(0)
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

	static int thresh=false;
    static int box=false;
    static int t=50;
    static int dir=false;
    static float dx=0;
    static float dy=0;
    static int cx=0;
    static int cy=0;
    
	switch (key)
	{
    case 't': thresh=!thresh; break;
    case 'b': box=!box; break;
    case 'o': t++; break;
    case 'p': t--; break;
        //case 'd': dir=!dir; break;
	}			

    camera=camera.RGB2GRAY();
    static bool first=true;

    Image tmp(camera);
    if (!first) camera=m_LastImage-camera;
    m_LastImage=tmp;
    first=false;

    if (thresh) cvThreshold(camera.m_Image,camera.m_Image,t,255,CV_THRESH_BINARY);
    if (box)
    {
        int minx,miny,maxx,maxy;
        minx=miny=maxx=maxy=0;
        camera.GetBB(t,minx,miny,maxx,maxy);
        cvRectangle(camera.m_Image,
                    cvPoint(minx,miny),
                    cvPoint(maxx,maxy), colors[0]);
        int tx=minx+(maxx-minx)/2;
        int ty=miny+(maxy-miny)/2;
        dx=(dx*0.0009)+(tx-cx)*0.0001;
        dy=(dy*0.0009)+(ty-cy)*0.0001;
        cx=tx;
        cy=ty;
    }
    if (dir)
    {
        printf("%f\n",dx);
        if (dx>0.01)
        {
            cvPutText(camera.m_Image, "right", cvPoint(10,60), &m_LargeFont, colors[0]);
        }
        else if(dx<-0.01)
        {
            cvPutText(camera.m_Image, "left", cvPoint(10,60), &m_LargeFont, colors[0]);
        }

        /*cvLine(camera.m_Image, 
               cvPoint(cx,cy),
               cvPoint(cx+dx,cy+dy), colors[0]);*/

    }

}

