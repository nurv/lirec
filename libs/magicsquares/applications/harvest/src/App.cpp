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
#include "Blob.h"
#include "BlobResult.h"

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
m_SingleImage(false)
{
	if (filename=="")
	{
		m_Capture = cvCaptureFromCAM(0);
	}
	else
	{
        cerr<<"loading from "<<filename<<endl;
		//m_Capture = cvCaptureFromFile(filename.c_str());
        m_Frame = cvLoadImage(filename.c_str());
        m_SingleImage=true;
	}
	
	cvInitFont( &m_Font, CV_FONT_HERSHEY_PLAIN, 0.5, 0.5, 0, 1, CV_AA );
	cvInitFont( &m_LargeFont, CV_FONT_HERSHEY_PLAIN, 5, 5, 0, 10, CV_AA );    
	cvNamedWindow( "harvest", 1 );
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
    if( !m_SingleImage ) 
	{
        m_Frame = cvQueryFrame( m_Capture );
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

	cvShowImage("harvest", camera.m_Image);
}

void App::Update(Image &camera)
{	
	///////////////////////////////////
	// dispatch from input

	int key=cvWaitKey(10);
	
	static int t=200;
    bool viewthresh=false;

	switch (key)
	{
    case 'q': t--; viewthresh=true; break;
    case 'w': t++; viewthresh=true; break;
	}			

    Image thresh=camera.RGB2GRAY();
    cvThreshold(thresh.m_Image,thresh.m_Image,t,255,CV_THRESH_BINARY);
    //cvFloodFill(thresh.m_Image,cvPoint(10,10), cvScalar(255),cvScalar(0),cvScalar(255));
    
    CBlobResult blobs;
    blobs = CBlobResult( thresh.m_Image, NULL, 255 );
    // exclude the ones smaller than param2 value
    blobs.Filter( blobs, B_EXCLUDE, CBlobGetArea(), B_LESS, 10);

    if (viewthresh) camera=thresh;

    CBlob *currentBlob;
    Image *out=NULL;

    if (key=='s')
    {
        // add the alpha channel
        out = new Image(camera.m_Image->width,
                        camera.m_Image->height, 8, 4);    
        
        for(int y=0; y<camera.m_Image->height; y++)
        {

            for(int x=0; x<camera.m_Image->width; x++)
            {
                CvScalar col = cvGet2D(camera.m_Image,y,x);
                CvScalar alpha = cvGet2D(thresh.m_Image,y,x);
                col.val[3]=256-alpha.val[0];
                cvSet2D(out->m_Image,y,x,col);
            }
        }   
    }
    
    for (int i = 0; i < blobs.GetNumBlobs(); i++ )
    {
        currentBlob = blobs.GetBlob(i);
        //currentBlob->FillBlob( camera.m_Image, CV_RGB(255,0,0));

        CvRect rect = currentBlob->GetBoundingBox();

        if (key=='s')
        {
            Image island = out->SubImage(rect.x,rect.y,
                                         rect.width,rect.height);
            
            char buf[256];
            sprintf(buf,"islands/island-%d-%d-%d.png",i,
                    rect.x+rect.width/2,
                    rect.y+rect.height/2);
            cerr<<"saving "<<buf<<endl;
            island.Save(buf);
        }
        else
        {
            char buf[256];
            sprintf(buf,"%d",currentBlob->GetID());
            cvPutText(camera.m_Image, buf, cvPoint(rect.x,rect.y), &m_Font, colors[0]);
            
            cvRectangle(camera.m_Image, 
                        cvPoint(rect.x,rect.y), 
                        cvPoint(rect.x+rect.width,rect.y+rect.height), 
                        colors[1]);
        }
    }

    if (out!=NULL) delete out;
}

