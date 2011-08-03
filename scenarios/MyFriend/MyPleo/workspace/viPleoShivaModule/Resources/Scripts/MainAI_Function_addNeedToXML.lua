--------------------------------------------------------------------------------
--  Function......... : addNeedToXML
--  Author........... : Paulo F. Gomes
--  Description...... : Adds specific pleo need to local xml variable.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.addNeedToXML ( hNeedsRoot , sNeedName, nNeedValue)
--------------------------------------------------------------------------------
	
    local sNeedValue = ""..nNeedValue
    local hNeedXMLElement = xml.appendElementChild ( hNeedsRoot, "need", "")
    xml.appendElementAttribute ( hNeedXMLElement, "name", sNeedName )
    xml.appendElementAttribute ( hNeedXMLElement, "value", sNeedValue )
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
