--------------------------------------------------------------------------------
--  State............ : Eating
--  Author........... : Tiago Paiva
--  Description...... : Pleo lowers neck and eats leaves.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.Eating_onEnter ( )
--------------------------------------------------------------------------------
    
    object.postEvent ( this.hBody ( ), 3, "MyPleoAI", "onRemove", "Foodbowl", "walk" )
        
    local hObject = this.hObject ( )
    
   animation.changeClip ( hObject, 0, 2 )
   animation.setPlaybackMode ( hObject, 0, animation.kPlaybackModeLoopMirrored )
    
    sound.play ( this.hBody ( ), 0, 100, false, 1 )
    
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
