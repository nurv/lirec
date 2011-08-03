--------------------------------------------------------------------------------
--  Function......... : createstick
--  Author........... : Tiago Paiva
--  Description...... : Place playing stick in the playground.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.createstick ( )
--------------------------------------------------------------------------------
	
  
    local hStick = scene.createRuntimeObject ( application.getCurrentUserScene ( ), "stick" )
    object.setTranslation ( hStick, 0, 0, 7, object.kGlobalSpace ) -- or some random algorithm
    scene.setRuntimeObjectTag ( application.getCurrentUserScene ( ), hStick, "Stick" )
        
    this.gotstick ( true )

--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
