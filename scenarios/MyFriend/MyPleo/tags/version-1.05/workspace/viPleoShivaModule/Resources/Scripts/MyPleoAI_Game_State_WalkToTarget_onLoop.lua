--------------------------------------------------------------------------------
--  State............ : WalkToTarget
--  Author........... : Tiago Paiva
--  Description...... : (unsure if it is being used)
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI_Game.WalkToTarget_onLoop ( )
--------------------------------------------------------------------------------
	
	local o = this.getObject ( )
	 local hObject = this.hObject ( )
  local hProg= application.getCurrentUserEnvironmentVariable ( "hProg")
        application.setCurrentUserEnvironmentVariable ( "hProg", false)  
    animation.changeClip ( hObject, 0, 3 )
   animation.setPlaybackMode ( hObject, 0, animation.kPlaybackModeLoop ) 
---
    navigation.setSpeedLimit( o, 40 ) 
    navigation.setAcceleration ( o, 40 )
   -- log.message ( "WALK TO TARGET LOOP" )
    
    
    if ( object.hasController ( o, object.kControllerTypeNavigation ) and  navigation.getNode   ( o ) )
    then
            navigation.setNearestTargetNode ( o, application.getCurrentUserSceneTaggedObject ( this.hTgt ( )) )
    end
    
    local vx, vy, vz = navigation.getVelocity ( o )                    -- Get the object's velocity per xyz.
    local x, y, z = object.getTranslation ( o, object.kGlobalSpace )   -- Get the object's postion in xyz.
    object.lookAtWithUp ( o, x - vx, y - vy, z - vz, 0, 1, 0, object.kGlobalSpace, .05 )   -- Look in the direction it's moving.
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
