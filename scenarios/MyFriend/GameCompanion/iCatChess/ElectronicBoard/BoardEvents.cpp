//---------------------------------------------------------------------------
#include "stdafx.h"
#include "BoardEvents.h"
#include "dgtbrd13.h"
#include <stdio.h>
#include <string.h>

//---------------------------------------------------------------------------

//#pragma package(smart_init)
#include <iostream>

unsigned int GameEventID;
unsigned char GameBoard[64];
bool rotated;
void *(pmfunction)();

char DGTTimeBlack[10];
int  DGTTimeBlackFlags = 0;
char DGTTimeWhite[10];
int  DGTTimeWhiteFlags = 0;

unsigned int OldDGTTimeflags = 0;

// kings movement. Called after every update.
void FindBoardEvents(unsigned char *BoardScan, void (*pmfunction)())
{
        GameEventID=CheckBoardEvents(BoardScan);
        if (GameEventID!=NULL) pmfunction();
        NormalizeBoard(GameBoard,BoardScan);
        GameEventID=CheckForceMove(GameBoard);
        if (GameEventID!=NULL) pmfunction();
        GameEventID=BOARDPOSITIONCHANGED;
        pmfunction();
}

void NormalizeBoard(unsigned char *NormBrd, unsigned char *BoardScan)
{
   int i;
   // Build NormBoard;
   if (rotated)
   for (i=0;i<64;i++)
       NormBrd[i]=BoardScan[63-i];
   else
   for (i=0;i<64;i++)
       NormBrd[i]=BoardScan[i];
}

// Board events functie
unsigned int CheckBoardEvents(unsigned char *BoardScan)
{
   bool kingspresent=false;
   int i;
   bool alloke; unsigned int kingcnt;

   unsigned char NewGamePos[64]=
   {BROOK,BKNIGHT,BBISHOP,BQUEEN,BKING,BBISHOP,BKNIGHT,BROOK,
    BPAWN,BPAWN,BPAWN,BPAWN,BPAWN,BPAWN,BPAWN,BPAWN,
    EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,
    EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,
    EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,
    EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,
    WPAWN,WPAWN,WPAWN,WPAWN,WPAWN,WPAWN,WPAWN,WPAWN,
    WROOK,WKNIGHT,WBISHOP,WQUEEN,WKING,WBISHOP,WKNIGHT,WROOK};
    unsigned char NewGamePosRot[64]=
   {WROOK,WKNIGHT,WBISHOP,WKING,WQUEEN,WBISHOP,WKNIGHT,WROOK,
    WPAWN,WPAWN,WPAWN,WPAWN,WPAWN,WPAWN,WPAWN,WPAWN,
    EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,
    EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,
    EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,
    EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY,
    BPAWN,BPAWN,BPAWN,BPAWN,BPAWN,BPAWN,BPAWN,BPAWN,
    BROOK,BKNIGHT,BBISHOP,BKING,BQUEEN,BBISHOP,BKNIGHT,BROOK};
             // Check on kings:
             int kingsonblack=0,kingsonwhite=0;
             if ((BoardScan[27]==WKING)||(BoardScan[27]==BKING))
               kingsonwhite++;
             if ((BoardScan[36]==WKING)||(BoardScan[36]==BKING))
               kingsonwhite++;
             if ((BoardScan[28]==WKING)||(BoardScan[28]==BKING))
               kingsonblack++;
             if ((BoardScan[35]==WKING)||(BoardScan[35]==BKING))
               kingsonblack++;
             if ((kingsonblack==2)&&(kingsonwhite==0))
             {
               return RESULTBLACKWINS;
             }
             if ((kingsonblack==0)&&(kingsonwhite==2))
             {
               return RESULTWHITEWINS;
             }
             if ((kingsonblack==1)&&(kingsonwhite==1))
             {
               return RESULTDRAW;
             }
             // not a result...
             // Check on RotateByPawn
             for(i=0;i<63;i++)
             // count kings
               if ((BoardScan[i]==BKING)||(BoardScan[i]==WKING))
               {
                  kingspresent=true;
                  break;
               }
             if (!kingspresent)
             {
                // check wpawn on normboard[56-63]
                for(i=0;i<8;i++)
                {
                   if (GameBoard[63-i]==WPAWN)  // rotate normalisation
                   {
                      rotated=!rotated;
                      NormalizeBoard(GameBoard, BoardScan);
                      return SETUPROTATED;
                   }
                   if (GameBoard[i]==BPAWN)
                   {
                      rotated=!rotated;
                      NormalizeBoard(GameBoard, BoardScan);
                      return SETUPROTATED;
                   }
                }
             }
             // not setuprotating, nor result...
             // Check on new game or rotated:
             if (!memcmp(BoardScan,NewGamePos,64))
             {
                if (rotated)
                {
                   rotated=false;
                   NormalizeBoard(GameBoard, BoardScan);
                }
                return STARTINGPOSITION;
             }
             else
             if (!memcmp(BoardScan, NewGamePosRot,64))
             {
                if (!rotated)
                {
                   rotated=true;
                   NormalizeBoard(GameBoard, BoardScan);
                }
                return STARTINGPOSITION;
             }
             if (!memcmp(BoardScan+8, NewGamePos+8, 48))
             {
                // check A and H > randomchess
                alloke=true; kingcnt=0;
                for (i=0;i<8;i++)
                {
                 switch (BoardScan[i])
                 {
                  case BROOK:
                  case BBISHOP:
                  case BKNIGHT:
                  case BQUEEN:
                  break;
                  case BKING:
                    kingcnt++;
                  break;
                  default:
                   alloke=false;
                  break;
                 }
                }
                if ((!alloke)||(kingcnt!=1)) goto CHECKROT;
                alloke=true; kingcnt=0;
                for (i=56;i<64;i++)
                {
                 switch (BoardScan[i])
                 {
                  case WROOK:
                  case WBISHOP:
                  case WKNIGHT:
                  case WQUEEN:
                  break;
                  case WKING:
                    kingcnt++;
                  break;
                  default:
                   alloke=false;
                  break;
                 }
                }
                if ((!alloke)||(kingcnt!=1)) goto CHECKROT;
                rotated=false;
                NormalizeBoard(GameBoard, BoardScan);
                return RANDOMFISCHERSTART;
               }
CHECKROT:
             if (!memcmp(BoardScan+8, NewGamePosRot+8, 48))
             {
                // check A and H > randomchess
                alloke=true; kingcnt=0;
                for (i=0;i<8;i++)
                {
                 switch (BoardScan[i])
                 {
                  case WROOK:
                  case WBISHOP:
                  case WKNIGHT:
                  case WQUEEN:
                  break;
                  case WKING:
                    kingcnt++;
                  break;
                  default:
                   alloke=false;
                  break;
                 }
                }
                if ((!alloke)||(kingcnt!=1)) return NULL;
                alloke=true; kingcnt=0;
                for (i=56;i<64;i++)
                {
                 switch (BoardScan[i])
                 {
                  case BROOK:
                  case BBISHOP:
                  case BKNIGHT:
                  case BQUEEN:
                  break;
                  case BKING:
                    kingcnt++;
                  break;
                  default:
                   alloke=false;
                  break;
                 }
                }
                if ((!alloke)||(kingcnt!=1)) return NULL;
                rotated=true;
                NormalizeBoard(GameBoard, BoardScan);
                return RANDOMFISCHERSTART;
            }
            return NULL;
// end gameevents
}

unsigned int CheckForceMove(unsigned char *NormBoard)
{
   static int Bkingpos=-1, Wkingpos=-1;
   static int oldBkingpos=-1, oldWkingpos=-1;
   int tmpBkingpos=-1, tmpWkingpos=-1;
   static unsigned char previouscolortostartevent=NEWPOSITIONWHITETOMOVE;
   int i; int kingsoncenter;
// state values:
#define NOKINGSFOUND 0
#define FIRSTKINGFOUND 1
#define BOTHKINGSFOUND 2
#define ONEKINGLEFT 3
   static int State=NOKINGSFOUND;
   switch (State)
   {
     case NOKINGSFOUND:
     for (i=0;i<64;i++)
     {
       if (NormBoard[i]==BKING) Bkingpos=i;
       if (NormBoard[i]==WKING) Wkingpos=i;
     }
     if ((Bkingpos<0)&&(Wkingpos<0))
     {
        return NULL;
     }
     if ((Bkingpos>=0)&&(Wkingpos>=0))
     {
       // exactly simultanious placed: newpos with default or previous color to play
       State=BOTHKINGSFOUND;
       return previouscolortostartevent;
     }
     State=FIRSTKINGFOUND;
     return NULL; // end NOKINGSFOUND
     case FIRSTKINGFOUND:
     for (i=0;i<64;i++)
     {
       if (NormBoard[i]==BKING) tmpBkingpos=i;
       if (NormBoard[i]==WKING) tmpWkingpos=i;
     }
     if ((tmpBkingpos>=0)&&(tmpWkingpos>=0))
     { // two found...
        kingsoncenter=0;
        if ((NormBoard[27]==WKING)||(NormBoard[27]==BKING))
             kingsoncenter++;
        if ((NormBoard[36]==WKING)||(NormBoard[36]==BKING))
             kingsoncenter++;
        if ((NormBoard[28]==WKING)||(NormBoard[28]==BKING))
             kingsoncenter++;
        if ((NormBoard[35]==WKING)||(NormBoard[35]==BKING))
             kingsoncenter++;
        if (kingsoncenter>1) // result entered...
          return NULL;
        if (tmpBkingpos==Bkingpos)
        {
          Wkingpos=tmpWkingpos;
          // Whitetostartevent
          previouscolortostartevent=NEWPOSITIONWHITETOMOVE;
          State=BOTHKINGSFOUND;
          return NEWPOSITIONWHITETOMOVE;
        }
        if (tmpWkingpos==Wkingpos)
        {
          Bkingpos=tmpBkingpos;
          // Blacktostartevent
          previouscolortostartevent=NEWPOSITIONBLACKTOMOVE;
          State=BOTHKINGSFOUND;
          return NEWPOSITIONBLACKTOMOVE;
        }
       // else: one is moved, other found simultaniously
       Bkingpos=tmpBkingpos;
       Wkingpos=tmpWkingpos;
       State=BOTHKINGSFOUND;
       return previouscolortostartevent;
       //
     } // end if both colors found.
     if ((tmpBkingpos<0)&&(tmpWkingpos<0))
     {
         // none found...
         Bkingpos=tmpBkingpos;
         Wkingpos=tmpWkingpos;
         State=NOKINGSFOUND;
         return SETUPENTERED;
      }
      // so only one found. Make pos=tmppos
      Bkingpos=tmpBkingpos;
      Wkingpos=tmpWkingpos;
      // state unchanged: one king found.
      return NULL; // end FIRSTKINGSFOUND
      case BOTHKINGSFOUND:
        kingsoncenter=0;
        if ((NormBoard[27]==WKING)||(NormBoard[27]==BKING))
             kingsoncenter++;
        if ((NormBoard[36]==WKING)||(NormBoard[36]==BKING))
             kingsoncenter++;
        if ((NormBoard[28]==WKING)||(NormBoard[28]==BKING))
             kingsoncenter++;
        if ((NormBoard[35]==WKING)||(NormBoard[35]==BKING))
             kingsoncenter++;
      if (kingsoncenter>1) // result entered...
        return NULL;
      for (i=0;i<64;i++)
      {
        if (NormBoard[i]==BKING) tmpBkingpos=i;
        if (NormBoard[i]==WKING) tmpWkingpos=i;
      }
      if ((tmpBkingpos<0)&&(tmpWkingpos<0))
      {
         // both disappeared...
         Bkingpos=tmpBkingpos;
         Wkingpos=tmpWkingpos;
         State=NOKINGSFOUND;
         // event: SetUp
         return SETUPENTERED;
      }
      if ((tmpBkingpos>=0)&&(tmpWkingpos>=0))
      {
         // both still there...
         Bkingpos=tmpBkingpos;
         Wkingpos=tmpWkingpos;
         State=BOTHKINGSFOUND;
         return NULL;
      }
      // one left, other disappeared...
      if (Bkingpos==tmpBkingpos)
      // Black stayed in place
      {
         oldWkingpos=Wkingpos; // to compare replacement later...
         oldBkingpos=-1;
         Wkingpos=tmpWkingpos; //away
         State=ONEKINGLEFT;
         return NULL;
      }
      if (Wkingpos==tmpWkingpos)
      // White stayed in place
      {
         oldBkingpos=Bkingpos; // to compare replacement later...
         oldWkingpos=-1;
         Bkingpos=tmpBkingpos; //away
         State=ONEKINGLEFT;
         return NULL;
      }
      Bkingpos=tmpBkingpos;
      Wkingpos=tmpWkingpos;
      State=BOTHKINGSFOUND; // which works good...
      return NULL;
      case ONEKINGLEFT:
      for (i=0;i<64;i++)
      {
        if (NormBoard[i]==BKING) tmpBkingpos=i;
        if (NormBoard[i]==WKING) tmpWkingpos=i;
      }
      if ((Bkingpos<0)&&(tmpBkingpos==oldBkingpos) && (tmpWkingpos==Wkingpos))
      {
         // movenow for black
          Bkingpos=oldBkingpos=tmpBkingpos;
          State=BOTHKINGSFOUND;
          return BLACKMOVENOW;
      }
      if ((Wkingpos<0)&&(tmpWkingpos==oldWkingpos) && (tmpBkingpos==Bkingpos))
      {
         // movenow for white
          Wkingpos=oldWkingpos=tmpWkingpos;
          State=BOTHKINGSFOUND;
          return WHITEMOVENOW;
      }
      if ((tmpBkingpos<=0)&&(tmpWkingpos<=0))
      {
         // both disappeared...
         Bkingpos=tmpBkingpos;
         Wkingpos=tmpWkingpos;
         State=NOKINGSFOUND;
         return SETUPENTERED;
      }
      // else: shifting of king, both kings...
      Bkingpos=tmpBkingpos;
      Wkingpos=tmpWkingpos;
      State=BOTHKINGSFOUND; // which works good...
      return NULL;
   } //end switch
   return NULL;
}

//void FindClockEvents(const char *DGTTime, int DGTTimeflags, void (*pmfunction)())
//{
/*uitvoer:
DGTTimeBlack
DGTTimeWhite
DGTTimeBlackFlags
DGTTimeWhiteFlags*/
  /*            if (rotated)
              {
                if (DGTTimeflags&DGTTIMELEFTTURN)
                   DGTTimeBlackFlags|=DGTTIMERUNNINGX;
                else
                   DGTTimeBlackFlags&=~DGTTIMERUNNINGX;
                if (DGTTimeflags&DGTTIMERIGHTTURN)
                   DGTTimeWhiteFlags|=DGTTIMERUNNINGX;
                else
                   DGTTimeWhiteFlags&=~DGTTIMERUNNINGX;
                if (strncmp(DGTTimeBlack,&DGTTime[0],7)!=0)
                {
                   sprintf(DGTTimeBlack,"%.7s",&DGTTime[0]);

                   GameEventID=BLACKCLOCKTIMECHANGED;
                   pmfunction();
                }
                if (strncmp(DGTTimeWhite,&DGTTime[8],7)!=0)
                {
                   sprintf(DGTTimeWhite,"%.7s",&DGTTime[8]);
                   GameEventID=WHITECLOCKTIMECHANGED;
                   pmfunction();
                }
                // beurt afgemeld
                if ((OldDGTTimeflags&DGTTIMELEFTTURN)&&(!(DGTTimeflags&DGTTIMELEFTTURN)))
                {
                   GameEventID=BLACKSTOPPEDCLOCK;
                   pmfunction();
                }
                if ((OldDGTTimeflags&DGTTIMERIGHTTURN)&&(!(DGTTimeflags&DGTTIMERIGHTTURN)))
                {
                   GameEventID=WHITESTOPPEDCLOCK;
                   pmfunction();
                }
//                if (clockrunning&&een vlag gevallen:speciaal geval: kijk naar tumbler&vlag
                if ( (DGTTimeflags&(DGTTIMEBLOCKFLAGRIGHT+DGTTIMEBLOCKFLAGLEFT)))
                {
                  if (!(OldDGTTimeflags&DGTTIMETUMBLERLEFT)&&((DGTTimeflags&DGTTIMETUMBLERLEFT)))
                  {
                    GameEventID=WHITESTOPPEDCLOCK;
                     pmfunction();
                  }
                  if ((OldDGTTimeflags&DGTTIMETUMBLERLEFT)&&(!(DGTTimeflags&DGTTIMETUMBLERLEFT)))
                  {
                    GameEventID=BLACKSTOPPEDCLOCK;
                     pmfunction();
                  }
                }
              }
              else
              {
                if (DGTTimeflags&DGTTIMERIGHTTURN)
                   DGTTimeBlackFlags|=DGTTIMERUNNINGX;
                else
                   DGTTimeBlackFlags&=~DGTTIMERUNNINGX;
                if (DGTTimeflags&DGTTIMELEFTTURN)
                   DGTTimeWhiteFlags|=DGTTIMERUNNINGX;
                else
                   DGTTimeWhiteFlags&=~DGTTIMERUNNINGX;
                if (strncmp(DGTTimeBlack,&DGTTime[8],7)!=0)
                {
                   sprintf(DGTTimeBlack,"%.7s",&DGTTime[8]);
                   GameEventID=BLACKCLOCKTIMECHANGED;
                   pmfunction();
                }
                if (strncmp(DGTTimeWhite,&DGTTime[0],7)!=0)
                {
                   sprintf(DGTTimeWhite,"%.7s",&DGTTime[0]);
                   GameEventID=WHITECLOCKTIMECHANGED;
                   pmfunction();
                }
// nu: vlag vallen, beurt afgemeld, block per kleur.
                // beurt afgemeld?
// bij gevallen vlag werkt e.e.a. niet meer.
//Uitzoeken
                if ((OldDGTTimeflags&DGTTIMERIGHTTURN)&&(!(DGTTimeflags&DGTTIMERIGHTTURN)))
                {
                   GameEventID=BLACKSTOPPEDCLOCK;
                   pmfunction();
                }
                if ((OldDGTTimeflags&DGTTIMELEFTTURN)&&(!(DGTTimeflags&DGTTIMELEFTTURN)))
                {
                   GameEventID=WHITESTOPPEDCLOCK;
                   pmfunction();
                }
//                if (clockrunning&&een vlag gevallen:speciaal geval: kijk naar tumbler&vlag
                if ( (DGTTimeflags&(DGTTIMEBLOCKFLAGLEFT+DGTTIMEBLOCKFLAGRIGHT)))
                {
                  if (!(OldDGTTimeflags&DGTTIMETUMBLERLEFT)&&((DGTTimeflags&DGTTIMETUMBLERLEFT)))
                  {
                    GameEventID=BLACKSTOPPEDCLOCK;
                     pmfunction();
                  }
                  if ((OldDGTTimeflags&DGTTIMETUMBLERLEFT)&&(!(DGTTimeflags&DGTTIMETUMBLERLEFT)))
                  {
                    GameEventID=WHITESTOPPEDCLOCK;
                     pmfunction();
                  }
                }
              }
              OldDGTTimeflags=DGTTimeflags;
}*/
