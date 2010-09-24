#ifndef SGMODULE_H
#define SGMODULE_H

#include "SgPort.h"
#include "SgPortContainer.h"
#include <yarp/os/all.h>
#include "SamgarVars.h"

namespace Sg
{
    typedef yarp::os::BufferedPort<yarp::os::Bottle> SgPortDataType;
    /** \brief Class modeling the SAMGAR module.
     */
    class SgModule : public SgObject
    {
    public:
        /** \breif Default constructor
         */
        SgModule(const SgNameType name, const SgObject* parentObject=0);

        /** \breif Parametric constructor
         *
         *  \param name the name of the module
         *  \param category the category to witch module belongs
         *  \param subcategory the sobcategory to witch module belongs
         *  \param isActive flag defining the current activity state of the module
         */
        SgModule(const SgNameType name, const std::string category, const std::string subcategory,
                 const bool isActive=true, const SgObject* parentObject=0);

        /** \brief Destructor
         */
        virtual ~SgModule();


        /** \brief Seting the category of the module
         *
         *  \param newCategory new category of the module.
         */
        void setCategory(const std::string newCategory);

        /** \brief Seting the subcategory of the module
         *
         *  \param newSubCategory new subcategory of the module.
         */
        void setSubCategory(const std::string newSubCategory);

        /** \brief Seting the activity flag of the module
         *
         *  \param newActive new activity flag of the module.
         */
        void setActive(const bool newActive);

        /** \brief Obtaining the category of the module
         *
         *  \return Returns the category of the module.
         */
        const std::string getCategory() const;

        /** \brief Obtaining the subcategory of the module
         *
         *  \return Returns the subcategory of the module.
         */
        const std::string getSubCategory() const;

        /** \brief Obtaining the current activity flag of the module
         *
         *  \return Returns the current activity flag of the module.
         */
        bool getActive() const;

        bool addPort(const SgNameType &newPortName);

        /** \brief Remove a port from the module
         *
         *  \param portName name of the port to be removed
         *  \return true on succes, false on failure.
         */
        bool delPort(const SgNameType &portName);

        /** \brief Remove all ports from the module
         *
         *  \return true on succes, false on failure.
         */
        bool clearPorts();

        /** \breif Check if port of given name exist in the module
         *
         *  \param portName name of the port to be checked if it exists
         *  \return true if exists, false otherwise
         */
        bool existPort(const SgNameType &portName);

        /** \breif Returns iterator to the port of given name
         *
         *  \param portName name of the port that iterator will be returned
         *  \return on succes return iterator corresponding to the port of a name portName,
         *          on failure returns value of a null() method
         */
        SgPortContainerIterator& getPort(const SgNameType &portName);

        /** \brief Iterator to the begin of the container
         *
         *  \return Iterator to the module
         */
        SgPortContainerIterator begin();

        /** \brief Iterator to the end of the container
         *
         *  \return Iterator to the module
         */
        SgPortContainerIterator end();

        /** \brief Special iterator coresponding to the NULL pointer
         *
         *  \return Iterator to the module
         */
        SgPortContainerIterator null();


        /** \brief Reference to container of the ports
         *
         *  \todo This is not good. It should be removed ASAP.
         */
        SgPortContainer& getPorts();

        bool refreshState();

        static SgPortDataType portForModules; // yarp port

        static SgNameType portForModulesName;

        static SgNameType FullModuleName(std::string moduleName)
        {
                    return "/Main_"+moduleName;
        }

        static SgNameType FullModuleName(const SgModule& org)
        {
                    return "/Main_"+ org.name;
        }

        static void StopAll()
        {
            yarp::os::Bottle& cc = portForModules.prepare();
            cc.clear();
            cc.addInt(Samgar::ModuleStateFullstopAll);
            portForModules.write();
        }

        static void StartAll()
        {
            yarp::os::Bottle& cc = portForModules.prepare();
            cc.clear();
            cc.addInt(Samgar::ModuleStateRunningAll);
            portForModules.write();
        }

    private:
        /** \breif Name of the category to witch module belongs.
         */
        std::string category;

        /** \breif Name of the subcategory to witch module belongs.
         */
        std::string subCategory;

        /** \breif Module activity flag. If nodule is active it is equal true.
         */
        bool active;

        /** \brief Container for ports belonging to the module.
         */
        SgPortContainer ports;
    };

} // namespace Sq (END)

#endif // SGMODULE_H
