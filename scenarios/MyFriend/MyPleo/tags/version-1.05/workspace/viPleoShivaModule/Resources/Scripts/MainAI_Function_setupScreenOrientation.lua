--------------------------------------------------------------------------------
--  Function......... : setupScreenOrientation
--  Author........... : Paulo F. Gomes
--  Description...... : If the Operative System is Android, rotates the view to
--                      to match the device's screen.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.setupScreenOrientation ( )
--------------------------------------------------------------------------------
	
	local OSType = system.getOSType()
    
    if(OSType == system.kOSTypeAndroid)
    then
        application.setOption ( application.kOptionViewportRotation, 3 )
    else 
        application.setOption ( application.kOptionViewportRotation, 0 )
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
