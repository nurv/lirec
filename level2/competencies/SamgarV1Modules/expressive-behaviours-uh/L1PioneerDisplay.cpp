/*
	PROGRAM:	Level 1 - Hardware Level of Pioneer's Display Module (communicating with SSC-32 card)  
	Author :	K . L. Koay
	Date   :    05 May 2010

*/
#include <Windows.h>
#include <iostream>
#include <cstdlib>
#include <SamgarMainClass.h>

using namespace std;
//using namespace yarp;

int main(int argc, char* argv[])
{
	Network yarp;		 //name					//Category	//subcategory	
	SamgarModule Display("PioneerBaseLights",	"Display",	"Lights", SamgarModule::run);
	Display.AddPortS("L1BIn");
	Bottle BehaviourIn;
	char command[100];

	printf("\n argc = %d, argv[1] = %s", argc, argv[1]);
//Setting up serial port in Linux   ----------------------
//	int fd = open("/dev/ttyUSB2",O_RDWR|O_NDELAY); //opening a port to the lights and arms
//	if (fd==-1)	{
//		printf("\nfd = Error opening USB2\n");
//	}
//	tcgetattr(fd,&options);
//	cfsetispeed(&options,B115200);
//	cfsetospeed(&options,B115200);
//	options.c_cflag &= ~PARENB; //no parity bit
//	options.c_cflag &= ~CSTOPB; //one stop bit
//	options.c_cflag &= ~CSIZE; //clear out the current data size setting
//	options.c_cflag |= CS8; //8-bits per work
//	options.c_cc[VMIN]=0;
//	options.c_cc[VTIME]=1;
//	options.c_cflag |= (CLOCAL | CREAD);  
  /* 
  //turn off echo, canonical mode, extended processing, signals 
  options.c_lflag &= ~(ECHO | ICANON | IEXTEN | ISIG);
  //turn off break sig, cr->nl, parity off, 8 bit strip, flow control 
  options.c_iflag &= ~(BRKINT | ICRNL | INPCK | ISTRIP | IXON);
  //clear size, turn off parity bit 
  options.c_cflag &= ~(CSIZE | PARENB);
  //set size to 8 bits 
  options.c_cflag |= CS8;
  //turn output processing off 
  options.c_oflag &= ~(OPOST);
  //Set time and bytes to read at once 
  options.c_cc[VTIME] = 0;
  options.c_cc[VMIN] = 0;  */
//	if(tcsetattr(fd, TCSAFLUSH, &options)!=0){
//		printf("\n Error applying setting USB2 \n");
//	}
//=====================================================

HANDLE fileHandle;
/*fileHandle = (/*argv[1] "COM4", GENERIC_READ | GENERIC_WRITE, 0, OPEN_EXISTING, 0, 0);
if (fileHandle == INVALID_HANDLE_VALUE){
	printf("\n Error open com port");
}
*/

DCB dcb;
//FillMemory(&dcb, sizeof(dcb),0);
fileHandle = CreateFile(/*argv[1]*/"COM8", GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING, 0, NULL);
if (fileHandle == INVALID_HANDLE_VALUE){
	printf("\n Error open com port");
}
//SetupComm(fileHandle, 128, 128);

GetCommState(fileHandle, &dcb);
dcb.DCBlength = sizeof(dcb);
dcb.BaudRate = 115200;
dcb.ByteSize = 8;
dcb.Parity = NOPARITY;
dcb.StopBits = ONESTOPBIT;
dcb.fBinary = TRUE;
dcb.fParity = TRUE;

/*SetCommState(fileHandle, &dcb);
if(!BuildCommDCB("115200,n,8,1",&dcb))
{	printf("\n Error setting port");
	return false;
}*/

if(!SetCommState(fileHandle, &dcb)){
	printf("\n Error applying setting to port\n");
	return false;
}
printf("\n Applied Setting to port %s",argv[1]);

if(!SetupComm(fileHandle,1024,1024)){
	printf("\n Error setting port buffer");
	return false;
}
printf("\n Comm port buffer set to 1024");

COMMTIMEOUTS cmt;
cmt.ReadIntervalTimeout =1000;
cmt.ReadTotalTimeoutMultiplier = 1000;
cmt.ReadTotalTimeoutConstant = 1000;
cmt.WriteTotalTimeoutMultiplier =1000;
cmt.WriteTotalTimeoutConstant = 1000;

if(!SetCommTimeouts(fileHandle, &cmt)){
	printf("error setting timeouts for fileHandle");
}

DWORD read = -1;
//ReadFile(fileHandle, data, size, &read, NULL)
DWORD write = -1;
//WriteFile(fileHandle, data, size, &write,NULL)

printf("\nReady.... ");

int Front, Left, Back, Right;

while (1){
	if (Display.GetBottleData("L1BIn", &BehaviourIn, SamgarModule::interupt)) {
		printf("got something in\n");
		Front	= BehaviourIn.get(0).asInt()*(2500/100);
		Left	= BehaviourIn.get(1).asInt()*(2500/100);
		Back	= BehaviourIn.get(2).asInt()*(2500/100);
		Right	= BehaviourIn.get(3).asInt()*(2500/100);
		sprintf(command,"#12 P%d #13 P%d #14 P%d #15 P%d T100\r", Front,Left,Back,Right);
		/*
		sprintf(command,"#12 P%d #13 P%d #14 P%d #15 P%d T100\r", 
			(int)(BehaviourIn.get(0).asInt()*(2500/100)),
			(int)(BehaviourIn.get(1).asInt()*(2500/100)),
			(int)(BehaviourIn.get(2).asInt()*(2500/100)), 
			(int)(BehaviourIn.get(3).asInt()*(2500/100)) );
		*/
		printf("Command is %s\n",command);
		WriteFile(fileHandle, command, sizeof(command), &write, NULL);
		BehaviourIn.clear();
		//printf("\n %s ",command);
		sprintf(command,"Q\r");
		while (WriteFile(fileHandle, command, sizeof(command), &write, NULL)=='.'){}
	}
	Display.SucceedFail(true,0);
  }   
}