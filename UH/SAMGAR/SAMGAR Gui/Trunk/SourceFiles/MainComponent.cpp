

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

float LineThick = 1.2;

// static BufferedPort<Bottle> ThePortForModules;
string RememberName;
 Port SendAdminCommands;

 list<connections> AllConnectionsCOPY;

double OriginalXsize =1200;
double OriginalYsize =600;

double TimeBetween = 10;
double TimeOverall = 100;

bool turnoff = false;

	 BufferedPort<Bottle> ThePortForModules;
 BufferedPort<Bottle> MigrationPort;

/*! main window constructor !*/
MainComponent::MainComponent (): 
quitButton (0),  MytextEditor(0), MytextEditor2(0),	ModParent1(0),	    ModChild1(0),  ModChild2(0),
ModParent2(0),		 LossBox(0),	  Connect(0),	   NetworkBox(0),		ConnectionStuff(0), ClearLog(0),   RefreshConnect(0),
StopButton(0),		 MigrateButton(0),StartButton(0),  OpenLogButton(0),	SaveLog(0),			SaveMod(0),    LoadCon(0),
LoadMod(0),			 SaveCon(0),	  DebugButton1(0), DebugButton2(0),     DebugButton3(0),    DebugButton4(0),TimeBetweenChecks(0)
{


	TimeBetweenChecks = new Slider ("TimeBetweenChecks");
	addAndMakeVisible(TimeBetweenChecks);
	TimeBetweenChecks->setBounds(740,550,100,30);
	TimeBetweenChecks->setTextBoxStyle (TimeBetweenChecks->TextBoxAbove ,false, 60, 20);
	TimeBetweenChecks->setValue(5.0,true,true);	 
	TimeBetweenChecks->setRange (0.1,10,0.01);


	WhatShownDebug[0]=1;
	WhatShownDebug[1]=1;
	WhatShownDebug[2]=1;
	WhatShownDebug[3]=1;


	DebugButton1 = new TextButton (String::empty);
	addAndMakeVisible (DebugButton1);
	DebugButton1->setBounds(740,20,100,25);
	DebugButton1->setButtonText (T("debug priority 1 on"));
    DebugButton1->addButtonListener (this);
	DebugButton1->setConnectedEdges(Button::ConnectedOnBottom);	

	DebugButton2 = new TextButton (String::empty);
	addAndMakeVisible (DebugButton2);
	DebugButton2->setBounds(740,50,100,25);
	DebugButton2->setButtonText (T("debug priority 2 on"));
    DebugButton2->addButtonListener (this);
	DebugButton2->setConnectedEdges(Button::ConnectedOnTop | Button::ConnectedOnBottom);	

	DebugButton3 = new TextButton (String::empty);
	addAndMakeVisible (DebugButton3);
	DebugButton3->setBounds(740,80,100,25);
	DebugButton3->setButtonText (T("debug priority 3 on"));
    DebugButton3->addButtonListener (this);
	DebugButton3->setConnectedEdges(Button::ConnectedOnTop | Button::ConnectedOnBottom);	

	DebugButton4 = new TextButton (String::empty);
	addAndMakeVisible (DebugButton4);
	DebugButton4->setBounds(740,110,100,25);
	DebugButton4->setButtonText (T("debug priority 4 on"));
    DebugButton4->addButtonListener (this);
	DebugButton4->setConnectedEdges(Button::ConnectedOnTop);	
    

	SaveLog = new TextButton (String::empty);
	addAndMakeVisible (SaveLog);
	SaveLog->setBounds(740,150,100,25);
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
	StopButton->setBounds(740,310-5,100,30);
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

	startTimer (5000); // was 5000
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
//////	ModParent2->addListener(comboBoxChanged);

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
//	RegisterMigrationPort();

	static bool opened2 = ThePortForModules.open("/PortForModules");


	ChangeServer(local);

PropSizeChangeX=0;
PropSizeChangeY=0;
setSize (1200, 600);

compare_Buttons();
}


/*! main window de-structor !*/
MainComponent::~MainComponent()
{

	MigrationPort.close();
	ThePortForModules.close();
	
	AllConnections.clear();
	ListOfKnownModules.clear();
	SeenModules.clear();
	SeenPorts.clear();
	SeenLines.clear();
	MigrationPlatformsAvail.clear();

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
    deleteAndZero (TimeBetweenChecks);

//	 deleteAllChildren();
	 
}

/*! changes the current namespace , allows to com with local and global server !*/
void ChangeServer(int change){	Network::setNameServerName((change==local)?NameOfServer.c_str():"/global");}

/*! Get the current namespace and save it so we can always switch back!*/
void GetCurrentServerName(void){NameOfServer=Network::getNameServerName();}

/*! Register the main migration port!*/
void MainComponent::RegisterMigrationPort (void)
{
//	IgnoreTimer=true;
static bool opened = false;

	ChangeServer(global);

//	Contact CV;
//	Bottle out,in;
//	out.addString("NAME_SERVER query /global");
//	CV.bySocket("mcast","224.2.1.1",10001);

//	Network::write(CV,out,in,false);

//	if(in.size()>0)
//	{
//		String CVB = out.toString().c_str();
//		AddToLog(CVB + " \n",2);
//	}

	NameofMigrate = NameOfServer + "_Migration";
	if(opened==false)
	{
		
	 opened = MigrationPort.open(NameofMigrate.c_str());
	
	}	
	
 ChangeServer(local);

	if(opened==true){AddToLog("Addid myself to global server :\n",1);}
	else			{AddToLog("could not Add myself to global server :\n",1);}


}


/*! Migrate function!*/
bool MainComponent::Migrate (string nameofwhere,Bottle Data)
{
	AddToLog(" Attempting to Migrate  \n",1);
	IgnoreTimer=true;


	ChangeServer(global);

	if(MigrationPort.isClosed()==true){AddToLog("Migration port is closed \n",1);}

	Bottle& MyBottle =MigrationPort.prepare();
	MyBottle.clear();
	MyBottle.copy(Data,0,Data.size());

	//nameofwhere = "/" + nameofwhere + "_Migration";

	AddToLog(nameofwhere.c_str(),1);AddToLog(" to ",1);AddToLog(MigrationPort.getName().c_str(),1);AddToLog("\n",1);

	bool true1= MigrationPort.addOutput(nameofwhere.c_str());  

	if(true1==false){AddToLog("Could not connect to migrate, operation aborted \n",1);ChangeServer(local);IgnoreTimer=false; return false;}
  
	 MigrationPort.write();
	AddToLog(" Migration Sucsessfull \n",1);

	Network::disconnect(MigrationPort.getName(),nameofwhere.c_str());
	Network::disconnect(nameofwhere.c_str(),MigrationPort.getName());


	ChangeServer(local);
	return true ;// if its sucsessfull
}





/*! update a list of available platforms we can migrate to !*/
Bottle MainComponent::UpdateMigrationProto(void)
{
	ChangeServer(global);
	
	Bottle msg, reply,msg2,reply2,Bplatforms;

    msg.addString("bot");
    msg.addString("list");
    AddToLog(T("Requesting list of ports from name server\n"),2);
    Network::write("/global",msg,reply);

	Bplatforms.addInt(50);

  for (int i=1; i<reply.size(); i++) 
  {
	  ConstString port = reply.get(i).asList()->check("name",Value("")).asString();
 
	  if (port!="" && port!="fallback" && port!="/global" && port!=NameofMigrate.c_str()) 
			{
				if(Network::connect(NameofMigrate.c_str(),port))
					{
					AddToLog(port.c_str(),2);
					AddToLog(" \n",2);
					Bplatforms.addString(port);
					}
				else
				{
					Network::unregisterName(port);
				}
			}
  	  }
	  ChangeServer(local);

	  return Bplatforms;
}




/*! Checks that each connection is working, ignores failed modules !*/
void MainComponent::ConnectionAutoUpdate(void)
{

/*
	 so this goes through all connections, if the daddy button is red then skip else reconnect all the ports.
*/

		list<connections>::iterator it2;
		list<TextButton*>::iterator itTextButton55;
		TextButton* TempModuleButton55;

//AddToLog("about to go into all connections \n",2);





	for ( it2=AllConnections.begin() ; it2 != AllConnections.end(); it2++ )
		{
		//	AddToLog("in for loop \n",2);
			connections mytempconnect;
			mytempconnect = *it2;
			string tempstring10 = mytempconnect.firstport;
			string tempstring20 = mytempconnect.secoundport;
			tempstring10=tempstring10.substr(1,tempstring10.length()-1);
			tempstring20=tempstring20.substr(1,tempstring20.length()-1);

			string firststring = "/Port_"   + mytempconnect.Daddyfirstport + "_"  + tempstring10.c_str();
			string secoundstring = "/Port_" + mytempconnect.Daddysecoundport + "_" + tempstring20.c_str();

			// only connect with UDP or TCP for time being
			bool true1,true2;
			bool skip;
			skip = false;

		//	AddToLog("Checking for false connections \n",2);
			for ( itTextButton55=SeenModules.begin() ; itTextButton55 != SeenModules.end(); itTextButton55++ )
			{
				TempModuleButton55 = *itTextButton55;
				if(TempModuleButton55->getButtonText()==mytempconnect.Daddyfirstport || TempModuleButton55->getButtonText()==mytempconnect.Daddysecoundport)
				{
					if(Colours::red==TempModuleButton55->findColour(0x1000102,false)){skip=true;}
				}
			}



			// ok this is where it messes up if its not on the same system. I think the skip isn't working
			// doesn't seem to slow down the system much, skipping works

			if(skip==false)
			{	
		//		AddToLog("not skipping connections \n",2);
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
				else
				{
				mytempconnect.IsConnected=true;
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


/*! LEGACY CODE: checks the main port for each module can be connected to so its up and running!*/
void MainComponent::CheckConnectionRight()
{

	/* so in this i only check the maim module port, if its not running i assume all other ports are dead */
		list<TextButton*>::iterator itTextButton66;
		TextButton* TempModuleButton66;
		Contact MyCont;

	for ( itTextButton66=SeenModules.begin() ; itTextButton66 != SeenModules.end(); itTextButton66++ )
		{
			TempModuleButton66 = *itTextButton66;
			//if (TempModuleButton66->
			if(Colours::red!=TempModuleButton66->findColour(0x1000102,false))
			{
				string mystring = TempModuleButton66->getButtonText();
				mystring = "/Main_" + mystring;
				MyCont=Network::queryName(mystring.c_str());

				if(MyCont.isValid()==false){break;}
				
				if(ThePortForModules.isClosed()==true){ThePortForModules.open("/PortForModules");AddToLog("Attempting to recover local port \n",2);}

				//ought to be check for connection and if not connected then reconnect;
			//	if(ThePortForModules.is
				if(Network::isConnected(mystring.c_str(),"/PortForModules",true)==false || Network::isConnected("/PortForModules",mystring.c_str(),true)==false)
				{
//				Network:
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

		stopTimer();
		CheckConnect();				//checks modules
		ConnectionAutoUpdate();		// checks ports to port
		GetModuleCommands();

		//GetAdminData();
		/* new readded */
		//	GetModuleCommands();
		/* should fix problem with cmion not working */

		if(AllConnections.size()>0){RefreashConnections();}
		startTimer (TimeBetweenChecks->getValue()*1000);


}

void MainComponent::GetAdminData(void)
{
	/*
static list<ModuleStruct> listofknownmodules;
static Bottle ModuleList;

	while(ThePortForModules.getPendingReads()>0)
		{
		    Bottle b = ThePortForModules.read(true);
			if(b!=NULL && b->isNull()==false) // if theres data on the port
			{
				int FirstData = b.get(0).asInt();
				Bottle& cc = ThePortForModules.prepare();
				switch(FirstData)
				{
				case 10:
					ModuleStruct TempMod;
					TempMod.name		= b.get(1).asString();
					TempMod.catagory	= b.get(2).asString();
					TempMod.subcatagory = b.get(3).asString();
					listofknownmodules.push_front();
				break;

				}
			}
		}
*/

}

void MainComponent::CheckConnect(void)
{
string mystring;

	for ( itTextButton2=SeenModules.begin() ; itTextButton2 != SeenModules.end(); itTextButton2++ )
		{
			TempModuleButton = *itTextButton2;
			mystring="/Main_"+TempModuleButton->getButtonText();

			if(Colours::red!=TempModuleButton->findColour(0x1000102,false))
			{
				if(Network::isConnected(mystring.c_str(),"/PortForModules",true)==false) 
					{
							TempModuleButton->setColour(0x1000102 ,Colours::red);
							repaint();
							SendOffModuleList();
					}
				else if(Network::isConnected("/PortForModules",mystring.c_str(),true)==false)
				{
							TempModuleButton->setColour(0x1000102 ,Colours::red);
							repaint();
							SendOffModuleList();
				}
			}
		}

		// balls it, just check if one of the daddys isn't online then disconnect.
}

void MainComponent::GetModuleCommands(void)
{
	static list<string>::iterator it;
	Bottle *b,*b2;

	if(MigrationPort.getPendingReads()>0)
	{
			b2 = ThePortForModules.read(true);
			if(b2!=NULL && b2->isNull()==false) // if theres data on the port
			{
				AddToLog("Recived migration data \n",2);
				 Bottle& cc = ThePortForModules.prepare();
				 cc.clear();
				 cc.addInt(3003);
				 cc.append(*b2);
			}
	}

		while(ThePortForModules.getPendingReads()>0)
		{

			AddToLog("Recived Data \n",2);
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

				// migrate
				else if(b->get(0).asInt()==36)
				{
					Bottle MigrationData;
					MigrationData.copy(*b,2,b->size()-1);
					bool SucMigrate = Migrate (b->get(1).asString().c_str(),MigrationData);
					cc.addInt(1001);
					if(SucMigrate==true){cc.addInt(1);}
					else				{cc.addInt(0);}
					ThePortForModules.write();
				}
				// register migration
				else if(b->get(0).asInt()==4004)
				{
				AddToLog("Registering migration port \n",2);
				RegisterMigrationPort();
				}
				// add to log
				else if(b->get(0).asInt()==30)
				{
					String MyNewString = b->get(1).asString().c_str();
					String LogScore    = b->get(2).asString().c_str();
					MyNewString = MyNewString + " : " + LogScore + " \n";
					AddToLog(MyNewString,b->get(3).asInt());
				}
				// get migration info
				else if(b->get(0).asInt()==40) 
				{


					cc=UpdateMigrationProto();
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


//		if(MigrationPort.Ivebeenused==1)
//		{
//			AddToLog("Someone has tryed to acess my migration port \n",1);
//			MigrationPort.Ivebeenused=0;
//		}
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

	String realname = "/Main_"+name;
	Network::connect(realname,"/PortForModules","tcp",true);
	Network::connect("/PortForModules",realname,"tcp",true);


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
	SeenModules.push_front (new TextButton (name,name));
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
			SeenPorts.push_front (new TextButton (name,name));
			//SeenPorts.front()->setButtonText (name);
			AddToLog("Found port " + name + " child of " + Parent + " \n",1);
			SeenPorts.front()->setRadioGroupId(TempModuleButton->getRadioGroupId());
			//ModChild1->addItem(name,menucount2);
			//ModChild2->addItem(name,menucount2);
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


void MainComponent::compare_Buttons ()
{
list<connections>::iterator it2;
int var[100];

int numberofmyconnections =0;
int placeinlist=0;
bool Alphaon = true;
// first lets assign colors to each button dependent on connections
for ( itTextButton2=SeenModules.begin() ; itTextButton2 != SeenModules.end(); itTextButton2++ ) // secound daddy
		{
		placeinlist++;
		numberofmyconnections =0;
		TempModuleButton2 = *itTextButton2;
		Alphaon = true;
		for ( it2=AllConnections.begin() ; it2 != AllConnections.end(); it2++ )
			{
			connections mytempconect = *it2;
			if(mytempconect.Daddyfirstport == TempModuleButton2->getButtonText() || mytempconect.Daddysecoundport==TempModuleButton2->getButtonText())
				{
					numberofmyconnections ++;
					if(mytempconect.IsConnected==false){Alphaon=false;}
				}
			}
		//                                         red,green,blue
		uint8 green,blue,red;

		
		//;
	//	Colour MyColor(float(0.5),float(0.5),float(0.5),float(0.5));
	//	Colour MyNewColor = MyColor.withRotatedHue(float((1/360)*(80*(numberofmyconnections+placeinlist))));
	//	Colour::Colour  	( const float    hue,	const float  saturation,	const float  brightness,	const float  alpha	) 	


		red   = (placeinlist*70)+(numberofmyconnections*85);
		green = (placeinlist*100)+(numberofmyconnections*45);
		blue  = (placeinlist*50)+(numberofmyconnections*65);
		
		while(green>255){green=green-255;}
		while(blue>255){blue=blue-255;}
		while(red>255){red=red-255;}


		var[placeinlist]=numberofmyconnections;
		TempModuleButton2->setColour(0x1000100,Colour (red, green, blue,float(1)));
		}


// sort them compared to number of outputs
for(int cvb=0;cvb<SeenModules.size()*2;cvb++)
{
placeinlist=0;
itTextButton2=SeenModules.begin();
itTextButton3=SeenModules.begin();
itTextButton3++;
for ( itTextButton=itTextButton3 ; itTextButton != SeenModules.end(); itTextButton++ ) // first daddy
	{
		placeinlist++;
		TempModuleButton  = *itTextButton;
		TempModuleButton2 = *itTextButton2;
		if(var[placeinlist]>var[placeinlist+1])
		{
			*itTextButton2=TempModuleButton;
			*itTextButton=TempModuleButton2;
			int tempyr = var[placeinlist];
			var[placeinlist]=var[placeinlist+1];
			var[placeinlist+1]=tempyr;
		}
		if(placeinlist+1>SeenModules.size())// wrap around for the list
		{
		if(var[placeinlist]>var[0])
		{
			*itTextButton2=TempModuleButton;
			*itTextButton=TempModuleButton2;
			int tempyr = var[placeinlist];
			var[placeinlist]=var[placeinlist+1];
			var[0]=tempyr;
		}

		}


		itTextButton2++;
	}
}

	/// then swap them

for ( itTextButton=SeenModules.begin() ; itTextButton != SeenModules.end(); itTextButton++ ) // first daddy
	{
	for ( itTextButton2=SeenModules.begin() ; itTextButton2 != SeenModules.end(); itTextButton2++ ) // secound daddy
		{
			TempModuleButton = *itTextButton;
			TempModuleButton2 = *itTextButton2;

		for ( it2=AllConnections.begin() ; it2 != AllConnections.end(); it2++ )
			{
				connections mytempconect = *it2;
				if(mytempconect.Daddyfirstport == TempModuleButton2->getButtonText() && mytempconect.Daddysecoundport==TempModuleButton->getButtonText())
				{
					*itTextButton2=TempModuleButton;
					*itTextButton=TempModuleButton2;
				}
			}
	}
}


}




/* New method : does the arrangement of buttons etc. */
void MainComponent::RefreashConnections(void)
{
static int HaveIDoneThisBefore22=0;
    list<connections>::iterator it2;
static int HowManyConp[50];

int mylittlecount=0;

float Yspace		=	(590-20)+(getHeight()-OriginalYsize) ; // how much screen room
float Xspace		=	(700-10)+(getWidth()-OriginalXsize) ;
float Ytab			=	Yspace/SeenModules.size();
float ButtonSizeY	=	(Ytab/100)*60;

compare_Buttons();

for ( itTextButton=SeenModules.begin() ; itTextButton != SeenModules.end(); itTextButton++ )
	{
		TempModuleButton = *itTextButton;
		TempModuleButton->setSize(50,ButtonSizeY);
		TempModuleButton->setTopLeftPosition(20,17+(Ytab*mylittlecount));  
		mylittlecount++;
	}

MainComponent::SortPorts(); // puts the ports next to the main modules

// draw the lines
for(int nnm=0;nnm<50;nnm++)
{
HowManyConp[nnm]=5;
}


int ColorLineCount=0;
int ColorLineShiftDown=0;
ColorLineCount=0;
 for ( itTextButton=SeenPorts.begin() ; itTextButton != SeenPorts.end(); itTextButton++ ) // first daddy
	{
		TempModuleButton = *itTextButton;
		HowManyConp[TempModuleButton->getRadioGroupId()]=0;
	for ( itTextButton2=SeenPorts.begin() ; itTextButton2 != SeenPorts.end(); itTextButton2++ ) // secound daddy
		{
			
			TempModuleButton2 = *itTextButton2;
			

		for ( it2=AllConnections.begin() ; it2 != AllConnections.end(); it2++ )
			{
			connections mytempconect = *it2;
			if(mytempconect.firstport == TempModuleButton->getButtonText() && mytempconect.secoundport==TempModuleButton2->getButtonText())
				{
					ColorLineCount++;
					ColorLineShiftDown++;
					//
					mytempconect.MyColor=TempModuleButton2->findColour(0x1000100,false);
					float Xstart=0;
					float Ystart=0;
					float Xfin=0;
					float Yfin=0;
					float numberofspacesawaytop = 0;
					float numberofspacesawaybot = 0;
					float StartDown   = 0;
					float StartRight  = 0;
					float SecoundDown =0;

					if(TempModuleButton->getY()<TempModuleButton2->getY())
					{
					 Xstart	=TempModuleButton->getX()+(TempModuleButton->getWidth()/2);
					 Ystart	=TempModuleButton->getY()+ TempModuleButton->getHeight();
					 Xfin	=TempModuleButton2->getX()+(TempModuleButton2->getWidth()/2);
					 Yfin	=TempModuleButton2->getY();

					 numberofspacesawaytop = ((TempModuleButton->getX()-(20+TempModuleButton->getWidth()))/TempModuleButton->getWidth())+1;
					 numberofspacesawaybot = ((TempModuleButton2->getX()-(20+TempModuleButton2->getWidth()))/TempModuleButton2->getWidth())+1;

					 StartDown   = Ystart + ((Ytab-ButtonSizeY)/2)-((LineThick*2)*numberofspacesawaytop);//-(LineThick*numberofspacesaway));
					 
					 StartRight  = Xspace - (7*((Ystart/Ytab)));
					 SecoundDown =  Yfin  - ((Ytab-ButtonSizeY)/2)+((LineThick*2)*numberofspacesawaybot);//-(LineThick*numberofspacesaway));//- ((Ytab-ButtonSizeY)/2) - TempModuleButton2->getHeight();
					
					}
					else
					{
					 Xstart	=TempModuleButton2->getX()+(TempModuleButton2->getWidth()/2);
					 Ystart	=TempModuleButton2->getY()+ TempModuleButton2->getHeight();
					 Xfin	=TempModuleButton->getX()+(TempModuleButton->getWidth()/2);
					 Yfin	=TempModuleButton->getY();

					 numberofspacesawaytop = ((TempModuleButton2->getX()-(20+TempModuleButton2->getWidth()))/TempModuleButton2->getWidth())+1;
					 numberofspacesawaybot = ((TempModuleButton->getX()-(20+TempModuleButton->getWidth()))/TempModuleButton->getWidth())+1;

					 StartDown   = Ystart + ((Ytab-ButtonSizeY)/2)-((LineThick*2)*numberofspacesawaytop);//-(LineThick*numberofspacesaway));
					 StartRight  = Xspace - (7*(Ystart/Ytab));
					 SecoundDown =  Yfin  - ((Ytab-ButtonSizeY)/2)+((LineThick*2)*numberofspacesawaybot);//-(LineThick*numberofspacesaway));//- ((Ytab-ButtonSizeY)/2) - TempModuleButton2->getHeight();
					}		
					
			

					mytempconect.MyPath.clear();
					mytempconect.MyPath.startNewSubPath (Xstart,Ystart);//startpoint
					// go a little down
					mytempconect.MyPath.lineTo			(Xstart,StartDown);
					// go right
					mytempconect.MyPath.lineTo			(StartRight,StartDown);
					// go down
					mytempconect.MyPath.lineTo			(StartRight,SecoundDown);
					// go left
					mytempconect.MyPath.lineTo			(Xfin,SecoundDown);
					//  go down to finnish
					mytempconect.MyPath.lineTo			(Xfin,Yfin);
					// go up 
					mytempconect.MyPath.lineTo			(Xfin,SecoundDown);
					// go right
					mytempconect.MyPath.lineTo			(StartRight,SecoundDown);
					// go up
					mytempconect.MyPath.lineTo			(StartRight,StartDown);
					// go left
					mytempconect.MyPath.lineTo			(Xstart,StartDown);

					mytempconect.MyPath.lineTo			(Xstart,Ystart);

					mytempconect.MyPath.closeSubPath();

				//	if
					*it2 = mytempconect;
				}
			} 
	}
}
repaint();

//if(HaveIDoneThisBefore22==0)	{centreWithSize (getWidth()+1, getHeight());HaveIDoneThisBefore22=1;}
//else							{centreWithSize (getWidth()-1, getHeight());HaveIDoneThisBefore22=0;}
}
/*!
This just puts the ports in the right order next to the modules used by refreash connections
 !*/
void MainComponent::SortPorts(void)
{

// adds children to the modules on the gui
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
			counter++;
			TempPortButton->setSize(TempModuleButton->getWidth(),TempModuleButton->getHeight());
			TempPortButton->setColour(0x1000100,TempModuleButton->findColour(0x1000100,false));
			TempPortButton->setTopLeftPosition(TempModuleButton->getX()+(counter*TempPortButton->getWidth()),TempModuleButton->getY());
			}
		}	
	}
/*
for ( itTextButton=SeenPorts.begin() ; itTextButton != SeenPorts.end(); itTextButton++ )
	{
	for ( itTextButton2=SeenPorts.begin() ; itTextButton2 != SeenPorts.end(); itTextButton2++ )
		{
			TempModuleButton = *itTextButton;
			TempPortButton   = *itTextButton2;

		//if(TempPortButton->getRadioGroupId()!=TempModuleButton->getRadioGroupId())
		//{
		if(TempPortButton->getY()>TempModuleButton->getY())
			{
			 *itTextButton = TempPortButton;
			 *itTextButton2 =  TempModuleButton;
			}
	//	}
		}
	}
*/
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

	float AddonX = getWidth()-OriginalXsize;
	g.setColour (Colours::black);
	g.drawSingleLineText (T("NC Timer Control"),730+AddonX+8+4,550-20-5+30+35-50-20);


//Path internalPath5;

	list<connections>::iterator it2;
	connections temmp;
	g.setColour (Colours::grey);

for ( it2=AllConnections.begin() ; it2 != AllConnections.end(); it2++ )
	{
    temmp=*it2;
	
	if(temmp.IsConnected==true)	{g.setColour (temmp.MyColor);}
	else						{g.setColour (Colours::grey);}
	g.strokePath (temmp.MyPath, PathStrokeType (LineThick));
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

	float AddonX = getWidth()-OriginalXsize;
	float AddonY = getHeight()-OriginalYsize;

	char mychars[30];

	TimeBetweenChecks->setBounds(730+AddonX+8,550-55-20-5+30+30,120,30);
//	TimeForChecks->setBounds(730+AddonX+8,550-55-20-5+30+35,120,30);


	if(AllConnections.size()>0){RefreashConnections();}

	DebugButton1->setBounds(740+AddonX,20 -5,100,25);
	DebugButton2->setBounds(740+AddonX,50 -10,100,25);
	DebugButton3->setBounds(740+AddonX,80 -15,100,25);
	DebugButton4->setBounds(740+AddonX,110-20,100,25);


	SaveLog->setBounds			(740+AddonX,150-5-20-5,100,25);
	ClearLog->setBounds			(740+AddonX,190-10-20-5,100,25);
	OpenLogButton->setBounds	(740+AddonX,230-15-20-5,100,25); // 390
	StartButton->setBounds		(740+AddonX,270-20-20-5,100,25);
	StopButton->setBounds		(740+AddonX,310-25-20-5,100,25);
	MigrateButton->setBounds	(740+AddonX,350-30-20-5,100,25);
	RefreshConnect->setBounds	(740+AddonX,390-35-20-5,100,25); // 230
	SaveMod->setBounds			(740+AddonX,430-40-20-5,100,25);
	LoadMod->setBounds			(740+AddonX,470-45-20-5,100,25);
	SaveCon->setBounds			(740+AddonX,510-50-20-5,100,25);
	LoadCon->setBounds			(740+AddonX,550-55-20-5,100,25);

    MytextEditor->setBounds   (890+AddonX, 20, 290, 300+AddonY);
    MytextEditor2->setBounds  (890+AddonX, 340+AddonY, 290, 100);
    ModParent1->setBounds     (890+AddonX, 480+AddonY, 140, 22);
    ModParent2->setBounds    (1040+AddonX, 480+AddonY, 140, 22);
    ModChild1->setBounds      (890+AddonX, 505+AddonY, 140, 22);
    ModChild2->setBounds     (1040+AddonX, 505+AddonY, 140, 22);


    LossBox->setBounds (890+AddonX,530+AddonY, 140, 22);
    Connect->setBounds (890+AddonX,555+AddonY, 140, 22);
    NetworkBox->setBounds (1040+AddonX,530+AddonY, 140, 22);
	ConnectionStuff->setBounds (1040+AddonX,555+AddonY, 140, 22);

	// box for connections
    internalPath4.clear();
    internalPath4.startNewSubPath (880.0f  +AddonX,  470.0f +AddonY );
    internalPath4.lineTo		  (1190.0f +AddonX , 470.0f +AddonY);
	internalPath4.lineTo		  (1190.0f  +AddonX, 590.0f +AddonY);
	internalPath4.lineTo		  (880.0f  +AddonX, 590.0f +AddonY);
	internalPath4.lineTo		  (880.0f  +AddonX, 470.0f +AddonY);
	internalPath4.closeSubPath();


	// main box on left	
    internalPath1.clear();
    internalPath1.startNewSubPath (10.0f, 10.0f);
    internalPath1.lineTo (700.0f+AddonX,  10.0f);
    internalPath1.lineTo (700.0f+AddonX,  590.0f +AddonY);
	internalPath1.lineTo (10.0f,		  590.0f +AddonY);
    internalPath1.lineTo (10.0f,		  10.0f);
    internalPath1.closeSubPath();

	// this shuold be box for buttons
	internalPath2.clear();
    internalPath2.startNewSubPath (720.0f+AddonX, 10.0f);// top left
    internalPath2.lineTo (860.0f+AddonX, 10.0f);// top right
    internalPath2.lineTo (860.0f+AddonX, 590.0f+AddonY); // bottem right
	internalPath2.lineTo (720.0f+AddonX, 590.0f+AddonY); //
    internalPath2.lineTo (720.0f+AddonX, 10.0f);
    internalPath2.closeSubPath();


	// to surround the text boxes
	internalPath5.clear();
    internalPath5.startNewSubPath (880.0f+AddonX,     10.0f);// top left
    internalPath5.lineTo		  (1190.0f+AddonX,    10.0f);// top right
    internalPath5.lineTo		  (1190.0f+AddonX,    450.0f+AddonY); // bottem right
	internalPath5.lineTo		  (880.0f+AddonX,	  450.0f+AddonY); //
    internalPath5.lineTo		  (880.0f+AddonX,     10.0f);
    internalPath5.closeSubPath();

}




///// putting in some stuff so when the parent box is clicked the child box changes
/*

*/
void MainComponent::comboBoxChanged(ComboBox* comboBoxThatHasChanged)
{
 if(comboBoxThatHasChanged == ModChild1)
  {
	MainComponent::AddToLog("child has been selected \n", 1);

 }


}

/*! 
Interupt when any button is clicked
!*/
void MainComponent::buttonClicked (Button* buttonThatWasClicked)
{
	static int presentchoice	=0;
	static int oldpresentchoice =0;
String tempnames[10];
  Bottle& cc = ThePortForModules.prepare();
  cc.clear();

	// mehod to allow direct connections from the gui
///// doesn't work at the moment but i'll mess around with it later, so you can choose ports by clicking directly
  for ( itTextButton2=SeenPorts.begin() ; itTextButton2 != SeenPorts.end(); itTextButton2++ )
		{
			TempModuleButton = *itTextButton2;
			if (buttonThatWasClicked == TempModuleButton)
			{
				if(presentchoice==0&&oldpresentchoice!=presentchoice)
				{
					ModChild1->clear();
					ModChild1->addItem(TempPortButton->getButtonText(),1);
					oldpresentchoice=presentchoice;
					presentchoice++;
				}
				TempModuleButton->setToggleState(true,false);	  	
			}
		}

  




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
	//  RegisterMigrationPort();
	//  UpdateMigrationProto();
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
//		int trysbeforebreak=0;
		connections mytempconnect;
	FileChooser chooser6 ("Please select log you wish to load...",File::getSpecialLocation (File::userHomeDirectory),"*.SamCon");
	if (chooser6.browseForFileToOpen ())
		{
		myFileforCon = chooser6.getResult ();
		FileInputStream *hello= myFileforCon.createInputStream();

		String Tempy = " hello ";

		while(Tempy.length()>0)
			{
				std::string hello22;
			Tempy=hello->readNextLine();
			//
			String Dad1 = Tempy;
			String Port1 = hello->readNextLine();
			String Dad2 = hello->readNextLine();
			String Port2 = hello->readNextLine();
			String Loss = hello->readNextLine();
			String Net = hello->readNextLine();
				if(Dad1.length()>0 && Port1.length()>0 && Dad2.length()>0 && Port2.length()>0)// && Dad1.length()>0 && Dad1.length()>0 && )
				{
				AddConnection(Dad1,Port1,Dad2,Port2,Loss,Net);
				}
			}
		}
	//chooser6.~FileChooser();
	RefreashConnections();
	RefreashConnections();
	repaint();
	
	}

if (buttonThatWasClicked == SaveCon)
    {
		String nameofFile;
		list<connections>::iterator itter;
		connections mytempconnect;

    FileChooser chooser5 ("Please select the save name...",File::getSpecialLocation (File::userHomeDirectory),"*.SamCon");
	if (chooser5.browseForFileToSave (false))
		{
		myFileforCon = chooser5.getResult ();
		nameofFile = myFileforCon.getFileName();
		
		if (myFileforCon.existsAsFile ()){myFileforCon.deleteFile ();}

			myFileforCon.create ();
			
			for ( itter=AllConnections.begin() ; itter != AllConnections.end(); itter++ )
			{
			mytempconnect = *itter;
			if(mytempconnect.Daddyfirstport.length()==0)
				{
				 itter = AllConnections.erase (itter);
				}
			}

		
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
//chooser5.~FileChooser();
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
//	chooser3.~FileChooser();
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
//	chooser4.~FileChooser();
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
//	chooser2.~FileChooser();
	}
if (buttonThatWasClicked == OpenLogButton)
    {
	FileChooser chooser ("Please select log you wish to load...",File::getSpecialLocation (File::userHomeDirectory),"*.SamLog");
	if (chooser.browseForFileToOpen ())
		{
		myFileforLog = chooser.getResult ();
		MytextEditor->setText (myFileforLog.loadFileAsString());
		}
//	chooser.~FileChooser();
	}

// this works now
if (buttonThatWasClicked == ConnectionStuff)
    {
		int abort = 0;
		int abort2=0;
		if( ModParent1->getText() == String("Parent 1"))			 {abort = 1;}
		if( ModParent2->getText() == String("Parent 2"))			 {abort = 1;}
		if( ModChild1->getText()  == String("Child 1"))				 {abort2 = 1;}
		if( ModChild2->getText()  == String("Child 2"))				 {abort2 = 1;}
		if( Connect->getText()    == String("Connect/Disconnect"))	 {abort2 = 1;}
		if( NetworkBox->getText() == String("Network Type"))		 {abort2 = 1;}

		int myverylittlecount1;
		int myverylittlecount2;


		if		(abort==1) {AddToLog(" choices have not been selected for connection", 1);} // if the parents haven't be done
		else if	(abort2==1)
			{
				ModChild1->clear();
				ModChild2->clear();
				myverylittlecount1=0;
				myverylittlecount2=0;
			for ( itTextButton=SeenModules.begin() ; itTextButton != SeenModules.end(); itTextButton++ ) // get modules
				{
				TempModuleButton = *itTextButton;
				for ( itTextButton2=SeenPorts.begin() ; itTextButton2 != SeenPorts.end(); itTextButton2++ ) // get ports
					{
					TempPortButton   = *itTextButton2;
					if(TempModuleButton->getRadioGroupId()==TempPortButton->getRadioGroupId()) // if port belongs to module
						{
							if(TempModuleButton->getButtonText()==ModParent1->getText())	// if module name is in the choice
							{
								myverylittlecount1++;
								ModChild1->addItem(TempPortButton->getButtonText(),myverylittlecount1); // add the port to the list
							}
							if(TempModuleButton->getButtonText()==ModParent2->getText())
							{
								myverylittlecount2++;
								ModChild2->addItem(TempPortButton->getButtonText(),myverylittlecount2);
							}
						}
					}	
				}
		}
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
temp.IsConnected=true;
AllConnections.push_front(temp);


child1=child1.substring(1);
child2=child2.substring(1);

String Port1 = "/Port_"+parent1+"_"+child1;
String Port2 = "/Port_"+parent2+"_"+child2;
String conntype;

if(Lossyornot==T("Lossy")){conntype="udp";}
else				   {conntype="tcp";}

Network::connect(Port1,Port2,conntype,true);
Network::connect(Port2,Port1,conntype,true);

//AddToLog(Port1, 1);AddToLog(" \n", 1);
//AddToLog(Port2, 1);AddToLog(" \n", 1);

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
		killcon = false;
		mytempconnect = *it2;
		x=0;

	if((mytempconnect.Daddyfirstport == parent1) && (mytempconnect.Daddysecoundport == parent2) && (mytempconnect.firstport == child1) && (mytempconnect.secoundport == child2))
		{ 
		AddToLog(T("found something to del \n"),2);
			SavedIt = it2;
			killcon = true;
		}
		else if(mytempconnect.Daddyfirstport == parent2 && mytempconnect.Daddysecoundport == parent1 && mytempconnect.firstport == child2 && mytempconnect.secoundport == child1)
		{ 
			SavedIt = it2;
			killcon = true;
		}
		
	}
	if(killcon == true)
		{
			if(AllConnections.size()<=1){AllConnections.clear();}
			else						{SavedIt = AllConnections.erase (SavedIt);} 

			String child1=mytempconnect.firstport.substring(1);
			String child2=mytempconnect.secoundport.substring(1);

			String Port1 = "/Port_"+parent1+"_"+child1;
			String Port2 = "/Port_"+parent2+"_"+child2;

				Network::disconnect(Port1,Port2);

				Network::disconnect(Port2,Port1);

		RefreashConnections();

		AddToLog("erased connection \n",1);
		}
	
}


