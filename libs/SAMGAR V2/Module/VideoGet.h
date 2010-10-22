
/*
A samgar module with its own innit method, makes the main code less messy

*/
#include "SamClass.h"



class Vget: public SamClass 
{
   public:

   BufferedPort<ImageOf<PixelBgr>> VideoRecive;					
   Network yarp;				 // make sure the network is ready
  
   void innit(void)
   {
	RecognisePort("In");
	StartModule("/VideoG");
	VideoRecive.open("/VideoG_In");
	VideoRecive.setReporter(myPortStatus);
   }
};

