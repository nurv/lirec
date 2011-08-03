--------------------------------------------------------------------------------
--  State............ : Drinking
--  Author........... : Tiago Paiva
--  Description...... : Pleo lowers neck and drinks water from bowl.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.Drinking_onEnter ( )
--------------------------------------------------------------------------------
	    
    object.postEvent ( this.hBody ( ), 5, "MyPleoAI", "onRemove", "Waterbowl", "walk" )
        
    local hObject = this.hObject ( )
    
   animation.changeClip ( hObject, 0, 2 )
   animation.setPlaybackMode ( hObject, 0, animation.kPlaybackModeLoopMirrored )
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
