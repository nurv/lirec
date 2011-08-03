--------------------------------------------------------------------------------
--  Function......... : notifyMainAIUpdateNeeds
--  Author........... : Paulo F. Gomes
--  Description...... : Notifies MainAI that needs and attachment estimate were
--                      updated.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.notifyMainAIUpdateNeeds ( )
--------------------------------------------------------------------------------
	
    local hCurrentUser = application.getCurrentUser ( )
    user.sendEvent ( hCurrentUser, "MainAI", "onNeedStatsUpdate")
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
