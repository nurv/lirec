--------------------------------------------------------------------------------
--  Handler.......... : onChangeStates
--  Authors.......... : Tiago Paiva and Paulo F. Gomes
--  Description...... : Handler that redirects events to MyPleoAI. It also
--                      hides/shows interface elements that are
--                      irrelevant/relevant to the situation.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.onChangeStates ( state )
--------------------------------------------------------------------------------
	
    local obj = application.getCurrentUserSceneTaggedObject ( "MyCharacter" )
    
    if(state == "eat")
    then
        object.sendEvent ( obj, "MyPleoAI", "onChangeStates", "eat" )
    end
    
    if(state == "drink")
    then
        object.sendEvent ( obj, "MyPleoAI", "onChangeStates", "drink" )
    end
    
    if(state == "sit")
    then
        log.message ( "SITTT" )
        object.sendEvent ( obj, "MyPleoAI", "onChangeStates", "sit" )
    end
    
    if(state == "cleanPoop")
    then
        object.sendEvent ( obj, "MyPleoAI", "onChangeStates", "cleanPoop" )
    end
    
    if(state == "wash")
    then
        log.message ( "WASHH" )
        object.sendEvent ( obj, "MyPleoAI", "onChangeStates", "wash" )
    end
    
    if(state == "petting")
    then
        object.sendEvent ( obj, "MyPleoAI", "onChangeStates", "petting" )
    end

    if(state == "stick")
    then
        log.message ( "STICK" )
        object.sendEvent ( obj, "MyPleoAI", "onChangeStates", "stick" )
    end
    
    if(state == "pill")
    then
        log.message ( "PILL" )
        
        local hUser = application.getCurrentUser ( )
        local festas = hud.getComponent ( hUser, "myHUD.Botao_Festas" )
        hud.setComponentVisible ( festas, false)
        application.setCurrentUserScene ( "Game_Principal" )

    end
    if(state == "good_exit")
    then
        log.message ( "GOOOD EXIST" )
                    
        local hUser = application.getCurrentUser ( )
        local win = hud.getComponent ( hUser, "myHIT.win" )
        hud.setComponentVisible ( win, false )
        local festas = hud.getComponent ( hUser, "myHUD.Botao_Festas" )
        hud.setComponentVisible ( festas, true )
        local bar_prog = hud.getComponent ( hUser, "myHIT.bar_prog" )
        hud.setComponentVisible ( bar_prog, false )
        
        application.setCurrentUserEnvironmentVariable ( "bTrainingFinished", true )

        application.setCurrentUserScene ( "Cena_Principal" )
    end
    if(state == "ball")
    then
        log.message ( "BALL" )
        object.sendEvent ( obj, "MyPleoAI", "onChangeStates", "ball" )
    end
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
