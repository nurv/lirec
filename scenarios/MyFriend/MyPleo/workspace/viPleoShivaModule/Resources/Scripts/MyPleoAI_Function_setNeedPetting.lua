--------------------------------------------------------------------------------
--  Function......... : setNeedPetting
--  Author........... : Paulo F. Gomes
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.setNeedPetting ( nValue )
--------------------------------------------------------------------------------
	
    if ( nValue < this.nNeedMin ( ))
    then
        application.setCurrentUserEnvironmentVariable ( "nNeedPetting" , this.nNeedMin ( ) )
    elseif ( nValue > this.nNeedMax ( ))
    then
        application.setCurrentUserEnvironmentVariable ( "nNeedPetting" , this.nNeedMax ( ) )
    else
       application.setCurrentUserEnvironmentVariable ( "nNeedPetting" , nValue ) 
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
