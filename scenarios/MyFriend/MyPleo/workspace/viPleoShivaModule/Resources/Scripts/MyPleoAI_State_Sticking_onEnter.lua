--------------------------------------------------------------------------------
--  State............ : Sticking
--  Author........... : Tiago Paiva
--  Description...... : (currently not being used)
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.Sticking_onEnter ( )
--------------------------------------------------------------------------------
	
    object.postEvent ( this.hBody ( ), 3, "MyPleoAI", "onRemove", nil , "walk" )

    object.postEvent ( this.hBody ( ), 15, "MyPleoAI", "onRemove", "Stick", nil )
        
    local hO = this.hObject ( )
    
   animation.changeClip ( hO, 0, 1 )
   animation.setPlaybackMode ( hO, 0, animation.kPlaybackModeLoopMirrored )
   
    sound.play ( this.hBody ( ), 5, 50, false, 0 )

--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
