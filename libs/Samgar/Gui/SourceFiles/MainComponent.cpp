

#include "MainComponent.h"

#define definedsizeofball 50
#define Pi  3.14159265
#define SetupMode 0
#define RunMode 1

static int MyMode;
static	Port MyConnectionTesterPort;

string NameOfServer = "/local"; 
using namespace std;

#define local 1
#define global  2

bool IgnoreTimer =false;

list<string> MigSites;
string NameofMigrate;

// static BufferedPort<Bottle> ThePortForModules;
string RememberName;
 Port SendAdminCommands;


/*! main window constructor !*/
MainComponent::MainComponent (): 
helloWorldLabel (0), quitButton (0),  MytextEditor(0), MytextEditor2(0),	ModParent1(0),	    ModChild1(0),  ModChild2(0),
ModParent2(0),		 LossBox(0),	  Connect(0),	   NetworkBox(0),		ConnectionStuff(0), ClearLog(0),   RefreshConnect(0),
StopButton(0),		 MigrateButton(0),StartButton(0),  OpenLogButton(0),	SaveLog(0),			SaveMod(0),    LoadCon(0),
LoadMod(0),			 SaveCon(0),	  DebugButton1(0), DebugButton2(0),     DebugButton3(0),    DebugButton4(0)
{

//	Network yarp;

	WhatShownDebug[0]=1;
	WhatShownDebug[1]=1;
	WhatShownDebug[2]=1;
	WhatShownDebug[3]=1;


	DebugButton1 = new TextButton (String::empty);
	addAndMakeVisible (DebugButton1);
	DebugButton1->setBounds(740,20,100,30);
	DebugButton1->setButtonText (T("debug priority 1 on"));
    DebugButton1->addButtonListener (this);
	DebugButton1->setConnectedEdges(Button::ConnectedOnBottom);	

	DebugButton2 = new TextButton (String::empty);
	addAndMakeVisible (DebugButton2);
	DebugButton2->setBounds(740,50,100,30);
	DebugButton2->setButtonText (T("debug priority 2 on"));
    DebugButton2->addButtonListener (this);
	DebugButton2->setConnectedEdges(Button::ConnectedOnTop | Button::ConnectedOnBottom);	

	DebugButton3 = new TextButton (String::empty);
	addAndMakeVisible (DebugButton3);
	DebugButton3->setBounds(740,80,100,30);
	DebugButton3->setButtonText (T("debug priority 3 on"));
    DebugButton3->addButtonListener (this);
	DebugButton3->setConnectedEdges(Button::ConnectedOnTop | Button::ConnectedOnBottom);	

	DebugButton4 = new TextButton (String::empty);
	addAndMakeVisible (DebugButton4);
	DebugButton4->setBounds(740,110,100,30);
	DebugButton4->setButtonText (T("debug priority 4 on"));
    DebugButton4->addButtonListener (this);
	DebugButton4->setConnectedEdges(Button::ConnectedOnTop);	
    setSize (1200, 600);

	SaveLog = new TextButton (String::empty);
	addAndMakeVisible (SaveLog);
	SaveLog->setBounds(740,150,100,30);
	SaveLog->setButtonText (T("Save log"));
    SaveLog->addButtonListener (this);

	ClearLog = new TextButton (String::empty);
	addAndMakeVisible (ClearLog);
	ClearLog->setBounds(740,190,100,30);
	ClearLog->setButtonText (T("Clear Log"));
    ClearLog->addButtonListener (this);

	RefreshConnect = new TextButton (String::empty);
	addAndMakeVisible (RefreshConnect);
	RefreshConnect->setBounds(740,390,100,30); // 230
	RefreshConnect->setButtonText (T("RefreshConnect"));
    RefreshConnect->addButtonListener (this);

	StartButton = new TextButton (String::empty);
	addAndMakeVisible (StartButton);
	StartButton->setBounds(740,270,100,30);
	StartButton->setButtonText (T("Start Modules"));
    StartButton->addButtonListener (this);

	StopButton = new TextButton (String::empty);
	addAndMakeVisible (StopButton);
	StopButton->setBounds(740,310,100,30);
	StopButton->setButtonText (T("Stop Modules"));
    StopButton->addButtonListener (this);

	MigrateButton = new TextButton (String::empty);
	addAndMakeVisible (MigrateButton);
	MigrateButton->setBounds(740,350,100,30);
	MigrateButton->setButtonText (T("Migrate - Test"));
    MigrateButton->addButtonListener (this);

	OpenLogButton = new TextButton (String::empty);
	addAndMakeVisible (OpenLogButton);
	OpenLogButton->setBounds(740,230,100,30); // 390
	OpenLogButton->setButtonText (T("Open log"));
    OpenLogButton->addButtonListener (this);

	SaveMod = new TextButton (String::empty);
	addAndMakeVisible (SaveMod);
	SaveMod->setBounds(740,430,100,30);
	SaveMod->setButtonText (T("Save Modules"));
    SaveMod->addButtonListener (this);

	LoadMod = new TextButton (String::empty);
	addAndMakeVisible (LoadMod);
	LoadMod->setBounds(740,470,100,30);
	LoadMod->setButtonText (T("Load Modules"));
    LoadMod->addButtonListener (this);

	SaveCon = new TextButton (String::empty);
	addAndMakeVisible (SaveCon);
	SaveCon->setBounds(740,510,100,30);
	SaveCon->setButtonText (T("Save Connections"));
    SaveCon->addButtonListener (this);

	LoadCon = new TextButton (String::empty);
	addAndMakeVisible (LoadCon);
	LoadCon->setBounds(740,550,100,30);
	LoadCon->setButtonText (T("Load Connections"));
    LoadCon->addButtonListener (this);



	/********************************************************************/

	startTimer (1000); // was 5000
	helloWorldLabel = new Label (String::empty,T("SAMGAR Network Profile"));
    addAndMakeVisible (helloWorldLabel);
    helloWorldLabel->setFont (Font (10.0000f, Font::bold));
	helloWorldLabel->setBounds(910,100,10,20);
    helloWorldLabel->setJustificationType (Justification::centred);
    helloWorldLabel->setEditable (false, false, false);
    helloWorldLabel->setColour (Label::textColourId, Colours::black);
    helloWorldLabel->setColour (TextEditor::textColourId, Colours::black);
    helloWorldLabel->setColour (TextEditor::backgroundColourId, Colour (0x0));

	addAndMakeVisible(MytextEditor = new TextEditor());
    MytextEditor->setBounds (890, 20, 290, 300);
    MytextEditor->setText (T("Debug Log Init \n"));
	MytextEditor->setMultiLine(true,true);
	MytextEditor->setReadOnly(true);
	MytextEditor->setCaretVisible(false);

	addAndMakeVisible(MytextEditor2 = new TextEditor());
    MytextEditor2->setBounds (890, 300+25+15, 290, 100);
    MytextEditor2->setText (T("Feedback Log Init \n"));
	MytextEditor2->setMultiLine(true,true);
	MytextEditor2->setReadOnly(true);
	MytextEditor2->setCaretVisible(false);


	addAndMakeVisible(ModParent1 = new ComboBox("N/A"));
    ModParent1->setBounds (890,480, 140, 22);
	ModParent1->setText(T("Parent 1"));

	addAndMakeVisible(ModParent2 = new ComboBox("N/A"));
    ModParent2->setBounds (1040,480, 140, 22);
	ModParent2->setText(T("Parent 2"));

	addAndMakeVisible(ModChild1 = new ComboBox("N/A"));
    ModChild1->setBounds (890,505, 140, 22);
	ModChild1->setText(T("Child 1"));

	addAndMakeVisible(ModChild2 = new ComboBox("N/A"));
    ModChild2->setBounds (1040,505, 140, 22);
	ModChild2->setText(T("Child 2"));

	addAndMakeVisible(LossBox = new ComboBox("N/A"));
    LossBox->setBounds (890,530, 140, 22);
	LossBox->setText(T("Lossy/Unlossy"));
	LossBox->addItem("Lossy",1);
	LossBox->addItem("Unlossy",2);

	addAndMakeVisible(Connect = new ComboBox("N/A"));
    Connect->setBounds (890,555, 140, 22);
	Connect->setText(T("Connect/Disconnect"));
	Connect->addItem("Connect",1);
	Connect->addItem("Disconnect",2);

	addAndMakeVisible(NetworkBox = new ComboBox("N/A"));
    NetworkBox->setBounds (1040,530, 140, 22);
	NetworkBox->setText(T("Network Type"));

	NetworkBox->addItem("Process",1);
	NetworkBox->addItem("Platform",2);
	NetworkBox->addItem("Local Network",3);

	ConnectionStuff = new TextButton (String::empty);
	addAndMakeVisible (ConnectionStuff);
	ConnectionStuff->setBounds (1040,555, 140, 22);
	ConnectionStuff->setButtonText (T("Proceed"));
    ConnectionStuff->addButtonListener (this);

    addAndMakeVisible (quitButton = new TextButton (String::empty));
	quitButton->setButtonText (T("Quit"));
    quitButton->addButtonListener (this);

	DebugButton1->setClickingTogglesState(true);
	DebugButton1->setColour(0x1000100,Colours::green);
	DebugButton1->setColour(0x1000101,Colours::red);

	DebugButton2->setClickingTogglesState(true);
	DebugButton2->setColour(0x1000100,Colours::green);
	DebugButton2->setColour(0x1000101,Colours::red);

	DebugButton3->setClickingTogglesState(true);
	DebugButton3->setColour(0x1000100,Colours::green);
	DebugButton3->setColour(0x1000101,Colours::red);

	DebugButton4->setClickingTogglesState(true);
	DebugButton4->setColour(0x1000100,Colours::green);
	DebugButton4->setColour(0x1000101,Colours::red);

	GetCurrentServerName();
	RegisterMigrationPort();
//	yarp::os::Time::delay(3);

	//IgnoreTimer=true;
	static bool opened2 = ThePortForModules.open("/PortForModules");
	//if(opened2==true){AddToLog("opened local port :\n",1);}
	//else			{
	//				AddToLog("could not open local port :\n",1);
	//				}
	
	//IgnoreTimer=false;
	//ThePortForModules.useCallback();
	//ThePortForModules.setStrict(true);

	//SendAdminCommands.setAdminMode(true);

//	ChangeServer(global);
//	SendAdminCommands.open("/thingy");

	ChangeServer(local);
	//	RegisterMigrationPort();

//	Network::connect("/thingy","/global");
//	Network::connect("/global","/thingy"); 

MySizeX=getWidth();
MySizeY=getHeight();
PropSizeChangeX=0;
PropSizeChangeY=0;


}

/*! main window de-structor !*/
MainComponent::~MainComponent()
{
	// save the mod log;
	stopTimer();
	MyTime.getCurrentTime();
	String datePlus = MyTime.toString(true,true,true,true);
	datePlus = datePlus + ".SamModLog";
	myFileforModReport = datePlus;
	myFileforModReport.create ();
	myFileforModReport.appendText (MytextEditor2->getText ()); // get from the mod log editor


	Network::fini();

	AllConnections.clear();

	MigrationPort.close();
//	ThePortForModules.close();



    deleteAndZero (helloWorldLabel);
    deleteAndZero (quitButton);
	deleteAndZero (MytextEditor);
    deleteAndZero (MytextEditor2);
	deleteAndZero (ModParent1);
    deleteAndZero (ModChild1);
	deleteAndZero (ModChild2);
    deleteAndZero (ModParent2);
	deleteAndZero (LossBox);
    deleteAndZero (Connect);
	deleteAndZero (NetworkBox);
    deleteAndZero (ConnectionStuff);
	deleteAndZero (ClearLog);
	deleteAndZero (RefreshConnect);
    deleteAndZero (StopButton);
    deleteAndZero (MigrateButton);
	deleteAndZero (StartButton);
    deleteAndZero (OpenLogButton);
	deleteAndZero (SaveLog);
    deleteAndZero (SaveMod);
	deleteAndZero (LoadCon);
    deleteAndZero (LoadMod);
	deleteAndZero (SaveCon);
    deleteAndZero (DebugButton1);
	deleteAndZero (DebugButton2);
    deleteAndZero (DebugButton3);
	deleteAndZero (DebugButton4);

	//myFileforLog;
	//File myFileforModReport;
	//File myFileforMod;
	//File myFileforCon;
}

/*! changes the current namespace , allows to com with local and global server !*/
void ChangeServer(int change){	Network::setNameServerName((change==local)?NameOfServer.c_str():"/global");}

/*! Get the current namespace and save it so we can always switch back!*/
void GetCurrentServerName(void){NameOfServer=Network::getNameServerName();}

/*! Register the main migration port!*/
void MainComponent::RegisterMigrationPort (void)
{
	IgnoreTimer=true;
	ChangeServer(global);
	NameofMigrate = NameOfServer + "_Migration";
	static bool opened = MigrationPort.open(NameofMigrate.c_str());

	ChangeServer(local);
	IgnoreTimer=false;

	if(opened==true){AddToLog("Addid myself to global server :\n",1);}
	else			{
					AddToLog("could not Add myself to global server :\n",1);
					AddToLog("This program will take a long while to shut down :\n",1);
					MigrationPort.close();
					}
	AddToLog(NameofMigrate.c_str(),1);
	AddToLog("\n",1);
}

/*! Migrate function!*/
bool MainComponent::Migrate (string nameofwhere)
{
	AddToLog(" Attempting to Migrate  \n",1);
	IgnoreTimer=true;
	ChangeServer(global);

	if(MigrationPort.isClosed()==true){AddToLog("Migration port is closed \n",1);}

	Bottle& MyBottle =MigrationPort.prepare();
	MyBottle.clear();

	nameofwhere = "/" + nameofwhere + "_Migration";

	AddToLog(nameofwhere.c_str(),1);AddToLog(" to ",1);AddToLog(MigrationPort.getName().c_str(),1);AddToLog("\n",1);

bool true1= MigrationPort.addOutput(nameofwhere.c_str());  


if(true1==false){AddToLog("Could not connect to migrate, operation aborted \n",1);}


if(true1==false){ChangeServer(local);IgnoreTimer=false; return false;}

  string line;
  ifstream myfile ("Personality.txt");
  
  if (myfile.is_open())
  {
	//  AddToLog(" opened file  \n",1);
    while (! myfile.eof() )
    {
     getline (myfile,line);
	  MyBottle.addString(line.c_str());
    }
    myfile.close();
//	AddToLog("   \n",1);
 }
  else
  {
	  AddToLog(" could not open file \n",1);
	  ChangeServer(local);
	  IgnoreTimer=false;
	  return false;
  }

 MigrationPort.write();
 AddToLog(" Migration Sucsessfull \n",1);

 Network::disconnect(MigrationPort.getName(),nameofwhere.c_str());
 Network::disconnect(nameofwhere.c_str(),MigrationPort.getName());


 ChangeServer(local);
IgnoreTimer=false;
return true ;// if its sucsessfull
}





/*! update a list of available platforms we can migrate to !*/
void MainComponent::UpdateMigrationProto(void)
{
IgnoreTimer=true;
static FILE *inpipe;
char inbuf[200];
String hello;
int lineno = 0;
/*
Bottle in,out;

ChangeServer(global);
bool isitcon1 = Network::isConnected("/thingy","/global");
bool isitcon2 =	Network::isConnected("/global","/thingy"); 

if(isitcon1==true && isitcon2==true)
{
AddToLog("Connected to global server \n",1);
	out.clear();
	//out.addString("bot");
	out.addString("list");
	bool ppp = SendAdminCommands.write(out,in);

	if(ppp == true){AddToLog("Has got reply data \n",1);}

	if(in.size()==0){AddToLog("The list is empty \n",1);}
	if(in.size()==1){AddToLog("The list is 1 \n",1);}
	if(in.size()==2){AddToLog("The list is 2 \n",1);}
	if(in.size()==3){AddToLog("The list is 3 \n",1);}
	if(in.size()==4){AddToLog("The list is 4 \n",1);}
	if(in.size()>4){AddToLog("The list is  greater than five\n",1);}
//	AddToLog(String(in.toString()),1);
	//	for(int jjj=0;jjj<in.size();jjj++)
//	{
//	AddToLog(in.get(jjj).asString().c_str(),1);
//	}
}
else
{
AddToLog("Cannot connect to global server \n",1);
	Network::connect("/thingy","/global");
	Network::connect("/global","/thingy"); 
}
ChangeServer(local);
*/
/*	ChangeServer(global);
	Network::connect("/thingy","/global");
	Network::connect("/global","/thingy");
	CV1.clear();
	CV1.addString("clean");
	bool ISThereData = SendAdminCommands.write(CV1);
	CV1.clear();
//	CV1.addString("bot");
	CV1.addString("list");
	ISThereData = SendAdminCommands.write(CV1);

	AddToLog("Checking for return data \n",1);
	while(SendAdminCommands.read(CV2)==false){;}
	AddToLog("Got return data \n",1);
//	yarp::os::Time::delay(2);
	Network::disconnect("/thingy","/global");
	Network::disconnect("/global","/thingy");
	ChangeServer(local);
*/








/*
if(ISThereData==true){AddToLog("Got Some Data back \n",1);}
else				 {AddToLog("Got No Data back \n",1);}


//SendAdminCommands.close();
for(int hh =0;hh<CV2.size();hh++)
{
	String MyLikkleString = CV2.get(hh).asString() + "\n"; 
	AddToLog(CV2.toString().c_str(),1);
	AddToLog("\n",1);
}
*/
//Bottle& vv = SendAdminCommands.prepare();

// if its windows

//yarp::os::Network::main(1,"yarp clean");

//char hello[]="yarp clean";
//hello ="yarp clean";


/// ok first thing here is to set the server port to admin
// void yarp::os::Port::setAdminMode  	(  	bool   	 adminMode = true  	 )   	
// we can then send the command botlist but lets try list first
//  MigrationPort.open

//MigrationPort.port.setAdminMode(true);
//Bottle& vv = MigrationPort.prepare();
//vv.addString("list");
//MigrationPort.write();

ChangeServer(global);
#ifdef	Rectangle
	inpipe = _popen("yarp clean","r");
	inpipe = _popen("yarp name list","r");
#else
	inpipe =  popen("yarp clean","r");
    inpipe =  popen("yarp name list", "r");
#endif

	MigrationPlatformsAvail.clear();
    if (!inpipe){AddToLog("Cannot access needid function to find other platforms\n",1);   }
    else 
	{
		AddToLog(" List of possible migration platforms :\n",1);

         while (fgets(inbuf, sizeof(inbuf), inpipe)) 
		 {

		    String mystring = inbuf;
			mystring=mystring +"\n";
			if(mystring.containsChar('/')==true && mystring.contains(String("Migration"))==true)
			 {
				 int start = mystring.indexOf(String("/"));
				 int fin   = mystring.indexOf(start,String(" "));

				 mystring=mystring.substring(start,fin);
				 MigrationPlatformsAvail.push_front(string(mystring));
				 mystring = "--->"+mystring+   "\n";
                 AddToLog(mystring,1);

				 /// need to add it to the list here as well
			 }
		}
    }
	
 ChangeServer(local);
/**/
 IgnoreTimer=false;
}




/*! Checks that each connection is working, ignores failed modules !*/
void MainComponent::ConnectionAutoUpdate(void)
{

		list<connections>::iterator it2;
		list<TextButton*>::iterator itTextButton55;
		TextButton* TempModuleButton55;


	for ( it2=AllConnections.begin() ; it2 != AllConnections.end(); it2++ )
		{
			connections mytempconnect;
			mytempconnect = *it2;
			string tempstring10(mytempconnect.firstport);
            string tempstring20(mytempconnect.secoundport);
			tempstring10=tempstring10.substr(1,tempstring10.length()-1);
			tempstring20=tempstring20.substr(1,tempstring20.length()-1);

			string firststring = "/Port_"   + string(mytempconnect.Daddyfirstport) + "_"  + tempstring10.c_str();
			string secoundstring = "/Port_" + string(mytempconnect.Daddysecoundport) + "_" + tempstring20.c_str();

			// only connect with UDP or TCP for time being
			bool true1,true2;
			bool skip;
			skip = false;


			for ( itTextButton55=SeenModules.begin() ; itTextButton55 != SeenModules.end(); itTextButton55++ )
			{
				TempModuleButton55 = *itTextButton55;
				if(TempModuleButton55->getButtonText()==mytempconnect.Daddyfirstport || TempModuleButton55->getButtonText()==mytempconnect.Daddysecoundport)
				{
					if(Colours::red==TempModuleButton55->findColour(0x1000102,false)){skip=true;}
				}
			}


			if(skip==false)
			{

				true1=Network::isConnected(firststring.c_str(),secoundstring.c_str(),true);
				true2=Network::isConnected(secoundstring.c_str(),firststring.c_str(),true);

				if(true1!=true || true2!=true)
				{
					if(mytempconnect.Lossy==String("Lossy"))
					{
						true1 = Network::connect(firststring.c_str(),secoundstring.c_str(),"udp",true);
						true2 = Network::connect(secoundstring.c_str(),firststring.c_str(),"udp",true);
					}
					else
					{
						true1 = Network::connect(firststring.c_str(),secoundstring.c_str(),"tcp",true);
						true2 = Network::connect(secoundstring.c_str(),firststring.c_str(),"tcp",true);
					}
					if(true1==true && true2==true){mytempconnect.IsConnected=true; }
					else						  {mytempconnect.IsConnected=false; }
				}
			}
			else
			{
				mytempconnect.IsConnected=false;
			}
			*it2=mytempconnect;
			RefreashConnections();
		}

}
/*! checks the main port for each module can be connected to so its up and running!*/
void MainComponent::CheckConnectionRight()
{
		list<TextButton*>::iterator itTextButton66;
		TextButton* TempModuleButton66;
		Contact MyCont;

	for ( itTextButton66=SeenModules.begin() ; itTextButton66 != SeenModules.end(); itTextButton66++ )
		{
			TempModuleButton66 = *itTextButton66;
			//if (TempModuleButton66->
			if(Colours::red!=TempModuleButton66->findColour(0x1000102,false))
			{
				string mystring(TempModuleButton66->getButtonText());
				mystring = "/Main_" + mystring;
				MyCont=Network::queryName(mystring.c_str());

				if(MyCont.isValid()==false){break;}
				
				if(ThePortForModules.isClosed()==true){ThePortForModules.open("/PortForModules");AddToLog("Attempting to recover local port \n",2);}

				//ought to be check for connection and if not connected then reconnect;
				if(Network::isConnected(mystring.c_str(),"/PortForModules",true)==false || Network::isConnected("/PortForModules",mystring.c_str(),true)==false)
				{
					if(Network::connect(mystring.c_str(),"/PortForModules","tcp",true)==false || Network::connect("/PortForModules",mystring.c_str(),"tcp",true)==false)
					{
						TempModuleButton66->setColour(0x1000102 ,Colours::red);
						AddToLog("The module Main_"+TempModuleButton66->getButtonText()+ " is in error \n",2);
						RememberName=TempModuleButton66->getButtonText();
						ListOfKnownModules.remove_if(DelFromList);
						SendOffModuleList(); // if a modules removed send off the new list
					}
				}
			}
		}

}

//bool single_digit (const int& value) { return (value<10); }
bool DelFromList (ModuleStruct& value)
{
	return	!value.name.compare(RememberName);
}


// the idea here is that everytime a new module is found or lost it will send off the new list to 
// all the other modules
void MainComponent::SendOffModuleList()
{

	Bottle& RR = ThePortForModules.prepare();
	RR.clear();
	RR.addInt(105);
for ( modulestructIT=ListOfKnownModules.begin() ; modulestructIT != ListOfKnownModules.end(); modulestructIT++ )
		{
		RR.addString(modulestructIT->name.c_str());
		RR.addString(modulestructIT->catagory.c_str());
		RR.addString(modulestructIT->subcatagory.c_str());
		}
ThePortForModules.write();
}
/*!
Time callback checks modules and connections , also checks the keytomodules port which recives data from ports ie stop commands etc
bounces a few of them back as well to enable one module to communicate with all of them
!*/

void MainComponent::timerCallback()
{
	if(IgnoreTimer==false)
	{
		ChangeServer(local);
	//	Network yarp;
		
			CheckConnectionRight(); // looks like the problem is in here
		
			ConnectionAutoUpdate(); 
		
			GetModuleCommands();  // this one seems fine
		
	}
}


void MainComponent::GetModuleCommands(void)
{
	static list<string>::iterator it;
	Bottle *b;
		while(ThePortForModules.getPendingReads()>0)
		{

		    b = ThePortForModules.read(true);
			if(b!=NULL && b->isNull()==false) // if theres data on the port
			{
				 Bottle& cc = ThePortForModules.prepare();
				 cc.clear();
				/// if its a report on how well its done then add to a log else
				if(b->get(0).asInt()==20)
				{
					String MyNewString = b->get(1).asString().c_str();
					double ModScore = b->get(2).asInt();
					MyNewString = MyNewString + " achived score " + String(ModScore) + " \n"; 
					MytextEditor2->insertTextAtCursor(MyNewString);
				}
				// add to log
				else if(b->get(0).asInt()==30)
				{
					String MyNewString = b->get(1).asString().c_str();
					String LogScore    = b->get(2).asString().c_str();
					MyNewString = MyNewString + " : " + LogScore + " \n";
					AddToLog(MyNewString,b->get(3).asInt());
				}
				else if(b->get(0).asInt()==40) 
				{


					UpdateMigrationProto();
		//			AddToLog("recived command to get all data from modules \n",1);
					cc.addInt(50); // code for platforms
					for ( it=MigrationPlatformsAvail.begin() ; it != MigrationPlatformsAvail.end(); it++ )
					{
						string temppy = *it;
						cc.addString(temppy.c_str());
					}
					ThePortForModules.write();

				}
///// reply back with new data once a new module has been created
				// just send on 
				// when new data is got from the module giving it new data send off the module list
				else if(b->get(0).asInt()==10)
				{
				ModuleStruct TempStruct;

				TempStruct.name        =b->get(1).asString().c_str();
				TempStruct.catagory    =b->get(2).asString().c_str();
				TempStruct.subcatagory =b->get(3).asString().c_str();

				ListOfKnownModules.push_front(TempStruct);
		
			//	AddToLog("GotCatagoryData \n",1);
				SendOffModuleList();
				}

				// send the global command on to all modules
				else if(b->get(0).asInt()>=0 && b->get(0).asInt()<=2)
				{
			//		AddToLog("recived global module command \n",1);
					cc.addInt(b->get(0).asInt());
					ThePortForModules.write();
				}
				// send the personal command to all modules
				else if(b->get(0).asInt()>=3 && b->get(0).asInt()<=5)
				{
			//	AddToLog("recived Personal module command \n",1);
					cc.addInt   (b->get(0).asInt());
					cc.addString(b->get(1).asString());
					ThePortForModules.write();
				}

			}
		}


		if(MigrationPort.Ivebeenused==1)
		{
			AddToLog("Someone has tryed to acess my migration port \n",1);
			MigrationPort.Ivebeenused=0;
		}
}


bool ListDeleatingFunction(ModuleStruct x,ModuleStruct y)
{
	if(x.name==y.name){return true;}
	return false;
}

/*! Sorts the varibles in the known module list!*/
bool ListSortingFunction(ModuleStruct x,ModuleStruct y)
{
	if(x.name>y.name){return true;}
	return false;
}






/*! 
Adds a module to the lists and displayes it on screen
!*/
void MainComponent::AddModule(String name)
{
static int TotalModules=5;
static int menucount = 1;
bool IsItOnList=false;
	// need to put a bit in here to add to the port list and give it its id

for ( itTextButton2=SeenModules.begin() ; itTextButton2 != SeenModules.end(); itTextButton2++ )
		{
		TempModuleButton = *itTextButton2;
		if(name==TempModuleButton->getButtonText())
			{
			TempModuleButton->setColour(0x1000102 ,Colours::black);
			IsItOnList=true;
			}
		}


	if(IsItOnList==false)
	{
	AddToLog("New Module called " + name +" has been found \n",1);
	TotalModules++;
	SeenModules.push_front (new TextButton (String::empty));
	SeenModules.front()->setButtonText (name);
	SeenModules.front()->setRadioGroupId(TotalModules);
	MainComponent::Updatemodules();
	ModParent1->addItem(name,menucount);
	ModParent2->addItem(name,menucount);
	menucount++;
	}
	else
	{
	AddToLog("Module called " + name +" has been reconnected \n",2);
	
	}
}


/*! 
Adds a port to the list and screen also sets its group id to its parent module

!*/
void MainComponent::AddPort(String Parent,String name)
{
bool IsItOnList=false;
static int menucount2=1;

	for ( itTextButton2=SeenModules.begin() ; itTextButton2 != SeenModules.end(); itTextButton2++ ) // go through pos parents
		{
		TempModuleButton = *itTextButton2;
		if(TempModuleButton->getButtonText()==Parent) // if it is parent
			{
		for ( itTextButton=SeenPorts.begin() ; itTextButton != SeenPorts.end(); itTextButton++ ) // check that child isn't on list
				{
				TempModuleButton2 = *itTextButton;
				// it its name is on the list and the list name has the same parent then its already been addid
				if(TempModuleButton2->getButtonText()==name && TempModuleButton2->getRadioGroupId() == TempModuleButton->getRadioGroupId())
					{
					IsItOnList=true;
					}
				}

			if(IsItOnList==false)
			{
			SeenPorts.push_front (new TextButton (String::empty));
			SeenPorts.front()->setButtonText (name);
			AddToLog("Found port " + name + " child of " + Parent + " \n",1);
			SeenPorts.front()->setRadioGroupId(TempModuleButton->getRadioGroupId());
			ModChild1->addItem(name,menucount2);
			ModChild2->addItem(name,menucount2);
			menucount2++;
			}
			else
			{
			AddToLog("Found port " + name + " child of " + Parent + " has been reconnected \n",1);
			}

			}
		}
MainComponent::Updatemodules();

}
/*!

This function is used to display the modules and ports in a nice circuler pattern
needs to have some work done so it resizes a little better

!*/
void MainComponent::Updatemodules(void)
{
double count =0, count2 =0;
int size;
size =SeenModules.size();
double radias;
double some;
int x ;
int y ;



sizeofball=definedsizeofball;
if(size>25){sizeofball=(25*definedsizeofball)/size;}

for ( itTextButton=SeenModules.begin() ; itTextButton != SeenModules.end(); itTextButton++ )
	{
	if(size>6){radias = ((sizeofball/2)*size)/Pi;}
	else	  {radias = sizeofball;}

	some=6.28318531/size;   
	x = radias*(cos((some)*count));   
	y = radias*(sin((some)*count));   

	/// this is the module button this works ok
	/// basicly it puts all modules in a circle, when theres many modules it increases the radias 
	/// and if need be decreases the size of the buttons
	TempModuleButton = *itTextButton;
	addAndMakeVisible (TempModuleButton);
	TempModuleButton->setBounds(345+x-(sizeofball/2),297+y-(sizeofball/2),sizeofball,sizeofball);
	TempModuleButton->addButtonListener (this);

	// for the ports.
	for ( itTextButton2=SeenPorts.begin() ; itTextButton2 != SeenPorts.end(); itTextButton2++ )
		{
		TempPortButton = *itTextButton2;
		if(TempPortButton->getRadioGroupId()==TempModuleButton->getRadioGroupId())
			{
			addAndMakeVisible (TempPortButton);
			double Angle = atan2(double(y),double(x));  // get the angle 
			int x2 = (sizeofball*0.075)+((sizeofball*0.85)*(count2+1.1))*(cos(Angle));   
			int y2 = (sizeofball*0.075)+((sizeofball*0.85)*(count2+1.1))*(sin(Angle));  
			x2=x2+TempModuleButton->getX();
			y2=y2+TempModuleButton->getY();
			TempPortButton->setBounds(x2,y2,sizeofball*0.85,sizeofball*0.85);
			TempPortButton->addButtonListener (this);
			count2++;
			}
		else
			{
			count2=0;
			}
		}
	count++;
	}

if(MyMode==RunMode || AllConnections.size()>0)
{
RefreashConnections();
return;
}
}

/*
RefreashConnections
Used to create the network profile by figuring out what modules go where and then drawing lines
from port to port to show how there interconnected, need to change connections from a array of 
strings to a list of objects, so they can be added easerly by other methods for starting creations
*/

void MainComponent::RefreashConnections(void)
{
	static int HaveIDoneThisBefore=0;
    list<connections>::iterator it2;


int mylittlecount=0;
/// puts everything top left to begin with
for ( itTextButton=SeenModules.begin() ; itTextButton != SeenModules.end(); itTextButton++ )
	{
		TempModuleButton = *itTextButton;
		TempModuleButton->setTopLeftPosition(20,20);  
	}
/// puts the modules across and down, if a child is found then it goes right and down, else it just goes down and
/// if it hits anouther module it pushes that one down
for(int ii=0;ii<10;ii++)
{
for ( itTextButton=SeenModules.begin() ; itTextButton != SeenModules.end(); itTextButton++ )
	{
	for ( itTextButton2=SeenModules.begin() ; itTextButton2 != SeenModules.end(); itTextButton2++ )
		{
			TempModuleButton = *itTextButton;
			TempModuleButton2 = *itTextButton2;

		for ( it2=AllConnections.begin() ; it2 != AllConnections.end(); it2++ )
			{
			connections mytempconect = *it2;
			/// if there connected move it down and right
			if(mytempconect.Daddyfirstport == TempModuleButton->getButtonText() && mytempconect.Daddysecoundport==TempModuleButton2->getButtonText())
				{
				TempModuleButton2->setTopLeftPosition(TempModuleButton->getX()+30,TempModuleButton->getY()+60);//woz 60
				}
			} 
			// if not connected just move it down
		for ( itTextButton3=SeenModules.begin() ; itTextButton3 != SeenModules.end(); itTextButton3++ )
			{
			TempModuleButton3 = *itTextButton3;
			if(TempModuleButton3->getY()==TempModuleButton2->getY()&&TempModuleButton2!=TempModuleButton3)
				{
				TempModuleButton3->setTopLeftPosition(TempModuleButton3->getX(),TempModuleButton3->getY()+60);//woz 60
				}
			}
		}
	}
// puts the ports next to the modules
MainComponent::SortPorts(); 
}

/// this bit just puts in the line in from port to port
for ( itTextButton=SeenPorts.begin() ; itTextButton != SeenPorts.end(); itTextButton++ )
	{
	for ( itTextButton2=SeenPorts.begin() ; itTextButton2 != SeenPorts.end(); itTextButton2++ )
		{
			TempModuleButton = *itTextButton;
			TempModuleButton2 = *itTextButton2;
		
		for ( it2=AllConnections.begin() ; it2 != AllConnections.end(); it2++ )
			{
		connections mytempconect = *it2;

			if(mytempconect.firstport == TempModuleButton->getButtonText() && mytempconect.secoundport==TempModuleButton2->getButtonText())
				{
				
			//	if(TempModuleButton->getY()>TempModuleButton2->getY())
			//		{
					mytempconect.MyPath.clear();
					mytempconect.MyPath.startNewSubPath (float(TempModuleButton->getX()+(sizeofball*0.425)),float(TempModuleButton->getY()-2+(sizeofball*0.85)));// top left
					mytempconect.MyPath.lineTo (float(TempModuleButton2->getX()+(sizeofball*0.425)), float(TempModuleButton2->getY()+2));// bottem left
					mytempconect.MyPath.closeSubPath();
					*it2 = mytempconect;
			//		}
			//	else
			//		{
			//		AddToLog("adding path 2 \n",1);
			//		mytempconect.MyPath.clear();
			//		mytempconect.MyPath.startNewSubPath (float(TempModuleButton->getX()+(sizeofball*0.425)),float(TempModuleButton->getY()-2+(sizeofball*0.85)));// top left
			//		mytempconect.MyPath.lineTo (float(TempModuleButton2->getX()+(sizeofball*0.425)), float(TempModuleButton2->getY()+2));// bottem left
			//		mytempconect.MyPath.closeSubPath();
			//		}
				}
			}
		}
	}
// bug in the system that unless the windows forced to redraw everything then not all lines are seen
// changed this by everytime changing the main window size a little.
if(HaveIDoneThisBefore==0)	{centreWithSize (getWidth()+1, getHeight());HaveIDoneThisBefore=1;}
else						{centreWithSize (getWidth()-1, getHeight());HaveIDoneThisBefore=0;}
}
/*!
This just puts the ports in the right order next to the modules used by refreash connections
 !*/
void MainComponent::SortPorts(void)
{
int counter=0;
for ( itTextButton=SeenModules.begin() ; itTextButton != SeenModules.end(); itTextButton++ )
	{
		counter=0;
		TempModuleButton = *itTextButton;
		TempModuleButton->setConnectedEdges(Button::ConnectedOnRight);
	for ( itTextButton2=SeenPorts.begin() ; itTextButton2 != SeenPorts.end(); itTextButton2++ )
		{
		
		TempPortButton   = *itTextButton2;
		TempPortButton->setConnectedEdges(Button::ConnectedOnRight|Button::ConnectedOnLeft);
		if(TempModuleButton->getRadioGroupId()==TempPortButton->getRadioGroupId())
			{
			TempPortButton->setTopLeftPosition(TempModuleButton->getX()+((sizeofball*0.85)*counter)+sizeofball,TempModuleButton->getY());
			counter++;
			}
		}	
	}
}

/*! 
adds data to the log, makes sure that if button is pressed it doesn't print data they dont want
!*/

void MainComponent::AddToLog(const String VV, int priority)
{
if(WhatShownDebug[priority]==1)
	{
	switch (priority)
		{
	case 0: MytextEditor->setColour(TextEditor::textColourId,Colours::black);	break; // normal debug stuff
	case 1: MytextEditor->setColour(TextEditor::textColourId,Colours::blue);	break; // SAMGAR only debug
	case 2: MytextEditor->setColour(TextEditor::textColourId,Colours::darkred);	break; // SAMGAR Crit Level 1
	case 3: MytextEditor->setColour(TextEditor::textColourId,Colours::red);		break; // normal CRIT Level 2
		}
	MytextEditor->insertTextAtCursor(VV);
	}
}
/*!
this bit actully does all the drawing for the whole program for lines and stuff like that
mainly used for drawing the lines
!*/

void MainComponent::paint (Graphics& g)
{
	// main box on left
	g.fillAll (Colour (0xff8f9ef6)); // background color
	g.setColour (Colour (0xffbdc5f7)); // color inside path
    g.fillPath (internalPath1);
	g.setColour (Colour (0xff5f74f1)); // line color
    g.strokePath (internalPath1, PathStrokeType (5.2000f));


//	g.fillAll (Colour (0xff8f9ef6)); // background color
	g.setColour (Colour (0xffbdc5f7)); // color inside path
   g.fillPath (internalPath4);
	g.setColour (Colour (0xff5f74f1)); // line color
    g.strokePath (internalPath4, PathStrokeType (5.2000f));

	// button box on right
	g.setColour (Colour (0xffbdc5f7)); // color inside path
	g.fillPath (internalPath2);
	g.setColour (Colour (0xff5f74f1)); // line color
    g.strokePath (internalPath2, PathStrokeType (5.2000f));

	g.setColour (Colour (0xffbdc5f7)); // color inside path
	g.fillPath (internalPath5);
	g.setColour (Colour (0xff5f74f1)); // line color
    g.strokePath (internalPath5, PathStrokeType (5.2000f));


//Path internalPath5;

	list<connections>::iterator it2;
	connections temmp;
	g.setColour (Colours::grey);

for ( it2=AllConnections.begin() ; it2 != AllConnections.end(); it2++ )
	{
    temmp=*it2;
	if(temmp.IsConnected==true)	{g.setColour (Colours::blue);}
	else						{g.setColour (Colours::grey);}
	g.strokePath (temmp.MyPath, PathStrokeType (5.2000f));
	}

}

/*
this bit is called when the window resizes, should be altered to allow better scaling of all buttons etc when resized
*/

void MainComponent::resized()
{
	//MySizeX=getWidth();
//MySizeY=getHeight();
//MyCurrentSizeX;
//MyCurrentSizeY
//PropSizeChangeX,PropSizeChangeY
	//this->centreWithSize

	PropSizeChangeX=1;//MySizeX/getWidth();
	PropSizeChangeY=1;//MySizeY/getHeight();

//	PropSizeChangeX=getWidth()/MySizeX;
//	PropSizeChangeY=getHeight()/MySizeY;


	// little connection box on right
    internalPath4.clear();
    internalPath4.startNewSubPath (880.0f  *PropSizeChangeX,  470.0f *PropSizeChangeY);
    internalPath4.lineTo		  (1190.0f *PropSizeChangeX , 470.0f *PropSizeChangeY);
	internalPath4.lineTo		  (1190.0f *PropSizeChangeX , 590.0f *PropSizeChangeY );
	internalPath4.lineTo		  (880.0f  *PropSizeChangeX , 590.0f *PropSizeChangeY);
	internalPath4.lineTo		  (880.0f  *PropSizeChangeX , 470.0f *PropSizeChangeY);
	internalPath4.closeSubPath();

	// main box on left	
    internalPath1.clear();
    internalPath1.startNewSubPath (10.0f, 10.0f);
    internalPath1.lineTo (700.0f, 10.0f);
    internalPath1.lineTo (700.0f, 590.0f);
	internalPath1.lineTo (10.0f, 590.0f);
    internalPath1.lineTo (10.0f, 10.0f);
    internalPath1.closeSubPath();

	// this shuold be box for buttons
	internalPath2.clear();
    internalPath2.startNewSubPath (720.0f, 10.0f);// top left
    internalPath2.lineTo (860.0f, 10.0f);// top right
    internalPath2.lineTo (860.0f, 590.0f); // bottem right
	internalPath2.lineTo (720.0f, 590.0f); //
    internalPath2.lineTo (720.0f, 10.0f);
    internalPath2.closeSubPath();


	// to surround the text boxes
	internalPath5.clear();
    internalPath5.startNewSubPath (880.0f,    10.0f);// top left
    internalPath5.lineTo		  (880.0f+310, 10.0f);// top right
    internalPath5.lineTo		  (880.0f+310, 450.0f); // bottem right
	internalPath5.lineTo		  (880.0f,	  450.0f); //
    internalPath5.lineTo		  (880.0f,    10.0f);
    internalPath5.closeSubPath();

}

/*! 
Interupt when any button is clicked
!*/

void MainComponent::buttonClicked (Button* buttonThatWasClicked)
{
String tempnames[10];
  Bottle& cc = ThePortForModules.prepare();
  cc.clear();

  if (buttonThatWasClicked == StopButton)
  {
	cc.addInt(0);
	ThePortForModules.write();
  }

  if (buttonThatWasClicked == StartButton)
  {
	cc.addInt(1);
	ThePortForModules.write();
  }
  
  if (buttonThatWasClicked == MigrateButton)
  {
	if(NameOfServer == "/Red"){Migrate("Blue");}
	if(NameOfServer == "/Blue"){Migrate("Red");}
  }



   if (buttonThatWasClicked == RefreshConnect)
    {
      MainComponent::RefreashConnections();
	  MyMode=RunMode;
    }
    if (buttonThatWasClicked == quitButton)
    {
      JUCEApplication::quit();
    }
if (buttonThatWasClicked == DebugButton1)
    {
		switch(WhatShownDebug[0])
		{
		case 0:
			WhatShownDebug[0]=1;
			DebugButton1->setButtonText (T("debug priority 1 on")); 
		break;
		case 1:
			WhatShownDebug[0]=0;
			DebugButton1->setButtonText (T("debug priority 1 off"));
		break;
		}
    }
if (buttonThatWasClicked == DebugButton2)
    {
		switch(WhatShownDebug[1])
		{
		case 0:
			WhatShownDebug[1]=1;
			DebugButton2->setButtonText (T("debug priority 2 on")); 
		break;
		case 1:
			WhatShownDebug[1]=0;
			DebugButton2->setButtonText (T("debug priority 2 off"));
		break;
		}
    }
if (buttonThatWasClicked == DebugButton3)
    {
		switch(WhatShownDebug[2])
		{
		case 0:
			WhatShownDebug[2]=1;
			DebugButton3->setButtonText (T("debug priority 3 on")); 
		break;
		case 1:
			WhatShownDebug[2]=0;
			DebugButton3->setButtonText (T("debug priority 3 off"));
		break;
		}
    }
if (buttonThatWasClicked == DebugButton4)
    {
		switch(WhatShownDebug[3])
		{
		case 0:
			WhatShownDebug[3]=1;
			DebugButton4->setButtonText (T("debug priority 4 on")); 
		break;
		case 1:
			WhatShownDebug[3]=0;
			DebugButton4->setButtonText (T("debug priority 4 off"));
		break;
		}
    }

if (buttonThatWasClicked == ClearLog)
    {
		MytextEditor->clear();
		MainComponent::AddToLog("Log has been cleared \n", 1);
	}

//	TextButton* SaveMod;
//  TextButton* LoadMod;
//	TextButton* SaveCon;
//  TextButton* LoadCon;

if (buttonThatWasClicked == LoadCon)
    {
		connections mytempconnect;
	FileChooser chooser6 ("Please select log you wish to load...",File::getSpecialLocation (File::userHomeDirectory),"*.SamCon");
	if (chooser6.browseForFileToOpen ())
		{
		myFileforCon = chooser6.getResult ();
		FileInputStream *hello= myFileforCon.createInputStream();

		String Tempy = " hello ";

		while(Tempy.length()>0)
		{
		Tempy=hello->readNextLine();
		mytempconnect.Daddyfirstport = Tempy;
		mytempconnect.firstport=hello->readNextLine();
		mytempconnect.Daddysecoundport=hello->readNextLine();
		mytempconnect.secoundport=hello->readNextLine();
		mytempconnect.Lossy=hello->readNextLine();
		mytempconnect.Network=hello->readNextLine();
		AllConnections.push_front(mytempconnect);
		}
		}
	}

if (buttonThatWasClicked == SaveCon)
    {
		list<connections>::iterator itter;
		connections mytempconnect;

    FileChooser chooser5 ("Please select the save name...",File::getSpecialLocation (File::userHomeDirectory),"*.SamCon");
	if (chooser5.browseForFileToSave (true))
		{
		myFileforCon = chooser5.getResult ();
		if (myFileforCon.existsAsFile ()){myFileforCon.deleteFile ();}

			myFileforCon.create ();

		for ( itter=AllConnections.begin() ; itter != AllConnections.end(); itter++ )
			{
			mytempconnect = *itter;
			myFileforCon.appendText(mytempconnect.Daddyfirstport + "\n");
			myFileforCon.appendText(mytempconnect.firstport + "\n");
			myFileforCon.appendText(mytempconnect.Daddysecoundport + "\n");
			myFileforCon.appendText(mytempconnect.secoundport + "\n");
			myFileforCon.appendText(mytempconnect.Lossy + "\n");
			myFileforCon.appendText(mytempconnect.Network + "\n");
			}
		}
	}

if (buttonThatWasClicked == SaveMod)
    {
    FileChooser chooser3 ("Please select the save name...",File::getSpecialLocation (File::userHomeDirectory),"*.SamMod");
	if (chooser3.browseForFileToSave (true))
		{
		myFileforMod = chooser3.getResult ();
		if (myFileforMod.existsAsFile ()){myFileforLog.deleteFile ();}

			myFileforMod.create ();
		for ( itTextButton=SeenModules.begin() ; itTextButton != SeenModules.end(); itTextButton++ )
			{
			TempModuleButton = *itTextButton;
			String SaveName = TempModuleButton->getButtonText() + "\n";
			myFileforMod.appendText(SaveName);
			}
		}
	}
if (buttonThatWasClicked == LoadMod)
    {
	FileChooser chooser4 ("Please select log you wish to load...",File::getSpecialLocation (File::userHomeDirectory),"*.SamLog");
	if (chooser4.browseForFileToOpen ())
		{
		myFileforMod = chooser4.getResult ();
		FileInputStream *hello= myFileforMod.createInputStream();

		String Tempy = " hello ";

		while(Tempy.length()>0)
		{
			Tempy=hello->readNextLine();
			if(Tempy.length()>0){AddModule(Tempy);}
		}
		}
	}

if (buttonThatWasClicked == SaveLog)
    {
    FileChooser chooser2 ("Please select the save name...",File::getSpecialLocation (File::userHomeDirectory),"*.SamLog");
	if (chooser2.browseForFileToSave (true))
		{
		myFileforLog = chooser2.getResult ();
		if (myFileforLog.existsAsFile ()){myFileforLog.deleteFile ();}

			myFileforLog.create ();
			myFileforLog.appendText (MytextEditor->getText ());
		}
	}
if (buttonThatWasClicked == OpenLogButton)
    {
	FileChooser chooser ("Please select log you wish to load...",File::getSpecialLocation (File::userHomeDirectory),"*.SamLog");
	if (chooser.browseForFileToOpen ())
		{
		myFileforLog = chooser.getResult ();
		MytextEditor->setText (myFileforLog.loadFileAsString());
		}
	}


if (buttonThatWasClicked == ConnectionStuff)
    {
		int abort = 0;
		if( ModParent1->getText() == String("Parent 1"))		 {abort = 1;}
		if( ModParent2->getText() == String("Parent 2"))		 {abort = 1;}
		if( ModChild1->getText()  == String("Child 1"))			 {abort = 1;}
		if( ModChild2->getText()  == String("Child 2"))			 {abort = 1;}
		if( Connect->getText()    == String("Connect/Disconnect"))	 {abort = 1;}
		if( NetworkBox->getText() == String("Network Type"))	 {abort = 1;}

		if(abort==1){AddToLog("not all choices have been selected for connection", 1);}
		else
		{
			if(Connect->getText() == String("Connect"))
			{
			AddConnection(ModParent1->getText(),ModChild1->getText(),ModParent2->getText(),ModChild2->getText(),Connect->getText(),NetworkBox->getText());
			}
			if(Connect->getText() == String("Disconnect"))
			{
			DelConnection(ModParent1->getText(),ModChild1->getText(),ModParent2->getText(),ModChild2->getText(),Connect->getText(),NetworkBox->getText());
			}
		ModParent1->setText("Parent 1");
		ModParent2->setText("Parent 2")	;
		ModChild1->setText("Child 1");
		ModChild2->setText("Child 2");
		Connect->setText("Connect/Disconnect");
		NetworkBox->setText("Network Type");
		}
	}

}


//==============================================================================
/*!
This function gets called by the connect button to add a new connection to the list
!*/
void MainComponent::AddConnection(String parent1,String child1,String parent2, String child2,String Lossyornot,String Network)
{
	static int county =0;
connections temp;
temp.Daddyfirstport=parent1;
temp.firstport=child1;
temp.Daddysecoundport=parent2;
temp.secoundport=child2;
temp.Lossy=Lossyornot;
temp.Network=Network;
AllConnections.push_front(temp);
}

/*!
This function gets called by the connect button to deleate a new connection to the list
!*/
void MainComponent::DelConnection(String parent1,String child1,String parent2, String child2,String Lossyornot,String Network)
{
connections mytempconnect;
list<connections>::iterator it2;
list<connections>::iterator SavedIt;
bool killcon;
int x =0;

	killcon = false;
	for ( it2=AllConnections.begin() ; it2 != AllConnections.end(); it2++ )
	{
	mytempconnect = *it2;
	x=0;

	string tempstring10(mytempconnect.firstport);
	string tempstring20(mytempconnect.secoundport);
	tempstring10=tempstring10.substr(1,tempstring10.length()-1);
	tempstring20=tempstring20.substr(1,tempstring20.length()-1);

	if(mytempconnect.Daddyfirstport == parent1){x++;}
	if(mytempconnect.Daddysecoundport == parent2){x++;}
	if(mytempconnect.firstport == child1){x++;}
	if(mytempconnect.secoundport == child2){x++;}
	if(x>=4)
	{ 
		SavedIt = it2;
		killcon = true;
	//	it2 = AllConnections.erase (it2);   AddToLog("erased connection \n",1); }
	}
	}
	if(killcon == true)
	{
        SavedIt = AllConnections.erase (SavedIt); 

        string tempstring10(mytempconnect.firstport);
		string tempstring20(mytempconnect.secoundport);
		tempstring10=tempstring10.substr(1,tempstring10.length()-1);
		tempstring20=tempstring20.substr(1,tempstring20.length()-1);

		string firststring = "/Port_"   + string(mytempconnect.Daddyfirstport) + "_"  + tempstring10.c_str();
        string secoundstring = "/Port_" + string(mytempconnect.Daddysecoundport) + "_" + tempstring20.c_str();

        Network::disconnect(firststring.c_str(),secoundstring.c_str());
        Network::disconnect(secoundstring.c_str(),firststring.c_str());
        RefreashConnections();

        AddToLog("erased connection \n",1);
	}
}


