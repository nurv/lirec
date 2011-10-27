--------------------------------------------------------------------------------
--  Handler.......... : onPressExitButton
--  Author........... : Paulo F. Gomes
--  Description...... : Handler that quits the application
--                      (see onApplicationWillQuit).
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.onPressExitButton (  )
--------------------------------------------------------------------------------
	
    -- local hMigrateButton = hud.getComponent ( this.getUser ( ), "myHUD.Migrate_Button" )
    -- local hExitNoMigrationButton = hud.getComponent ( this.getUser ( ), "myHUD.Exit_No_Migration_Button" )
    -- hud.setComponentVisible ( hMigrateButton, true )
    -- hud.setComponentVisible ( hExitNoMigrationButton, true )
	
    application.quit ( )
    
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
