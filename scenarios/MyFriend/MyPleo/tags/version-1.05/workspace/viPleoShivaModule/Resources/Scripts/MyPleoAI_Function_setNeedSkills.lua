--------------------------------------------------------------------------------
--  Function......... : setNeedSkills
--  Author........... : Paulo F. Gomes
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.setNeedSkills ( nValue)
--------------------------------------------------------------------------------
	
    if ( nValue < this.nNeedMin ( ))
    then
        application.setCurrentUserEnvironmentVariable ( "nNeedSkills" , this.nNeedMin ( ) )
    elseif ( nValue > this.nNeedMax ( ))
    then
        application.setCurrentUserEnvironmentVariable ( "nNeedSkills" , this.nNeedMax ( ) )
    else
       application.setCurrentUserEnvironmentVariable ( "nNeedSkills" , nValue ) 
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
