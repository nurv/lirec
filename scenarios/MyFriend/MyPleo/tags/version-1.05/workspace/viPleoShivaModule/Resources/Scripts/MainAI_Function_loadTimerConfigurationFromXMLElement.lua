--------------------------------------------------------------------------------
--  Function......... : loadTimerConfigurationFromXMLElement
--  Author........... : Paulo F. Gomes
--  Description...... : Loads automatic migration values from local xml
--                      variable.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.loadTimerConfigurationFromXMLElement ( hConfigurationElement )
--------------------------------------------------------------------------------
	
    local hTimerConfigurationDurationAttribute = xml.getElementAttributeWithName ( hConfigurationElement, "duration")
    local sTimerDuration = xml.getAttributeValue ( hTimerConfigurationDurationAttribute )
    local nTimerDurationMinutesTemp = string.toNumber ( sTimerDuration )
    log.message ( "Timer Duration: " .. nTimerDurationMinutesTemp)
    
    local hTimerActiveAttribute = xml.getElementAttributeWithName ( hConfigurationElement, "active")
    local sTimerActive = xml.getAttributeValue ( hTimerActiveAttribute )
    local bTimerActiveTemp
    if ( string.compare ( sTimerActive, "true" ) == 0)
    then
        bTimerActiveTemp = true;
        log.message ( "Timer Active: True" )
    else
        bTimerActiveTemp = false
        log.message ( "Timer Active: False" )
    end
    
    this.nTimerDurationMinutes ( nTimerDurationMinutesTemp )
    this.bTimerActive ( bTimerActiveTemp )
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
