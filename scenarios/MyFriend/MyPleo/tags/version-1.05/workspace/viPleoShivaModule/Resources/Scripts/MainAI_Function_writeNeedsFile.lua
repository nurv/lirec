--------------------------------------------------------------------------------
--  Function......... : writeNeedsFile
--  Author........... : Paulo F. Gomes
--  Description...... : Rewrites needs.xml file according to local needs xml
--                      variable.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.writeNeedsFile ( xmlNeedsAbstract )
--------------------------------------------------------------------------------
	
	local sXMLFilePath = application.getPackDirectory ( ).."/files/needs.xml"
    local sXMLFileURI =  "file://"..sXMLFilePath
    -- debug
    -- log.message ( "sXMLFileURI: "..sXMLFileURI )
    local bStoreRequestSuccessfull = xml.send ( xmlNeedsAbstract, sXMLFileURI )
    if ( bStoreRequestSuccessfull )
    then
        this.writeNeedsFileCycle ( xmlNeedsAbstract )
    else
        log.error ( "Unable to store XML")
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
