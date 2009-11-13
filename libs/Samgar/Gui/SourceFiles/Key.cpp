


//#include "includes.h"
#include "juce_amalgamated.h"
#include "MainComponent.h"
#include "LogComponent.h"
//#include <stdio.h>
#include <time.h>

using namespace std;
using namespace yarp::os;

static MainComponent* MyComponent;




 // this port is connected to the server
 class DataPort : public BufferedPort<Bottle> 
{
public:

int hasitworked;
    

	DataPort(MainComponent* MyComp)
	{
		// cant init mycomponent here but need it to be static so its done elsewhere
		MyComponent=MyComp; //transfer over pointer to the main gui
		useCallback();      // set the port to use onRead
		MyComponent->AddToLog("Connecting to server \n ",1); // add to the log
		open("/KeyToLocalServer"); // open the port
	//	setStrict(true);
		hasitworked=Network::connect(Network::getNameServerName(),"/KeyToLocalServer","tcp"); // connect them up
	}


	  virtual void onRead(Bottle& b) 
	 {
		static String myvar;
		static String TempVar;
	//	MyComponent->stopTimer();
		myvar=b.toString().c_str();
	//	MyComponent->AddToLog("Misc "+myvar+" \n",1);
		// this adds new ports to the gui

		if(myvar.indexOfWholeWord(String("[add]"))==0) // if a port is addid
		{
			if(myvar.indexOfWholeWord(String("/Main"))==7) // if main is the next word signifying its a module and not just a port
			{
			TempVar = myvar.substring(13);
			TempVar=TempVar.dropLastCharacters(1);
			MyComponent->AddModule(TempVar);
			Network::connect(string(TempVar).c_str(),"/PortForModules");
			Network::connect("/PortForModules",string(TempVar).c_str());
			yarp::os::Time::delay(1);

			}
			else if(myvar.indexOfWholeWord(String("/Port"))==7)
			{
			TempVar = myvar.substring(13);
			String parent=TempVar.dropLastCharacters(TempVar.length()-TempVar.indexOf(String("_")));
			String Child =TempVar.replaceSection(0,TempVar.indexOf(String("_"))+1,String(" "));
			Child = Child.dropLastCharacters(1);
			MyComponent->AddPort(parent,Child);
			}
		}
	//	MyComponent->startTimer(5000);
     }

};


class HelloWorldWindow  : public DocumentWindow
{
public:
    //==============================================================================
    HelloWorldWindow(): DocumentWindow (T("SAMGAR network profile"),Colours::lightgrey,DocumentWindow::allButtons,true)
    {
		//yarp::os::Network::init();
	//	Network yarp;
        MainComponent* const contentComponent = new MainComponent();
        setContentComponent (contentComponent, true, true);
        centreWithSize (getWidth(), getHeight());
        setVisible (true);
		MyComponent= new MainComponent();// used for port, needs to be setup outside class
		static DataPort PortForLocalServer(contentComponent); // make sure the port is static and pass it the pointer to the gui
    }

    ~HelloWorldWindow()   { /* (the content component will be deleted automatically, so no need to do it here) */    }

    void closeButtonPressed()   
	{
		yarp::os::Network::fini(); 
	//Network::fini();
		JUCEApplication::quit();
	
	} // little cross top right
};

class JUCEHelloWorldApplication : public JUCEApplication
{
    Network yarp;
    HelloWorldWindow* helloWorldWindow;

public:
 
    void initialise (const String& commandLine)
    {
        helloWorldWindow = new HelloWorldWindow();
    }

    void shutdown()
    {
        if (helloWorldWindow != 0)
            delete helloWorldWindow;
    }

    //==============================================================================
    const String getApplicationName()
    {
        return T("SAMGAR Network Profile");
    }

    const String getApplicationVersion()
    {
        return T("1.0");
    }

    bool moreThanOneInstanceAllowed()
    {
        return true;
    }

    void anotherInstanceStarted (const String& commandLine)
    {
    }
};



START_JUCE_APPLICATION (JUCEHelloWorldApplication)
