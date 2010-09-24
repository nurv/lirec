#ifndef SGCONNECTIONCONTAINER_H
#define SGCONNECTIONCONTAINER_H


#include "SgConnection.h"
#include <map>

namespace Sg
{
    typedef SgConnection* SgConnectionPtr;
    /** \brief Type of data where Connections are stored
     *
     */
    typedef std::map<SgNameType, SgConnectionPtr> SgConnectionContainerType;

    /** \breif Iterator to the Ports data storage
     *
     */
    typedef SgConnectionContainerType::iterator SgConnectionContainerIteratorType;

    class SgConnectionContainerIterator: public SgConnectionContainerIteratorType
    {
    public:
        SgConnectionContainerIterator();
        SgConnectionContainerIterator(const SgConnectionContainerIteratorType &org);
        SgConnectionPtr& operator* ();
        SgConnectionPtr* operator->();
    };

    class SgConnectionContainer: public SgObject
    {
    public:

        /** \breif Constructor
         */
        SgConnectionContainer(const SgObject* parentObject=0);

        /** \brief Destructor
         */
        virtual ~SgConnectionContainer();

        /** \brief Add new Port to the container
         *
         *  \param newPort reference to the new Port structure
         *  \return true on succes, false on failure
         */

        bool add(const SgModule* module1, const SgPort* port1,
                 const SgModule* module2, const SgPort* port2,
                 SgConnection::SgProtocolType protocol, SgConnection::SgNetworkType network);

        /** \brief Delete all Ports and its data
         *
         *  \return true on succes, false on failure
         */
        bool clear();

        /** \brief Delete port
         *
         *  \param portName name of the Port to be deleted
         *  \return true on succes, false on failure
         */
        bool del(const SgNameType & connectionName);

        /** \breif Returns iterator to the port
         *
         *  \param portName name of the Port that iterator will be returned
         *  \return on succes return iterator corresponding to the Port of a name PortName,
         *          on failure returns value of a null() method
         */
        SgConnectionContainerIterator get(const SgNameType &connectionName);

        /** \brief Check if Port exists
         *
         *  \param portName name of the Port to check if it exists
         *  \return true if Port exists, false otherwise
         */
        bool exist(const SgNameType &connectionName);

        /** \brief Iterator to the begin of the container
         *
         *  \return Iterator to the Port
         */
        SgConnectionContainerIterator begin();

        /** \brief Iterator to the end of the container
         *
         *  \return Iterator to the Port
         */
        SgConnectionContainerIterator end();

        /** \brief Special iterator coresponding to the NULL pointer
         *
         *  \return Iterator to the Port
         */
        SgConnectionContainerIterator null();

        int size();

    private:
        /** \breif Container storing Ports
         */
        SgConnectionContainerType connections; // list of Connection names
    };

} // namespac Sg (END)

#endif // SGCONNECTIONCONTAINER_H
