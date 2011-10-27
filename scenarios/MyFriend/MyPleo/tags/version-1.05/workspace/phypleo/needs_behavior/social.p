#include <Drive.inc>
#include <Property.inc>
#include <Log.inc>

#include "scripts.inc"

// These forward declarations of the functions
// called by the firmware are prototypes.
forward public social_init();
forward public social_eval();
forward public social_behavior_eval(behavior_id);
forward public social_exit();
forward public social_activate();
forward public social_deactivate();

public social_init()
{

    behavior_add(scr_wag, "social", 0, 100, 100, 100, 1000);
    behavior_add(scr_whine, "social", 0, 100, 100, 100, 1000);
    behavior_add(scr_empty, "social", 0, 100, 100, 100, 1000);
    
}

public social_eval()
{   
    // By default, return a value of 50, so that 
    // the social drive wins most of the time.
    return 50;

}

public social_behavior_eval(behavior_id)
{

    switch (behavior_id)
    {
    
        // By default, Pleo runs the wagging script.
        case scr_wag:
        {
        
            return 50;
            
        }
        // If Pleo's happiness level is low,
        // he will run the whining script.
        case scr_whine:
        {
        
            if (property_get(property_petting) < 30)
            {
                return 100;
            }
        
        }
        case scr_empty:
        {
            if (property_get(property_mode) == 1)
            {
                return 200;
            }
        }
    
    }

    // This method must return a value.
    return 0;

}