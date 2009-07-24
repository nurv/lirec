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
#include "PCAClassifier.h"

//#define SAVE_FRAMES

using namespace std;

int w=20;
int h=30;

App::App(const string &filename) :
m_Capture(NULL),
m_Cascade(NULL),
m_Storage(NULL),
m_Classifier(NULL),
m_FaceBank(NULL),
m_FaceNum(1),
m_Learn(true),
frame(NULL),
frame_copy(NULL),
m_FrameNum(0)
{
	m_CtrlPort.open("/faceident-ctrl"); 
	m_Cascade = (CvHaarClassifierCascade*)cvLoad("haarcascade_frontalface_alt.xml", 0, 0, 0);
	assert(m_Cascade);
	m_Storage = cvCreateMemStorage(0);
	
	if (filename=="")
	{
		m_Capture = cvCaptureFromCAM(0);
	}
	else
	{
		m_Capture = cvCaptureFromAVI(filename.c_str());
	}
	
	assert(m_Capture);
	
	PCA pca(w*h);
	//FILE *f=fopen("../data/eigenspaces/spacek-50x80.pca", "rb");
	FILE *f=fopen("../data/eigenspaces/spacek-20x30.pca", "rb");
	pca.Load(f);
	fclose(f);
	pca.Compress(3,30);
	
	m_Classifier = new PCAClassifier(pca);
	m_FaceBank = new FaceBank(w,h,0.2,0.1,m_Classifier);
	cvInitFont( &m_Font, CV_FONT_HERSHEY_PLAIN, 0.5, 0.5, 0, 1, CV_AA );
    
	cvNamedWindow( "face classifier", 1 );
}

App::~App()
{
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

void App::Update()
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
	
	/////////////////////////
	
	Image camera(frame_copy);
	
	cvClearMemStorage(m_Storage);

	CvSeq* faces = cvHaarDetectObjects( camera.m_Image, m_Cascade, m_Storage,
			1.1, 2, 0
			//|CV_HAAR_FIND_BIGGEST_OBJECT
			//|CV_HAAR_DO_ROUGH_SEARCH
			//|CV_HAAR_DO_CANNY_PRUNING
			//|CV_HAAR_SCALE_IMAGE
			,
			cvSize(30, 30) );
		
	///////////////////////////////////
	// dispatch from input

	int key=cvWaitKey(10);
		
	switch (key)
	{
		case 'd': m_Learn=false; break;
		case '1': m_FaceNum=1; m_Learn=true; break;
		case '2': m_FaceNum=2; m_Learn=true; break;
		case '3': m_FaceNum=3; m_Learn=true; break;
		case '4': m_FaceNum=4; m_Learn=true; break;
		case '5': m_FaceNum=5; m_Learn=true; break;
		case '6': m_FaceNum=6; m_Learn=true; break;
		case '7': m_FaceNum=7; m_Learn=true; break;
		case '8': m_FaceNum=8; m_Learn=true; break;
		case '9': m_FaceNum=9; m_Learn=true; break;
		case '0': m_FaceNum=0; m_Learn=true; break;
		case 'c': m_FaceBank->Clear(); break;
	}
			
	for(int i = 0; i < (faces ? faces->total : 0); i++ )
	{
		CvRect* r = (CvRect*)cvGetSeqElem( faces, i );
		CvMat small_img_roi;

		unsigned int ID=999;
		int imagenum=-1;
		float confidence=0;
		// get the face area as a sub image
		Image face = camera.SubImage(r->x, r->y, r->width, r->height);
		
		//face.SubMean();
		//camera.Blit(face.Scale(w,h).RGB2GRAY(),100,100);
		
		// pass it into the face bank 
		if (m_Learn)
		{
			confidence=m_FaceBank->Suggest(face,m_FaceNum);
			ID=m_FaceNum;
		}
		else
		{	
			confidence=m_FaceBank->Identify(face,ID,imagenum);
		}				

		// if it's recognised the face (should really check the confidence)
		if (ID!=999)
		{
			char s[32];
			sprintf(s,"%d %0.2f",ID,confidence);
			cvPutText(camera.m_Image, s, cvPoint(r->x,r->y+r->height-5), &m_Font, colors[0]);

			if (!m_Learn)
			{
				m_SceneState.AddPresent(ID, SceneState::User(confidence));
			}
		}

		cvRectangle(camera.m_Image, cvPoint(r->x,r->y), cvPoint(r->x+r->width,r->y+r->height), colors[0]);
	}

	char info[256];
	if (m_Learn)
	{
		snprintf(info,256,"Learning user :%d",m_FaceNum);
		
		PCAClassifier *c = static_cast<PCAClassifier*>(m_FaceBank->GetClassifier());
		if (c->GroupExists(m_FaceNum))
		{
			Vector<float> p = c->GetGroupMean(m_FaceNum);
			cerr<<p.Magnitude()<<endl;
			Vector<float> r = c->GetPCA().Synth(p);
			camera.Blit(Image(w,h,1,r),0,100);
		}
	}
	else
	{
		snprintf(info,256,"Detecting users");
	}
	
	cvPutText(camera.m_Image, info, cvPoint(10,10), &m_Font, colors[0]);

	m_SceneState.Update();
	
    m_FrameNum++;
#ifdef SAVE_FRAMES
	char name[256];
	sprintf(name,"out-%0.4d.jpg",m_FrameNum);
	cerr<<"saving "<<name<<endl;
	cvSaveImage(name,camera.m_Image);
#endif

	cvShowImage("face classifier", camera.m_Image);

}

