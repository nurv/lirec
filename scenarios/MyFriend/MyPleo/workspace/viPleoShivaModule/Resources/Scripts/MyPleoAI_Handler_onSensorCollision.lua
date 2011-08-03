--------------------------------------------------------------------------------
--  Handler.......... : onSensorCollision
--  Author........... : Tiago Paiva
--  Description...... : Handler for collisions with food, water, stick and ball.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.onSensorCollision ( nSensorID, hTargetObject, nTargetSensorID )
--------------------------------------------------------------------------------
	
    if nTargetSensorID == 1 and this.gotofood ( ) then
        log.message ( "FOOD found" )
        this.gotofood ( false )
    
    elseif nTargetSensorID == 2 and this.gotowater ( ) then
        log.message ( "WATER found" )
        this.gotowater ( false )
        --this.Drinking ( )
    elseif nTargetSensorID == 3 and this.gotstick ( ) then
        log.message ( "STICK found" )
        this.gotstick ( false )
        --this.Drinking ( )
    elseif nTargetSensorID == 4 and this.gotstick ( ) then
        log.message ( "BALL found" )
        this.gotstick ( false )        
        --this.Drinking ( )
    -- etc
        
    end
    

--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
