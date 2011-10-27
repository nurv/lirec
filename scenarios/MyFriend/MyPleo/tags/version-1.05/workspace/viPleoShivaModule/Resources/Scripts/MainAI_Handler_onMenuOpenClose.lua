--------------------------------------------------------------------------------
--  Handler.......... : onMenuOpenClose
--  Author........... : Tiago Paiva
--  Description...... : Handler for the event of closing or opening the actions
--                      menu or sub-menus.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.onMenuOpenClose ( botao )
--------------------------------------------------------------------------------
	
    local hUser = application.getCurrentUser ( )

    local alimentar = hud.getComponent ( hUser, "myHUD.Container_Alimentar" ) -- 1
    local jogar = hud.getComponent ( hUser, "myHUD.Container_Jogar" ) -- 2 
    local limpar = hud.getComponent ( hUser, "myHUD.Container_Limpar" ) -- 3 
    --local tratar = hud.getComponent ( hUser, "myHUD.Botao_Tratar" ) -- 4
    
    --local progress = hud.getComponent ( hUser, "myHUD.Stats_Barra_Menu" )
    
    local menu = hud.getComponent ( hUser, "myHUD.Menu" )
    local mais =  hud.getComponent ( hUser, "myHUD.Botao_Mais" )
    if (botao == "1")
    then   
        hud.setComponentVisible ( alimentar, true )
        hud.setComponentVisible ( jogar, false )
        hud.setComponentVisible ( limpar, false )
    end
    if (botao == "2")
    then   
        hud.setComponentVisible ( alimentar, false )
        hud.setComponentVisible ( jogar, true )
        hud.setComponentVisible ( limpar, false )
    end
    if (botao == "3")
    then   
        hud.setComponentVisible ( alimentar, false )
        hud.setComponentVisible ( jogar, false )
        hud.setComponentVisible ( limpar, true )
    end
	if (botao == "5")
    then   
        hud.setComponentVisible ( alimentar, false )
        hud.setComponentVisible ( jogar, false )
        hud.setComponentVisible ( limpar, false )
    end
    if (botao == "6")
    then   
        hud.setComponentVisible ( alimentar, false )
        hud.setComponentVisible ( jogar, false )
        hud.setComponentVisible ( limpar, false )
        hud.setComponentVisible ( menu, false )
        hud.setComponentVisible ( mais, false )

    end
    
    ----------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
