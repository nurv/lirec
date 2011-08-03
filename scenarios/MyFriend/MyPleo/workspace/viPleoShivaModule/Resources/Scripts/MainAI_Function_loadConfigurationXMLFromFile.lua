--------------------------------------------------------------------------------
--  Function......... : loadConfigurationXMLFromFile
--  Author........... : Paulo F. Gomes
--  Description...... : Reads the needs.xml file to a local xml.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.loadConfigurationXMLFromFile ( )
--------------------------------------------------------------------------------
	
    local bSuccess = false
    local sXMLFilePath = application.getPackDirectory ( ).."/files/configuration.xml"
    local sXMLFileURI =  "file://"..sXMLFilePath
    local bLoadRequestSuccessfull = xml.receive ( this.xmlConfiguration ( ), sXMLFileURI )
    if ( bLoadRequestSuccessfull )
    then
        bSuccess = this.loadConfigurationXMLFromFileCycle ( this.xmlConfiguration ( ) )
    else
        log.error ( "Unable to load Configuration XML.")
    end
	return bSuccess
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
