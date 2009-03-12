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

using namespace std;

#ifdef _EiC
#define WIN32
#endif

static CvMemStorage* storage = 0;
static CvHaarClassifierCascade* cascade = 0;
static CvHaarClassifierCascade* nested_cascade = 0;
int use_nested_cascade = 0;

void detect_and_draw( IplImage* image );

const char* cascade_name =
    "/usr/local/share/opencv/haarcascades/haarcascade_frontalface_alt.xml";
/*    "haarcascade_profileface.xml";*/
const char* nested_cascade_name =
    "/usr/local/share/opencv/haarcascades/haarcascade_eye_tree_eyeglasses.xml";
//    "../../data/haarcascades/haarcascade_eye.xml";
double scale = 1;

//////////////////////////////////////////////////////////
// These are the tweakable bits - see comments in FaceBank.h

const int nfaces = 3;
FaceBank facebank(nfaces, 30, 40, 0.05); 

// number of frames to detect new faces for
int calibrate_frames = 100;

// show all faces currently detected 
//#define SHOW_FACES

//#define SAVE_FRAMES

// globals
int frame=0;
bool learn=true;

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
        "Usage: facedetect [--cascade=\"<cascade_path>\"]\n"
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
	
	#ifdef SHOW_FACES
	char name[256];
	for (i=0; i<nfaces; i++)
	{
		sprintf(name,"face%i",i);
    	cvNamedWindow(name, 1);
	}
	#endif
	
    if( capture )
    {
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

            if( cvWaitKey( 10 ) >= 0 )
                goto _cleanup_;
        }

        cvWaitKey(0);
_cleanup_:
        cvReleaseImage( &frame_copy );
        cvReleaseCapture( &capture );
    }
    else
    {
        if( image )
        {
            detect_and_draw( image );
            cvWaitKey(0);
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
                        c = cvWaitKey(0);
                        if( c == 27 || c == 'q' || c == 'Q' )
                            break;
                        cvReleaseImage( &image );
                    }
                }
                fclose(f);
            }
        }
    }
    
    cvDestroyWindow("result");
	#ifdef SHOW_FACES
	for (i=0; i<nfaces; i++)
	{
		sprintf(name,"face%i",i);
    	cvDestroyWindow(name);
	}
	#endif

    return 0;
}


void detect_and_draw( IplImage* img )
{
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

    IplImage *gray, *small_img;
    int j;

    gray = cvCreateImage( cvSize(img->width,img->height), 8, 1 );
    small_img = cvCreateImage( cvSize( cvRound (img->width/scale),
                         cvRound (img->height/scale)), 8, 1 );

    cvCvtColor( img, gray, CV_BGR2GRAY );
    cvResize( gray, small_img, CV_INTER_LINEAR );
    cvEqualizeHist( small_img, small_img );
    cvClearMemStorage( storage );

	CvFont font;
    cvInitFont( &font, CV_FONT_HERSHEY_PLAIN, 2, 2, 0, 1, CV_AA );
	
    if( cascade )
    {
        double t = (double)cvGetTickCount();
        CvSeq* faces = cvHaarDetectObjects( small_img, cascade, storage,
                                            1.1, 2, 0
                                            //|CV_HAAR_FIND_BIGGEST_OBJECT
                                            //|CV_HAAR_DO_ROUGH_SEARCH
                                            |CV_HAAR_DO_CANNY_PRUNING
                                            //|CV_HAAR_SCALE_IMAGE
                                            ,
                                            cvSize(30, 30) );
        t = (double)cvGetTickCount() - t;
        //printf( "detection time = %gms\n", t/((double)cvGetTickFrequency()*1000.) );
		
		if (frame==calibrate_frames) 
		{
			learn=false;
			cerr<<"finished learning new faces"<<endl;
		}
		
		frame++;
		
        for(int i = 0; i < (faces ? faces->total : 0); i++ )
        {
            CvRect* r = (CvRect*)cvGetSeqElem( faces, i );
            CvMat small_img_roi;
			
			unsigned int ID=999;
			// get the face area as a sub image
			IplImage *face = SubImage(img, *r);
			// pass it into the face bank 
			facebank.Identify(face,ID,learn);
			cvReleaseImage(&face);
			
			// if it's recognised the face (should really check the confidence)
			if (ID!=999)
			{
				char s[32];
				sprintf(s,"%d",ID);
				cvPutText(img, s, cvPoint(r->x,r->y+25), &font, CV_RGB(1,1,1));
			}

            CvScalar color = colors[ID%8];
			cvRectangle(img, cvPoint(r->x,r->y), cvPoint(r->x+r->width,r->y+r->height), color);
        }
    }

    cvShowImage( "result", img );

	#ifdef SAVE_FRAMES
	char name[256];
	sprintf(name,"out-%0.4d.jpg",frame);
	cvSaveImage(name,img);
	#endif
	
	#ifdef SHOW_FACES
	char name[256];
	int i=0;
	for(list<Face*>::iterator ii=facebank.GetFaceList().begin(); 
		ii!=facebank.GetFaceList().end(); ++ii)
	{
		sprintf(name,"face%i",i);
    	cvShowImage(name,(*ii)->m_Image);
		i++;
	}
	#endif
    
	cvReleaseImage( &gray );
    cvReleaseImage( &small_img );
}
