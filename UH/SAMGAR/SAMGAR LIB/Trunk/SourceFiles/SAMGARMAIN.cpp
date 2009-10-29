

#include <SamgarMainClass.h>

using namespace std;
using namespace yarp::os;
using namespace yarp::sig;

/*! 
Constructor class, needs four varibles
first is the name of module on the network
secound is the main catagory of the module
third is the sub catagory of the module
forth is the type of module either -run -interupt
!*/
SamgarModule::SamgarModule(string NameOfModule,string Catagory,string SubCatagory,int Type)
{
	Network yarp;
MyName			=NameOfModule;
MyCatagory		=Catagory;
MySubCatagory	=SubCatagory;
modulemode		=Type;
string Tempname = "/Main_" + MyName;
DoIWantModules = false;
while(Network::getNameServerName()=="/global"){;}
open(Tempname.c_str());
currentmode=running;
useCallback();

while(getInputCount()<1){;}
yarp::os::Time::delay(3);
// upon innit give its data to the gui
Bottle& MyBottle =prepare();
MyBottle.addInt(10); // code for gui
MyBottle.addString(MyName.c_str());
MyBottle.addString(MyCatagory.c_str());
MyBottle.addString(MySubCatagory.c_str());
write();

//setstrict
//this
}
/*!
A method which enables the module list to be updated when new data is available or called for on the network
SendAllModulesCommand can be used to update the module list
!*/
void SamgarModule::TurnOnModuleListener(void){DoIWantModules=true;}

/*!
Sends a query to SAMGAR Key to get available platforms
!*/
void SamgarModule::GetAvailPlatforms(void)
{
Bottle& MyBottle =prepare();
MyBottle.clear();
MyBottle.addInt(40);
write();
}



/*!
This function sends commands to all available modules within the group
0 = stop module , 1 = start module , 2 = get modules to respond with name and type
!*/
void SamgarModule::SendAllModulesCommand(int cm)
{
Bottle& MyBottle =prepare();
MyBottle.clear();
MyBottle.addInt(cm);
write();
}


/*!
This function sends commands to a singuler module
3 = stop module , 4 = start module , 5 = get modules to respond with name and type
!*/
void SamgarModule::SendModuleCommand(string name,int cm)
{
Bottle& MyBottle =prepare();
MyBottle.clear();
MyBottle.addInt(cm);
MyBottle.addString(name.c_str()); // always adds to end of current list
write();
}

/*!
This function is a interupt driven module for the main port of the module
DO NOT MANUALLY CALL
!*/

void SamgarModule::onRead(Bottle& b) 
{
int myswitch = b.get(0).asInt();
Bottle& MyBottle =prepare();
MyBottle.clear();


//printf("got some data with int %i \n",myswitch);

/// have here a statment that catchs 10's is the user has specified it
if(b.get(0).asInt()==105)
{

if(DoIWantModules==true)
{
	ModuleStruct TempStruct;
	ListOfKnownModules.clear();
	int FF;
	FF=1;
	while(FF<=b.size())
	{
		TempStruct.name        =b.get(FF).asString().c_str();FF++;
		TempStruct.catagory    =b.get(FF).asString().c_str();FF++;
		TempStruct.subcatagory =b.get(FF).asString().c_str();FF++;
		ListOfKnownModules.push_front(TempStruct);
	}

}
}
if(b.get(0).asInt()==50 && DoIWantModules==true)
{
	ListOfKnownPlatforms.clear();

	for(int hh =1;hh<b.size();hh++)
	{
		ListOfKnownPlatforms.push_front(b.get(hh).asString().c_str());
	}
}


switch (myswitch)
{
case 0 :
	currentmode=fullstop;
//	printf("mode changed to stop \n");
	break; // get all modules to stop
case 1 :
	currentmode=running; 
//	printf("mode changed to running \n");
	break;  // get all modules to go again
case 2 :
//	  printf("Sent some catagory data from a module \n");
      MyBottle.addInt(10); // code for gui
	  MyBottle.addString(MyName.c_str());
	  MyBottle.addString(MyCatagory.c_str());
	  MyBottle.addString(MySubCatagory.c_str());
      write();
	  break;
}


string mestring = b.get(1).asString().c_str();
//if(myswitch>2 && b.get(1).asString()/*.c_str()*/ == MyName/*.c_str()*/) // for personal commands
if(myswitch>2 && mestring.compare(MyName)==0)
{
//	printf("got into secound statment to turn off modules \n");
	switch (myswitch)
	{
	case 3 :currentmode=fullstop;break; // get all modules to stop
	case 4 :currentmode=running; break;  // get all modules to go again
	case 5 :
      MyBottle.addInt(10); // code for gui
	  MyBottle.addString(MyName.c_str());
	  MyBottle.addString(MyCatagory.c_str());
	  MyBottle.addString(MySubCatagory.c_str());
      write();
	  break;
	}
}
}
/*! Deleates copys of varibles in the known module list !*/
bool ListDeleatingFunction(ModuleStruct x,ModuleStruct y)
{
	if(x.name==y.name){return true;}
	return false;
}

/*! Sorts the varibles in the known module list!*/
bool ListSortingFunction(ModuleStruct x,ModuleStruct y)
{
	if(x.name>y.name){return true;}
	return false;
}



/*! Setups a special port for images only !*/
void SamgarModule::SetupImagePort(string outputname)
{
string Tempname = "/Port_" + MyName + "_" + outputname;
Time::delay(1);
YarpImagePort.open(Tempname.c_str());
}

/*! sends a picture out in the yarp native format !*/
void SamgarModule::SendPictureYarpNative(ImageOf<PixelBgr> Image)
{
YarpImagePort.prepare() =Image; // no longer likes this line
YarpImagePort.write();
}
/*! Recives a picture in the yarp native format !*/
ImageOf<PixelBgr> SamgarModule::RecivePictureYarpNative(void)
{
ImageOf<PixelBgr> TempImage = *YarpImagePort.read();
return TempImage;
}

/*! Sends a picture in the OpenCV native format IplImage !*/
void SamgarModule::SendPictureOCVNative(IplImage* Image)
{
	ImageOf<PixelBgr> yarpReturnImage;
    yarpReturnImage.wrapIplImage(Image);
	SendPictureYarpNative(yarpReturnImage);
}
/*! Revices a picture in the OpenCV native formate IplImage !*/
IplImage* SamgarModule::RecivePictureOCVNative(void)
{
if(YarpImagePort.getInputCount()>0)
	{
	ImageOf<PixelBgr> *img = YarpImagePort.read();
	IplImage *frame2=(IplImage*) img->getIplImage();
	return frame2;
	}
return false;
}



/*! 
Very important function, allows all modules to sleep/start from send commands 
Also if the module has been designated as a interupt module then it will send back 
false for failure also can have additional data on how well the module has done
!*/


void SamgarModule::SucceedFail(bool Awns,double additionaldata)
{

if(modulemode==interupt )// only send it when you got worthwhile data
	{
	Bottle& MyBottle =prepare();
	MyBottle.clear();
	string Tempstring;
	MyBottle.addInt(20); // so it knows its a log report
	MyBottle.addString(MyName.c_str());
	MyBottle.addDouble(additionaldata);
	if(getOutputCount()>0){write();}
	currentmode = stoped;
	}

while(currentmode!=running)
	{
	yarp::os::Time::delay(0.01);
	}


}



/*!
Sends a message for the log in the gui
!*/
void SamgarModule::SendToLog(string LogData,int priority)
{
Bottle& MyBottle =prepare();
MyBottle.clear();
MyBottle.addInt(30);
MyBottle.addString(MyName.c_str());
MyBottle.addString(LogData.c_str());
MyBottle.addInt(priority);
write();

}
/*!
Just adds a port to the module
!*/
void SamgarModule::AddPortS(string outputname)
{
	string Tempname = "/Port_" + MyName + "_" + outputname;
	PortList.push_front(new DataPort(&currentmode));
	Time::delay(1);
	while(Network::getNameServerName()=="/global"){;}
	PortList.front()->open(Tempname.c_str());
	PortList.front()->useCallback();
}




void SamgarModule::SendBottleData(string port,Bottle data){SendData(TypeBottle,port,0,0,0,0,data);}
/*! simpler function calls to the bigger functions !*/
void SamgarModule::SendIntData(string port,int data){SendData(TypeInt,port," ",data,0,0,0);}
/*! simpler function calls to the bigger functions !*/
void SamgarModule::SendFloatData(string port,float data){SendData(TypeFloat,port," ",0,0,data,0);}
/*! simpler function calls to the bigger functions !*/
void SamgarModule::SendDoubleData(string port,double data){SendData(TypeDouble,port," ",0,data,0,0);}
/*! simpler function calls to the bigger functions !*/
void SamgarModule::SendStringData(string port,string data){SendData(TypeString,port,data,0,0,0,0);}

/*! private function, DO NOT CALL DIRECTLY !*/
void SamgarModule::SendData(int type,string port,string S,int I ,double D,float F,Bottle B)
{
	BufferedPort<Bottle> *MyTempPort;
	string TempName;
	bool FoundPort;

	TempName="/Port_" + MyName + "_" + port.c_str();

	FoundPort=false;
	for ( ItPort=PortList.begin() ; ItPort != PortList.end(); ItPort++ )
	{
		MyTempPort=*ItPort;
		if(MyTempPort->getName()==TempName.c_str())
		{
			FoundPort=true;
		break; // we have the port so break the loop
		}
	}
if(FoundPort==true)
{
	Bottle& MyBottle =MyTempPort-> prepare();
	MyBottle.clear();
	     if(type==TypeInt   ) {MyBottle.addInt   (I);}
	else if(type==TypeFloat ) {MyBottle.addDouble(F);}
	else if(type==TypeDouble) {MyBottle.addDouble(D);}
	else if(type==TypeString) {MyBottle.addString(S.c_str());}
	else if(type==TypeBottle) {MyBottle=B;}
	MyTempPort->write();
}

}



//These are just window dressing to make it easer for the user, instead of using GetDataFromPort

/*! Gets int data from port, you give it the int you want changed and it changes it, it also replys with weather the port has been updated True/False !*/
bool SamgarModule::GetIntData   (string NameOfPort,int    *I){return GetDataFromPort(NameOfPort,TypeInt     ,I,0,0,0,0);}
/*! Gets float data from port, you give it the int you want changed and it changes it, it also replys with weather the port has been updated True/False !*/
bool SamgarModule::GetFloatData (string NameOfPort,float  *I){return GetDataFromPort(NameOfPort,TypeFloat   ,0,I,0,0,0);}
/*! Gets double data from port, you give it the int you want changed and it changes it, it also replys with weather the port has been updated True/False !*/
bool SamgarModule::GetDoubleData(string NameOfPort,double *I){return GetDataFromPort(NameOfPort,TypeDouble  ,0,0,I,0,0);}
/*! Gets string data from port, you give it the int you want changed and it changes it, it also replys with weather the port has been updated True/False !*/
bool SamgarModule::GetStringData(string NameOfPort,string *I){return GetDataFromPort(NameOfPort,TypeString  ,0,0,0,I,0);}
/*! Gets Bottle data from port, you give it the int you want changed and it changes it, it also replys with weather the port has been updated True/False !*/
bool SamgarModule::GetBottleData(string NameOfPort,Bottle *I){return GetDataFromPort(NameOfPort,TypeBottle  ,0,0,0,0,I);}


/*! DO NOT CALL DIRECTLY !*/
bool SamgarModule::GetDataFromPort(string NameOfPort,int TypeOfData, int *I ,float *F ,double *D, string *S ,Bottle *B)
{

//BufferedPort<Bottle> *MyTempPort;

DataPort *MyTempPort;

bool HaveIfoundPort;




NameOfPort="/Port_" + MyName + "_" + NameOfPort.c_str();
HaveIfoundPort=false;

for ( ItPort=PortList.begin() ; ItPort != PortList.end(); ItPort++ ) // find the right port loop
	{
	MyTempPort=*ItPort;
	if(MyTempPort->getName()==NameOfPort.c_str()){HaveIfoundPort=true;break;}// have the data we need so break the loop
	}

if(MyTempPort->getInputCount()==0){return false;} // if nothings connected to it  	

Bottle MyBottle;

//Bottle *MyBottle = MyTempPort->read();
if(MyTempPort->istherebottle==1)
{
 MyBottle = MyTempPort->SavedBottle;
MyTempPort->istherebottle=0;
}
else
{
return false;
}
if(MyBottle==NULL){return false;}
if(HaveIfoundPort==true)
{


if(MyBottle.isNull()==true){return false;}
if(MyBottle==NULL){return false;}

if     (TypeOfData==TypeInt   &&MyBottle.get(0).isInt()   !=false) {*I=MyBottle.get(0).asInt()   ;return true;}
else if(TypeOfData==TypeFloat &&MyBottle.get(0).isDouble()!=false)	{*F=MyBottle.get(0).asDouble();return true;}
else if(TypeOfData==TypeString&&MyBottle.get(0).isString()!=false) {*S=MyBottle.get(0).asString();return true;}
else if(TypeOfData==TypeDouble&&MyBottle.get(0).isDouble()!=false)	{*D=MyBottle.get(0).asDouble();return true;}
else if(TypeOfData==TypeBottle)										{*B=MyBottle;return true;}
}
//MyTempPort->release();  	
return false;
}

