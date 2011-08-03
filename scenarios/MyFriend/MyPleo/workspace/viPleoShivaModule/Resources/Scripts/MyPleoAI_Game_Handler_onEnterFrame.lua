--------------------------------------------------------------------------------
--  Handler.......... : onEnterFrame
--  Author........... : Tiago Paiva
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI_Game.onEnterFrame (  )
--------------------------------------------------------------------------------
	
    local hObject = this.hObject ( )

	object.matchTranslation ( this.hObject ( ), this.hBody ( ), object.kGlobalSpace )
    object.matchRotation ( this.hObject ( ), this.hBody ( ), object.kGlobalSpace )
    object.setRotation ( this.hObject ( ), -90, 0, 0, object.kLocalSpace )
    
   
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
