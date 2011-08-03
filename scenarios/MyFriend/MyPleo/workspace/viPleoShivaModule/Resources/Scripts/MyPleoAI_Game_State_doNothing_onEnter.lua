--------------------------------------------------------------------------------
--  State............ : doNothing
--  Author........... : Tiago Paiva
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI_Game.doNothing_onEnter ( )
--------------------------------------------------------------------------------
	 
    local hObject = this.hObject ( )
    
    log.message ( "PARADOOOO" )
        	local o = this.getObject ( )
 if ( navigation.getSpeed (o) == 0 )   -- If the object is moving, then do the following...
    then
   animation.changeClip ( hObject, 0, 5 )
   animation.setPlaybackMode ( hObject, 0, animation.kPlaybackModeLoop ) 

end
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
