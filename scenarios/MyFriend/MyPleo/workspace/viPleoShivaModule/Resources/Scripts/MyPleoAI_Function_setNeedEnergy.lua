--------------------------------------------------------------------------------
--  Function......... : setNeedEnergy
--  Author........... : Paulo F. Gomes
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.setNeedEnergy ( nValue )
--------------------------------------------------------------------------------
	
    if ( nValue < this.nNeedMin ( ))
    then
        application.setCurrentUserEnvironmentVariable ( "nNeedEnergy" , this.nNeedMin ( ) )
    elseif ( nValue > this.nNeedMax ( ))
    then
        application.setCurrentUserEnvironmentVariable ( "nNeedEnergy" , this.nNeedMax ( ) )
    else
       application.setCurrentUserEnvironmentVariable ( "nNeedEnergy" , nValue ) 
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
