--------------------------------------------------------------------------------
--  State............ : Eating
--  Authors.......... : Tiago Paiva and Paulo F. Gomes
--  Description...... : Updates the energy need and schedules a poop creation.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.Eating_onLeave ( )
--------------------------------------------------------------------------------
		    
    this.setNeedEnergy ( this.getNeedEnergy ( ) + this.nNeedWaterIncreaseBowl ( ))

    object.postEvent ( this.hBody ( ), this.nCreatePoopDelay ( ), "MyPleoAI", "onCreatePoop" )
    sound.stop ( this.hBody ( ), 0 )
    this.gotstate (false )
    
    this.bFufillingNeed ( false )
    this.bNeedEnergyActive ( false )

--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
