#ifndef __CHESSMODULE_H__
#define __CHESSMODULE_H__

#include <math.h>
#include <vector>

#include "TimedModule.h"
#include "OutputPortTypes.h"
#include "AnimationModuleInterface.h"
#include "Form1.h"
#include "StateMachine.h"
#include "ChessModCfg.h"
#include <vcclr.h>

using namespace System::Threading;
using namespace System::Windows::Forms;
using namespace iCatChess;
using namespace std;
using namespace System;

//apriximaçao valor esperado: 0-basica 1-Yo&Andre 2-Moving Averages
//#define CALC_EXPECTED_VALUE 1
#define SIMPLE_APR 0
#define YOANDRE_APR 1
#define MOVINGAVERAGES_APR 2

#define EMOTIVECTOR4 4 
#define EMOTIVECTOR9 9 
#define THRESHOLD 10

//animation 
#define ANIMATION_CHANNEL 2
#define LIPSYNC_CHANNEL 1
#define MOOD_CHANNEL 3
#define STATEM_CHANNEL 4

//game state
#define INIT 0
#define INITIAL_BOARD 1
#define ICAT_TURN 2
#define WAIT_FOR_MOVE 3
#define OPP_TURN 4
#define REACT_TO_MOVE 5
#define THINK_BEFORE_MOVE 6
#define GAME_OVER 7
#define ENDING 8

//Emotional reactions
#define STRONGER_R 0
#define WEAKER_R 1
#define STRONGER_P 2
#define WEAKER_P 3
#define THINK 4
#define EXPECTED_R 5
#define UNEXPECTED_R 6
#define UNEXPECTED_P 7
#define EXPECTED_P 8

//#define 

class ChessModule:public TimedModule
{
   private:

	   //CONSTANTES
   	   static const char* emotionalActionsPath[];
	   static const char* moodBehaviourPath;

   	   static const char* letsPlayPath;
   	   static const char* giveUpPath;

	   char *_lookAtBoardActionPath;

	  // static const char* textConfirmMove[];
	   //static const char* textNotMyMove[];
	 //  static const char* textIllegalMove[];
	   //static vector <const char*> textConfirmMove;


	   AnimationModuleInterface * _animation;
	   ChessEngine *_chessEngine;
	   ElectronicBoard *_electronicBoard; 
	   ICatMood *_iCatMood;
	   StateMachine *_stateMachine;
	   ChessModCfg *_chessConfiguration;
	   
	   //DEBUG
	   gcroot <Form1^> _formDebug;
	   
	   bool _boardPresent;
	   bool _pressedButton;

	   int _gameState;
	   int _currentEmotion;

	   int _wait;

	   //para as contas do Emotivector
	   vector<int> _mismatchValues;
	   vector<int> _sensedValues;
	   int _expectedValue;
	   float _n;
	   float _prevExpectedValue;
	   int _threshold;

	   DMLString  _icat_move;
	   DMLString _last_move;
	   int _move_number;

	   DMLString _endingAnimation;

	void oppTurnDebug();		

   public:

       ChessModule (Form1 ^frm, bool bp);
      ~ChessModule (void);

	  gcroot <Form1^> getForm1();
	  void setMovePlayed(bool b);
	  bool getMovePlayed();

	  StateMachine *getStateMachine();
	  AnimationModuleInterface *getAnimationModule();

	  void ThreadProc();
      void vDoAction (void);
	  void vOnStart (void);
	  bool wonOpp();
	  bool wonIcat();
	  int reactToMove(void);
	  bool verifyMove(void);
	  static void OnAniModStatus(ChessModule * d);
	  bool strCompare(DMLString str1, DMLString str2);
	  int calcEmotivector4(int delta, int expected, int sensed);
	  int calcEmotivector9(int delta, int expected, int sensed, int threshold);
	  
	  double calcLog(int base, int number);
	  void calcChessMood(int sensed); 
	  int movingAverages (vector<int> sensedValues, int sensed, int prevExpectedValue, int n);

	  void stateInitBoard(void);
	  void stateInit(void);
	  void stateICatTurn(void);
	  void stateWaitForMove(void);
	  void stateOppTurn(void);
	  void stateReactToMove(void);
	  void stateGameOver(void);

	  void copyEBtoCE();

}; // ChessModule

#endif //__CHESSMODULE_H__
