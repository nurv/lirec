#ifndef __ELECTRONICBOARD_H__
#define __ELECTRONICBOARD_H__

#include <iostream>
#include <math.h>
#include <windows.h>
#include "ElectronicBoard\dgtb105.h"
#include "ElectronicBoard\dgtbrd13.h"
#include "ElectronicBoard\processevents.h"

class ElectronicBoard {
  public:
	ElectronicBoard(int port);							// constructor
    void Update();							// makes a move in the time wanted or number of moves 
	char* getMove();							// returns the position in char*	
	unsigned char* getBoard(void);
	int newMove();
	int stableBoard();
	void sendClockMessage(char * message);
	char getPieceFromPosition(char column, int line);
};
#endif