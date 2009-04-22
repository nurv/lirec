/*
** cam_display.c
** 
** Made by (Arne Caspari)
** Login   <arne@localhost>
** 
** Started on  Fri Oct 12 11:09:49 2007 Arne Caspari
*/

#include <cv.h>
#include <unicap.h>
#include <ucil.h>
#include <stdio.h>
#include <glib.h>

static volatile int quit = 0;

struct caminfo
{
      unicap_handle_t    handle;
      char              *device_identifier;
      unsigned int       fourcc;
      unicap_rect_t      format_size;
      char              *window;
      unicap_data_buffer_t buffer;
      IplImage          *image;
      int                frame_count;
      unicap_property_t *properties;
      int                property_count;
      CvHaarClassifierCascade *cascade;
      CvMemStorage            *storage;
};


//
// Define arrays of properties which should be set during
// initialisation here. The information could obtained from the
// "device_info" example. 
//
static unicap_property_t camera0_properties[] =
{
/*    { */
/*       identifier:      "Brightness",  */
/*       relations_count: 0, */
/*       { value:         16000.0 },  */
/*       flags:           UNICAP_FLAGS_MANUAL, */
/*       flags_mask:      UNICAP_FLAGS_MANUAL, */
/*    }, */
};

static unicap_property_t camera1_properties[] =
{
   {
      identifier:      "focus", 
      relations_count: 0,
      { value:         600.0 }, 
      flags:           UNICAP_FLAGS_MANUAL,
      flags_mask:      UNICAP_FLAGS_MANUAL,
   },
};

//
// Define the cameras that should be opened here. The information
// could be obtained from the "device_info" example
//
static struct caminfo cameras[] = 
{
   {
      handle:            NULL,
      device_identifier: "Imaging Source DFx 31AF03-Z 1940800048", 
      fourcc:            UCIL_FOURCC( 'Y', '8', '0', '0' ), 
      format_size:       { 0, 0, 1024, 768 }, 
      window:            "Camera 1",
      image:             NULL,
      properties:        camera0_properties,
      property_count:    sizeof( camera0_properties ) / sizeof( unicap_property_t ),

      cascade: NULL,
      storage: NULL,
   },
};
      
//
// Define the FourCC for the target buffer here. 
// For example: 
// Y800 for monochrome 8 bit images
// RGB3 for 24 bit RGB images
//
#define TARGET_FOURCC ( UCIL_FOURCC( 'Y', '8', '0', '0' ) )

//
// Define the BitsPerPixel for the target buffer here
// For example: 
// 8 for 8 bit monochrome images
// 24 for 24 bit RGB images
//
#define TARGET_BPP ( 8 )


#define CASCADE_NAME "../haarcascade_frontalface_default.xml"

void detect_and_draw( IplImage* img, CvHaarClassifierCascade *cascade, CvMemStorage *storage )
{
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

    double scale = 2;
    IplImage* small_img = cvCreateImage( cvSize( cvRound (img->width/scale),
						 cvRound (img->height/scale)),
					 8, 1 );
    int i;

    double t = (double)cvGetTickCount();

    cvResize( img, small_img, CV_INTER_LINEAR );
    cvEqualizeHist( img, img );
/*     cvPyrDown( img, small_img, CV_GAUSSIAN_5x5 ); */
    cvClearMemStorage( storage );

    if( cascade )
    {
        CvSeq* faces = cvHaarDetectObjects( small_img, cascade, storage,
                                            1.2, 2, CV_HAAR_DO_CANNY_PRUNING,
                                            cvSize(30, 30) );
        t = (double)cvGetTickCount() - t;
        printf( "detection time = %gms\n", t/((double)cvGetTickFrequency()*1000.) );
        for( i = 0; i < (faces ? faces->total : 0); i++ )
        {
            CvRect* r = (CvRect*)cvGetSeqElem( faces, i );
            CvPoint center;
            int radius;
            center.x = cvRound((r->x + r->width*0.5)*scale);
            center.y = cvRound((r->y + r->height*0.5)*scale);
            radius = cvRound((r->width + r->height)*0.25*scale);
            cvCircle( img, center, radius, colors[i%8], 3, 8, 0 );
        }
    }

    cvReleaseImage( &small_img );
}

//
// Implement your image processing function in this callback. 
//
// Currently this callback will only display an image through CV. 
// cvWaitKey( 5 ) is required to get the window updated. 
// The program will terminate when the callbacks for all cameras will
// have set their 'quit' condition.
//
static void new_frame_cb( unicap_event_t event, unicap_handle_t handle, unicap_data_buffer_t *buffer, struct caminfo *camera )
{

   ucil_convert_buffer( &camera->buffer, buffer );

   detect_and_draw( camera->image, camera->cascade, camera->storage );
   cvShowImage( camera->window, camera->image );

   if( cvWaitKey( 5 ) == 'q' )
   {
      quit++;
   }
   
}

//
//
//
int main( int argc, char **argv )
{
   int dev_count = sizeof( cameras ) / sizeof( struct caminfo );
   int res = 0;
   int i; 

   //
   // Important: You need to call g_thread_init since OpenCVs HighGUI
   // uses gtk/glib and we are calling it from a threaded environment
   //
   g_thread_init( NULL );

   for( i = 0; i < dev_count; i++ )
   {
      unicap_device_t device, device_spec;
      unicap_format_t format, format_spec;
      int j;
      
      unicap_void_device( &device_spec );
      strcpy( device_spec.identifier, cameras[i].device_identifier );
      if( !SUCCESS( unicap_enumerate_devices( &device_spec, &device, 0 ) ) )
      {
	 fprintf( stderr, "Could not find device: %s\n", device_spec.identifier );
	 exit( 1 );
      }
      
      if( !SUCCESS( unicap_open( &cameras[i].handle, &device ) ) )
      {
	 fprintf( stderr, "Failed to open device: %s\n", device.identifier );
	 exit( 1 );
      }
      
      unicap_void_format( &format_spec );
      format_spec.fourcc = cameras[i].fourcc;
      format_spec.size.width = cameras[i].format_size.width;
      format_spec.size.height = cameras[i].format_size.height;
      if( !SUCCESS( unicap_enumerate_formats( cameras[i].handle, &format_spec, &format, 0 ) ) )
      {
	 fprintf( stderr, "Could not find format! \n" );
	 exit( 1 );
      }
      
      format.buffer_type = UNICAP_BUFFER_TYPE_SYSTEM;
      format.size.width = cameras[i].format_size.width;
      format.size.height = cameras[i].format_size.height;      

      if( !SUCCESS( unicap_set_format( cameras[i].handle, &format ) ) )
      {
	 fprintf( stderr, "Failed to set format: %s \n", format.identifier );
	 exit( 1 );
      }

      // Read back format
      if( !SUCCESS( unicap_get_format( cameras[i].handle, &format ) ) )
      {
	 fprintf( stderr, "Failed to get format\n" );
	 exit( 1 );
      }

      unicap_copy_format( &cameras[i].buffer.format, &format );
      cameras[i].buffer.format.fourcc = TARGET_FOURCC; 
      cameras[i].buffer.format.bpp = TARGET_BPP;
      cameras[i].buffer.format.buffer_size = cameras[i].buffer.buffer_size = 
	 cameras[i].buffer.format.size.width * cameras[i].buffer.format.size.height * cameras[i].buffer.format.bpp / 8;
      cameras[i].buffer.data = malloc( cameras[i].buffer.format.buffer_size );
      cameras[i].image = cvCreateImage( cvSize( format.size.width, format.size.height ), 8, cameras[i].buffer.format.bpp / 8 );
      cameras[i].image->imageData = cameras[i].buffer.data;
      unicap_register_callback( cameras[i].handle, UNICAP_EVENT_NEW_FRAME, (unicap_callback_t) new_frame_cb, (void*)&cameras[i] );


      for( j = 0; j < cameras[i].property_count; j++ )
      {
	 unicap_property_t property, property_spec;
	 unicap_void_property( &property_spec );
	 strcpy( property_spec.identifier, cameras[i].properties[j].identifier );
	 unicap_enumerate_properties( cameras[i].handle, &property_spec, &property, 0 );

	 property.flags = cameras[i].properties[j].flags;
	 property.flags_mask = cameras[i].properties[j].flags_mask;
	 
	 switch( property.type )
	 {
	    case UNICAP_PROPERTY_TYPE_RANGE:
	    case UNICAP_PROPERTY_TYPE_VALUE_LIST:
	       property.value = cameras[i].properties[j].value;
	       break;
	       
	    case UNICAP_PROPERTY_TYPE_MENU:
	       strcpy( property.menu_item, cameras[i].properties[j].menu_item );
	       break;
	 }

	 unicap_set_property( cameras[i].handle, &property );
      }


      cameras[i].cascade = (CvHaarClassifierCascade*)cvLoad( CASCADE_NAME, 0, 0, 0 );
      if( !cameras[i].cascade )
      {
	 fprintf( stderr, "Failed to load cascade!\n" );
	 exit( 1 );
      }
      cameras[i].storage = cvCreateMemStorage(0);
      cvNamedWindow( cameras[i].window, 1 );
      
      if( !SUCCESS( unicap_start_capture( cameras[i].handle ) ) )
      {
	 fprintf( stderr, "Failed to start capture!\n" );
	 exit( 1 );
      }
   }


   // While this loop runs, the callbacks for each cameras will be
   // called in their own threads
   while( quit < dev_count )
   {
      usleep( 10000 );
   }

   for( i = 0; i < dev_count; i++ )
   {
      unicap_stop_capture( cameras[i].handle );
      unicap_close( cameras[i].handle );
   }
   
   cvDestroyAllWindows();

   return( 0 );
}

