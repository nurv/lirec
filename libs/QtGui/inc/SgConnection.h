#ifndef SGCONNECTION_H
#define SGCONNECTION_H

#include "SgModule.h"

namespace Sg
{    
    class SgConnection : public SgObject
    {
    public:
        enum SgProtocolType { TCP, UDP, MCAST};
        enum SgNetworkType { LAN, PROCESS, PLATFORM };

        static SgNameType ProtocolName(SgProtocolType protocol){
            switch (protocol)
            {
            case TCP: return "tcp";
            case UDP: return "udp";
            case MCAST: return "mcast";
            }
            return "";
        }

        static SgNameType NetworkName(SgNetworkType network){
            switch (network)
            {
            case LAN: return "Local Network";
            case PROCESS: return "Process";
            case PLATFORM: return "Platform";
            }
            return "";
        }


    public:
        SgConnection(const SgModule* module1, const SgPort* port1,
                     const SgModule* module2, const SgPort* port2,
                     SgProtocolType protocol, SgNetworkType network, const SgObject* parentObject=0);
        virtual ~SgConnection();          // destructor

        const SgModule* getFirstModule() const;    // get first module name
        const SgPort* getFirstModulePort() const;  // get name of port of first module
        const SgModule* getSecondModule() const;   // get second module
        const SgPort* getSecondModulePort() const; // get name of port of second module
        SgProtocolType getProtocol() const;        // get if connection is lossy
        SgNameType getProtocolName() const;        // get if connection is lossy
        SgNetworkType getNetwork() const;          // get network type
        SgNameType getNetworkName() const;          // get network type
        bool getConnected() const;                 // get if connection is fine

        void setFirstModule(const SgModule * );   // set first module name
        void setFirstModulePort(const SgPort * ); // set name of port of first module
        void setSecondModule(const SgModule*);    // set second module
        void setSecondModulePort(const SgPort* ); // set name of port of second module
        void setProtocol(SgProtocolType);         // set if connection is lossy
        void setNetwork(SgNetworkType);           // set network type
        void setConnected(const bool);            // set if connection is fine

        bool refreshState();

        static SgNameType FullConnectionName(std::string module1Name, std::string port1Name,
                                             std::string module2Name, std::string port2Name,
                                             SgProtocolType protocol, SgNetworkType network){
                    return module1Name+"."+port1Name+"_"+module2Name+"."+port2Name+
                           "_"+ ProtocolName(protocol)+"_"+NetworkName(network);
        }
        static SgNameType FullConnectionName(const SgConnection& org){
                    return org.getFirstModule()->getName()+"."+org.getFirstModulePort()->getName()+
                           "_"+org.getSecondModule()->getName()+"."+org.getSecondModulePort()->getName()+
                           "_"+ProtocolName(org.protocol)+"_"+NetworkName(org.network);
        }

    private:
        const SgModule* firstModule;    // first module name
        const SgPort* firstModulePort;  // name of port of first module
        const SgModule* secondModule;   // second module
        const SgPort* secondModulePort; // name of port of second module
        SgProtocolType protocol;        // if connection is lossy
        SgNetworkType network;      // network type
        bool connected;           // if connection is fine
    };
} // namespace Sg (END)

#endif // SGCONNECTION_H
