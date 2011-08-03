--------------------------------------------------------------------------------
--  Function......... : tryToUpdateAttachmentEstimateProgressBar
--  Author........... : Paulo F. Gomes
--  Description...... : Updates attachment progress bar if the bar is visible.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.tryToUpdateAttachmentEstimateProgressBar ( )
--------------------------------------------------------------------------------
	
	local hCurrentUser = application.getCurrentUser ( )
    local hAttachmentEstimateProgressBar =  hud.getComponent ( hCurrentUser , "myHUD.Stats_Barra_Menu")
    if (  hAttachmentEstimateProgressBar )
    then
        local bAttachmentEstimateProgressBarVisible = hud.isComponentVisible ( hAttachmentEstimateProgressBar )
    
        if ( bAttachmentEstimateProgressBarVisible )
        then
            local nAttachmentEstimate = application.getCurrentUserEnvironmentVariable ( "nAttachmentEstimate" )
            local nNeedToProgressBarCoefficient = application.getCurrentUserEnvironmentVariable ( "nNeedToProgressBarCoefficient" )
            hud.setProgressValue ( hAttachmentEstimateProgressBar, nAttachmentEstimate * nNeedToProgressBarCoefficient)
        end
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
