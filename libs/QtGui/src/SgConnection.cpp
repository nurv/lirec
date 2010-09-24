#include "SgConnection.h"
#include <yarp/os/all.h>

namespace Sg
{
    SgConnection::SgConnection(const SgModule* module1, const SgPort* port1,
                 const SgModule* module2, const SgPort* port2,
                 SgProtocolType protocol, SgNetworkType network, const SgObject* parentObject):
               SgObject(SgConnection::FullConnectionName(module1->getName(), port1->getName(),
                                                         module2->getName(), port2->getName(),
                                                         protocol, network),
                        parentObject),
               firstModule(module1), firstModulePort(port1), secondModule(module2),
               secondModulePort(port2), protocol(protocol), network(network), connected(false)
    {
        fullName = SgConnection::FullConnectionName(*this);
        //refreshState();
    }

    SgConnection::~SgConnection()
    {
        yarp::os::Network::disconnect(SgPort::FullPortName(*firstModulePort).c_str(),
                                      SgPort::FullPortName(*secondModulePort).c_str());
        yarp::os::Network::disconnect(SgPort::FullPortName(*secondModulePort).c_str(),
                                      SgPort::FullPortName(*firstModulePort).c_str());
    }

    const SgModule* SgConnection::getFirstModule() const
    {
        return firstModule;
    }

    const SgPort* SgConnection::getFirstModulePort() const
    {
        return firstModulePort;
    }

    const SgModule* SgConnection::getSecondModule() const
    {
        return secondModule;
    }

    const SgPort* SgConnection::getSecondModulePort() const
    {
        return secondModulePort;
    }

    SgConnection::SgProtocolType SgConnection::getProtocol() const
    {
        return protocol;
    }

     SgConnection::SgNetworkType SgConnection::getNetwork() const
    {
        return network;
    }

    SgNameType SgConnection::getProtocolName() const
    {
        return ProtocolName(protocol);
    }

    SgNameType SgConnection::getNetworkName() const
    {
        return NetworkName(network);
    }

    bool SgConnection::getConnected() const
    {
        return connected;
    }

    void SgConnection::setFirstModule(const SgModule* newModule)
    {
        firstModule = newModule;
        modified();
    }

    void SgConnection::setFirstModulePort(const SgPort* newPort)
    {
        firstModulePort = newPort;
        modified();
    }

    void SgConnection::setSecondModule(const SgModule* newModule)
    {
        secondModule = newModule;
        modified();
    }

    void SgConnection::setSecondModulePort(const SgPort* newPort)
    {
        secondModulePort = newPort;
        modified();
    }

    void SgConnection::setProtocol(SgProtocolType newProtocol)
    {
        protocol = newProtocol;
        modified();
    }

    void SgConnection::setNetwork( SgConnection::SgNetworkType newNetwork)
    {
        network = newNetwork;
        modified();
    }

    void SgConnection::setConnected(const bool newConnected)
    {
        connected = newConnected;
        modified();
    }

    bool SgConnection::refreshState()
    {
        bool firstModuleActive = false;
        bool secondModuleActive = false;
        // check if both modules are active
        if ( this->firstModule == NULL || this->secondModule == NULL) // module exists
        {
            this->setConnected(false);
            return false;
        }
        else
        {
            firstModuleActive = this->firstModule->getActive();
            secondModuleActive = this->secondModule->getActive();
        }

        // get ports full name
        std::string firstStr = this->firstModulePort->getFullName();
        std::string secondStr = this->secondModulePort->getFullName();

        if(firstModuleActive && secondModuleActive) // modules are active so they should be connected
        {
            bool connForwardFlag, connReturnFlag;
            connForwardFlag = yarp::os::Network::isConnected(firstStr.c_str(),secondStr.c_str(),true);
            connReturnFlag = yarp::os::Network::isConnected(secondStr.c_str(),firstStr.c_str(),true);
            std::string conProto = SgConnection::ProtocolName(this->protocol);
            if (!connForwardFlag || !connReturnFlag)
            {
                yarp::os::Network::disconnect(firstStr.c_str(), secondStr.c_str());
                yarp::os::Network::disconnect(secondStr.c_str(), firstStr.c_str());
                connForwardFlag = yarp::os::Network::connect(firstStr.c_str(), secondStr.c_str(), conProto.c_str(), true);
                connReturnFlag = yarp::os::Network::connect(secondStr.c_str(), firstStr.c_str(), conProto.c_str(), true);
            }
            if(connForwardFlag && connReturnFlag)
            {
                if (!this->connected)
                    this->setConnected(true);
            }
            else
            {
                if (this->connected)
                    this->setConnected(false);
            }
        }
        else
            if (this->connected)
                this->setConnected(false);
        return this->connected;
    }

} // namespace Sg (END)
