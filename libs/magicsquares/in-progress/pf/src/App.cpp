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
#include <iostream>
#include "App.h"
#include "ParticleFilter.h"

//#define SAVE_FRAMES

using namespace std;

//int w=50;
//int h=80;

int w=20;
int h=30;

App::App(const string &filename) :
m_PF(500),
frame(NULL),
frame_copy(NULL),
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
	cvInitFont( &m_LargeFont, CV_FONT_HERSHEY_PLAIN, 25, 25, 0, 10, CV_AA );

	m_PF.SetNoiseLevels(1,0.01,0.01);
	m_PF.SetResampleWeight(0.0001);

	cvNamedWindow( "pf", 1 );
}

App::~App()
{
}

static CvScalar colors[] =
    {
        {{0,0,255}},
        {{0,128,255}},
        {{0,255,255}},
        {{0,255,0}},
        {{255,128,0}},
        {{255,255,0}},
        {{255,0,0}},
        {{255,0,255}},
		{{255,255,255}}
    };

void App::Run()
{
	frame = cvQueryFrame( m_Capture );
    if( !frame ) 
	{
		cerr<<"no frame captured"<<endl;
		return;
	}
	
	if( !frame_copy )
        frame_copy = cvCreateImage( cvSize(frame->width,frame->height),
                                    IPL_DEPTH_8U, frame->nChannels );
    if( frame->origin == IPL_ORIGIN_TL )
        cvCopy( frame, frame_copy, 0 );
    else
        cvFlip( frame, frame_copy, 0 );
	
	Update(frame_copy);
	
	m_FrameNum++;
#ifdef SAVE_FRAMES
	char name[256];
	sprintf(name,"out-%0.4d.jpg",m_FrameNum);
	cerr<<"saving "<<name<<endl;
	cvSaveImage(name,frame_copy);
#endif

	cvShowImage("pf", frame_copy);
}

void Plot(IplImage *Image, ParticleFilter::State State, int colour,int size)
{
	int x = State.x*2 + 200;
	int y = State.y*2 + 200;
	cvRectangle(Image, cvPoint(x-size,y-size), cvPoint(x+size,y+size), colors[colour]);	
}

void PlotReal(IplImage *Image, ParticleFilter::State State, int colour)
{
	int x = State.x*2 + 200;
	int y = State.y*2 + 200;
	cvRectangle(Image, cvPoint(x-1,y-1), cvPoint(x+1,y+1), colors[colour]);	
}

void PlotEst(IplImage *Image, ParticleFilter::State State, int colour)
{
	int x = State.x*2 + 200;
	int y = State.y*2 + 200;
	cvLine(Image, cvPoint(200,200), cvPoint(x,y), colors[colour]);	
}

void App::Update(IplImage *camera)
{	
	int key=cvWaitKey(10);
	
	cvRectangle(camera, cvPoint(0,0), cvPoint(camera->width,camera->height), colors[8], -1);	
	
	m_PF.Predict();
	
	// Our actual state
	ParticleFilter::State RealState;
	// We'll move the target on a sine wave - the particle state is not
	// able to model this behaviour very well, as it only copes with 
	// linear velocity
	RealState.x=50*sin(m_FrameNum*0.01f);
	RealState.y=-50;
	
	// Create an observation of the state
	ParticleFilter::Observation Obs = RealState.Observe();
	PlotReal(camera,RealState,1);
	
	// Feed the observation in and return the estimated state
	ParticleFilter::State Estimate = m_PF.Update(Obs);
	PlotEst(camera,Estimate,3);
	
	const vector<ParticleFilter::Particle> &p = m_PF.GetParticles();
	for (vector<ParticleFilter::Particle>::const_iterator i=p.begin();
		i!=p.end(); ++i)
	{
		Plot(camera,i->m_State,0,i->m_Weight*20);
	}
}
