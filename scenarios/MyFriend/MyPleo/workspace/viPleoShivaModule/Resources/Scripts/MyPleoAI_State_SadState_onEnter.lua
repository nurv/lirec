--------------------------------------------------------------------------------
--  State............ : SadState
--  Author........... : Tiago Paiva
--  Description...... : (unsure if it is currently being used)
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.SadState_onEnter ( )
--------------------------------------------------------------------------------
    
    local o = this.getObject ( )

    navigation.setNearestTargetNode ( o, this.hBody ( ) )

    object.postEvent ( this.hBody ( ), 10, "MyPleoAI", "onRemove", nil, "walk" )
        
    local hObject = this.hObject ( )
    
   animation.changeClip ( hObject, 0, 8 )
   animation.setPlaybackMode ( hObject, 0, animation.kPlaybackModeLoopMirrored )
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
