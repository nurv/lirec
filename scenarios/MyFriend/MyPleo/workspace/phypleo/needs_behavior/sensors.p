// Save space by packing all strings.
#pragma pack 1

#include <Log.inc>
#include <Script.inc>
#include <Sensor.inc>
#include <Property.inc>

#include "user_properties.inc"


public init()
{
}

public on_sensor(time, sensor_name: sensor, value)
{
    new name[32];
    sensor_get_name(sensor, name);
    
    if (sensor == SENSOR_TOUCH_PETTED)
    {
      property_set(property_petting, get(property_petting) + 20);
      // debug
      // printf("sensors:on_sensor(%d, %s, %d)\n", time, name, value);
    }
    // Pleo eats
    else if(sensor == SENSOR_MOUTH)
    {
      property_set(property_eating,1);
      // debug
      // printf("sensors:on_sensor(%d, %s, %d)\n", time, name, value);
    }
    else if(sensor == SENSOR_TRACKABLE_OBJECT)
    {
      property_set(property_saw_leaf,1);
      // debug
      // printf("sensors:on_sensor(%d, %s, %d)\n", time, name, value);
    }
    
    // Reset sensor trigger.
    return true;
}

public close()
{
}