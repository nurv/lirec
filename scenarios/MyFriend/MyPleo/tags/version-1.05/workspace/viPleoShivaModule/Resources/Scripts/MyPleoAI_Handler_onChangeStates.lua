--------------------------------------------------------------------------------
--  Handler.......... : onChangeStates
--  Authors.......... : Tiago Paiva and Paulo Gomes
--  Description...... : Updates pleo current state according to received state.
--                      MainAI redirects interface events to this handler.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.onChangeStates ( state )
--------------------------------------------------------------------------------
	
    log.message ( "onchangeStates" )
    log.message ( "DESIRED STATE: " .. state )
    
if( not this.gotstate() )
then
    if (state == "eat")
    then
        this.sAction ("eat") -- change NoWalk loop
        this.createfoodbowl()
        this.bFufillingNeed ( true )
        this.sFulfillingNeedName ( "energy" )
        -- this.NoWalk ( )
    end

    if (state == "drink")
    then
        this.sAction ("eat") -- change NoWalk loop
        this.createwaterbowl()
        --this.NoWalk ( )
    end
    
    if (state == "sit")
    then
        log.message ( "sitt" )
        this.LowEnergy ( )
    end
    
    if (state == "clean")
    then
        log.message ( " chamei o clean" )
        this.LowClean ( )
    end
    
    if (state == "petting")
    then
        if (not this.bNeedEnergyActive ())
        then
            this.Petting()
        end
    end
   
    if (state == "wash")
    then
        log.message ( "washing" )
        this.destroyPoop (  )
        this.Washing ( )
    end
    if (state == "stick")
    then
        log.message ( "sticking" )
        this.createstick (  )
    end
    if (state == "pill")
    then
        log.message ( "pilling" )
        this.Pilling ( )
    end
    
    
    if (state == "ball")
    then
        this.createball ( )
    end
    
    this.gotstate (true )
end

    if (state == "cleanPoop")
    then
        log.message ( " destroy poop" )
        this.destroyPoop (  )
    end
    
    if (state == "walk")
    then
        this.RandomWalk ( )
    end
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
