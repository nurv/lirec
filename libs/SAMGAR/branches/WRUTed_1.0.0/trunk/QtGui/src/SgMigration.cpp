#include "SgMigration.h"
#include <fstream>

namespace Sg
{
    MigrationPortClass::MigrationPortClass()
    {
        //useCallback(); // TODO: this generates problems on exit - probably hangs on onRead
    }

    MigrationPortClass::~MigrationPortClass(){}

    /** it opens Perasonality.txt file on write the content of the bottle to it
     */
    void MigrationPortClass::onRead(yarp::os::Bottle& b)
    {
        Ivebeenused = 1;
        int length = b.size();
        std::ofstream myfile;
        myfile.open("Personality.txt"); // TODO: what for is this file? to log?
        for(int cc=0;cc<length;cc++)
        {
            b.get(cc).asString(); // get the first line from the bottle
            myfile <<  b.get(cc).asString().c_str() << "\n";
        }
        myfile.close();
    }
}
