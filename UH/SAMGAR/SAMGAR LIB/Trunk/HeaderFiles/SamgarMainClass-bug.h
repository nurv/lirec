
#include <UserVars.h>


struct ModuleStruct
{
string name;
string catagory;
string subcatagory;
};


bool ListSortingFunction(ModuleStruct x,ModuleStruct y);
bool ListDeleatingFunction(ModuleStruct x,ModuleStruct y);


class DataPort : public BufferedPort<Bottle> 
{
private:
	 int *Status;
	
     
public:

	 Bottle SavedBottle;
	 list<Bottle> SavedBottles;
	 int istherebottle;
	int InRead;

	virtual void onRead(Bottle& b)
	{
	//	InRead=1;
		Status = running;   // if interupt this changes the status to running
		SavedBottle=b;
		istherebottle=1;
	//	InRead=0;
	}
	 DataPort(int lala) {*Status = *lala; }
};

// My main class

 class SamgarModule : public BufferedPort<Bottle> 
{
private :

	 int currentmode;
	 int modulemode;
	 bool DoIWantModules;

	 /* lists all the ports available for a module */
	 list<DataPort*> PortList;
	 /* Iterator for port lists */
	 list<DataPort*>::iterator ItPort;
	 /* module specific data */
	 string MyName,MyCatagory,MySubCatagory,MyType;
	 /* a special port for images only */
	 BufferedPort<ImageOf<PixelBgr> > YarpImagePort;
	 /* a special image holder */
	 ImageOf<PixelBgr> yarpImage;
	 /* only for internal use , use acessor methods to retrive data */
	 bool GetDataFromPort(string NameOfPort,int TypeOfData, int *I = 0,float *F =0,double *D =0 , string *S= 0,Bottle *B =0);
	 /* Only for internal use , user acessor methods to send data */
	 void SendData(int type,string port,string S ,int I ,double D,float F,Bottle B);
	

public :

	void GetAvailPlatforms(void);

	// you can acess this list whenever you want really
	// should all be good , it should work and be updated etc
    list<ModuleStruct> ListOfKnownModules;
	list<string> ListOfKnownPlatforms;

    void TurnOnModuleListener(void);

	BufferedPort<Bottle> TempPort;
	
	void SendAllModulesCommand(int cm,string comm);

	void SendModuleCommand(string name,int cm);
	/* A interupt module method, takes the core data ie respond with type, pause stop etc */
	void onRead(Bottle& b) ;
	
	/* constructor method, give the module a name and catagories, ie locomotion,wheel. only put in type continuas run/ interupt run */
	SamgarModule(string NameOfModule,string Catagory,string SubCatagory,int Type);

	/* a a port to your module with a given name */
	void AddPortS(string outputname);
	/* give a name and setup the image port */
	void SetupImagePort(string outputname);
	/* get the int data of port named, also give it the memory locatoin of interger to be chaned, returns true if new data avail */
	bool GetIntData(string NameOfPort,int *I);
	/* get the float data of port named, also give it the memory locatoin of float to be chaned, returns true if new data avail */
	bool GetFloatData(string NameOfPort,float *I);
	/* get the double data of port named, also give it the memory locatoin of double to be chaned, returns true if new data avail */
	bool GetDoubleData(string NameOfPort,double *I);
	/* get the string data of port named, also give it the memory locatoin of string to be chaned, returns true if new data avail */
	bool GetStringData(string NameOfPort,string *I);
	/* get the Bottle data of port named, also give it the memory locatoin of string to be chaned, returns true if new data avail */
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
	/* if a interupt port then this ends the current run, intill anyport gets new data, its also needid as it allows stopping and pausing of the program */
	void SucceedFail(bool Awns,double additoinaldata);
	/* Send data to the main gui log */
	void SendToLog(string LogData,int priority);
	/* Migrate */
	void Migrate(string migratewhere);
	/* find possible migrates */
	list<string> WherePossMigrate(void);

};

