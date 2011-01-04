#ifndef SGPORT_H
#define SGPORT_H

#include "SgObject.h"
#include "SgPort.h"
#include <string>

namespace Sg {
    /** port data
     */
    class SgPort: public SgObject
    {
    public:
        /** \breif Constructor
         *
         *  \param newName name of the port
         */
        SgPort(SgNameType newName, const SgObject* parentObject=0);


        /** \breif Destructor
         *
         */
        virtual ~SgPort();

        static SgNameType FullPortName(SgNameType moduleName, SgNameType portName)
        {
            return "/Port_"+moduleName+"_"+portName;
        }

        static SgNameType FullPortName(const SgPort& org)
        {
            if (org.parent)
                if (org.parent->parent)
                    return "/Port_"+ org.parent->parent->getName()+"_"+org.name;
            return "/Port_"+org.name;
        }
    };

} // namespace Sg (END)

#endif // SGPORT_H
