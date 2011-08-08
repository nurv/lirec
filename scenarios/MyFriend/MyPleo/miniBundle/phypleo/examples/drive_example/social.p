//
// social.p
// PDK drive system example
//
// This script defines the callbacks that the LifeOS drive system
// uses to manage Pleo's social drive.
// 
// The social drive defines two behaviors: wag and whine, which correspond
// to the scripts wag.p and whine.p.  When the social drive is the active
// drive, one of these two behaviors will be active, depending on the value
// of Pleo's happiness property. The value of the happiness property depends
// on sensor input (managed by sensors.p) and leakage (set in main.p).
//
// The social drive is Pleo's default drive, so the drive evaluator always
// returns the same value:
//
//    public social_eval()
//    {
//        return 50;
//    }
//
// By default, the social drive runs the wag script:
//
//        case scr_wag:
//        {
//            return 50;
//        }
//
// However, if Pleo's happiness level is low, the social drive
// runs the whine script:
//
//        case scr_whine:
//        {        
//            if (property_get(property_happiness) < 40)
//            {
//                return 100;
//            }        
//        }
//

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


//
// social_init
// This function is run by LifeOS
// when the behavior is activated.
//
public social_init()
{

    behavior_add(scr_wag, "social", 0, 100, 100, 100, 1000);
    behavior_add(scr_whine, "social", 0, 100, 100, 100, 1000);

}


//
// social_eval
// This function returns the current weight
// of the social drive.  Because it's the default drive,
// we don't pay attention to any properties - it always
// returns the same value.
//
public social_eval()
{

    printf("Life Statistics: blood_sugar is %d, happiness is %d\n", get(property_blood_sugar), get(property_happiness));
    
    // By default, return a value of 50, so that 
    // the social drive wins most of the time.
    return 50;

}


//
// social_behavior_eval(behavior_id);
// This function goes through each of the behaviors
// that are a part of this drive and selects one
// based on Pleo's happiness level.
//
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
        
            if (property_get(property_happiness) < 40)
            {
                return 100;
            }
        
        }
    
    }

    // This method must return a value.
    return 0;

}