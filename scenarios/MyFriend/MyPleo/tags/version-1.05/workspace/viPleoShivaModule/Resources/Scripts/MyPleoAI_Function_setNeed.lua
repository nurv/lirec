--------------------------------------------------------------------------------
--  Function......... : setNeed
--  Author........... : Paulo F. Gomes
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.setNeed ( sNeedName, nValue )
--------------------------------------------------------------------------------
	
	if ( string.compare ( sNeedName, "cleanliness" ) == 0 )
    then
       this.setNeedCleanliness ( nValue ) 
       
    elseif ( string.compare ( sNeedName, "energy" ) == 0)
    then
        this.setNeedEnergy ( nValue )
        
    elseif ( string.compare ( sNeedName, "petting" ) == 0)
    then
        this.setNeedPetting ( nValue )
        
    elseif ( string.compare ( sNeedName, "skills" ) == 0)
    then
        this.setNeedSkills ( nValue )
        
    elseif ( string.compare ( sNeedName, "water" ) == 0)
    then
        this.setNeedWater ( nValue )
    else
        log.error ( "Trying to set invalid need: " + sNeedName )
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
