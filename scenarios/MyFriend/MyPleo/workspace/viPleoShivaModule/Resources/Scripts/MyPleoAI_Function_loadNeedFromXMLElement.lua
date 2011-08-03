--------------------------------------------------------------------------------
--  Function......... : loadNeedFromXMLElement
--  Author........... : Paulo F. Gomes
--  Description...... : Load need from xml variable.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.loadNeedFromXMLElement ( hNeedElement )
--------------------------------------------------------------------------------
	
    local hNeedNameAttribute = xml.getElementAttributeWithName ( hNeedElement, "name")
    local sNeedName = xml.getAttributeValue ( hNeedNameAttribute )
    local hNeedValueAttribute = xml.getElementAttributeWithName ( hNeedElement, "value")
    local sNeedValue = xml.getAttributeValue ( hNeedValueAttribute )
    local nNeedValue = string.toNumber ( sNeedValue )
    this.setNeed ( sNeedName , nNeedValue )
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
