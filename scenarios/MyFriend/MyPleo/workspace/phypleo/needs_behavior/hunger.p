#include <Drive.inc>
#include <Log.inc>

#include "scripts.inc"

// These forward declarations of the functions
// called by the firmware are prototypes.
forward public hunger_init();
forward public hunger_eval();
forward public hunger_behavior_eval(behavior_id);
forward public hunger_exit();
forward public hunger_activate();
forward public hunger_deactivate();

public hunger_init()
{

    behavior_add(scr_eat, "hunger", 0, 100, 100, 100, 1000);
    behavior_add(scr_empty, "hunger", 0, 100, 100, 100, 1000);

}

public hunger_eval()
{
    if (property_get(property_energy) <= 25)
    {
        return 100;
    }
    
    // By default, this drive isn't active, 
    // so it returns a losing value.
    return 0;
}


public hunger_behavior_eval(behavior_id)
{

    switch (behavior_id)
    {
        case scr_eat:
        {
            return 100;
        }
        case scr_empty:
        {
            if (property_get(property_mode) == 1)
            {
                return 200;
            }
        }
    
    }

    return 0;

}