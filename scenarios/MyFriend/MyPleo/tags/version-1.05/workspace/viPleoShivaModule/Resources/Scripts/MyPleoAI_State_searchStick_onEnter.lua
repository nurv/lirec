--------------------------------------------------------------------------------
--  State............ : searchStick
--  Author........... : Tiago Paiva
--  Description...... : (currently not being used)
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.searchStick_onEnter ( )
--------------------------------------------------------------------------------
	
	--
	-- Write your code here, using 'this' as current AI instance.
	--
    local hO = this.hObject ( )

    log.message ( "A mudar para correr" )
	animation.changeClip ( this.hObject ( ), 0, 3 )
    --navigation.setAcceleration (this.hBody ( ), 100000 )
    --navigation.setSpeedLimit ( this.hBody ( ), 1000000 )
   --animation.setPlaybackMode ( hObject, 0, animation.kPlaybackModeLoopMirrored )
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
