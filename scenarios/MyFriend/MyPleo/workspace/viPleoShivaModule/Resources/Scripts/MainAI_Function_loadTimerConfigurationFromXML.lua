--------------------------------------------------------------------------------
--  Function......... : loadTimerConfigurationFromXML
--  Author........... : Paulo F. Gomes
--  Description...... : Loads automatic migration configuration from local xml
--                      variable.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.loadTimerConfigurationFromXML ( )
--------------------------------------------------------------------------------
	
    local hXMLConfigurationRoot = xml.getRootElement ( this.xmlConfiguration ( ) )
	if (hXMLConfigurationRoot)
    then
        log.message ( "Configuration xml:" .. xml.toString ( hXMLConfigurationRoot) )
        local hMACAddressConfigurationElement = xml.getElementFirstChild ( hXMLConfigurationRoot )
        local hMACAddressConfigurationCommentElement = xml.getElementNextSibling ( hMACAddressConfigurationElement )
        local hTimerConfigurationElement = xml.getElementNextSibling ( hMACAddressConfigurationCommentElement )
        this.loadTimerConfigurationFromXMLElement ( hTimerConfigurationElement )
    else
        log.error ( "Unable to get root element of XML configuration." )
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
