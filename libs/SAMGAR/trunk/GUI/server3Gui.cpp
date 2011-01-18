#include "server3Gui.h"

string MyConnectionView::fakenames[10]={"","","","","","","","","",""};

int Connecting=0;
int Currentconntype=0;
int numofconnections=0;
double connstartx=0;
double connstarty=0;
double Xshift=0;

SamConnection SamCons[maxconns];
SamConnection TempConnect;


PortIcon* SamConnection::GetDad(void)
{
   QMutexLocker locker(&mutex);
   return dad;
}

PortIcon* SamConnection::GetMom(void)	
{
   QMutexLocker locker(&mutex);
   return mom;
}

void SamConnection::SetDad(PortIcon *d)	
{
   QMutexLocker locker(&mutex);
   dad=d;
}

void SamConnection::SetMom(PortIcon *m)	
{
   QMutexLocker locker(&mutex);
   mom=m;
}

int SamConnection::GetConn(void)			
{
   QMutexLocker locker(&mutex);
   return conn;
}
void SamConnection::SetConn(int x)
{
   QMutexLocker locker(&mutex);
   conn=x;
}
bool SamConnection::GetValid(void)	
{
   QMutexLocker locker(&mutex);
   return valid;
}
void SamConnection::SetValid(bool x)	
{
   QMutexLocker locker(&mutex);
   valid=x;
}
bool SamConnection::GetConnected(void)	
{
   QMutexLocker locker(&mutex);
   return isConnected;
}
void SamConnection::SetConnected(bool x)
{
   QMutexLocker locker(&mutex);
   isConnected=x;
}


//////////////////////////////////////////////////////////////////////
// PortIcon
//////////////////////////////////////////////////////////////////////

PortIcon::PortIcon(void)
{
   IExist = false;
}

PortIcon::PortIcon(string name,string realname)
{
   IExist = true;
   myName=name;
   myRealName=realname;
   mynum = 1;
   clicked = false;
   setFlag(QGraphicsItem::ItemIsSelectable, true);
}


void PortIcon::paint(QPainter *painter, const QStyleOptionGraphicsItem *option,
		     QWidget *widget)
{
   QRectF target2(55+(mynum*95), -33, 100.0, 100.0);
   QRectF source2(0, 108.0, 100.0, 100.0);

   if(this->isSelected())
      painter->drawPixmap(target2,QPixmap(":/images/Sprites.png"), source2);
   else
      painter->drawPixmap(target2,QPixmap(":/images/Sprites.png"), source2);

   QRectF target3(75+(mynum*95),-25,60,80);
   QFont font;// = QApplication::font();
   font.setPixelSize(15);
   font.setBold(true);
   painter->setFont( font );

   QTextOption hey;
   hey.setWrapMode(hey.WrapAtWordBoundaryOrAnywhere);
   hey.setAlignment(Qt::AlignHCenter|Qt::AlignVCenter  );
   painter->drawText(target3,myName.c_str(),hey);
}

QRectF PortIcon::boundingRect() const
{
   qreal adjust = 0.5;
   return QRectF(45+(mynum*95),-50,110,100);
}

void PortIcon::mousePressEvent(QGraphicsSceneMouseEvent *event)
{
   this->update(this->boundingRect());
   if(Connecting==0) // nothing already connected
   {
      connstartx= this->scenePos().rx();
      connstarty= this->scenePos().ry();
      Connecting = 1;
      Xshift = 75+((mynum)*95);
      TempConnect.SetDad(this);
      //SamCons[numofconnections].SetDad(this);
   }
   else if(Connecting==1)
   {
      connstartx= this->scenePos().rx();
      connstarty= this->scenePos().ry();
      Connecting=0;
      TempConnect.SetMom(this);
      CreateDeleateCons();
   }
}

void PortIcon::CreateDeleateCons(void)
{
   Bottle cmd,reply;

   //AddAlterConnection
   bool NewonetoAdd = true;
   TempConnect.SetConn(Currentconntype);
		
   if(TempConnect.GetMom()==TempConnect.GetDad()) // dont allow a port to attach to itself;
   {
      puts("I'm sorry, Dave. I'm afraid I can't do that"); 
      return;
   }
   // check for the connection already existing
   for(int bb=0; bb<maxconns; bb++)
   {
      if (SamCons[bb].GetValid()!=true) // connection does not exist
	 continue;
      if(( TempConnect.GetMom()==SamCons[bb].GetMom() &&
	   TempConnect.GetDad()==SamCons[bb].GetDad() ) 
	 || ( TempConnect.GetDad()==SamCons[bb].GetMom() &&
	      TempConnect.GetMom()==SamCons[bb].GetDad()))
      {
	 SamCons[bb].SetValid(false);
	 SamCons[bb].SetConnected(false);
	 NewonetoAdd=false;

 Network::disconnect(TempConnect.GetMom()->GetRealName().c_str(),TempConnect.GetDad()->GetRealName().c_str());
 Network::disconnect(TempConnect.GetDad()->GetRealName().c_str(),TempConnect.GetMom()->GetRealName().c_str());
/*
	 cmd.clear();
	 cmd.addString("unsubscribe");
	 cmd.addString(SamCons[bb].GetMom()->GetRealName().c_str());
	 cmd.addString(SamCons[bb].GetDad()->GetRealName().c_str());
	 Network::write(Network::getNameServerContact(),cmd,reply);
	 cmd.clear();
	 cmd.addString("unsubscribe");
	 cmd.addString(SamCons[bb].GetDad()->GetRealName().c_str());
	 cmd.addString(SamCons[bb].GetMom()->GetRealName().c_str());
	 Network::write(Network::getNameServerContact(),cmd,reply);
	 Network::disconnect(TempConnect.GetMom()->GetRealName().c_str(),
			     TempConnect.GetDad()->GetRealName().c_str());
      
*/  
  }
   }
  
   this->scene()->update(); // force refreshment of the scene after removing the connection
   
   if(NewonetoAdd) // if its a new one 
   {
      for(int bb=0; bb<maxconns; bb++) // find a empty space
      {
	 if(SamCons[bb].GetValid()==false)
	 {
	    SamCons[bb].SetConn(TempConnect.GetConn());// could do = but then wouldn't be thread safe
	    SamCons[bb].SetMom(TempConnect.GetMom());
	    SamCons[bb].SetDad(TempConnect.GetDad());
	    SamCons[bb].SetValid(true);
	    string connType;
	    switch (SamCons[bb].GetConn()) {
	    case SAMdef::udp :  connType = "udp"; break;
	    case SAMdef::tcp :  connType = "tcp"; break;
	    case SAMdef::local: connType = "local"; break;
	    case SAMdef::shmem: connType = "shmem"; break;
	    default: connType = "shmem";
	    }
	    cmd.clear();
	    cmd.addString("subscribe");
	    cmd.addString(TempConnect.GetMom()->GetRealName().c_str());
	    cmd.addString(TempConnect.GetDad()->GetRealName().c_str());
	    cmd.addString(connType.c_str());
	    Network::write(Network::getNameServerContact(),cmd,reply);
	    cmd.clear();
	    cmd.addString("subscribe");
	    cmd.addString(TempConnect.GetDad()->GetRealName().c_str());
	    cmd.addString(TempConnect.GetMom()->GetRealName().c_str());
	    cmd.addString(connType.c_str());
	    Network::write(Network::getNameServerContact(),cmd,reply);
	    //Network::connect(TempConnect.GetMom()->myRealName.c_str(),TempConnect.GetDad()->myRealName.c_str(),connType.c_str());
	    //Network::connect(TempConnect.GetDad()->myRealName.c_str(),TempConnect.GetMom()->myRealName.c_str(),connType.c_str());
	    return;
	 }
      }
   } 
}

void PortIcon::SetName(string name)
{
   QMutexLocker locker(&mutex);
   myName=name;
}

void PortIcon::SetRealName(string name)
{
   QMutexLocker locker(&mutex);
   myRealName=name;
}

void PortIcon::SetParentName(string name) {
   QMutexLocker locker(&mutex);
   myParentsName=name;
}

string PortIcon::GetName(void)
{
   QMutexLocker locker(&mutex);
   return myName;
}

string PortIcon::GetRealName(void){
   QMutexLocker locker(&mutex);
   return myRealName;
}

string PortIcon::GetParentName(void){
   QMutexLocker locker(&mutex);
   return myParentsName;
}

//////////////////////////////////////////////////////////////////////
// ModuleIcon
//////////////////////////////////////////////////////////////////////



ModuleIcon::ModuleIcon(void)
{
   IExist = false;
   howmanyportsiown=0;
}

ModuleIcon::ModuleIcon(string name,string Pname[maxports]) 
{
   myName=name;
   IExist = true;
   IsRunning = false;
   online = false;
   place = 0;
  
   for(int bb =0;bb<maxports;bb++)
   {
      string hh = myName;
      if(!Pname[bb].empty())

      {
	 hh.append("_");
	 hh.append(Pname[bb]);
	 firstport[bb] = new PortIcon(Pname[bb],hh);
	 firstport[bb]->setParentItem(this);
	 firstport[bb]->mynum=bb;
	 firstport[bb]->SetParentName(myName);
	 howmanyportsiown=bb;
	 firstport[bb]->IExist=true;
      }
      //	 else
      //	{
      //		firstport[bb] = new PortIcon("Shouldn't Exist","shouldn't exist");
      //		firstport[bb]->setParentItem(this);
      //        firstport[bb]->IExist=false;
      //	}
   }
   //     firstport = new PortIcon();
   //	 firstport->setParentItem(this);
  
   this->setAcceptDrops(true);
   clicked = false;
   setFlag(QGraphicsItem::ItemIsMovable, true);
   setFlag(QGraphicsItem::ItemIsSelectable, true);
  
}
 

void ModuleIcon::paint(QPainter *painter, 
		       const QStyleOptionGraphicsItem *option,QWidget *widget)
{
   QRectF target(-40, -40, 100.0, 100.0);
   QRectF target2(-50, -50, 100.0, 100.0);
   QRectF Nsource(2.0, 1.0, 100.0, 100.0);	
   QRectF Ssource(211.0, 0.0, 100.0, 100.0);
   QRectF Asource(0.0, 0.0, 100.0, 100.0);
   QRectF Osource(100.0, 100.0, 100.0, 100.0);
   QRectF SOsource(211.0, 100.0, 100.0, 100.0);
  
   painter->save();
   painter->translate( -5,-5 );
   painter->rotate( place );
   if(IsRunning)
      place++;
   if(place>360)
      place=0;
   painter->drawPixmap(target2,QPixmap(":/images/Rotate.png"),Asource);
   painter->restore();

   if(this->isSelected())
   {
      if(online)
	 painter->drawPixmap(target,QPixmap(":/images/Sprites.png"), Ssource);
      else	  
	 painter->drawPixmap(target,QPixmap(":/images/Sprites.png"), SOsource);
   }
   else
   {
      if(online)
	 painter->drawPixmap(target,QPixmap(":/images/Sprites.png"), Nsource);
      else
	 painter->drawPixmap(target,QPixmap(":/images/Sprites.png"), Osource);
   }

   QRectF target3(-30, -40, 80.0, 80.0);
   QFont font;// = QApplication::font();
   font.setPixelSize(15);
   font.setBold(true);
   painter->setFont( font );
  
   QRect bound(0,0,100,100);
   QTextOption hey;
   hey.setWrapMode(hey.WrapAtWordBoundaryOrAnywhere);
   hey.setAlignment(Qt::AlignHCenter|Qt::AlignVCenter  );
   painter->drawText(target3,myName.c_str(),hey);
}

QRectF ModuleIcon::boundingRect() const
{
   qreal adjust = 0.5;
   return QRectF(-70,-70,140,140);
}

void ModuleIcon::advance(int step)
{
   this->update();
}

//////////////////////////////////////////////////////////////////////
// LogReadoutWindow
//////////////////////////////////////////////////////////////////////

LogReadoutWindow::LogReadoutWindow(QWidget *parent)
{
   ClearButton = new QPushButton("Clear",this);
   SaveButton  = new QPushButton("Save",this);
  
   this->resize(200,300);
   this->show();
   this->setWindowTitle(" SamgarLogWindow");
  
   QGridLayout *mainLayout = new QGridLayout; // a layout, using it incase i want to add more later
   QGridLayout *ButtonLayout = new QGridLayout;
  
   Text = new QTextEdit;
   Text->setReadOnly(true);
  
   //Text->zoomOut
   ButtonLayout->addWidget(ClearButton, 0, 0);
   ButtonLayout->addWidget(SaveButton, 0, 1);
   mainLayout->addLayout(ButtonLayout,0,0);
   mainLayout->addWidget(Text, 1, 0);
   setLayout(mainLayout);
  
   PrintHeader();
  
   connect(ClearButton, SIGNAL(clicked()),this, SLOT(ClearText()));
   connect(SaveButton, SIGNAL(clicked()),this, SLOT(SaveTxt()));
}

void LogReadoutWindow::WriteToLog(string input,bool Good)
{
   QTime time = QTime::currentTime();
   QString timeString = time.toString();
   timeString = timeString + ": " + input.c_str();
   if(Good)
      Text->setTextColor(QColor(0,100,0));
   else	 
      Text->setTextColor(QColor(100,0,0));
   Text->append(timeString);
}

void LogReadoutWindow::PrintHeader(void)
{
   QTime StartTime = QTime::currentTime();
   QDate StartDate = QDate::currentDate();
   QString header = StartDate.toString() + " " + StartTime.toString();
   Text->setTextColor(QColor(0,0,0));
   Text->append(header);
}

void LogReadoutWindow::ClearText(void)
{
   Text->clear(); 
   PrintHeader();
}

void LogReadoutWindow::SaveTxt(void)
{
  
   // QString nameoffile = StartDate.toString();
   QString fileName = QFileDialog::getSaveFileName(Text,"Save Log",QString(),".SAMlog");
   QFile file(fileName);
   if (!file.open(QFile::WriteOnly | QFile::Text)) 
      QMessageBox::warning(this, tr("Application"),
			   tr("Cannot write file %1:\n%2.").arg(fileName).arg(file.errorString()));
  
   QTextStream out(&file);
   QApplication::setOverrideCursor(Qt::WaitCursor);
   out << Text->toPlainText();
   QApplication::restoreOverrideCursor();
}


//////////////////////////////////////////////////////////////////////
// ToolWIndow
//////////////////////////////////////////////////////////////////////


ToolWindow::ToolWindow()
{
   resize(50,50);
   show();
   setWindowTitle(" Samgar Tool Window");
  
   ZoomLabel = new QLabel(tr("Zoom"));
   ZoomSlider = new QSlider(Qt::Horizontal,this);
   MSave = new QPushButton("Save",this);
   MLoad = new QPushButton("Load",this);
   About = new QPushButton("About Us",this);
   contypebox = new QComboBox(this);
   contypebox->addItem("UDP (Network,lossy)");
   contypebox->addItem("TCP (Network,unlossy)");
   contypebox->addItem("Process(process,unlossy)(use if possible)");
   contypebox->addItem("Platform(shared mem,unlossy)(unstable)");
  
   ZoomSlider->setTickPosition(QSlider::TicksBothSides);
   ZoomSlider->setTickInterval(1);
   ZoomSlider->setSingleStep(1);
   ZoomSlider->setMaximum(25);
   ZoomSlider->setMinimum(1);
  
   QGridLayout *mainLayout = new QGridLayout; // a layout, using it incase i want to add more later
   QGridLayout *SLayout = new QGridLayout;
   QGridLayout *BLayout = new QGridLayout;
  
   SLayout->addWidget(ZoomSlider, 0,1);
   SLayout->addWidget(ZoomLabel, 0, 0);
   BLayout->addWidget(MSave,0,0);
   BLayout->addWidget(MLoad,0,1);
  
   mainLayout->addLayout(SLayout,0,0);
   mainLayout->addLayout(BLayout,1,0);
   mainLayout->addWidget(About,3,0);
   mainLayout->addWidget(contypebox,2,0);
   setLayout(mainLayout);
  
   connect(About, SIGNAL(clicked()),this, SLOT(Thanks()));
   connect(contypebox, SIGNAL(currentIndexChanged(int)),this, SLOT(connectionchanged(int)));
  
   // QGridLayout *mainLayout = new QGridLayout;
}

void ToolWindow::connectionchanged(int c)
{
   Currentconntype = c;
}

void ToolWindow::Thanks(void)
{
   Thanky = new QWidget();
   Thanky->setWindowTitle("About");
   mylab =  new QLabel(Thanky);
   //mylab->setTextFormat(Qt:
   //
   mylab->setAlignment(Qt::AlignHCenter);
   mylab->setText(" \n \n ******************Samgar V3********************* \n \n Built upon yarp 2.3.0 \n http://eris.liralab.it/yarp/ \n \n \n Thanks to \n \n Supervisors \n Kerstin Dautenhahn \n Steve Ho \n Khenglee Koay \n \n Debuggers \n Fotis Papadopoulos \n Mike Bowler \n \n \n Part of LIREC \n (http://lirec.eu/) \n \n \n Also a special thanks to A.Openshaw : Coffee Fairy \n \n Author K.Du Casse \n K.Du-Casse@herts.ac.uk");
   Thanky->resize(300,450);
  
   //mylab.
   Thanky->show();
}


//////////////////////////////////////////////////////////////////////
// MyConnectionView
//////////////////////////////////////////////////////////////////////

MyConnectionView::MyConnectionView()
{
   for(int gg =0;gg<maxmodules;gg++) {
      myModuleIcon[gg] = new ModuleIcon();
   }
   mylogwindow = new LogReadoutWindow();
  
   howmanymodulesexist = 0;
   lastnum =1;
   myToolWindow = new ToolWindow;
   QRectF SizeOfMap(QPointF(-1000,-1000),QPointF(1000,1000)); 
   scene = new QGraphicsScene();
  
   scene->setSceneRect(SizeOfMap);
   scene->setItemIndexMethod(QGraphicsScene::NoIndex);
  
   myline1 = new QGraphicsLineItem(0,0,1,1);
   myline1 = scene->addLine(0,0,0,0);
   myline2 = new QGraphicsLineItem(0,0,1,1);
   myline2 = scene->addLine(0,0,0,0);
   myline3 = new QGraphicsLineItem(0,0,1,1);
   myline3 = scene->addLine(0,0,0,0);
  
   for(int yu = 0;yu<100;yu++)
   {
      myCline1[yu] = new QGraphicsLineItem(0,0,1,1);
      myCline1[yu] = scene->addLine(0,0,0,0);
      myCline2[yu] = new QGraphicsLineItem(0,0,1,1);
      myCline2[yu] = scene->addLine(0,0,0,0);
      myCline3[yu] = new QGraphicsLineItem(0,0,1,1);
      myCline3[yu] = scene->addLine(0,0,0,0);
   }

   setBackgroundBrush(QPixmap(":/images/BackGround.png"));

   setScene(scene);
   setRenderHint(QPainter::Antialiasing);
   setCacheMode(QGraphicsView::CacheBackground);
   setViewportUpdateMode(QGraphicsView::BoundingRectViewportUpdate);
   setDragMode(QGraphicsView::ScrollHandDrag);
   setWindowTitle(QT_TRANSLATE_NOOP(QGraphicsView, "Connection Window"));
   resize(700, 600);
   show();
  
   QObject::connect(&timer, SIGNAL(timeout()), scene, SLOT(advance()));
   QObject::connect(&timer, SIGNAL(timeout()), this, SLOT(updateline()));
   timer.start(1000 / 100);
  
   connect(myToolWindow->ZoomSlider, SIGNAL(valueChanged(int)), this, SLOT(zoom(int)));
   connect(myToolWindow->MSave, SIGNAL(clicked()), this, SLOT(SaveAllM()));
   connect(myToolWindow->MLoad, SIGNAL(clicked()), this, SLOT(LoadAllM()));
}
	

// everything that alters the state of the modules must go through this, as it 
// will stop thread/interupt clashes also makes it easer to see the flow of data
void MyConnectionView::AddAlterModule(int purpose, string name,
				      string Pnames[10], int x,
				      int y, bool online,
				      string dadname, 
				      string momname, int conntype,
				      bool start, bool isconn)
{
   QMutexLocker locker(&mutex); // this method can only be processed once, others have to wait or fail,
  
   switch(purpose)
   {
      case SAMdef::add_module: 
	 AddMod(name,Pnames,x,y,online); 
	 break;
      case SAMdef::online_module:
	 OffOnMod(name,true); 
	 break;
      case SAMdef::offline_module:
	 OffOnMod(name,false); 
	 break;
      case SAMdef::deleate_module:
	 DeleateMod(name);
	 break;
      case SAMdef::save_modules:
	 SaveAllmod2();
	 break;
      case SAMdef::load_modules:
	 LoadAll2();
	 break;
      case SAMdef::add_connection:
	 CreateConnection(dadname,momname,conntype,start,isconn);
	 break;
      case SAMdef::offline_connection:
	 updateconnection(dadname,momname,false,false);
	 break;
      case SAMdef::online_connection:       
	 updateconnection(dadname,momname,false,true);
	 break;
      case SAMdef::deleate_connection:
	 updateconnection(dadname,momname,true,isconn);
	 break;
   }
   //updateconnection(string d,string m,bool deleate,bool connected)
}



void MyConnectionView::DeleateMod(string name)
{
   string report = name;
   for(int yy =0;yy<maxmodules;yy++)
   {
      if(myModuleIcon[yy]->IExist && myModuleIcon[yy]->myName.compare(name)==0) // find one with the right name
      {
	 myModuleIcon[yy]->IExist=false;
	 scene->removeItem(myModuleIcon[yy]);
	 report.append(" has been deleated ");
	 mylogwindow->WriteToLog(report,false);
	 return;
      }
   }
}

void MyConnectionView::OffOnMod(string name,bool online)
{
   string report = name;
   for(int yy =0;yy<maxmodules;yy++)
   {
      if (myModuleIcon[yy]->IExist && myModuleIcon[yy]->myName.compare(name)==0) // find one with the right name
      {
	 myModuleIcon[yy]->online=online;
	 if (online==true)
	    report.append(" is now online ");
	 else
	    report.append(" is now offline ");
	 mylogwindow->WriteToLog(report,online);
	 //return;
      }
   }
   for(int uu =0;uu<maxconns;uu++)
   {
      if(SamCons[uu].GetValid()) // empty space
      {
	 if(name.compare(SamCons[uu].GetDad()->GetParentName())==0)
	    SamCons[uu].SetConnected(false);
	 if(name.compare(SamCons[uu].GetMom()->GetParentName())==0)
	    SamCons[uu].SetConnected(false);
      }
   }
}

void MyConnectionView::AddMod(string name,string Pnames[10],int x,int y,bool online)
{
   string mystring = "module ";
   mystring.append(name);
  
   // need to check it doesn't already exist
   for(int yy =0;yy<maxmodules;yy++)
   {
      if(myModuleIcon[yy]->IExist) // find a empty one
      {
	 if(myModuleIcon[yy]->myName.compare(name)==0)
	 {
	    mystring.append(" already exists, will not add to system ");
	    mylogwindow->WriteToLog(mystring,false);
	    myModuleIcon[yy]->online=online;
	    return;
	 }
      }
   }
   // doesn't exist, add one in a available spot
   for(int yy =0;yy<maxmodules;yy++)
   {
      if(!myModuleIcon[yy]->IExist) // find a empty one
      {
	 mystring.append(" has been addid to the system sucsesfully ");
	 mylogwindow->WriteToLog(mystring,true);
	 myModuleIcon[yy]= new ModuleIcon(name,Pnames);
	 scene->addItem(myModuleIcon[yy]);
	 myModuleIcon[yy]->setPos(x,y);
	 myModuleIcon[yy]->online=online;
	 return;
      }
   }
   mystring.append(" could not be addid to system, no empty spaces ");
   mylogwindow->WriteToLog(mystring,false);
}

void MyConnectionView::SaveAllmod2()
{
   QString Fname = QFileDialog::getSaveFileName(this,"Save Log",QString(),".SAMstate");
   QFile f( Fname );	
   f.open( QIODevice::WriteOnly );
   QTextStream out(&f);
   QString name;
   QTime StartTime = QTime::currentTime();
   QDate StartDate = QDate::currentDate();
   QString header = StartDate.toString() + " " + StartTime.toString();
  
   out << "this is the SAMGAR savefile \nCreated: " << header << "\n" 
       << "************************Modules & Ports************* \nModule name,# of ports -1,x,y,ports...\n";
  
   for(int hh = 0;hh<maxmodules;hh++)//void*
   {
      if(myModuleIcon[hh]->IExist)
      {
	 name = myModuleIcon[hh]->myName.c_str();
	 out << "\n"<< name << "," << myModuleIcon[hh]->howmanyportsiown << "," 
	     << myModuleIcon[hh]->pos().x() << "," << myModuleIcon[hh]->pos().y() << ",";
			
	 for(int ii=0;ii<=myModuleIcon[hh]->howmanyportsiown;ii++)
	 {
	    name = myModuleIcon[hh]->firstport[ii]->GetName().c_str();
	    out << name << ",";
	 }
      }
   }
   out << "\n\n******************connections************************** \n\nPort1,Port2,connection type \n"; // new line so we can tell 
   for(int hh =0;hh<maxconns ;hh++)
   {
      if(SamCons[hh].GetValid())
	 out << "\n" << SamCons[hh].GetDad()->GetRealName().c_str() << "," 
	     << SamCons[hh].GetMom()->GetRealName().c_str() << "," << SamCons[hh].GetConn() ;
   }

}

void MyConnectionView::LoadAll2(void)
{
   QString filename = QFileDialog::getOpenFileName(this,"Samgar File Load",
						   QString(),"*.SAMstate");
   QFile f( filename );
   f.open( QIODevice::ReadOnly );
   QTextStream in(&f);
  
   QString name;

   // TODO: assume that the comment line special sighn is # and ignore all data
   // that stands after it. 
   in.readLine(); 
   in.readLine();
   in.readLine();
   in.readLine();
   in.readLine();// ignore first couple of lines cos there for humans

   QString Data = in.readLine(); // get first valid line
   while(!Data.isEmpty()&&!Data.isNull())
   {
      string Pdata[10];// = {" "};
    
      QStringList list1 = Data.split(",");
      string name=list1.at(0).toStdString();
      int x = list1.at(2).toInt();
      int y = list1.at(3).toInt();
    
      for(int zz = 4;zz<list1.size();zz++)
      {
	 Pdata[zz-4]=list1.at(zz).toStdString();
      }
      AddMod(name,Pdata,x,y);
      Data = in.readLine();
   }
   in.readLine(); in.readLine();in.readLine();in.readLine();
  
   // load connections // put stuff here to load connections, dont worry about
   // threads as the conns themselves should be thread safe 

   Data = in.readLine(); // get first valid line
   string fake[maxports];
   while(!Data.isEmpty()&&!Data.isNull())
   {
      QStringList list1 = Data.split(",");
      string dname=list1.at(0).toStdString();
      string mname=list1.at(1).toStdString();
      int conntype = list1.at(2).toInt();
      CreateConnection(dname,mname,conntype,true,false);
      Data = in.readLine();
   }

   f.close();
  
}
void MyConnectionView::LoadAuto(void)
{

	
	if(QFile::exists(QString("AutoLoad.SAMstate")))
	{
	this->mylogwindow->WriteToLog("Loading Autoconnect file",true);
	QFile f(QString("AutoLoad.SAMstate"));
   f.open( QIODevice::ReadOnly );
   QTextStream in(&f);
  
   QString name;

   // TODO: assume that the comment line special sighn is # and ignore all data
   // that stands after it. 
   in.readLine(); 
   in.readLine();
   in.readLine();
   in.readLine();
   in.readLine();// ignore first couple of lines cos there for humans

   QString Data = in.readLine(); // get first valid line
   while(!Data.isEmpty()&&!Data.isNull())
   {
      string Pdata[10];// = {" "};
    
      QStringList list1 = Data.split(",");
      string name=list1.at(0).toStdString();
      int x = list1.at(2).toInt();
      int y = list1.at(3).toInt();
    
      for(int zz = 4;zz<list1.size();zz++)
      {
	 Pdata[zz-4]=list1.at(zz).toStdString();
      }
      AddMod(name,Pdata,x,y);
      Data = in.readLine();
   }
   in.readLine(); in.readLine();in.readLine();in.readLine();
  
   // load connections // put stuff here to load connections, dont worry about
   // threads as the conns themselves should be thread safe 

   Data = in.readLine(); // get first valid line
   string fake[maxports];
   while(!Data.isEmpty()&&!Data.isNull())
   {
      QStringList list1 = Data.split(",");
      string dname=list1.at(0).toStdString();
      string mname=list1.at(1).toStdString();
      int conntype = list1.at(2).toInt();
      CreateConnection(dname,mname,conntype,true,false);
      Data = in.readLine();
   }

   f.close();
	}
	else
	{
	this->mylogwindow->WriteToLog("No Autoconnect file exists",false);
	}
}
void MyConnectionView::updateconnection(string d,string m,bool deleate,
					bool connected)
{
   //mylogwindow->WriteToLog("looking for connection",true);
   for(int uu =0;uu<maxconns;uu++)
   {	
      if(SamCons[uu].GetValid()) // empty space
      {
	 if(d.compare(SamCons[uu].GetDad()->GetRealName())==0 && 
	    m.compare(SamCons[uu].GetMom()->GetRealName())==0)
	 {
	    ///	 mylogwindow->WriteToLog("found connection",true);
	    if(deleate)
	    {
	       SamCons[uu].SetValid(false);
	       return;
	    } 
	    else
	    {
	       SamCons[uu].SetConnected(connected);
	       return;
	    }
	    // SamCons[uu].
	 }
	 if(m.compare(SamCons[uu].GetDad()->GetRealName())==0 && 
	    d.compare(SamCons[uu].GetMom()->GetRealName())==0)
	 {
	    //mylogwindow->WriteToLog("found connection",true);
	    if(deleate)
	    {
	       SamCons[uu].SetValid(false);
	       return;
	    } 
	    else
	    {
	       SamCons[uu].SetConnected(connected);
	       return;
	    }
	 }
      }
   }
}


void MyConnectionView::CreateConnection(string d,string m,int conntype,
					bool startcon,bool iscon)
{
   Bottle cmd,reply;
   SamConnection BuiltCon;
  
   bool gotmom = false;
   bool gotdad = false;
   QString Dtota = d.c_str();
   QString Mtota = m.c_str();
   QStringList Dlist = Dtota.split("_");
   QStringList Mlist = Mtota.split("_");

   if(startcon){}// write actull yarp code to start it
  
   /*	 mylogwindow->WriteToLog("trying to connect",true);
	 mylogwindow->WriteToLog(d,false);
	 mylogwindow->WriteToLog(m,false);

	 mylogwindow->WriteToLog(Dlist.at(0).toStdString(),false);
	 mylogwindow->WriteToLog(Dlist.at(1).toStdString(),false);
	 mylogwindow->WriteToLog(Mlist.at(0).toStdString(),false);
	 mylogwindow->WriteToLog(Mlist.at(1).toStdString(),false);
   */
   for(int uu =0; uu<maxmodules; uu++)
   {										// if it exists and has the right name
      if(myModuleIcon[uu]->IExist)
      {
	 if (myModuleIcon[uu]->myName.compare(Dlist.at(0).toStdString())==0 ||
	     myModuleIcon[uu]->myName.compare(Mlist.at(0).toStdString())==0)
	 {
	    //	 mylogwindow->WriteToLog("found dad/mom module",true);
	    for(int pp=0;pp<myModuleIcon[uu]->howmanyportsiown+1;pp++)
	    {
	       if(myModuleIcon[uu]->firstport[pp]->GetName().compare(Dlist.at(1).toStdString())==0)
	       {
		  BuiltCon.SetDad(myModuleIcon[uu]->firstport[pp]);
		  gotdad=true;
		  // mylogwindow->WriteToLog("got dad port",true);
	       }
	       if(myModuleIcon[uu]->firstport[pp]->GetName().compare(Mlist.at(1).toStdString())==0)
	       {
		  BuiltCon.SetMom(myModuleIcon[uu]->firstport[pp]);
		  gotmom=true;
		  // mylogwindow->WriteToLog("got mom port",true);
	       }
	    }
	 }
      }
   }
   for(int uu =0;uu<maxconns;uu++)
   {	
      if(!SamCons[uu].GetValid()&&gotmom&&gotdad) // empty space
      {
	 SamCons[uu].SetConn(conntype);
	 SamCons[uu].SetConnected(iscon);
	 SamCons[uu].SetMom(BuiltCon.GetMom());
	 SamCons[uu].SetDad(BuiltCon.GetDad());
	 SamCons[uu].SetValid(true);
	 string connType;
	 switch (SamCons[uu].GetConn()) {
	 case SAMdef::udp :  connType = "udp"; break;
	 case SAMdef::tcp :  connType = "tcp"; break;
	 case SAMdef::local: connType = "local"; break;
	 case SAMdef::shmem: connType = "shmem"; break;
	 default: connType = "shmem";
	 }
	 cmd.clear();
	 cmd.addString("subscribe");
	 cmd.addString(BuiltCon.GetMom()->GetRealName().c_str());
	 cmd.addString(BuiltCon.GetDad()->GetRealName().c_str());
	 cmd.addString(connType.c_str());
	 Network::write(Network::getNameServerContact(),cmd,reply);
	 cmd.clear();
	 cmd.addString("subscribe");
	 cmd.addString(BuiltCon.GetDad()->GetRealName().c_str());
	 cmd.addString(BuiltCon.GetMom()->GetRealName().c_str());
	 cmd.addString(connType.c_str());
	 Network::write(Network::getNameServerContact(),cmd,reply);
	 //Network::connect(BuiltCon.GetMom()->GetRealName().c_str(),BuiltCon.GetDad()->GetRealName().c_str(),connType.c_str());
	 //Network::connect(BuiltCon.GetDad()->GetRealName().c_str(),BuiltCon.GetMom()->GetRealName().c_str(),connType.c_str());
	 return;
      }
   }
   // can just do what you want cause the actull data will be mutexed
}

void MyConnectionView::SaveAllM(void)
{
   AddAlterModule(SAMdef::save_modules);
}

void MyConnectionView::LoadAllM(void)
{
   AddAlterModule(SAMdef::load_modules);
}

void MyConnectionView::updateline(void)
{
   QPoint begin, end;
   QPen pen;

   //stuff draws connections
   for(int cc=0;cc<100;cc++)
   {
      if(SamCons[cc].GetValid())
      {
	 // define pen properties
	 pen.setWidth(4);
	 pen.setColor(SAMdef::samgarConnColor(SAMdef::SamgarConnections(SamCons[cc].GetConn())));
	 if(SamCons[cc].GetConnected()) 
	    pen.setStyle(Qt::SolidLine);
	 else 
	    pen.setStyle(Qt::DashLine);
	 // set current pen
	 myCline1[cc]->setPen(pen);
	 myCline2[cc]->setPen(pen);
	 myCline3[cc]->setPen(pen);

      
	 begin.setX( SamCons[cc].GetDad()->scenePos().rx() +
		     ((SamCons[cc].GetDad()->mynum+1)*100));
	 begin.setY( SamCons[cc].GetDad()->scenePos().ry());
	 end.setX( SamCons[cc].GetMom()->scenePos().rx() +
		   ((SamCons[cc].GetMom()->mynum+1)*100));
      	 end.setY(SamCons[cc].GetMom()->scenePos().ry());	
      

	 myCline1[cc]->setLine(begin.x(), begin.y(), begin.x(), 
			       ((end.y() - begin.y())/2) + begin.y());
	 myCline2[cc]->setLine(begin.x(), ((end.y()-begin.y())/2) + begin.y(),
			       end.x(), ((end.y()- begin.y())/2) + begin.y());
	 myCline3[cc]->setLine(end.x(), ((end.y() - begin.y())/2)+begin.y(),
			       end.x(), end.y());
      }
      else
      {
	 myCline1[cc]->setLine(0,0,0,0);
	 myCline2[cc]->setLine(0,0,0,0);
	 myCline3[cc]->setLine(0,0,0,0);
      }
   }
  
   /// this stuff is for dynamic drawing of lines if you have started to draw one
   if(Connecting == 1)
   {
      // define pen properties
      pen.setWidth(5);
      pen.setColor(SAMdef::samgarConnColor(SAMdef::SamgarConnections(Currentconntype)));
      // set current pen
      myline1->setPen(pen);
      myline2->setPen(pen);
      myline3->setPen(pen);
      QPointF mousePos = this->mapToScene(this->mapFromGlobal(this->cursor().pos()));

      begin.setX( TempConnect.GetDad()->scenePos().rx() +
		  ((TempConnect.GetDad()->mynum+1)*100));
      begin.setY( TempConnect.GetDad()->scenePos().ry());
      end.setX( mousePos.rx() );
      end.setY( mousePos.ry() );
      
      myline1->setLine(begin.x(), begin.y(), begin.x(), 
			    ((end.y() - begin.y())/2) + begin.y());
      myline2->setLine(begin.x(), ((end.y()-begin.y())/2) + begin.y(),
			    end.x(), ((end.y()- begin.y())/2) + begin.y());
      myline3->setLine(end.x(), ((end.y() - begin.y())/2)+begin.y(),
			    end.x(), end.y());
   }
   else
   {
      myline1->setLine(0,0,0,0);
      myline2->setLine(0,0,0,0);
      myline3->setLine(0,0,0,0);
   }
   scene->update();
}

void MyConnectionView::zoom(int num)     
{
   this->resetTransform();
   double bitty=num/10;
   scale(1/(1+bitty),1/(1+bitty));
  
   lastnum=num;
}

void MyConnectionView::zoomOut(void)     
{
   scale(1 / 1.2, 1 / 1.2); 
}

void MyConnectionView::rotateLeft(void)  
{
   rotate(-10); 
}

void MyConnectionView::rotateRight(void) {
   rotate(10); 
}

