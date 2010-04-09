#include "stdafx.h"
#include <windows.h>
#include "dgtbrd13.h"

//#pragma argsused
//#include "dgtb105.h"
#include "BoardEvents.h"

//---------------------------------------------------------------------------
// start of dgtbs code

// Only locally relevant defines:
// Discontinued board modes and :
#define INITUPDATE           2
#define INITUPDATESAFE       3
#define UPDATEONLY           4
#define UPDATEONLYSAFE       5
#define UPDATEBOARDONLY      6
#define CONSTANTUPDATEALL    7

// embedded board modes for BoardComSubMode
#define FASTSCAN             10
#define COMPLETESCAN         11
#define SYNCBOARDCLOCK       12
#define SYNCALL              13
#define UNFINISHED           99

// assumed dgt board comm. constants
#define BAUDRATE              CBR_9600
#define BYTESIZE              8
#define PARITY                NOPARITY
#define STOPBITS              ONESTOPBIT
// interval define for time (ms)
#define SAFEINTERVAL          3000
#define DEADINTERVAL          5000
// Length defines for buffers
#define COMPORTBUFLENGTH      9000
#define EEDATALENGTH          9000
// return values for ComGetc
#define COMOKE                0
#define COMEMPTY              1
#define COMOVERFLOW           2

#define DGTCLOCKMESSAGE       0x2b
#define CMDISPLAY             0x03
#define DGTCLOCKMESSAGELENGTH 0x0a
#define DGTCLOCKMESSAGEENDLENGTH 0x02
#define CM2DISP               0x01
#define CM2DBEEP              0x02
#define CM2END                0x0
#define CM2ENDTIME            300
#define CM2DISPTIME           800

static unsigned char ctab[('z'-'-')+3] = {'-','z',  // first, last ascii char in tab
0x40, 0x0,  0x0,                                      // '-'
0x7e, 0x06, 0x5B, 0x4F, 0x66, 0x6D, 0x7D, 0x07, //7
0x7F, 0x7B, 0x0,  0x0,  0x0,  0x0,  0x0,  0x0,  //@
0x0,  0x77, 0x7C, 0x39, 0x5E, 0x79, 0x71, 0x6F,  //2 e o A
0x76, 0x06, 0x38, 0x00, 0x0E, 0x00, 0x15, 0x1D,
0x67, 0x73, 0x05, 0x5B, 0x0F, 0x1C, 0x3E, 0x00,
0x37, 0x33, 0x6D, 0x0,  0x0,  0x0,  0x0,  0x0, //_
0x0,  0x77, 0x7C, 0x39, 0x5E, 0x79, 0x71, 0x6F,  //2 e o A
0x76, 0x06, 0x38, 0x00, 0x0E, 0x00, 0x15, 0x1D,
0x67, 0x73, 0x05, 0x5B, 0x0F, 0x1C, 0x3E, 0x00,
0x37, 0x33, 0x6D // Z
};

// Local variables:
static char ComportBuf[COMPORTBUFLENGTH];
static int  Writepointer=0;
static int  Readpointer=0;
static int  filledcount = 0;
static bool Overflow=false;
static COMMTIMEOUTS timeouts;
static HANDLE       hComm; // voor communcatiepoort
static bool PortIsOpen = false;
static int BoardComMode = SILENT;
static int BoardComSubMode = SILENT;

// Globally relevant:
unsigned char BoardScan[64];
unsigned int versionmain, versionsub=0;
char timestring[10];
char serialnumber[10];
char *diagnostic_message;
char trademark[300];
char DGTTime[16];
unsigned int DGTTimeflags = 0;
unsigned int busadres=0;
unsigned char eedata[EEDATALENGTH];
unsigned int eemovesprogress=0;
unsigned char BoardEventID=0;
void (*pmfunction)();

void ReadComport(void);
unsigned int ComGetc(unsigned char *c);
unsigned int ComPutc(unsigned char c);
void GetCommT(LPCOMMTIMEOUTS to);
unsigned char CheckOnMessages(void);
unsigned char CheckGameEvents(void);
void checkforcemove(void);


//---------------------------------------------------------------------------
 DLLFUNCTION void __stdcall serveDGTBoard(void)
// is called every 100 ms or so by main form timer. Use this to send comms to board
// on regular time base. Processes BoardComMode
{
#define IDLE  0
    static unsigned int lasttime=0;
    static unsigned int nowtime=1;
    static int repeat=0;
    ReadComport();                            // read all pending chars from com
    nowtime=GetTickCount();
    do
    {
      BoardEventID = CheckOnMessages();        // parse data on messages
      if (BoardEventID==UNFINISHED)
      {
        if (( ((nowtime-lasttime)>DEADINTERVAL) || (nowtime<lasttime)) &&(repeat>15))
        {
           // Dead
           BoardEventID = BOARDLOST;
           pmfunction();
           BoardEventID=0;
        }
        else return;
      }
      if (BoardEventID)
      pmfunction();        // do what you want with messages

    }
    while (BoardEventID!= 0);
    if ( ( ((nowtime-lasttime)>SAFEINTERVAL) || (nowtime<lasttime)) &&(repeat>15))
    {
       repeat=0;  // check passed time for re-reading info
       switch (BoardComSubMode)
       {
        case IDLE:
         ComPutc(DGT_SEND_VERSION);           // giving responce for BOARDLOST
         break;
        case INITANDFASTSCAN:
        case FASTSCAN:
         BoardComSubMode=SYNCBOARDCLOCK;
         break;
        case INITANDCOMPLETESCAN:
        case COMPLETESCAN:
         BoardComSubMode=SYNCALL;
         break;
       }
       lasttime=nowtime;
    }

    switch (BoardComSubMode)
    {
     case SILENT:
     {
        ComPutc(DGT_SEND_RESET);
        BoardComSubMode=IDLE;
        break;
     }
     case SYNCBOARDCLOCK:
     {
        if (repeat==5)
        {
           ComPutc(DGT_SEND_CLK);
        }
        if (repeat==9)
        {
           ComPutc(DGT_SEND_UPDATE_NICE);
           ComPutc(DGT_SEND_BRD);
           BoardComSubMode=FASTSCAN;
        }
        break;
     }
     case SYNCALL:
     {
        if (repeat==5)
        {
           ComPutc(DGT_SEND_TRADEMARK);
           ComPutc(DGT_RETURN_SERIALNR);
        }
        if (repeat==9)
        {
           ComPutc(DGT_RETURN_BUSADRES);
           ComPutc(DGT_SEND_CLK);
        }
        if (repeat==13)
        {
           ComPutc(DGT_SEND_UPDATE_NICE);
           ComPutc(DGT_SEND_BRD);
           BoardComSubMode=COMPLETESCAN;
        }
        break;
     }
    } // end switch
    if (repeat++ > 100) repeat=100;
}

//---------------------------------------------------------------------------
 DLLFUNCTION int __stdcall ComInit(char *ComPortName, int BoardComModex,void (*pmf)())
//werkt niet: DLLFUNCTION int  (__stdcall *ComInit)(char *ComPortName, int BoardComMode, void (*pmfunction)())// Starts communication with the board as defined in BoardComMode.

{
  DCB dcb;
  switch (BoardComModex)
  {
    case SILENT:
      BoardComMode=SILENT;
    break;
    case IDLE:
      BoardComMode=IDLE;
    break;
    case INITUPDATE:
    case UPDATEONLY:
    case UPDATEBOARDONLY:
    case INITANDFASTSCAN:
      BoardComMode=INITANDFASTSCAN;
    break;
    case INITUPDATESAFE:
    case UPDATEONLYSAFE:
    case CONSTANTUPDATEALL:
    case INITANDCOMPLETESCAN:
      BoardComMode=INITANDCOMPLETESCAN;
    break;
    default: BoardComMode=INITANDCOMPLETESCAN;
  }
  BoardComSubMode=BoardComMode;
  pmfunction=pmf;                      //initialize pmfunction point to ProcessMessage

  if (PortIsOpen)
  {
		diagnostic_message = "ERROR (ComInit: PortIsOpen)";
		BoardEventID = DMESSAGE;
		pmfunction();
                return 0;
  }
  // initialize arrays for data
  BoardScan[0]=-1;
  versionmain = versionsub=0;
  timestring[0]=NULL;
  serialnumber[0]=NULL;
  trademark[0]=NULL;
  busadres=0;
  eedata[0]=-1;
  eemovesprogress=0;
  // open the port
  hComm = CreateFileA(ComPortName,
      GENERIC_READ | GENERIC_WRITE,
      0, /* exclusive access */
      NULL, /* no security attrs */
      OPEN_EXISTING,
      NULL, //FILE_FLAG_OVERLAPPED,
      NULL
      );
  if (hComm == INVALID_HANDLE_VALUE)
  {
		diagnostic_message = "ERROR (ComInit: COMINITERROR)";
		BoardEventID = DMESSAGE;
		pmfunction();
    return COMINITERROR;
  }

  PortIsOpen = GetCommState(hComm, &dcb);
  if (!PortIsOpen)
  {
    ComClose();
		diagnostic_message = "ERROR (ComInit: COMGETCOMMSTATEERROR)";
		BoardEventID = DMESSAGE;
		pmfunction();
    return COMGETCOMMSTATEERROR;
  }

  dcb.BaudRate = BAUDRATE;
  dcb.ByteSize = BYTESIZE;
  dcb.Parity = PARITY;
  dcb.StopBits = STOPBITS;

  PortIsOpen = SetCommState(hComm, &dcb);
  if (!PortIsOpen)
  {
    ComClose();
		diagnostic_message = "ERROR (ComInit: COMSETCOMMSTATEERROR)";
		BoardEventID = DMESSAGE;
		pmfunction();
    return COMSETCOMMSTATEERROR;
  }

  timeouts.ReadIntervalTimeout = MAXDWORD;
  timeouts.ReadTotalTimeoutMultiplier = 0;
  timeouts.ReadTotalTimeoutConstant = 0;
  timeouts.WriteTotalTimeoutMultiplier = 0;
  timeouts.WriteTotalTimeoutConstant = 0;

  if (!SetCommTimeouts(hComm, &timeouts))
  {
    ComClose();
	diagnostic_message = "ERROR (ComInit: COMSETCOMMTOERROR)";
	BoardEventID = DMESSAGE;
	pmfunction();
    return COMSETCOMMTOERROR;
  }
//  BoardComModeBusy=true;
  return COMINITSUCCES;
}

//---------------------------------------------------------------------------
 DLLFUNCTION bool __stdcall ComClose(void)
{
    PortIsOpen = false;
    return CloseHandle(hComm);
}

//---------------------------------------------------------------------------
void ReadComport(void)
{
unsigned long  nrofbytesread;
unsigned char Onechar;
    if (Overflow) return;
    do
    {
        ReadFile(hComm,&Onechar,1,&nrofbytesread,NULL);
        if (nrofbytesread != 1) // no waiting character
           return;
        ComportBuf[Writepointer++]=Onechar;
        filledcount++;
        if (Writepointer>=COMPORTBUFLENGTH) Writepointer=0;
        if (filledcount >= (COMPORTBUFLENGTH - 5))
        {
          Overflow=true;
          return;
        }
    } while (true);
}

//---------------------------------------------------------------------------
unsigned int ComGetc(unsigned char *c)
{
    if (filledcount<=0) return COMEMPTY;
    if (Overflow) return COMOVERFLOW;
    *c=ComportBuf[Readpointer++];
    if (filledcount-- <0) filledcount=0;
    if (Readpointer>=COMPORTBUFLENGTH) Readpointer=0;
    return COMOKE;
}

//---------------------------------------------------------------------------
unsigned int ComPutc(unsigned char c)
{
    unsigned long nrofbyteswritten;
    WriteFile(hComm,&c,1,&nrofbyteswritten,NULL);
    if (nrofbyteswritten!=1) return 0;
    return 0;
}
 DLLFUNCTION void __stdcall SendToDGTBoard(unsigned char c)
{
//     BoardComMode=SILENT;
     serveDGTBoard();
     ComPutc(c);
     Sleep(3);
     serveDGTBoard();

}
 DLLFUNCTION void __stdcall SendDisplayMessageEnd(void)
{
    serveDGTBoard();
    ComPutc(DGTCLOCKMESSAGE);
    ComPutc(DGTCLOCKMESSAGEENDLENGTH);
    ComPutc(CMDISPLAY);
    ComPutc(CM2END);
    Sleep(CM2ENDTIME);
}

 DLLFUNCTION void __stdcall SendDisplayMessage(char *df, bool beep, bool raw)
{
    int i;
    serveDGTBoard();
    ComPutc(DGTCLOCKMESSAGE);
    ComPutc(DGTCLOCKMESSAGELENGTH);
    ComPutc(CMDISPLAY);
    if (beep) ComPutc(CM2DBEEP);
    else ComPutc(CM2DISP);
    for (i=0;i<DGTCLOCKMESSAGELENGTH-2;i++)
    {
        if (raw) ComPutc((char)(df[i]&0x7f));
        else
        {
          if ((df[i]>=ctab[0])&&(df[i]<=ctab[1]))
            ComPutc(ctab[df[i]-ctab[0]+2]);
          else ComPutc(0);
        }
    }
    Sleep(CM2DISPTIME);  // kan beter: bij aanroep eerst testen of een vorige
    // bericht al afgehandeld is, adhv. system time.
}
/*
code voor announcing move:
Bij aanroep de "string" saven en middels de timer af laten handelen, zodat
geen deadlocks of lange onbeschikbaarheid bestaat.
*/
// indices in sounds-array:
#define S_AVAN 1
#define S_EENVAN 9
#define S_EENNAAR 17
#define S_TAKES 27
#define S_KROKADE 25
#define S_LROKADE 26
#define S_TAKES 27
#define S_OFFERREMISE 28
#define S_ACCEPTREMISE 29
#define S_IRESIGN 30
#define S_MAT 31
#define S_PAT 32

#define S_PAWN 33
#define S_ROOK 34
#define S_KNIGHT 35
#define S_BISHOP 36
#define S_QUEEN 37
#define S_KING 38
#define S_ENPASSANT 39
#define S_OKE 40
#define S_CLICK 41
#define MAXSOUNDS 41



//---------------------------------------------------------------------------
void GetCommT(LPCOMMTIMEOUTS to)
{
    GetCommTimeouts(hComm,to);
}

//---------------------------------------------------------------------------
unsigned char CheckOnMessages(void)
// leest uit de compoort, onder besturing van een statemachine, tot er geen karakters meer zijn,
// of een message volledig binnengekomen is.
// Geen karakters meer, dan return, met behoud van alle toestanden.
// Message volledig binnengekomen, dan wordt deze verwerkt, en vervolgens wordt het
// proces van karakters lezen vervolgd.
{
// state values:
#define S_IDLE 0
#define S_ID   1
#define S_LH   2
#define S_LL   3

 static int state=S_IDLE;
 static bool message_found=false;
 static bool boardpositionchanged, clockchanged, versionchanged, trademarkchanged = false;
 static bool serialnumberchanged, busadreschanged, eedatachanged, deadtimepassed = false;
 static bool boardlostreported=false;
 static int messagelength, messagelength_h, messagelength_l,datacounter=0;
 static unsigned char messageID;
 static unsigned char busadresh,busadresl;
 static int updatefieldnr;

 static unsigned char charbuffer;
 static unsigned int comgetcvalue;
 static unsigned int lasttime=-1;
 static unsigned int nowtime=0;
 static unsigned int lastboardchangetime = -1;
 static bool waitforstable = false;

 // toevoegen: als geen boardchange in deze aanroep, en
// boardposchanged en nowtime-lastchangetime>x
// dan return BOADPOSITIONSTABLE
 do
 {
   comgetcvalue = ComGetc(&charbuffer);
   nowtime=GetTickCount();
   if (!PortIsOpen) lasttime=nowtime;
   if (lasttime>nowtime)  // in case of lime overflow
   {
      lasttime=nowtime;
      lastboardchangetime = nowtime;
   }
   if (((nowtime-lasttime)>DEADINTERVAL)&&(BoardComMode!=IDLE))
       deadtimepassed=true;
       else deadtimepassed=false;
   if (comgetcvalue)  // Geen kar. ontvangen of overflow
   {
      if ((deadtimepassed)&&!(boardlostreported)&&PortIsOpen)
      {
         boardlostreported=true;
         state=S_IDLE;
         return BOARDLOST;
      }
      if (((nowtime-lastboardchangetime)>STABLETIME)&&(waitforstable))
      {
         waitforstable=false;
// for slow responce:
//         checkforcemove();
//         BoardEventID=CheckGameEvents(); // moved to GameEvents.cpp
         if (BoardEventID!=NULL)
           pmfunction();
         // hier misschien de aanvullende events als rotate en result??
         return STABLEBOARDPOSITION;
      }
      if (state!=S_IDLE)
        return  UNFINISHED;
      else return 0;// normal void end
   }
   if (charbuffer>=128)
   {
      messageID=(unsigned char)(charbuffer-128);
      state = S_IDLE;
   }
   switch (state)
   {
      case S_IDLE:
        if (charbuffer>=128)
        {
           messageID=(unsigned char)(charbuffer-128);
           state = S_ID;
        }
        break;
      case S_ID:
        if (charbuffer > 62)
        {
           state = S_IDLE; // to prevent overflow.
           break;
        }
        messagelength_h = charbuffer;
        state = S_LH;
        break;
      case S_LH:
        messagelength_l = charbuffer;
        state = S_LL;
        messagelength = (int)messagelength_l + (128 * (int)messagelength_h);
        datacounter = 0;
        break;
      case S_LL:  // read data
        switch (messageID)
        {
           case (DGT_BOARD_DUMP):
           {
             if (charbuffer!=BoardScan[datacounter])
             {
                boardpositionchanged=true;
             }
             BoardScan[datacounter] = charbuffer;
             break;
           }
           case (DGT_FIELD_UPDATE):
           {
             if (datacounter==0) updatefieldnr = charbuffer;
             if (datacounter==1)
             {
               if (updatefieldnr >= 64) break;
               if (charbuffer!=BoardScan[updatefieldnr])
               {
                   boardpositionchanged=true;
               }

               BoardScan[updatefieldnr]=charbuffer;
             }
             break;
           }
           case (DGT_BWTIME):
           {
             if (charbuffer!=timestring[datacounter])
             {
                clockchanged=true;
             }
             timestring[datacounter] = charbuffer;
             break;
           }
           case (DGT_VERSION):
           {
             if (datacounter==0)
             {
               if ((unsigned int) charbuffer != versionmain)
               {
                  versionchanged=true;
               }
               versionmain = charbuffer;
             }
             if (datacounter==1)
             {
               if ((unsigned int) charbuffer != versionsub)
               {
                  versionchanged=true;
               }
               versionsub = charbuffer;
             }
             break;
           }
           case (DGT_SERIALNR):
           {
             if (datacounter < 5)
             {
               if (charbuffer!=serialnumber[datacounter]) serialnumberchanged=true;
               serialnumber[datacounter] = charbuffer;
             }
             if (datacounter==4) serialnumber[5]= '\0';
             break;
           }
           case (DGT_TRADEMARK):
           {
             if (charbuffer!=trademark[datacounter])
             {
                trademarkchanged=true;
             }
             trademark[datacounter] = charbuffer;
             break;
           }
           case (DGT_BUSADRES):
           {
             if (datacounter==0)
             {
               if (charbuffer!=busadresh)
               {
                  busadreschanged=true;
               }
               busadresh = charbuffer;
             }
             if (datacounter==1)
             {
               if (charbuffer!=busadresl)
               {
                  busadreschanged=true;
               }
               busadresl=charbuffer;
               busadres = (int)busadresl + (128 * (int)busadresh);
             }
             break;
           }
           case (DGT_EE_MOVES):
           {
             eedata[datacounter] = charbuffer;
             eedatachanged=true;
             eemovesprogress=messagelength-datacounter-3;
             lasttime=nowtime;                        // to prevent BOARDLOST
             break;
           }
        }  // einde switch (messageID)
        datacounter++;
        if (datacounter>=messagelength-3)
        {
           deadtimepassed=false;                      // full message arrived!
           boardlostreported=false;
           message_found=true;
           state = S_IDLE;
           lasttime=nowtime;
           if (busadreschanged)
           {
             busadreschanged=false;
             return BUSADRESCHANGED;
           }
           if (boardpositionchanged)
           {
             lastboardchangetime = nowtime;
             boardpositionchanged=false;
             waitforstable = true;
             BoardEventID=BOARDPOSITIONCHANGED;
             pmfunction();
// check on dynamic placement of kings:
// recognise:
// lifting and placement of the kings in sequence, to indicate
//  new position, color to play
// lifting and replacing one king, to force MoveNow black or white
// for quick responce:
//             checkforcemove();

             return NULL;
           }
           if (clockchanged)
           {
             clockchanged=false;
             // Rearrange clock times to simple string Time and
             // unsigned int Timeflags

             if ((timestring[6]&0x20)!=0)
             {
                DGTTimeflags = DGTTIMENOCLOCK;
                strcpy(DGTTime,"-:--:-- -:--:--");
             }
             else
             {
                DGTTimeflags =
                ( (((int) timestring[0]&0x70)>>4)
                + (((int) timestring[3]&0x70)>>1)
                + (((int) timestring[6]&0x3f)<<8));
        	sprintf(DGTTime,"%1d:%1d%1d:%1d%1d %1d:%1d%1d:%1d%1d",
				((timestring[3]>>0)&0xf)%10,	//uur
				((timestring[4]>>4)&0x7)%6,   //10min
				((timestring[4]>>0)&0xf)%10,  //min
				((timestring[5]>>4)&0x7)%6,   //10sec
				((timestring[5]>>0)&0xf)%10,   //sec
				((timestring[0]>>0)&0xf)%10,	//uur
				((timestring[1]>>4)&0x7)%6,   //10min
				((timestring[1]>>0)&0xf)%10,  //min
				((timestring[2]>>4)&0x7)%6,   //10sec
				((timestring[2]>>0)&0xf)%10); //sec
             }
             return CLOCKCHANGED;       // general change...
           }
           if (versionchanged)
           {
             versionchanged=false;
             return VERSIONCHANGED;
           }
           if (trademarkchanged)
           {
             trademarkchanged=false;
             return TRADEMARKCHANGED;
           }
           if (serialnumberchanged)
           {
              serialnumberchanged=false;
             return SERIALNRCHANGED;
           }
           if (eedatachanged)
           {
             eedatachanged=false;
             eemovesprogress=0;
             return EEMOVESRECEIVED;
           }
        } // einde if datacounter > length-3
        break;

   } // einde switch (state)
 } // einde do block
 while (true);
}


//---------------------------------------------------------------------------
// eof

