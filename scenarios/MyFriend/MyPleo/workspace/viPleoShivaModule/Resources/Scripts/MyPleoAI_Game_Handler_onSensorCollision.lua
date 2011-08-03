--------------------------------------------------------------------------------
--  Handler.......... : onSensorCollision
--  Author........... : Tiago Paiva and Paulo F. Gomes
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI_Game.onSensorCollision ( nSensorID, hTargetObject, nTargetSensorID )
--------------------------------------------------------------------------------
	
        local hObject = this.hObject ( )
    	local o = this.getObject ( )
        local hProg= application.getCurrentUserEnvironmentVariable ( "hProg")

        --this.WalkToPer ( )
   if nTargetSensorID == 10 and (this.hPer ( ) == "per1") then
    log.message ( "PERCURSO 1 REACHED" )
        if(this.hBvalue ( ) == this.hPvalue ( ))
        then
            this.hTgt ( "tgt3")
        else 
            this.loseWay ( )
        end
        --this.WalkToTarget ( )
    end
    
    if nTargetSensorID == 0 and this.gotplank ( ) == false
    then

       log.message ( "COLLISION SENSOR ID 0" )
        navigation.setSpeedLimit( o, 10 ) 
        navigation.setAcceleration ( o, 10 )
        this.gotplank ( true )
        
       -- log.message ( navigation.getSpeed (o) )
        
        
        sound.play ( this.hBody ( ), 8, 50, false, 1 )
        --sound.stop ( this.hBody ( ), 8 )
        --this.doNothing ( )

        this.doNothing ( )
    
    end
    if nTargetSensorID == 1 and this.gotbox ( ) == false
    then
     log.message ( "COLLISION SENSOR ID 1" )
        navigation.setSpeedLimit( o, 10 ) 
        navigation.setAcceleration ( o, 10 )
        this.gotbox ( true )
        
        log.message ( navigation.getSpeed (o) )
        
        sound.play ( this.hBody ( ), 8, 50, false, 1 )
        --sound.stop ( this.hBody ( ), 8 )
        --this.doNothing ( )

        this.doNothing ( )
    end
    
    if nTargetSensorID == 2 and this.gotplank2 ( ) == false
    then
      log.message ( "COLLISION SENSOR ID 2" )
        navigation.setSpeedLimit( o, 10 ) 
        navigation.setAcceleration ( o, 10 )
        this.gotplank2 ( true )
        
        log.message ( navigation.getSpeed (o) )
        
        log.message ( "CHEGOU AO FIM" )
        sound.play ( this.hBody ( ), 8, 50, false, 1 )

        local hUser = application.getCurrentUser ( )
        local win = hud.getComponent ( hUser, "myHIT.win" )
        hud.setComponentVisible ( win, true )
        
        local nNeedSkills = application.getCurrentUserEnvironmentVariable ( "nNeedSkills" )
        local congratulations = hud.getComponent ( hUser, "myHIT.congratulations" )
        if ( nNeedSkills > 0)
        then
            hud.setComponentVisible ( congratulations, false )
        else
            hud.setComponentVisible ( congratulations, true )
        end
           
        local gameButtons = hud.getComponent ( hUser, "myHIT.bar_prog" )
        hud.setComponentVisible ( gameButtons, false )

        this.doNothing ( )

    end
    if ( navigation.getSpeed (o) == 0)   -- If the object is moving, then do the following...
        then
            
       --log.message ( "COLLISION SPEED 0" )
       --log.message ( "ESTA PARADO" )
        animation.changeClip ( hObject, 0, 1 )
        animation.setPlaybackMode ( hObject, 0, animation.kPlaybackModeLoop ) 
        animation.setPlaybackKeyFrameBegin ( hObject, 0, animation.getPlaybackKeyFrameEnd ( hObject, 0 ) )
        application.setCurrentUserEnvironmentVariable ( "hProg", true)  

    end
    
   ----- local x, y, z = object.getTranslation ( this.hCamera ( ), object.kGlobalSpace )   -- Get the object's postion in xyz.
    --object.lookAtWithUp ( hObject, x, 0, z, 0, 1, 0, object.kGlobalSpace, .05 )   -- Look in the direction it'
   ----- object.lookAt ( hObject,x,0,z,object.kGlobalSpace, 0.05 )
   -------animation.changeClip ( hObject, 0, 1 )
   --animation.setPlaybackMode ( hObject, 0, animation.kPlaybackModeLoop ) 
   ------animation.setPlaybackKeyFrameBegin ( hObject, 0, animation.getPlaybackKeyFrameEnd ( hObject, 0 ) )
   --this.loseWay ( )
   ------end

--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
