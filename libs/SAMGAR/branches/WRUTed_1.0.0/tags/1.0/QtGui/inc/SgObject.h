#ifndef SGOBJECT_H
#define SGOBJECT_H

#include <string>

namespace Sg
{
    typedef std::string SgNameType;

    class SgObject
    {
    public:
        SgObject(SgNameType objectName="", const SgObject* parentObject=0);

        //SgObject(const SgObject& org);

        virtual ~SgObject();

        /** \breif Seting name of the port
         *
         *  \param newName new name of the port
         */
        virtual void setName(SgNameType);

        /** \brief Obtaining the name of the port
         *
         *  \return Returns the name of the port
         */
        virtual const SgNameType getName() const;

        virtual const SgNameType getFullName() const;

        virtual void refresh() const;

        const SgObject* parent;

    protected:
        /** \breif Executed when changes appear
         *
         *  By the default this method does nothing.
         */
        virtual void modified() const;

        /** \breif Port name
         */
        SgNameType name;
        SgNameType fullName;
    };
} // namespace Sg (END)

#endif // SGOBJECT_H
