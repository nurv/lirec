--------------------------------------------------------------------------------
--  Function......... : setupTimer
--  Author........... : Paulo F. Gomes
--  Description...... : Starts, or not, a timer for automatic migration
--                      according to the configuration xml.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.setupTimer ( )
--------------------------------------------------------------------------------
	
   local bSuccessLoadConfigurationXMLFromFile = this.loadConfigurationXMLFromFile ( );
   if(bSuccessLoadConfigurationXMLFromFile)
   then
       this.loadTimerConfigurationFromXML ( )
   end	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
