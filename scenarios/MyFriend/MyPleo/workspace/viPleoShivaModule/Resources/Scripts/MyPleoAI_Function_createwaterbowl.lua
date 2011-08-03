--------------------------------------------------------------------------------
--  Function......... : createwaterbowl
--  Author........... : Tiago Paiva
--  Description...... : Place water bowl in the playground.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.createwaterbowl ( )
--------------------------------------------------------------------------------
	
    if not this.gotowater ( )
    then
        local hWaterbowl = scene.createRuntimeObject ( application.getCurrentUserScene ( ), "bowl_water" )
        object.setTranslation ( hWaterbowl, 7, 0, 3, object.kGlobalSpace ) -- or some random algorithm
        scene.setRuntimeObjectTag ( application.getCurrentUserScene ( ), hWaterbowl, "Waterbowl" )
        this.gotowater ( true )
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
