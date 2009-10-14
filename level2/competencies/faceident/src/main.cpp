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

// this is a hacked version of the opencv face detection sample code

#include "cv.h"
#include "highgui.h"
#include <yarp/os/all.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <math.h>
#include <float.h>
#include <limits.h>
#include <time.h>
#include <ctype.h>

#include "FaceBank.h"
#include "ImageUtils.h"
#include "SceneState.h"
#include "Image.h"
#include "FileTools.h"

using namespace std;
using namespace yarp::os;

#ifdef _EiC
#define WIN32
#endif

#ifdef WIN32
#include <string>
#define snprintf _snprintf 
#else
#include <unistd.h>
#endif

static CvMemStorage* storage = 0;
static CvHaarClassifierCascade* cascade = 0;
static CvHaarClassifierCascade* nested_cascade = 0;
int use_nested_cascade = 0;

void detect_and_draw( IplImage* image );

const char* cascade_name = "haarcascade_frontalface_alt.xml";
// "/usr/local/share/opencv/haarcascades/haarcascade_frontalface_alt.xml";
/*    "haarcascade_profileface.xml";*/
const char* nested_cascade_name = "haarcascade_eye_tree_eyeglasses.xml";
//"/usr/local/share/opencv/haarcascades/haarcascade_eye_tree_eyeglasses.xml";
//    "../../data/haarcascades/haarcascade_eye.xml";
double scale = 1;

//////////////////////////////////////////////////////////
// These are the tweakable bits - see comments in FaceBank.h
FaceBank facebank(80, 120, 0.2, 0.1); //0.2
SceneState scenestate;

// show all faces currently detected 
//#define SHOW_FACES
//#define SAVE_FRAMES

// globals
bool learn=false;
bool idle=false;
int facenum=0;
int framenum=0;

map<int,string> m_DebugNames;

BufferedPort<Bottle> ctrlport;   

void Benchmark(const string &test);

//////////////////////////////////////////////////////////

int main( int argc, char** argv )
{
	CvCapture* capture = 0;
	IplImage *frame, *frame_copy = 0;
	IplImage *image = 0;
	const char* scale_opt = "--scale=";
	int scale_opt_len = (int)strlen(scale_opt);
	const char* cascade_opt = "--cascade=";
	int cascade_opt_len = (int)strlen(cascade_opt);
	const char* nested_cascade_opt = "--nested-cascade";
	int nested_cascade_opt_len = (int)strlen(nested_cascade_opt);
	int i;
	const char* input_name = 0;

	/////////////////////////
	// yarp bit, would like to move this somewhere else

	ctrlport.open("/faceident-ctrl");     

	/////////////////////////

	//facebank.Load("a");

	for( i = 1; i < argc; i++ )
	{
		if( strncmp( argv[i], cascade_opt, cascade_opt_len) == 0 )
			cascade_name = argv[i] + cascade_opt_len;
		else if( strncmp( argv[i], nested_cascade_opt, nested_cascade_opt_len ) == 0 )
		{
			if( argv[i][nested_cascade_opt_len] == '=' )
				nested_cascade_name = argv[i] + nested_cascade_opt_len + 1;
			nested_cascade = (CvHaarClassifierCascade*)cvLoad( nested_cascade_name, 0, 0, 0 );
			if( !nested_cascade )
				fprintf( stderr, "WARNING: Could not load classifier cascade for nested objects\n" );
		}
		else if( strncmp( argv[i], scale_opt, scale_opt_len ) == 0 )
		{
			if( !sscanf( argv[i] + scale_opt_len, "%lf", &scale ) || scale < 1 )
				scale = 1;
		}
		else if( strncmp( argv[i], "--load=", 7 ) == 0 )
		{
			cerr<<"loading "<<argv[i]+7<<endl;
			facebank.Load(argv[i]+7);
		}
		else if( argv[i][0] == '-' )
		{
			fprintf( stderr, "WARNING: Unknown option %s\n", argv[i] );
		}
		else
			input_name = argv[i];
	}

	cascade = (CvHaarClassifierCascade*)cvLoad( cascade_name, 0, 0, 0 );

	if( !cascade )
	{
		fprintf( stderr, "ERROR: Could not load classifier cascade\n" );
		fprintf( stderr,
			"Usage: faceident [--cascade=\"<cascade_path>\"]\n"
			"   [--nested-cascade[=\"nested_cascade_path\"]]\n"
			"   [--scale[=<image scale>\n"
			"   [filename|camera_index]\n" );
		return -1;
	}
	storage = cvCreateMemStorage(0);

	if( !input_name || (isdigit(input_name[0]) && input_name[1] == '\0') )
		capture = cvCaptureFromCAM( !input_name ? 0 : input_name[0] - '0' );
	else if( input_name )
	{
		image = cvLoadImage( input_name, 1 );
		if( !image )
			capture = cvCaptureFromAVI( input_name );
	}
	else
		image = cvLoadImage( "lena.jpg", 1 );

	cvNamedWindow( "result", 1 );

	if( capture )
	{
	    Benchmark("yale");

	
	
		for(;;)
		{
			if( !cvGrabFrame( capture ))
				break;
			frame = cvRetrieveFrame( capture );
			if( !frame )
				break;
			if( !frame_copy )
				frame_copy = cvCreateImage( cvSize(frame->width,frame->height),
				IPL_DEPTH_8U, frame->nChannels );
			if( frame->origin == IPL_ORIGIN_TL )
				cvCopy( frame, frame_copy, 0 );
			else
				cvFlip( frame, frame_copy, 0 );

			detect_and_draw( frame_copy );
		}

_cleanup_:
		cvReleaseImage( &frame_copy );
		cvReleaseCapture( &capture );
	}
	else
	{
		if( image )
		{
			detect_and_draw( image );
			cvReleaseImage( &image );
		}
		else if( input_name )
		{
			// assume it is a text file containing the
			//   list of the image filenames to be processed - one per line 
			FILE* f = fopen( input_name, "rt" );
			if( f )
			{
				char buf[1000+1];
				while( fgets( buf, 1000, f ) )
				{
					int len = (int)strlen(buf), c;
					while( len > 0 && isspace(buf[len-1]) )
						len--;
					buf[len] = '\0';
					printf( "file %s\n", buf ); 
					image = cvLoadImage( buf, 1 );
					if( image )
					{
						detect_and_draw( image );                        
						cvReleaseImage( &image );
					}
				}
				fclose(f);
			}
		}
	}
	facebank.Save("faces");
	cvDestroyWindow("result");
	return 0;
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
		{{255,0,255}}
};



void Update( Image &img )
{

	CvSize imgsize = cvGetSize(img.m_Image);

	CvFont font;
	cvInitFont( &font, CV_FONT_HERSHEY_PLAIN, 0.75, 0.75, 0, 1, CV_AA );

	CvFont infofont;
	cvInitFont( &infofont, CV_FONT_HERSHEY_PLAIN, 1, 1, 0, 1, CV_AA );

	CvFont helpfont;
	cvInitFont( &helpfont, CV_FONT_HERSHEY_PLAIN, 0.5, 0.5, 0, 1, CV_AA );

	CvFont largefont;
	cvInitFont( &largefont, CV_FONT_HERSHEY_PLAIN, 25, 25, 0, 10, CV_AA );

	if( cascade )
	{
		double t = (double)cvGetTickCount();
		CvSeq* faces = cvHaarDetectObjects( img.m_Image, cascade, storage,
			1.1, 2, 0
			//|CV_HAAR_FIND_BIGGEST_OBJECT
			//|CV_HAAR_DO_ROUGH_SEARCH
			//|CV_HAAR_DO_CANNY_PRUNING
			//|CV_HAAR_SCALE_IMAGE
			,
			cvSize(30, 30) );
		t = (double)cvGetTickCount() - t;
		//printf( "detection time = %gms\n", t/((double)cvGetTickFrequency()*1000.) );

		/*framenum++;
		if (framenum==100) 
		{
		cerr<<"next face"<<endl;
		facenum++; 
		}

		if (framenum==220) 
		{
		cerr<<"stopped learning"<<endl;
		cerr<<facebank.GetFaceMap().size()<<" faces recorded"<<endl;
		learn=false;  
		}*/

		///////////////////////////////////
		// dispatch from input

		int key=cvWaitKey(10);

		switch (key)
		{
		case 'd': learn=false; break;
		case '1': facenum=1; learn=true; break;
		case '2': facenum=2; learn=true; break;
		case '3': facenum=3; learn=true; break;
		case '4': facenum=4; learn=true; break;
		case '5': facenum=5; learn=true; break;
		case '6': facenum=6; learn=true; break;
		case '7': facenum=7; learn=true; break;
		case '8': facenum=8; learn=true; break;
		case '9': facenum=9; learn=true; break;
		case '0': facenum=0; learn=true; break;
		case 'c': facebank.Clear(); break;
		}

		///////////////////////////////////
		// read from yarp

		Bottle *b=ctrlport.read(false);
		if (b!=NULL)
		{
			if (b->get(0).asString()=="train")
			{
				facenum=b->get(1).asInt();
				learn=true;
				idle=false;
			}
			if (b->get(0).asString()=="idle")
			{
				facenum=b->get(1).asInt();
				idle=true;
			}
			else if (b->get(0).asString()=="clear")
			{
				facebank.Clear();
			}
			else if (b->get(0).asString()=="detect")
			{
				learn=false;
				idle=false;
			}
			else if (b->get(0).asString()=="load")
			{
				facebank.Load(b->get(1).asString().c_str());
			}
			else if (b->get(0).asString()=="save")
			{
				facebank.Save(b->get(1).asString().c_str());
			}
			else if (b->get(0).asString()=="errorthresh")
			{
				facebank.SetErrorThresh(b->get(1).asDouble());
			}
			else if (b->get(0).asString()=="newimagethresh")
			{
				facebank.SetNewImageThresh(b->get(1).asDouble());
			}
		}

		///////////////////////////////////
		if (!idle)
		{
			for(int i = 0; i < (faces ? faces->total : 0); i++ )
			{
				CvRect* r = (CvRect*)cvGetSeqElem( faces, i );
				CvMat small_img_roi;

				unsigned int ID=999;
				int imagenum=-1;
				float confidence=0;
				// get the face area as a sub image
				IplImage *face = SubImage(img.m_Image, *r);
				// pass it into the face bank 
				if (learn)
				{
					confidence=facebank.Suggest(face,facenum);
					ID=facenum;
				}
				else
				{	
					confidence=facebank.Identify(face,ID,imagenum);
				}

				cvReleaseImage(&face);
				CvScalar color = colors[ID%8];

				// if it's recognised the face (should really check the confidence)
				if (ID!=999)
				{
					char s[32];
					sprintf(s,"%d",ID);
					cvPutText(img.m_Image, s, cvPoint(r->x,r->y+r->height-5), &largefont, color);
					int x=(facebank.GetFaceWidth()+8)*ID;
					int y=imgsize.height-facebank.GetFaceHeight();
					//y-=(facebank.GetFaceHeight()+2)*imagenum;
					y-=5*imagenum;
					
					cvLine(img.m_Image, cvPoint(r->x+r->width/2,r->y+r->height/2),
						cvPoint(x+facebank.GetFaceWidth()/2,y+facebank.GetFaceHeight()/2), color);

					cvRectangle(img.m_Image,cvPoint(x-1,y-1),cvPoint(x+facebank.GetFaceWidth(),y+facebank.GetFaceHeight()), color);

					if (imagenum>=0)
					{
						BlitImageAlpha(facebank.GetFaceMap()[ID]->m_ImageVec[imagenum],img.m_Image,cvPoint(r->x,r->y),0.75);
					}

					if (!learn)
					{
						scenestate.AddPresent(ID, SceneState::User(confidence));
					}
				}

				cvRectangle(img.m_Image, cvPoint(r->x,r->y), cvPoint(r->x+r->width,r->y+r->height), color);
			}
		}
		else
		{
			// idling, so free up some cpu
		#ifdef WIN32
			Sleep(2000);
		#else
			usleep(200000);
		#endif
		}
	}

	scenestate.Update();

	char info[256];
	if (learn)
	{
		snprintf(info,256,"learning user %d",facenum);
	}
	else
	{
		snprintf(info,256,"detecting faces");
	}
	cvPutText(img.m_Image, info, cvPoint(20,30), &infofont, CV_RGB(0,0,0));

	snprintf(info,256,"keys:");
	cvPutText(img.m_Image, info, cvPoint(20,50), &helpfont, CV_RGB(0,0,0));
	snprintf(info,256,"number key 0-9 : learn face");
	cvPutText(img.m_Image, info, cvPoint(20,60), &helpfont, CV_RGB(0,0,0));
	snprintf(info,256,"'d' : face detect mode");
	cvPutText(img.m_Image, info, cvPoint(20,70), &helpfont, CV_RGB(0,0,0));
	snprintf(info,256,"'c' : clear all faces");
	cvPutText(img.m_Image, info, cvPoint(20,80), &helpfont, CV_RGB(0,0,0));

#ifdef SHOW_FACES
	for(map<unsigned int,Face*>::iterator ii=facebank.GetFaceMap().begin(); 
		ii!=facebank.GetFaceMap().end(); ++ii)
	{
		int x=(facebank.GetFaceWidth()+8)*ii->first;
		int y=imgsize.height-facebank.GetFaceHeight();
		for(vector<IplImage *>::iterator im=ii->second->m_ImageVec.begin();
			im!=ii->second->m_ImageVec.end(); im++)
		{
			//BlitImage(*im,small_img,cvPoint(x,y));
			BlitImage(*im,img.m_Image,cvPoint(x,y));
			//y-=facebank.GetFaceHeight()+2;
			y-=5;
		}
	}
#endif
}

void detect_and_draw( IplImage* img )
{
	IplImage *small_img;
	int j;

	small_img = cvCreateImage( cvSize( cvRound (img->width/scale),
		cvRound (img->height/scale)), 8, 3 );
	CvSize imgsize = cvGetSize(small_img);
	cvResize( img, small_img, CV_INTER_LINEAR );
	cvClearMemStorage( storage );
	
	Image img2(small_img);
	Update(img2);

	cvShowImage( "result", img2.m_Image );
	framenum++;
#ifdef SAVE_FRAMES
	char name[256];
	sprintf(name,"out-%0.4d.jpg",framenum);
	cerr<<"saving "<<name<<endl;
	cvSaveImage(name,img2.m_Image);
#endif

	cvReleaseImage( &small_img );
}


void Benchmark(const string &test)
{
	cerr<<"Running benchmark test"<<endl;
	string path(string("../data/benchmark/")+test);
	vector<string> people=Glob(path+string("/training/*"));
	int ID=0;
	learn=true;
	
	for(vector<string>::iterator pi=people.begin(); pi!=people.end(); ++pi)
	{
		m_DebugNames[ID]=pi->substr(pi->find_last_of("/")+1,pi->length());
		vector<string> images=Glob(*pi+"/*.jpg");
		cerr<<*pi<<endl;
		for(vector<string>::iterator ii=images.begin(); ii!=images.end(); ++ii)
		{
			cerr<<ID<<" "<<*ii<<endl;
			facenum=ID;
			Image image(*ii);
			Update(image);
			string fn=*ii+"-out.png";
			cvSaveImage(fn.c_str(),image.m_Image);
		}
		ID++;
	}
	
	learn=false;
	
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
