--------------------------------------------------------------------------------
--  State............ : ChangeHitMenu
--  Author........... : Tiago Paiva
--  Description...... : (unsure if it is being used)
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI_Game.ChangeHitMenu_onEnter ( )
--------------------------------------------------------------------------------
	
    local hObject = this.hObject ( )
    
    
   animation.changeClip ( hObject, 0, 5 )
   animation.setPlaybackMode ( hObject, 0, animation.kPlaybackModeLoop ) 


	local nValue = math.random (0, 10 )
    
    local hUser = application.getCurrentUser ( )

    local c1 = hud.getComponent ( hUser, "myHIT.c1" ) -- 1
    
    --log.message ( nValue )
    
    hud.setComponentVisible ( c1, true )
    hud.setComponentBackgroundColor ( c1, 255, 255,255,255 )
    
    --hud.setComponentBackgroundColor ( c1, 255, 255,255,255 )

    object.postEvent ( this.hBody ( ), 2, "MyPleoAI_Game", "onRandomStart")


    
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
