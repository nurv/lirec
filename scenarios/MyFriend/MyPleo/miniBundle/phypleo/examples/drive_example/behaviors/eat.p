//
// eat.p
// PDK drive system example
//
// This behavior is part of the hunger drive.
// It is activated when the hunger drive is the 
// dominant drive and Pleo has a low blood_sugar level (see
// hunger.p).
// 
// This behavior plays the graze command in an infinite loop,
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

    print("Initializing eat behavior\n");    

}

// 
// main
// In this behavior, Pleo walks around and grazes.
//
public main()
{

    for (;;)
    {

        // Execute the graze command.
        command_exec(cmd_graze);

        // Wait for the command to finish.
        while (command_is_playing(cmd_graze))
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

    print("Exiting eat behavior\n");    

}
