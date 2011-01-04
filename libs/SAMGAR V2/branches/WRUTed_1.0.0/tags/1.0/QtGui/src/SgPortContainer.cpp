#include "SgPortContainer.h"

namespace Sg
{
    SgPortContainerIterator::SgPortContainerIterator():
            SgPortContainerIteratorType()
    {
    }

    SgPortContainerIterator::SgPortContainerIterator(const SgPortContainerIteratorType &org):
            SgPortContainerIteratorType(org)
    {
    }

    SgPortPtr& SgPortContainerIterator::operator* ()
    {
        return ((SgPortContainerIteratorType)(*this))->second;
    }

    SgPortPtr* SgPortContainerIterator::operator-> ()
    {
        return &(((SgPortContainerIteratorType)(*this))->second);
    }


    SgPortContainer::SgPortContainer(const SgObject* parentModule) : SgObject("", parentModule)
    {
    }

    SgPortContainer::~SgPortContainer()
    {
        for (SgPortContainerIterator iter = ports.begin(); iter != ports.end(); iter++) {
            if ( (*iter) != NULL){
                delete (*iter);
                (*iter)=NULL;
            }
        }
        ports.clear();
    }

    bool SgPortContainer::add(const SgNameType & newPortName)
    {
        SgPortContainerIterator iter;
        if ( (iter = ports.find(newPortName)) == ports.end() )
        {
            Sg::SgPort* newPort = new Sg::SgPort(newPortName, this);
            ports.insert( std::pair<SgNameType, SgPortPtr>(newPort->getName(),newPort) );
            modified();
            return true;
        };
        return false;
    }

    bool SgPortContainer::clear()
    {
        for (SgPortContainerIterator iter = ports.begin(); iter != ports.end(); iter++) {
            if ( (*iter) != NULL){
                delete (*iter);
                (*iter)=NULL;
            }
        }
        ports.clear();
        return true;
    }

    bool SgPortContainer::del(const SgNameType &portName)
    {
        SgPortContainerIterator iter = ports.find(portName);
        if ((*iter))
        {
            delete (*iter);
            (*iter)=NULL;
            ports.erase(portName);
            modified();
            return true;
        }
        return false;
    }


    SgPortContainerIterator SgPortContainer::get(const SgNameType &portName)
    {
        return ports.find(portName);
    }

    bool SgPortContainer::exist(const SgNameType &portName)
    {
        return ( ports.find(portName) != ports.end() );
    }

    SgPortContainerIterator SgPortContainer::begin()
    {
        return ports.begin();
    }

    SgPortContainerIterator SgPortContainer::end()
    {
        return ports.end();
    }

    SgPortContainerIterator SgPortContainer::null()
    {
        return ports.end();
    }

    int SgPortContainer::size()
    {
        return ports.size();
    }

} // namespace Sg (END)
