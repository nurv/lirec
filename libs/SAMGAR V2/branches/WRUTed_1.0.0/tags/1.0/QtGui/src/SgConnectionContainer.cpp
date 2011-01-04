#include "SgConnectionContainer.h"

namespace Sg
{
    SgConnectionContainerIterator::SgConnectionContainerIterator():
            SgConnectionContainerIteratorType()
    {
    }

    SgConnectionContainerIterator::SgConnectionContainerIterator(const SgConnectionContainerIteratorType &org):
            SgConnectionContainerIteratorType(org)
    {
    }

    SgConnectionPtr& SgConnectionContainerIterator::operator* ()
    {
        return ((SgConnectionContainerIteratorType)(*this))->second;
    }

    SgConnectionPtr* SgConnectionContainerIterator::operator-> ()
    {
        return &(((SgConnectionContainerIteratorType)(*this))->second);
    }


    SgConnectionContainer::SgConnectionContainer(const SgObject* parentObject): SgObject("", parentObject)
    {
    }

    SgConnectionContainer::~SgConnectionContainer()
    {
        for (SgConnectionContainerIterator iter = connections.begin(); iter != connections.end(); iter++) {
            if ( (*iter) != NULL){
                delete (*iter);
                (*iter)=NULL;
            }
        }
        connections.clear();
    }

    bool SgConnectionContainer::add(const SgModule* module1, const SgPort* port1,
                                    const SgModule* module2, const SgPort* port2,
                                    SgConnection::SgProtocolType protocol, SgConnection::SgNetworkType network)
    {   
        SgNameType connName = SgConnection::FullConnectionName(module1->getName(), port1->getName(),
                                                               module2->getName(), port2->getName(),
                                                               protocol, network);
        SgConnectionContainerIterator iter;
        if ( (iter = connections.find(connName)) == connections.end() )
        {
            SgConnectionPtr itConn = new SgConnection(module1, port1, module2, port2, protocol, network, this);
            connections.insert( std::pair<SgNameType, SgConnectionPtr>(connName, itConn) );
            modified();
            return true;
        }
        return false;
    }

    bool SgConnectionContainer::clear()
    {
        for (SgConnectionContainerIterator iter = connections.begin(); iter != connections.end(); iter++) {
            if ( (*iter) != NULL){
                delete (*iter);
                (*iter)=NULL;
            }
        }
        connections.clear();
        modified();
        return true;
    }

    bool SgConnectionContainer::del(const SgNameType &connectionName)
    {
        SgConnectionContainerIterator iter;
        if ( connections.find((connectionName)) != connections.end())
        {
            if ( &(*connections.find(connectionName)) != NULL)
            {
                iter = connections.find(connectionName);
                if ( connections.erase(connectionName) )
                {
                    delete (*iter);
                    modified();
                    return true;
                }
            }
        }
        return false;
    }

    SgConnectionContainerIterator SgConnectionContainer::get(const SgNameType &connectionName)
    {
        return connections.find(connectionName);
    }

    bool SgConnectionContainer::exist(const SgNameType &connectionName)
    {
        return ( connections.find(connectionName) != connections.end() );
    }

    SgConnectionContainerIterator SgConnectionContainer::begin()
    {
        return connections.begin();
    }

    SgConnectionContainerIterator SgConnectionContainer::end()
    {
        return connections.end();
    }

    SgConnectionContainerIterator SgConnectionContainer::null()
    {
        return connections.end();
    }

    int SgConnectionContainer::size()
    {
        return connections.size();
    }
} // namespace Sg (END)

