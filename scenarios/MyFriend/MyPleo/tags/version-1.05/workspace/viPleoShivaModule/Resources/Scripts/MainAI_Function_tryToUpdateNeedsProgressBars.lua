--------------------------------------------------------------------------------
--  Function......... : tryToUpdateNeedsProgressBars
--  Author........... : Paulo F. Gomes
--  Description...... : Updates needs' progress bars if the bars are visible.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.tryToUpdateNeedsProgressBars ( )
--------------------------------------------------------------------------------
	
    local hCurrentUser = application.getCurrentUser ( )
    local hNeedEnergyProgressBar =  hud.getComponent ( hCurrentUser , "myHUD.Energy_Progress")
    if (  hNeedEnergyProgressBar )
    then
        local bNeedStatsVisible = hud.isComponentVisible ( hNeedEnergyProgressBar )
    
        if ( bNeedStatsVisible )
        then
            this.updateNeedsProgressBars ( )
        end
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
