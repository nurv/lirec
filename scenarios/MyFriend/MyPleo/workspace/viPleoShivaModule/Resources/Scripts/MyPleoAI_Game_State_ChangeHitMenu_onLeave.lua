--------------------------------------------------------------------------------
--  State............ : ChangeHitMenu
--  Author........... : Tiago Paiva
--  Description...... : (unsure if it is being used)
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI_Game.ChangeHitMenu_onLeave ( )
--------------------------------------------------------------------------------

            local hUser = application.getCurrentUser ( )

    local c1 = hud.getComponent ( hUser, "myHIT.c1" ) -- 1

    --log.message ( nValue )
    
    hud.setComponentVisible ( c1, false )
    hud.setComponentBackgroundColor ( c1, 255, 255,255,255 )
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
