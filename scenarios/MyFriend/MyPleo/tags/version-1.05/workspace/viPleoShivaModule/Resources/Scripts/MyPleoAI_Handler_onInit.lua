--------------------------------------------------------------------------------
--  Handler.......... : onInit
--  Authors.......... : Tiago Paiva and Paulo F. Gomes
--  Description...... : Targets the camera to focus Pleo (?) and initializes
--                      need values.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.onInit (  )
--------------------------------------------------------------------------------
	
    this.hBody ( this.getObject ( ) ) 
	this.hObject ( application.getCurrentUserSceneTaggedObject ( "Mesh" ) )
	this.hCamera ( application.getCurrentUserActiveCamera ( ) )
    
    local x, y, z = object.getTranslation ( this.hBody ( ), object.kGlobalSpace )
    
    this.initializeNeeds ( )
    if( application.getCurrentUserEnvironmentVariable ( "bTrainingFinished" ))
    then
        this.initializeAfterTraining ( )
    end
    this.startUpdateNeedsTimer ( )
    
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
