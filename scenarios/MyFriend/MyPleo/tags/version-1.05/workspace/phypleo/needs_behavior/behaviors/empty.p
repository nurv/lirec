#include <Log.inc>
#include <Animation.inc>
#include <Property.inc>
#include <Motion.inc>

#include "commands.inc"
#include "motions.inc"
#include "user_properties.inc"

// These forward declarations of the functions
// called by the firmware are prototypes.
forward public init();
forward public main();
forward public close();

public init()
{

    print("BEHAVIOR: empty init\n"); 
    property_leak_enable(false); 
	// stop all motions
    motion_stop(mot_none);
    motion_play(mot_fat_sleep_lay);
    //property_set(property_migrating,1);
}

public main()
{

    for (;;)
    {
      //if (property_get(property_migrating) == 1 && !motion_is_playing(mot_fat_sleep_lay))
      //{
      //  motion_play(mot_fat_sleep_lay);
      //}
      //else{
      //  if(property_get(property_migrating) == 1)
      //    property_set(property_migrating,0);
      //  else
      //    sleep;
      //}
    }

}

public close()
{

    //printf("needs: cleanliness %d, energy %d, petting %d, skills %d, water %d\n", get(property_cleanliness), get(property_energy), get(property_petting), get(property_skills), get(property_water));
    print("BEHAVIOR: empty close\n");
    property_leak_enable(true); 
}

