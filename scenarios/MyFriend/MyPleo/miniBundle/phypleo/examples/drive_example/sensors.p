//
// sensors.p
// PDK drive system example
//
// This script runs in the Sensors VM.  Its primary job,
// in this example, is to watch for sensor input and modify
// properties based on the nature of that sensor input. 
// The drive system and other scripts are monitoring these same 
// properties, and they'll take some action based on the values
// of these properties.
//
// For example, in on_sensor, when one of Pleo's touch sensors is 
// activated, we increase Pleo's happiness by increasing the value of
// the happiness property:
//
//    if ((sensor > SENSOR_TOUCH_FIRST) && (sensor < SENSOR_TOUCH_LAST))
//    {
//        property_set(property_happiness, get(property_happiness) + 10);
//    }
//
//
// When Pleo's mouth sensor is activated, that means that something 
// is in Pleo's mouth, so we increase the value of his blood_sugar
// property:
//
//        case SENSOR_MOUTH:
//        {
//            property_set(property_blood_sugar, get(property_blood_sugar) + 25);
//        }
//
 
// Save space by packing all strings.
#pragma pack 1

#include <Log.inc>
#include <Script.inc>
#include <Sensor.inc>
#include <Property.inc>

#include "user_properties.inc"


public init()
{
    print("sensors:init() enter\n");
    
    print("sensors:init() exit\n");
}

//
// on_sensor
// 
// If Pleo is touched, up the happiness property.
// If Pleo is fed, up the blood_sugar property.
//
public on_sensor(time, sensor_name: sensor, value)
{
    new name[32];
    sensor_get_name(sensor, name);
    
    printf("sensors:on_sensor(%d, %s, %d)\n", time, name, value);
    
    // If Pleo is touched, increase his happiness.
    if ((sensor > SENSOR_TOUCH_FIRST) && (sensor < SENSOR_TOUCH_LAST))
    {
        property_set(property_happiness, get(property_happiness) + 10);
    }
    
    switch (sensor)
    {
    
        // If Pleo eats something, 
        // increase his blood sugar..
        case SENSOR_MOUTH:
        {
            property_set(property_blood_sugar, get(property_blood_sugar) + 25);
        }
    
    }
    
    // Reset sensor trigger.
    return true;
}

public close()
{
    print("sensors:close() enter\n");

    print("sensors:close() exit\n");
}