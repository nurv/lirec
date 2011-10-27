--------------------------------------------------------------------------------
--  Function......... : checkExperimentTimer
--  Author........... : Paulo F. Gomes
--  Description...... : Quits the application if it is time to migrate.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.checkExperimentTimer ( )
--------------------------------------------------------------------------------
	
   local nApplicationTotalElapsedTimeSeconds = application.getTotalFrameTime ( )
   local nTimerDurationSeconds = this.nTimerDurationMinutes ( ) * this.nSecondsPerMinute ( )
   
   if ( nApplicationTotalElapsedTimeSeconds > nTimerDurationSeconds)
   then
        application.quit ( )
   end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
