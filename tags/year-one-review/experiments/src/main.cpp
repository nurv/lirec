#define CV_NO_BACKWARD_COMPATIBILITY

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

#ifdef _EiC
#define WIN32
#endif

#include "ImageUtils.h"

static CvMemStorage* storage = 0;

void detect_and_draw( IplImage* image );

double scale = 1;

int main( int argc, char** argv )
{
    CvCapture* capture = 0;
    IplImage *frame, *frame_copy = 0;
    IplImage *image = 0;
    const char* scale_opt = "--scale=";
    int scale_opt_len = (int)strlen(scale_opt);
    int i;
    const char* input_name = 0;

    for( i = 1; i < argc; i++ )
    {
        if( strncmp( argv[i], scale_opt, scale_opt_len ) == 0 )
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
        for(;;)
        {
            frame = cvQueryFrame( capture );
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
            /* assume it is a text file containing the
               list of the image filenames to be processed - one per line */
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

    if (storage)
    {
        cvReleaseMemStorage(&storage);
    }

    return 0;
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
	
void lpbhist(int x, int y, IplImage* img, IplImage* mainimg)
{
	IplImage* gray = cvCreateImage( cvSize(img->width,img->height), 8, 1 );
    cvCvtColor( img, gray, CV_BGR2GRAY );
    IplImage *lbp = cvCreateImage( cvSize(img->width,img->height), 8, 1 );
	LBPImage(gray, lbp);
	unsigned int *h=HistMono8Bit(lbp);
	BlitImage(lbp,mainimg,cvPoint(x,y));
	DrawHistogram8(x+img->width, y, -0.1, colors[0], h, lbp);
	delete[] h;
	cvReleaseImage( &lbp );
	cvReleaseImage( &gray );
}

void detect_and_draw( IplImage* img )
{
    IplImage *gray, *small_img;
    int i, j;

    gray = cvCreateImage( cvSize(img->width,img->height), 8, 1 );
    small_img = cvCreateImage( cvSize( cvRound (img->width/scale),
                         cvRound (img->height/scale)), 8, 1 );

    cvCvtColor( img, gray, CV_BGR2GRAY );
    cvResize( gray, small_img, CV_INTER_LINEAR );
    cvEqualizeHist( small_img, small_img );
    cvClearMemStorage( storage );
 
 	IplImage *li = cvLoadImage("test3-0.png");
	lpbhist(0, 0, li, gray);
	cvReleaseImage( &li );
 
 	li = cvLoadImage("test3-1.png");
	lpbhist(100, 0, li, gray);
	cvReleaseImage( &li );
 
 	li = cvLoadImage("test3-3.png");
	lpbhist(200, 0, li, gray);
	cvReleaseImage( &li );

    cvShowImage( "result", gray );
    cvReleaseImage( &gray );
    cvReleaseImage( &small_img );
}

