#include "SamClass.h"

/****** SAMGAR V2 EXAMPLES******/

/*
a couple of class's showing how to use SAMGAR, they dont have to be done in header files at all, but it can be better
its easer to have a option of compiling them together or seperately, both of which have there advantages in differnt situatoins


if you want to send imgs there is a class in yarp exactly for that, you can just send a IPL image.
*/


// This class just sends out some data //

class ExampleOneWrite: public SamClass 
{
private:
int myint;
BufferedPort<Bottle> myfirst,myfirst2; // create buffered ports for bottles like this
Network yarp;						   // make sure the network is ready

public:
	void SamInit(void)
	{
	myint=0;
	RecognisePort("W1");				// name the port to be shown in the gui
	myfirst.open("/Writer_W1");			// open the port with MODULENAME_PORTNAME
	myfirst.setReporter(myPortStatus);	// set reporter, this is important
	RecognisePort("W2");				// name the port to be shown in the gui
	myfirst2.open("/Writer_W2");		// open the port
	myfirst2.setReporter(myPortStatus);	// set reporter


	StartModule("/Writer");				// we weve addid all ports and are happy then send the module name, this must be done last!!
	puts("started writer");
	}
	void SamIter(void)
	{
	Bottle& B = myfirst.prepare();		// prepare the bottle/port
	B.clear();
	B.addInt(myint++);
	myfirst.write();					// add stuff then send
	Bottle& C = myfirst2.prepare();
	C.clear();
	C.addInt(myint+5);
	myfirst2.write();	
	puts("running writer");
	}
};


class ExampleTwoRead: public SamClass
{
private:
int myint;
BufferedPort<Bottle> myfirst;
Network yarp;

public:
	void SamInit(void)
	{
	myint=0;
	RecognisePort("R1");
	myfirst.open("/Reader_R1");
	myfirst.setReporter(myPortStatus);
	StartModule("/Reader");
	puts("started reader");
	}
	void SamIter(void)
	{
		puts("running reader");
		Bottle *input = myfirst.read(false); // get in the input from the port, if you want it to wait use true, else use false
		if(input!=NULL)						 // check theres data
		{
		puts("got a msg");
		puts(input->toString());
		}
		else{puts("didn't get a msg");}
	}
};

// A very simple data class
class DataForm
{
public:
int x;
int y;
int p;
};


class ExampleThreeSendClass: public SamClass
{
private:
int myint;
BufferedPort<BinPortable<DataForm>> myfirst; // use BinPortable to make a class ready to be used on the network
Network yarp;
public:
	void SamInit(void)
	{
	myint=0;
	RecognisePort("Out");
	myfirst.open("/SClass_Out");
	myfirst.setReporter(myPortStatus);
	StartModule("/SClass");
	}
	void SamIter(void)
	{
		myint++;
		BinPortable<DataForm> &MyData = myfirst.prepare(); // prepare data/port
		MyData.content().x=myint;						
		MyData.content().y=myint+5;
		MyData.content().p=myint+10;
		myfirst.write();								   // add stuff and write
	}
};


// a Interupt port, when data hits this port it'll do whatever is onread, be carefull, fast firing interupts can cause big problems as in normal code

class DataPort : public BufferedPort<BinPortable<DataForm>>  
{
     virtual void onRead(BinPortable<DataForm>& b) 
	 {
		 printf("X %i Y %i P %i \n",b.content().x,b.content().y,b.content().p);
     }
};
class ExampleFourReciveClassInterupt: public SamClass
{
private:
int myint;
DataPort myfirst;
Network yarp;
public:
	void SamInit(void)
	{
	myint=0;
	RecognisePort("In");
	myfirst.open("/RClass_In");
	myfirst.setReporter(myPortStatus);
	myfirst.useCallback();				// this tells it to use the onread method
	StartModule("/RClass");
	}
	
};