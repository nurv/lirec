#include "samgargui.h"
#include "ui_samgargui.h"
#include <sstream>
#include <fstream>
#include <QTextCursor>

std::string int2str(int i)
{
    std::stringstream ss;
    std::string outString;
    ss << i;
    ss >> outString;
    return outString;
}

int str2int(std::string s)
{
    std::stringstream ss;
    int i;
    ss << s;
    ss >> i;
    return i;
}

void QtModuleContainer::modified() const
{
    emit needToRefresh();
}

void QtConnectionContainer::modified() const
{
    emit needToRefresh();
}


SamgarGui::SamgarGui(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::SamgarGui)
{
    ui->setupUi(this);

    ui->debugLogTextEdit->setReadOnly(true);
    ui->debugLogTextEdit->insertPlainText("Debug Log Init \n");

    ui->feedbackLogTextEdit->setReadOnly(true);
    ui->feedbackLogTextEdit->insertPlainText("Feedback Log Init");

    ui->parent1ComboBox->addItem("Source");
    ui->parent2ComboBox->addItem("Destination");
    ui->parentPort1ComboBox->addItem("Source Port");
    ui->parentPort2ComboBox->addItem("Destination Port");
    ui->lossyComboBox->addItem(Sg::SgConnection::ProtocolName(Sg::SgConnection::TCP).c_str(),1);
    ui->lossyComboBox->addItem(Sg::SgConnection::ProtocolName(Sg::SgConnection::UDP).c_str(),2);
    ui->lossyComboBox->addItem(Sg::SgConnection::ProtocolName(Sg::SgConnection::MCAST).c_str(),3);
    ui->connectComboBox->addItem("Connect",1);
    ui->connectComboBox->addItem("Disconnect",2);
    ui->networkComboBox->addItem(Sg::SgConnection::NetworkName(Sg::SgConnection::LAN).c_str(),1);
    ui->networkComboBox->addItem(Sg::SgConnection::NetworkName(Sg::SgConnection::PROCESS).c_str(),2);
    ui->networkComboBox->addItem(Sg::SgConnection::NetworkName(Sg::SgConnection::PLATFORM).c_str(),3);

    QStringList header;
    header.append("Source");
    header.append("Source Port");
    header.append("Destination");
    header.append("Destination Port");
    header.append("Protocol");
    header.append("Network");
    header.append("State");
    ui->tableWidget->setColumnCount(header.count());
    ui->tableWidget->setHorizontalHeaderLabels(header);

    ui->treeWidget->setColumnCount(2);
    ui->treeWidget->setHeaderLabels(QStringList() << tr("Parameter") << tr("Value"));
    ui->treeWidget->header()->setResizeMode(0, QHeaderView::Stretch);
    ui->treeWidget->header()->setResizeMode(1, QHeaderView::Stretch);

    WhatShownDebug[0]=1;
    WhatShownDebug[1]=1;
    WhatShownDebug[2]=1;
    WhatShownDebug[3]=1;

#if MIGRATION
    MigrationPort = new MigrationPortClass;
#endif
    GetCurrentServerName();
#if MIGRATION
    RegisterMigrationPort();
#endif

    Sg::SgModule::portForModules.open(Sg::SgModule::portForModulesName.c_str());
    //yarp::os::Network::sync(Sg::SgModule::portForModulesName.c_str());
    Sg::SgModule::portForModules.setStrict();

    connect(&timer, SIGNAL(timeout()), this, SLOT(timerCallback()));
    connect(ui->level1CheckBox, SIGNAL(stateChanged(int)), this, SLOT(changeDebugLevel()));
    connect(ui->level2CheckBox, SIGNAL(stateChanged(int)), this, SLOT(changeDebugLevel()));
    connect(ui->level3CheckBox, SIGNAL(stateChanged(int)), this, SLOT(changeDebugLevel()));
    connect(ui->level4CheckBox, SIGNAL(stateChanged(int)), this, SLOT(changeDebugLevel()));
    connect(ui->parent1ComboBox, SIGNAL(currentIndexChanged (int)), this, SLOT(firstModulePortComboBoxUpdate()));
    connect(ui->parent2ComboBox, SIGNAL(currentIndexChanged (int)), this, SLOT(secondModulePortComboBoxUpdate()));
    connect(ui->proceedButton, SIGNAL(clicked()), this, SLOT(connectPorts()));
    connect(ui->tableWidget, SIGNAL(cellClicked(int, int)), this, SLOT(connectionSelect(int, int)));

    connect(ui->startModuleButton, SIGNAL(clicked()), this, SLOT(startAllModules()));
    connect(ui->stopModulesButton, SIGNAL(clicked()), this, SLOT(stopAllModules()));

    connect(ui->saveButton, SIGNAL(clicked()), this, SLOT(saveAll()));
    connect(ui->loadButton, SIGNAL(clicked()), this, SLOT(loadAll()));

    connect(this, SIGNAL(connectionTableModified()), this, SLOT(firstModuleComboBoxUpdate()));
    connect(this, SIGNAL(connectionTableModified()), this, SLOT(secondModuleComboBoxUpdate()));
    connect(this, SIGNAL(connectionTableModified(Sg::SgNameType)), this, SLOT(UpdateConnectionTable(Sg::SgNameType)));

    connect(&modules, SIGNAL(needToRefresh()), this, SLOT(RefreshModulesTree()));
    connect(&connections, SIGNAL(needToRefresh()), this, SLOT(RefreshConnectionsTable()));

    ChangeServer(LocalServer);
    mainPort = new DataPort(this);
    timer.setInterval(TIMER_INTERVAL);
    timer.start();
}


SamgarGui::~SamgarGui()
{
    Sg::SgModule::portForModules.disableCallback();
#if MIGRATION
    MigrationPort->disableCallback();
#endif
    mainPort->disableCallback();
    timer.stop();

    connections.clear();
    modules.clear();

    Sg::SgModule::portForModules.close();
#if MIGRATION
    MigrationPort->close();
#endif
    mainPort->close();

    yarp::os::Network::unregisterName("/PortForModules");
    yarp::os::Network::unregisterName("/KeyToLocalServer");
    yarp::os::Network::fini();

    delete ui;
#if MIGRATION
    delete MigrationPort;
#endif
    delete mainPort;
}

void SamgarGui::changeEvent(QEvent *e)
{
    QWidget::changeEvent(e);
    switch (e->type()) {
    case QEvent::LanguageChange:
        ui->retranslateUi(this);
        break;
    default:
        break;
    }
}


/*
 * taken from MainComponent.cpp
 */

/**
 * changes the current namespace, allows to comunicate with with local and global server
 *
 * TODO: this is pointless to choose server via int value.
 * It should allow to pass the name or some type save identifier of the server.
 */
void SamgarGui::ChangeServer(ServerType change)
{
    yarp::os::Network::setNameServerName((change==LocalServer)?NameOfServer.c_str():"/global");
}

/**
 * Get the current namespace and save it so we can always switch back
 */
void SamgarGui::GetCurrentServerName(void)
{
    NameOfServer=yarp::os::Network::getNameServerName();
}

#if MIGRATE
/**
 * Register the main migration port
 */
void SamgarGui::RegisterMigrationPort (void)
{
  timer.stop();
  ChangeServer(GlobalServer);
  NameofMigrate = NameOfServer + "_Migration";
  static bool opened = MigrationPort->open(NameofMigrate.c_str());

  ChangeServer(LocalServer);
  timer.start();

  if(opened==true){AddToLog("Addig myself to global server :\n",1);}
  else			{
    AddToLog("could not Add myself to global server :\n",1);
    AddToLog("This program will take a long while to shut down :\n",1);
    MigrationPort->close();
  }
  AddToLog(NameofMigrate.c_str(),1);
  AddToLog("\n",1);
}

/** Migrate function
 *
 *  This method
 */
bool SamgarGui::Migrate (std::string nameofwhere)
{
  AddToLog(" Attempting to Migrate  \n",1);
  timer.stop();
  ChangeServer(GlobalServer);

  if(MigrationPort->isClosed()==true){AddToLog("Migration port is closed \n",1);}

  yarp::os::Bottle& MyBottle =MigrationPort->prepare();
  MyBottle.clear();

  nameofwhere = "/" + nameofwhere + "_Migration";

  AddToLog(nameofwhere.c_str(),1);AddToLog(" to ",1);AddToLog(MigrationPort->getName().c_str(),1);AddToLog("\n",1);

  bool true1= MigrationPort->addOutput(nameofwhere.c_str());


  if(true1==false){AddToLog("Could not connect to migrate, operation aborted \n",1);}


  if(true1==false){ChangeServer(LocalServer);timer.start(); return false;}

  std::string line;
  std::ifstream myfile ("Personality.txt");

  if (myfile.is_open())
    {
      while (! myfile.eof() )
        {
          getline (myfile,line);
          MyBottle.addString(line.c_str());
        }
      myfile.close();
    }
  else
    {
      AddToLog(" could not open file \n",1);
      ChangeServer(LocalServer);
      timer.start();
      return false;
    }

  MigrationPort->write();
  AddToLog(" Migration Sucsessfull \n",1);

  yarp::os::Network::disconnect(MigrationPort->getName(),nameofwhere.c_str());
  yarp::os::Network::disconnect(nameofwhere.c_str(),MigrationPort->getName());

  ChangeServer(LocalServer);
  timer.start();
  return true ;// if its sucsessfull
}


/**
 * update a list of available platforms we can migrate to
 */
void SamgarGui::UpdateMigrationProto(void)
{
  timer.stop();
  static FILE *inpipe;
  char inbuf[200];
  std::string hello;

  ChangeServer(GlobalServer);
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

          std::string mystring = inbuf;
          mystring=mystring +"\n";
          if(mystring.find('/')!=std::string::npos && mystring.find(std::string("Migration"))!=std::string::npos)
            {
              size_t start = mystring.find('/');
              size_t fin   = mystring.find(' ',start);

              mystring=mystring.substr(start,fin-start);
              MigrationPlatformsAvail.push_back(mystring);
              mystring = "--->"+mystring+   "\n";
              AddToLog(mystring,1);

              /// need to add it to the list here as well
            }
        }
    }

  ChangeServer(LocalServer);
  timer.start();
}
#endif

/**
 * Checks that each connection is working, ignores failed modules
 */
void SamgarGui::connectionsUpdate(void)
{
    Sg::SgConnectionContainerIterator itConn;
    bool connected = false;
    // look through all connection and check their state
    for ( itConn=connections.begin() ; itConn != connections.end(); itConn++ )
    {
        connected = (*itConn)->getConnected();
        if ((*itConn)->refreshState() != connected)
            emit connectionTableModified((*itConn)->getName());
    }
}

/**
 * checks the main port for each module can be connected to so its up
 * and running
 *
 */
void SamgarGui::modulesUpdate()
{
  bool needToSendOffModules=false;
  Sg::SgModuleContainerIterator itModule;
  bool active = false;
  // look through all known modules
  for ( itModule=modules.begin() ; itModule != modules.end(); itModule++ )
  {
      active = (*itModule)->getActive();
      if ( (*itModule)->refreshState() != active )
          needToSendOffModules=true;
  }
  if (needToSendOffModules)
      SendOffModuleList(); // if a modules removed send off the new list
}

/**
 * the idea here is that everytime a new module is found or lost it
 * will send off the new list to all the other modules
 */
void SamgarGui::SendOffModuleList()
{
    Sg::SgModuleContainerIterator itModule;
    yarp::os::Bottle& RR = Sg::SgModule::portForModules.prepare();
    RR.clear();
    RR.addInt( Samgar::ResetModulesListCode );
    // add ListOfKnownModules to bottle
    for ( itModule=modules.begin() ; itModule != modules.end(); itModule++ )
    {
        RR.addString((*itModule)->getName().c_str());
        RR.addString((*itModule)->getCategory().c_str());
        RR.addString((*itModule)->getSubCategory().c_str());
    }
    Sg::SgModule::portForModules.write();
}

void SamgarGui::GetModuleCommands(void)
{
  static std::vector<std::string>::iterator it;
  yarp::os::Bottle *b;
  while(Sg::SgModule::portForModules.getPendingReads()>0)
  {
      b = Sg::SgModule::portForModules.read(true);
      if(b!=NULL && b->isNull()==false) // if theres data on the port
      {
          /// if its a report on how well its done then add to a log else

          if(b->get(0).asInt() == Samgar::ActivationCode)
          {
              std::string MyNewString = b->get(1).asString().c_str();
              double ModScore = b->get(2).asInt();
              std::ostringstream strs;
              strs << ModScore;
              MyNewString = MyNewString + " achived score " + strs.str() + " \n";
          }
          else if(b->get(0).asInt() == Samgar::LogReportCode) // add to log
          {
              std::string MyNewString = b->get(1).asString().c_str();
              std::string LogScore    = b->get(2).asString().c_str();
              MyNewString = MyNewString + " : " + LogScore + " \n";
              AddToLog(MyNewString,b->get(3).asInt());
          }
          else if(b->get(0).asInt() == Samgar::AvailablePlatformsCode)
          {
#if MIGRATION
              UpdateMigrationProto();
              yarp::os::Bottle& cc = ThePortForModules->prepare();
              //			AddToLog("recived command to get all data from modules \n",1);
              cc.addInt( Samgar::ResetAvailablePlatformsCode ); // code for platforms
              for ( it=MigrationPlatformsAvail.begin() ; it != MigrationPlatformsAvail.end(); it++ )
                {
                  std::string temppy = *it;
                  cc.addString(temppy.c_str());
                }
              ThePortForModules->write();
#endif
          }
          ///// reply back with new data once a new module has been created
          // just send on
          // when new data is got from the module giving it new data send off the module list
          else if(b->get(0).asInt() == Samgar::ModuleInfoCode)
          {
              std::cout<<"--------> ModuleInfoCode"<<std::endl;
              Sg::SgModuleContainerIterator itModule = modules.get(b->get(1).asString().c_str());
              if (itModule != modules.null())
              {
                  (*itModule)->setActive(true);
                  (*itModule)->setCategory(b->get(2).asString().c_str());
                  (*itModule)->setSubCategory( b->get(3).asString().c_str());
              }
              SendOffModuleList();
          }
          // send the global command on to all modules
          else if(b->get(0).asInt()>=0 && b->get(0).asInt()<=2)
          {
              yarp::os::Bottle& cc = Sg::SgModule::portForModules.prepare();
              cc.addInt(b->get(0).asInt());
              Sg::SgModule::portForModules.write();
          }
          // send the personal command to all modules
          else if(b->get(0).asInt()>=3 && b->get(0).asInt()<=5)
          {
              // AddToLog("recived Personal module command \n",1);
              yarp::os::Bottle& cc = Sg::SgModule::portForModules.prepare();
              cc.addInt   (b->get(0).asInt());
              cc.addString(b->get(1).asString());
              Sg::SgModule::portForModules.write();
          }
      }
  }
#if MIGRATION
  if(MigrationPort->Ivebeenused==1)
  {
      AddToLog("Someone has tryed to acess my migration port \n",1);
      MigrationPort->Ivebeenused=0;
  }
#endif
}


/**
 * This function is used to display the modules and ports in a nice
 * circuler pattern needs to have some work done so it resizes a little
 * better
 */
void SamgarGui::RefreshModulesTree(void)
{
    Sg::SgModuleContainerIterator itModule;
    ui->treeWidget->clear(); // clear tree view
    //AddToLog("RefreshModulesTree\n",1);
    for ( itModule=modules.begin() ; itModule != modules.end(); itModule++ )
    {
        // module root item
        QTreeWidgetItem* currModuleItem=new QTreeWidgetItem(ui->treeWidget);
        currModuleItem->setText(0,(*itModule)->getName().c_str());
        currModuleItem->setText(1,(*itModule)->getActive()?"true":"false");
        currModuleItem->setFlags(Qt::ItemIsEnabled);

        QTreeWidgetItem* currModuleParamItem=new QTreeWidgetItem(currModuleItem);
        currModuleParamItem->setText(0,"name");
        currModuleParamItem->setText(1,(*itModule)->getName().c_str());
        currModuleParamItem->setFlags(Qt::ItemIsEnabled);

        currModuleParamItem=new QTreeWidgetItem(currModuleItem);
        currModuleParamItem->setText(0,"category");
        currModuleParamItem->setText(1,(*itModule)->getCategory().c_str());
        currModuleParamItem->setFlags(Qt::ItemIsEnabled);

        currModuleParamItem=new QTreeWidgetItem(currModuleItem);
        currModuleParamItem->setText(0,"subcategory");
        currModuleParamItem->setText(1,(*itModule)->getSubCategory().c_str());
        currModuleParamItem->setFlags(Qt::ItemIsEnabled);

        currModuleParamItem=new QTreeWidgetItem(currModuleItem);
        currModuleParamItem->setText(0,"isActive");
        currModuleParamItem->setText(1,(*itModule)->getActive()?"true":"false");
        currModuleParamItem->setFlags(Qt::ItemIsEnabled);

        currModuleParamItem=new QTreeWidgetItem(currModuleItem);
        currModuleParamItem->setText(0,"ports");
        currModuleParamItem->setFlags(Qt::ItemIsEnabled);
        Sg::SgPortContainerIterator itPort;
        for ( itPort=(*itModule)->getPorts().begin() ; itPort != (*itModule)->getPorts().end(); itPort++ )
        {
            QTreeWidgetItem* currPortItem=new QTreeWidgetItem(currModuleParamItem);
            currPortItem->setText(0,(*itPort)->getName().c_str());
            currPortItem->setFlags(Qt::ItemIsEnabled);
        }
    }
    emit connectionTableModified();
}

/**
 * RefreshConnections Used to create the network profile by figuring
 * out what modules go where and then drawing lines from port to port
 * to show how there interconnected, need to change connections from a
 * array of strings to a list of objects, so they can be added easerly
 * by other methods for starting creations
 */
void SamgarGui::RefreshConnectionsTable(void)
{
    Sg::SgConnectionContainerIterator itConn;
    int y=0;
    int conListSize = connections.size();

    ui->tableWidget->clearContents();
    ui->tableWidget->setRowCount(conListSize);
    QTableWidgetItem *newTableWidgetItem;
    for (int i=0;i<conListSize;i++){
        for (int j=0; j<7; j++){
            newTableWidgetItem = new QTableWidgetItem(QTableWidgetItem::Type);
            ui->tableWidget->setItem(i, j, newTableWidgetItem);
        }
    }
    for ( itConn = connections.begin(); itConn !=connections.end(); itConn++)
    {
         ui->tableWidget->item(y,0)->setText((*itConn)->getFirstModule()->getName().c_str());
         ui->tableWidget->item(y,1)->setText((*itConn)->getFirstModulePort()->getName().c_str());
         ui->tableWidget->item(y,2)->setText((*itConn)->getSecondModule()->getName().c_str());
         ui->tableWidget->item(y,3)->setText((*itConn)->getSecondModulePort()->getName().c_str());
         ui->tableWidget->item(y,4)->setText((*itConn)->getProtocolName().c_str());
         ui->tableWidget->item(y,5)->setText((*itConn)->getNetworkName().c_str());
         ui->tableWidget->item(y,6)->setText((*itConn)->getConnected()?"true":"false");
         y++;
    }
}


void SamgarGui::UpdateConnectionTable(Sg::SgNameType connectionName)
{
    Sg::SgConnectionContainerIterator itConn = connections.get(connectionName);
    if (itConn == connections.null())
        return;
    if ((*itConn) == NULL)
        return;
    for ( int y=0; y < ui->tableWidget->rowCount(); y++)
    {
        if ((*itConn)->getFirstModule()->getName() == ui->tableWidget->item(y,0)->text().toStdString() &&
            (*itConn)->getFirstModulePort()->getName() == ui->tableWidget->item(y,1)->text().toStdString() &&
            (*itConn)->getSecondModule()->getName() == ui->tableWidget->item(y,2)->text().toStdString() &&
            (*itConn)->getSecondModulePort()->getName() == ui->tableWidget->item(y,3)->text().toStdString() )
        {
            if ( y < ui->tableWidget->rowCount()){
               ui->tableWidget->item(y,0)->setText((*itConn)->getFirstModule()->getName().c_str());
               ui->tableWidget->item(y,1)->setText((*itConn)->getFirstModulePort()->getName().c_str());
               ui->tableWidget->item(y,2)->setText((*itConn)->getSecondModule()->getName().c_str());
               ui->tableWidget->item(y,3)->setText((*itConn)->getSecondModulePort()->getName().c_str());
               ui->tableWidget->item(y,4)->setText((*itConn)->getProtocolName().c_str());
               ui->tableWidget->item(y,5)->setText((*itConn)->getNetworkName().c_str());
               ui->tableWidget->item(y,6)->setText((*itConn)->getConnected()?"true":"false");
            }
        }
        y++;
    }
}


/**
 * adds data to the log, makes sure that if button is pressed it
 * doesn't print data they dont want
 * \param VV some kind of text (probably log info)
 * \param priority level
 * <ul>
 * <li><b>0</b>  normal debug stuff </li>
 * <li><b>1</b>  SAMGAR only debug </li>
 * <li><b>2</b>  SAMGAR Crit Level 1 </li>
 * <li><b>3</b>  normal CRIT Level 2 </li>
 * </ul>
 */
void SamgarGui::AddToLog(const std::string VV, int priority)
{
    if(WhatShownDebug[priority]==1)
    {
        switch (priority)
        {
        case 0:
            ui->debugLogTextEdit->setTextColor(QColor("black"));
            break; // normal debug stuff
        case 1:
            ui->debugLogTextEdit->setTextColor(QColor("blue"));
            break; // SAMGAR only debug
        case 2:
            ui->debugLogTextEdit->setTextColor(QColor("darkRed"));
            break; // SAMGAR Crit Level 1
        case 3:
            ui->debugLogTextEdit->setTextColor(QColor("red"));
            break; // normal CRIT Level 2
        }
        ui->debugLogTextEdit->insertPlainText(QString(VV.c_str()));
        QTextCursor c = ui->debugLogTextEdit->textCursor();
        c.movePosition(QTextCursor::End);
        ui->debugLogTextEdit->setTextCursor(c);
    }
}





//==============================================================================
/**
 * This function gets called by the connect button to add a new
 * connection to the list
 *
 * TODO: how to use lossy and Network - connected with ConnectionAutoUpdate
 */
bool SamgarGui::addConnection(const Sg::SgNameType& module1Name, const Sg::SgNameType& port1Name,
                              const Sg::SgNameType& module2Name, const Sg::SgNameType& port2Name,
                              Sg::SgConnection::SgProtocolType protocol, Sg::SgConnection::SgNetworkType network)
{
    Sg::SgModuleContainerIterator itModule;
    Sg::SgPortContainerIterator itPortStruct;
    Sg::SgModule *module1=NULL, *module2=NULL;
    Sg::SgPort *port1=NULL, *port2=NULL;
    // look through all known modules

    for ( itModule=modules.begin() ; itModule != modules.end(); itModule++ )
    {
        if ((*itModule)->getName().compare(module1Name)==0)
            module1 = &(**itModule);
        if ((*itModule)->getName().compare(module2Name)==0)
            module2 = &(**itModule);
    }
    if (module1==NULL || module2==NULL)
        return false;
    for ( itPortStruct = module1->getPorts().begin() ; itPortStruct != module1->getPorts().end(); itPortStruct++ )
    {
        if ((*itPortStruct)->getName().compare(port1Name)==0)
            port1=&(**itPortStruct);
    }
    if (port1==NULL)
        return false;
    for ( itPortStruct = module2->getPorts().begin() ; itPortStruct != module2->getPorts().end(); itPortStruct++ )
    {
        if ((*itPortStruct)->getName().compare(port2Name)==0)
            port2=&(**itPortStruct);
    }
    if (port1==NULL)
        return false;

    if (connections.add(module1, port1, module2, port2, protocol, network))
            return true;
        else
            return false;
}

/**
 * This function gets called by the connect button to deleate a new
 * connection to the list
 *
 * TODO: how to use lossy and Network - connected with ConnectionAutoUpdate
 */
bool SamgarGui::delConnection(const Sg::SgNameType& module1Name, const Sg::SgNameType& port1Name,
                              const Sg::SgNameType& module2Name, const Sg::SgNameType& port2Name,
                              Sg::SgConnection::SgProtocolType protocol, Sg::SgConnection::SgNetworkType network)
{
    Sg::SgNameType connectionName = Sg::SgConnection::FullConnectionName(module1Name, port1Name,
                                             module2Name, port2Name,
                                             protocol, network);
    return connections.del(connectionName);
}

bool SamgarGui::addModule(const Sg::SgNameType & newModuleName)
{
    return modules.add(newModuleName);
    //this->SendOffModuleList();
}

bool SamgarGui::addModulePort(const Sg::SgNameType & moduleName, const Sg::SgNameType & newPortName)
{
    return modules.addPort(moduleName, newPortName);
}

bool SamgarGui::delModule(const Sg::SgNameType & moduleName)
{
    return modules.del(moduleName);
}

bool SamgarGui::delModulePort(const Sg::SgNameType & moduleName, const Sg::SgNameType & portName)
{
    return modules.delPort(moduleName, portName);
}


/**************************************************************************
                              SLOTS
 **************************************************************************/

/**
 * Time callback checks modules and connections , also checks the
 * keytomodules port which recives data from ports ie stop commands etc
 * bounces a few of them back as well to enable one module to
 * communicate with all of them
 */
void SamgarGui::timerCallback()
{
      ChangeServer(LocalServer);
      //	Network yarp;
      modulesUpdate();
      connectionsUpdate();
      GetModuleCommands();  // this one seems fine
}

void SamgarGui::changeDebugLevel()
{
    WhatShownDebug[0]=ui->level1CheckBox->isTristate();
    WhatShownDebug[1]=ui->level2CheckBox->isTristate();
    WhatShownDebug[2]=ui->level3CheckBox->isTristate();
    WhatShownDebug[3]=ui->level4CheckBox->isTristate();
}

/** update parent1ComboBox with posible module names
 */
void SamgarGui::firstModuleComboBoxUpdate()
{
    Sg::SgModuleContainerIterator itModule;
    // look through all known modules
    ui->parent1ComboBox->clear();
    for (itModule=modules.begin() ; itModule != modules.end(); itModule++ )
        ui->parent1ComboBox->addItem((*itModule)->getName().c_str());
    if (modules.begin() == modules.end())
        ui->parent1ComboBox->addItem("Source");
}

/** update parentPort1ComboBox with posible module names
 */
void SamgarGui::firstModulePortComboBoxUpdate()
{
    Sg::SgModuleContainerIterator itModule;
    Sg::SgPortContainerIterator itPortStruct;

     // look through all known modules
     ui->parentPort1ComboBox->clear();
     for ( itModule=modules.begin() ; itModule != modules.end(); itModule++ )
     {
         if ((*itModule)->getName().compare(ui->parent1ComboBox->currentText().toStdString())==0)
         {
             for ( itPortStruct=(*itModule)->getPorts().begin(); itPortStruct != (*itModule)->getPorts().end(); itPortStruct++ )
             {
                ui->parentPort1ComboBox->addItem((*itPortStruct)->getName().c_str());
             }
             if ((*itModule)->getPorts().begin() == (*itModule)->getPorts().end())
                 ui->parent1ComboBox->addItem("Source Port");
         }
     }
}

/** update parent2ComboBox with posible module names
 */
void SamgarGui::secondModuleComboBoxUpdate()
{;
    Sg::SgModuleContainerIterator itModule;
    // look through all known modules
    ui->parent2ComboBox->clear();
    for ( itModule=modules.begin() ; itModule != modules.end(); itModule++ )
        ui->parent2ComboBox->addItem((*itModule)->getName().c_str());
    if (modules.begin() == modules.end())
        ui->parent2ComboBox->addItem("Source");
}

void SamgarGui::secondModulePortComboBoxUpdate()
{
    Sg::SgModuleContainerIterator itModule;
    Sg::SgPortContainerIterator itPortStruct;

    // look through all known modules
    ui->parentPort2ComboBox->clear();
    for ( itModule=modules.begin() ; itModule != modules.end(); itModule++ )
    {
        if ((*itModule)->getName().compare(ui->parent2ComboBox->currentText().toStdString())==0)
        {
            for ( itPortStruct=(*itModule)->getPorts().begin();
                  itPortStruct != (*itModule)->getPorts().end(); itPortStruct++ )
            {
               ui->parentPort2ComboBox->addItem((*itPortStruct)->getName().c_str());
            }
            if ((*itModule)->getPorts().begin() == (*itModule)->getPorts().end())
                ui->parent2ComboBox->addItem("Source Port");
        }
    }
}

void SamgarGui::connectPorts()
{
    std::string firstStr = "/Port_" + ui->parent1ComboBox->currentText().toStdString() +
                           "_" + ui->parentPort1ComboBox->currentText().toStdString();
    std::string secondStr = "/Port_" + ui->parent2ComboBox->currentText().toStdString() +
                            "_" + ui->parentPort2ComboBox->currentText().toStdString();

    if(ui->connectComboBox->currentText().compare("Connect")==0)
      {
        AddToLog("[AddConnection] : " + firstStr + " <=> " + secondStr +
                 ui->networkComboBox->currentText().toStdString() +"["+
                 (ui->lossyComboBox->currentText().compare("Lossy")==0?"lossy":"unlossy")+"]",1);
        if (addConnection(ui->parent1ComboBox->currentText().toStdString(),
                          ui->parentPort1ComboBox->currentText().toStdString(),
                          ui->parent2ComboBox->currentText().toStdString(),
                          ui->parentPort2ComboBox->currentText().toStdString(),
                          Sg::SgConnection::SgProtocolType(ui->lossyComboBox->currentIndex()),
                          Sg::SgConnection::SgNetworkType(ui->networkComboBox->currentIndex())))
            AddToLog("[  OK  ]\n",1);
        else
            AddToLog("[FAILED]\n",1);
      }
    else
      {
        AddToLog("[DelConnection] : " + firstStr + " <=> " + secondStr +
                 ui->networkComboBox->currentText().toStdString() +"["+
                 (ui->lossyComboBox->currentText().compare("Lossy")==0?"lossy":"unlossy")+"]",1);
        if (delConnection(ui->parent1ComboBox->currentText().toStdString(),
                          ui->parentPort1ComboBox->currentText().toStdString(),
                          ui->parent2ComboBox->currentText().toStdString(),
                          ui->parentPort2ComboBox->currentText().toStdString(),
                          Sg::SgConnection::SgProtocolType(ui->lossyComboBox->currentIndex()),
                          Sg::SgConnection::SgNetworkType(ui->networkComboBox->currentIndex())))
            AddToLog("[  OK  ]\n",1);
        else
            AddToLog("[FAILED]\n",1);
      }
}

void SamgarGui::connectionSelect(int row, int column)
{
    AddToLog("connectionSelect row="+int2str(row)+" column="+int2str(column)+" "+int2str(ui->tableWidget->rowCount())+" "+int2str(ui->tableWidget->columnCount())+"\n",3);
    QTableWidgetSelectionRange range(row,0,row,ui->tableWidget->columnCount()-1);
    QTableWidgetSelectionRange full(0,0,ui->tableWidget->rowCount()-1,ui->tableWidget->columnCount()-1);
    ui->tableWidget->setRangeSelected(full,false);
    ui->tableWidget->setRangeSelected(range,true);
}

void SamgarGui::startAllModules(void)
{
    Sg::SgModule::StartAll();
}

void SamgarGui::stopAllModules(void)
{
    Sg::SgModule::StopAll();
}

void SamgarGui::saveAll()
{
    std::ofstream settings;
    settings.open("samgar.conf"); // file where information about connections is stored

    // first write a list of all modules, format:
    // M,module_name,module_category,module_subcategory,[P,port1_name,port2_name,..,portk_name],E
    Sg::SgModuleContainerIterator itModule;
    Sg::SgPortContainerIterator itPort;
    // look through all known modules
    for ( itModule=modules.begin() ; itModule != modules.end(); itModule++ )
    {
        settings <<  "M,"<< (*itModule)->getName() <<","<< (*itModule)->getCategory()
                << "," << (*itModule)->getSubCategory();
        if ((*itModule)->getPorts().size()>0)
        {
            settings << ",P";
            for (itPort = (*itModule)->getPorts().begin(); itPort != (*itModule)->getPorts().end(); itPort++)
            {
                settings<<","<<(*itPort)->getName();
            }
        }
        settings<<",E"<<std::endl;
    }

    // after modules put the list of all connections, format:
    // C,module1_name,port1_name,module2_name,port2_name,protocol,network,E
    Sg::SgConnectionContainerIterator itConn;
    // look through all known modules
    for ( itConn=connections.begin() ; itConn != connections.end(); itConn++ )
    {
        settings <<  "C,"<< (*itConn)->getFirstModule()->getName() <<","<< (*itConn)->getFirstModulePort()->getName()
                << "," << (*itConn)->getSecondModule()->getName() <<","<< (*itConn)->getSecondModulePort()->getName()
                << "," << (*itConn)->getProtocol() << "," << (*itConn)->getNetwork()<<",E"<<std::endl;
    }
    settings.close();
}

void SamgarGui::loadAll()
{
    std::ifstream settings;
    settings.open("samgar.conf"); // file where information about connections is stored

    // first write a list of all modules, format:
    // M,module_name,module_category,module_subcategory,[P,port1_name,port2_name,..,portk_name],E
    // connections should be after modules
    // C,module1_name,port1_name,module2_name,port2_name,protocol,network,E
    Sg::SgNameType moduleName, moduleCategory, moduleSubCategory, portName;
    Sg::SgNameType module1Name, module2Name, port1Name, port2Name;
    int protocolID, networkID;
    std::string line, tmp1, tmp2;
    size_t begin, end;

    while(std::getline(settings, line))
    {
        //std::cout<<"-------> "<<line<<std::endl;
        // line by line extracting
        if (line[0]=='M')  // module
        {
            begin = line.find(',')+1;
            end = line.find(',', begin);
            moduleName = line.substr(begin,end-begin);

            begin = line.find(',',end)+1;
            end = line.find(',', begin);
            moduleCategory = line.substr(begin,end-begin);

            begin = line.find(',',end)+1;
            end = line.find(',', begin);
            moduleSubCategory = line.substr(begin,end-begin);

            //std::cout<<moduleName<<"|"<<moduleCategory<<"|"<<moduleSubCategory<<"|";
            begin = line.find(',',end)+1;
            modules.add(moduleName, moduleCategory, moduleSubCategory);
            if (line[begin]=='P')
            {
                end = line.find(',', begin);
                begin = line.find(',',end)+1;
                while (line[begin]!='E')
                {
                    end = line.find(',', begin);
                    portName = line.substr(begin,end-begin);
                    //std::cout<<portName<<"|";
                    begin = line.find(',',end)+1;
                    modules.addPort(moduleName, portName);
                }
            }
            //std::cout<<std::endl;
            modulesUpdate();
        }
        else if (line[0]=='C') // connection
        {
            begin = line.find(',')+1;
            end = line.find(',', begin);
            module1Name = line.substr(begin,end-begin);

            begin = line.find(',',end)+1;
            end = line.find(',', begin);
            port1Name = line.substr(begin,end-begin);

            begin = line.find(',',end)+1;
            end = line.find(',', begin);
            module2Name = line.substr(begin,end-begin);

            begin = line.find(',',end)+1;
            end = line.find(',', begin);
            port2Name = line.substr(begin,end-begin);

            begin = line.find(',',end)+1;
            end = line.find(',', begin);
            tmp1 = line.substr(begin,end-begin);
            protocolID = str2int(tmp1);

            begin = line.find(',',end)+1;
            end = line.find(',', begin);
            tmp2 = line.substr(begin,end-begin);
            networkID = str2int(tmp2);

            //std::cout<<module1Name<<"|"<<port1Name<<"|"<<module2Name<<"|"<<port2Name<<"|"<<protocolID <<"|"<<networkID<<"|"<<std::endl;

            addConnection(module1Name, port1Name, module2Name, port2Name,
                          Sg::SgConnection::SgProtocolType(protocolID),
                          Sg::SgConnection::SgNetworkType(networkID));
        }
    }
    settings.close();
    connectionsUpdate();
}


/**********************************************************************************************************/

DataPort::DataPort(SamgarGui* MyComp)
  {
    // cant init mycomponent here but need it to be static so its done elsewhere
    MyComponent=MyComp; //transfer over pointer to the main gui
    useCallback();      // set the port to use onRead
    MyComponent->AddToLog("Connecting to server \n",1); // add to the log
    open("/KeyToLocalServer"); // open the port
    //setStrict(true);
    hasitworked = yarp::os::Network::connect(yarp::os::Network::getNameServerName(),"/KeyToLocalServer","tcp"); // connect them up
  }

DataPort::~DataPort()
{
    disableCallback();      // set the port to use onRead
    MyComponent->AddToLog("Disconnecting from server \n",1); // add to the log
    hasitworked = yarp::os::Network::disconnect(yarp::os::Network::getNameServerName(),"/KeyToLocalServer","tcp"); // connect them up
    yarp::os::Network::unregisterName("/KeyToLocalServer");
}

void DataPort::onRead(yarp::os::Bottle& b)
  {
    static std::string myvar;
    static std::string TempVar;
    Sg::SgModuleContainerIterator itModule;

    myvar=b.toString().c_str();
    // this adds new ports to the gui
    if(myvar.find("[add]")==0) // if a port is addid
      {
        if(myvar.find("/Main")==7) // if main is the next word signifying its a module and not just a port
          {
            TempVar = myvar.substr(13);
            TempVar = TempVar.erase(TempVar.size()-1);
            MyComponent->AddToLog("[onRead] module: name = ("+TempVar+")\n",1);
            MyComponent->addModule(TempVar);
          }
        else if(myvar.find("/Port")==7)
          {
            TempVar = myvar.substr(myvar.find("_")+1);;
            std::string moduleName = TempVar;
            moduleName = moduleName.erase(moduleName.find("_"));
            std::string portName = TempVar.erase(0,TempVar.find("_")+1);
            portName = portName.erase(portName.size()-1);
            MyComponent->AddToLog("[onRead] updated port: name = "+portName+"\n",1);
            MyComponent->addModulePort(moduleName, portName);
          }
      }
 }

/**
 *  Interupt when any button is clicked
 */
/*
void SamgarGui::buttonClicked (Button* buttonThatWasClicked)
{

  if (buttonThatWasClicked == MigrateButton)
    {
      if(NameOfServer == "/Red"){Migrate("Blue");}
      if(NameOfServer == "/Blue"){Migrate("Red");}
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
      SamgarGui::AddToLog("Log has been cleared \n", 1);
    }

  if (buttonThatWasClicked == OpenLogButton)
    {
      FileChooser chooser ("Please select log you wish to load...",
                           File::getSpecialLocation (File::userHomeDirectory),
                           "*.SamLog");
      if (chooser.browseForFileToOpen ())
        {
          myFileforLog = chooser.getResult ();
          MytextEditor->setText (myFileforLog.loadFileAsString());
        }
    }

}
*/
