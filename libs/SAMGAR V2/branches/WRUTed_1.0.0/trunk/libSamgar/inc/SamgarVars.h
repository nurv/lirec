#ifndef SAMGARVARS_H
#define SAMGARVARS_H

namespace Samgar{

  /*! data types
   *
   */
  enum DataType {
    TypeInt,     /*!< Integer data, */  
    TypeString,  /*!< String data, */
    TypeDouble,  /*!< Double data, */
    TypeBottle,  /*!< Bottle data */
  };
  
  /*! modules types
   *
   */
  enum ModuleMode {
    ModeInterupt,    /*!< interupt module, */
    ModeRun          /*!< run module */
  };
  
  /*! modes types
   *
   */
  enum ModuleState{
    StateRunning,      /*!< module is running, */
    StatePaused,       /*!< module is passed, */
    StateStoped,       /*!< module is stoped, */
    StateFullstop      /*!< module is in fullstop */
  };

  enum SamgarSpecialCodes {
    ModuleInfoCode = 10,
    ActivationCode = 20,
    LogReportCode = 30,
    AvailablePlatformsCode = 40,  
    ResetAvailablePlatformsCode = 50,
    ResetModulesListCode = 105
  };

  enum ModuleStateCodes{
      ModuleStateFullstopAll = 0,
      ModuleStateRunningAll = 1,
      ModuleStateInfoCodeAll = 2,
      ModuleStateFullstop = 3,
      ModuleStateRunning = 4,
      ModuleStateInfoCode = 5,
  };

} // namespace Samgar

#endif // SAMGARVARS_H
