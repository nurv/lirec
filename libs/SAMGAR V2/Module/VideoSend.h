#include "SamClass.h"


 
class Vsend: public SamClass 
{
   public:

   BufferedPort<ImageOf<PixelBgr>> VideoSend;
   Network yarp;						
  
   void innit(void)
   {
	RecognisePort("Out");
	StartModule("/VideoS");
	VideoSend.open("/VideoS_Out");
	VideoSend.setReporter(myPortStatus);
   }

};
