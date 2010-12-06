#include <stdio.h>
#include "SamClass.h"
#include <yarp/sig/Image.h>

using namespace yarp::os;


/******************************************************/
class samDataOut: public SamClass 
{
private:
	int iCount;
	BufferedPort<Bottle> b;
public:
	samDataOut(std::string name);
	virtual void SamInit();
	virtual void SamIter();
	virtual void PamInit(std::string namep);
	virtual void PamIter(int n, double *data);
};
/******************************************************/
class samDataIn: public SamClass
{
private:
	int iCount;
	BufferedPort<Bottle> b;
public:
	samDataIn(std::string name);
	virtual void SamInit();
	virtual void SamIter();
	virtual void PamInit(std::string namep);
	virtual void PamIter(int *n, double *data);
};
/******************************************************/
class samVideoOut: public SamClass 
{
private:
	int iCount;
	yarp::os::BufferedPort<yarp::sig::ImageOf<yarp::sig::PixelBgr> > videoOutput;	
public:
   samVideoOut(std::string name);
   virtual void SamInit();
   virtual void SamIter();
   virtual void PamInit(std::string namep);
   void setImagePtr(const yarp::sig::ImageOf<yarp::sig::PixelBgr>& image);
};
/******************************************************/
class samVideoIn: public SamClass 
{
private:
	int iCount;
	yarp::os::BufferedPort<yarp::sig::ImageOf<yarp::sig::PixelBgr> > videoRecive;
public:			
   samVideoIn(std::string name);
   virtual void SamInit();
   virtual void SamIter();
   virtual void PamInit(std::string namep);
   yarp::sig::ImageOf<yarp::sig::PixelBgr>* getImagePtr();
};
