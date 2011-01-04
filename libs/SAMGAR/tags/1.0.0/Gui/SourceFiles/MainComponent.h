#include <juce_amalgamated.h>
#include <ModuleGui.h>
#include <iostream>
#include <list>
#include <fstream>
#include <string>

#include <yarp/sig/all.h>
#include <yarp/os/all.h>
//#include <ace/OS.h>


//#include <windows.h> 

using namespace std;
using namespace yarp::os;
using namespace yarp::sig;// image stuff


#ifndef __JUCER_HEADER_MAINCOMPONENT_MAINCOMPONENT_D0F6CD31__
#define __JUCER_HEADER_MAINCOMPONENT_MAINCOMPONENT_D0F6CD31__

struct ModuleStruct
{
string name;
string catagory;
string subcatagory;
};

struct connections
{
String Daddyfirstport;
String firstport;
String Daddysecoundport;
String secoundport;
String Lossy;
String Network;
bool IsConnected;
Path MyPath;
};

 class MigrationPortClass : public BufferedPort<Bottle> 
{
public:
	int Ivebeenused;
	string Myvalue;

	MigrationPortClass()
	{
	//Network::connect(Network::getNameServerName(),"/KeyToLocalServer","tcp");
    useCallback();
	}
	  virtual void onRead(Bottle& b) 
	 {
		 Ivebeenused = 1;
		int length = b.size();
		ofstream myfile;
		myfile.open ("Personality.txt");
		for(int cc=0;cc<length;cc++)
		{
		 b.get(cc).asString(); // get the first line from the bottle
		 myfile <<  b.get(cc).asString().c_str() << "\n";
		}
		myfile.close();
     }
};
/*
  class ConnectionPortClass : public BufferedPort<Bottle> 
{
public:
	int Ivebeenused;
	string Myvalue;

	ConnectionPortClass()
	{
		open("/PortForModules");
    useCallback();
	}
	  virtual void onRead(Bottle& b) 
	 {
	//	 Ivebeenused = 1;
	//	int length = b.size();
	//	ofstream myfile;
	//	myfile.open ("Personality.txt");
	//	for(int cc=0;cc<length;cc++)
	//	{
	//	 b.get(cc).asString(); // get the first line from the bottle
	//	 myfile <<  b.get(cc).asString().c_str() << "\n";
	//	}
	//	myfile.close();
     }
};
*/




 


//string NameOfServer;// ="/local";

void  ChangeServer(int change);
//void UpdateMigrationProto(void);
void GetCurrentServerName(void);
string GetServerName(void);
bool ListDeleatingFunction(ModuleStruct x,ModuleStruct y);
bool DelFromList (ModuleStruct& value);

/*! Sorts the varibles in the known module list!*/
bool ListSortingFunction(ModuleStruct x,ModuleStruct y);







 class MainComponent  : public Component,
					   public Timer,
                       public ButtonListener
					   
{
public:

	//  
     MigrationPortClass MigrationPort;
	 BufferedPort<Bottle> ThePortForModules;
	// ConnectionPortClass ThePortForModules;
    MainComponent ();
    ~MainComponent();
    void paint (Graphics& g);
    void resized();
    void buttonClicked (Button* buttonThatWasClicked);
	void AddToLog(const String,int);
	void AddModule(String name);
	void Updatemodules(void);
	void RefreashConnections(void);
	void SortPorts(void);
	void AddConnection(String parent1,String child1,String parent2, String child2,String Lossyornot,String Network);
	void AddPort(String Parent,String name);
	void RemoveModuleOrPort(String name);
    juce_UseDebuggingNewOperator
	void timerCallback();
	void UpdateMigrationProto(void);
	void RegisterMigrationPort (void);
	bool Migrate (string nameofwhere);
	void DelConnection(String parent1,String child1,String parent2, String child2,String Lossyornot,String Network);
	void ConnectionAutoUpdate(void);
	void CheckConnectionRight(void);
	void GetModuleCommands(void);
	void SendOffModuleList(void);

private:


	
	list<ModuleStruct> ListOfKnownModules;
	list<ModuleStruct>::iterator modulestructIT;
	int MySizeX,MySizeY;
	int MyCurrentSizeX,MyCurrentSizeY;
	float PropSizeChangeX,PropSizeChangeY;

	juce::Time MyTime;
	File myFileforLog;
	File myFileforModReport;
	File myFileforMod;
	File myFileforCon;
	list<connections> AllConnections;
	string NameOfPlatform;
	int sizeofball;
//	MyTimer = new Timer();
	ComboBox* ModParent1;
	ComboBox* ModParent2;
	ComboBox* ModChild1;
	ComboBox* ModChild2;
	ComboBox* LossBox;
	ComboBox* NetworkBox;
	ComboBox* Connect;
    Label* helloWorldLabel;
	TextButton* SaveMod;
    TextButton* LoadMod;
	TextButton* SaveCon;
    TextButton* LoadCon;

	TextButton* ConnectionStuff;
    TextButton* quitButton;
	TextButton* DebugButton1;
	TextButton* DebugButton2;
	TextButton* DebugButton3;
	TextButton* DebugButton4;
	TextButton* StopButton;
	TextButton* StartButton;
	TextButton* OpenLogButton;
	TextButton* MigrateButton;
	TextButton* ClearLog;
	TextButton* SaveLog;
	TextButton* TestButton;
	TextEditor* MytextEditor;
	TextEditor* MytextEditor2;
    Path internalPath1;
	Path internalPath2;
	Path internalPath3;
	Path internalPath4;
	Path internalPath5;
	Path TempPath;
	Path mynewpath;
	DrawablePath TempPath2;
	int WhatShownDebug[4];
	list<TextButton*>::iterator itTextButton;
	list<TextButton*>::iterator itTextButton2;
	list<TextButton*>::iterator itTextButton3;
	list<Path>::iterator itPath;
	list<String*>::iterator itString;
	TextButton* TempModuleButton;
	TextButton* TempModuleButton2;
	TextButton* TempModuleButton3;
	TextButton* TempPortButton;
	TextButton* Blackboard;
	TextButton* IonInterface;
	TextButton* RefreshConnect;
	TextButton* BigButton;
	list<TextButton*> SeenModules;
	list<TextButton*> SeenPorts;
	list<Path> SeenLines;
	list<string> MigrationPlatformsAvail;
    MainComponent (const MainComponent&);
    const MainComponent& operator= (const MainComponent&);
};


#endif   // __JUCER_HEADER_MAINCOMPONENT_MAINCOMPONENT_D0F6CD31__



