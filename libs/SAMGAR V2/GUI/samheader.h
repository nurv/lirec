#ifndef _SAMGARHEADER_H
#define _SAMGARHEADER_H

#include "server3Gui.h"

//#define maxmodules 50
//#define maxconns   100
//#define maxports   10

MyConnectionView *myconnwindow;

class DataPort : public BufferedPort<Bottle> 
{
	 
     virtual void onRead(Bottle& b) // will it wait for this one to finish before calling it again?
	 {
		 string mystring = b.get(0).asString().c_str();
		 if(mystring.compare("Add_Module")==0)
		 {
			 string portnames[maxports];
			 string name=b.get(1).asString().c_str();
			 for(int uu = 2;uu<b.size();uu++)
			 {
				 portnames[uu-2]=b.get(uu).asString().c_str();
			 }
			 myconnwindow->AddAlterModule(SAMdef::add_module,name,portnames,0,0,true);
		 }
		 else if(mystring.compare("Active_connection")==0)
		 {
			 myconnwindow->mylogwindow->WriteToLog("in active connection",true);
			 string fake;
			 string fake10[maxports];
			 myconnwindow->AddAlterModule(SAMdef::online_connection,fake,fake10,0,0,false,b.get(1).asString().c_str(),b.get(2).asString().c_str());			
		 }
		else if(mystring.compare("Disactive_connection")==0)
		 {

			 string fake;
			 string fake10[maxports];
			 myconnwindow->AddAlterModule(SAMdef::offline_connection,fake,fake10,0,0,false,b.get(1).asString().c_str(),b.get(2).asString().c_str());			
		 }
     }
};
class myPortReport:public PortReport
{
 void report(const PortInfo& info)
 {
	if(info.created)
	{
	printf("found con %s \n",info.sourceName.c_str());
	myconnwindow->AddAlterModule(SAMdef::online_module,info.sourceName.c_str());
	}
	else
	{
	printf("lost con %s \n",info.sourceName.c_str());
	myconnwindow->AddAlterModule(SAMdef::offline_module,info.sourceName.c_str());
	}

 }
};

#endif 
