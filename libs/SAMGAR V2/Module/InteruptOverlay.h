#include <yarp/os/all.h>
#include <yarp/sig/all.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <iostream>
using namespace yarp;
using namespace yarp::os;
using namespace yarp::sig;
using namespace std;

///// a special class for sending and reciving data from the server, HIVE mind
class MainPortOverlay : public BufferedPort<Bottle> 
{
  private:

  public:
   void SetUp(string name)
   {
      this->open(name.c_str());
   }
   virtual void onRead(Bottle& b) 
   {
      // process data in b
   }
};


class myPortReport : public PortReport
{
  public:
   BufferedPort<Bottle> *CopyMainPort;
   void report(const PortInfo& info)
   {
      string s1,s2;
      s1 = info.sourceName.c_str();
      s2 = info.targetName.c_str();

      if(s1.compare("/Control")!=0 && s2.compare("/Control")!=0)
      {
	 if(info.created)
	 {
	    //puts("********************************* active connection********************");
	    while(CopyMainPort->isWriting()){}
	    Bottle& B = CopyMainPort->prepare();
	    B.clear();
	    B.addString("Active_connection");
	    B.addString(info.sourceName.c_str());
	    B.addString(info.targetName.c_str());
	    CopyMainPort->write();	
	 }
	 else
	 {
	    while(CopyMainPort->isWriting()){}
	    Bottle& B = CopyMainPort->prepare();
	    B.clear();
	    B.addString("Disactive_connection");
	    B.addString(info.sourceName.c_str());
	    B.addString(info.targetName.c_str());
	    CopyMainPort->write();
	 }
      }
   }
};




