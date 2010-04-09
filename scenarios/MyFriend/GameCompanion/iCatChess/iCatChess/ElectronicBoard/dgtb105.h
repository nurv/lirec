#ifndef dgtbH
#define dgtbH

//---------------------------------------------------------------------------
// Header file for single board DGT Electronic Chessboard communications.
// This header file is used for DLL generation, DLL usage and source compilation.
//
// See sample codes at the end of this file for the different usages:
// - when using the DLL: how to get the function entries.
// - when linking the functions at link time.
// Version 1.0 jan, 2001
// Author: Ben Bulsink
// version 101: added communication messages output
// version 102: bug fixed: queen/king piececode swapped
// version 103: BoardEvents stripped and placed in GameEvents.cpp
// Version 104: Serial Number reading adapted: fixed to 5 positions
// added: clock splitting in black/white transferred to GameEvents.cpp: not yet finished
//---------------------------------------------------------------------------

// This same header file is used to export functions if a DLL is built.
// BC++Builder defines __DLL__ automatically so DLLFUNCTION is expanded to
// export the functions from the DLL
#ifdef __DLL__
#define DLLFUNCTION extern "C" __declspec(dllexport)
#else
#define DLLFUNCTION
#endif

// This same header file is used for DLL compilation and for prototyping for
// applications that use the DLL. Applications must define DGTDLLCALLER, to
// get the correct prototypes.
// If the dgtb100.cpp source is compiled and linked into the application,
// and DGTDLLCALLER nor __DLL__ are defined, the correct declaration is
// made for the extern functions
// NOTE: The variables exported from the DLL must be accessed by pointers,
// i.e. the variable unsigned char BoardEventID is prototyped as
// unsigned char *BoardEventID if accessed as an exported DLL variable, and
// must be referenced as a pointer.

#ifdef DGTDLLCALLER
#define DLLREDIRECT *           // redirection when using DLL functions
#define DLLFUNCTION
#else
#define DLLREDIRECT
#define DLLFUNCTION extern      // functions are linked at link time
#endif

 DLLFUNCTION int (__stdcall DLLREDIRECT ComInit)(char *ComPortName, int BoardComMode, void (*pmfunction)());// initialises the comport, when not open.
// ComPortName may be COM1, COM2, COM3 or COM4
// return values for ComInit:
#define COMINITSUCCES         0
#define COMINITERROR          1
#define COMGETCOMMSTATEERROR  2
#define COMSETCOMMSTATEERROR  3
#define COMSETCOMMTOERROR     4

// BoardComMode values used at calling ComInit:

// SILENT: At initialising a port: Just open the port, and set the board
// to reset mode

// INITANDFASTSCAN: At initialising a port: Opens the port, requests for all board
// information, and afterwards switches to UPDATEONLY.

// INITUPDATESAFE: At initialising a port: Opens the port, requests for all board
// information, and afterwards switches to UPDATEONLYSAFE.

// UPDATEONLY: Sends an DGT_UPDATE_NICE to the board every 5 seconds

// UPDATEONLYSAFE: Sends an DGT_UPDATE_NICE to the board every 5 seconds,
// and asks for board and clock dumps every 5 seconds.

// UPDATEBOARDONLY: Sends an DGT_UPDATE_BRD and DGT_SEND_BRD to the board
// every 3 seconds.

// CONSTANTUPDATEALL: At initialising a port: Opens the port, requests for
// all board info every 3 seconds, sends an Update_Nice.

#define SILENT               1
#define INITANDFASTSCAN      8  // Starts communcation and scans only the clock/pieces data
#define INITANDCOMPLETESCAN  9  // Starts communcation and scans all board data (i.e. trademark)


// pmfunction is the function which has to handle the found events.
//---------------------------------------------------------------------------
 DLLFUNCTION bool  (__stdcall DLLREDIRECT ComClose)(void);
// closes the current opened comport

 DLLFUNCTION void (__stdcall DLLREDIRECT serveDGTBoard)(void);
// embedded function: called every 0,1 to 0,2 seconds it processes the incoming
// data, sends the commands, and calls the pmfunction to have events processed

 DLLFUNCTION void (__stdcall DLLREDIRECT SendToDGTBoard)(unsigned char c);
// sends a byte to the opened com port. Before and after, incoming data is read
// and events reported.

 DLLFUNCTION void (__stdcall DLLREDIRECT SendDisplayMessage)(char *df, bool beep, bool raw);
// NOTE: Not supported yet by our clocks DGT TopMatch, DGT Fide or DGT Plus!
// sends the string df to the clock display,
// if beep, giving a short beep
// if raw, using df bits as segment info, else reading as ASCII
// The function sleeps for about 0.8 seconds to have the data
// transferred to the clock

 int __stdcall _DGTDLL_DisplayClockMessage (char *message, int time);

 DLLFUNCTION void (__stdcall DLLREDIRECT SendDisplayMessageEnd)(void);
// NOTE: Not supported yet by our clocks DGT TopMatch, DGT Fide or DGT Plus!
// ends eventually displayed message and restores time display on clock
// The function sleeps for about 0.3 seconds to have the data
// transferred to the clock

// DLLFUNCTION int (__stdcall DLLREDIRECT AnnounceMove)(char *move, char piececode, unsigned int special, bool status);
// not yet supported, Full set of .wav files expected summer 2000
// Announces the move + special messages over the system speakers.
// move points to a 4-character string i.e. "e2e4" or to a NULL string.
// piececode ranges from PAWN to KING, and is used for long notation
// or promotion piece type
// special is an addition of auxiliary messages, listed below.
// AnnounceMove takes care of the right sequence of messages.
// If special has a Rokade bit set, the move string is neglected.
// if status==true the function returns 1 if previous sound is still
// ongoing, and 0 if sounds are finished.

// bit codes voor specials:
#define PAWN 1
#define ROOK 2
#define KNIGHT 3
#define BISHOP 4
#define KING 5
#define QUEEN 6

#define TAKES 1
#define KROKADE 2
#define LROKADE 4
#define OFFERREMISE 8
#define ACCEPTREMISE 16
#define IRESIGN 32
#define MAT 64
#define PAT 128
#define PROMOTION 256
#define ENPASSANT 512
#define CONFIRMOKE 	  1024
#define CONFIRMCLICK 	  2048

// subdirectory names for announcements (not yet supported)

#ifdef DGTDLLCALLER
DLLFUNCTION unsigned char *BoardEventID;
#else
DLLFUNCTION unsigned char BoardEventID;
#endif
// The board communication function serveDGTBoard will call your message
// processing function, where the variable BoardEventID is set to the following
// event values:

#define BOARDPOSITIONCHANGED        1 // if a piece disappears or appears
#define CLOCKCHANGED                2 // on any change in clock data
#define BOARDLOST                   3 // if the communication drops
#define BOARDFOUND                  4 // not used;
#define VERSIONCHANGED              5 // if the version comes available or changes
#define TRADEMARKCHANGED            6 // if the trade mark message comes available or changes
#define SERIALNRCHANGED             7 // if the serial number comes available or changes
#define BUSADRESCHANGED             8 // if the bus address comes available or changes
#define EEMOVESRECEIVED             9 // if all 8KB - 256 EEPROM data is received
#define STABLEBOARDPOSITION         10 // STABLETIME ms after the last BOARDPOSITIONCHANGED event
#define ROTATIONCHANGED             11 // if the playing direction changed
#define RESULTWHITEWINS             12 // if both kings are on the d5 and e4 squares
#define RESULTBLACKWINS             13 // if both kings are on the d4 and e5 squares
#define RESULTDRAW                  14 // if both kings are on the center 4 squares, each king on a different color
#define STARTINGPOSITION            15 // if the traditional chess starting position is found,
#define SETUPROTATED                16 // if the playing direction is rotated during setup (no kings on the board)
#define NEWPOSITIONWHITETOMOVE       17 // if the kings were placed on the board, the white at last
#define NEWPOSITIONBLACKTOMOVE       18 // if the kings were placed on the board, the black at last
#define WHITEMOVENOW                19 // if the white king was lifted for a moment, and placed back
#define BLACKMOVENOW                20 // if the black king was lifted for a moment, and placed back
#define SETUPENTERED                21 // if the kings are removed from the board
#define DMESSAGE                    22 // diagnostic message
#define BLACKCLOCKTIMECHANGED       23 // if the clock time for black changed
#define WHITECLOCKTIMECHANGED       24 // if the clock time for white changed
#define BLACKSTOPPEDCLOCK           25 // if black stopped his/her clock
#define WHITESTOPPEDCLOCK           26 // if white stopped his/her clock

// The event STABLEBOARDPOSITION is generated the below ms time after the last
// board change event (which generated a BOARDPOSITIONCHANGED event).
// The below define value can be changed before compiling the COMM.CPP unit
#define STABLETIME                  900  // original 700

//---------------------------------------------------------------------------
// The data structures where the board info can be read:
#ifdef DGTDLLCALLER
 DLLFUNCTION unsigned char *BoardScan;             // contains piece code as in dgtbrd13.h
#else
 DLLFUNCTION unsigned char BoardScan[64];             // contains piece code as in dgtbrd13.h
#endif
#ifdef DGTDLLCALLER
 DLLFUNCTION unsigned int *versionmain, *versionsub;    // i.e. 1 and 4 for vs. 1.4
#else
 DLLFUNCTION unsigned int versionmain, versionsub;    // i.e. 1 and 4 for vs. 1.4
#endif
#ifdef DGTDLLCALLER
 DLLFUNCTION char *timestring;                     // contains data as defined in dgtbrd13.h
#else
 DLLFUNCTION char timestring[10];                     // contains data as defined in dgtbrd13.h
#endif
#ifdef DGTDLLCALLER
 DLLFUNCTION char *DGTTime;                        // contains formatted timedata:
#else
 DLLFUNCTION char DGTTime[16];                        // contains formatted timedata:
#endif

#ifdef DGTDLLCALLER
 DLLFUNCTION unsigned int *DGTTimeflags;               // contains DGT Timer flags according to
#else
 DLLFUNCTION unsigned int DGTTimeflags;               // contains DGT Timer flags according to
#endif
// these defines:
// Notice that these low level info is upgraded by BoardEvents.cpp and translated
// to board events.
#define DGTTIMEBLOCKFLAGLEFT       0x8   // if the left (front view) clock flag falls and the clock blocks
#define DGTTIMEBLOCKFLAGRIGHT      0x1   // if the right clock flag falls and the clock blocks
#define DGTTIMETIMEPERMOVELEFT     0x10  // if th Added time per move indicator is on for the left clock
#define DGTTIMETIMEPERMOVERIGHT    0x2   // if th Added time per move indicator is on for the right clock
#define DGTTIMEFLAGLEFT            0x20  // if the left clock flag indicator is on
#define DGTTIMEFLAGRIGHT           0x4   // if the right clock flag indicator is on
#define DGTTIMERUNNING             0x100 // if the clock is not stopped
#define DGTTIMETUMBLERLEFT         0x200 // if the left side of the tumbler is down
#define DGTTIMELOWBATTERY          0x400 // if the low bat is on
#define DGTTIMERIGHTTURN           0x800 // if the right player clock is running
#define DGTTIMELEFTTURN            0x1000 // if the left player clock is running
#define DGTTIMENOCLOCK             0x2000 // if no clock is connected to the board


#ifdef DGTDLLCALLER
DLLFUNCTION char *diagnostic_message;
#else
DLLFUNCTION char *diagnostic_message;
#endif

#ifdef DGTDLLCALLER
 DLLFUNCTION char *serialnumber;                   // contains i.e 01240
#else
 DLLFUNCTION char serialnumber[10];                   // contains i.e 01240
#endif

#ifdef DGTDLLCALLER
 DLLFUNCTION char *trademark;                     // as defined in dgtbrd13.h
#else
 DLLFUNCTION char trademark[300];                     // as defined in dgtbrd13.h
#endif

#ifdef DGTDLLCALLER
 DLLFUNCTION unsigned int *busadres;                   // binary serialnumber
#else
 DLLFUNCTION unsigned int busadres;                   // binary serialnumber
#endif

#ifdef DGTDLLCALLER
 DLLFUNCTION unsigned char *eedata;                  // length 9000 currently
                                                      // contens as defined in dgtbrd13.h
#else
 DLLFUNCTION unsigned char eedata[9000];                  // length 9000 currently
                                                      // contens as defined in dgtbrd13.h
#endif

#ifdef DGTDLLCALLER
 DLLFUNCTION unsigned int *eemovesprogress;            // contains nr of bytes still to read
#else
 DLLFUNCTION unsigned int eemovesprogress;            // contains nr of bytes still to read
#endif



//---------------------------------------------------------------------------

/* The following code will deliver proper communication with the board, where
the code is linked in the application.
Have a timer declared in the program, i.e. named TForm1::ComportTimerTimer, with
an interval of 100-200 ms. pmfunction is initialized as the address of the event
handling program, which you have to write yourself. This program (i.e.
ProcessMessage() is called by serveDGTBoard when an event occured.

#include "dgtbrd13.h"
#include "dgtb100.h"

void __fastcall TForm1::ComportTimerTimer(TObject *Sender)
// Standard procedure to read the comport and have the messages processed
// by ProcessBoardEvents()
{
   serveDGTBoard();               //calls ProcessMessage()
                                  //until no messages are pending
}
// and have a function to process BoardEvents, known by BoardEventID, with a
// sample code as this:
void ProcessBoardEvents(void)
{
     switch (BoardEventID)
     {
       case (BOARDPOSITIONCHANGED):
       {
        printpos(NormBoard);
        break;
       }
       case (CLOCKCHANGED)
       {
        printclock(DGTTime);
        break;
       }
       // etc.
     }
}

// Start communication over port ComPortName:
void StartCom(void)
{
    ComInit(ComPortName,INITUPDATESAFE,ProcessBoardEvents);
                                 // reads all info
}

void EndCom(void)
{
     ComClose();                        // closes comport
}


// for retrieving EEPROM data:
SendToDGTBoard(DGT_SEND_EE_MOVES);      // which results in a call of ProcessMessages,
                                        // with BoardEventID = EEMOVESRECEIVED
*/

//---------------------------------------------------------------------------
/* The following code will deliver proper communication with the board, when the
application uses the dll.
Have a timer declared in the program, i.e. named TForm1::ComportTimerTimer, with
an interval of 100-200 ms. pmfunction is initialized as the address of the event
handling program, which you have to write yourself. This program (i.e.
ProcessMessage() is called by serveDGTBoard when an event occured.

#include "dgtbrd13.h"
#include "dgtb100.h"

void __fastcall TForm1::ComportTimerTimer(TObject *Sender)
// Standard procedure to read the comport and have the messages processed
// by ProcessBoardEvents()
{
   serveDGTBoard();               //calls ProcessMessage()
                                  //until no messages are pending
}
// and have a function to process BoardEvents, known by BoardEventID, with a
// sample code as this:
void ProcessBoardEvents(void)
{
     switch (*BoardEventID)
     {
       case (BOARDPOSITIONCHANGED):
       {
        printpos(NormBoard);
        break;
       }
       case (CLOCKCHANGED)
       {
        printclock(DGTTime);
        break;
       }
       // etc.
     }
}

// Start communication over port ComPortName:
void StartCom(void)
{
    if (LoadDLLFunctions()!=NULL) return;
    ComInit(ComPortName,INITUPDATESAFE,ProcessBoardEvents);
                                 // reads all info
}

void EndCom(void)
{
     ComClose();                        // closes comport
}


// for retrieving EEPROM data:
SendToDGTBoard(DGT_SEND_EE_MOVES);      // which results in a call of ProcessMessages,
                                        // with BoardEventID = EEMOVESRECEIVED
// To get the entries from the DLL:
HINSTANCE lib;
int LoadDLLFunctions()
{
   lib=LoadLibrary("dgtb101.dll");
   ComInit = (int(__stdcall*)(char *ComPortName, int BoardComMode, void (*pmfunction)())) GetProcAddress(lib, "ComInit");
   ComClose=(bool (__stdcall*)(void)) GetProcAddress(lib, "ComClose");
   serveDGTBoard=(void (__stdcall*)(void)) GetProcAddress(lib, "serveDGTBoard");
   SendToDGTBoard=(void (__stdcall*)(unsigned char c)) GetProcAddress(lib, "SendToDGTBoard");
   SendDisplayMessage=(void (__stdcall*)(char *df, bool beep, bool raw)) GetProcAddress(lib, "SendDisplayMessage");
   SendDisplayMessageEnd=(void (__stdcall*)(void)) GetProcAddress(lib, "SendDisplayMessageEnd");
   AnnounceMove=(int (__stdcall*)(char *move, char piececode, unsigned int special, bool status)) GetProcAddress(lib, "AnnounceMove");
   BoardEventID= (unsigned char *)GetProcAddress(lib, "_BoardEventID");
   BoardScan= (unsigned char *)GetProcAddress(lib, "_BoardScan");
   NormBoard= (unsigned char *)GetProcAddress(lib, "_NormBoard");
   Rotated= (bool *)GetProcAddress(lib, "_Rotated");
   versionmain= (unsigned int *)GetProcAddress(lib, "_versionmain");
   versionsub= (unsigned int *)GetProcAddress(lib, "_versionsub");
   timestring= (char *)GetProcAddress(lib, "_timestring");
   DGTTime= (char *)GetProcAddress(lib, "_DGTTime");
   DGTTimeflags= (unsigned int *)GetProcAddress(lib, "_DGTTimeflags");
   serialnumber= (char *)GetProcAddress(lib, "_serialnumber");
   trademark= (char *)GetProcAddress(lib, "_trademark");
   busadres= (unsigned int *)GetProcAddress(lib, "_busadres");
   eedata= (unsigned char *)GetProcAddress(lib, "_eedata");
   eemovesprogress= (unsigned int *)GetProcAddress(lib, "_eemovesprogress");
        if (
        (ComInit ==NULL)||
        (ComClose==NULL)||
        (serveDGTBoard==NULL)||
        (SendToDGTBoard==NULL)||
        (SendDisplayMessage==NULL)||
        (SendDisplayMessageEnd==NULL)||
        (AnnounceMove==NULL)||
        (BoardEventID==NULL)||
        (BoardScan==NULL)||
        (NormBoard==NULL)||
        (Rotated==NULL)||
        (versionmain==NULL)||
        (versionsub==NULL)||
        (timestring==NULL)||
        (DGTTime==NULL)||
        (DGTTimeflags==NULL)||
        (serialnumber==NULL)||
        (trademark==NULL)||
        (busadres==NULL)||
        (eedata==NULL)||
        (eemovesprogress==NULL))
        {
            MessageBox(NULL, "Failure locating procedures from dgtb100.dll.", "Loading DLL",
                MB_OK);
          return -1 ;
        }
        else return 0;
}


*/

#endif





