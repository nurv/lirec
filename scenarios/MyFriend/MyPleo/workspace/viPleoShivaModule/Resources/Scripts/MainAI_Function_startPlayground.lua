--------------------------------------------------------------------------------
--  Function......... : startPlayground
--  Author........... : Tiago Paiva
--  Description...... : Loads main scene and interfaces (myHIT will be hidden
--                      later).
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.startPlayground ( )
--------------------------------------------------------------------------------
	
	hud.newTemplateInstance ( this.getUser ( ), "Menu_Accoes", "myHUD" )
    application.setCurrentUserScene ( "Cena_Principal" )
    hud.newTemplateInstance ( this.getUser ( ), "HUD_Game_Menu", "myHIT" )
    
    music.play ( application.getCurrentUserScene ( ), 2, 5 )
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
