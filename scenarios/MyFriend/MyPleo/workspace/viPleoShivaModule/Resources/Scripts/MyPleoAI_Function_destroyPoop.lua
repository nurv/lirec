--------------------------------------------------------------------------------
--  Function......... : destroyPoop
--  Authors.......... : Tiago Paiva and Paulo F. Gomes
--  Description...... : Removes poop from scenario and updates the cleanliness
--                      variable accordingly.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.destroyPoop ( )
--------------------------------------------------------------------------------
	
    if this.bPoopPresent ( ) then
        local poop = scene.getTaggedObject ( application.getCurrentUserScene ( ) , "Poop" )

        scene.destroyRuntimeObject ( application.getCurrentUserScene ( ), poop )
        this.setNeedCleanliness ( this.getNeedCleanliness ( ) + this.nNeedCleanlinessIncreaseCleanPoop ( ))
        this.bPoopPresent ( false )
        this.gotstate (false )
        
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
