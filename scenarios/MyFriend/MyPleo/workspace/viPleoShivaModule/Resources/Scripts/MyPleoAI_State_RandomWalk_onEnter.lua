--------------------------------------------------------------------------------
--  State............ : RandomWalk
--  Author........... : Tiago Paiva
--  Description...... : Default state in which walks around randomly in the
--                      playground.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.RandomWalk_onEnter ( )
--------------------------------------------------------------------------------
    
    local hObject = this.hObject ( )
    
    animation.setPlaybackLevel ( hObject, 0, 1 )
    
   animation.changeClip ( hObject, 0, 0 )
   animation.setPlaybackMode ( hObject, 0, animation.kPlaybackModeLoop ) 

--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
