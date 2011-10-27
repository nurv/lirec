--------------------------------------------------------------------------------
--  State............ : RandomWalk
--  Author........... : Tiago Paiva
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI_Game.RandomWalk_onLoop ( )
--------------------------------------------------------------------------------
	
	 local o = this.getObject ( )

    if ( object.hasController ( o, object.kControllerTypeNavigation )
    and  navigation.getNode   ( o ) )
    then
        if ( not navigation.getTargetNode ( o ) )
        then
            navigation.setRandomTargetNode ( o )
        end
    else
        navigation.setNearestNode ( o, o )
        navigation.setAcceleration( o, 10 ) 

        navigation.setSpeedLimit( o, 10 ) 
        
    end
    
    if ( navigation.getSpeed (o) > 0 )   -- If the object is moving, then do the following...
    then
        local vx, vy, vz = navigation.getVelocity ( o )                    -- Get the object's velocity per xyz.
        local x, y, z = object.getTranslation ( o, object.kGlobalSpace )   -- Get the object's postion in xyz.
        object.lookAtWithUp ( o, x - vx, y - vy, z - vz, 0, 1, 0, object.kGlobalSpace, .05 )   -- Look in the direction it's moving.
    end
    
    --this.walkPlank ( )
    --this.loseWay ( )
    this.doNothing ( )
   -- this.WalkToTarget ( )
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
