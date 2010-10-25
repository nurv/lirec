#include "SamClass.h"

SamClass::SamClass(std::string newName)
{
  name = newName;
  this->open(name.c_str());
  this->addOutput(SAM_CONTROL_PORT_NAME,"tcp");
}

void SamClass::StartModule()
{
   while(this->isWriting()){}
   yarp::os::Bottle& B = this->prepare();
   B.addString("Add_Module");
   B.addString(name.c_str());

   std::map<std::string, yarp::os::Contactable*>::iterator it;
   for (it = portsPtrContainer.begin(); it != portsPtrContainer.end(); it++ )
      B.addString((*it).first.c_str());

   this->write();
}

void SamClass::RegisterForMigration(void)
{
   // switch namespaces
   // open port
   // switch namespace back
}

void SamClass::UpdateMigratoinPlatforms(void)
{
   // switch to global namespace
   // get a list of possible platforms 
   //string PossibleMigrationPlatforms[maxmigraionplatforms];
}

bool SamClass::ConnectToPlatform(std::string platform)
{
   //switch namespaces
   // uses yarp.connect(thisname,platformname)
}

/** \brief does nothing
 */
void SamClass::onRead(yarp::os::Bottle& b)
{
   // process data in b
} 

void SamClass::report(const yarp::os::PortInfo& info)
{
   std::string source, target;
   source = info.sourceName.c_str();
   target = info.targetName.c_str();
   
   // prevent sending message to itself
   if(source.compare(SAM_CONTROL_PORT_NAME)!=0 && target.compare(SAM_CONTROL_PORT_NAME)!=0)
   {
      if(info.created)
      {
	 //puts("********************************* active connection********************");
	 while(this->isWriting()){}
	 yarp::os::Bottle& B = this->prepare();
	 B.clear();
	 B.addString("Active_connection");
	 B.addString(info.sourceName.c_str());
	 B.addString(info.targetName.c_str());
	 this->write();	
      }
      else
      {
	 while(this->isWriting()){}
	 yarp::os::Bottle& B = this->prepare();
	 B.clear();
	 B.addString("Disactive_connection");
	 B.addString(info.sourceName.c_str());
	 B.addString(info.targetName.c_str());
	 this->write();
      }
   }
}

void  SamClass::newPort(yarp::os::Contactable* portPtr, std::string portName)
{
   portPtr->open( (name+"_"+portName).c_str() );		  // open the port with MODULENAME_PORTNAME
   portPtr->setReporter(*this);	  // set reporter, this is important
   portsPtrContainer.insert( std::pair<std::string, yarp::os::Contactable*>(portName,portPtr) );
}
