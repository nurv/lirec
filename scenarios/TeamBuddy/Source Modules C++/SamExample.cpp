#include "SamClass.h"

//#include "ExampleOne.h"

int locations[7]= {0, 4, 0, 1, 0 , 3, 6 };
int reached = 1;
class ExampleOneWrite: public SamClass 
{
private:

BufferedPort<Bottle> myfirst; // create buffered ports for bottles like this
Network yarp;						   // make sure the network is ready

public:
	int myint;
	std::string str;
	int counter;
	void SamInit(void)
	{
	myint=0;
	//RecognisePort("TTS");				// name the port to be shown in the gui
	
	RecognisePort("W1");				// name the port to be shown in the gui

	StartModule("/Writer");	
	myfirst.setStrict(true);

	myfirst.open("/Writer_W1");		// open the port
	myfirst.setReporter(myPortStatus);	// set reporter


	// we weve addid all ports and are happy then send the module name, this must be done last!!

	
	puts("started writer");
    counter=0;
	
	
	}
	void SamIter(void)
	{
		if(myint!=0)
		{
			Bottle& B = myfirst.prepare();		// prepare the bottle/port
			B.clear();
			B.addString("greet");
			B.addInt(myint);
			myfirst.writeStrict();
			puts("sent first value");
			//myint=1;
			//counter++;
		}

		/*
		Bottle *input = NULL;
		//puts("running reader");
	//	while (myfirst.getPendingReads() > 0)
		input = myfirst.read(false);

			
		if(input!=NULL)						 // check theres data
		{
			reached  = input->get(0).asInt();
            std::cout<< "reached " << reached <<std::endl;

			yarp::os::Time::delay(10);

			Bottle& B = myfirst.prepare();		// prepare the bottle/port
			B.clear();
			B.addInt(locations[counter]);
			//B.addInt(myint);
		
			myfirst.writeStrict();
			//myfirst.write();					// add stuff then send
			//reached = 0;

			std::cout<< "going to location " << locations[counter] <<std::endl;
			counter++;
			if(counter==7)
				counter=0;
		
		}
		*/
	
	
	//puts("sending command");
	}
};




int main(void) 
{
	ExampleOneWrite myfirstmodule;
	myfirstmodule.SamInit();
	yarp::os::Time::delay(5);

	
    int cmd=0;
	while(1)
	{
		std::cout<< "enter a value" <<std::endl;
		std::cin>> myfirstmodule.myint;
		
		
		myfirstmodule.SamIter();
		
		yarp::os::Time::delay(1);
	}

  return 0;
}


	