// Save space by packing all strings.
#pragma pack 1

#include <Log.inc>
#include <Script.inc>
#include <Drive.inc>
#include <Property.inc>

#include "user_properties.inc"

#include "social.p"
#include "hunger.p"

public init()
{
    print("BEHAVIOR: start\r\n")

    // Add two drives: social and hunger, which will
    // be evaluated each second.
    drive_add("social", -25, 25, 1000);
    drive_add("hunger", -25, 25, 1000);

    // need properties
    // Values vary between 0 and 100.
    property_set(property_cleanliness, 50);
    property_set(property_energy, 50);
    property_set(property_petting, 50);
    property_set(property_skills, 0);
    property_set(property_water, 50);
    
    // behavior active
    property_set(property_mode,1);
    
    // Start need decay
    property_set_leak(property_cleanliness, -1, 2000, 100, 0); 
    property_set_leak(property_energy, -1, 2000, 100, 0);
    property_set_leak(property_petting, -1, 2000, 100, 0);
    // property_set_leak(property_skills, 0, 2000, 100, 0);
    property_set_leak(property_water, -1, 2000, 100, 0);
}

public close()
{
    print("BEHAVIOR: exit\r\n")
}