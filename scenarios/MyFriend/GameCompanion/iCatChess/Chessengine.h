#ifndef __CHESSENGINE_H__
#define __CHESSENGINE_H__

#include <iostream>

#include "engine\defs.h"
#include "engine\data.h"
#include "engine\protos.h"

class ChessEngine {
  public:
	ChessEngine();							// constructor
    char* play();							// makes a move in the time wanted or number of moves 
	char* playRandom();
    int getvalue();							
    int getcolor();							// returns the color to move
	void newgame();							// reset's the game to the beginning
	void printpos();						// print's the position in the console
	int * getboardpos();					// returns the position in char*
	int * getboardcolor();					// returns the position in char*
	void putposition(int * board, int * boardColor, int c, int cast);	// put desired position by receiving the color to play and the board
	void thinktime(int n);					// use n seconds for each move
	void thinkmoves(int m);					// calculate m moves by play
	void takeBack();						// takes back one move
	bool insertmove(char *str);				// user inserts a move
	int gameResult();
	bool check();
	int evalMove();							// get's the evaluation value of the position
	void generateMoves();

  private:
    int computer_side;
};
#endif