//---------------------------------------------------------------------------

#ifndef BoardEventsH
#define BoardEventsH
//---------------------------------------------------------------------------
#endif
#include  "dgtb105.h"

#define RANDOMFISCHERSTART           27

void FindBoardEvents(unsigned char *BoardScan, void (*pmfunction)());
void FindClockEvents(unsigned char *DGTTime, int DGTTimeflags, void (*pmfunction)());
void NormalizeBoard(unsigned char *NormBrd, unsigned char *BoardScan);
unsigned int CheckBoardEvents(unsigned char *BoardScan);
unsigned int CheckForceMove(unsigned char *NormBoard);

extern unsigned int GameEventID;
extern unsigned char GameBoard[64];
extern char DGTTime[16];
extern unsigned int DGTTimeflags;
extern bool rotated;


extern char DGTTimeBlack[];
extern int DGTTimeBlackFlags;

#define DGTTIMECHANGED 0x01
#define DGTTIMESTOPPED 0x02
#define DGTTIMERUNNINGX 0x04

extern char DGTTimeWhite[10];
extern int DGTTimeWhiteFlags;


