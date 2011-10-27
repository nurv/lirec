#include <Log.inc>
#include <Animation.inc>
#include <Property.inc>
#include <Motion.inc>
#include <Sound.inc>

#include "commands.inc"
#include "motions.inc"
#include "sounds.inc"
#include "user_properties.inc"

// These forward declarations of the functions
// called by the firmware are prototypes.
forward public init();
forward public main();
forward public close();


public init()
{
  property_set(property_saw_leaf,0);
  property_set(property_eating,0);
}

// 
// main
// In this behavior, Pleo walks around and grazes.
//
public main()
{
    for (;;)
    {

        if(property_get(property_saw_leaf)==0)
          command_exec(cmd_search_food);
        else{
          if(command_is_playing(cmd_search_food))
            command_remove(cmd_search_food);
          motion_play(mot_hungry_bite_straight_high);
          sound_play(snd_hungry_bite_straight_high);
        }

        // Wait for the command to finish.
        while (command_is_playing(cmd_search_food) || motion_is_playing(mot_hungry_bite_straight_high))
        {
            sleep;
        }
        
        if(property_get(property_eating)==1)
          property_set(property_energy, get(property_energy) + 50);
    }

}

public close()
{
  
}
