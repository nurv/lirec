--------------------------------------------------------------------------------
--  State............ : NoWalk
--  Author........... : Tiago Paiva
--  Description...... : (unsure if it is currently being used)
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.NoWalk_onLoop ( )
--------------------------------------------------------------------------------
	
    local o = this.getObject ( )
     -- Looks at where is walking to.
    if ( navigation.getSpeed (o) > 0 )   -- If the object is moving, then do the following...
    then
        local vx, vy, vz = navigation.getVelocity ( o )                    -- Get the object's velocity per xyz.
        local x, y, z = object.getTranslation ( o, object.kGlobalSpace )   -- Get the object's postion in xyz.
        object.lookAtWithUp ( o, x - vx, y - vy, z - vz, 0, 1, 0, object.kGlobalSpace, .05 )   -- Look in the direction it's moving.
    else
        local o = application.getCurrentUserSceneTaggedObject ( "Mesh" ) 

        -- olhar para a camera
        log.message ( "ESTOU PARADO" )
       --- animation.setPlaybackLevel ( o, 0, 0 )
       
        local action = this.sAction ( )
        
        --- mudar estado para a accao correspondente
        if (action == "eat")
        then
            local hScene = application.getCurrentUserScene ( )
            if( hScene  ~= nil)
            then
            local hObject = scene.createRuntimeObject ( hScene, "bowl" )
             scene.setRuntimeObjectTag ( hScene, hObject, "Food" )
             local dummy = scene.createRuntimeObject ( hScene, "dummy" ) 
             local x,y,z = object.getTranslation ( this.getObject ( ), object.kGlobalSpace )
             
            local bowl = application.getCurrentUserSceneTaggedObject ( "Food" ) 
        
             local x1,y1,z1 = object.getTranslation ( dummy, object.kGlobalSpace )
             log.message ( x1 )
             log.message ( y1 )
             log.message ( z1 )
                --object.matchTranslation ( dummy, hObject, object.kGlobalSpace ) 
            if (x > 0 )
            then
                    object.translate ( dummy, 17, y, 0, object.kGlobalSpace ) 
                    object.translate (bowl, 26, 0, 0, object.kGlobalSpace)
            else
                    object.translate ( dummy, -17, y, 0, object.kGlobalSpace ) 
                    object.translate (bowl, -26, 0, 0, object.kGlobalSpace)

            end
            
            
            
             if( dummy ~= nil)
             then
                 this.oComida( dummy )
             end
            end
         
        this.sendStateChange ( "Eating" )
        end 
    end
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
