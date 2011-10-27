--------------------------------------------------------------------------------
--  Function......... : printNeeds
--  Author........... : Paulo F. Gomes
--  Description...... : Prints all needs to log (Used for debugging purposes
--                      only)
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.printNeeds ( )
--------------------------------------------------------------------------------
	
    local nNeedCleanliness = application.getCurrentUserEnvironmentVariable ( "nNeedCleanliness" )
    local nNeedEnergy = application.getCurrentUserEnvironmentVariable ( "nNeedEnergy" )
    local nNeedPetting = application.getCurrentUserEnvironmentVariable ( "nNeedPetting" )
    local nNeedSkills = application.getCurrentUserEnvironmentVariable ( "nNeedSkills" )
    local nNeedWater = application.getCurrentUserEnvironmentVariable ( "nNeedWater" )
    
    log.message ( "nNeedCleanliness: "..nNeedCleanliness)
    log.message ( "nNeedEnergy: "..nNeedEnergy)
    log.message ( "nNeedPetting: "..nNeedPetting)
    log.message ( "nNeedSkills: "..nNeedSkills)
    log.message ( "nNeedWater: "..nNeedWater)
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
