--------------------------------------------------------------------------------
--  Function......... : initializeNeeds
--  Author........... : Paulo F. Gomes
--  Description...... : If needs have already been initialized, does nothing.
--                      If not, tries to initialize need values from needs xml.
--                      If not successful, initializes need values from needs'
--                      initial values in variables. 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.initializeNeeds ( )
--------------------------------------------------------------------------------
	
	if( application.getCurrentUserEnvironmentVariable ( "bNeedsSet" ) == false)
    then

        local bSuccessLoadNeedsXMLFromFile = this.loadNeedsXMLFromFile ( )
        if(bSuccessLoadNeedsXMLFromFile)
        then
            this.loadNeedsFromXML ( )
        else
            this.loadNeedsFromVariables ( )
        end        
        application.setCurrentUserEnvironmentVariable ( "bNeedsSet", true )
        
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
