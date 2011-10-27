--------------------------------------------------------------------------------
--  Function......... : updateAttachmentEstimate
--  Author........... : Paulo F. Gomes
--  Description...... : Updates the attachment estimate calculating a weighted
--                      sum of all needs.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.updateAttachmentEstimate ( )
--------------------------------------------------------------------------------
	
	local nAttachmentEstimate = 0
    nAttachmentEstimate = nAttachmentEstimate + this.getNeedCleanliness ( ) * this.nNeedCleanlinessWeight ( )
    nAttachmentEstimate = nAttachmentEstimate + this.getNeedEnergy ( ) * this.nNeedEnergyWeight ( )
    nAttachmentEstimate = nAttachmentEstimate + this.getNeedPetting ( ) * this.nNeedPettingWeight ( )
    nAttachmentEstimate = nAttachmentEstimate + this.getNeedSkills ( ) * this.nNeedSkillsWeight ( )
    nAttachmentEstimate = nAttachmentEstimate + this.getNeedWater ( ) * this.nNeedWaterWeight ( )
    
    application.setCurrentUserEnvironmentVariable ( "nAttachmentEstimate" , nAttachmentEstimate )
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
