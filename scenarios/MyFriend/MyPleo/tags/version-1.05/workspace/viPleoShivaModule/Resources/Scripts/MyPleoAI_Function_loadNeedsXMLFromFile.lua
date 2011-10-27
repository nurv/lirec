--------------------------------------------------------------------------------
--  Function......... : loadNeedsXMLFromFile
--  Author........... : Paulo F. Gomes
--  Description...... : Loads needs to xml variable from needs xml file.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.loadNeedsXMLFromFile ( )
--------------------------------------------------------------------------------
	
    local bSuccess = false
    local sXMLFilePath = application.getPackDirectory ( ).."/files/needs.xml"
    local sXMLFileURI =  "file://"..sXMLFilePath
    local bLoadRequestSuccessfull = xml.receive ( this.xmlNeeds ( ), sXMLFileURI )
    if ( bLoadRequestSuccessfull )
    then
        bSuccess = this.loadNeedsXMLFromFileCycle ( this.xmlNeeds ( ) )
    else
        log.error ( "Unable to load Needs XML.")
    end
	return bSuccess
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
