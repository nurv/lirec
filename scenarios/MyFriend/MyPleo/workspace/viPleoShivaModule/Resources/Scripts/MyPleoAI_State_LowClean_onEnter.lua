--------------------------------------------------------------------------------
--  State............ : LowClean
--  Author........... : Tiago Paiva
--  Description...... : (Currently not being used)
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.LowClean_onEnter ( )
--------------------------------------------------------------------------------
    
    local o = this.getObject ( )

    navigation.setNearestTargetNode ( o, this.hBody ( ) )

    object.postEvent ( this.hBody ( ), 5, "MyPleoAI", "onRemove", nil, "walk" )
        
    local hObject = this.hObject ( )
    
   animation.changeClip ( hObject, 0, 4 )
   
   local x,y,z = object.getTranslation ( this.hCamera ( ) , object.kGlobalSpace )

   object.lookAtWithUp ( o, x,0,z, 0,1,0, object.kGlobalSpace, 0.05 )
   

--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
