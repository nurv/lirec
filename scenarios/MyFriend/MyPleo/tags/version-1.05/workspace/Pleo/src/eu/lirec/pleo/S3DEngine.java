//----------------------------------------------------------------------
package eu.lirec.pleo;
//----------------------------------------------------------------------        
import android.app.Application;
import android.content.res.Configuration;
//----------------------------------------------------------------------                
public class S3DEngine extends Application        
{
    //------------------------------------------------------------------
    // Application overrides
    //
    @Override
    public void onCreate ( )
    {
        //Log.d ( "S3DEngine", "### onCreate" ) ;
        
        // Call parent constructor
        //
        super.onCreate  ( ) ;
    }
    
    //------------------------------------------------------------------
    @Override
    public void onTerminate ( ) 
    {
        //Log.d ( "S3DENGINE MAIN", "### onTerminate" ) ;
        super.onTerminate ( ) ;
    }
    
    //------------------------------------------------------------------
    @Override
    public void onConfigurationChanged ( Configuration newConfig )
    {
        //Log.d ( "S3DENGINE MAIN", "### onConfigurationChanged : " + newConfig.orientation ) ;
        //super.onConfigurationChanged ( newConfig ) ;
    }
}

