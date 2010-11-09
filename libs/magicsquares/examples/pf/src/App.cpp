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
#include <RadarParticleFilter.h>

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

    m_PF.Initialise();
	m_PF.SetNoiseLevels(1);//,0.01,0.01);
	m_PF.SetResampleWeight(0.005);

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
		{{0,0,0}},
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

void PlotPoint(IplImage *Image, RadarParticleFilter::RadarState State, int colour,int size)
{
	int x = State.x*2 + 200;
	int y = State.y*2 + 200;
	cvRectangle(Image, cvPoint(x-size,y-size), cvPoint(x+size,y+size), colors[colour]);
}

void PlotRect(IplImage *Image, RadarParticleFilter::RadarState State, int colour)
{
	int x = State.x*2 + 200;
	int y = State.y*2 + 200;
	cvRectangle(Image, cvPoint(x-1,y-1), cvPoint(x+1,y+1), colors[colour]);	
}

void PlotXHairs(IplImage *Image, RadarParticleFilter::RadarState State, int colour)
{
	int x = State.x*2 + 200;
	int y = State.y*2 + 200;
	cvLine(Image, cvPoint(x,0), cvPoint(x,400), colors[colour]);
	cvLine(Image, cvPoint(0,y), cvPoint(400,y), colors[colour]);
}

void PlotRadar(IplImage *Image, RadarParticleFilter::RadarState State, int colour)
{
	int x = State.x*2 + 200;
	int y = State.y*2 + 200;
	cvLine(Image, cvPoint(200,200), cvPoint(x,y), colors[colour]);	
}

float avnoise=0;
float averror=0;

float avx = 0;
float avy = 0;

void App::Update(IplImage *camera)
{	
	int key=cvWaitKey();
	
	cvRectangle(camera, cvPoint(0,0), cvPoint(camera->width,camera->height), colors[8], -1);	
	
	m_PF.Predict();
	
	// Our actual state
	RadarParticleFilter::RadarState RealState;
	// We'll move the target on a sine wave - the particle state is not
	// able to model this behaviour very well, as it only copes with 
	// linear velocity
	RealState.x=50*sin(m_FrameNum*0.02f);
	RealState.y=-50;
	
	// Create an observation of the state
	RadarParticleFilter::RadarObservation *Obs = 
        static_cast<RadarParticleFilter::RadarObservation*>(RealState.Observe());
	// Add noise to the observation
	Obs->Angle+=GaussianNoise()*4;
	Obs->Dist+=GaussianNoise()*2;
	// Recalculate the state for this observation as we want to plot it
	RadarParticleFilter::RadarState ToPF;
	GetPos(Obs->Angle, Obs->Dist, ToPF.x, ToPF.y);
	
	
	const vector<RadarParticleFilter::Particle> &p = m_PF.GetParticles();
	for (vector<RadarParticleFilter::Particle>::const_iterator i=p.begin();
		i!=p.end(); ++i)
	{
		PlotPoint(camera,*((RadarParticleFilter::RadarState*)i->m_State),2,i->m_Weight*100);
	}
	
	// Feed the observation in and return the estimated state
	RadarParticleFilter::RadarState *Estimate = 
        (RadarParticleFilter::RadarState*)m_PF.Update(Obs);
	
	PlotRadar(camera,*Estimate,4);
	
	// Plot what is being sent to the filter
	PlotXHairs(camera,ToPF,3);
	
	// Plot the real state we are trying to find
	PlotXHairs(camera,RealState,1);

	float blend=0.9;	
	
	avx=(avx*blend)+(ToPF.x*(1-blend));
	avy=(avy*blend)+(ToPF.y*(1-blend));
	//cerr<<"average filter inputs: "<<Distance(avx, avy, RealState.x, RealState.y)<<endl;
	avnoise=(avnoise*blend)+(Distance(ToPF.x, ToPF.y, RealState.x, RealState.y)*(1-blend));
	averror=(averror*blend)+(Distance(Estimate->x, Estimate->y, RealState.x, RealState.y)*(1-blend));
	//cerr<<"average filter error: "<<averror<<endl;
	cerr<<"performance: "<<avnoise-averror<<endl;
	
    delete Estimate;
    delete Obs;
}
