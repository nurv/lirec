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
#include "FileTools.h"

//#define SAVE_FRAMES

using namespace std;

//int w=50;
//int h=80;

int w=20;
int h=30;

App::App(const string &filename) :
m_Capture(NULL),
m_Classifier(NULL),
m_FaceBank(NULL),
m_FaceNum(1),
m_Learn(true),
m_Idle(false),
frame(NULL),
frame_copy(NULL),
m_FrameNum(0)
{
	m_CtrlPort.open("/faceident-ctrl"); 
	
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
	//FILE *f=fopen("../no-redist/eigenspaces/spacek-50x80.pca", "rb");
	FILE *f=fopen("../../../libs/magicsquares/data/eigenspaces/spacek-20x30.pca", "rb");
	pca.Load(f);
	fclose(f);
	pca.Compress(10,500);
	
	m_Classifier = new PCAClassifier(pca);
	m_FaceBank = new FaceBank(w,h,0.4,0.1,m_Classifier);
	cvInitFont( &m_Font, CV_FONT_HERSHEY_PLAIN, 0.5, 0.5, 0, 1, CV_AA );
	cvInitFont( &m_LargeFont, CV_FONT_HERSHEY_PLAIN, 25, 25, 0, 10, CV_AA );
    
	cvNamedWindow( "face classifier", 1 );
	
	//Benchmark("yale");
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
	
	Image camera(frame_copy);
	Update(camera);
	
	m_FrameNum++;
#ifdef SAVE_FRAMES
	char name[256];
	sprintf(name,"out-%0.4d.jpg",m_FrameNum);
	cerr<<"saving "<<name<<endl;
	cvSaveImage(name,camera.m_Image);
#endif

	cvShowImage("face classifier", camera.m_Image);
}

void App::Update(Image &camera)
{	
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
		

	///////////////////////////////////
	// read from yarp

	Bottle *b=m_CtrlPort.read(false);
	if (b!=NULL)
	{
		if (b->get(0).asString()=="train")
		{
			m_FaceNum=b->get(1).asInt();
			m_Learn=true;
			m_Idle=false;
		}
		if (b->get(0).asString()=="idle")
		{
			m_FaceNum=b->get(1).asInt();
			m_Idle=true;
		}
		else if (b->get(0).asString()=="clear")
		{
			m_FaceBank->Clear();
		}
		else if (b->get(0).asString()=="detect")
		{
			m_Learn=false;
			m_Idle=false;
		}
		else if (b->get(0).asString()=="load")
		{
			m_FaceBank->Load(b->get(1).asString().c_str());
		}
		else if (b->get(0).asString()=="save")
		{
			m_FaceBank->Save(b->get(1).asString().c_str());
		}
		else if (b->get(0).asString()=="errorthresh")
		{
			m_FaceBank->SetErrorThresh(b->get(1).asDouble());
		}
	}

	if (m_Idle)
	{
			// idling, so free up some cpu
		#ifdef WIN32
			Sleep(2000);
		#else
			usleep(200000);
		#endif
	}
	else
	{
		vector<Rect> rects = m_FaceFinder.Find(camera,m_Learn);
		for(vector<Rect>::iterator i = rects.begin(); i!=rects.end(); i++ )
		{
			unsigned int ID=999;
			int imagenum=-1;
			float confidence=0;
			// get the face area as a sub image
			Image face = camera.SubImage(*i);

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
				map<int,string>::iterator d = m_DebugNames.find(ID);
				if (d!=m_DebugNames.end())
				{
					sprintf(s,"%s",d->second.c_str());
				}
				else
				{
					sprintf(s,"%d",ID);
				}

				cvPutText(camera.m_Image, s, cvPoint(i->x,i->y+i->h-5), &m_LargeFont, colors[ID]);

				if (!m_Learn)
				{
					m_SceneState.AddPresent(ID, SceneState::User(confidence));
				}
			}

			cvRectangle(camera.m_Image, cvPoint(i->x,i->y), cvPoint(i->x+i->w,i->y+i->h), colors[0]);
		}

		char info[256];
		if (m_Learn)
		{
			snprintf(info,256,"Learning user :%d",m_FaceNum);

			PCAClassifier *c = static_cast<PCAClassifier*>(m_FaceBank->GetClassifier());
			if (c->GroupExists(m_FaceNum))
			{
				Vector<float> p = c->GetGroupMean(m_FaceNum);
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
	}
	
}

void App::Benchmark(const string &test)
{
	cerr<<"Running benchmark test"<<endl;
	string path(string("../data/benchmark/")+test);
	vector<string> people=Glob(path+string("/training/*"));
	int ID=0;
	m_Learn=true;
	
	for(vector<string>::iterator pi=people.begin(); pi!=people.end(); ++pi)
	{
		m_DebugNames[ID]=pi->substr(pi->find_last_of("/")+1,pi->length());
		vector<string> images=Glob(*pi+"/*.jpg");
		for(vector<string>::iterator ii=images.begin(); ii!=images.end(); ++ii)
		{
			cerr<<ID<<" "<<*ii<<endl;
			m_FaceNum=ID;
			Image image(*ii);
			Update(image);
			//string fn=*ii+"-out.png";
			//cvSaveImage(fn.c_str(),image.m_Image);
		}
		ID++;
	}
	
	m_Learn=false;
	
	/*vector<string> images=Glob(path+string("/control/*.jpg"));
	for(vector<string>::iterator ti=images.begin(); ti!=images.end(); ++ti)
	{	
		cerr<<*ti<<endl;
		Image test(*ti);	
		Update(test);
		string fn=*ti+"-out.png";
		cvSaveImage(fn.c_str(),test.m_Image);
	}*/

	int imgw=1024;
	int imgh=768;
	Image out(imgw,imgh,8,3);
	int across=13;
	int down=13;
	int w=imgw/across;
	int h=imgh/down;

	int i=0;
	vector<string> images=Glob(path+string("/test/*.jpg"));
	for(vector<string>::iterator ti=images.begin(); ti!=images.end(); ++ti)
	{	
		cerr<<*ti<<endl;
		Image test(*ti);	
		Update(test);
		int x=i%across;
		int y=i/across;
		out.Blit(test.Scale(w,h),x*w,y*h);
		cerr<<x*w<<" "<<y*h<<endl;
		i++;
	}
	
	char fn[256];
	snprintf(fn,256,"%s/out.jpg",path.c_str());
	cvSaveImage(fn,out.m_Image);
}
