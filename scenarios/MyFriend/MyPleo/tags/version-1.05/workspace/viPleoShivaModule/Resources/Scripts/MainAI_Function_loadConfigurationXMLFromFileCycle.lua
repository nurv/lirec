--------------------------------------------------------------------------------
--  Function......... : loadConfigurationXMLFromFileCycle
--  Author........... : Paulo F. Gomes
--  Description...... : Waits until xml file is read or an error occurs.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.loadConfigurationXMLFromFileCycle ( xmlConfigurationAbstract)
--------------------------------------------------------------------------------
	
	local bSuccess = false
    local nStoreStatus = xml.getReceiveStatus ( xmlConfigurationAbstract )
    if ( nStoreStatus == -1)
    then
        log.warning ( "Configuration file does not exist, or receive hasn't been used for this XML." )
    end
    while ( nStoreStatus < 1 and nStoreStatus > -1)
    do
        nStoreStatus = xml.getReceiveStatus ( xmlConfigurationAbstract )
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
