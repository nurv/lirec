--------------------------------------------------------------------------------
--  Handler.......... : onPressedButton
--  Author........... : Tiago Paiva
--  Description...... : Handler for when the user manages to hit the red button.
--                      Pleo walks to 'next location' and 'next location' is
--                      updated.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI_Game.onPressedButton ( botao )
--------------------------------------------------------------------------------
	
    this.hPvalue (botao)
    
    if(this.hTgt ( ) == "tgt3")
    then
        this.hTgt ("tgt4")
    else if (this.hTgt ( ) == "tgt4")
    then
        this.hTgt ( "tgt5")
    else
        this.hTgt ( "tgt3")
       end 
    end
    
    this.WalkToTarget ( )
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
