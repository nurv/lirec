#ifndef SAMGAR_H
#define SAMGAR_H
/*! \file Samhar.h
 * File contains definition of the structure ModuleStruct, class
 * DataPort and class SamgarModule
 */ 
#include <yarp/os/all.h>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <list>
#include <yarp/sig/all.h> //image stuff
#include <math.h>
#include <cxtypes.h>
#include <fstream>

#include <SamgarVars.h>

using namespace std;
using namespace yarp::os; // network stuff
using namespace yarp::sig;// image stuff

namespace Samgar{
  /*! \brief Structure containin basic information about module
   */
  struct ModuleStruct 
  {
    string name; //! module name
    /*! module category 
     *
     * \todo maybe it should be standarized like it is in player?
     */
    string catagory; 
    string subcatagory; //! module subcategory
  };
  
  bool operator==(ModuleStruct x, ModuleStruct y);
  bool operator>(ModuleStruct x, ModuleStruct y);


  /*! \brief Port for SAMGAR
   *  This class extends standard BufferedPort<Bottle> from the YARP
   *
   *  \todo Maybe it should be based on template or just allow to send any
   *  Portable objects from YARP?
   */
  class DataPort : public BufferedPort<Bottle> 
  {
  private:
    ModuleState *Status; //! SAMGAR module state \see ModuleState

    Bottle SavedBottle; //! some storage for the Bottle
			//! \todo why only for one?
  
  public:  

    /*! what should be done when the data is being read
     */
    virtual void onRead(Bottle& b);

    /*! get Bottel from the port, and clear Bottle storage in the DataPort
     */
    Bottle getBottle();
    
    /*! \brief constructor
     * /todo add default constructor
     */
    DataPort(ModuleState *modulState);
  };

  /*! \brief SAMGAR main class
   */
  class SamgarModule : public BufferedPort<Bottle> 
  {
  private :

    std::string fullModuleName;
    std::string imagePortName;

    ModuleState currentmode;
    ModuleMode modulemode;
    bool DoIWantModules;

    /* lists all the ports available for a module */
    list<DataPort*> PortList;
    list<std::string> portNameList;
    /* Iterator for port lists */
    //list<DataPort*>::iterator ItPort;
    /* module specific data */
    string MyName,MyCatagory, MySubCatagory, MyType;
    /* a special port for images only */
    BufferedPort<ImageOf<PixelBgr> > YarpImagePort;

    /* a special image holder */
    ImageOf<PixelBgr> yarpImage;
    /* only for internal use , use acessor methods to retrive data */
    bool GetDataFromPort(string NameOfPort,int TypeOfData, void *data);
    //		       int *I = 0, float *F = 0, double *D = 0,
    //                   string *S = 0, Bottle *B = 0);
    /* Only for internal use , user acessor methods to send data */
    void SendData(DataType type, string port, void* data);

  public :
    void GetAvailPlatforms(void);

    ModuleState getCurrentState();

    // you can acess this list whenever you want really
    // should all be good , it should work and be updated etc
    list<ModuleStruct> ListOfKnownModules;
    list<string> ListOfKnownPlatforms;

    void TurnOnModuleListener(void);

    BufferedPort<Bottle> TempPort;
	
    void SendAllModulesCommand(int cm);

    void SendModuleCommand(string name,int cm);
    /* A interupt module method, takes the core data ie respond 
     * with type, pause stop etc 
     */
    virtual void onRead(Bottle& b) ;
	
    /* constructor method, give the module a name and catagories, 
     * ie locomotion,wheel. only put in type continuas run/ interupt
     * run 
     */
    SamgarModule(string NameOfModule,string Catagory,string SubCatagory,ModuleMode Type);

    /* a a port to your module with a given name */
    void AddPortS(string outputname);
    /* give a name and setup the image port */
    void SetupImagePort(string outputname);
    /* get the int data of port named, also give it the memory 
     * locatoin of interger to be chaned, returns true if new data
     * avail 
     */
    bool GetIntData(string NameOfPort,int *I);
    /* get the float data of port named, also give it the memory 
     * locatoin of float to be chaned, returns true if new data avail 
     */
    bool GetFloatData(string NameOfPort,float *I);
    /* get the double data of port named, also give it the memory 
     * locatoin of double to be chaned, returns true if new data avail 
     */
    bool GetDoubleData(string NameOfPort,double *I);
    /* get the string data of port named, also give it the memory 
     * locatoin of string to be chaned, returns true if new data avail 
     */
    bool GetStringData(string NameOfPort,string *I);
    /* get the Bottle data of port named, also give it the memory 
     * locatoin of string to be chaned, returns true if new data avail 
     */
    bool GetBottleData(string NameOfPort,Bottle *B);

    /* send data to port */
    void SendIntData(string port,int data);
    /* send data to port */
    void SendFloatData(string port,float data);
    /* send data to port */
    void SendDoubleData(string port,double data);
    /* send data to port */
    void SendStringData(string port,string data);
    /* send data to port */
    void SendBottleData(string port,Bottle data);

    /* send a picture on port in YARP native Bgr form */
    void SendPictureYarpNative(ImageOf<PixelBgr> Image);
    /* get a picture on port in YARP native Bgr form */
    ImageOf<PixelBgr> RecivePictureYarpNative(void);
    /* for open CV, Send image to port */
    void SendPictureOCVNative( IplImage* Image);
    /* for open CV, Get image from port */
    IplImage* RecivePictureOCVNative(void);
    /* if a interupt port then this ends the current run, intill anyport 
     * gets new data, its also needid as it allows stopping and pausing 
     * of the program 
     */
    void SucceedFail(bool Awns,double additoinaldata);
    /* Send data to the main gui log */
    void SendToLog(string LogData,int priority);
    /* Migrate */
    void Migrate(string migratewhere);
    /* find possible migrates */
    list<string> WherePossMigrate(void);

    bool checkConnection();

  };

} // namespace Samgar

#endif // SAMGAR_H
