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

        //int ncams = cvcamGetCamerasCount( );    //returns the number of available cameras in the system
        //int* out; int nselected = cvcamSelectCamera(&out);

		m_Capture = cvCaptureFromCAM(1);
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

char *spirits[]={"CanopySpirit","VerticalSpirit","CoverSpirit"};

void App::Update(Image &camera)
{	
	///////////////////////////////////
	// dispatch from input

	int key=cvWaitKey(10);

//    usleep(500);
	
	static int t=150;
    static bool viewthresh=false;
    static bool off=false;
    static int spirit=0;
    static int crop_x=0;
    static int crop_y=0;
    static int crop_w=camera.m_Image->width;
    static int crop_h=camera.m_Image->height;

	switch (key)
	{
    case 't': viewthresh=!viewthresh; break;
    case 'q': t--; break;
    case 'w': t++; break;
    case 'e': t-=20; break;
    case 'r': t+=20; break;
    case 'o': off=!off; break;
    case 'p': spirit++; break;
    case 'z': crop_x+=10; break;    
    case 'x': crop_x-=10; break;    
    case 'c': crop_y+=10; break;    
    case 'v': crop_y-=10; break;    
    case 'b': crop_w+=10; break;    
    case 'n': crop_w-=10; break;    
    case 'm': crop_h+=10; break;    
    case ',': crop_h-=10; break;    
	}			

    if (crop_x<0) crop_x=0;
    if (crop_x>=camera.m_Image->width) crop_x=camera.m_Image->width; 
    if (crop_y<0) crop_x=0;
    if (crop_y>=camera.m_Image->width) crop_x=camera.m_Image->width; 
    if (crop_w+crop_x>camera.m_Image->width)
    { 
        crop_w=camera.m_Image->width-crop_x;
    } 
    if (crop_h+crop_y>camera.m_Image->height)
    { 
        crop_h=camera.m_Image->height-crop_y;
    } 

    if (off) 
    {
        sleep(1);
        cerr<<"off..."<<endl;
        return;
    }

    Image thresh=camera.RGB2GRAY().SubImage(crop_x,crop_y,crop_w,crop_h);
    cvThreshold(thresh.m_Image,thresh.m_Image,t,255,CV_THRESH_BINARY);
    // copy the threshold into a colour image
    Image tofill=thresh.GRAY2RGB();
    cvFloodFill(tofill.m_Image,cvPoint(10,10), CV_RGB(0,255,0),cvScalar(0),cvScalar(255));
    
    CBlobResult blobs;
    blobs = CBlobResult( thresh.m_Image, NULL, 255 );
    // exclude the ones smaller than param2 value
    blobs.Filter( blobs, B_EXCLUDE, CBlobGetArea(), B_LESS, 100);

    CBlob *currentBlob;
    Image *out=NULL;

    if (key=='s')
    {
        // add the alpha channel
        Image src=camera.SubImage(crop_x,crop_y,crop_w,crop_h);
        out = new Image(src.m_Image->width,
                        src.m_Image->height, 8, 4);    
        
        for(int y=0; y<src.m_Image->height; y++)
        {

            for(int x=0; x<src.m_Image->width; x++)
            {
                CvScalar col = cvGet2D(src.m_Image,y,x);
                CvScalar alpha = cvGet2D(tofill.m_Image,y,x);
                if (alpha.val[0]==0 && 
                    alpha.val[1]==255 && 
                    alpha.val[2]==0)
                    col.val[3]=0;
                else
                    col.val[3]=255;
                cvSet2D(out->m_Image,y,x,col);
            }
        }   
    }

    if (key=='s')
    {
        cerr<<"deleting old images in islands/"<<endl;
        int r=system("rm islands/*");
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
            cvPutText(camera.m_Image, buf, cvPoint(crop_x+rect.x+rect.width/2,
                                                   crop_y+rect.y+rect.height/2), 
                      &m_Font, colors[0]);
            
            cvRectangle(camera.m_Image, 
                        cvPoint(crop_x+rect.x,crop_y+rect.y), 
                        cvPoint(crop_x+rect.x+rect.width,
                                crop_y+rect.y+rect.height), 
                        colors[1]);
        }
    }

    if (key=='s')
    {
        cerr<<"copying images to server"<<endl;
        //int r=system("scp -r islands garden@t0.fo.am:/home/garden/GerminationX/oak/");
        string path("/home/dave/code/lirec/scenarios/GerminationX/oak/public/");
        path+=string(spirits[spirit%3]);
        string command=string("rm ")+path+string("/*.*");
        int r=system(command.c_str());
        string command2=string("cp islands/* ")+path;
        r=system(command2.c_str());
        //cerr<<"finished copying...("<<r<<")"<<endl;
    }

    if (viewthresh) camera=tofill;

    char buf[256];
    sprintf(buf,"spirit: %s thresh: %d", spirits[spirit%3], t);
    cvPutText(camera.m_Image, buf, cvPoint(10,10), 
              &m_Font, colors[0]);

    cvRectangle(camera.m_Image, 
                cvPoint(crop_x,crop_y), 
                cvPoint(crop_x+crop_w,crop_y+crop_h), 
                colors[2]);

    if (out!=NULL) delete out;
}

