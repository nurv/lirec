--------------------------------------------------------------------------------
--  Function......... : loadNeedsFromXML
--  Author........... : Paulo F. Gomes
--  Description...... : Loads needs from needs xml variable.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.loadNeedsFromXML ( )
--------------------------------------------------------------------------------
	
    local hXMLNeedsRoot = xml.getRootElement ( this.xmlNeeds ( ) )
	if (hXMLNeedsRoot)
    then
        local hNeedElement = xml.getElementFirstChild ( hXMLNeedsRoot )
        while(hNeedElement ~= nil)
        do
            this.loadNeedFromXMLElement ( hNeedElement )
            hNeedElement = xml.getElementNextSibling ( hNeedElement )
        end
    else
        log.error ( "Unable to get root element of XML needs." )
    end
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
