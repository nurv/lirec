--------------------------------------------------------------------------------
--  Function......... : createXML
--  Author........... : Paulo F. Gomes
--  Description...... : Creates temporary empty xml in local variables.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.createXML ( )
--------------------------------------------------------------------------------
	
	local sNeedsRoot = "<needs></needs>"
    local bSuccess = xml.createFromString ( this.xmlNeeds ( ), sNeedsRoot )
    
    if (bSuccess)
    then
        return this.xmlNeeds ( )
	else
        return nil
    end
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
