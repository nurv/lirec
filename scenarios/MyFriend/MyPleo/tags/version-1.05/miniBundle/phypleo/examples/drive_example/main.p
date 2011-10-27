//
// main.p
// PDK drive system example
//
// This script runs in the Main VM.  In this example,
// it has two responsibilities:
//  
// 1. Add two drives: social and hunger:
//
//      drive_add("social", -25, 25, 1000);
//      drive_add("hunger", -25, 25, 1000);
//
// 2. Initialize and set a leak on the blood_sugar
//    and happiness properties:
//
//      property_set(property_blood_sugar, 50);
//      property_set(property_happiness, 70);
//
//      property_set_leak(property_blood_sugar, -3, 2000, 100, 0);
//      property_set_leak(property_happiness, -6, 3000, 300, 0);
//
// For more information on properties and leaky integrators,
// check out the property system documentation found in the PDK
// programmer's guide.
//
// Note that, in this example, main:main() is not defined.  
// The behaviors associated with the drives we've defined, 
// social and hunger, handle the logic that causes
// Pleo to act and react the way he does.  For more
// information about how Pleo's drive system works,
// check out the PDK programmer's guide.
//
 
// Save space by packing all strings.
#pragma pack 1

#include <Log.inc>
#include <Script.inc>
#include <Drive.inc>
#include <Property.inc>

#include "user_properties.inc"

// We include the drive scripts so that  
// LifeOS can find the callbacks
// it needs to manage the drives.
#include "social.p"
#include "hunger.p"

//
// init
// In this script, init adds the two drives, initializes
// the properties, and sets a leak for the properties.
//
public init()
{

    print("main::init() enter\n");

    // Add two drives: social and hunger, which will
    // be evaluated each second.
    drive_add("social", -25, 25, 1000);
    drive_add("hunger", -25, 25, 1000);

    // Set initial values for the properties.
    // Values vary between 0 and 100.
    property_set(property_blood_sugar, 50);
    property_set(property_happiness, 70);

    // Start the properties leaking.  
    // blood_sugar will lose 3 points every two seconds,
    // and happiness will lose 6 points every three seconds.
    property_set_leak(property_blood_sugar, -3, 2000, 100, 0);
    property_set_leak(property_happiness, -6, 3000, 300, 0);

    print("main::init() exit\n");

}

//
// main
// In this example, main main:main() does not do anything, since
// the behavior scripts perform the logic.
//

//
// close
//
public close()
{
    print("main:close() enter\n");

    print("main:close() exit\n");
}
