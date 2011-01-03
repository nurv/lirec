/** file ExampleOne.h
 *
 * SAMGAR V2 EXAMPLES
 *
 * A couple of class's showing how to use SAMGAR, they dont have to be done in
 * header files at all, but it can be better its easer to have a option of
 * compiling them together or seperately, both of which have there advantages in
 * differnt situatoins 
 *
 * if you want to send imgs there is a class in yarp exactly for that, you can
 * just send a IPL image. 
 *
 * IMPORTANT: update 1, need to name and create module before opening the
 * ports!!, due to persistent connections in latest yarp release 
 *
 */
#include "SamClass.h"
using namespace yarp::os;


// This class just sends out some data 
class ExampleOneWrite: public SamClass 
{
  private:
   int myint;
   BufferedPort<Bottle> myfirst,myfirst2; // create buffered ports for bottles like this
  
  public:
   ExampleOneWrite(std::string name);
   virtual void SamInit();
   virtual void SamIter();

};

////////////////////////////////////////////////////////////////////////////////

class ExampleTwoRead: public SamClass
{
  private:
   int myint;
   BufferedPort<Bottle> myfirst;

  public:
   ExampleTwoRead(std::string name);
   virtual void SamInit();
   virtual void SamIter();

};

////////////////////////////////////////////////////////////////////////////////

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
   BufferedPort< BinPortable<DataForm> > myfirst; // use BinPortable to make
   
  public:
   ExampleThreeSendClass(std::string name);
   virtual void SamInit();
   virtual void SamIter();

};

////////////////////////////////////////////////////////////////////////////////

// a Interupt port, when data hits this port it'll do whatever is onread, be
// carefull, fast firing interupts can cause big problems as in normal code 

class DataPort : public BufferedPort< BinPortable<DataForm> >  
{
   virtual void onRead(BinPortable<DataForm>& b);
};

class ExampleFourReciveClassInterupt: public SamClass
{
  private:
   int myint;
   DataPort myfirst;
  public:
   ExampleFourReciveClassInterupt(std::string name);
   virtual void SamInit();
   virtual void SamIter();

};
