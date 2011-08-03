--------------------------------------------------------------------------------
--  Function......... : printAttachment
--  Author........... : Paulo F. Gomes
--  Description...... : Prints attachment estimate to log (used for debugging
--                      purposes only).
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.printAttachment ( )
--------------------------------------------------------------------------------
	
	log.message ( "Attachment Estimate: ".. application.getCurrentUserEnvironmentVariable ( "nAttachmentEstimate" ))
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
