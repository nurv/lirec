--------------------------------------------------------------------------------
--  State............ : Washing
--  Author........... : Tiago Paiva
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.Washing_onEnter ( )
--------------------------------------------------------------------------------
    
    local o = this.getObject ( )

    navigation.setNearestTargetNode ( o, this.hBody ( ) )

    object.postEvent ( this.hBody ( ), 2, "MyPleoAI", "onRemove", nil, "walk" )
        
    local hObject = this.hObject ( )
    
    
   animation.changeClip ( hObject, 0, 7 )
   animation.setPlaybackMode ( hObject, 0, animation.kPlaybackModeLoopMirrored )
   
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
