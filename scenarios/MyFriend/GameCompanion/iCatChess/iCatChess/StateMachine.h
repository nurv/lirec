#include <math.h>
#include <vector> 
#include <string> 
#include "DMLString.h"
using namespace std;


/*#define INIT 0
#define WAIT_FOR_MOVE 1
#define IDLE 2*/

//states
#define SLEEPING_BY_NODDING 0
#define SLEEPING_BY_FLASHING 1
#define INTERACTIVE 2
#define	IDLE_LOOK_FRONT 3
#define	IDLE_LOOK_RIGHT 4
#define	IDLE_LOOK_LEFT 5
#define INTRO01 6
#define	INTRO02 7
#define	INTRO03 8

struct AnimationTable {
	//vector <int> weights;
	//int [] weights;
	//vector <string> animations;
	int * weights;
	const char ** animations;
	int size;
};

class StateMachine {

private:
	int _currentState;
	int _prevChessState;
	int _nextState;
	int _sleepingNods;
	int _timeout;
	bool _doTransition;
	DMLString _currentAnimation;
	AnimationTable _idle_front_table;
	AnimationTable _idle_left_table;
	AnimationTable _idle_right_table;
	AnimationTable _interactive_table;

public:
	StateMachine();
	void Reset();
	void update(int state, AnimationModuleInterface * _animation, int channel);
	bool ThrowDice(int threeshold);
	const char * SelectRandom(AnimationTable at);
	int getState();
	int getNextState();
	void setTimeOut(int value);
	int getTimeOut();
	void GotoState(int nextstate, bool timeout);
	DMLString getCurrentAnimation();
	DMLString GetTransitionAnimation();
	DMLString GetNextAnimation();
};
