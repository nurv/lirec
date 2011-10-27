--------------------------------------------------------------------------------
--  Function......... : decayNeedVariables
--  Author........... : Paulo F. Gomes
--  Description...... : Decrease need values according to respective decays.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.decayNeedVariables ( )
--------------------------------------------------------------------------------
	
    this.setNeedCleanliness( this.getNeedCleanliness ( ) - this.nNeedCleanlinessDecay ( ))
    this.setNeedEnergy( this.getNeedEnergy ( ) - this.nNeedEnergyDecay ( ))
    this.setNeedPetting( this.getNeedPetting ( ) - this.nNeedPettingDecay ( ))
    this.setNeedSkills( this.getNeedSkills ( ) - this.nNeedSkillsDecay ( ))
    this.setNeedWater( this.getNeedWater ( ) - this.nNeedWaterDecay ( ))
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
