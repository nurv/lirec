--------------------------------------------------------------------------------
--  Function......... : setNeedCleanliness
--  Author........... : Paulo F. Gomes
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.setNeedCleanliness ( nValue )
--------------------------------------------------------------------------------
    
    if ( nValue < this.nNeedMin ( ))
    then
        application.setCurrentUserEnvironmentVariable ( "nNeedCleanliness" , this.nNeedMin ( ) )
    elseif ( nValue > this.nNeedMax ( ))
    then
        application.setCurrentUserEnvironmentVariable ( "nNeedCleanliness" , this.nNeedMax ( ) )
    else
       application.setCurrentUserEnvironmentVariable ( "nNeedCleanliness" , nValue ) 
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
