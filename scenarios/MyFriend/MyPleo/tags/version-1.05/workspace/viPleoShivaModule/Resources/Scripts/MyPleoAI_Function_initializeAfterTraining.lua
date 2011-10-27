--------------------------------------------------------------------------------
--  Function......... : initializeAfterTraining
--  Author........... : Paulo F. Gomes
--  Description...... : Initializations for when Pleo has returned from the
--                      training course.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.initializeAfterTraining ( )
--------------------------------------------------------------------------------
	
    this.setNeedSkills ( this.getNeedSkills ( ) + this.nNeedSkillsIncreaseTraining ( ))
    
    local hUser = application.getCurrentUser ( )
    local menu = hud.getComponent ( hUser, "myHUD.Menu" )
    hud.setComponentVisible ( menu, true )
    
    application.setCurrentUserEnvironmentVariable ( "bTrainingFinished", false )
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
