--------------------------------------------------------------------------------
--  Function......... : loadNeedsFromVariables
--  Author........... : Paulo F. Gomes
--  Description...... : Load needs from needs' initial values in variables.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.loadNeedsFromVariables ( )
--------------------------------------------------------------------------------
	
    this.setNeedCleanliness ( this.nNeedCleanlinessInitial ( ) ) 
    this.setNeedEnergy ( this.nNeedEnergyInitial ( ) ) 
    this.setNeedPetting ( this.nNeedPettingInitial ( ) ) 
    this.setNeedSkills ( this.nNeedSkillsInitial ( ) ) 
    this.setNeedWater ( this.nNeedWaterInitial ( ) ) 
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
