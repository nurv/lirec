--------------------------------------------------------------------------------
--  Function......... : storeNeeds
--  Author........... : Paulo F. Gomes
--  Description...... : Stores current needs to xml.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.storeNeeds ( )
--------------------------------------------------------------------------------
	
	local xmlNeedsAbstract = this.createXML ( )
    if( xmlNeedsAbstract )
    then
        local hNeedsRoot = xml.getRootElement ( xmlNeedsAbstract )
        this.addNeedsToXML ( hNeedsRoot )
        -- debug
        -- log.message ( xml.toString ( hNeedsRoot ) )
        
        this.writeNeedsFile ( xmlNeedsAbstract )
    else
        log.error ( "Unable to create XML")
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
