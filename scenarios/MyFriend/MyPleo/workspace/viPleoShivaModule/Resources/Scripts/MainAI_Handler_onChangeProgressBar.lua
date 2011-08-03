--------------------------------------------------------------------------------
--  Handler.......... : onChangeProgressBar
--  Author........... : Tiago Paiva
--  Description...... : Oscillates red button in obstacle course game bar.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function MainAI.onChangeProgressBar (  )
--------------------------------------------------------------------------------
	
    local hProg= application.getCurrentUserEnvironmentVariable ( "hProg")
    local bCar= application.getCurrentUserEnvironmentVariable ( "bCarregou")

    local hUser = application.getCurrentUser ( )

    if(this.hLast ( ) == 20)
    then
        this.hUp ( true )
        this.hDown (false )
        this.hLast ( 2)
    end
    
    if(this.hLast ( ) == 10)
    then
     --   log.message ( "mudou para down" )
        this.hDown ( true)
        this.hUp ( false )
        this.hLast ( 4)
    end
    
     local c2 = hud.getComponent ( hUser, "myHIT.c2" )
      local c3 = hud.getComponent ( hUser, "myHIT.c3" )
       local c4 = hud.getComponent ( hUser, "myHIT.c4" )
        local c5 = hud.getComponent ( hUser, "myHIT.c5" )
         local c1 = hud.getComponent ( hUser, "myHIT.c1" )
        local container = application.getCurrentUserEnvironmentVariable ( "bar_prog" )

    if(hProg == true)
    then
    
    --log.message ( bCar )
    if(bCar == 0)
    then
        --log.message ( "PASSSOU O TEMPO" )
         --local hUser = application.getCurrentUser ( )
       --  local win = hud.getComponent ( hUser, "myHIT.win" )
       -- hud.setComponentVisible ( win, true )
    end
    
    application.setCurrentUserEnvironmentVariable ( "bCarregou", bCar - 1 )
    
    if(this.hUp ( ))
    then
        if(this.hLast ( ) == 1)
        then
            hud.setComponentVisible ( c2, true )
            hud.setComponentVisible ( c3, false )
            hud.setComponentVisible ( c4, false )
            hud.setComponentVisible ( c5, false )
            hud.setComponentVisible ( c1, false )
            this.hLast ( 2)
        else if (this.hLast ( ) == 2)
        then
            this.hLast ( 3)
            hud.setComponentVisible ( c2, false )
            hud.setComponentVisible ( c3, true )
            hud.setComponentVisible ( c4, false )
            hud.setComponentVisible ( c5, false )
            hud.setComponentVisible ( c1, false )
        
         else if (this.hLast ( ) == 3)
        then
            this.hLast ( 4)
            hud.setComponentVisible ( c2, false )
            hud.setComponentVisible ( c3, false )
            hud.setComponentVisible ( c4, false )
            hud.setComponentVisible ( c5, false )
            hud.setComponentVisible ( c1, true )
        
         else if (this.hLast ( ) == 4)
        then
            this.hLast ( 5)
            hud.setComponentVisible ( c2, false )
            hud.setComponentVisible ( c3, false )
            hud.setComponentVisible ( c4, true )
            hud.setComponentVisible ( c5, false )
            hud.setComponentVisible ( c1, false )
        
         else if (this.hLast ( ) == 5)
        then
            this.hLast ( 10)
            hud.setComponentVisible ( c2, false )
            hud.setComponentVisible ( c3, false )
            hud.setComponentVisible ( c4, false )
            hud.setComponentVisible ( c5, true )
            hud.setComponentVisible ( c1, false )
        end
        end
        end
        end
        end
    else

        if(this.hLast ( ) == 1)
        then
            hud.setComponentVisible ( c2, true )
            hud.setComponentVisible ( c3, false )
            hud.setComponentVisible ( c4, false )
            hud.setComponentVisible ( c5, false )
            hud.setComponentVisible ( c1, false )
            this.hLast ( 20)
        else if (this.hLast ( ) == 2)
        then
            this.hLast ( 1)
            hud.setComponentVisible ( c2, false )
            hud.setComponentVisible ( c3, true )
            hud.setComponentVisible ( c4, false )
            hud.setComponentVisible ( c5, false )
            hud.setComponentVisible ( c1, false )
        
         else if (this.hLast ( ) == 3)
        then
            this.hLast ( 2)
            hud.setComponentVisible ( c2, false )
            hud.setComponentVisible ( c3, false )
            hud.setComponentVisible ( c4, false )
            hud.setComponentVisible ( c5, false )
            hud.setComponentVisible ( c1, true )
        
         else if (this.hLast ( ) == 4)
        then
            this.hLast ( 3)
            hud.setComponentVisible ( c2, false )
            hud.setComponentVisible ( c3, false )
            hud.setComponentVisible ( c4, true )
            hud.setComponentVisible ( c5, false )
            hud.setComponentVisible ( c1, false )
        
         else if (this.hLast ( ) == 5)
        then
            this.hLast ( 4)
            hud.setComponentVisible ( c2, false )
            hud.setComponentVisible ( c3, false )
            hud.setComponentVisible ( c4, false )
            hud.setComponentVisible ( c5, true )
            hud.setComponentVisible ( c1, false )
        end
        end
    end
    end
    end
    end
    else
        hud.setComponentVisible ( c2, false )
        hud.setComponentVisible ( c3, false )
        hud.setComponentVisible ( c4, false )
        hud.setComponentVisible ( c5, false )
        hud.setComponentVisible ( c1, false )
    end
    --hud.setComponentVisible ( c1, true )
    --hud.setComponentBackgroundColor ( c1, 255, 255,255,255 )
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------