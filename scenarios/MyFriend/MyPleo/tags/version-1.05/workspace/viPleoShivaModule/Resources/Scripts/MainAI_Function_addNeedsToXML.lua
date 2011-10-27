--------------------------------------------------------------------------------
--  Function......... : addNeedsToXML
--  Author........... : Paulo F. Gomes
--  Description...... : Adds current Pleo needs to local xml variable.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.addNeedsToXML ( hNeedsRoot )
--------------------------------------------------------------------------------
	
    local nNeedCleanliness = application.getCurrentUserEnvironmentVariable ( "nNeedCleanliness" )
    local nNeedEnergy = application.getCurrentUserEnvironmentVariable ( "nNeedEnergy" )
    local nNeedPetting = application.getCurrentUserEnvironmentVariable ( "nNeedPetting" )
    local nNeedSkills = application.getCurrentUserEnvironmentVariable ( "nNeedSkills" )
    local nNeedWater = application.getCurrentUserEnvironmentVariable ( "nNeedWater" )
    this.addNeedToXML ( hNeedsRoot, "cleanliness", nNeedCleanliness )
    this.addNeedToXML ( hNeedsRoot, "energy", nNeedEnergy )
    this.addNeedToXML ( hNeedsRoot, "petting", nNeedPetting )
    this.addNeedToXML ( hNeedsRoot, "skills", nNeedSkills )
    this.addNeedToXML ( hNeedsRoot, "water", nNeedWater )
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
