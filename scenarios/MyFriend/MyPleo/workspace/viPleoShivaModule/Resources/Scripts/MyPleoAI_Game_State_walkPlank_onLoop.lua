--------------------------------------------------------------------------------
--  State............ : walkPlank
--  Author........... : Tiago Paiva
--  Description...... : (unsure if it is being used)
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI_Game.walkPlank_onLoop ( )
--------------------------------------------------------------------------------
	
	local o = this.getObject ( )

    navigation.setSpeedLimit( o, 30 ) 
    navigation.setAcceleration ( o, 30 )
    log.message ( "WALKPLANK" )
    
    if ( object.hasController ( o, object.kControllerTypeNavigation ) and  navigation.getNode   ( o ) )
    then
        navigation.setNearestTargetNode ( o, application.getCurrentUserSceneTaggedObject ( "plataforma_final" ) )
    end
    
    local vx, vy, vz = navigation.getVelocity ( o )                    -- Get the object's velocity per xyz.
    local x, y, z = object.getTranslation ( o, object.kGlobalSpace )   -- Get the object's postion in xyz.
    object.lookAtWithUp ( o, x - vx, y - vy, z - vz, 0, 1, 0, object.kGlobalSpace, .05 )   -- Look in the direction it's moving.
    
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
