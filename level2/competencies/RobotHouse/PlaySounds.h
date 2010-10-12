
//#include "SamClass.h"
#include <iostream>
#include <windows.h> // NAUGHTY !!!! WINDOWS ONLY HEADER!!


      int REST = 0;
      int GbelowC = 196;
      int A = 220;
      int Asharp = 233;
      int B = 247;
      int C = 262;
      int Csharp = 277;
      int D = 294;
      int Dsharp = 311;
      int E = 330;
      int F = 349;
      int Fsharp = 370;
      int G = 392;
      int Gsharp = 415;
      
      double WHOLE = 1600;
      double HALF = WHOLE / 2;
      double QUARTER = HALF / 2;
      double EIGHTH = QUARTER / 2;
      double SIXTEENTH = EIGHTH / 2;
  

class Vsounds: public SamClass
{
private:
BufferedPort<Bottle> myfirst;


Network yarp;
public:

	void SamInit(void)
	{
	RecognisePort("In");
	StartModule("/Sound");
	myfirst.open("/Sound_In"); //myPortStatus
	myfirst.setReporter(myPortStatus);

	}
	
	
	
	
	void SamIter(void)
	{

		Bottle *b = myfirst.read(); // will wait here untill a bottle is recived
		
		for(int x =0;x<b->size();x=x+2)
		{
			Beep(b->get(x).asDouble(),b->get(x+1).asDouble());
		}




	//	puts("playing sounds");
		
		//Beep(B,HALF);		TRAVELING MUSIC
		//Beep(C,QUARTER);
		//Beep(D,EIGHTH);
		//Beep(F,QUARTER);

		//Beep(F,HALF); // sounds like walking almost
		//Beep(G,HALF);

		//Beep(A,QUARTER); // quite a good sucess song
		//Beep(D,EIGHTH);
		//Beep(F,EIGHTH);
		//Beep(C,QUARTER);
		//Beep(E,EIGHTH);
		//Beep(G,EIGHTH);
		//Beep(E,HALF);
		//Beep(D,HALF);
		//yarp::os::Time::delay(2);
	}
};
