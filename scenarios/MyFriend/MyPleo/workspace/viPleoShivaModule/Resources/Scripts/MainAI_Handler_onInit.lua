--------------------------------------------------------------------------------
--  Handler.......... : onInit
--  Author........... : Paulo F. Gomes
--  Description...... : Handler for the startup event. Orients screen, loads
--                      main scene and starts the automatic migration if needed.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.onInit (  )
--------------------------------------------------------------------------------
    
    log.message ( "INIT MAIN AI" )
    
    this.setupScreenOrientation ( )
    this.startPlayground ( )
    this.setupTimer ( )
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
