--------------------------------------------------------------------------------
--  Handler.......... : onAddNeeds
--  Author........... : Tiago Paiva
--  Description...... : (Probably no longer used)
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.onAddNeeds ( need, newValue )
--------------------------------------------------------------------------------
	
	    local hNeed = application.getCurrentUserEnvironmentVariable ( need )
        
        application.setCurrentUserEnvironmentVariable ( need, hNeed + 5)  
        log.message ( "need adicionada" )
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
