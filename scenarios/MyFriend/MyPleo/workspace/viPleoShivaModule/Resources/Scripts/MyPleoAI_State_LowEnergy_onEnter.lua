--------------------------------------------------------------------------------
--  State............ : LowEnergy
--  Author........... : Tiago Paiva
--  Description...... : State in which duw to low energy Pleo sits down.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.LowEnergy_onEnter ( )
--------------------------------------------------------------------------------
    
    local o = this.getObject ( )

    navigation.setNearestTargetNode ( o, this.hBody ( ) )

    object.postEvent ( this.hBody ( ), 5, "MyPleoAI", "onRemove", nil, "walk" )
        
    local hObject = this.hObject ( )
    
   animation.changeClip ( hObject, 0, 1 )
   animation.setPlaybackMode ( hObject, 0, animation.kPlaybackModeLoopMirrored )
   
   local x,y,z = object.getTranslation ( this.hCamera ( ) , object.kGlobalSpace )

   object.lookAtWithUp ( o, x,0,z, 0,1,0, object.kGlobalSpace, 0.05 )
   sound.play ( this.hBody ( ), 3, 100, true, 1 )

--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
