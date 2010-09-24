#include "SgModule.h"
#include <iostream>

namespace Sg
{
    // static fields declarations
    SgNameType SgModule::portForModulesName("/PortForModules");
    SgPortDataType SgModule::portForModules;


    SgModule::SgModule(const SgNameType newName, const SgObject * parentObject): SgObject(newName, parentObject), category(""),
            subCategory(""), active("false"), ports(this)
    {
        fullName = SgModule::FullModuleName(*this);
        //refreshState();
    }

    SgModule::SgModule(const SgNameType newName, const std::string newCategory,
                       const std::string newSubCategory, const bool newActive, const SgObject * parentObject):
             SgObject(newName,parentObject), category(newCategory), subCategory(newSubCategory), active(newActive), ports(this)
    {
        fullName = SgModule::FullModuleName(*this);
        //refreshState();
    }

    SgModule::~SgModule()
    {
        //yarp::os::Network::unregisterName(fullName.c_str());
    }

    void SgModule::setCategory(const std::string newCategory)
    {
        this->category = newCategory;
        modified();
    }

    void SgModule::setSubCategory(const std::string newSubCategory)
    {
        this->subCategory = newSubCategory;
        modified();
    }

    void SgModule::setActive(const bool newActive)
    {
        this->active = newActive;
        modified();
    }

    const std::string SgModule::getCategory() const
    {
        return this->category;
    }

    const std::string SgModule::getSubCategory() const
    {
        return this->subCategory;
    }

    bool SgModule::getActive() const
    {
        return this->active;
    }

    SgPortContainer& SgModule::getPorts()
    {
        return this->ports;
    }

    /**
     *  Adds a port to the list and screen also sets its group id to its
     *  parent module
     */

    bool SgModule::addPort(const SgNameType &newPortName)
    {
        return ports.add(newPortName);
    }

    bool SgModule::delPort(const SgNameType &portName)
    {
        return ports.del(portName);
    }


    bool YARPexists(std::string portName)
    {
        static FILE *inpipe;
        char inbuf[200];

        inpipe =  popen((std::string("yarp exists ")+portName+" ; echo $?").c_str(),"r");
        //std::cout << (std::string("yarp exists ")+portName+" ; echo $?").c_str() << std::endl;
        if (!inpipe){ std::cout<<"PING FAILED"<<std::endl;}
        else
        {
            if (fgets(inbuf, sizeof(inbuf), inpipe))
              {
                std::string mystring = inbuf;
                if (inbuf[0]=='0') {
                    pclose(inpipe);
                    return true;
                }
              }                
        }
        pclose(inpipe);
        return false;
    }

    bool SgModule::refreshState()
    {
        if (SgModule::portForModules.isClosed()) {// AddToLog("Attempting to recover local port \n",2);
            SgModule::portForModules.open(SgModule::portForModulesName.c_str());
        }

        std::string fullName = "/Main_" + this->name; // full name of the yarp module to check
        if (!YARPexists(fullName))
        {
            if (this->active)
                this->setActive(false);
            //yarp::os::Network::unregisterName(fullName.c_str());
            //yarp::os::Network::registerName(fullName.c_str());
            //yarp::os::Network::sync(fullName.c_str(), true);
            /*
        for (SgPortContainerIterator iter = ports.begin(); iter != ports.end(); iter++)
        {
            if ( (*iter) != NULL)
            {
                if ( !yarp::os::Network::queryName((*iter)->getFullName().c_str()).isValid() )
                {
                    std::cout << "X: " << (*iter)->getFullName() << std::endl;
                    //yarp::os::Network::unregisterName((*iter)->getFullName().c_str());
                    //yarp::os::Network::registerName((*iter)->getFullName().c_str());
                    //yarp::os::Network::sync((*iter)->getFullName().c_str());
                }
            }
        }
        */
            return false;
    }
        else
        //ought to be check for connection and if not connected then reconnect;
        //check if directional connection betwean our port and other module is down
        if(yarp::os::Network::isConnected(fullName.c_str(), SgModule::portForModulesName.c_str(),true)==false ||
           yarp::os::Network::isConnected(SgModule::portForModulesName.c_str(), fullName.c_str(),true)==false)
        {
            // try to set up bidirectional TCP connection
            if(yarp::os::Network::connect(fullName.c_str(),SgModule::portForModulesName.c_str(),"tcp",false)==false ||
               yarp::os::Network::connect(SgModule::portForModulesName.c_str(), fullName.c_str(),"tcp",false)==false)
            {
                if (this->active) // AddToLog("The module Main_" + this->name + " is in error \n",2);
                    this->setActive(false);
            }
            else {
                if (!this->active) //AddToLog("The module Main_" + this->name + " is working \n",1);
                {
                    this->setActive(true);
                    yarp::os::Bottle& cc = Sg::SgModule::portForModules.prepare();
                    cc.clear();
                    cc.addInt(Samgar::ModuleStateInfoCode);
                    cc.addString(name.c_str());
                    Sg::SgModule::portForModules.write();
                }
            }
        }
        return this->active;
    }


} // namespace Sg (END)
