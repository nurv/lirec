#include "SgPort.h"
#include <yarp/os/all.h>
#include <iostream>

/*
 *  PortType
 */
namespace Sg
{
    SgPort::SgPort(SgNameType newName, const SgObject* parentObject): SgObject(newName, parentObject)
    {
        fullName = SgPort::FullPortName(*this);
    }

    SgPort::~SgPort()
    {
        //yarp::os::Network::unregisterName(fullName.c_str());
    }

} // namespace Sg (END)
