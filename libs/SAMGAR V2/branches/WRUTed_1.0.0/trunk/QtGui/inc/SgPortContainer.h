#ifndef SGPORTCONTAINER_H
#define SGPORTCONTAINER_H

#include "SgPort.h"
#include <map>


namespace Sg
{
    class SgModule;

    typedef SgPort* SgPortPtr;
    
    /** \brief Type of data where Ports are stored
     *
     */
    typedef std::map<SgNameType, SgPortPtr> SgPortContainerType;

    /** \breif Iterator to the Ports data storage
     *
     */
    typedef SgPortContainerType::iterator SgPortContainerIteratorType;
    
    class SgPortContainerIterator: public SgPortContainerIteratorType
    {
    public:
        SgPortContainerIterator();
        SgPortContainerIterator(const SgPortContainerIteratorType &org);
        SgPortPtr& operator* ();
        SgPortPtr* operator->();
    };

    class SgPortContainer: public SgObject
    {
    public:
        /** \breif Constructor
         */
        SgPortContainer(const SgObject* parentObject=0);

        /** \brief Destructor
         */
        virtual ~SgPortContainer();

        bool add(const SgNameType & newPortName);

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
        bool del(const SgNameType & portName);

        /** \breif Returns iterator to the port
         *
         *  \param portName name of the Port that iterator will be returned
         *  \return on succes return iterator corresponding to the Port of a name PortName,
         *          on failure returns value of a null() method
         */
        SgPortContainerIterator get(const SgNameType &portName);

        /** \brief Check if Port exists
         *
         *  \param portName name of the Port to check if it exists
         *  \return true if Port exists, false otherwise
         */
        bool exist(const SgNameType &portName);

        /** \brief Iterator to the begin of the container
         *
         *  \return Iterator to the Port
         */
        SgPortContainerIterator begin();

        /** \brief Iterator to the end of the container
         *
         *  \return Iterator to the Port
         */
        SgPortContainerIterator end();

        /** \brief Special iterator coresponding to the NULL pointer
         *
         *  \return Iterator to the Port
         */
        SgPortContainerIterator null();

        int size();

    private:
        /** \breif Container storing Ports
         */
        SgPortContainerType ports; // list of Ports names
    };

} // namespac Sg (END)

#endif // SGPORTCONTAINER_H
