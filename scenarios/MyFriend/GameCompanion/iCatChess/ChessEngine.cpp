#include "stdafx.h"
#include "ChessEngine.h"

ChessEngine::ChessEngine(){
	init_hash();
	init_board();
	open_book();
	gen();
	computer_side = EMPTY;
	max_time = 1 << 25;
	max_depth = 4;
}

// makes a move in the time wanted or number of moves 
char* ChessEngine::play() {
   		think(1);
		if (!pv[0][0].u) {
			//printf("(no legal moves)\n");
			computer_side = EMPTY;
			return 0;
		}
		//printf("Computer's move: %s\n", move_str(pv[0][0].b));
		makemove(pv[0][0].b);
		ply = 0;
		gen();
		//print_result();
		return move_str(pv[0][0].b);
}

char* ChessEngine::playRandom(){
		int i=0, aux=0;
		//i = rand() % first_move[1];
		while(aux==0){
			i = rand() % first_move[1];
			if(!strcmp(move_str(gen_dat[i].m.b),"e1g1"))
			{
				if (color[F1] != EMPTY || color[G1] != EMPTY ||
						attack(F1, xside) || attack(G1, xside) || in_check(side))
						continue;
			}
			if(!strcmp(move_str(gen_dat[i].m.b),"e1c1"))
			{
				if (color[B1] != EMPTY || color[C1] != EMPTY || color[D1] != EMPTY ||
						attack(C1, xside) || attack(D1, xside)|| in_check(side))
						continue;
			}
			if(!strcmp(move_str(gen_dat[i].m.b),"e8g8"))
			{
				if (color[F8] != EMPTY || color[G8] != EMPTY ||
						attack(F8, xside) || attack(G8, xside)|| in_check(side))
						continue;
			}
			if(!strcmp(move_str(gen_dat[i].m.b),"e8c8"))
			{
				if (color[B8] != EMPTY || color[C8] != EMPTY || color[D8] != EMPTY ||
						attack(C8, xside) || attack(D8, xside)|| in_check(side))
						continue;
			}
			aux=1;
		}
		if (makemove(gen_dat[i].m.b))
			return move_str(gen_dat[i].m.b);
		else
			return play();
}

void ChessEngine::generateMoves(){
		ply = 0;
		gen();
}

// get's the evaluation value of the position
int ChessEngine::getvalue() {
   return retLastX();
}

int ChessEngine::evalMove(){
return eval();
}

// returns the color to move
int ChessEngine::getcolor() {
   //printf("color to move is %d\n", side);
   return side;
}

// reset's the game to the beginning
void ChessEngine::newgame() {
	computer_side = EMPTY;
	init_board();
	gen();
}

// print's the position in the console
void ChessEngine::printpos() {
   print_board();
}

// returns the pieces in the position
int * ChessEngine::getboardpos() {
   return piece;
}

// returns the color of the pieces in the position
int * ChessEngine::getboardcolor() {
   return color;
}

// put desired position by receiving the color to play and the board
void ChessEngine::putposition(int * board, int * boardColor, int c, int cast) {
   	int i;
	computer_side = EMPTY;
	for (i = 0; i < 64; ++i) {
		color[i] = boardColor[i];
		piece[i] = board[i];
	}
	side = c;
	if (side == 0)
		xside = DARK;
	else
		xside = LIGHT;
	castle = cast;
	ep = -1;
	fifty = 0;
	ply = 0;
	hply = 0;
	set_hash();  /* init_hash() must be called before this function */
	first_move[0] = 0;
	gen();
}

// use n seconds for each move
void ChessEngine::thinktime(int n) {
	max_time=n;
	//max_time *= 1000;  IOLANDA COMENTOU ISTO
	max_depth = 32;
}

// calculate m moves by play
void ChessEngine::thinkmoves(int m) {
	max_depth=m;
	max_time = 1 << 25;
}

void ChessEngine::takeBack() {
	if (hply)
	{
	computer_side = EMPTY;
	takeback();
	ply = 0;
	gen();
	}
}

bool ChessEngine::insertmove(char* str) {
	int m = parse_move(&str[1]);
	if (m == -1 || !makemove(gen_dat[m].m.b))
		return false;
	else {
		ply = 0;
		gen();
		//print_result();
		return true;
	}
}

int ChessEngine::gameResult(){
 return isMate();
}

bool ChessEngine::check(){
if (in_check(side))
	return true;
else return false;
}