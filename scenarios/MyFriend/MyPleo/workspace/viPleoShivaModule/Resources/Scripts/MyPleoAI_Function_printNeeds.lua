--------------------------------------------------------------------------------
--  Function......... : printNeeds
--  Author........... : Paulo F. Gomes
--  Description...... : Prints need values to log (used for debugging
--                      purposes only).
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.printNeeds ( )
--------------------------------------------------------------------------------
	
	log.message ( "Cleanliness: "..this.getNeedCleanliness ( ).." Energy: "..this.getNeedEnergy ( ).." Petting: "..this.getNeedPetting ().." Skills: "..this.getNeedSkills ( ).." Water: "..this.getNeedWater ( ))
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
