#include "stdafx.h"
#include <String>
#include "processevents.h"
using namespace System;

unsigned char auxbuff [64];
unsigned char retbuff [64];
int newmove=0;
int specialmove=0;
int staboard=0;
char lastbuff [5]={'S','S','S','S','S'};
char ltlauxbuff [5]={'S','S','S','S','S'};
char lastevent [3]={'S','S','S'};
char Piececode[16]={'S','P','R','N','B','K','Q','p','r','n','b','k','q','z','z','z'};

unsigned char * retBoard(){
	int i=0;
	return auxbuff;
}

int retStableBoard(){
	if (staboard >1)
		return 1;
	return 0;
}

int retNewMove(){
return newmove;
}

void resetNewMove(){
newmove=0;
}

char* retLtlauxbuff(){
return lastbuff;
}

void cpbuffers(char * b1, char * b2,int l)
{
	int i;
	for (i=0;i<l;i++){
		b1[i]=b2[i];
	}
}

void clearpos(void)
{
  unsigned char empty[64]=
  {0,0,0,0,0,0,0,0,
   0,0,0,0,0,0,0,0,
   0,0,0,0,0,0,0,0,
   0,0,0,0,0,0,0,0,
   0,0,0,0,0,0,0,0,
   0,0,0,0,0,0,0,0,
   0,0,0,0,0,0,0,0,
   0,0,0,0,0,0,0,0};
  printpos(empty);
}

unsigned char * initialpos(void)
{
  unsigned char initial[64]=
  {8,9,10,12,11,10,9,8,
   7,7,7,7,7,7,7,7,
   0,0,0,0,0,0,0,0,
   0,0,0,0,0,0,0,0,
   0,0,0,0,0,0,0,0,
   0,0,0,0,0,0,0,0,
   1,1,1,1,1,1,1,1,
   2,3,4,6,5,4,3,2};
  cpbuffers(auxbuff,initial, 64);
  return initial;
}

			/*if (ltlauxbuff[0]=='P')
				if(ltlauxbuff[2]=='7')
					if(ltlauxbuff[4]=='8')
						specialmove=1;*/

char * diff(unsigned char *buf)
{
	int i,j;
	if (specialmove==1)
		specialmove=2;
	else if(specialmove==2){
		Console::WriteLine(L"SPECIAL MOVE!!!!!!!!!!");
		newmove=1;
		strcpy(lastbuff, ltlauxbuff);
		strcpy(ltlauxbuff, "SSSSS");
		specialmove=0;
	}
	else
	{
	//printf("EPAH DETECTEI EVENT e o Buff e %s\n",97+j,56-i,Piececode[buf[j+i*8]]);
	for (i=0;i<8;i++)
    {
		for (j=0;j<8;j++)
        {
			//printf("%c - %c\n",Piececode[buf[j+i*8]], Piececode[auxbuff[j+i*8]]);
			if (Piececode[buf[j+i*8]]!=Piececode[auxbuff[j+i*8]]){
				//printf("EPAH DETECTEI EVENT EM %c - %c e o Buff e %c\n",97+j,56-i,Piececode[buf[j+i*8]]);
				if (Piececode[buf[j+i*8]]=='S'){
					if(ltlauxbuff[1]=='S'){
						ltlauxbuff[0]=Piececode[auxbuff[j+i*8]];
						ltlauxbuff[2]=56-i;
						ltlauxbuff[1]=97+j;
					}
					else{
						lastevent[0]=Piececode[auxbuff[j+i*8]];
						lastevent[2]=56-i;
						lastevent[1]=97+j;
					}
				}
				else if(ltlauxbuff[1]!='S'){
					ltlauxbuff[3]=97+j;
					ltlauxbuff[4]=56-i;
					if (lastevent[1]!='S'){
						if (ltlauxbuff[3]==ltlauxbuff[1] && ltlauxbuff[4]==ltlauxbuff[2]){
							ltlauxbuff[0]=lastevent[0];
							ltlauxbuff[1]=lastevent[1];
							ltlauxbuff[2]=lastevent[2];
						}
						lastevent[0]='S';
						lastevent[1]='S';
						lastevent[2]='S';
					}
					//printf("EPAH DETECTEI EVENT EM %c - %c e o Buff e %c\n",97+j,56-i,Piececode[buf[j+i*8]]);
				}}}
	}
	if (ltlauxbuff[3]!='S'){
		if (isupper(ltlauxbuff[0])){
			if (strcmp("Ke1g1",ltlauxbuff)==0||strcmp("Ke1c1",ltlauxbuff)==0) //white castle
				specialmove=1;
		}
		else {
			if(strcmp("ke8g8",ltlauxbuff)==0||strcmp("ke8c8",ltlauxbuff)==0)
				specialmove=1;
		}
		if(specialmove!=1){
			newmove=1;
			strcpy(lastbuff, ltlauxbuff);
			strcpy(ltlauxbuff, "SSSSS");
		}
	}
	}
	cpbuffers(auxbuff, buf, 64);
	return 0;
}


void printpos(unsigned char *buf)
{
	char memotxt[75];
	printf("\n8 ");
	
	int i,j;
    int memotxtptr=0;

    for (i=0;i<8;i++)
    {
		for (j=0;j<8;j++)
        {
        	if (!fmod((float)j+i,(float)2)) memotxt[memotxtptr++]=Piececode[buf[j+i*8]];
              else memotxt[memotxtptr++]=Piececode[buf[j+i*8]];
        }
    }
    memotxt[memotxtptr]='\0';

	for (i = 0; i < 64; ++i) {
		printf(" %c", memotxt[i]);
		if ((i + 1) % 8 == 0 && i != 63)
				printf("\n%d ", 7 - ROW(i));
	}
	printf("\n\n   a b c d e f g h\n\n");
}

char * buffunc(){
	return ltlauxbuff;
}

void cpbuffers(unsigned char * b1,unsigned char * b2,int l)
{
	int i;
	for (i=0;i<l;i++){
		b1[i]=b2[i];
	}
}

void EndCom(void)
{
     ComClose();
}

void ProcessScanEvents(void) // oude code
{
     switch (BoardEventID)
     {
      case (BOARDPOSITIONCHANGED):
      {
        FindBoardEvents(BoardScan, ProcessGameEvents);
      }
      case (STABLEBOARDPOSITION):
		  staboard ++;
        //printf("Stable Board\n");
       break;
      case (BOARDLOST):
      {
        printf("Board Lost\n");
        break;
      }
     }
}

void ProcessGameEvents(void)
{
     switch (GameEventID)
     {
      case (BOARDPOSITIONCHANGED):
        //printpos(GameBoard);
		diff(GameBoard);
        //printpos(BoardScan);
      break;

      case (RANDOMFISCHERSTART):
        //printf("Random Fischer Start");
      break;

      case (RESULTBLACKWINS):
        //printf("Black wins\n");
        break;

      case (RESULTWHITEWINS):
// a sample for announcements:
       //printf("White wins\n");
       break;
      case (RESULTDRAW):
        //printf("Draw\n");
        break;
      case (ROTATIONCHANGED):
       //printf("Rotated\n");
       break;
      case (STARTINGPOSITION):
       //printf("Starting position\n");
       break;
      case (SETUPROTATED):
       //printf("Rotated by setup\n");
       break;
      case (NEWPOSITIONWHITETOMOVE):
       //printf("New pos:White to move\n");
       break;
      case (NEWPOSITIONBLACKTOMOVE):
       //printf("New pos:Black to move\n");
       break;
      case (WHITEMOVENOW):
       //printf("White move now\n");
       break;
      case (BLACKMOVENOW):
       //printf("Black move now\n");
       break;
      case (SETUPENTERED):
       //printf("Setup entered\n");
       break;
      case (DMESSAGE):
       printf(diagnostic_message);
       break;

     } // end of switch
}