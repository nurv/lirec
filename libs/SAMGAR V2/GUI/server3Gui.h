
/*
Server Gui:

No Cpp file needid for graphics, its all in this header as a class

made one mistep, my first attempted i tryed to acess varibles that could be in conflict due to multiple threads
through one method (so only mutexed in one place), made the code messy.

Although for connections the varibles themselves are mutexed

Max connections,modules and ports are predefined, makes it easer for memory and speed (scince memory is predefined and speed becouse theres no create/deleate)
*/






#include "conio.h"
#include <math.h>
#include <QtGui/QApplication>
#include <QtGui/QMainWindow>
#include <QtGui/QGridLayout>
#include <QtGui/QTextEdit>
#include <QtGui/QPushButton>
#include <QtGui/QFileDialog>
#include <QtGui/QMessageBox>
#include <QtGui/QGraphicsScene>
#include <QtGui/QGraphicsView>
#include <QtGui/QSlider>
#include <QtGui/QLabel>
#include <QtCore/QMimeData>
#include <QtGui/QGraphicsItem>

 #include <QtGui/QGraphicsSceneMouseEvent>
#include <QtGui/QBitmap>
#include <QtCore/QTime>
#include <QtCore/QDate>
#include <QtCore/QTextStream>
#include <QtCore/QTimer>
#include <QtGui/QMouseEvent>
#include <QtGui/QDrag>
#include <QtGui/QComboBox>
#include <QtGui/QMenu>
#include <QtCore/QMutex>


#include "string.h"
#include <stdio.h>
#include <stdlib.h>

#include <stdio.h>
#include <stdlib.h>

#include <yarp/os/all.h>

#include <yarp/name/NameServerManager.h>
#include <yarp/name/BootstrapServer.h>

#include "TripleSourceCreator.h"
#include "NameServiceOnTriples.h"
#include "AllocatorOnTriples.h"
#include "SubscriberOnSql.h"
#include "ComposedNameService.h"
#include "ParseName.h"


using namespace yarp::os;
using namespace yarp::name;
using namespace std;
//#include <QtGui>

// not using proper lists, only arrays so we can easily preallocated memory
// so define the max sizes

#define maxmodules 50
#define maxconns   100
#define maxports   10


using namespace std;


//extern Network yarphelp;
extern int Connecting,Currentconntype,numofconnections;
extern double connstartx,connstarty,Xshift;

class PortIcon;  
class SamConnection;
extern SamConnection TempConnect;
extern SamConnection SamCons[100];

class SAMdef
{
public:
static const int add_module     = 1;
static const int online_module  = 2;
static const int offline_module = 3;
static const int deleate_module = 4;
static const int save_modules   = 5;
static const int load_modules   = 6;

static const int add_connection     =7;
static const int deleate_connection =8;
static const int online_connection  =9;
static const int offline_connection =10;
};


class SamConnection 
{
public:


	PortIcon* GetDad(void)		{QMutexLocker locker(&mutex);return dad;}
	PortIcon* GetMom(void)		{QMutexLocker locker(&mutex);return mom;}
	void SetDad(PortIcon *d)	{QMutexLocker locker(&mutex);dad=d;}
	void SetMom(PortIcon *m)	{QMutexLocker locker(&mutex);mom=m;}
	int GetConn(void)			{QMutexLocker locker(&mutex);return conn;}
	void SetConn(int x)			{QMutexLocker locker(&mutex);conn=x;}
	bool GetValid(void)			{QMutexLocker locker(&mutex);return valid;}
	void SetValid(bool x)		{QMutexLocker locker(&mutex);valid=x;}
	bool GetConnected(void)		{QMutexLocker locker(&mutex);return isConnected;}
	void SetConnected(bool x)	{QMutexLocker locker(&mutex);isConnected=x;}

private:
mutable QMutex mutex;
	PortIcon *dad;
	PortIcon *mom;
	int conn;
	bool isConnected;
	bool valid;
};


class PortIcon : public QGraphicsItem
{
private:
string myName;
string myRealName;
string myParentsName;
mutable QMutex mutex;
public:
QPixmap pm;
int mynum;
bool IExist;
bool clicked;
	PortIcon(void)
	{
	IExist = false;
	}

	PortIcon(string name,string realname)
	{
		IExist = true;
		myName=name;
		myRealName=realname;
		mynum = 1;
		clicked = false;
	     setFlag(QGraphicsItem::ItemIsSelectable, true);
		 pm.load("Sprites.png");
		 pm.setMask(pm.createHeuristicMask(true));
	}


	void paint(QPainter *painter, const QStyleOptionGraphicsItem *option,QWidget *widget)
	 {
			QRectF target2(55+(mynum*95), -33, 100.0, 100.0);
			QRectF source2(0, 108.0, 100.0, 100.0);
			

			if(this->isSelected())
			{
			painter->drawPixmap(target2,pm, source2);
			}
			else
			{
			painter->drawPixmap(target2,pm, source2);
			}
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

	QRectF boundingRect() const
	{
     qreal adjust = 0.5;
     return QRectF(45+(mynum*95),-50,110,100);
	 
	}

 void mousePressEvent(QGraphicsSceneMouseEvent *event)
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

void CreateDeleateCons(void)
{

	//AddAlterConnection
		bool NewonetoAdd = true;
		TempConnect.SetConn(Currentconntype);
		
		if(TempConnect.GetMom()==TempConnect.GetDad()){puts("I'm sorry, Dave. I'm afraid I can't do that"); return;}// dont allow a port to attach to itself;
		// check for the connection already existing
		Bottle cmd,reply;
		for(int bb =0;bb<maxconns;bb++)
		{
			
				if(TempConnect.GetMom()==SamCons[bb].GetMom() && TempConnect.GetDad()==SamCons[bb].GetDad() &&  SamCons[bb].GetValid()==true)
				{
					SamCons[bb].SetValid(false);
					SamCons[bb].SetConnected(false);
					NewonetoAdd=false;


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
					Network::disconnect(TempConnect.GetMom()->GetRealName().c_str(),TempConnect.GetDad()->GetRealName().c_str());

				}
				if(TempConnect.GetDad()==SamCons[bb].GetMom() && TempConnect.GetMom()==SamCons[bb].GetDad() &&  SamCons[bb].GetValid()==true)
				{
					SamCons[bb].SetValid(false);
					SamCons[bb].SetConnected(false);
					NewonetoAdd=false;

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

					Network::disconnect(TempConnect.GetMom()->GetRealName().c_str(),TempConnect.GetDad()->GetRealName().c_str());
				}

		}

		this->scene()->update();

		if(NewonetoAdd) // if its a new one 
		{

		for(int bb =0;bb<maxconns;bb++) // find a empty space
			{
				if(SamCons[bb].GetValid()==false)
				{
					SamCons[bb].SetConn(TempConnect.GetConn());// could do = but then wouldn't be thread safe
					SamCons[bb].SetMom(TempConnect.GetMom());
					SamCons[bb].SetDad(TempConnect.GetDad());
					SamCons[bb].SetValid(true);
				//	yarphelp.connect
					string conny;
							if(SamCons[bb].GetConn()==0){conny="udp";}
					else	if(SamCons[bb].GetConn()==1){conny="tcp";}
					else	if(SamCons[bb].GetConn()==2){conny="local";}
					else								{conny="shmem";}


					Bottle cmd,reply;
					cmd.clear();
					cmd.addString("subscribe");
					cmd.addString(TempConnect.GetMom()->GetRealName().c_str());
					cmd.addString(TempConnect.GetDad()->GetRealName().c_str());
					cmd.addString(conny.c_str());
					Network::write(Network::getNameServerContact(),cmd,reply);
					cmd.clear();
					cmd.addString("subscribe");
					cmd.addString(TempConnect.GetDad()->GetRealName().c_str());
					cmd.addString(TempConnect.GetMom()->GetRealName().c_str());
					cmd.addString(conny.c_str());
					Network::write(Network::getNameServerContact(),cmd,reply);
					//Network::connect(TempConnect.GetMom()->myRealName.c_str(),TempConnect.GetDad()->myRealName.c_str(),conny.c_str());
					//Network::connect(TempConnect.GetDad()->myRealName.c_str(),TempConnect.GetMom()->myRealName.c_str(),conny.c_str());
					return;
				}

			}
		} 
}


void SetName(string name){QMutexLocker locker(&mutex);myName=name;}
void SetRealName(string name){QMutexLocker locker(&mutex);myRealName=name;}
void SetParentName(string name) {QMutexLocker locker(&mutex);myParentsName=name;}
string GetName(void){QMutexLocker locker(&mutex);return myName;}
string GetRealName(void){QMutexLocker locker(&mutex);return myRealName;}
string GetParentName(void){QMutexLocker locker(&mutex);return myParentsName;}
};







class ModuleIcon : public QGraphicsItem
 {
	public:
	bool dragOver;
	bool clicked;
	bool IsRunning;
	bool IExist;
	bool online;
	double place;
	PortIcon *firstport[maxports];
	QPixmap myiconpic;
	QPixmap Rotatingpic;
	string myName;
	int howmanyportsiown;

	ModuleIcon(void)
	{
	IExist = false;
	howmanyportsiown=0;
	}

     ModuleIcon(string name,string Pname[maxports]) 
	 {
		 myName=name;
		 IExist = true;
		 IsRunning = false;
		 online = false;
		 place = 0;
		 myiconpic.load("Sprites.png");
		 Rotatingpic.load("Rotate.png");
		 myiconpic.setMask(myiconpic.createHeuristicMask());
		 Rotatingpic.setMask(Rotatingpic.createHeuristicMask());


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
 

     void paint(QPainter *painter, const QStyleOptionGraphicsItem *option,QWidget *widget)
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
			if(IsRunning){ place++;}
			if(place>360){place=0;}
			painter->drawPixmap(target2,Rotatingpic, Asource);
		   painter->restore();
		


	

		 if(this->isSelected())
		{
			if(online){painter->drawPixmap(target,myiconpic, Ssource);}
			else	  {painter->drawPixmap(target,myiconpic, SOsource);}
		 }
		 else
		 {
			 if(online){painter->drawPixmap(target,myiconpic, Nsource);}
			 else	   {painter->drawPixmap(target,myiconpic, Osource);}
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
	QRectF boundingRect() const
	{
     qreal adjust = 0.5;
     return QRectF(-70,-70,140,140);
	}

	 protected:
     void advance(int step)
	 {
		 this->update();
	 }


	

 };


class LogReadoutWindow : public QWidget
 {
    Q_OBJECT

 private:
	 QGridLayout mainLayout;
	 QGridLayout ButtonLayout;
     QTextEdit *Text;
	 QPushButton *ClearButton;
	 QPushButton *SaveButton;
	 QPushButton *ZoomInButton;
	 QPushButton *ZoomOutButton;

 public:
     LogReadoutWindow(QWidget *parent = 0)
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


	 void WriteToLog(string input,bool Good)
	 {
		 QTime time = QTime::currentTime();
	     QString timeString = time.toString();
		 timeString = timeString + ": " + input.c_str();
		 if(Good){Text->setTextColor(QColor(0,100,0));}
		 else	 {Text->setTextColor(QColor(100,0,0));}
		 Text->append(timeString);
	 }

	 void PrintHeader(void)
	 {
		QTime StartTime = QTime::currentTime();
		QDate StartDate = QDate::currentDate();
		QString header = StartDate.toString() + " " + StartTime.toString();
		Text->setTextColor(QColor(0,0,0));
		Text->append(header);

	 }
private slots:

	 void ClearText(void)
	 {
		 Text->clear(); 
		PrintHeader();
	 }
	 void SaveTxt(void)
	 {

		// QString nameoffile = StartDate.toString();
		  QString fileName = QFileDialog::getSaveFileName(Text,"Save Log",QString(),".SAMlog");
		  QFile file(fileName);
		  if (!file.open(QFile::WriteOnly | QFile::Text)) 
		  {
          QMessageBox::warning(this, tr("Application"),
                tr("Cannot write file %1:\n%2.")
                .arg(fileName)
                .arg(file.errorString()));
		  }
		    QTextStream out(&file);
		    QApplication::setOverrideCursor(Qt::WaitCursor);
			out << Text->toPlainText();
			QApplication::restoreOverrideCursor();
	 }


 };

class ToolWindow : public QWidget
 {
// private:
Q_OBJECT
  public:
 QSlider *ZoomSlider;
 QLabel  *ZoomLabel;
 QPushButton *MSave,*MLoad,*About;
 QWidget *Thanky;
 QLabel *mylab;
 QComboBox *contypebox;

	ToolWindow()
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
private slots:

	void connectionchanged(int c)
	{
	Currentconntype = c;

	}
	 void Thanks(void)
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

};






class MyConnectionView : public QGraphicsView
 {
 Q_OBJECT

 private:
 QGraphicsScene *scene;
 mutable QMutex mutex;
 ToolWindow *myToolWindow;
 
 ModuleIcon *myModuleIcon[maxmodules];
 string hh[50];
 //static string fakenames[10];
static string fakenames[10];
 int howmanymodulesexist;
 int lastnum;
QTimer timer;
QGraphicsLineItem *myline1;
QGraphicsLineItem *myline2;
QGraphicsLineItem *myline3;

QGraphicsLineItem *myCline1[100];
QGraphicsLineItem *myCline2[100];
QGraphicsLineItem *myCline3[100];

 //int ConnectionStatusSAM = 0;// 0 nothing,1 one connected,2 finnished
 public:
	LogReadoutWindow *mylogwindow;


	MyConnectionView()
	 {
		for(int gg =0;gg<maxmodules;gg++)
		{
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



	 setBackgroundBrush(QPixmap("BackGround.png"));


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
	 connect(myToolWindow->MSave,       SIGNAL(clicked()), this, SLOT(SaveAllM()));
	 connect(myToolWindow->MLoad,       SIGNAL(clicked()), this, SLOT(LoadAllM()));

	 }
	

// everything that alters the state of the modules must go through this, as it will stop thread/interupt clashes
// also makes it easer to see the flow of data
	void AddAlterModule(int purpose, string name = " ",string Pnames[10] = fakenames ,int x =0,int y =0,bool online = false,string dadname = "  ",string momname = " ",int conntype = 0,bool start = false,bool isconn = false)
{
 QMutexLocker locker(&mutex); // this method can only be processed once, others have to wait or fail,
 

 if(purpose == SAMdef::add_module)				{	AddMod(name,Pnames,x,y,online);}
 else if(purpose ==SAMdef::online_module)		{	OffOnMod(name,true);}
 else if(purpose ==SAMdef::offline_module)		{	OffOnMod(name,false);}
 else if(purpose ==SAMdef::deleate_module)		{	DeleateMod(name);}
 else if(purpose ==SAMdef::save_modules)		{   SaveAllmod2(); }
 else if(purpose == SAMdef::load_modules)		{	LoadAll2();}
 else if(purpose == SAMdef::add_connection)		{   CreateConnection(dadname,momname,conntype,start,isconn);}
 else if(purpose == SAMdef::offline_connection)	{	updateconnection(dadname,momname,false,false);}
 else if(purpose == SAMdef::online_connection)	{	updateconnection(dadname,momname,false,true);}
 else if(purpose == SAMdef::deleate_connection) {	updateconnection(dadname,momname,true,isconn);}


//updateconnection(string d,string m,bool deleate,bool connected)
}




void DeleateMod(string name)
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

void OffOnMod(string name,bool online)
{
	string report = name;
 for(int yy =0;yy<maxmodules;yy++)
	{
		if(myModuleIcon[yy]->IExist && myModuleIcon[yy]->myName.compare(name)==0) // find one with the right name
		{
			myModuleIcon[yy]->online=online;
			if(online==true){report.append(" is now online ");}
			else			{report.append(" is now offline ");}
			mylogwindow->WriteToLog(report,online);
			//return;
	    }
	}

	for(int uu =0;uu<maxconns;uu++)
	{

		if(SamCons[uu].GetValid()) // empty space
		{
			if(name.compare(SamCons[uu].GetDad()->GetParentName())==0){SamCons[uu].SetConnected(false);}
			if(name.compare(SamCons[uu].GetMom()->GetParentName())==0){SamCons[uu].SetConnected(false);}
		}
	}
}

void AddMod(string name,string Pnames[10],int x=0,int y=0,bool online = false)
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

void SaveAllmod2()
	 {
	  QString Fname = QFileDialog::getSaveFileName(this,"Save Log",QString(),".SAMstate");
	  QFile f( Fname );	
      f.open( QIODevice::WriteOnly );
	  QTextStream out(&f);
	  QString name;
	  QTime StartTime = QTime::currentTime();
	  QDate StartDate = QDate::currentDate();
	  QString header = StartDate.toString() + " " + StartTime.toString();
	  
	  out << "this is the SAMGAR savefile \nCreated: " << header << "\n" << "************************Modules & Ports************* \nModule name,# of ports -1,x,y,ports...\n";
	  
	  for(int hh = 0;hh<maxmodules;hh++)//void*
	  {
		 if(myModuleIcon[hh]->IExist)
			{
			name = myModuleIcon[hh]->myName.c_str();
			out << "\n"<< name << "," << myModuleIcon[hh]->howmanyportsiown << "," << myModuleIcon[hh]->pos().x() << "," << myModuleIcon[hh]->pos().y() << ",";
			
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
		 {
			 out << "\n" << SamCons[hh].GetDad()->GetRealName().c_str() << "," << SamCons[hh].GetMom()->GetRealName().c_str() << "," << SamCons[hh].GetConn() ;
		 }
	 }

  }
void LoadAll2(void)
	 {
	  QString filename = QFileDialog::getOpenFileName(this,"Samgar File Load",QString(),".SAMstate");
      QFile f( filename );
	  f.open( QIODevice::ReadOnly );
	  QTextStream in(&f);
	
	  QString name;
	  in.readLine(); in.readLine();in.readLine();in.readLine();in.readLine();// ignore first couple of lines cos there for humans

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
	
	// load connections // put stuff here to load connections, dont worry about threads as the conns themselves should be thread safe

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

void updateconnection(string d,string m,bool deleate,bool connected)
{
 //mylogwindow->WriteToLog("looking for connection",true);
for(int uu =0;uu<maxconns;uu++)
		 {	
			 if(SamCons[uu].GetValid()) // empty space
			 {
				 if(d.compare(SamCons[uu].GetDad()->GetRealName())==0 && m.compare(SamCons[uu].GetMom()->GetRealName())==0)
				 {
				///	 mylogwindow->WriteToLog("found connection",true);
					 if(deleate){SamCons[uu].SetValid(false);return;} 
					 else		{SamCons[uu].SetConnected(connected);return;}
					// SamCons[uu].
				 }
				 if(m.compare(SamCons[uu].GetDad()->GetRealName())==0 && d.compare(SamCons[uu].GetMom()->GetRealName())==0)
				{
					//mylogwindow->WriteToLog("found connection",true);
					 if(deleate){SamCons[uu].SetValid(false);return;} 
					else		{SamCons[uu].SetConnected(connected);return;}
				}
			 }
		 }


}




void CreateConnection(string d,string m,int conntype,bool startcon,bool iscon)
	 {
		 if(startcon){}// write actull yarp code to start it
		SamConnection BuiltCon;

		bool gotmom = false;
		bool gotdad = false;
		 QString Dtota = d.c_str();
		 QString Mtota = m.c_str();
		 QStringList Dlist = Dtota.split("_");
		 QStringList Mlist = Mtota.split("_");
		
	/*	 mylogwindow->WriteToLog("trying to connect",true);
		 mylogwindow->WriteToLog(d,false);
		 mylogwindow->WriteToLog(m,false);
		

		 mylogwindow->WriteToLog(Dlist.at(0).toStdString(),false);
		 mylogwindow->WriteToLog(Dlist.at(1).toStdString(),false);
		 mylogwindow->WriteToLog(Mlist.at(0).toStdString(),false);
		 mylogwindow->WriteToLog(Mlist.at(1).toStdString(),false);
*/
		 for(int uu =0;uu<maxmodules;uu++)
		 {										// if it exists and has the right name
			 if(myModuleIcon[uu]->IExist && myModuleIcon[uu]->myName.compare(Dlist.at(0).toStdString())==0)
			 {
			//	 mylogwindow->WriteToLog("found dad module",true);
				for(int pp=0;pp<myModuleIcon[uu]->howmanyportsiown+1;pp++)
				{
					if(myModuleIcon[uu]->firstport[pp]->GetName().compare(Dlist.at(1).toStdString())==0)
					{
						BuiltCon.SetDad(myModuleIcon[uu]->firstport[pp]);
						gotdad=true;
				//		mylogwindow->WriteToLog("got dad port",true);
					}
//
				}
			}
			if(myModuleIcon[uu]->IExist && myModuleIcon[uu]->myName.compare(Mlist.at(0).toStdString())==0)
			 {
			//	 mylogwindow->WriteToLog("found mom module",true);
				 for(int pp=0;pp<myModuleIcon[uu]->howmanyportsiown+1;pp++)
				{
					if(myModuleIcon[uu]->firstport[pp]->GetName().compare(Mlist.at(1).toStdString())==0)
					{
						BuiltCon.SetMom(myModuleIcon[uu]->firstport[pp]);
						gotmom=true;
				//		mylogwindow->WriteToLog("got mom port",true);
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
				 string conny;
							if(SamCons[uu].GetConn()==0){conny="udp";}
					else	if(SamCons[uu].GetConn()==1){conny="tcp";}
					else	if(SamCons[uu].GetConn()==2){conny="local";}
					else								{conny="shmem";}

					Bottle cmd,reply;
					cmd.clear();
					cmd.addString("subscribe");
					cmd.addString(BuiltCon.GetMom()->GetRealName().c_str());
					cmd.addString(BuiltCon.GetDad()->GetRealName().c_str());
					cmd.addString(conny.c_str());
					Network::write(Network::getNameServerContact(),cmd,reply);
					cmd.clear();
					cmd.addString("subscribe");
					cmd.addString(BuiltCon.GetDad()->GetRealName().c_str());
					cmd.addString(BuiltCon.GetMom()->GetRealName().c_str());
					cmd.addString(conny.c_str());
					Network::write(Network::getNameServerContact(),cmd,reply);
					//Network::connect(BuiltCon.GetMom()->GetRealName().c_str(),BuiltCon.GetDad()->GetRealName().c_str(),conny.c_str());
					//Network::connect(BuiltCon.GetDad()->GetRealName().c_str(),BuiltCon.GetMom()->GetRealName().c_str(),conny.c_str());

				 return;
			 }
		}

		// can just do what you want cause the actull data will be mutexed


	}
 private slots:

	// wanted to save and load from the modules direcly instead of doing it here but QT was a bit funny and i didn't have time

	void SaveAllM(void)
	{
		AddAlterModule(SAMdef::save_modules);
	}





	 void LoadAllM(void)
	 {
		 AddAlterModule(SAMdef::load_modules);
	 }

	 void updateline(void)
	 {
		double Sx,Sy,Fx,Fy;

		QPen mypenforc;
		//stuff draws connections
		for(int cc=0;cc<100;cc++)
		{

			if(SamCons[cc].GetValid())
			{
				Sx = SamCons[cc].GetDad()->scenePos().rx() + ((SamCons[cc].GetDad()->mynum+1)*100);
				Fx = SamCons[cc].GetMom()->scenePos().rx() + ((SamCons[cc].GetMom()->mynum+1)*100);

				Sy = SamCons[cc].GetDad()->scenePos().ry();
				Fy = SamCons[cc].GetMom()->scenePos().ry();	
				
				mypenforc.setWidth(4);
				
				if(SamCons[cc].GetConnected()) {mypenforc.setStyle(Qt::SolidLine);}
				else						   {mypenforc.setStyle(Qt::DashLine);}

				if(SamCons[cc].GetConn()==0){mypenforc.setColor(QColor(50,50,50));}
				if(SamCons[cc].GetConn()==1){mypenforc.setColor(QColor(150,50,50));}
				if(SamCons[cc].GetConn()==2){mypenforc.setColor(QColor(50,150,50));}
				if(SamCons[cc].GetConn()==3){mypenforc.setColor(QColor(50,50,150));}


				myCline1[cc]->setPen(mypenforc);
				myCline2[cc]->setPen(mypenforc);
				myCline3[cc]->setPen(mypenforc);
				myCline1[cc]->setLine(Sx,Sy,Sx,((Fy-Sy)/2)+Sy);
				myCline2[cc]->setLine(Sx,((Fy-Sy)/2)+Sy,Fx,((Fy-Sy)/2)+Sy);
				myCline3[cc]->setLine(Fx,((Fy-Sy)/2)+Sy,Fx,Fy);

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
		

		QPen mypen;
		mypen.setWidth(5);


		if(Currentconntype==0){mypen.setColor(QColor(50,50,50));}
		if(Currentconntype==1){mypen.setColor(QColor(150,50,50));}
		if(Currentconntype==2){mypen.setColor(QColor(50,150,50));}
		if(Currentconntype==3){mypen.setColor(QColor(50,50,150));}
		//
		myline1->setPen(mypen);
		myline2->setPen(mypen);
		myline3->setPen(mypen);

		double x = this->cursor().pos().rx()-this->pos().rx()-(this->size().width()/2);
		double y = this->cursor().pos().ry()-this->pos().ry()-(this->size().height()/2);


		 double YoffSet =0;

		 if(y>connstarty+25){YoffSet=0;}
		 else				{YoffSet=0;}


		 double xdiff = x - (connstartx+Xshift);
		 double ydiff = y - (connstarty+YoffSet);

			 
		 myline1->setLine(connstartx+Xshift,connstarty+YoffSet,connstartx+Xshift,connstarty+YoffSet+(ydiff/2));// straight down but only half value
		 myline2->setLine(connstartx+Xshift,connstarty+YoffSet+(ydiff/2),connstartx+Xshift+xdiff,connstarty+YoffSet+(ydiff/2));
		 myline3->setLine(connstartx+Xshift+xdiff,connstarty+YoffSet+(ydiff/2),connstartx+Xshift+xdiff,connstarty+YoffSet+(ydiff));


		 scene->update();
		 }
		 else
		 {
		 myline1->setLine(0,0,0,0);
		 myline2->setLine(0,0,0,0);
		 myline3->setLine(0,0,0,0);
		 }

	 }

	 void zoom(int num)     
	 {
		 this->resetTransform();

		 double bitty=num/10;
		 scale(1/(1+bitty),1/(1+bitty));

		 lastnum=num;
	 
	 }
     void zoomOut(void)     { scale(1 / 1.2, 1 / 1.2); }
     void rotateLeft(void)  { rotate(-10); }
     void rotateRight(void) { rotate(10); }
 };

 