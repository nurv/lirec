#include <Samgar.h>
#include <iostream>

using namespace std;
using namespace yarp::os;
using namespace yarp::sig;

namespace Samgar{

////////////////////////////////////////////////////////////////////////////////
// Data Port
////////////////////////////////////////////////////////////////////////////////

  void DataPort::onRead(Bottle& b)
  {
    *Status = StateRunning;
    SavedBottle=b;
  }
  
  Bottle DataPort::getBottle(){
    Bottle result=SavedBottle;
    SavedBottle=Bottle::getNullBottle();
    return result;
  }

  DataPort::DataPort(ModuleState *state) {
    Status = state; 
    SavedBottle=Bottle::getNullBottle(); 
  }

////////////////////////////////////////////////////////////////////////////////

  /*! 
   * Constructor class, needs four varibles
   * first is the name of module on the network
   * secound is the main catagory of the module
   * third is the sub catagory of the module
   * forth is the type of module either -ModeRun -ModeInterupt
   */
  SamgarModule::SamgarModule(string NameOfModule, string Catagory,
			     string SubCatagory, ModuleMode Type) {
    Network yarp;
    MyName          = NameOfModule;
    MyCatagory      = Catagory;
    MySubCatagory   = SubCatagory;
    modulemode      = Type;
    fullModuleName  = "/Main_" + MyName;
    DoIWantModules  = false;
    if( modulemode == ModeInterupt )
        currentmode = Samgar::StateStoped;
    else
        currentmode = Samgar::StateRunning;
  }

  /*!
   * A method which enables the module list to be updated when new data
   * is available or called for on the network
   * SendAllModulesCommand can be used to update the module list
   */
  void SamgarModule::TurnOnModuleListener(void){
    DoIWantModules=true;
  }

  /*!
   * Sends a query to SAMGAR Key to get available platforms
   */
  void SamgarModule::GetAvailPlatforms(void) {
    Bottle& MyBottle = prepare();
    MyBottle.clear();
    MyBottle.addInt( AvailablePlatformsCode );
    write();
  }

  /*!
   * This function sends commands to all available modules within the group
   * 0 = stop module , 1 = start module , 2 = get modules to respond with 
   * name and type
   */
  void SamgarModule::SendAllModulesCommand(int cm) {
    Bottle& MyBottle = prepare();
    MyBottle.clear();
    MyBottle.addInt(cm);
    write();
  }

  /*!
   * This function sends commands to a singuler module
   * 3 = stop module , 4 = start module , 5 = get modules to respond
   * with name and type 
   */
  void SamgarModule::SendModuleCommand(string name, int cm) {
    Bottle& MyBottle = prepare();
    MyBottle.clear();
    MyBottle.addInt(cm);
    MyBottle.addString(name.c_str()); // always adds to end of current list
    write();
  }

  /*!
   * This function is a interupt driven module for the main port of the module
   * DO NOT MANUALLY CALL
   */
  void SamgarModule::onRead(Bottle& b) {
    int myswitch = b.get(0).asInt();
    //std::cout<<"got some data with int "<<myswitch<<std::endl;

    /// have here a statment that catchs 10's is the user has specified it
    if( DoIWantModules )
    {
        switch (myswitch)
        {
        case ResetModulesListCode:
            {
                int FF;
                ModuleStruct TempStruct;
                printf("got new modules \n");
                ListOfKnownModules.clear();
                FF = 1;
                while(FF<=b.size())
                {
                    TempStruct.name        = b.get(FF).asString().c_str();FF++;
                    TempStruct.catagory    = b.get(FF).asString().c_str();FF++;
                    TempStruct.subcatagory = b.get(FF).asString().c_str();FF++;
                    ListOfKnownModules.push_front(TempStruct);
                }
            }
            break;
        case ResetAvailablePlatformsCode:
            ListOfKnownPlatforms.clear();
            printf("got new platforms \n");
            for(int hh =1; hh<b.size(); hh++)
            {
                ListOfKnownPlatforms.push_front(b.get(hh).asString().c_str());
            }
            break;
        }
    }

    switch (myswitch)
    {
    case ModuleStateFullstopAll :
        currentmode = StateFullstop;
        //printf("mode changed to stop \n");
        break; // get all modules to stop
    case ModuleStateRunningAll :
        currentmode = StateRunning;
        //printf("mode changed to running \n");
        break;  // get all modules to go again
    case ModuleStateInfoCodeAll :
                  printf("Sent some catagory data from a module \n");
        Bottle& MyBottle = prepare();
        MyBottle.clear();
        MyBottle.addInt( ModuleInfoCode ); // code for gui
        MyBottle.addString( MyName.c_str() );
        MyBottle.addString( MyCatagory.c_str() );
        MyBottle.addString( MySubCatagory.c_str() );
        write();
        break;
    }

    if (myswitch>ModuleStateInfoCodeAll)
    {
        string mestring = b.get( 1 ).asString().c_str();
        //if(myswitch>2 && b.get(1).asString().c_str() == MyName.c_str()) // for personal commands
        if( mestring.compare( MyName ) == 0)
        {
            //printf("got into secound statment to turn off modules \n");
            switch ( myswitch )
            {
            case ModuleStateFullstop :
                currentmode = StateFullstop;
                break; // get all modules to stop
            case ModuleStateRunning :
                currentmode = StateRunning;
                break;  // get all modules to go again
            case ModuleStateInfoCode :
                Bottle& MyBottle = prepare();
                MyBottle.clear();
                MyBottle.addInt( ModuleInfoCode ); // code for gui
                MyBottle.addString( MyName.c_str() );
                MyBottle.addString( MyCatagory.c_str() );
                MyBottle.addString( MySubCatagory.c_str() );
                write();
                break;
            }
        }
    }
  }

  /*! equality operator
   */
  bool operator==( ModuleStruct x, ModuleStruct y) {
    if( x.name == y.name ) return true;
    return false;
  }

  /*! grater than operator
   */
  bool operator>( ModuleStruct x, ModuleStruct y) {
    if(x.name>y.name) return true;
    return false;
  }

  /*! 
   * Setups a special port for images only 
   */
  void SamgarModule::SetupImagePort( string outputname ) {
    string Tempname = "/Port_" + MyName + "_" + outputname;
    //Time::delay(1);
    imagePortName = Tempname.c_str();
    //YarpImagePort.open( Tempname.c_str() );
  }

  /*! 
   * sends a picture out in the yarp native format 
   */
  void SamgarModule::SendPictureYarpNative( ImageOf<PixelBgr> Image ) {
    YarpImagePort.prepare() = Image; // no longer likes this line
    YarpImagePort.write();
  }

  /*! 
   * Recives a picture in the yarp native format 
   */
  ImageOf<PixelBgr> SamgarModule::RecivePictureYarpNative( void ) {
    ImageOf<PixelBgr> TempImage = *YarpImagePort.read();
    return TempImage;
  }

  /*! 
   * Sends a picture in the OpenCV native format IplImage 
   */
  void SamgarModule::SendPictureOCVNative( IplImage* Image ) {
    ImageOf<PixelBgr> yarpReturnImage;
    yarpReturnImage.wrapIplImage( Image );
    SendPictureYarpNative( yarpReturnImage );
  }

  /*! 
   * Revices a picture in the OpenCV native formate IplImage 
   */
  IplImage* SamgarModule::RecivePictureOCVNative( void ) {
    if( YarpImagePort.getInputCount() > 0 ) {
      ImageOf<PixelBgr> *img = YarpImagePort.read();
      IplImage *frame2 = (IplImage*) img->getIplImage();
      return frame2;
    }
    return false;
  }

  /*! 
   * Very important function, allows all modules to sleep/start from 
   * send commands. Also if the module has been designated as a interupt
   * module then it will send back false for failure also can have 
   * additional data on how well the module has done 
   */
  void SamgarModule::SucceedFail( bool Awns, double additionaldata ) {
    if( modulemode == ModeInterupt ) {// only send it when you got
				      // worthwhile data
      Bottle &MyBottle = prepare();
      MyBottle.clear();
      MyBottle.addInt( ActivationCode ); // so it knows its a log report
      MyBottle.addString(MyName.c_str());
      MyBottle.addDouble(additionaldata);
      if(getOutputCount()>0)
        write(); 
      currentmode = StateStoped;
    }
    long i;
    while( currentmode != StateRunning)
    {
      yarp::os::Time::delay(0.01);
      i++;
      if (i%100==0)
      {
        checkConnection();
        //return;
      }
    }
    checkConnection();
  }

  /*!
   * Sends a message for the log in the gui
   */
  void SamgarModule::SendToLog( string LogData, int priority ) {
    Bottle &MyBottle = prepare();
    MyBottle.clear();
    MyBottle.addInt( LogReportCode );
    MyBottle.addString(MyName.c_str());
    MyBottle.addString(LogData.c_str());
    MyBottle.addInt(priority);
    write();
  }

  /*!
   * Just adds a port to the module
   */
  void SamgarModule::AddPortS( string outputname ) {
    string Tempname = "/Port_" + MyName + "_" + outputname;
    portNameList.push_front(Tempname);
  }

  /*! 
   *  simpler function calls to the bigger functions 
   */
  void SamgarModule::SendBottleData(string port,Bottle data){
    SendData(TypeBottle,port,(void*)&data);
  }

  /*! 
   *  simpler function calls to the bigger functions 
   */
  void SamgarModule::SendIntData(string port,int data){
    SendData(TypeInt,port,(void*)&data);
  }

  /*! 
   * simpler function calls to the bigger functions 
   */
  void SamgarModule::SendDoubleData(string port,double data){
    SendData(TypeDouble,port,(void*)&data);
  }

  /*!
   * simpler function calls to the bigger functions 
   */
  void SamgarModule::SendStringData(string port,string data){
    SendData(TypeString,port,(void*)&data);
  }

  /*! 
   * private function, DO NOT CALL DIRECTLY 
   */
  void SamgarModule::SendData(DataType type,string port,void* data) {
    DataPort* MyTempPort;
    list<DataPort*>::iterator ItPort;
    string TempName;
    bool FoundPort;

    TempName="/Port_" + MyName + "_" + port.c_str();

    FoundPort=false;
    for ( ItPort=PortList.begin() ; ItPort != PortList.end(); ItPort++ ) {
      MyTempPort=*ItPort;
      if(MyTempPort->getName() == TempName.c_str()) {
	FoundPort=true;
	break; // we have the port so break the loop
      }
    }
    if(FoundPort==true) {
      Bottle& MyBottle = MyTempPort-> prepare();
      MyBottle.clear();
      switch (type){
      case TypeInt:    MyBottle.addInt( *((int*) data) ); break;
      case TypeDouble: MyBottle.addDouble( *((double*) data) ); break;
      case TypeString: MyBottle.addString( ((string*) data)->c_str()); break;
      case TypeBottle: MyBottle = *((Bottle*)data); break;
      }
      MyTempPort->write();
    }
  }



  //These are just window dressing to make it easer for the user, instead of using GetDataFromPort

  /*! 
   * Gets int data from port, you give it the int you want changed and
   * it changes it, it also replys with weather the port has been
   * updated True/False 
   */
  bool SamgarModule::GetIntData(string NameOfPort,int *I){
    return GetDataFromPort(NameOfPort,TypeInt,I);
  }

  /*! 
   * Gets double data from port, you give it the int you want changed 
   * and it changes it, it also replys with weather the port has been 
   * updated True/False 
   */
  bool SamgarModule::GetDoubleData(string NameOfPort,double *I){
    return GetDataFromPort(NameOfPort,TypeDouble,I);
  }

  /*! 
   * Gets string data from port, you give it the int you want changed 
   * and it changes it, it also replys with weather the port has been 
   * updated True/False 
   */
  bool SamgarModule::GetStringData(string NameOfPort,string *I){
    return GetDataFromPort(NameOfPort,TypeString,I);
  }

  /*! 
   * Gets Bottle data from port, you give it the int you want changed
   * and it changes it, it also replys with weather the port has been 
   * updated True/False 
   */
  bool SamgarModule::GetBottleData(string NameOfPort,Bottle *I){
    return GetDataFromPort(NameOfPort,TypeBottle,I);
  }

  /*! 
   * DO NOT CALL DIRECTLY 
   */
  bool SamgarModule::GetDataFromPort(string NameOfPort,int TypeOfData, void* data) {
    list<DataPort*>::iterator ItPort;
    DataPort *MyTempPort;
    bool HaveIfoundPort;
  
    NameOfPort = "/Port_" + MyName + "_" + NameOfPort.c_str();
    HaveIfoundPort = false;

    for ( ItPort=PortList.begin(); ItPort != PortList.end(); ItPort++ ) {// find the right port loop
      MyTempPort=*ItPort;
      if(MyTempPort->getName() == NameOfPort.c_str()){
	HaveIfoundPort = true;
	break;
      }// have the data we need so break the loop
    }

    if ( !HaveIfoundPort )
	return false;

    if (MyTempPort->isClosed())
        return false;

    if(MyTempPort->getInputCount() == 0)
      return false; // if nothings connected to it  	

    // gets Bottle from  the DataPort storage
    Bottle MyBottle=MyTempPort->getBottle();

      if(MyBottle.isNull())
	return false;
    
      if (TypeOfData == TypeInt && MyBottle.get(0).isInt() != false) {
	*((int*)data) = MyBottle.get(0).asInt();
	return true;
      }
      else if (TypeOfData == TypeString && MyBottle.get(0).isString() != false) {
	*((string*)data) = MyBottle.get(0).asString();
	return true;
      }
      else if(TypeOfData == TypeDouble && MyBottle.get(0).isDouble() != false) {
	*((double*)data) = MyBottle.get(0).asDouble();
	return true;
      }
      else if(TypeOfData == TypeBottle) {
	*((Bottle*)data) = MyBottle;
	return true;
      }
    return false;
  }

  bool YARPexists(std::string portName)
  {
      static FILE *inpipe;
      char inbuf[200];

      inpipe =  popen((std::string("yarp exists ")+portName+" ; echo $?").c_str(),"r");
      if (!inpipe){ std::cout<<"PING FAILED"<<std::endl;}
      else
      {
          if (fgets(inbuf, sizeof(inbuf), inpipe))
            {
              std::string mystring = inbuf;
              if (inbuf[0]=='0')
              {
                  pclose(inpipe);
                  return true;
              }
          }
      }
      pclose(inpipe);
      return false;
  }

  bool SamgarModule::checkConnection()
  {
      ModuleState oldCurrentMode;
      oldCurrentMode = currentmode;
      if (YARPexists("/PortForModules"))
      {
          if (!yarp::os::Network::isConnected("/PortForModules",fullModuleName.c_str()) ||
              !yarp::os::Network::isConnected(fullModuleName.c_str(),"/PortForModules"))
          {
              currentmode     = StateStoped;
              // there is no connection with /PortForModules so we shut down all ports
              disableCallback();
              close();
              //yarp::os::Network::unregisterName(fullModuleName.c_str());
              list<DataPort*>::iterator ItPort;
              for ( ItPort=PortList.begin(); ItPort != PortList.end(); ItPort++ )
              {
                  (*ItPort)->disableCallback();
                  (*ItPort)->close();
                  //yarp::os::Network::unregisterName((*ItPort)->getName());
              }
              if (!imagePortName.empty())
              YarpImagePort.close();

              //yarp::os::Time::delay(0.1);
              while(Network::getNameServerName()=="/global"){;}
              //if (this->where().getName() == fullModuleName.c_str())
              //  open(this->where());
              //else
              while (!open(fullModuleName.c_str()))
              {
                  yarp::os::Network::unregisterName(fullModuleName.c_str());
                  close();
              }
              this->useCallback();
              //yarp::os::Time::delay(0.1);
              Bottle& MyBottle = prepare();
              MyBottle.addInt( ModuleInfoCode ); // code for gui
              MyBottle.addString( MyName.c_str() );
              MyBottle.addString( MyCatagory.c_str() );
              MyBottle.addString( MySubCatagory.c_str() );
              write();
              //yarp::os::Time::delay(0.1);
              PortList.clear();
              for ( std::list<std::string>::iterator ItName=portNameList.begin(); ItName != portNameList.end(); ItName++ )
              {
                  PortList.push_front( new DataPort(&currentmode) );
                  PortList.front()->open((*ItName).c_str());
                  PortList.front()->useCallback();
              }
              if (!imagePortName.empty())
              YarpImagePort.open( imagePortName.c_str() );
              currentmode = oldCurrentMode;
          }
          //else
          //    currentmode     = StateRunning;
      }
      return false;
  }

  ModuleState SamgarModule::getCurrentState()
  {
      return currentmode;
  }

} // namespace Samgar
