--------------------------------------------------------------------------------
--  Handler.......... : onInit
--  Author........... : Tiago Paiva
--  Description...... : Handler for AI startup. Focuses camera on Pleo and
--                      hides/shows appropriate menus.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI_Game.onInit (  )
--------------------------------------------------------------------------------
	
    local hO = this.hObject ( )

    log.message ( "GAME INIT" )
	this.hBody ( this.getObject ( ) ) 
	this.hObject ( application.getCurrentUserSceneTaggedObject ( "Mesh" ) )
	this.hCamera ( application.getCurrentUserActiveCamera ( ) )
    
    local x, y, z = object.getTranslation ( this.hBody ( ), object.kGlobalSpace )
    object.setTranslation ( this.hObject ( ), x,y,z, object.kGlobalSpace )
    
    local hUser = application.getCurrentUser ( )
    local c1 = hud.getComponent ( hUser, "myHIT.bar_prog" )
    hud.setComponentVisible ( c1, true )
    local menu = hud.getComponent ( hUser, "myHUD.Menu" )
    hud.setComponentVisible ( menu, false )
    
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
