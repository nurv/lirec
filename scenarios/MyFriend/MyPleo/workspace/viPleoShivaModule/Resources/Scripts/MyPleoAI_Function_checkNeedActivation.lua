--------------------------------------------------------------------------------
--  Function......... : checkNeedActivation
--  Author........... : Paulo F. Gomes
--  Description...... : Verifies if the energy need is below a critical value
--                      and activates a LowEnergy state if so.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.checkNeedActivation ( )
--------------------------------------------------------------------------------
	
   
    if (this.getNeedEnergy ( ) < this.nNeedEnergyMinimum ( ))
    then
        this.bNeedEnergyActive ( true )
        if ( not this.bFufillingNeed ( ) or this.sFulfillingNeedName ( ) ~= "energy")
        then
            this.LowEnergy ( )
        end
    else
        this.bNeedEnergyActive ( false )
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
