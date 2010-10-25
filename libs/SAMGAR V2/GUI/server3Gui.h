#ifndef _SERVER3GUI_H
#define _SERVER3GUI_H

/*
Server Gui:

No Cpp file needid for graphics, its all in this header as a class

made one mistep, my first attempted i tryed to acess varibles that could be in conflict due to multiple threads
through one method (so only mutexed in one place), made the code messy.

Although for connections the varibles themselves are mutexed

Max connections,modules and ports are predefined, makes it easer for memory and speed (scince memory is predefined and speed becouse theres no create/deleate)
*/

#ifdef _WIN32
  #include "conio.h"
#endif
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

class PortIcon;  
class SamConnection;

namespace SAMdef
{
   enum SamgarDefinition {add_module = 1,      //!< add module flag 
			  online_module,       //!< module is online
			  offline_module,      //!< module is offline
			  deleate_module,      /*!< module is deleated or going
						*   to be delated 
						*/
			  save_modules,        //!< save modules and connections
			  load_modules,        //!< load modules and connections
			  add_connection,      //!< add new connection
			  deleate_connection,  //!< delete existing connection
			  online_connection,   //!< connection is valid
			  offline_connection   //!< connection is invalid
   };

   enum SamgarConnections { udp=0,    //!< udp connection
			    tcp,      //!< tcp connection
			    local,    //!< local connection
			    shmem     //!< shared memory connection
   };

   static QColor samgarConnColor(SamgarConnections conn)
   {
     switch (conn) {
     case SAMdef::udp :  return QColor(50,50,50);
     case SAMdef::tcp :  return QColor(150,50,50);
     case SAMdef::local: return QColor(50,150,50);
     case SAMdef::shmem: return QColor(50,50,150);
     default: return QColor(50,50,150);
     }
   }
};

class SamConnection 
{
 public:
  PortIcon* GetDad(void);	
  PortIcon* GetMom(void);	
  void SetDad(PortIcon *d);
  void SetMom(PortIcon *m);
  int GetConn(void);
  void SetConn(int x);
  bool GetValid(void);
  void SetValid(bool x);
  bool GetConnected(void);
  void SetConnected(bool x);

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
  int mynum;
  bool IExist;
  bool clicked;

  PortIcon(void);
  PortIcon(string name,string realname);
  
  void paint(QPainter *painter, const QStyleOptionGraphicsItem *option,
	     QWidget *widget);
  QRectF boundingRect() const;
  void mousePressEvent(QGraphicsSceneMouseEvent *event);

  void CreateDeleateCons(void);

  void SetName(string name);
  void SetRealName(string name);
  void SetParentName(string name);
  string GetName(void);
  string GetRealName(void);
  string GetParentName(void);
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
  string myName;
  int howmanyportsiown;
  
  ModuleIcon(void);
  ModuleIcon(string name,string Pname[maxports]);
  
  void paint(QPainter *painter, const QStyleOptionGraphicsItem *option,
	     QWidget *widget);
  QRectF boundingRect() const;
 protected:
  void advance(int step);
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
  LogReadoutWindow(QWidget *parent = 0);

  void WriteToLog(string input,bool Good);
  void PrintHeader(void);

private slots:

  void ClearText(void);
  void SaveTxt(void);
};

class ToolWindow : public QWidget
{
  Q_OBJECT

 public:
  QSlider *ZoomSlider;
  QLabel  *ZoomLabel;
  QPushButton *MSave,*MLoad,*About;
  QWidget *Thanky;
  QLabel *mylab;
  QComboBox *contypebox;
  
  ToolWindow();
private slots:
  
  void connectionchanged(int c);
  
  void Thanks(void);
};

class MyConnectionView : public QGraphicsView
{
  Q_OBJECT

 private:
  QGraphicsScene *scene;
  mutable QMutex mutex;
  ToolWindow *myToolWindow;
  
  ModuleIcon *myModuleIcon[maxmodules];
  QPixmap *modulePixmap[maxmodules];
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
  
  MyConnectionView();

  // everything that alters the state of the modules must go through this, as it will stop thread/interupt clashes
  // also makes it easer to see the flow of data
  void AddAlterModule(int purpose, string name = " ",
		      string Pnames[10] = fakenames ,int x =0,int y =0,
		      bool online = false,string dadname = "  ",
		      string momname = " ",int conntype = 0,
		      bool start = false,bool isconn = false);

  void DeleateMod(string name);

  void OffOnMod(string name,bool online);
  
  void AddMod(string name,string Pnames[10],int x=0,int y=0,bool online = false);

  void SaveAllmod2();

  void LoadAll2(void);

  void updateconnection(string d, string m, bool deleate, bool connected); 

  void CreateConnection(string d, string m, int conntype, bool startcon,
			bool iscon);

 private slots:

  // wanted to save and load from the modules direcly instead of doing
  //  it here but QT was a bit funny and i didn't have time
  
  void SaveAllM(void);
  void LoadAllM(void);
  void updateline(void);
  void zoom(int num);     
  void zoomOut(void);
  void rotateLeft(void);
  void rotateRight(void);
};

#endif
