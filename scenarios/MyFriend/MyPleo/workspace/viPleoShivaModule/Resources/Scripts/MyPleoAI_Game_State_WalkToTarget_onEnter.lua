--------------------------------------------------------------------------------
--  State............ : WalkToTarget
--  Author........... : Tiago Paiva
--  Description...... : (unsure if it is being used)
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI_Game.WalkToTarget_onEnter ( )
--------------------------------------------------------------------------------
	
   -- log.message ( "WALK TO TARGET" )
	 local hObject = this.hObject ( )
    
       -- hud.newTemplateInstance (  )
       log.message ( "DEStruIDOOOOO" )
       
        local hProg= application.getCurrentUserEnvironmentVariable ( "hProg")
        application.setCurrentUserEnvironmentVariable ( "hProg", false)  

    --hud.destroyTemplateInstance ( application.getCurrentUser ( ), "myHIT" )
   -----------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
