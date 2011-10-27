//
// wag.p
// PDK drive system example
//
// This behavior is part of the social drive.
// It is activated when the social drive is the 
// dominant drive and Pleo has a high happiness level (see
// social.p).
// 
// This behavior plays the wag command in an infinite loop,
// until another behavior is selected or Pleo is turned off.
//

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

    print("Initializing wag behavior\n");    

}


//
// main
// In this behavior, Pleo wags his tail and makes
// happy panting noises.
//
public main()
{

    for (;;)
    {
    
        // Execute the wag command.
        command_exec(cmd_wag);

        // Wait for the command to finish.
        while (command_is_playing(cmd_wag))
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

    print("Exiting wag behavior\n");    

}


