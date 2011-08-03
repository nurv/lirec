--------------------------------------------------------------------------------
--  Function......... : loadNeedsXMLFromFileCycle
--  Author........... : Paulo F. Gomes
--  Description...... : Wait until needs' file has been totally read, or an
--                      error has occurred.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MyPleoAI.loadNeedsXMLFromFileCycle ( xmlNeedsAbstract )
--------------------------------------------------------------------------------
	
	local bSuccess = false
    local nStoreStatus = xml.getReceiveStatus ( xmlNeedsAbstract )
    if ( nStoreStatus == -1)
    then
        log.warning ( "Need file does not exist, or receive hasn't been used for this XML." )
    end
    while ( nStoreStatus < 1 and nStoreStatus > -1)
    do
        nStoreStatus = xml.getReceiveStatus ( xmlNeedsAbstract )
        if ( nStoreStatus == -2)
        then
            log.error ( "Server not responding or wrong URI." )
        elseif ( nStoreStatus == -3)
        then
            log.error ( "Invalid XML parsed." )
        end
    end
    
    if ( nStoreStatus == 1)
    then
        log.message ( "XML loaded successfully." )
        bSuccess = true
    end
    
    return bSuccess
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
