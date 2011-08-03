--------------------------------------------------------------------------------
--  Handler.......... : onUpdateNeeds
--  Author........... : Paulo F. Gomes
--  Description...... : Handler for the event update needs. Interval between
--                      updates is defined by nNeedsTimerInterval.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.onUpdateNeeds (  )
--------------------------------------------------------------------------------

	this.decayNeedVariables ( )
    this.updateAttachmentEstimate ( )
    this.notifyMainAIUpdateNeeds ( )
    this.checkNeedActivation ( )
    this.resetUpdateNeedsTimer ( )
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
