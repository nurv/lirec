--------------------------------------------------------------------------------
--  State............ : doNothing
--  Author........... : Tiago Paiva
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI_Game.doNothing_onLoop ( )
--------------------------------------------------------------------------------

    	local o = this.getObject ( )

	 local vx, vy, vz = navigation.getVelocity ( o )                    -- Get the object's velocity per xyz.
    local x, y, z = object.getTranslation ( o, object.kGlobalSpace )   -- Get the object's postion in xyz.
 
    --log.message ( "MUDAR VISAO" )
       object.lookAtWithUp ( o, x +vx, y +vy, z +vz, 0, 1, 0, object.kGlobalSpace, .05 )
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
