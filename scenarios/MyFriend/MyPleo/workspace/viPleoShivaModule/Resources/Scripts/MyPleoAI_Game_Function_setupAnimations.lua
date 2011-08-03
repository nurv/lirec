--------------------------------------------------------------------------------
--  Function......... : setupAnimations
--  Author........... : Tiago Paiva
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI_Game.setupAnimations ( )
--------------------------------------------------------------------------------
	
	  local hObject = this.hObject ( )
    
    if ( object.hasController ( hObject, object.kControllerTypeAnimation ) )
    then
        animation.changeClip                ( hObject, 0, 0 )
        animation.changeClip                ( hObject, 1, 1 )
        animation.changeClip                ( hObject, 2, 2 )
        animation.changeClip                ( hObject, 3, 3 )   
        animation.changeClip                ( hObject, 4, 4 )
        animation.changeClip                ( hObject, 5, 5 )
        animation.changeClip                ( hObject, 6, 6 )
        animation.changeClip                ( hObject, 7, 7 )
        animation.changeClip                ( hObject, 8, 8 )


        animation.setPlaybackKeyFrameBegin  ( hObject, 0, animation.getClipKeyFrameRangeMin ( hObject, 0 ) )
        animation.setPlaybackKeyFrameBegin  ( hObject, 1, animation.getClipKeyFrameRangeMin ( hObject, 1 ) )
        animation.setPlaybackKeyFrameBegin  ( hObject, 2, animation.getClipKeyFrameRangeMin ( hObject, 2 ) )
        animation.setPlaybackKeyFrameBegin  ( hObject, 3, animation.getClipKeyFrameRangeMin ( hObject, 2 ) )
        animation.setPlaybackKeyFrameBegin  ( hObject, 4, animation.getClipKeyFrameRangeMin ( hObject, 2 ) )
        animation.setPlaybackKeyFrameBegin  ( hObject, 5, animation.getClipKeyFrameRangeMin ( hObject, 2 ) )
        animation.setPlaybackKeyFrameBegin  ( hObject, 6, animation.getClipKeyFrameRangeMin ( hObject, 2 ) )
        animation.setPlaybackKeyFrameBegin  ( hObject, 7, animation.getClipKeyFrameRangeMin ( hObject, 2 ) )
        animation.setPlaybackKeyFrameBegin  ( hObject, 8, animation.getClipKeyFrameRangeMin ( hObject, 2 ) )

        animation.setPlaybackKeyFrameEnd    ( hObject, 0, animation.getClipKeyFrameRangeMax ( hObject, 0 ) )
        animation.setPlaybackKeyFrameEnd    ( hObject, 1, animation.getClipKeyFrameRangeMax ( hObject, 1 ) )
        animation.setPlaybackKeyFrameEnd    ( hObject, 2, animation.getClipKeyFrameRangeMax ( hObject, 2 ) )
        animation.setPlaybackKeyFrameEnd    ( hObject, 3, animation.getClipKeyFrameRangeMax ( hObject, 2 ) )
        animation.setPlaybackKeyFrameEnd    ( hObject, 4, animation.getClipKeyFrameRangeMax ( hObject, 2 ) )
        animation.setPlaybackKeyFrameEnd    ( hObject, 5, animation.getClipKeyFrameRangeMax ( hObject, 2 ) )
        animation.setPlaybackKeyFrameEnd    ( hObject, 6, animation.getClipKeyFrameRangeMax ( hObject, 2 ) )
        animation.setPlaybackKeyFrameEnd    ( hObject, 7, animation.getClipKeyFrameRangeMax ( hObject, 2 ) )
        animation.setPlaybackKeyFrameEnd    ( hObject, 8, animation.getClipKeyFrameRangeMax ( hObject, 2 ) )

        animation.setPlaybackMode           ( hObject, 0, animation.kPlaybackModeLoop )
        animation.setPlaybackMode           ( hObject, 1, animation.kPlaybackModeLoopReversed )
        animation.setPlaybackMode           ( hObject, 2, animation.kPlaybackModeLoopReversed )
        animation.setPlaybackMode           ( hObject, 3, animation.kPlaybackModeLoopReversed )
        animation.setPlaybackMode           ( hObject, 4, animation.kPlaybackModeLoopReversed )
        animation.setPlaybackMode           ( hObject, 5, animation.kPlaybackModeLoopReversed )
        animation.setPlaybackMode           ( hObject, 6, animation.kPlaybackModeLoopReversed )
        animation.setPlaybackMode           ( hObject, 7, animation.kPlaybackModeLoopReversed )
        animation.setPlaybackMode           ( hObject, 8, animation.kPlaybackModeLoopReversed )

    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
