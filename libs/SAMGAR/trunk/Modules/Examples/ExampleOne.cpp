#include "ExampleOne.h"

ExampleOneWrite::ExampleOneWrite(std::string name): SamClass(name)
{
}
 
void ExampleOneWrite::SamInit(void)
{
   myint=0;
   newPort(&myfirst2, "W2"); // add new port
   newPort(&myfirst, "W1");  
   StartModule();	
   puts("started writer");
}
   
void ExampleOneWrite::SamIter(void)
{
   Bottle& B = myfirst.prepare();	  // prepare the bottle/port
   B.clear();
   B.addInt(myint++);
   myfirst.write();			  // add stuff then send
   Bottle& C = myfirst2.prepare();
   C.clear();
   C.addInt(myint+5);
   myfirst2.write();	
   puts("running writer");
}

////////////////////////////////////////////////////////////////////////////////

ExampleTwoRead::ExampleTwoRead(std::string name): SamClass(name)
{
}
 
void ExampleTwoRead::SamInit(void)
{
   myint=0;
   newPort(&myfirst, "R1");
   StartModule();
   puts("started reader");
}

void ExampleTwoRead::SamIter(void)
{
   puts("running reader");
   Bottle *input = myfirst.read(false); // get in the input from the port, if
   // you want it to wait use true, else use false 
   if(input!=NULL)			 // check theres data
   {
      puts("got a msg");
      puts(input->toString());
   }
   else
      puts("didn't get a msg");
}

////////////////////////////////////////////////////////////////////////////////

ExampleThreeSendClass::ExampleThreeSendClass(std::string name): SamClass(name)
{
}

void ExampleThreeSendClass::SamInit(void)
{
   //name = "/SClass";
   myint=0;
   newPort(&myfirst, "Out");
   StartModule();
}

void ExampleThreeSendClass::SamIter(void)
{
   myint++;
   BinPortable<DataForm> &MyData = myfirst.prepare(); // prepare data/port
   MyData.content().x=myint;						
   MyData.content().y=myint+5;
   MyData.content().p=myint+10;
   myfirst.write();				   // add stuff and write
}

////////////////////////////////////////////////////////////////////////////////

// a Interupt port, when data hits this port it'll do whatever is onread, be
// carefull, fast firing interupts can cause big problems as in normal code 

void DataPort ::onRead(BinPortable<DataForm>& b) 
{
   printf("X %i Y %i P %i \n",b.content().x,b.content().y,b.content().p);
}

ExampleFourReciveClassInterupt::ExampleFourReciveClassInterupt(std::string name): SamClass(name)
{
}

void ExampleFourReciveClassInterupt::SamInit(void)
{
   myint=0;
   //name="/RClass";
   newPort(&myfirst, "In");
   myfirst.useCallback();				// this tells it to use the onread method
   StartModule();
}	

void ExampleFourReciveClassInterupt::SamIter(void)
{
}
