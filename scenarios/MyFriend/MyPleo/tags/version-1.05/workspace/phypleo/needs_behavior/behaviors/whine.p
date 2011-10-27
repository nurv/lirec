#include <Log.inc>
#include <Animation.inc>

#include "commands.inc"

// These forward declarations of the functions
// called by the firmware are prototypes.
forward public init();
forward public main();
forward public close();

//
// init
//
public init()
{

    //print("Initializing whine behavior\n");    

}

//
// main
// In this behavior, Pleo whines.
//
public main()
{

    for (;;)
    {
    
        // Execute the whine command.
        command_exec(cmd_whine);

        // Wait for the command to finish.
        while (command_is_playing(cmd_whine))
        {
            sleep;
        }

    }

}

//
// close
//
public close()
{

    //print("Exiting whine behavior\n");    

}

