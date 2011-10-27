//----------------------------------------------------------------------
#include <jni.h>
#include <android/log.h>
//----------------------------------------------------------------------        
#include <stdio.h>
#include <stdlib.h>
#include <GLES/gl.h>
//----------------------------------------------------------------------        
#include "S3DClient_Wrapper.h"
//----------------------------------------------------------------------        
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "MyPleo", __VA_ARGS__)
//----------------------------------------------------------------------        
extern "C"
{ 
    //----------------------------------------------------------------------
    static JavaVM      *pJavaVM                         = NULL ; 
    static JNIEnv 	   *pJNIEnv 					    = NULL ;
    static char         aCacheDirPath   [512]           = "" ; 
    static char         aHomeDirPath    [512]           = "" ; 
    static char         aPackDirPath    [512]           = "" ; 
	static char         aOverlayMovie   [512]	        = "" ; 
    static char         aDeviceName      [64]           = "" ; 
    static char         aDeviceModel     [64]           = "" ; 
    static char         aSystemVersion   [32]           = "" ; 
    static char         aSystemLanguage  [32]           = "" ; 
    static int          iSurfaceWidth                   = 320 ; 
    static int          iSurfaceHeight                  = 240 ; 
    static bool 	    bMouseButtonDown                = false ; 
    static bool 	    bSupportLocation                = false ; 
    static bool 	    bSupportHeading                 = false ; 
    static bool         bForceNoViewportRotation        = false ;
	static bool         bSupportLowLatencyAudioTrack	= false ;
	static bool         bForceAudioBackendOpenAL        = false  ;
	static bool         bForceAudioBackendAndroid       = false ;	
	//static bool     bVibrate					= false ;
    //----------------------------------------------------------------------       
	static jclass		jclass_Activity ;
	static jmethodID    jmethod_onOpenURL ;
	static jmethodID    jmethod_onInitSound ;
	static jmethodID    jmethod_onShutdownSound ;
	static jmethodID    jmethod_onLoadSound ;
	static jmethodID    jmethod_onUnloadSound ;
	static jmethodID    jmethod_onPlaySound ;
	static jmethodID    jmethod_onPauseSound ;
	static jmethodID    jmethod_onResumeSound ;
	static jmethodID    jmethod_onStopSound ;
	static jmethodID    jmethod_onSetSoundVolume ;
	static jmethodID    jmethod_onSetSoundPitch ;
	static jmethodID    jmethod_onSetSoundLooping ;
	static jmethodID    jmethod_onEnableLocationUpdates ;
    //----------------------------------------------------------------------        
    static void CreateDotNoMediaFile ( )
    {
        char      aFilePath   [512] ;
        strcpy  ( aFilePath,  aCacheDirPath ) ;
        strcat  ( aFilePath,  "/.nomedia"   ) ;
        
        FILE   * pFile = fopen ( aFilePath, "w" ) ;
        if     ( pFile )
        {
            fclose ( pFile ) ;
        }        
    }
    //----------------------------------------------------------------------        
    static bool DumpBufferToFile ( const char *_pBuffer, int _iBufferSize, const char *_pFilePath )
    {
        FILE   * pFile = fopen ( _pFilePath, "wb" ) ;
        if     ( pFile )
        {
            fwrite ( _pBuffer, _iBufferSize, 1, pFile ) ;
            fclose ( pFile ) ;
            return true ;
        }
        return false ;
    }
    //----------------------------------------------------------------------        
    static void S3DLogCallback ( int _iLogCategory, const char *_pLogMessage )
    {
        LOGI( _pLogMessage ) ;
    }
    //----------------------------------------------------------------------        
    static void S3DOpenURLCallback ( const char *_pURL, const char *_pTarget, void *_pOwner )
    {
        if ( jmethod_onOpenURL && jclass_Activity && pJNIEnv )
        {
            pJNIEnv->CallStaticIntMethod ( jclass_Activity, jmethod_onOpenURL, pJNIEnv->NewStringUTF ( _pURL ), pJNIEnv->NewStringUTF ( _pTarget ) ) ;
        }
    }
    //----------------------------------------------------------------------        
	
    static bool S3DSoundDeviceInitializeCallback ( void *_pOwner )
    {
        if ( jmethod_onInitSound && jclass_Activity && pJNIEnv )
        {
            return pJNIEnv->CallStaticBooleanMethod ( jclass_Activity, jmethod_onInitSound ) ;
        }
        return false ;
    }
    //----------------------------------------------------------------------        
    static void S3DSoundDeviceShutdownCallback ( void *_pOwner )
    {
        if ( jmethod_onShutdownSound && jclass_Activity && pJNIEnv )
        {
             pJNIEnv->CallStaticVoidMethod ( jclass_Activity, jmethod_onShutdownSound ) ;
        }
    }
    //----------------------------------------------------------------------        
    static int S3DSoundLoadCallback ( const char *_pBuffer, int _iBufferSize, void *_pOwner )
    {
        if ( jmethod_onLoadSound && jclass_Activity && pJNIEnv )
        {
             char      aFileName [32] ;
             sprintf ( aFileName, "%08x", _iBufferSize ) ;
             char      aFilePath   [512] ;
             strcpy  ( aFilePath,  aCacheDirPath ) ;
             strcat  ( aFilePath,  "/"         ) ;
             strcat  ( aFilePath,  aFileName     ) ;
             if      ( memcmp ( _pBuffer, "OggS", 4 ) == 0 ) strcat ( aFilePath, ".ogg" ) ;
             else if ( memcmp ( _pBuffer, "VAGp", 4 ) == 0 ) strcat ( aFilePath, ".vag" ) ;
             else                                            strcat ( aFilePath, ".wav" ) ;
             
             if ( DumpBufferToFile ( _pBuffer, _iBufferSize, aFilePath ) )
             {
                 return pJNIEnv->CallStaticIntMethod ( jclass_Activity, jmethod_onLoadSound, pJNIEnv->NewStringUTF ( aFilePath ) ) ;
             }
        }
        return 0 ;
    }
    //----------------------------------------------------------------------        
    static void S3DSoundUnloadCallback ( int _iSoundIndex, void *_pOwner )
    {
        if ( jmethod_onUnloadSound && jclass_Activity && pJNIEnv )
        {
            pJNIEnv->CallStaticVoidMethod ( jclass_Activity, jmethod_onUnloadSound, _iSoundIndex ) ;
        }
    }
    //----------------------------------------------------------------------        
    static int S3DSoundPlayCallback ( int _iSoundIndex, float _fVolume, bool _bLoop, float _fPriority, void *_pOwner )
    {
        if ( jmethod_onPlaySound && jclass_Activity && pJNIEnv )
        {
            return pJNIEnv->CallStaticIntMethod ( jclass_Activity, jmethod_onPlaySound, _iSoundIndex, _fVolume, _bLoop, _fPriority ) ;
        }
        return -1 ;
    }
    //----------------------------------------------------------------------        
    static void S3DSoundPauseCallback ( int _iSoundIndex, void *_pOwner )
    {
        if ( jmethod_onPauseSound && jclass_Activity && pJNIEnv )
        {
             pJNIEnv->CallStaticVoidMethod ( jclass_Activity, jmethod_onPauseSound, _iSoundIndex ) ;
        }
    }
    //----------------------------------------------------------------------        
    static void S3DSoundResumeCallback ( int _iSoundIndex, void *_pOwner )
    {
        if ( jmethod_onResumeSound && jclass_Activity && pJNIEnv )
        {
             pJNIEnv->CallStaticVoidMethod ( jclass_Activity, jmethod_onResumeSound, _iSoundIndex ) ;
        }
    }
    //----------------------------------------------------------------------        
    static void S3DSoundStopCallback ( int _iSoundIndex, void *_pOwner )
    {
        if ( jmethod_onStopSound && jclass_Activity && pJNIEnv )
        {
             pJNIEnv->CallStaticVoidMethod ( jclass_Activity, jmethod_onStopSound, _iSoundIndex ) ;
        }
    }
    //----------------------------------------------------------------------        
    static void S3DSoundSetVolumeCallback ( int _iSoundIndex, float _fVolume, void *_pOwner )
    {
        if ( jmethod_onSetSoundVolume && jclass_Activity && pJNIEnv )
        {
			 pJNIEnv->CallStaticVoidMethod ( jclass_Activity, jmethod_onSetSoundVolume, _iSoundIndex, _fVolume ) ;
        }
    }
    //----------------------------------------------------------------------        
    static void S3DSoundSetPitchCallback ( int _iSoundIndex, float _fPitch, void *_pOwner )
    {
        if ( jmethod_onSetSoundPitch && jclass_Activity && pJNIEnv )
        {
             pJNIEnv->CallStaticVoidMethod ( jclass_Activity, jmethod_onSetSoundPitch, _iSoundIndex, _fPitch ) ;
        }
    }
    //----------------------------------------------------------------------        
    static void S3DSoundSetLoopingCallback ( int _iSoundIndex, bool _bLoop, void *_pOwner )
    {
        if ( jmethod_onSetSoundLooping && jclass_Activity && pJNIEnv )
        {
             pJNIEnv->CallStaticVoidMethod ( jclass_Activity, jmethod_onSetSoundLooping, _iSoundIndex, _bLoop ) ;
        }
    }
	
    //----------------------------------------------------------------------        
    //static void S3DVibrateCallback ( float _fMagnitude, void *_pOwner )
    //{
	//	bVibrate = ( _fMagnitude <= -0.5f || _fMagnitude >=  0.5f ) ;
    //}
    //----------------------------------------------------------------------        
    static bool S3DPlayOverlayMovieCallback ( const char *_pFileName, void *_pOwner )
    {
        strcpy ( aOverlayMovie, _pFileName ) ;
        return true ;
    }
    //----------------------------------------------------------------------        
    static void S3DStopOverlayMovieCallback ( void *_pOwner )
    {
		aOverlayMovie[0] = '\0' ;
    }
    //----------------------------------------------------------------------        
    static bool S3DEnableLocationCallback ( bool _bEnable, void *_pOwner )
    {
        LOGI( "### S3DEnableLocationCallback" ) ;
        if ( jmethod_onEnableLocationUpdates && jclass_Activity && pJNIEnv )
        {
             return pJNIEnv->CallStaticBooleanMethod ( jclass_Activity, jmethod_onEnableLocationUpdates, _bEnable ) ;
        }
        return false ;
    }        
    //----------------------------------------------------------------------        
    static bool S3DEnableHeadingCallback ( bool _bEnable, void *_pOwner )
    {
        LOGI( "### S3DEnableHeadingCallback" ) ;		
        return bSupportHeading ;
    }        
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineSetDirectories ( JNIEnv *_pEnv, jobject obj, jstring sCacheDirPath, jstring sHomeDirPath, jstring sPackDirPath )
    {
        LOGI( "### engineSetDirectories" ) ;
        const char *pCacheStr = _pEnv->GetStringUTFChars ( sCacheDirPath, NULL ) ;
        const char *pHomeStr  = _pEnv->GetStringUTFChars ( sHomeDirPath,  NULL ) ;
        const char *pPackStr  = _pEnv->GetStringUTFChars ( sPackDirPath,  NULL ) ;
        if ( pCacheStr ) strcpy ( aCacheDirPath, pCacheStr ) ;
        if ( pHomeStr  ) strcpy ( aHomeDirPath,  pHomeStr  ) ;
        if ( pPackStr  ) strcpy ( aPackDirPath,  pPackStr  ) ;
        if ( pCacheStr ) _pEnv->ReleaseStringUTFChars ( sCacheDirPath, pCacheStr ) ;
        if ( pHomeStr  ) _pEnv->ReleaseStringUTFChars ( sHomeDirPath,  pHomeStr  ) ;
        if ( pPackStr  ) _pEnv->ReleaseStringUTFChars ( sPackDirPath,  pPackStr  ) ;
    }
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineSetLocationSupport ( JNIEnv *_pEnv, jobject obj, jboolean bSupport )
    {
        bSupportLocation = bSupport ;
    }        
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineSetHeadingSupport ( JNIEnv *_pEnv, jobject obj, jboolean bSupport )
    {
        bSupportHeading = bSupport ;
    }        
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineSetDeviceName ( JNIEnv *_pEnv, jobject obj, jstring sName )
	{
        const char *pNameStr = _pEnv->GetStringUTFChars ( sName, NULL ) ;
        if ( pNameStr  ) strcpy ( aDeviceName,  pNameStr  ) ;
        if ( pNameStr ) _pEnv->ReleaseStringUTFChars ( sName, pNameStr ) ;
	}
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineSetDeviceModel ( JNIEnv *_pEnv, jobject obj, jstring sModel )
	{
        const char *pModelStr = _pEnv->GetStringUTFChars ( sModel, NULL ) ;
        if ( pModelStr  ) strcpy ( aDeviceModel,  pModelStr  ) ;
        if ( pModelStr ) _pEnv->ReleaseStringUTFChars ( sModel, pModelStr ) ;
	}
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineSetSystemVersion ( JNIEnv *_pEnv, jobject obj, jstring sVersion )
	{
        const char *pVersionStr = _pEnv->GetStringUTFChars ( sVersion, NULL ) ;
        if ( pVersionStr  ) strcpy ( aSystemVersion,  pVersionStr  ) ;
        if ( pVersionStr ) _pEnv->ReleaseStringUTFChars ( sVersion, pVersionStr ) ;
	}
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineSetSystemLanguage ( JNIEnv *_pEnv, jobject obj, jstring sLanguage )
	{
        const char *pLanguageStr = _pEnv->GetStringUTFChars ( sLanguage, NULL ) ;
        if ( pLanguageStr  ) strcpy ( aSystemLanguage,  pLanguageStr  ) ;
        if ( pLanguageStr ) _pEnv->ReleaseStringUTFChars ( sLanguage, pLanguageStr ) ;
	}
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineForceDefaultOrientation ( JNIEnv *_pEnv, jobject obj, jboolean b )
    {
        bForceNoViewportRotation = b ; // TODO: set an engine's option instead
    }
    //----------------------------------------------------------------------        
    jboolean Java_eu_lirec_pleo_S3DRenderer_engineInitialize ( JNIEnv *_pEnv, jobject obj )
    {
        LOGI( "### engineInitialize" ) ;
        
        // Get a pointer to the Java VM
        //
        _pEnv->GetJavaVM ( &pJavaVM ) ;

		// Bind Java methods
		//
        if ( pJavaVM )
        {
            if ( pJavaVM->GetEnv ( (void**) &pJNIEnv, JNI_VERSION_1_4 ) >= 0 )
            {
                jclass_Activity  = pJNIEnv->FindClass ( "eu/lirec/pleo/MyPleo" ) ;
                if ( jclass_Activity != 0 )
                {		
					jmethod_onOpenURL               = pJNIEnv->GetStaticMethodID ( jclass_Activity, "onOpenURL", "(Ljava/lang/String;Ljava/lang/String;)V" ) ;
					jmethod_onInitSound             = pJNIEnv->GetStaticMethodID ( jclass_Activity, "onInitSound", "()Z" ) ;
					jmethod_onShutdownSound         = pJNIEnv->GetStaticMethodID ( jclass_Activity, "onShutdownSound", "()V" ) ;
					jmethod_onLoadSound             = pJNIEnv->GetStaticMethodID ( jclass_Activity, "onLoadSound", "(Ljava/lang/String;)I" ) ;
					jmethod_onUnloadSound           = pJNIEnv->GetStaticMethodID ( jclass_Activity, "onUnloadSound", "(I)V" ) ;
					jmethod_onPlaySound             = pJNIEnv->GetStaticMethodID ( jclass_Activity, "onPlaySound", "(IFZF)I" ) ;
					jmethod_onPauseSound            = pJNIEnv->GetStaticMethodID ( jclass_Activity, "onPauseSound", "(I)V" ) ;
					jmethod_onResumeSound           = pJNIEnv->GetStaticMethodID ( jclass_Activity, "onResumeSound", "(I)V" ) ;
					jmethod_onStopSound             = pJNIEnv->GetStaticMethodID ( jclass_Activity, "onStopSound", "(I)V" ) ;
					jmethod_onSetSoundVolume        = pJNIEnv->GetStaticMethodID ( jclass_Activity, "onSetSoundVolume", "(IF)V" ) ;
					jmethod_onSetSoundPitch         = pJNIEnv->GetStaticMethodID ( jclass_Activity, "onSetSoundPitch", "(IF)V" ) ;
					jmethod_onSetSoundLooping       = pJNIEnv->GetStaticMethodID ( jclass_Activity, "onSetSoundLooping", "(IZ)V" ) ;
					jmethod_onEnableLocationUpdates = pJNIEnv->GetStaticMethodID ( jclass_Activity, "onEnableLocationUpdates", "(Z)Z" ) ;
				}
			}
		}

        // Create the ".nomedia" file
        //
        CreateDotNoMediaFile ( ) ;

		// Depending on the OS version, use OpenAL/AudioTrack or the crappy SoundPool
		//
		/* Wrong assumption, not depending on the OS but on the hardware...
		if ( strlen( aSystemVersion ) >= 3 )
		{
			int iMajor, iMinor ;
			
			if ( sscanf ( aSystemVersion, "%d.%d",  &iMajor, &iMinor ) == 2 )
			{
				// Starting from Froyo (2.2) LLAT is supported
				//
		    	bSupportLowLatencyAudioTrack = (iMajor >= 2) && (iMinor >= 2) ;
		    }
		}
		*/
		// On Tegra and Qualcomm chips LLAT *seems* to be supported
		//
		const char *pGPUVendor = (const char *)glGetString ( GL_VENDOR ) ; 
		
		bSupportLowLatencyAudioTrack = pGPUVendor && ( strstr ( pGPUVendor, "NVIDIA" ) || strstr ( pGPUVendor, "Qualcomm" ) ) ;

        // Initialize engine
        //
        //char aLoadPackPath [512] ; sprintf ( aLoadPackPath, "file://%s/S3DStartup.stk", aPackDirPath ) ; 
        char aMainPackPath [512] ; sprintf ( aMainPackPath, "file://%s/S3DMain.stk",    aPackDirPath ) ;
        S3DClient_Init                                          ( aHomeDirPath ) ;
        S3DClient_SetGraphicContainer                           ( NULL , 0, 0, iSurfaceWidth, iSurfaceHeight ) ;
        S3DClient_SetInputContainer                             ( NULL , 0, 0, iSurfaceWidth, iSurfaceHeight ) ;
        S3DClient_SetFullscreen                                 ( false ) ;
        S3DClient_SetClientType                                 ( S3DClient_Type_StandAlone ) ;
        S3DClient_SetLogCallbacks                               ( S3DLogCallback, S3DLogCallback, S3DLogCallback ) ;
		S3DClient_SetOpenURLCallback                            ( S3DOpenURLCallback,				NULL ) ;
        S3DClient_SetPlayOverlayMovieCallback                   ( S3DPlayOverlayMovieCallback,      NULL ) ;
        S3DClient_SetStopOverlayMovieCallback                   ( S3DStopOverlayMovieCallback,      NULL ) ;
		if ( bForceAudioBackendAndroid || ( ! bSupportLowLatencyAudioTrack && ! bForceAudioBackendOpenAL ) )
		{
			S3DClient_Android_SetSoundDeviceUseExternalDriver   ( true ) ;
            S3DClient_Android_SetSoundDeviceInitializeCallback  ( S3DSoundDeviceInitializeCallback, NULL ) ;
            S3DClient_Android_SetSoundDeviceShutdownCallback    ( S3DSoundDeviceShutdownCallback,   NULL ) ;
            S3DClient_Android_SetSoundLoadCallback              ( S3DSoundLoadCallback,             NULL ) ;
            S3DClient_Android_SetSoundUnloadCallback            ( S3DSoundUnloadCallback,           NULL ) ;
            S3DClient_Android_SetSoundPlayCallback              ( S3DSoundPlayCallback,             NULL ) ;
            S3DClient_Android_SetSoundPauseCallback             ( S3DSoundPauseCallback,            NULL ) ;
            S3DClient_Android_SetSoundResumeCallback            ( S3DSoundResumeCallback,           NULL ) ;
            S3DClient_Android_SetSoundStopCallback              ( S3DSoundStopCallback,             NULL ) ;
            S3DClient_Android_SetSoundSetVolumeCallback         ( S3DSoundSetVolumeCallback,        NULL ) ;
            S3DClient_Android_SetSoundSetPitchCallback          ( S3DSoundSetPitchCallback,         NULL ) ;
            S3DClient_Android_SetSoundSetLoopingCallback        ( S3DSoundSetLoopingCallback,       NULL ) ;
            S3DClient_Android_InitializeSoundDevice             ( ) ;
        }
        //TODO: S3DClient_Android_SetVibrateCallback        		( S3DVibrateCallback,               NULL ) ;
        S3DClient_Android_SetLocationSupported                  ( bSupportLocation ) ;
		S3DClient_Android_SetHeadingSupported					( bSupportHeading ) ;
        S3DClient_Android_SetEnableLocationCallback             ( S3DEnableLocationCallback,        NULL ) ;
        S3DClient_Android_SetEnableHeadingCallback              ( S3DEnableHeadingCallback,         NULL ) ;
        S3DClient_Android_SetDeviceName                         ( aDeviceName ) ;
        S3DClient_Android_SetDeviceModel                        ( aDeviceModel ) ;
        S3DClient_Android_SetSystemVersion                      ( aSystemVersion ) ;
        S3DClient_Android_SetSystemLanguage                     ( aSystemLanguage ) ;
        S3DClient_LoadPack                                      ( NULL, aMainPackPath,              NULL ) ;
        S3DClient_RunOneFrame                                   ( ) ; // Call it one time to clear the stopped flag
        S3DClient_iPhone_OnTouchesChanged                       ( 0, 0, 0.0f, 0.0f, 0, 0, 0.0f, 0.0f, 0, 0, 0.0f, 0.0f, 0, 0, 0.0f, 0.0f, 0, 0, 0.0f, 0.0f ) ; // Force no touches (should not be needed...)
        return true ;
    }
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineShutdown ( JNIEnv *_pEnv, jobject obj ) 
    { 
        LOGI( "### engineShutdown" ) ; 
        S3DClient_Shutdown ( ) ;
        pJavaVM = NULL ;
    }
    //----------------------------------------------------------------------        
    jboolean Java_eu_lirec_pleo_S3DRenderer_engineRunOneFrame ( JNIEnv *_pEnv, jobject obj )
    {
        //LOGI( "### runOneFrame" ) ; 
        if (   bForceNoViewportRotation  ) S3DClient_iPhone_SetViewportRotation ( 0 ) ; // FIXME: avoid extra call by setting an engine's option instead
        if ( ! S3DClient_RunOneFrame ( ) ) return false ;
        if (   S3DClient_Stopped     ( ) ) return false ;
        return true ;
    }
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineOnResize ( JNIEnv *_pEnv, jobject obj, jint w, jint h )        
    {
        LOGI( "### engineOnResize" ) ;
        iSurfaceWidth  = w ;        
        iSurfaceHeight = h ;        
        S3DClient_SetGraphicContainer ( NULL, 0, 0, w, h ) ;        
        S3DClient_SetInputContainer   ( NULL, 0, 0, w, h ) ;        
    }
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineOnMouseMove ( JNIEnv *_pEnv, jobject obj, jfloat x, jfloat y )        
    {
        S3DClient_iPhone_OnMouseMoved           ( 2.0f * x / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y ) / (float)iSurfaceHeight - 1.0f ) ;        
        if ( ! bMouseButtonDown )
        {
            bMouseButtonDown = true ;        
            S3DClient_iPhone_OnMouseButtonPressed ( ) ;
        }
        //LOGI( "### Move: %f %f", 2.0f * x / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y ) / (float)iSurfaceHeight - 1.0f ) ;      
    }
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineOnMouseButtonDown ( JNIEnv *_pEnv, jobject obj, jfloat x, jfloat y )        
    {
        bMouseButtonDown = true ;
        S3DClient_iPhone_OnMouseMoved           ( 2.0f * x / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y ) / (float)iSurfaceHeight - 1.0f ) ;        
        S3DClient_iPhone_OnMouseButtonPressed   ( ) ;        

        //LOGI( "### Down: %f %f", 2.0f * x / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y ) / (float)iSurfaceHeight - 1.0f ) ;      
    }
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineOnMouseButtonUp ( JNIEnv *_pEnv, jobject obj, jfloat x, jfloat y )        
    {
        bMouseButtonDown = false ;
        S3DClient_iPhone_OnMouseMoved           ( 2.0f * x / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y ) / (float)iSurfaceHeight - 1.0f ) ;        
        S3DClient_iPhone_OnMouseButtonReleased  ( ) ;
        
        //LOGI( "### Up: %f %f", 2.0f * x / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y ) / (float)iSurfaceHeight - 1.0f ) ;      
    }
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineOnTouchesChange ( JNIEnv *_pEnv, jobject obj, jint tc1, jfloat x1, jfloat y1, jint tc2, jfloat x2, jfloat y2, jint tc3, jfloat x3, jfloat y3, jint tc4, jfloat x4, jfloat y4, jint tc5, jfloat x5, jfloat y5 )
    {
        if ( S3DClient_iPhone_IsMultiTouchEnabled ( ) )
        {
            S3DClient_iPhone_OnTouchesChanged   ( 0, tc1, 2.0f * x1 / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y1 ) / (float)iSurfaceHeight - 1.0f,
                                                  0, tc2, 2.0f * x2 / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y2 ) / (float)iSurfaceHeight - 1.0f,
                                                  0, tc3, 2.0f * x3 / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y3 ) / (float)iSurfaceHeight - 1.0f,
                                                  0, tc4, 2.0f * x4 / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y4 ) / (float)iSurfaceHeight - 1.0f,
                                                  0, tc5, 2.0f * x5 / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y5 ) / (float)iSurfaceHeight - 1.0f ) ;
        }

		//LOGI( "###### %d %d %d %d %d", tc1, tc2, tc3, tc4, tc5 ) ;
    }
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineOnDeviceMove ( JNIEnv *_pEnv, jobject obj, jfloat x, jfloat y, jfloat z )
    {
        if ( iSurfaceWidth > iSurfaceHeight ) // FIXME
            S3DClient_iPhone_OnDeviceMoved (  y / 9.81f, -x / 9.81f, z / 9.81f ) ;
        else
            S3DClient_iPhone_OnDeviceMoved ( -x / 9.81f, -y / 9.81f, z / 9.81f ) ;
    }
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineOnKeyboardKeyDown ( JNIEnv *_pEnv, jobject obj, jint key, jint uni )        
    {
        S3DClient_Android_OnKeyboardKeyPressed ( key, uni ) ;
    }
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineOnKeyboardKeyUp ( JNIEnv *_pEnv, jobject obj, jint key, jint uni )        
    {
        S3DClient_Android_OnKeyboardKeyReleased ( key, uni ) ;
    }
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineOnLocationChanged ( JNIEnv *_pEnv, jobject obj, jfloat x, jfloat y, jfloat z )
    {
        S3DClient_Android_UpdateLocation ( x, y, z ) ;
    }
    //----------------------------------------------------------------------        
    void Java_eu_lirec_pleo_S3DRenderer_engineOnHeadingChanged ( JNIEnv *_pEnv, jobject obj, jfloat angle )
    {
        S3DClient_Android_UpdateHeading ( angle, angle ) ;
    }
    //----------------------------------------------------------------------   
	void Java_eu_lirec_pleo_S3DRenderer_engineOnOverlayMovieStopped ( JNIEnv *_pEnv, jobject obj )
	{
		aOverlayMovie[0] = '\0' ;
		S3DClient_OnOverlayMovieStopped ( ) ;
	}
    //----------------------------------------------------------------------   
	jstring Java_eu_lirec_pleo_S3DRenderer_engineGetOverlayMovie ( JNIEnv *_pEnv, jobject obj )
	{
		return _pEnv->NewStringUTF ( aOverlayMovie ) ;
	}
    //----------------------------------------------------------------------   
	//jboolean Java_eu_lirec_pleo_S3DRenderer_engineGetVibratorState ( JNIEnv *_pEnv, jobject obj )
	//{
	//	return bVibrate ;
	//}
	//----------------------------------------------------------------------        	
} 
