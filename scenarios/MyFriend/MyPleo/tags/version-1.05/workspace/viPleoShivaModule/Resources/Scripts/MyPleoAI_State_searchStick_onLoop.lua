--------------------------------------------------------------------------------
--  State............ : searchStick
--  Author........... : Tiago Paiva
--  Description...... : (currently not being used)
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.searchStick_onLoop ( )
--------------------------------------------------------------------------------
	
	local o = this.getObject ( )
    navigation.setAcceleration( o, 40 ) 

        navigation.setSpeedLimit( o, 40 ) 
    if ( object.hasController ( o, object.kControllerTypeNavigation ) and  navigation.getNode   ( o ) )
    then
        navigation.setNearestTargetNode ( o, application.getCurrentUserSceneTaggedObject ( "Stick" ) )
        
    end
    
    local vx, vy, vz = navigation.getVelocity ( o )                    -- Get the object's velocity per xyz.
    local x, y, z = object.getTranslation ( o, object.kGlobalSpace )   -- Get the object's postion in xyz.
    object.lookAtWithUp ( o, x - vx, y - vy, z - vz, 0, 1, 0, object.kGlobalSpace, .05 )   -- Look in the direction it's moving.
    
    if not this.gotstick ( ) then
        navigation.setNearestTargetNode ( o, this.hBody ( ) )
        navigation.setAcceleration( o, 10 ) 

        navigation.setSpeedLimit( o, 10 )

        this.Sticking ( )
    end
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
