#include "SamModules.h"

/******************************************************/
samDataOut::samDataOut(std::string name): SamClass(name){}
void samDataOut::SamInit() {}
void samDataOut::SamIter() {}
void samDataOut::PamInit(std::string namep)
{
   iCount = 0;
   newPort(&b, namep);
   StartModule();
   printf(" %s starts writing \n", namep.c_str()); 
}
void samDataOut::PamIter(int n, double *data)
{
   Bottle& B = b.prepare();
   B.clear();
   for (int i=0; i<n; i++) B.addDouble(data[i]);
   b.write();
   //printf("\n %d data send out in bottle %d ", n, ++iCount); 
}
/******************************************************/
samDataIn::samDataIn(std::string name): SamClass(name){}
void samDataIn::SamInit() {}
void samDataIn::SamIter() {}
void samDataIn::PamInit(std::string namep)
{
   iCount = 0;
   newPort(&b, namep);
   StartModule();	
   printf(" %s starts reading \n", namep.c_str()); 
}
void samDataIn::PamIter(int *n, double *data)
{
	Bottle *t = b.read(false);
	if ( t == NULL ) 
	{
		*n = 0;
		data[0] = 0;
	}
	else
	{
		*n = t->size();
		for (int i=0; i<*n; i++) data[i] = t->get(i).asDouble();
	}
	//printf("\n %d data received in bottle %d ", *n, ++iCount); 
}
/******************************************************/
samVideoOut::samVideoOut(std::string name): SamClass(name){}
void samVideoOut::SamInit() {}
void samVideoOut::SamIter() {}
void samVideoOut::PamInit(std::string namep)
{
	iCount = 0;
	newPort(&videoOutput, namep);
	StartModule();	
	printf(" %s starts writing \n", namep.c_str()); 
}
void samVideoOut::setImagePtr(const yarp::sig::ImageOf<yarp::sig::PixelBgr>& image)
{
	yarp::sig::ImageOf<yarp::sig::PixelBgr> &yarpImage = this->videoOutput.prepare();
	yarpImage.copy(image);
	this->videoOutput.write();
	//printf("\n 1 frame send out in bottle %d ", ++iCount); 
}
/******************************************************/
samVideoIn::samVideoIn(std::string name): SamClass(name){}
void samVideoIn::SamInit() {}
void samVideoIn::SamIter() {}
void samVideoIn::PamInit(std::string namep)
{
   
   iCount = 0;
   newPort(&videoRecive, namep);
   StartModule();	
   printf(" %s starts reading \n", namep.c_str()); 
}
yarp::sig::ImageOf<yarp::sig::PixelBgr>* samVideoIn::getImagePtr()
{
	//printf("\n 1 frame received in bottle %d ", ++iCount); 
	return videoRecive.read(true);
}
/******************************************************/
