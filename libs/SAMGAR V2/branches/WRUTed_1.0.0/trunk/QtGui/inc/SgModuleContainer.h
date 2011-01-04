#ifndef SGMODULECONTAINER_H
#define SGMODULECONTAINER_H

#include "SgModule.h"
#include <map>

namespace Sg
{
    typedef SgModule* SgModulePtr;
    /** \brief Type of data where modules are stored
     *
     */
    typedef std::map<SgNameType, SgModulePtr> SgModuleContainerType;

    /** \breif Iterator to the modules data storage
     *
     */
    typedef SgModuleContainerType::iterator SgModuleContainerIteratorType;

    class SgModuleContainerIterator: public SgModuleContainerIteratorType
    {
    public:
        SgModuleContainerIterator();
        SgModuleContainerIterator(const SgModuleContainerIteratorType &org);
        SgModulePtr& operator* ();
        SgModulePtr* operator->();
    };

    /** \brief This class models container in witch all modules are stored
     *
     */
    class SgModuleContainer : public SgObject
    {
    public:
        SgModuleContainer(const SgObject* parentObject=0);

        /** \breif Destructor
         */
        virtual ~SgModuleContainer();


        /** \brief Add new module to the container
         *
         *  \param newModuleName a new port name to be added
         *  \return true on succes, false on failure
         */
        bool add(const SgNameType & newModuleName, const SgNameType &categoryName="", const SgNameType &subCategoryName="");

        /** \brief Add a new port to the module stored in the container
         *
         *  \param moduleName name of the module to whitch new port should be added
         *  \param newPortName a new port name to be added
         *  \return true on succes, false on failure
         */
        bool addPort(const SgNameType & moduleName, const SgNameType & newPortName);

        /** \brief Delete all modules and its data
         *
         *  \return true on succes, false on failure
         */
        bool clear();

        /** \brief Delete module
         *
         *  \param moduleName name of the module to be deleted
         *  \return true on succes, false on failure
         */
        bool del(const SgNameType &moduleName);

        /** \breif Delete port from the module stored in the container
         *
         *  \param moduleName name of the module from whitch port should be removed
         *  \param portName name of the port to be removed
         *  \return true on succes, false on failure
         */
        bool delPort(const SgNameType &moduleName, const SgNameType &portName);

        /** \breif Returns iterator to the module
         *
         *  \param moduleName name of the module that iterator will be returned
         *  \return on succes return iterator corresponding to the module of a name moduleName,
         *          on failure returns value of a null() method
         */
        SgModuleContainerIterator get(const SgNameType &moduleName);

        /** \brief Check if module exists
         *
         *  \param moduleName name of the module to check if it exists
         *  \return true if module exists, false otherwise
         */
        bool exist(const SgNameType &moduleName);

        /** \brief Iterator to the begin of the container
         *
         *  \return Iterator to the module
         */
        SgModuleContainerIterator begin();

        /** \brief Iterator to the end of the container
         *
         *  \return Iterator to the module
         */
        SgModuleContainerIterator end();

        /** \brief Special iterator coresponding to the NULL pointer
         *
         *  \return Iterator to the module
         */
        SgModuleContainerIterator null();

        int size();

    private:
        /** \breif Container storing modules
         */
        SgModuleContainerType modules; // list of modules names
    };
} // namespace Sg (END)

#endif // SGMODULECONTAINER_H
