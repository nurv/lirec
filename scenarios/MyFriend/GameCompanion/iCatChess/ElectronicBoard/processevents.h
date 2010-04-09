#include <math.h>
#include <stdio.h>
#include "dgtb105.h"
//#include "dgtbrd13.h"
#include "boardEvents.h"

#define ROW(x)			(x >> 3)
#define COL(x)			(x & 7)

unsigned char * retBoard();
int retStableBoard();
char * diff(unsigned char *buf);
char * buffunc();
unsigned char * initialpos(void);
void cpbuffers(unsigned char * b1,unsigned char * b2,int l);
void printpos(unsigned char *buf); 
void ProcessGameEvents(void);
void ProcessScanEvents(void);
void clearpos(void);
void EndCom(void);
int retNewMove();
void resetNewMove();
char* retLtlauxbuff();

extern char Piececode[16];