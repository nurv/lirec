--------------------------------------------------------------------------------
--  State............ : searchWater
--  Author........... : Tiago Paiva
--  Description...... : Pleo is walking towards water bowl.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.searchWater_onLoop ( )
--------------------------------------------------------------------------------
	
	--
	-- Write your code here, using 'this' as current AI instance.
	--
	local o = this.getObject ( )

    if ( object.hasController ( o, object.kControllerTypeNavigation ) and  navigation.getNode   ( o ) )
    then
        navigation.setNearestTargetNode ( o, application.getCurrentUserSceneTaggedObject ( "Waterbowl" ) )
        navigation.setAcceleration( o, 10 ) 

        navigation.setSpeedLimit( o, 10 ) 
    end
    
    local vx, vy, vz = navigation.getVelocity ( o )                    -- Get the object's velocity per xyz.
    local x, y, z = object.getTranslation ( o, object.kGlobalSpace )   -- Get the object's postion in xyz.
    object.lookAtWithUp ( o, x - vx, y - vy, z - vz, 0, 1, 0, object.kGlobalSpace, .05 )   -- Look in the direction it's moving.
    
    if not this.gotowater ( ) then
        navigation.setNearestTargetNode ( o, this.hBody ( ) )
        this.Drinking ( )
    end
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
