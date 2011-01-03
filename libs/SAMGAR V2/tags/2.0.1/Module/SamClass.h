/** \file SamClass.h
 *  This file is the main header for the Samgar V2 modules.
 *  It includes all essential definitions to create a Samgar V2 module.
 *
 */
#include <yarp/os/all.h>
#include <string>
#include <iostream>

#include <map>
#define SAM_CONTROL_PORT_NAME "/CONTROL" 
#define maxmigraionplatforms 10


/** Main Samgar V2 module class.
 *
 *  In order to create a new Samgar V2 module one neads to implement to
 *  pure virtual functions: \link SamClass::SamInit \endlink and  \link
 *  SamClass::SamIter \endlink
 */
class SamClass : public yarp::os::BufferedPort<yarp::os::Bottle>, public yarp::os::PortReport
{
  private:

   std::string name;            //!< Moudle name 

   /** yarp network 
    *
    * \todo Check if it is good to have it in all modules. It might be enought
    * to declarate only one yarp network instance for the whole file.
    */
   yarp::os::Network yarp;       

   /** \brief Ports pointer pontainer
    *
    *  Container for pointer to ports and other concatable classes
    */ 
   std::map<std::string, yarp::os::Contactable*> portsPtrContainer;

   /** \breief Migration platform container
    *
    *  Fixed array containing names of the posible migration platforms
    *  \todo Check if it should be in this class du to the fact that migration
    *  process should happen at the CMION level.
    */
   std::string PossibleMigrationPlatforms[maxmigraionplatforms];

  public:
   
   /** \brief Constructor 
    *
    *  \param newName name of the module.
    */
   SamClass(std::string newName);

   /** \breif Start module 
    *
    *  This method is responsible for sending information to the sever about
    *  this moduele and about the ports that this module have. Therefore it
    *  shoud be run AFTER adding all ports with \link SamClass::newPort \endlink
    */
   void StartModule();

   /** \breif does nothing
    *  \todo fill it or remove
    */
   void RegisterForMigration(void);

   /** \breif does nothing
    *  \todo fill it or remove
    */
   void UpdateMigratoinPlatforms(void);

   /** \breif does nothing
    *  \todo fill it or remove
    */
   bool ConnectToPlatform(std::string platfom);
   
   /** \brief Method that shoud be to initialize module
    *  
    *  This is pure virtual method and it should be filled.
    *  Inside this method \link SamClass::StartModule \endlink
    *  should be run.
    */
   virtual void SamInit(void)=0;

   /** \brief Method that shoud be run in main program loop.
    *  
    *  This is pure virtual method and it should be filled.
    *  This method should be run inside the main loop of the program. It should
    *  contains algorith that should be performed in each itteration of the
    *  given Samgar module.
    */   
   virtual void SamIter(void)=0;

   /** \brief does nothing
    *  \todo fill it or remove
    */
   virtual void onRead(yarp::os::Bottle& b);

   /** \brief Send report
    *
    *  This method is responsible for sending reports to the main control
    *  module. The name of the control module is define by the preprocessor
    *  variable SAM_CONTROL_PORT_NAME
    */
   void report(const yarp::os::PortInfo& info);
   
   /** \brief Add new port to module
    *
    *  \param portPtr  pointer to the port 
    *  \param portName name of the port, should be unique for a given module
    */
   void newPort(yarp::os::Contactable* portPtr, std::string portName);
};
