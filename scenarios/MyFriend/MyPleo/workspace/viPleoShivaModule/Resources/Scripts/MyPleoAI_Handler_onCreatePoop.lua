--------------------------------------------------------------------------------
--  Handler.......... : onCreatePoop
--  Authors.......... : Tiago Paiva and Paulo F. Gomes
--  Description...... : Handler for the event of creating a poop. This event is
--                      called nCreatePoopDelay seconds after a leaf is eaten.
--                      Only creates poop if the playground does not have
--                      already a poop.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.onCreatePoop (  )
--------------------------------------------------------------------------------
	
	if (not this.bPoopPresent ( ))
    then
        local x,y,z = object.getTranslation ( this.hObject ( ), object.kGlobalSpace )
        
        local poop = scene.createRuntimeObject ( application.getCurrentUserScene ( ), "shit" )
        object.setTranslation ( poop, x, y, z, object.kGlobalSpace )
        scene.setRuntimeObjectTag ( application.getCurrentUserScene ( ), poop, "Poop" )
        
        this.setNeedCleanliness ( this.getNeedCleanliness ( ) - this.nNeedCleanlinessDecreasePoop ( ))
        
        this.bPoopPresent ( true )
    end
    
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
