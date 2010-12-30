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
    m_PF(500),
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

    m_PF.Initialise();
	m_PF.SetNoiseLevels(0.01);;
	m_PF.SetResampleWeight(0.002);

	cvNamedWindow( "ledpf", 1 );
}

App::~App()
{
}

static CvScalar colors[] =
    {
        {{0,0,255}},
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
        m_FrameCopy = cvCreateImage( cvSize(m_Frame->width,m_Frame->height),
                                    IPL_DEPTH_8U, m_Frame->nChannels );
    if( m_Frame->origin == IPL_ORIGIN_TL )
        cvCopy( m_Frame, m_FrameCopy, 0 );
    else
        cvFlip( m_Frame, m_FrameCopy, 0 );
	
	Image camera(m_FrameCopy);
	Update(camera);
	
	m_FrameNum++;
#ifdef SAVE_FRAMES
	char name[256];
	sprintf(name,"out-%0.4d.jpg",m_FrameNum);
	cerr<<"saving "<<name<<endl;
	cvSaveImage(name,camera.m_Image);
#endif

	cvShowImage("ledpf", camera.m_Image);
}


void PlotPoint(IplImage *Image, LEDParticleFilter::LEDState State, int colour,int size)
{
	int x = State.x;
	int y = State.y;
	cvRectangle(Image, cvPoint(x-size,y-size), cvPoint(x+size,y+size), colors[colour]);
}

void App::Update(Image &camera)
{	
	///////////////////////////////////
	// dispatch from input

	int key=cvWaitKey(10);
	
	int learn=-1;
	
	switch (key)
	{
		case 's': learn=0; break;
		case 'f': learn=1; break;
	}			

    // cerr<<camera.Get(10,10,0)<<endl;

    m_PF.Predict();
    LEDParticleFilter::LEDObservation Obs;    
    Image tmp(camera);
    Obs.m_Image=&tmp;

    const vector<LEDParticleFilter::Particle> &p = m_PF.GetParticles();
	for (vector<LEDParticleFilter::Particle>::const_iterator i=p.begin();
		i!=p.end(); ++i)
	{
        LEDParticleFilter::LEDState *s = (LEDParticleFilter::LEDState*)i->m_State;
		PlotPoint(camera.m_Image,*s,1,i->m_Weight*1000);
	}

    // Feed the observation in and return the estimated state
	LEDParticleFilter::LEDState *Estimate = 
        (LEDParticleFilter::LEDState*)m_PF.Update(&Obs);
	
    PlotPoint(camera.m_Image,*Estimate,0,10);

    delete Estimate; 
}
