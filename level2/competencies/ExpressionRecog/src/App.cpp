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

#define SAVE_FRAMES

using namespace std;

//int w=50;
//int h=80;

int w=20;
int h=30;

App::App(const string &filename) :
m_Capture(NULL),
m_Frame(NULL),
m_FrameCopy(NULL),
m_ExtVec(NULL),
m_FrameNum(0)
{
	m_CtrlPort.open("/faceident-ctrl"); 

	m_Extremes[0]=NULL;	
	m_Extremes[1]=NULL;	
	
	if (filename=="")
	{
		m_Capture = cvCaptureFromCAM(0);
	}
	else
	{
		m_Capture = cvCaptureFromAVI(filename.c_str());
	}
	
	assert(m_Capture);
	
	m_PCA = new PCA(w*h);
	//FILE *f=fopen("../no-redist/eigenspaces/spacek-50x80.pca", "rb");
	FILE *f=fopen("../../../libs/magicsquares/data/eigenspaces/spacek-20x30.pca", "rb");
	m_PCA->Load(f);
	fclose(f);
	m_PCA->Compress(30,70);
	
	cvInitFont( &m_Font, CV_FONT_HERSHEY_PLAIN, 0.5, 0.5, 0, 1, CV_AA );
	cvInitFont( &m_LargeFont, CV_FONT_HERSHEY_PLAIN, 5, 5, 0, 10, CV_AA );
    
	cvNamedWindow( "expression recog", 1 );
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

	cvShowImage("expression recog", camera.m_Image);
}

void App::Update(Image &camera)
{	
	///////////////////////////////////
	// dispatch from input

	int key=cvWaitKey(10);
	
	int learn=-1;
	
	switch (key)
	{
		case 'h': learn=0; break;
		case 's': learn=1; break;
	}
			
	vector<Rect> rects = m_FaceFinder.Find(camera,true);
	for(vector<Rect>::iterator i = rects.begin(); i!=rects.end(); i++ )
	{
		// get the face area as a sub image
		Image face = camera.SubImage(*i).Scale(w,h).RGB2GRAY();
		face.SubMean();
		Vector<float> params = m_PCA->Project(face.ToFloatVector());
		
		if (learn!=-1) 
		{
			m_Extremes[learn] = new Vector<float>(params);
			if (m_Extremes[0]!=NULL && m_Extremes[1]!=NULL)
			{
				Vector<float> v=(*m_Extremes[1])-(*m_Extremes[0]);
				m_ExtVec = new Vector<float>(v);
			}
		}
		
		if (m_ExtVec!=NULL)
		{
			float d = m_ExtVec->Dot(params-(*m_Extremes[0]));
			 
			if (d>1.00)
			{
				cvPutText(camera.m_Image, "frown", cvPoint(15,200), &m_LargeFont, colors[1]);
			}
			
			cerr<<d<<endl;
			
			if (d<1.00)
			{
				cvPutText(camera.m_Image, "smile", cvPoint(15,200), &m_LargeFont, colors[1]);
			}
		}
		
		/*for (int n=0; n<params.Size(); ++n)
		{
			cvRectangle(camera.m_Image, cvPoint(n*15,50+params[n]*50), 
								        cvPoint(n*15+15,50), colors[1]);
			char info[256];	
			sprintf(info,"%d",n);
			cvPutText(camera.m_Image, info, cvPoint(n*15,60), &m_Font, colors[0]);
		}*/
		
		Image out(w,h,1,m_PCA->Synth(params*2));
		camera.Blit(out.Scale(40,60),i->x-30,i->y);
		
		// pass it into the face bank 
		
		//cvRectangle(camera.m_Image, cvPoint(i->x,i->y), cvPoint(i->x+i->w,i->y+i->h), colors[0]);
	}

	//char info[256];	
	//cvPutText(camera.m_Image, info, cvPoint(10,10), &m_Font, colors[0]);

}

