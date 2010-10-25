#include "SamClass.h"
#include <yarp/sig/Image.h>

class VideoSend: public SamClass 
{
 public:
   VideoSend(std::string name);
   virtual void SamInit();
   virtual void SamIter();
   void setImagePtr(const yarp::sig::ImageOf<yarp::sig::PixelBgr>& image);
 private:
   yarp::os::BufferedPort<yarp::sig::ImageOf<yarp::sig::PixelBgr> > videoOutput;	
};
