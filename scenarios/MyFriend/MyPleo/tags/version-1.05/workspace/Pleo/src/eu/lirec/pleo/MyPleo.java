/** 
 * MyPleo.java - Android activity that launches the ShiVa module and is responsible for
 * 				 showing the splash screen.
 *  
 */

package eu.lirec.pleo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

class Globals
{
    public static String sPackageName = "eu.lirec.pleo";

    public static String sApplicationName = "MyPleo";

    public static boolean bUseGLES2 = false;

    public static boolean bForceDefaultOrientation = false;

}
//----------------------------------------------------------------------

public class MyPleo extends Activity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener
{
    //------------------------------------------------------------------
	public static final int MSG_START_ENGINE 		= 0 ;
	public static final int MSG_HIDE_SPLASH 		= 1 ;
	public static final int MSG_PLAY_OVERLAY_MOVIE 	= 2 ;
	public static final int MSG_OPEN_CAMERA_DEVICE	= 3 ;
	public static final int MSG_CLOSE_CAMERA_DEVICE	= 4 ;
	public static final int MSG_ENABLE_VIBRATOR 	= 5 ;

	public static final int SPLASH_DURATION_MILI_SECONDS = 500; 
	
    //------------------------------------------------------------------
    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        // Call parent constructor
        //
        super.onCreate  ( savedInstanceState ) ;

        // Get singleton
        //
        oThis = this ;

        // Print some infos about the device :
        //
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        Log.d ( Globals.sApplicationName, "Starting activity " + Globals.sApplicationName ) ;
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        Log.d ( Globals.sApplicationName, "Device infos :" ) ;
        Log.d ( Globals.sApplicationName, "    BOARD:         " + Build.BOARD ) ;
        Log.d ( Globals.sApplicationName, "    BRAND:         " + Build.BRAND ) ;
        Log.d ( Globals.sApplicationName, "    CPU_ABI:       " + Build.CPU_ABI ) ;
        Log.d ( Globals.sApplicationName, "    DEVICE:        " + Build.DEVICE ) ;
        Log.d ( Globals.sApplicationName, "    DISPLAY:       " + Build.DISPLAY ) ;
        Log.d ( Globals.sApplicationName, "    MANUFACTURER:  " + Build.MANUFACTURER ) ;
        Log.d ( Globals.sApplicationName, "    MODEL:         " + Build.MODEL ) ;
        Log.d ( Globals.sApplicationName, "    PRODUCT:       " + Build.PRODUCT ) ;
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;

        // Get APK file path
        //
        PackageManager  oPackageManager = getPackageManager ( ) ;
        try
        {
            ApplicationInfo oApplicationInfo = oPackageManager.getApplicationInfo ( Globals.sPackageName, 0 );
            mAPKFilePath  = oApplicationInfo . sourceDir ;
        }
        catch ( NameNotFoundException e ) { e.printStackTrace ( ) ; }

        // Request fullscreen mode
        //
        setFullscreen   ( ) ;
        setNoTitle      ( ) ;

		// Create the main view group and show startup screen
		//
		oSplashView 		= View.inflate ( this, R.layout.shiva, null ) ;
		oViewGroup 			= new RelativeLayout ( this ) ;
		oViewGroup.addView 	( oSplashView ) ;
        setContentView  	( oViewGroup ) ;

        // Send a delayed event to actually start loading and show the 3D View
        //
        Message msg     = new Message ( )  ;
        msg.what        = MSG_START_ENGINE ;
        msg.obj         = this ;
        oUIHandler  	.sendMessageDelayed ( msg, SPLASH_DURATION_MILI_SECONDS ) ;
    }
    
    //------------------------------------------------------------------
    public Handler oUIHandler = new Handler ( ) 
    {
        @Override
        public void handleMessage ( Message msg ) 
        {
            switch ( msg.what ) 
            {
			case MSG_START_ENGINE :
				{
	                // Create useful directories, extract packs and create 3DView
                    //
                    if ( ! createCacheDirectory ( ) ||
                         ! createHomeDirectory  ( ) ||
                         ! extractPacks         ( ) ||
                         ! extractXMLs          ( ) )
                    {
                        // FIXME: verify translations !!!
                        //
                        String    sLocale = Locale.getDefault  ( ).getLanguage ( ) ;
                        if      ( sLocale.contentEquals ( "fr" ) ) showError   ( "L'espace de stockage disponible est insuffisant pour lancer cette application. Veuillez en liberer et relancer l'application." ) ; // OK
                        else if ( sLocale.contentEquals ( "it" ) ) showError   ( "Spazio libero in memoria insufficiente per lanciare l'applicazione. Liberare pi\371 spazio e ripetere l'operazione." ) ; // OK
                        else if ( sLocale.contentEquals ( "es" ) ) showError   ( "Esta aplicaci\363n no puede comenzar debido al espacio de almacenamiento libre escaso. Libere por favor para arriba un cierto espacio y vuelva a efectuar la aplicaci\363n." ) ;
                        else if ( sLocale.contentEquals ( "de" ) ) showError   ( "Diese Anwendung kann wegen des unzul\344nglichen freien Speicherplatzes nicht beginnen. Geben Sie bitte oben etwas Platz frei und lassen Sie die Anwendung wieder laufen." ) ;
                        else                                       showError   ( "This application cannot start due to insufficient free storage space. Please free up some space and rerun the application." ) ; // OK
                    }
                    else
                    {
                        // Get useful system services
                        //
                        oVibrator           = (Vibrator)        getSystemService ( Context.VIBRATOR_SERVICE ) ;
                        oLocationManager    = (LocationManager) getSystemService ( Context.LOCATION_SERVICE ) ;
                        oSensorManager      = (SensorManager)   getSystemService ( Context.SENSOR_SERVICE ) ;
                        oPowerManager       = (PowerManager)    getSystemService ( Context.POWER_SERVICE ) ;

                        // Create the 3D view
                        //
                        o3DView             = new S3DSurfaceView ( (Context)msg.obj, mCacheDirPath, mHomeDirPath, mPackDirPath, Globals.bUseGLES2, Globals.bForceDefaultOrientation ) ;
    
                        if ( o3DView != null )
                        {									
							oViewGroup.addView ( o3DView ) ;//, 0 ) ;

                            // Enable motion sensors (FIXME : pas bo)
                            //
							Sensor oDefaultAccelerometerSensor = oSensorManager.getDefaultSensor ( Sensor.TYPE_ACCELEROMETER  ) ;
							Sensor oDefaultOrientationSensor   = oSensorManager.getDefaultSensor ( Sensor.TYPE_ORIENTATION    ) ;
							//Sensor oDefaultMagneticFieldSensor = oSensorManager.getDefaultSensor ( Sensor.TYPE_MAGNETIC_FIELD ) ;
							
                            if ( oDefaultAccelerometerSensor != null ) oSensorManager.registerListener ( o3DView, oDefaultAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME ) ;
                            if ( oDefaultOrientationSensor   != null ) oSensorManager.registerListener ( o3DView, oDefaultOrientationSensor,   SensorManager.SENSOR_DELAY_GAME ) ;
                            //if ( oDefaultMagneticFieldSensor != null ) oSensorManager.registerListener ( o3DView, oDefaultMagneticFieldSensor, SensorManager.SENSOR_DELAY_GAME ) ;
                            
                            // Enable wake lock
                            //
                            onEnableWakeLock ( true ) ;
                        }
                    }
				}
				break ;
				
            case MSG_HIDE_SPLASH :
                {
					if ( o3DView != null )
                	{
                        // Remove splash view
                        //
						oViewGroup.removeView ( oSplashView ) ;
					}
                }
                break ;

            case MSG_OPEN_CAMERA_DEVICE :
                {
					//onOpenCameraDevice ( ) ;
                }
                break ;

            case MSG_CLOSE_CAMERA_DEVICE :
                {
					//onCloseCameraDevice ( ) ;
                }
                break ;

			case MSG_PLAY_OVERLAY_MOVIE :
				{
					onPlayOverlayMovie ( (String)msg.obj ) ;
				}
				break ;

			case MSG_ENABLE_VIBRATOR :
				{
					onVibrate ( msg.arg1 > 0 ) ;
				}
				break ;
            }
            super.handleMessage ( msg ) ;
        }
    } ;
    
    //------------------------------------------------------------------
    @Override
    protected void onResume ( )
    {
        super.onResume ( ) ;
        
        if ( o3DView != null )
        {
            // Resume view
            //
            o3DView.onResume ( ) ;
        }
    }
    
    //------------------------------------------------------------------
    @Override
    protected void onPause ( ) 
    {
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        Log.d ( Globals.sApplicationName, "Paused activity " + Globals.sApplicationName ) ;
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        super.onPause ( ) ;

        // Disable motion sensors
        //
        if ( oSensorManager != null )
        {
            oSensorManager.unregisterListener ( o3DView ) ;
        }
        
		// Disable camera capture
		//
		//onCloseCameraDevice ( ) ;

        // Disable location updates
        //
        onEnableLocationUpdates ( false ) ;

        // Disable wake lock
        //
        onEnableWakeLock ( false ) ;

        if ( o3DView != null )
        {
            o3DView.onPause     ( ) ;
            o3DView.onTerminate ( ) ;
        }
    }
    
    //------------------------------------------------------------------
    protected void onStop ( ) 
    {
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        Log.d ( Globals.sApplicationName, "Stopped activity " + Globals.sApplicationName ) ;
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        super.onStop ( ) ;
        finish ( ) ;
    }
    
    //------------------------------------------------------------------
    protected void onDestroy ( ) 
    {
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        Log.d ( Globals.sApplicationName, "Finished activity " + Globals.sApplicationName ) ;
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        
        super.onDestroy ( ) ;
    }
    
    //------------------------------------------------------------------
    @Override
    public void onConfigurationChanged ( Configuration newConfig )
    {
        super.onConfigurationChanged ( newConfig ) ;
    }
    
    //------------------------------------------------------------------
    @Override
    public void onBackPressed ( )
    {
        // Nothing for now...
    }
    
    //------------------------------------------------------------------
    @Override
    public void onLowMemory ( )
    {
    
    }
    
    //------------------------------------------------------------------
    // OpenURL callback.
    //
	public static void onOpenURL ( String sURL, String sTarget )
	{
	    if ( oThis != null )
	    {
            Intent i            = new Intent ( Intent.ACTION_VIEW ) ;
		    i.setData		    ( Uri.parse  ( sURL ) ) ;
		    oThis.startActivity ( i ) ;
	    }
	}

    //------------------------------------------------------------------
    // Sound functions.
    //
    public static boolean onInitSound ( )
    {
        if ( oSoundPool == null )
        {
            oSoundPool = new SoundPool ( 32, AudioManager.STREAM_MUSIC, 0 ) ;
        }
        return ( oSoundPool != null ) ;
    }
    
    //------------------------------------------------------------------
    public static void onShutdownSound ( )
    {
        if ( oSoundPool != null )
        {
            oSoundPool.release ( ) ;
            oSoundPool = null ;
        }
    }
    
    //------------------------------------------------------------------
    public static int onLoadSound ( String sURI )
    {
        return oSoundPool.load ( sURI, 1 ) ;
        /*
        try
        {
            FileInputStream fis = new FileInputStream ( sURI ) ;
            return oSoundPool.load ( fis.getFD ( ), 0, fis.available ( ), 1 ) ;
        }
        catch ( IOException e ) { e.printStackTrace ( ) ; }
        return 0 ;
        */
    }
    
    //------------------------------------------------------------------
    public static void onUnloadSound ( int iSound )
    {
        oSoundPool.unload ( iSound ) ;
    }
    
    //------------------------------------------------------------------
    public static int onPlaySound ( int iSound, float fVolume, boolean bLoop, float fPriority )
    {
        return oSoundPool.play ( iSound, fVolume, fVolume, (int)(fPriority * 255.0f), bLoop ? -1 : 0, 1.0f ) ;
    }

    //------------------------------------------------------------------
    public static void onPauseSound ( int iStream )
    {
        oSoundPool.pause ( iStream ) ;
    }
    
    //------------------------------------------------------------------
    public static void onResumeSound ( int iStream )
    {
        oSoundPool.resume ( iStream ) ;
    }
    
    //------------------------------------------------------------------
    public static void onStopSound ( int iStream )
    {
        oSoundPool.stop ( iStream ) ;
    }
    
    //------------------------------------------------------------------
    public static void onSetSoundPitch ( int iStream, float fPitch )
    {
        oSoundPool.setRate ( iStream, fPitch ) ;
    }
    
    //------------------------------------------------------------------
    public static void onSetSoundLooping ( int iStream, boolean bLoop )
    {
        oSoundPool.setLoop ( iStream, bLoop ? -1 : 0 ) ;
    }
    
    //------------------------------------------------------------------
    public static void onSetSoundVolume ( int iStream, float fVolume )
    {
        oSoundPool.setVolume ( iStream, fVolume, fVolume ) ;
    }
    
    //------------------------------------------------------------------
    // Vibrator control
    //
    private static void onVibrate ( boolean b )
    {
        if ( b )
        {
            oVibrator.vibrate ( 10000 ) ;
        }
        else
        {
            oVibrator.cancel ( ) ;
        }
    }    
    
    //------------------------------------------------------------------
    // Wake lock control
    //
    public static void onEnableWakeLock ( boolean bEnable )
    {
        if ( bEnable )
        {
            if ( oPowerManager != null )
            {
                oWakeLock         = oPowerManager.newWakeLock ( PowerManager.SCREEN_DIM_WAKE_LOCK, "S3DEngineWakeLock" ) ;
                oWakeLock.acquire ( ) ;   
            }
        }
        else
        {
            if ( oWakeLock != null )
            {
                if ( oWakeLock.isHeld ( ) )
                {
                    oWakeLock.release ( ) ;
                }
            }
        }

		if ( o3DView != null )
		{
			o3DView.setKeepScreenOn ( bEnable ) ;
		}
    }    
    
    //------------------------------------------------------------------
    // Movie playback related methods
    //
    private static boolean onPlayOverlayMovie ( String sURI )
    {
		try 
		{
	        if ( oVideoView == null )        
	        {
	            oVideoView = new VideoView ( oThis ) ;    
                
	            if ( oVideoView != null )
	            {
					oVideoView.setOnPreparedListener	( oThis ) ;
					oVideoView.setOnErrorListener		( oThis ) ;
	                oVideoView.setOnCompletionListener 	( oThis ) ;
	
					RelativeLayout.LayoutParams oVideoViewLayoutParams = new RelativeLayout.LayoutParams ( RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT ) ;
					oVideoViewLayoutParams.addRule 	( RelativeLayout.CENTER_IN_PARENT ) ;
					oViewGroup.addView				( oVideoView, oVideoViewLayoutParams ) ;
					//o3DView.setVisibility 			( View.INVISIBLE ) ; // Kills the rendering context, play with ZOrder instead
		            oVideoView.setVideoURI  		( Uri.parse ( sURI ) ) ;
					oVideoView.setMediaController 	( new MediaController ( oThis ) ) ;
				   	oVideoView.requestFocus 		( ) ;
		            oVideoView.start 		        ( ) ;
					oVideoView.setZOrderMediaOverlay( true ) ;
                    if ( ! sURI.contains ( ".mp3" ) )
                    {
	 					// TODO: backup the current orientation
                        oThis.setRequestedOrientation ( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ) ;
                    }		
		            return oVideoView.isPlaying 	( ) ;
				}
	        }
		}
		catch ( Exception e )
		{
			Log.d ( Globals.sApplicationName, "onPlayOverlayMovie: " + e.getMessage ( ), e ) ;
			
			onStopOverlayMovie ( ) ;
		}

        return false ;
    }   

    //------------------------------------------------------------------
    private static void onStopOverlayMovie ( )
    {
        if ( oVideoView != null )
        {
            oVideoView.stopPlayback 		( ) ;
			oViewGroup.removeView   		( oVideoView ) ;
			oVideoView 						= null ;
			//o3DView.setVisibility 			( View.VISIBLE ) ;
			o3DView.onOverlayMovieStopped	( ) ;
        }
		oThis.setRequestedOrientation ( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ) ; // TODO: restore the original orientation
    }   

    //------------------------------------------------------------------
    public void onPrepared ( MediaPlayer mp )
    {
        
    } 
    
    //------------------------------------------------------------------
    public void onCompletion ( MediaPlayer mp )
    {
        onStopOverlayMovie ( ) ;
    } 
    
    //------------------------------------------------------------------
    public boolean onError ( MediaPlayer mp, int what, int extra )
    {
		return false ;
    } 
    
    //------------------------------------------------------------------
    // Camera
    //
	/*
    public static boolean onOpenCameraDevice ( )
    {
        if ( oCamera == null ) 
		{
        	oCameraPreview = new CameraPreview ( oThis ) ;
			
			if ( oCameraPreview != null )
			{
        		oCamera = Camera.open ( ) ;

				if ( oCamera != null )
				{
					oCameraPreview.setCamera ( oCamera ) ;
					oViewGroup.addView 		 ( oCameraPreview ) ;
					return true ;
				}
			}
		}
		return false ;
	}
	
    //------------------------------------------------------------------
    public static void onCloseCameraDevice ( )
    {
        if ( oCamera != null ) 
		{
            oCameraPreview.setCamera ( null ) ;
            oCamera.release ( ) ;
            oCamera = null ;
        }
	}
	*/
    //------------------------------------------------------------------
    // Location
    //
    public static boolean areLocationUpdatesSupported ( )
    {
        // ??? 
        //
        return oLocationManager.isProviderEnabled ( LocationManager.GPS_PROVIDER     ) ||
               oLocationManager.isProviderEnabled ( LocationManager.NETWORK_PROVIDER ) ;
    }
    
    //------------------------------------------------------------------
    public static boolean onEnableLocationUpdates ( boolean bEnable )
    {
        if ( ( oLocationManager != null ) && ( o3DView != null ) )
        {
            if ( bEnable )
            {
                boolean bAtLeastOneProviderEnabled = false ;
                
                if ( oLocationManager.isProviderEnabled ( LocationManager.NETWORK_PROVIDER ) )
                {
                    Log.d ( Globals.sApplicationName, "Coarse location sensor available" ) ;
                    oLocationManager.requestLocationUpdates ( LocationManager.NETWORK_PROVIDER, 0, 0, o3DView, Looper.getMainLooper ( ) ) ;                    
                    bAtLeastOneProviderEnabled = true ;
                }
                else
                {
                    Log.d ( Globals.sApplicationName, "Coarse location sensor not available" ) ;
                }
                if ( oLocationManager.isProviderEnabled ( LocationManager.GPS_PROVIDER ) )
                {
                    Log.d ( Globals.sApplicationName, "Fine location sensor available" ) ;                    
                    oLocationManager.requestLocationUpdates ( LocationManager.GPS_PROVIDER, 0, 0, o3DView, Looper.getMainLooper ( ) ) ;
                    bAtLeastOneProviderEnabled = true ;
                }
                else
                {
                    Log.d ( Globals.sApplicationName, "Fine location sensor not available" ) ;
                }
                return bAtLeastOneProviderEnabled ;
            }
            else
            {
                oLocationManager.removeUpdates ( o3DView ) ;
            }           
        }
        return false ;
    }
 
    //------------------------------------------------------------------
    // Heading
    //
    public static boolean areHeadingUpdatesSupported ( )
    {
        return ! oSensorManager.getSensorList ( Sensor.TYPE_ORIENTATION ).isEmpty ( ) ;
    }

    
    //------------------------------------------------------------------
    // View options methods (must be called before SetContentView).
    //
    public void setFullscreen ( )
    {
        requestWindowFeature   ( Window.FEATURE_NO_TITLE ) ;
        getWindow ( ).setFlags ( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN ) ;
    }
    public void setNoTitle ( )
    {
        requestWindowFeature ( Window.FEATURE_NO_TITLE ) ;
    }

    //------------------------------------------------------------------
    // Utility function to create a writable directory
    //
    protected boolean createWritableDirectory ( String sDir, boolean bDeleteOnExit )
    { 
        // Can we create to the output directory ?
        //
        try 
        { 
            File dir = new File ( sDir ) ;
            
            if ( ! dir.exists ( ) )
            {
                if ( ! dir.mkdirs ( ) ) 
                {
                    Log.d ( Globals.sApplicationName, "Could not create directory: " + sDir ) ;
                    return false ;
                }
            }

            if ( bDeleteOnExit ) dir.deleteOnExit ( ) ; // We want the directory to delete itself when the activity is finished
        } 
        catch ( SecurityException e ) { e.printStackTrace ( ) ; return false ; }
        
        // Can we write to the output directory ?
        //
        try 
        { 
            if ( System.getSecurityManager ( ) != null )
            {
                System.getSecurityManager ( ).checkWrite ( sDir ) ;
            }
        } 
        catch ( SecurityException e ) { e.printStackTrace ( ) ; return false ; }
    
        // Seems ok :)
        //
        return true ;
    }
    
    //------------------------------------------------------------------
    // Utility function to extract and dump a STK file from the APK.
    //
    protected boolean extractAssetFromAPK ( String sAssetName, String sOutputDirPath, String sOutputName )
    { 
        if ( ! createWritableDirectory ( sOutputDirPath, true ) )
        {
            return false ;
        }

        // Extract data
        //
        try
        {
            InputStream oIn  = getAssets ( ).open ( sAssetName ) ;
            if ( oIn != null )
            {
                FileOutputStream oOut = new FileOutputStream ( sOutputDirPath + "/" + sOutputName ) ;
                byte aBuffer [ ] = new byte [ 1024 ] ;
                while ( oIn.available ( ) > 0 )
                {
                    int iLen = ( oIn.available ( ) > 1024 ) ? 1024 : (int)oIn.available ( ) ;
                    oIn .read  ( aBuffer, 0, iLen ) ;
                    oOut.write ( aBuffer, 0, iLen ) ;
                }
                oIn .close ( ) ;
                oOut.close ( ) ;

                Log.d ( Globals.sApplicationName, "Extracted asset " + sOutputName + " to folder" + sOutputDirPath ) ;
                return true ;               
            }
            /* THIS CODE ONLY WORKS FOR UNCOMPRESSED ASSETS
            else
            {
                AssetFileDescriptor oFD = getAssets ( ).openFd ( sAssetName ) ;
                if ( oFD != null )
                {
                    long iFDOffset  = oFD.getStartOffset ( ) ;
                    long iFDLength  = oFD.getLength      ( ) ;
                    FileInputStream  oIn  = new FileInputStream  ( oFD.getFileDescriptor ( ) ) ;
                    FileOutputStream oOut = new FileOutputStream ( sOutputDirPath + "/" + sOutputName ) ;
                    oIn .skip  ( iFDOffset ) ;
                    byte aBuffer [ ] = new byte [ 1024 ] ;
                    for ( long i = 0 ; i < iFDLength ; i += 1024 )
                    {
                        int iLen = ( iFDLength > i + 1024 ) ? 1024 : (int)iFDLength - (int)i ;
                        oIn .read  ( aBuffer, 0, iLen ) ;
                        oOut.write ( aBuffer, 0, iLen ) ;
                    }
                    oIn .close ( ) ;
                    oOut.close ( ) ;
                    oFD .close ( ) ;

                    Log.d ( Globals.sApplicationName, "Extracted asset " + sOutputName + " to folder" + sOutputDirPath ) ;
                    return true ;               
                }
            }
            */
        }
        catch ( IOException e ) { e.printStackTrace ( ) ; }
        return false ;               
    }    
    
    //------------------------------------------------------------------
    // Utility function to extract STK files to a temporary directory
    //    
    private boolean extractPacks ( )
    {
        // First try on the SD card
        //
        mPackDirPath = "/sdcard/Android/data/" + Globals.sPackageName ;
                
        // Extract STK files from the APK and dump them to the packs directory
        //
        if ( //extractAssetFromAPK ( "S3DStartup.stk", mPackDirPath, "S3DStartup.stk" ) &&
             extractAssetFromAPK ( "S3DMain.stk",    mPackDirPath, "S3DMain.stk"    ) )
        {
            return true ;
        }

        // If something went wrong try on the phone internal filesystem 
        //
        mPackDirPath = getCacheDir ( ).getAbsolutePath ( ) ;
                
        // Extract STK files from the APK and dump them to the packs directory
        //
        if ( //extractAssetFromAPK ( "S3DStartup.stk", mPackDirPath, "S3DStartup.stk" ) &&
             extractAssetFromAPK ( "S3DMain.stk",    mPackDirPath, "S3DMain.stk"    ) )
        {
            return true ;
        }

        // No more alternatives :(
        //
        mPackDirPath = "" ;
        return false ;
    }

    //------------------------------------------------------------------
    // Utility function to extract STK files to a temporary directory
    //    
    private boolean extractXMLs ( )
    {
        if ( mPackDirPath != "" )
        {
            try 
            {
                // List XML assets
                //
                String aAssets [] = getAssets ( ).list ( "" ) ;
            
                for ( int i = 0 ; i < aAssets.length ; i++ )
                {
                    if ( aAssets[i].endsWith ( ".xml" ) )
                    {
                        // Extract XML file
                        //
                        if ( ! extractAssetFromAPK ( aAssets[i], mPackDirPath, aAssets[i] ) )
                        {
                            return false ;
                        }
                    }
                }
                
                // OK
                //
                return true ;
            }
            catch ( IOException e ) { e.printStackTrace ( ) ; return false ; }
        }
        return false ;
    }    
    
    //------------------------------------------------------------------
    // Utility function to create the cache directory
    //    
    private boolean createCacheDirectory ( )
    {
        // First try on the SD card
        //
        mCacheDirPath = "/sdcard/Android/data/" + Globals.sPackageName + "/cache" ;
          
        if ( createWritableDirectory ( mCacheDirPath, false ) )
        {
            Log.d ( Globals.sApplicationName, "Using cache directory: " + mCacheDirPath ) ;
            return true ;
        }

        // If something went wrong try on the phone internal filesystem 
        //
        File dir  = getCacheDir ( ) ;
        if ( dir != null )
        {
            mCacheDirPath = dir.getAbsolutePath ( ) ;
            Log.d ( Globals.sApplicationName, "Using cache directory: " + mCacheDirPath ) ;
            return true ;
        }
        
        // No more alternatives :(
        //
        mCacheDirPath = "" ;
        return false ;
    }

    //------------------------------------------------------------------
    // Utility function to create the home directory
    //    
    private boolean createHomeDirectory ( )
    {
        // Get home directory path (persistent scratch pad)
        //
        File dir  = getDir ( "home", 0 ) ;
        if ( dir != null )
        {
            mHomeDirPath = getDir ( "home", 0 ).getAbsolutePath ( ) ;
            Log.d ( Globals.sApplicationName, "Using home directory: " + mHomeDirPath ) ;
            return true ;
        }
        return false ;
    }
    
    //------------------------------------------------------------------
    // Utility function to display a fatal error message.
    //    
    private void showError ( String s )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder ( this ) ;
        builder.setMessage ( s ) ;
        builder.setTitle   ( Globals.sApplicationName ) ;
        //???builder.setIcon    ( R.drawable.app_icon ) ;
        builder.setPositiveButton ( "OK", new DialogInterface.OnClickListener ( ) 
                                              {
                                                  public void onClick ( DialogInterface dialog, int id) 
                                                  {
                                                     finish ( ) ;
                                                  }
                                              }
                                   ) ;
        AlertDialog dialog = builder.create ( ) ;
        dialog.show ( ) ;
    }    

    //------------------------------------------------------------------
    // Software keyboard view.
    //
    //private static KeyboardView     oKeyboardView   ;
    //private static EditText         oEditText       ;

    //------------------------------------------------------------------
    // Main view group.
    //
    private static RelativeLayout   oViewGroup      ;
    
    //------------------------------------------------------------------
    // Splash screen view.
    //
    private static View 			oSplashView		;

    //------------------------------------------------------------------
    // Video surface view.
    //
    private static VideoView        oVideoView      ;
    
    //------------------------------------------------------------------
    // 3D surface view.
    //
    private static S3DSurfaceView   o3DView         ;

    //------------------------------------------------------------------
    // Sound pool object to play sounds from Java.
    //
    private static SoundPool        oSoundPool      ;

    //------------------------------------------------------------------
    // Media player object to play musics from Java.
    //
    @SuppressWarnings("unused")
	private static MediaPlayer      oMediaPlayer    ;

    //------------------------------------------------------------------
    // Vibrator object.
    //
    private static Vibrator         oVibrator       ;
    
    //------------------------------------------------------------------
    // Camera.
    //
    //private static Camera           oCamera     	;
	//private static CameraPreview 	oCameraPreview  ;
    
    //------------------------------------------------------------------
    // Sensor manager object.
    //
    private static SensorManager    oSensorManager  ;       
    
    //------------------------------------------------------------------
    // Sensor manager object.
    //
    private static LocationManager  oLocationManager;
    
    //------------------------------------------------------------------
    // Power manager & wake lock object.
    //
    private static PowerManager           oPowerManager ;
    private static PowerManager.WakeLock  oWakeLock     ;
          
    //------------------------------------------------------------------
    // Singleton object.
    //
    private static MyPleo   oThis         ;        
          
    //------------------------------------------------------------------
    // Various files access infos.
    //
    private String  mCacheDirPath   ;
    private String  mHomeDirPath    ;
    private String  mPackDirPath    ;
    @SuppressWarnings("unused")
	private String  mAPKFilePath    ;              

    //------------------------------------------------------------------
    // Engine native library loading.
    //
    static 
    {
        System.loadLibrary ( "openal" ) ;
        System.loadLibrary ( "S3DClient" ) ;
    }    
}


