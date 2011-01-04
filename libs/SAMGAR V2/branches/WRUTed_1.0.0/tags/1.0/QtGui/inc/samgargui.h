#ifndef SAMGARGUI_H
#define SAMGARGUI_H

#include <QWidget>

#include <QTime>
#include <QFile>
#include <QTimer>
#include <QTableWidgetItem>

#define TIMER_INTERVAL 100
#define MIGRATE 1

#include <iostream>
#include <fstream>
#include <string>

#include <yarp/sig/all.h>
#include <yarp/os/all.h>
#include <SamgarVars.h>

#include "SgConnection.h"
#include "SgModuleContainer.h"
#include "SgConnectionContainer.h"

enum ServerType {LocalServer, GlobalServer};


#if MIGRATE
#include "SgMigration.h"
#endif

namespace Ui
{
    class SamgarGui;
}

class SamgarGui;

class DataPort : public Sg::SgPortDataType
{
public:
    SamgarGui* MyComponent;
    DataPort(SamgarGui* MyComp);
    ~DataPort();
    virtual void onRead(yarp::os::Bottle& b);
private:
    bool hasitworked;
};

/*
 *
 */
class QtModuleContainer : public QObject, public Sg::SgModuleContainer
{
    Q_OBJECT
protected:
    virtual void modified() const;
public:
signals:
    void needToRefresh() const;
protected:
    void changeEvent(QEvent *e);
};

/*
 *
 */
class QtConnectionContainer : public QObject, public Sg::SgConnectionContainer
{
    Q_OBJECT
protected:
    virtual void modified() const;
public:
signals:
    void needToRefresh() const;
protected:
    void changeEvent(QEvent *e);
};

/*
 *
 */
class SamgarGui : public QWidget
{
    Q_OBJECT
public:
    SamgarGui(QWidget *parent = 0);
    ~SamgarGui();

    void AddToLog(const std::string,int); // add string to 1 of 4 log levels


public:
    /*
     *  Connection list
     */
    bool addConnection(const Sg::SgNameType& module1Name, const Sg::SgNameType& port1Name,
                       const Sg::SgNameType& module2Name, const Sg::SgNameType& port2Name,
                       Sg::SgConnection::SgProtocolType protocol,
                       Sg::SgConnection::SgNetworkType network); // add new conection
    bool delConnection(const Sg::SgNameType& module1Name, const Sg::SgNameType& port1Name,
                       const Sg::SgNameType& module2Name, const Sg::SgNameType& port2Name,
                       Sg::SgConnection::SgProtocolType protocol,
                       Sg::SgConnection::SgNetworkType network); // del conection
    /*
     *  Modules list
     */
    bool addModule(const Sg::SgNameType & newModuleName);
    bool addModulePort(const Sg::SgNameType & moduleName, const Sg::SgNameType & newPortName);
    bool delModule(const Sg::SgNameType & moduleName);
    bool delModulePort(const Sg::SgNameType & moduleName, const Sg::SgNameType & portName);

private:
    void connectionsUpdate(void); // update connection state
    void modulesUpdate(void); // check if it is posible to connect to main ports
    void GetModuleCommands(void); // geather information from the port for modules
    void SendOffModuleList(void); // when modules are changed it propagate new module list

#if MIGRATE
    void UpdateMigrationProto(void); // update list of platform where can migrate
    void RegisterMigrationPort (void); // add new migration port
    bool Migrate (std::string nameofwhere); // megrate to server of given name
    std::vector<std::string> MigrationPlatformsAvail; // list of possible migration platforms
#endif

    std::string NameOfServer;  // local yarp server name
    std::string NameofMigrate; // migration port name on local yarp server

    void ChangeServer(ServerType change); // change the current yarp server that aplication is communicationg with (change namespace)
    void GetCurrentServerName(void); // set NameOfServer based uppon corrent yarp setings

    QTime MyTime; // used for putting date in log file
    // log files
    QFile myFileforLog;
    QFile myFileforModReport;
    QFile myFileforMod;
    QFile myFileforCon;

    QTimer timer;

    QtModuleContainer modules; // structure keeping all modules inormation, ports are included in modules
    QtConnectionContainer  connections; // structure keeping all conections inormation

    int WhatShownDebug[4]; // debug flags

    Ui::SamgarGui *ui;
    DataPort* mainPort;

#if MIGRATE
    Sg::MigrationPortClass *MigrationPort; //class used to perform migration process
#endif

private slots:
    void timerCallback(void);
    void changeDebugLevel(void);
    void firstModuleComboBoxUpdate(void);
    void firstModulePortComboBoxUpdate(void);
    void secondModuleComboBoxUpdate(void);
    void secondModulePortComboBoxUpdate(void);
    void connectPorts(void);
    void connectionSelect(int row, int column);
    void UpdateConnectionTable(Sg::SgNameType connectionName);
    void RefreshModulesTree(void);      // refresh information about modules - delete old and put new one
    void RefreshConnectionsTable(void); // refresh information about connections - delete old and put new one
    void startAllModules(void);
    void stopAllModules(void);
    void saveAll();
    void loadAll();

signals:
    void connectionTableModified();
    void connectionTableModified(Sg::SgNameType connectionName);

protected:
    void changeEvent(QEvent *e);
};

#endif // SAMGARGUI_H
