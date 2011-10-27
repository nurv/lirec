--------------------------------------------------------------------------------
--  State............ : loseWay
--  Author........... : Tiago Paiva
--  Description...... : (unsure if it is currently being used)
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI_Game.loseWay_onLoop ( )
--------------------------------------------------------------------------------
	
	--
	-- Write your code here, using 'this' as current AI instance.
	--
	local o = this.getObject ( )

    navigation.setSpeedLimit( o, 40 ) 
    navigation.setAcceleration ( o, 40 )
    log.message ( "LOSEWAY" )
    
    if ( object.hasController ( o, object.kControllerTypeNavigation ) and  navigation.getNode   ( o ) )
    then
        navigation.setNearestTargetNode ( o, application.getCurrentUserSceneTaggedObject ( "sair2" ) )
    end
    
    local vx, vy, vz = navigation.getVelocity ( o )                    -- Get the object's velocity per xyz.
    local x, y, z = object.getTranslation ( o, object.kGlobalSpace )   -- Get the object's postion in xyz.
    object.lookAtWithUp ( o, x - vx, y - vy, z - vz, 0, 1, 0, object.kGlobalSpace, .05 )   -- Look in the direction it's moving.
    
   -- this.doNothing ( )
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
