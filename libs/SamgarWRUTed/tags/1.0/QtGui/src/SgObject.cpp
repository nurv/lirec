#include "SgObject.h"

namespace Sg
{
    SgObject::SgObject(SgNameType objectName, const SgObject* parentObject):
            parent(parentObject), name(objectName), fullName(name)
    {
    }
/*
    SgObject::SgObject(const SgObject& org): parent(org.parent), name(org.name)
    {
    }
*/
    SgObject::~SgObject()
    {
    }

    void SgObject::setName(SgNameType newName)
    {
        name = newName;
        modified();
    }

    const SgNameType SgObject::getName() const
    {
        return name;
    }

    const SgNameType SgObject::getFullName() const
    {
        return fullName;
    }

    void SgObject::refresh() const
    {
        modified();
    }

    void SgObject::modified() const
    {
        if (parent) parent->refresh();
    }

} // namespace Sg (END)

