--------------------------------------------------------------------------------
--  State............ : RandomWalk
--  Author........... : Tiago Paiva
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI_Game.RandomWalk_onEnter ( )
--------------------------------------------------------------------------------
	
	log.message ( "entrei no randomwalk" )
    
    local hObject = this.hObject ( )
    
    
   animation.changeClip ( hObject, 0, 3 )
   animation.setPlaybackMode ( hObject, 0, animation.kPlaybackModeLoop ) 

--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
