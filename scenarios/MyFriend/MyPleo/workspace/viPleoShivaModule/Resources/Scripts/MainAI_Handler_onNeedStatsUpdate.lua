--------------------------------------------------------------------------------
--  Handler.......... : onNeedStatsUpdate
--  Author........... : Paulo F. Gomes
--  Description...... : Updates attachment and needs' progress bars if the bars
--                      are visible.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.onNeedStatsUpdate (  )
--------------------------------------------------------------------------------
	
    this.tryToUpdateNeedsProgressBars ( )
    this.tryToUpdateAttachmentEstimateProgressBar ( )
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
