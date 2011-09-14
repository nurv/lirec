#pragma once
#include "SamClass.h"
#include <stdlib.h>
#include <iostream>
#include <fstream>
#include <windows.h>
#include <stdio.h>
#include <ctype.h>
#include <time.h>
#include <sstream>
#include "phidget21.h"




CPhidgetInterfaceKitHandle ifKit;
double dVoltage;
double dVoltageNow;
double dCurrent;
double dCurrentLaptop;
double dCpuUsage;
double dPower;
int iPhoneRing;
bool bPhoneRingChanged=false;
time_t start,end, now;


//------------------------------------------------------------------------------------------------------------------
// Prototype(s)...
//------------------------------------------------------------------------------------------------------------------
CHAR cpuusage(void);

//-----------------------------------------------------
typedef BOOL ( __stdcall * pfnGetSystemTimes)( LPFILETIME lpIdleTime, LPFILETIME lpKernelTime, LPFILETIME lpUserTime );
static pfnGetSystemTimes s_pfnGetSystemTimes = NULL;

static HMODULE s_hKernel = NULL;

int __stdcall SensorChangeHandler(CPhidgetInterfaceKitHandle IFK, void *usrptr, int Index, int Value);
void GetSystemTimesAddress();
CHAR cpuusage();
int iflag=0;
string task;

using namespace std;
using namespace yarp;

class PhidgetSensor: public SamClass
{
private:
int iVoltage;
BufferedPort<Bottle> bRead, bPhone;
Network yarp;

public:
	bool bCharging;
	
	void SamInit(void)
	{
	iVoltage=0;
	task="";
	bCharging=true;
	RecognisePort("PIn");
	RecognisePort("PhoneOut");
	StartModule("/Phidget");
	bRead.open("/Phidget_PIn");
	bPhone.open("/Phidget_PhoneOut");

	bRead.setStrict(true);
	bPhone.setStrict(true);
	bRead.setReporter(myPortStatus);
	bPhone.setReporter(myPortStatus);

	//relay switch: on mobile base and start modules
	CPhidgetInterfaceKit_setOutputState (ifKit, 6, 1);
	yarp::os::Time::delay(2);
	system("start.bat");

	}
	void SamIter(void)
	{
		Bottle *b = bRead.read(false);

		//while (bRead.getPendingReads() > 0)
		//	b = bRead.read(false); // get in the input from the port, if you want it to wait use true, else use false

		if(b!=NULL)						 // check theres data
		{
			
			if(b->size()>0)
			{
				task = b->get(0).asString();
				int iStatus=atoi(b->get(1).asString().c_str());
				//check if message is related to charging
				if(task.compare("ChargingStatus")==0)
				{
					
					//relay switch: off mobile base and close modules
					if(iStatus==0)
					{
						yarp::os::Time::delay(10);
						CPhidgetInterfaceKit_setOutputState (ifKit, 6, 0);
						system("stop.bat");
						bCharging=true;
					}
					else if(iStatus==1 && bCharging==true)//relay switch: on mobile base and start modules
					{
						CPhidgetInterfaceKit_setOutputState (ifKit, 6, 1);
						yarp::os::Time::delay(2);
						system("start.bat");
						bCharging=false;
					}
				}
				else
				{
					iflag = iStatus;
					//start timer 
					if(iflag==1)
						time (&start);
				}
			
			}

			std::cout << "got a task " << b->toString().c_str() << std::endl;
		}

		//send phone ring sensor data only if sensor value is changed
		if(bPhoneRingChanged)
		{
			// send back a bottle with current voltage value
			Bottle& b3 = bPhone.prepare();	  // prepare the bottle/port
			b3.clear();
			b3.addInt( iPhoneRing ); // indicates robot voltage		
			bPhone.writeStrict();
			bPhoneRingChanged=false;
		}

	}

	void SamSendVoltage(void)
	{
		/*
		int iVolt=0;
		if(dVoltageNow<=12.4999)//require recharge
			iVolt=0;
		else if(dVoltageNow>=12.5000 && dVoltageNow<14.0000)//fine
			iVolt=1;
		else if (dVoltageNow>=14.0000)//fully charged
			iVolt=2;
		*/

		
		std::string s;
 
		// convert double b to string s
		std::ostringstream ss;
		ss << dVoltageNow;
		s = ss.str();

		
		//if(iVoltage!=iVolt)
		//{
			// send back a bottle with current voltage value
			Bottle& b2 = bRead.prepare();// prepare the bottle/port
			b2.clear();
			//b2.addInt( iVolt );// indicates robot voltage		
			b2.addString(s.c_str());
			bRead.writeStrict();
			
			//iVoltage=iVolt;
		//}
		std::cout << " voltage " << dVoltageNow << std::endl; 


	}
	
};


int main () 
{
	
	ifKit = 0; //Declare an InterfaceKit handle
	CPhidgetInterfaceKit_create(&ifKit); //Create the InterfaceKit object

	CPhidget_open((CPhidgetHandle)ifKit, -1);
	CPhidget_waitForAttachment((CPhidgetHandle)ifKit, 2500);
	CPhidgetInterfaceKit_setDataRate (ifKit, 1, 5);//current sensor robot
	CPhidgetInterfaceKit_setDataRate (ifKit, 2, 5);//voltage sensor robot
	CPhidgetInterfaceKit_setDataRate (ifKit, 4, 5);//current sensor laptop
	CPhidgetInterfaceKit_setDataRate (ifKit, 5, 5);//phone light sensor

	CPhidgetInterfaceKit_setRatiometric (ifKit, PTRUE);


	CPhidgetInterfaceKit_set_OnSensorChange_Handler (ifKit, SensorChangeHandler, NULL);
	
	GetSystemTimesAddress();
	PhidgetSensor ph;
	ph.SamInit();
	yarp::os::Time::delay(1);
	//ExampleOneWrite myfirstmodule;
	//myfirstmodule.SamInit();

	
	double Cntr;	
	dVoltage=0;
	dCurrentLaptop=0;
	dCurrent=0;
	dCpuUsage=0;
	dPower=0;
	Cntr=0;
	iPhoneRing=0;
	
	
	while(1)
	{

		// task is received
		if(iflag==1)
			yarp::os::Time::delay(0.05);
		else
		{	//send voltage when robot does not have a task running
			yarp::os::Time::delay(2);
			ph.SamSendVoltage();
		}

		//if(testCntr%2==0)
		//	CPhidgetInterfaceKit_setOutputState (ifKit, 2, 0);
		//else
		//	CPhidgetInterfaceKit_setOutputState (ifKit, 2, 1);


		int vtval;
		CPhidgetInterfaceKit_getSensorRawValue(ifKit, 2, &vtval);
		dVoltageNow = ((vtval / 4.095) * 0.06) - 30;
		

		ph.SamIter();
		
		if(iflag==1)
		{

			Cntr++;
			int cval;
			int clval;
			int vval;
			//for (int i = 0; i < 10; i++) {
				//CPhidgetInterfaceKit_getSensorValue(ifKit, 1, &cval);
			    
				//dCurrent = (cval / 13.2) - 37.8787;
				CPhidgetInterfaceKit_getSensorRawValue(ifKit, 1, &cval);
				//dCurrent = ((cval / 4.095) / 13.2) - 37.8787;
				dCurrent = dCurrent + ((cval / 4.095) / 13.2) - 37.8787;
				//dCurrent = cval / 4.095; RawSensorValue / 4.095
				//std::cout << "current robot " << ((cval / 4.095) / 13.2) - 37.8787 << std::endl;

				//dCurrent = (cval / 13.2) - 37.8787;
				CPhidgetInterfaceKit_getSensorRawValue(ifKit, 4, &clval);
				//dCurrentLaptop = ((clval / 4.095) / 13.2) - 37.8787;
				dCurrentLaptop = dCurrentLaptop + ((clval / 4.095) / 13.2) - 37.8787 ;
				//std::cout << "current laptop " << ((clval / 4.095) / 13.2) - 37.8787 << std::endl;
				

				//CPhidgetInterfaceKit_getSensorValue(ifKit, 2, &vval);
				CPhidgetInterfaceKit_getSensorRawValue(ifKit, 2, &vval);
				//dVoltage = ((vval / 4.095) * 0.06) - 30;
				dVoltage = dVoltage + ((vval / 4.095) * 0.06) - 30;
				//std::cout << "voltage " << ((vval / 4.095) * 0.06) - 30 << std::endl;

				//dCpuUsage = (double) cpuusage();
				dCpuUsage= dCpuUsage+ (double) cpuusage();
				//std::cout << "cpu " << cpuusage() << std::endl;

				
				
				
				//printf("Value: %d\n", val);
				
			//}
				//std::cout << "started power monitor" << std::endl;
		}
		else if(iflag==2)
		{
			dPower = dCurrent * dVoltage;
			if(Cntr!=0)
			{
				std::cout << "total" <<"cpuusage% " << dCpuUsage << " current " << dCurrent  << " Voltage " << dVoltage << " Power " << dPower <<  " cntr " << Cntr << std::endl;
				std::cout << " avg " << "cpuusage% " << dCpuUsage/Cntr << "Current laptop " <<  dCurrentLaptop/Cntr << " current " << dCurrent/Cntr  << " Voltage " << dVoltage/Cntr << " Power " << dPower/Cntr <<  " cntr " << Cntr << std::endl;
			

				time (&end);
				double dif;
				dif = difftime (end,start);

				ofstream myfile;
				myfile.open ("PowerLog.txt",ios::app);

				time (&now);
				string strtimenow = (string)ctime(&now);
				myfile << task.c_str() << " " << dCpuUsage/Cntr << " " << dCurrentLaptop/Cntr << " " << dCurrent/Cntr  << " " << dVoltage/Cntr << " " << dPower/Cntr <<  " " << dif << " " << strtimenow.c_str();
				myfile.close();

				dVoltage=0;
				dCurrentLaptop=0;
				dCurrent=0;
				dCpuUsage=0;
				dPower=0;
				Cntr=0;
				iflag=0;
			}
		
		}
		

			
	}

	CPhidget_close((CPhidgetHandle)ifKit);
	CPhidget_delete((CPhidgetHandle)ifKit);


}



int __stdcall SensorChangeHandler(CPhidgetInterfaceKitHandle IFK, void *usrptr, int Index, int Value)
{

	//std::cout << "Sensor ID " << Index << " " << " Value " << Value << std::endl;	

	if (Index==5)
	{
		if(Value < 10 )
			iPhoneRing = 0;//not ringing
		else if(Value >= 10 && Value <= 30)//ringing
			iPhoneRing = 1;
		else if(Value > 30)//phone picked
	   		iPhoneRing = 2;

		std::cout << "Sensor ID " << Index << " " << " Value " << Value << std::endl;	
		bPhoneRingChanged=true;
	}
	
	return 0;
}




//-----------------------------------------------------
void GetSystemTimesAddress()
{
	if( s_hKernel == NULL )
	{   
		s_hKernel = LoadLibrary( "Kernel32.dll" );
		if( s_hKernel != NULL )
		{
			s_pfnGetSystemTimes = (pfnGetSystemTimes)GetProcAddress( s_hKernel, "GetSystemTimes" );
			if( s_pfnGetSystemTimes == NULL )
			{
				FreeLibrary( s_hKernel ); s_hKernel = NULL;
			}
		}
	}
}
//----------------------------------------------------------------------------------------------------------------

//----------------------------------------------------------------------------------------------------------------
// cpuusage(void)
// ==============
// Return a CHAR value in the range 0 - 100 representing actual CPU usage in percent.
//----------------------------------------------------------------------------------------------------------------


CHAR cpuusage()
{
	FILETIME               ft_sys_idle;
	FILETIME               ft_sys_kernel;
	FILETIME               ft_sys_user;

	ULARGE_INTEGER         ul_sys_idle;
	ULARGE_INTEGER         ul_sys_kernel;
	ULARGE_INTEGER         ul_sys_user;

	static ULARGE_INTEGER	 ul_sys_idle_old;
	static ULARGE_INTEGER  ul_sys_kernel_old;
	static ULARGE_INTEGER  ul_sys_user_old;

	CHAR  usage = 0;

	// we cannot directly use GetSystemTimes on C language
	/* add this line :: pfnGetSystemTimes */
	s_pfnGetSystemTimes(&ft_sys_idle,    /* System idle time */
		&ft_sys_kernel,  /* system kernel time */
		&ft_sys_user);   /* System user time */

	CopyMemory(&ul_sys_idle  , &ft_sys_idle  , sizeof(FILETIME)); // Could been optimized away...
	CopyMemory(&ul_sys_kernel, &ft_sys_kernel, sizeof(FILETIME)); // Could been optimized away...
	CopyMemory(&ul_sys_user  , &ft_sys_user  , sizeof(FILETIME)); // Could been optimized away...

	usage  =
		(
		(
		(
		(
		(ul_sys_kernel.QuadPart - ul_sys_kernel_old.QuadPart)+
		(ul_sys_user.QuadPart   - ul_sys_user_old.QuadPart)
		)
		-
		(ul_sys_idle.QuadPart-ul_sys_idle_old.QuadPart)
		)
		*
		(100)
		)
		/
		(
		(ul_sys_kernel.QuadPart - ul_sys_kernel_old.QuadPart)+
		(ul_sys_user.QuadPart   - ul_sys_user_old.QuadPart)
		)
		);

	ul_sys_idle_old.QuadPart   = ul_sys_idle.QuadPart;
	ul_sys_user_old.QuadPart   = ul_sys_user.QuadPart;
	ul_sys_kernel_old.QuadPart = ul_sys_kernel.QuadPart;

	return usage;
}
//------------------------------------------------------------------------------------------------------------------
// Entry point code taken from http://en.literateprograms.org/CPU_usage_%28C,_Windows_XP%29
//------------------------------------------------------------------------------------------------------------------

