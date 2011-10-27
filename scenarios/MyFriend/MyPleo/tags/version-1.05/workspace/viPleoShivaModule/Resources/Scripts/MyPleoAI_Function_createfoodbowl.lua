--------------------------------------------------------------------------------
--  Function......... : createfoodbowl
--  Author........... : Tiago Paiva
--  Description...... : Place patch of leaves in the playground.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.createfoodbowl ( )
--------------------------------------------------------------------------------
	
    if not this.gotofood ( ) then
    
        local hFoodbowl = scene.createRuntimeObject ( application.getCurrentUserScene ( ), "leaf" )
        object.setTranslation ( hFoodbowl, 3, 0, 7, object.kGlobalSpace ) -- or some random algorithm
        scene.setRuntimeObjectTag ( application.getCurrentUserScene ( ), hFoodbowl, "Foodbowl" )
        
        this.gotofood ( true )
    
    end
    
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
