--------------------------------------------------------------------------------
--  Function......... : createball
--  Author........... : Tiago Paiva
--  Description...... : Place ball in the playground.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.createball ( )
--------------------------------------------------------------------------------
	
        local hBall = scene.createRuntimeObject ( application.getCurrentUserScene ( ), "Sphere" )
        object.setTranslation ( hBall, 3, 0, 7, object.kGlobalSpace ) -- or some random algorithm
        scene.setRuntimeObjectTag ( application.getCurrentUserScene ( ), hBall, "Bola" )
        
        this.gotball ( true )
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
