#include "SgModuleContainer.h"

namespace Sg
{
    SgModuleContainerIterator::SgModuleContainerIterator():
            SgModuleContainerIteratorType()
    {
    }

    SgModuleContainerIterator::SgModuleContainerIterator(const SgModuleContainerIteratorType &org):
            SgModuleContainerIteratorType(org)
    {
    }

    SgModulePtr& SgModuleContainerIterator::operator* ()
    {
        return ((SgModuleContainerIteratorType)(*this))->second;
    }

    SgModulePtr* SgModuleContainerIterator::operator-> ()
    {
        return &(((SgModuleContainerIteratorType)(*this))->second);
    }

    SgModuleContainer::SgModuleContainer(const SgObject* parentObject): SgObject("", parentObject)
    {
    }

    SgModuleContainer::~SgModuleContainer()
    {
        for (SgModuleContainerIterator iter = modules.begin(); iter != modules.end(); iter++) {
            if ( (*iter) != NULL){
                delete (*iter);
                (*iter)=NULL;
            }
        }
        modules.clear();
    }

    bool SgModuleContainer::add(const SgNameType& newModuleName, const SgNameType &categoryName, const SgNameType &subCategoryName)
    {
        SgModuleContainerIterator iter;
        if ( (iter = modules.find(newModuleName)) == modules.end() )
        {
            Sg::SgModule* newModule = new Sg::SgModule(newModuleName, categoryName, subCategoryName, false, this);
            modules.insert( std::pair<SgNameType, SgModulePtr>(newModule->getName(),newModule) );
            modified();
            return true;
        }
        else
        {
            (*iter)->setCategory(categoryName);
            (*iter)->setSubCategory(subCategoryName);
        }
        (*iter)->setActive(true);
        modified();
        return false;
    }

    bool SgModuleContainer::addPort(const SgNameType &moduleName, const SgNameType &newPortName)
    {
        bool portAdded = false;
        SgModuleContainerIterator itModule = modules.find(moduleName);
        if ( itModule != this->null() )
            if ( (portAdded = (*itModule)->addPort(newPortName)) )
                modified();
        return portAdded;
    }

    bool SgModuleContainer::clear()
    {
        for (SgModuleContainerIterator iter = modules.begin(); iter != modules.end(); iter++) {
            if ( (*iter) != NULL){
                delete (*iter);
                (*iter)=NULL;
            }
        }
        modules.clear();
        modified();
        return true;
    }

    bool SgModuleContainer::del(const SgNameType &moduleName)
    {
        if ( &(*modules.find(moduleName)) != NULL)
            delete &(*modules.find(moduleName));
        if ( modules.erase(moduleName) )
        {
            modified();
            return true;
        }
        return false;
    }

    bool SgModuleContainer::delPort(const SgNameType &moduleName, const SgNameType &portName)
    {
        bool portRemoved = false;
        SgModuleContainerIterator itModule = modules.find(moduleName);
        if ( itModule != this->null() )
            if ( (portRemoved =(*itModule)->delPort(portName)) )
                modified();
        return portRemoved;
    }

    SgModuleContainerIterator SgModuleContainer::get(const SgNameType &moduleName)
    {
        return modules.find(moduleName);
    }

    bool SgModuleContainer::exist(const SgNameType &moduleName)
    {
        return ( modules.find(moduleName) != modules.end() );
    }

    SgModuleContainerIterator SgModuleContainer::begin()
    {
        return modules.begin();
    }

    SgModuleContainerIterator SgModuleContainer::end()
    {
        return modules.end();
    }

    SgModuleContainerIterator SgModuleContainer::null()
    {
        return modules.end();
    }

    int SgModuleContainer::size()
    {
        return modules.size();
    }

} // namespace Sg (END)
