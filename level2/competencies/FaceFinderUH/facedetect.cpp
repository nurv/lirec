
/*
	Not a Very good face detector
	Taken from an example of face detection from open cv, it uses more patterns to match (increasting CPU useage) 
	but has a system of calculating where the patterns converge to estimate likelyhood of a face, can work to a side 
	face as ears and faces in profile have been addid , if more side face patterns are added then im reasonably sure it
	would be able to recognise the front,angle and side of face. 

	Has been used with YARP/SAMGAR etc, this will be changed shortly to co-inside with V3 samgar
	K Du Casse ,20th july 09

	ToDo : more patterns, more efficiency (this was a quick proto)

*/


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
	#include <list>
	#include <yarp/os/all.h>

	using namespace yarp::os;
	using namespace std;


	#ifdef _EiC
	#define WIN32
	#endif

	static CvMemStorage* storage = 0;
	static CvHaarClassifierCascade* cascade[13]; // we add alot more cascades and put them into an array
	static float BestScore[20];
	void detect_and_draw( IplImage* image );
	void SendData(int x);
	BufferedPort<Bottle> ctrlport;   

	double scale = 1;

	struct KStuff
	{
	int x,y,r;


	};

	using namespace std;


	int main( int argc, char** argv )
	{

		CvCapture* capture = 0;
		IplImage *frame, *frame_copy = 0;
		IplImage *image = 0;

		ctrlport.open("/CameraFace"); 
		Network::connect("/CameraFace"	,"/VirtualRobot","tcp",true);
		Network::connect("/VirtualRobot","/CameraFace"	,"tcp",true);


		/* Load up a load of patterns, each patten addid will increase reliablity but increase time taken to recognise a face */

		cascade[0] = (CvHaarClassifierCascade*)cvLoad("haarcascade_frontalface_alt.xml", 0, 0, 0 );
		cascade[1] = (CvHaarClassifierCascade*)cvLoad("haarcascade_eye.xml", 0, 0, 0 );
		cascade[2] = (CvHaarClassifierCascade*)cvLoad("haarcascade_eye_tree_eyeglasses.xml", 0, 0, 0 );
		cascade[3] = (CvHaarClassifierCascade*)cvLoad("haarcascade_frontalface_alt2.xml", 0, 0, 0 );
		cascade[4] = (CvHaarClassifierCascade*)cvLoad("haarcascade_frontalface_alt_tree.xml", 0, 0, 0 );
		cascade[5] = (CvHaarClassifierCascade*)cvLoad("haarcascade_frontalface_default.xml", 0, 0, 0 );
		cascade[6] = (CvHaarClassifierCascade*)cvLoad("haarcascade_profileface.xml", 0, 0, 0 );
		cascade[7] = (CvHaarClassifierCascade*)cvLoad("ojoD.xml", 0, 0, 0 );
		cascade[8] = (CvHaarClassifierCascade*)cvLoad("ojoI.xml", 0, 0, 0 );
		cascade[9] = (CvHaarClassifierCascade*)cvLoad("parojos.xml", 0, 0, 0 );
		cascade[10] = (CvHaarClassifierCascade*)cvLoad("parojosG.xml", 0, 0, 0 );
		cascade[11] = (CvHaarClassifierCascade*)cvLoad("Mouth.xml", 0, 0, 0 );
		cascade[12] = (CvHaarClassifierCascade*)cvLoad("Nariz.xml", 0, 0, 0 );


	  
		storage = cvCreateMemStorage(0);
		capture = cvCaptureFromCAM( 0);
		cvNamedWindow( "result", 1 );
		frame = cvRetrieveFrame( capture );
		frame_copy = cvCreateImage( cvSize(frame->width,frame->height),IPL_DEPTH_8U, frame->nChannels );

		
		/*Main loop basicly initiate, look for faces and escape */
		if( capture )
		{
			for(;;)
			{
				frame = cvRetrieveFrame( capture );
				if( !frame ) break;

				if( frame->origin == IPL_ORIGIN_TL ){  cvCopy( frame, frame_copy, 0 );}
				else								{	cvFlip( frame, frame_copy, 0 );}
	            
				detect_and_draw( frame_copy );

				if( cvWaitKey( 10 ) >= 0 ){  goto _cleanup_;}
			}

			cvWaitKey(0);
	_cleanup_:
			cvReleaseImage( &frame_copy );
			cvReleaseCapture( &capture );
		}
	  
	    
	   
		cvDestroyWindow("result");

		return 0;
	}

	void SendData( int x)
	{
		puts("sending data");
		Bottle& output = ctrlport.prepare();
		output.clear();
		output.addString("FaceLOC");
		output.addInt(x);
		ctrlport.write();
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
		int i,ii,count;
		int radius,DisScore;
		float PastBest;
		int TempScore;
		double Score;
		double tolerance =0;
		int totalpatternmatch=0;
		double xdis,ydis,totaldis;
		list<KStuff>::iterator it;
		list<KStuff>::iterator it2;
		 CvScalar color;
	//	int Maxsize;
		list <KStuff> ManyFaces;
		KStuff BestFace;
		KStuff Temp,Temp2;
		CvPoint center;

		char bufff[50];


		gray = cvCreateImage( cvSize(img->width,img->height), 8, 1 );
		small_img = cvCreateImage( cvSize( cvRound (img->width/scale),
							 cvRound (img->height/scale)), 8, 1 );

		cvCvtColor( img, gray, CV_BGR2GRAY );
		cvResize( gray, small_img, CV_INTER_LINEAR );
		cvEqualizeHist( small_img, small_img );
		cvClearMemStorage( storage );


		ManyFaces.erase(ManyFaces.begin(),ManyFaces.end()); // erase the whole list

	 /* for each of the patterns */
	  for(ii=0;ii<7;ii++)
	  {
			// get the faces for that pattern
			CvSeq* faces = cvHaarDetectObjects( small_img, cascade[ii], storage,1.1, 2, 0|/*CV_HAAR_FIND_BIGGEST_OBJECT|*/CV_HAAR_DO_CANNY_PRUNING,cvSize(30, 30) );
			// for each face 
			for( i = 0; i < (faces ? faces->total : 0); i++ ) // (faces ? faces->total : 0) think this is just the max so the colors mean nothing
			{
				
				CvRect* r = (CvRect*)cvGetSeqElem( faces, i );

				if(cvRound(r->x + (r->width/2))>0&&cvRound(r->x + (r->width/2))<img->width&&cvRound(r->y + (r->height/2))>0&&cvRound(r->y + (r->height/2))<img->height)
					{
						// get its location
						Temp.x=cvRound(r->x + (r->width/2));
						Temp.y=cvRound(r->y + (r->height/2));
						Temp.r=cvRound((r->width + r->height)*0.25);
						ManyFaces.push_front(Temp);
					}
			 }
	  }
	Score=0;
	BestScore[0]=5000000;
	DisScore=0;

	// we now have a list of all the faces for each pattern, 
	// this enables us to check each face against each other face,
	// they calculate the distance for each face, compared to each other face
	// then gets the total distance/number of faces, this gives the average distance
	// if this distance is low then it gets put in a placeholder for later
	  for ( it=ManyFaces.begin() ; it !=ManyFaces.end(); it++ )
	  {
		  Score=0;
		for ( it2=ManyFaces.begin() ; it2 !=ManyFaces.end(); it2++ )
		{
		Temp=*it;
		Temp2=*it2;
	    
		xdis=abs(Temp.x-Temp2.x)^2;
		ydis=abs(Temp.y-Temp2.y)^2;
		totaldis=sqrt(xdis+ydis);
		Score+=totaldis-ManyFaces.size();///(ManyFaces.size()^8); // have to divide it otherwise if only one pattern matched then would have the best score
		}
		center.x=Temp.x;
		center.y=Temp.y;
		if(Temp.x>0&&Temp.x<img->width&&Temp.y>0&&Temp.y<img->height&&Temp.r<100)
			{
			cvCircle(img, center, Temp.r, color, 3, 8, 0 );
			}
		if(Score<BestScore[0])
		{
		BestScore[0]=Score;
		BestFace.r=Temp.r;
		BestFace.x=Temp.x;
		BestFace.y=Temp.y;
		//sprintf(bufff,"Ave Best %d ",PastBest); // seems ok here
		//puts(bufff);
		}
	  }
		PastBest=0;

		// we know know the best distance, we make sure its not a invalid number
		// we then average this against the past 19 values, so we can make sure that this reading isn't a blip

		if(BestScore[0]!=5000000)
		{
		//	if(ManyFaces.size()<10){BestScore[0]+=50;}
		//	BestScore[0]/=ManyFaces.size()^3;
			for(i=19;i>0;i--)
				{
				BestScore[i]=BestScore[i-1];
				}
			for(i=0;i<19;i++)
				{
				PastBest+=BestScore[i];
				} 

		// we then take this number and divide it by 20 (the amount of patterns)
		// this gives us a nice number normally, plus when the patterns are far away from each other
		// but if there close then we have a minus number.
		PastBest/=20;

		center.x=BestFace.x;
		center.y=BestFace.y;
		sprintf(bufff,"Ave Best %f ",PastBest);
		//sprintf(bufff,"totalnum of faces %d ",ManyFaces.size());
		puts(bufff);
		}
		
		// if are number is less than zero, we draw a green circle on the pattern which is closest to all others.
		tolerance+=PastBest/100;
		if(BestFace.r>1 && PastBest<0+tolerance)
		{
		color=colors[3];
		cvCircle(img, center, BestFace.r, color, 3, 8, 0 );
		if(BestFace.x>(img->width/3)*2){SendData(-2);}
		else						   {SendData(2);}
		
		}
	

		cvShowImage( "result", img );
		cvReleaseImage( &gray );
		cvReleaseImage( &small_img );
	}
