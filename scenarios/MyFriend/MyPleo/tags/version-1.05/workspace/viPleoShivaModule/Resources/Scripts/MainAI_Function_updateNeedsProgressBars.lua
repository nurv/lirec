--------------------------------------------------------------------------------
--  Function......... : updateNeedsProgressBars
--  Author........... : Paulo F. Gomes
--  Description...... : Updates needs' progress bars according to current need
--                      values.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.updateNeedsProgressBars ( )
--------------------------------------------------------------------------------
	
    local nNeedCleanliness = application.getCurrentUserEnvironmentVariable ( "nNeedCleanliness" )
    local nNeedEnergy = application.getCurrentUserEnvironmentVariable ( "nNeedEnergy" )
    local nNeedPetting = application.getCurrentUserEnvironmentVariable ( "nNeedPetting" )
    local nNeedSkills = application.getCurrentUserEnvironmentVariable ( "nNeedSkills" )
    local nNeedWater = application.getCurrentUserEnvironmentVariable ( "nNeedWater" )

    local hCurrentUser = application.getCurrentUser ( )
    local hNeedCleanlinessProgressBar = hud.getComponent ( hCurrentUser, "myHUD.Clean_Progress" )
    local hNeedEnergyProgressBar = hud.getComponent ( hCurrentUser, "myHUD.Energy_Progress" )
    local hNeedPettingProgressBar = hud.getComponent ( hCurrentUser, "myHUD.Petting_Progress" )
    local hNeedSkillsProgressBar = hud.getComponent ( hCurrentUser, "myHUD.Chasing_Progress" )
    local hNeedWaterProgressBar = hud.getComponent ( hCurrentUser, "myHUD.Water_Progress" )
    
    -- Progress bar values range from 0 to 255 while need values range from 0 to 100.
    -- Multiplying by nNeedToProgressBarCoefficient converts need values to progress bar values.
    local nNeedToProgressBarCoefficient = application.getCurrentUserEnvironmentVariable ( "nNeedToProgressBarCoefficient" )
    hud.setProgressValue ( hNeedCleanlinessProgressBar, nNeedCleanliness * nNeedToProgressBarCoefficient )
    hud.setProgressValue ( hNeedEnergyProgressBar, nNeedEnergy * nNeedToProgressBarCoefficient )
    hud.setProgressValue ( hNeedPettingProgressBar, nNeedPetting * nNeedToProgressBarCoefficient )
    hud.setProgressValue ( hNeedSkillsProgressBar, nNeedSkills * nNeedToProgressBarCoefficient )
    hud.setProgressValue ( hNeedWaterProgressBar, nNeedWater * nNeedToProgressBarCoefficient )
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
