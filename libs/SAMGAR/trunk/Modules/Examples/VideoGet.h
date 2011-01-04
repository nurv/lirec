
/*
A samgar module with its own innit method, makes the main code less messy

*/
#include "SamClass.h"
#include <yarp/sig/Image.h>

class VideoGet: public SamClass 
{
 public:			
   VideoGet(std::string name);
   virtual void SamInit();
   virtual void SamIter();
   yarp::sig::ImageOf<yarp::sig::PixelBgr>* getImagePtr();
 private:
   yarp::os::BufferedPort<yarp::sig::ImageOf<yarp::sig::PixelBgr> > videoRecive;
};

