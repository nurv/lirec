#ifndef SGMIGRATION_H
#define SGMIGRATION_H

#include <yarp/os/all.h>
#include <iostream>

namespace Sg
{
    /** TODO: find out what it is for?
     *
     *  Probably this class is responsible for writing to the file "Personality.txt"
     *  data about the agent to allow migration process. If it is so the name of the
     *  file sould be pedefined and changeable via some config file.
     *
     *  Later "Personality.txt" file is used by the MainComponent::Migrate()
     *  Probably this is just a mockup and it does not work, or at at least there is
     *  same problem tih the MigrationButton
     *
     */
    class MigrationPortClass : public yarp::os::BufferedPort<yarp::os::Bottle>
    {
    public:
        int Ivebeenused;
        std::string Myvalue;

        MigrationPortClass();
        ~MigrationPortClass();

        /** it opens Perasonality.txt file on write the content of the bottle to it
         */
        virtual void onRead(yarp::os::Bottle& b);
    };
}
#endif // SGMIGRATION_H
