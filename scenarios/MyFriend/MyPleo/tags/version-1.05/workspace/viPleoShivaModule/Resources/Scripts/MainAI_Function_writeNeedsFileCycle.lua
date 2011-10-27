--------------------------------------------------------------------------------
--  Function......... : writeNeedsFileCycle
--  Author........... : Paulo F. Gomes
--  Description...... : Waits until xml file is written or an error occurs.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.writeNeedsFileCycle ( xmlNeedsAbstract )
--------------------------------------------------------------------------------
	
    local nStoreStatus = -3
    while ( nStoreStatus < 1 and nStoreStatus ~= -2)
    do
        nStoreStatus = xml.getSendStatus ( xmlNeedsAbstract )
        if ( nStoreStatus == -2)
        then
            log.error ( "Error when storing the xml (URI not found, unknown error, ...)" )
        end
    end
    if ( nStoreStatus == 1)
    then
        log.message ( "XML stored successfully" )
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
