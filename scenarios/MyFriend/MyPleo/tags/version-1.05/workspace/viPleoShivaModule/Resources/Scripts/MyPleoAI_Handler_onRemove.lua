--------------------------------------------------------------------------------
--  Handler.......... : onRemove
--  Author........... : Tiago Paiva
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.onRemove ( objtag, newaction )
--------------------------------------------------------------------------------
	
    if(objtag ~= nil)
    then
        scene.destroyRuntimeObject ( application.getCurrentUserScene ( ), scene.getTaggedObject ( application.getCurrentUserScene ( ), objtag ) )
    end
    
    if newaction == "walk" then
        this.RandomWalk ( )
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
