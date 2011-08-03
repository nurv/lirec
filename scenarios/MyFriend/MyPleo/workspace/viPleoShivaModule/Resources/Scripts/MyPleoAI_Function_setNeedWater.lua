--------------------------------------------------------------------------------
--  Function......... : setNeedWater
--  Author........... : Paulo F. Gomes
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.setNeedWater ( nValue )
--------------------------------------------------------------------------------
	
    if ( nValue < this.nNeedMin ( ))
    then
        application.setCurrentUserEnvironmentVariable ( "nNeedWater" , this.nNeedMin ( ) )
    elseif ( nValue > this.nNeedMax ( ))
    then
        application.setCurrentUserEnvironmentVariable ( "nNeedWater" , this.nNeedMax ( ) )
    else
        application.setCurrentUserEnvironmentVariable ( "nNeedWater" , nValue ) 
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
