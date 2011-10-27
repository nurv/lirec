--------------------------------------------------------------------------------
--  Handler.......... : onButtonPressed
--  Author........... : Tiago Paiva
--  Description...... : Handler for the event of pressing the red button on the
--                      obstacle game course ?? (unsure about effect)
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.onButtonPressed ( botao )
--------------------------------------------------------------------------------
	
	    local obj = application.getCurrentUserSceneTaggedObject ( "MyCharacter" )
        local bCar= application.getCurrentUserEnvironmentVariable ( "bCarregou")
        application.setCurrentUserEnvironmentVariable ( "bCarregou", 15 )

        object.postEvent ( obj, 0, "MyPleoAI_Game", "onPressedButton", botao )
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
