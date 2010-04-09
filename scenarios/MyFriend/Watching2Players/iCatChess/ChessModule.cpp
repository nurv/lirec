#include "stdafx.h"
#include <string>
#include <ctype.h>
#include <stdlib.h> //new to run java
#include <iostream>
#include <fstream>
#include "stdio.h"

#include <windows.h>
#include <mmsystem.h>
#pragma comment(lib, "winmm.lib")

#using <mscorlib.dll>

using namespace std;
using namespace System;
using namespace System::Runtime::InteropServices;

//#define companionName "André"
//#define oppName "Samuel"

#define STRONGER_R 0
#define WEAKER_R 3
#define STRONGER_P 2
#define WEAKER_P 1
#define THINK 4
#define EXPECTED_R 5
#define UNEXPECTED_R 6
#define UNEXPECTED_P 7
#define EXPECTED_P 8


#define MOVE_THINKING_TIME 50

#define ACTIONS_PATH_SIZE 9
const char *ChessModule::emotionalActionsPath[] = {
	"\\STANDARD\\Empathy\\Excited_Empathy.raf",
	"\\STANDARD\\Empathy\\Happy_Empathy.raf",
	"\\STANDARD\\Empathy\\Scared_Empathy.raf",
	"\\STANDARD\\Empathy\\Weaker_P_Empathy.raf",
	"\\STANDARD\\Empathy\\Think_Empathy.raf",
	"\\STANDARD\\Empathy\\Expected_R_Empathy.raf",
	"\\STANDARD\\Empathy\\Arrogant_Empathy.raf",
	"\\STANDARD\\Empathy\\Unexpected_P_Empathy.raf",
	"\\STANDARD\\Empathy\\Angry_Empathy.raf"};

	const char *ChessModule::moodBehaviourPath = "\\STANDARD\\CatMood.rbf";

	const char *ChessModule::letsPlayPath = "\\STANDARD\\Empathy\\vamosJogar_Empathy.raf";
	const char *ChessModule::giveUpPath = "\\STANDARD\\Actions\\Social\\desisto_en.raf";
	const char *ChessModule::lookLeftStayTherePath = "\\STANDARD\\Empathy\\IntrLookLeftStayThere.raf";
	const char *ChessModule::leftToCenterPath = "\\STANDARD\\Empathy\\IntrReturnLeft.raf";
	const char *ChessModule::lookRightStayTherePath = "\\STANDARD\\Empathy\\LookRightAndStay.raf";
	const char *ChessModule::rightToCenterPath = "\\STANDARD\\Empathy\\ReturnRight.raf";


	int SensedValue, LastAction;
	IntPtr firstPtr;
	char* formStringTbox, *companionName, *oppName;

	string CAPTURED_PIECE;
	bool STATEMACHINEAVAILABLE;

	const __wchar_t * str1;

	const char *ChessModule::type_victories_icat = "victories_icat";

	bool alreadySpokeEncouragement[4] = {false,false,false,false};

	ChessModule::ChessModule(Form1 ^ frm, bool bp)
		: TimedModule ("ChessModule",   // This module is called "ChessModule"
		300)  //,        // and calls vDoAction each 5000ms	 estava 200	                                
	{
		_chessConfiguration = new ChessModCfg();
		if (_chessConfiguration->loadFromFile("chessModCfg.xml")==1){
			exit(0);
		}

		_stateMachine=new StateMachine();
		_stateMachine->Reset();
		_gameState = INIT;
		_wait = -1;

		STATEMACHINEAVAILABLE=true;

		_companionPlayedLastTurn = false;

		_electronicBoard = new ElectronicBoard(_chessConfiguration->getPort());

		_chessEngine = new ChessEngine();
		//_chessEngine->thinkmoves(_chessConfiguration->getSearchDepth());
		_chessEngine->thinktime(MOVE_THINKING_TIME);

		_animation = new AnimationModuleInterface((t_EventHandler)ChessModule::OnAniModStatus);
		_animation->Initialize(this);

		_iCatMood = new ICatMood(_animation);

		_formDebug= frm;
		_currentEmotion = THINK;

		_expectedValue = 0;
		_prevExpectedValue = 0;
		_n = 2;
		_threshold = 0;
		_sensedValues.push_back(0); //o 1º sensed value é 0!
		_mismatchValues.push_back(0);

		_icat_move="";
		_move_number=0;
		_user_movenumber=0;
		_fileCreated=false;
		_iCatIsSpeaking = 0;
		_alreadySpoke=false;
		fStart ();
		_speechTimeout = 0;
		srand(time(0));

		//std::locale::global(std::locale(""));
		setlocale(LC_ALL, "pt_BR.UTF-8");

	} // ChessModule


	ChessModule::~ChessModule (void)
	{
		_formDebug->Close();
	} // ~ChessModule

	void ChessModule::setMovePlayed(bool b){
		//	_move_played=b;
	}

	bool ChessModule::getMovePlayed(){
		//	return _move_played;
		return true;
	}

	StateMachine * ChessModule::getStateMachine(){
		return _stateMachine;
	}


	AnimationModuleInterface * ChessModule::getAnimationModule(){
		return _animation;
	}

	// vOnStart is called when the state of the module changes from IDLE to RUNNING. 
	void ChessModule::vOnStart (void)
	{
		_languageEngine = new LanguageEngineMaster();
		_languageEngine->start();

	} // vOnStart

	void ChessModule::vDoAction (void)
	{
		int action = 0;

		_electronicBoard->Update();

		switch (_gameState) {
		case INIT:
			stateInit();
			break;
		case INITIAL_BOARD:
			stateInitBoard();
			break;
		case ICAT_TURN:
			stateICatTurn();
			break;
		case WAIT_FOR_MOVE:
			stateWaitForMove();
			break;
		case COMPANION_TURN:
			stateCompanionTurn();
			break;
		case OPP_TURN:
			stateOppTurn();
			break;
		case THINK_BEFORE_MOVE:
			//CODIGO PARA VER SE ACABOU A ANIMAÇAO esta na callback
			break;
		case REACT_TO_MOVE:
			stateReactToMove();
			break;
		case GAME_OVER:
			stateGameOver();
			break;
		}

		if (_formDebug->button3->Enabled == false) {
			_formDebug->button3->Enabled = true;
			_endingAnimation = "\\STANDARD\\Actions\\Social\\okdesististes_en.raf";
			_animation->PlayAnimation(_endingAnimation.pszGetPointer(), ANIMATION_CHANNEL);
			_gameState = ENDING;
			return;
		}

		if (_formDebug->buttdesisto->Enabled == false) {
			_formDebug->buttdesisto->Enabled = true;
			//_animation->Say("Desisto, sei que me vais ganhar... Parabéns!");
			_endingAnimation = "\\STANDARD\\Actions\\Social\\desisto_en.raf";
			_animation->PlayAnimation(_endingAnimation.pszGetPointer(), ANIMATION_CHANNEL);
			_gameState = ENDING;
			return;
		}

		if (_formDebug->button4->Enabled == false) {
			_animation->Say("I would like to propose a draw... do you accept it?");
			//_animation->Say("I would like to propose a draw... do you accept it?");
			_formDebug->button4->Enabled = true;
			return;
		}

		if (_gameState!= INIT){
			if (_formDebug->buttonRestart->Enabled == false) {
				_stateMachine->Reset();
				copyEBtoCE();
				_gameState = OPP_TURN;

				_formDebug->buttonRestart->Enabled = true;
				return;
			}
		}

		if (_gameState!= INIT){
			if (_formDebug->newGameBut->Enabled == false) {
				//beep to synchronize
				//		Beep(3000, 1000);
				//PlaySound(TEXT("ringout.wav"), NULL, SND_ALIAS | SND_APPLICATION);

				formStringTbox = (char *) (void*) Marshal::StringToHGlobalAnsi(_formDebug->tboxName->Text);
				companionName = (char *) (void*) Marshal::StringToHGlobalAnsi(_formDebug->tboxName->Text);
				oppName = (char *) (void*) Marshal::StringToHGlobalAnsi(_formDebug->tBoxOppName->Text);

				if (openorcreatefile(formStringTbox)!=0) { //start new game

					_animation->PlayAnimation((char *)letsPlayPath, ANIMATION_CHANNEL);	
					_expectedValue = 0;
					_prevExpectedValue = 0;
					_n = 2;
					_threshold = 0;
					_sensedValues.clear();
					_sensedValues.push_back(0); //o 1º sensed value é 0!
					_mismatchValues.clear();
					_mismatchValues.push_back(0);
					_iCatMood->setValence(0);

				}

				formStringTbox = (char *) (void*) Marshal::StringToHGlobalAnsi(_formDebug->tboxName->Text);

				formStringTbox = (char *) (void*) Marshal::StringToHGlobalAnsi(_formDebug->textBox5->Text);
				_chessConfiguration->setCastle(atoi(formStringTbox));

				_stateMachine->Reset();
				copyEBtoCE();
				_gameState = OPP_TURN;
				_formDebug->newGameBut->Enabled = true;
				return;
			}
		}

		if (_formDebug->button5->Enabled == false) {


			//SO PARA TESTAR O SayTo "better than I was..."

			//SpeechAct *teste = new SpeechAct("unexpected_r_comp");
			//teste->addContextVariable("user", "Samuel");
			//_languageEngine->say(*teste);

			//	this->SayTo(_languageEngine->say(*teste).c_str(), LEFT);
			this->SayTo("unexpected_r_comp", companionName, RIGHT);
			_formDebug->button5->Enabled = true;
			return;


			/*_formDebug->button5->Enabled = true;
			_endingAnimation = "\\STANDARD\\Actions\\Social\\empate2_en.raf";
			_animation->PlayAnimation(_endingAnimation.pszGetPointer(), ANIMATION_CHANNEL);
			_gameState = ENDING;
			return;*/
		}


		_iCatMood->updateEmotion();

		if (_iCatIsSpeaking!=0) {
			if (_speechTimeout >= 8) {
				//animaçao de voltar para o centro
				if (_iCatIsSpeaking == LEFT)
					getAnimationModule()->PlayAnimation((char *)leftToCenterPath, STATEM_CHANNEL);
				else
					getAnimationModule()->PlayAnimation((char *)rightToCenterPath, STATEM_CHANNEL);

				getStateMachine()->GetNextAnimation();
				_iCatIsSpeaking = 0;
				//return; TESTAR USTO ASSIM *****************************
				_speechTimeout = 0;
			}
			else
				_speechTimeout++;
		}



	} // vDoAction



	void ChessModule::stateInitBoard(){
		copyEBtoCE();


		/*if(_chessConfiguration->getSideToMove()!= _chessConfiguration->getIcatSide())
		_gameState=OPP_TURN;
		else
		_gameState=ICAT_TURN;*/
	}

	void ChessModule::stateInit(){
		_animation->Load(LIPSYNC_CHANNEL, "lipsync");
		_animation->Start(LIPSYNC_CHANNEL, 1);

		if (_chessConfiguration->getPersonality() != 0) {
			_animation->Load(MOOD_CHANNEL, (char*)moodBehaviourPath);
			_animation->Start(MOOD_CHANNEL, 1);

			_iCatMood->createGlobalVars();
		}

		_animation->MergeLogic("icat.Head.RightEyeLid", "c2>c1>c4>c3");
		_animation->MergeLogic("icat.Head.leftEyeLid", "c2>c1>c4>c3");


		if (_electronicBoard->stableBoard()) {
			_electronicBoard->sendClockMessage("....");
			Console::WriteLine("!!!!!!!!!!!!!!!!!!!!STABLE BOARD!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			//_animation->PlayAnimation((char *)letsPlayPath, ANIMATION_CHANNEL);
			_wait=-1;
			_boardPresent = true;
			_formDebug->newGameBut->Enabled = TRUE;
			_gameState = INITIAL_BOARD;
		}
		else
			_wait++;

		//não detectou o tabuleiro
		if (_wait >=50) {
			_boardPresent = false;
			_animation->PlayAnimation((char *)emotionalActionsPath[THINK], ANIMATION_CHANNEL);
			_gameState=INITIAL_BOARD;
			_wait=-1;
		}
	}

	//apenas para as jogadas em portugues
	char * prepareToSay(char *move, int size) {
		int i = 0;
		char *buffer;

		for (i = 0; i < size; i++){
			if (move[i] == 'a')
				move[i]='à';
			if (move[i] == 'e')
				move[i]='é';
		}

		buffer = (char*) malloc (size+1);

		buffer[0]=move[0];
		buffer[1]=move[1];
		buffer[2]=' ';
		buffer[3]='\0';
		strcat(buffer, move+2);

		if ((size > 3) && (buffer[5]=='q')) {
			buffer[5]=' ';
			strcat(buffer, "rainha");
		}

		return buffer;
	}

	void ChessModule::stateICatTurn(){


		Console::WriteLine("\n********\n*******\niCat Turn\n********\n******");
		if(_chessConfiguration->getSideToMove()== _chessConfiguration->getIcatSide()){
			_move_number++;
		}
		if (rand()%100 < _chessConfiguration->getRandomness()){
			_icat_move= _chessEngine->playRandom();
			_chessEngine->generateMoves();
			//_formDebug->label1->Text=gcnew System::String (_icat_move.pszGetPointer());
		}
		else{
			if (_icat_move == "")
				_icat_move = _chessEngine->play(MOVE_THINKING_TIME);

			//senao _icat_move ja tem a jogada (ver reactToMove)
		}

		_animation->Say(prepareToSay(_icat_move.pszGetPointer(), _icat_move.unLength()));


		_out << "\t" << _icat_move.pszGetPointer();
		//////////////////////////////////////////////*********ESTAMOS AQUI

		_formDebug->richTextBox1->Text += "\t" + gcnew System::String(_icat_move.pszGetPointer())+ "\t" + _electronicBoard->getPieceFromPosition(_icat_move.pszGetPointer()[2],_icat_move.pszGetPointer()[3]-48);
		_out << "\t" << _electronicBoard->getPieceFromPosition(_icat_move.pszGetPointer()[2],_icat_move.pszGetPointer()[3]-48);
		if(_boardPresent)
			_electronicBoard->sendClockMessage(_icat_move.pszGetPointer());
		//_animation->Say(_icat_move.pszGetPointer());
		_gameState = WAIT_FOR_MOVE;
	}


	void ChessModule::stateWaitForMove(){

		if (_iCatIsSpeaking == 0  && STATEMACHINEAVAILABLE==true) {
			_stateMachine->update(_gameState,_animation, STATEM_CHANNEL); 
		}
		if (_boardPresent){
			if (_electronicBoard->newMove()){
				if (!verifyMove()) {
					_chessEngine->takeBack();
					if(_move_number>1){
						_chessEngine->takeBack();
						if(verifyMove()){
							_animation->Say(_chessConfiguration->getRandomItem(_chessConfiguration->playAgainSent));
							_move_number--;
							_gameState=OPP_TURN;
							return;
						}
						//_formDebug->label1->Text=gcnew System::String (_last_move.pszGetPointer());
						_chessEngine->insertmove(_last_move.pszGetPointer());
					}

					if(!verifyMove()){ 
						if ((rand()%2) == 0) 
							_animation->Say(_chessConfiguration->getRandomItem(_chessConfiguration->illegalMoveSent));
						else 
							_animation->PlayAnimation(_chessConfiguration->getRandomItem(_chessConfiguration->illegalMoveAnim), ANIMATION_CHANNEL);
					}
					//_formDebug->label2->Text=gcnew System::String (_icat_move.pszGetPointer());
					_chessEngine->insertmove(_icat_move.pszGetPointer()-1);
				}	
				else {
					if (wonIcat()){
						_gameState=ENDING;
						return;
					}
					_gameState = OPP_TURN;
				}
			}
		}
		else
			_gameState = OPP_TURN;
	}


	void ChessModule::stateCompanionTurn(){

		Console::WriteLine("Companion Turn*");

		if(_chessConfiguration->getSideToMove() == _chessConfiguration->getIcatSide()){
			//Console::WriteLine("Move Number: " + _move_number);
			_move_number++;
		}

		if (_iCatIsSpeaking == 0  && STATEMACHINEAVAILABLE==true) {
			_stateMachine->update(_gameState,_animation, STATEM_CHANNEL); 
		}

		if(_boardPresent){
			if (_electronicBoard->newMove()){
				CAPTURED_PIECE = "";
				CAPTURED_PIECE += _chessEngine->getPieceFromPosition(_electronicBoard->getMove()[3],_electronicBoard->getMove()[4]-48);

				if (_chessEngine->insertmove(_electronicBoard->getMove())) {

					_last_move=_electronicBoard->getMove();
					if(wonIcat()){
						_gameState = ENDING;
					}
					else{
						//_lookAtBoardActionPath = _chessConfiguration->getRandomItem(_chessConfiguration->lookAtBoard);
						//_animation->PlayAnimation(_lookAtBoardActionPath, ANIMATION_CHANNEL);
						_wait = 6;
						_companionPlayedLastTurn = true;
						_gameState = THINK_BEFORE_MOVE;
					}
				}else {
					if (!verifyMove()){
						_animation->Say(_chessConfiguration->getRandomItem(_chessConfiguration->illegalMoveSent));
					}
				}
			}
		}
	}


	void ChessModule::stateOppTurn(){

		Console::WriteLine("Opponent Turn");

		if(_chessConfiguration->getSideToMove()!= _chessConfiguration->getIcatSide())
			_move_number++;

		if (_iCatIsSpeaking == 0 && STATEMACHINEAVAILABLE==true) {
			_stateMachine->update(_gameState,_animation, STATEM_CHANNEL);  
		}

		if(_boardPresent){
			if (_electronicBoard->newMove()){
				string printCapturedPiece ="";
				printCapturedPiece += _chessEngine->getPieceFromPosition(_electronicBoard->getMove()[3],_electronicBoard->getMove()[4]-48);


				if (_chessEngine->insertmove(_electronicBoard->getMove())) {
					_user_movenumber++;
					//codigo novo
					_out << "\n" << _user_movenumber << "\t" << currentTime() << "\t"<< _electronicBoard->getMove() << "\t" << printCapturedPiece;
					_formDebug->richTextBox1->Text += "\n" + _user_movenumber + "\t" + currentTime() + "\t" + gcnew System::String (_electronicBoard->getMove()) + "\t" + gcnew System::String (printCapturedPiece.c_str());
					///////////////////////////////////////////////////////////////////////

					_last_move=_electronicBoard->getMove();
					if(wonOpp())
						_gameState=ENDING;
					else{
						//_lookAtBoardActionPath = _chessConfiguration->getRandomItem(_chessConfiguration->lookAtBoard);
						//_animation->PlayAnimation(_lookAtBoardActionPath, ANIMATION_CHANNEL);
						//_formDebug->label2->Text=gcnew System::String (_lookAtBoardActionPath);
						_wait = 6;
						_gameState = THINK_BEFORE_MOVE;
						_companionPlayedLastTurn = false;
					}
				}
				else {
					if (!verifyMove()){
						_animation->Say(_chessConfiguration->getRandomItem(_chessConfiguration->illegalMoveSent));
					}
				}
			}
		}
	}

	void ChessModule::stateReactToMove(){
		if (_electronicBoard->newMove()){
			_electronicBoard->getMove();
			_chessEngine->takeBack();
			if(verifyMove()){
				_animation->Say(_chessConfiguration->getRandomItem(_chessConfiguration->playAgainSent));
				_move_number--;
				_gameState=OPP_TURN;
				return;
			}
			_chessEngine->insertmove(_last_move.pszGetPointer());
		}

		if (_wait == 6) {
			int action = reactToMove();
			STATEMACHINEAVAILABLE=false;
			_animation->PlayAnimation((char *)emotionalActionsPath[action], ANIMATION_CHANNEL);
			_currentEmotion = action;

			if(_companionPlayedLastTurn){
				_gameState = OPP_TURN;
			}else{
				_gameState = COMPANION_TURN;
			}
		}

		if (_wait==0){

			_wait = -1;

		}else{
			_wait--;
		}
	}


	void ChessModule::stateGameOver(){
		_animation->PlayAnimation("\\STANDARD\\Empathy\\Transitions\\Interactive2Sleep.raf", ANIMATION_CHANNEL);
		_gameState = -1;
	}



	gcroot <Form1^> ChessModule::getForm1(){
		return _formDebug;
	}

	//void ChessModule::SayTo(const char *utterance, int side) {
	void ChessModule::SayTo(const char *speechActType, const char *user, int side) {
		_iCatIsSpeaking = side;
		_speechTimeout = 0;
		time_t startSpeakingTime = time (NULL);
		if (side == LEFT) {
			_animation->PlayAnimation((char *)lookLeftStayTherePath, STATEM_CHANNEL);
		}
		else {
			_animation->PlayAnimation((char *)lookRightStayTherePath, STATEM_CHANNEL);
		}
		SpeechAct *sa = new SpeechAct(speechActType);
		sa->addContextVariable("user", user);
		_animation->Say(_languageEngine->say(*sa).c_str());
	}

	void ChessModule::commentGame() {

		//Express encouragement and emotivector and captured piece speech

		if ((_chessEngine->getCurrentSide()==0) && (CAPTURED_PIECE[0] != 'S') && (LastAction == STRONGER_R || LastAction == UNEXPECTED_R || LastAction == EXPECTED_R) && ((rand()%2)==0))
			this->SayTo("captured_piece", companionName, LEFT);
		else
			if (expressEncouragement(SensedValue)==false)
				expressVerballyEmotivector(LastAction);


		/*if(_companionPlayedLastTurn){
		_gameState = OPP_TURN;
		}else{
		_gameState = COMPANION_TURN;
		}*/
	}


	void ChessModule::OnAniModStatus(ChessModule * d){
		DMLString status = d->getAnimationModule()->GetStatus();
		//detectar quando parou de voltar ao centro dps de falar
		DMLString auxstr_left;
		auxstr_left="stopped ";
		auxstr_left+=STATEM_CHANNEL;
		auxstr_left+= " ";
		auxstr_left += d->leftToCenterPath;

		DMLString auxstr_right;
		auxstr_right="stopped ";
		auxstr_right+=STATEM_CHANNEL;
		auxstr_right+= " ";
		auxstr_right += d->rightToCenterPath;

		if (d->strCompare(status, auxstr_left) || d->strCompare(status, auxstr_right)){	
			d->getStateMachine()->GetNextAnimation();	
			if (d->getStateMachine()->getCurrentAnimation() != ""){
				d->getAnimationModule()->PlayAnimation(d->getStateMachine()->getCurrentAnimation().pszGetPointer(), STATEM_CHANNEL);
			}
			d->_iCatIsSpeaking = 0;
			//return;
		}

		DMLString aux_letsplay;
		aux_letsplay="stopped ";
		aux_letsplay+=ANIMATION_CHANNEL;
		aux_letsplay+= " ";
		aux_letsplay += d->letsPlayPath;

		if (d->strCompare(status, aux_letsplay)) {
			DMLString ola = "";
			ola += "Olá ";	
			ola += companionName;
			ola += ", e ";
			ola += oppName;
			ola += ". Desejo-vos um bom jogo.";
			d->_animation->Say(ola.pszGetPointer());
		}

		//detectar quando parou de falar
		if (d->strCompare(status, "icat.speech.events -3") && (d->_iCatIsSpeaking!=0)) {
			//animaçao de voltar para o centro
			if (d->_iCatIsSpeaking == LEFT)
				d->getAnimationModule()->PlayAnimation((char *)leftToCenterPath, STATEM_CHANNEL);
			else
				d->getAnimationModule()->PlayAnimation((char *)rightToCenterPath, STATEM_CHANNEL);
			return;
		}


		if (d->_iCatIsSpeaking!=0)
			return;
		//fim novo codigo

		if(d->_gameState==WAIT_FOR_MOVE || d->_gameState==OPP_TURN || d->_gameState == COMPANION_TURN){ //NEW: d->_gameState == COMPANION_TURN
			DMLString auxstr2;
			auxstr2="stopped ";
			auxstr2+=ANIMATION_CHANNEL;
			auxstr2+= " ";
			auxstr2 += emotionalActionsPath[LastAction];

			if (d->strCompare(status, auxstr2)) {
				d->commentGame();
				STATEMACHINEAVAILABLE = true;
			}


			DMLString auxstr;
			auxstr="stopped ";
			auxstr+=STATEM_CHANNEL;
			auxstr+= " ";
			auxstr += d->getStateMachine()->getCurrentAnimation();

			if (d->strCompare(status, auxstr)){	
				d->getStateMachine()->GetNextAnimation();	
				if (d->getStateMachine()->getCurrentAnimation() != ""){
					Console::WriteLine(d->getStateMachine()->getCurrentAnimation().pszGetPointer());
					d->getAnimationModule()->PlayAnimation(d->getStateMachine()->getCurrentAnimation().pszGetPointer(), STATEM_CHANNEL);
				}
			}
		} 

		if (d->_gameState == THINK_BEFORE_MOVE) {

			d->_gameState = REACT_TO_MOVE;
			/*DMLString auxstr;
			auxstr="stopped ";
			auxstr+=ANIMATION_CHANNEL;
			auxstr+= " ";
			auxstr += d->_lookAtBoardActionPath;

			if (d->strCompare(status, auxstr)) {
			if (d->_chessConfiguration->getPersonality() == 0) {
			//d->_icat_move = d->_chessEngine->play(MOVE_THINKING_TIME);
			//d->_gameState = ICAT_TURN;
			//d->_formDebug->richTextBox1->Text += gcnew System::String("sensed value:  " + sensed + "\n");
			}
			else {
			d->_gameState = REACT_TO_MOVE;
			}
			}*/
		}


		if (d->_gameState == ENDING ) {
			DMLString auxstr;
			auxstr="stopped ";
			auxstr+=ANIMATION_CHANNEL;
			auxstr+= " ";
			auxstr += d->_endingAnimation;

			if (d->strCompare(status, auxstr))
				d->_gameState = GAME_OVER;

		}
	}

	bool ChessModule::strCompare(DMLString str1, DMLString str2){
		System::String ^Sstr1;
		System::String ^Sstr2;
		Sstr1=gcnew System::String(str1.pszGetPointer());
		Sstr2=gcnew System::String(str2.pszGetPointer());
		return Sstr2->ToLower()->Equals(Sstr1->ToLower());
	}


	int ChessModule::calcEmotivector4(int delta, int expected, int sensed) {
		int reaction=THINK;

		//Console::WriteLine(L"delta: " + delta + "expected: " + (float)expected + ".  sensed: " + sensed);

		if (delta >= 0) {
			if (expected <= sensed) {
				reaction = STRONGER_R;
				_iCatMood->incValence(30);
			}
			else {
				reaction = WEAKER_R;
				_iCatMood->incValence(10);
			}
		}
		//punishments
		else if (delta < 0) {
			if (expected <= sensed) {
				reaction = WEAKER_P;
				_iCatMood->incValence(-40);
			}
			else {
				reaction = STRONGER_P;
				_iCatMood->incValence(-20);
			}
		}

		return reaction;
	}

	double ChessModule::calcLog(int base, int number) {
		return log ((double)number) / log ((double)base);
	}

	void ChessModule::calcChessMood(int sensed) {
		double resLog = 0;

		if (sensed > 0) 
			resLog = calcLog(10, sensed+1);
		else
			resLog = - calcLog(10, -sensed+1);

		if (_chessConfiguration->getPersonality() == 1) { //emoçoes random->mood random
			if ((rand()%2) == 0) {
				_iCatMood->setValence((-1)*rand()%9999);
			}
			else
				_iCatMood->setValence(rand()%9999);

			return;
		}

		if (resLog == 0) {
			_iCatMood->setValence(0);
		}
		else {
			_iCatMood->setValence((resLog/2)*100/2);
		}
	}


	void ChessModule::expressVerballyEmotivector(int reaction){
		string whatToSay;
		/*if(_chessEngine->getCurrentSide()== LEFT){
		if((rand()%5) >= 3){ //ATENÇAO
		return;
		}	
		}else{
		if((rand()%5) >= 2){
		return;
		}	
		}*/

		switch(reaction){
			case UNEXPECTED_R:
				if(_chessEngine->getCurrentSide()==0){	
					this->SayTo("unexpected_r_comp",companionName,LEFT);
				}else{
					this->SayTo("reward_opp",oppName,RIGHT);
				}break;
			case UNEXPECTED_P:
				if(_chessEngine->getCurrentSide()==0){
					this->SayTo("unexpected_p_comp",companionName,LEFT);
				}else{
					this->SayTo("punishment_opp",oppName,RIGHT);
				}break;
			case EXPECTED_R:
				if(_chessEngine->getCurrentSide()==0){
					this->SayTo("expected_r_comp",companionName,LEFT);
				}else{
					this->SayTo("reward_opp",oppName,RIGHT);
				}break;
			case EXPECTED_P:
				if(_chessEngine->getCurrentSide()==0){
					this->SayTo("expected_p_comp",companionName,LEFT);
				}else{
					this->SayTo("punishment_opp",oppName,RIGHT);
				}break;
			case STRONGER_R:
				if(_chessEngine->getCurrentSide()==0){
					this->SayTo("stronger_r_comp",companionName,LEFT);
				}else{
					this->SayTo("reward_opp",oppName,RIGHT);
				}break;
			case STRONGER_P:
				if(_chessEngine->getCurrentSide()==0){
					this->SayTo("stronger_p_comp",companionName,LEFT);
				}else{
					this->SayTo("punishment_opp",oppName,RIGHT);
				}break;
			case WEAKER_P:
				if(_chessEngine->getCurrentSide()==0){
					this->SayTo("weaker_p_comp",companionName,LEFT);
				}else{
					this->SayTo("reward_opp",oppName,RIGHT);
				}break;

			case WEAKER_R:
				if(_chessEngine->getCurrentSide()==0){
					this->SayTo("weaker_r_comp",companionName,LEFT);
				}else{
					this->SayTo("punishment_opp",oppName,RIGHT);
				}break;
		}
	}



	int ChessModule::calcEmotivector9(int delta, int expected, int sensed, int threshold) {

		int reaction=THINK;

		calcChessMood(sensed);

		//_formDebug->richTextBox1->Text += gcnew System::String("threshold:  " + threshold + "\n");
		_formDebug->richTextBox1->Text += "\t";
		_out << "\t";

		//negligible
		if ((delta < 3) && (delta > -3)) {
			if (sensed > (expected + threshold)) {
				reaction = UNEXPECTED_R;
				_formDebug->richTextBox1->Text += gcnew System::String("UNEXPECTED_R");	
				_out << "UNEXPECTED_R";  
			} else if (sensed < (expected - threshold)) {
				reaction = UNEXPECTED_P;
				_formDebug->richTextBox1->Text += gcnew System::String("UNEXPECTED_P");
				_out << "UNEXPECTED_P";
			} else {
				_formDebug->richTextBox1->Text += gcnew System::String("NEGLIGIBLE");
				reaction = THINK;
				_out << "NEGLIGIBLE";
			}
		}
		else if (delta > 0) {  //expected R
			if (sensed > (expected + threshold)) {
				reaction = STRONGER_R;
				//_iCatMood->incValence(30);
				_formDebug->richTextBox1->Text += gcnew System::String("STRONGER_R");
				_out << "STRONGER_R";
			}
			else if (sensed < (expected - threshold)) {
				reaction = WEAKER_R;
				//_iCatMood->incValence(10);
				_formDebug->richTextBox1->Text += gcnew System::String("WEAKER_R");
				_out << "WEAKER_R";
			} else {
				reaction = EXPECTED_R;
				//_iCatMood->incValence(20);
				_formDebug->richTextBox1->Text += gcnew System::String("EXPECTED_R");
				_out << "EXPECTED_R";
			}
		}
		else if (delta < 0) {  //expected P
			if (sensed > (expected + threshold)) {
				reaction = WEAKER_P;
				//_iCatMood->incValence(-10);
				_formDebug->richTextBox1->Text += gcnew System::String("WEAKER_P");
				_out << "WEAKER_P";
			}
			else if (sensed < (expected - threshold)) {
				reaction = STRONGER_P;
				//_iCatMood->incValence(-30);
				_formDebug->richTextBox1->Text += gcnew System::String("STRONGER_P");
				_out << "STRONGER_P";
			} else {
				reaction = EXPECTED_P;
				//_iCatMood->incValence(-20);
				_formDebug->richTextBox1->Text += gcnew System::String("EXPECTED_P");
				_out << "EXPECTED_P";
			}
		}
		//_formDebug->richTextBox1->Text += gcnew System::String("delta:" + delta + " expected:" + (float)expected + " sensed:" + sensed + "\n");
		_formDebug->richTextBox1->Text += "\t\t" + sensed + "\t" + _iCatMood->getValence();
		// _out << "\t\t" << sensed << "\t" << _iCatMood->getValence();
		return reaction;
	}

	int ChessModule::movingAverages (vector<int> sensedValues, int sensed, int prevExpectedValue, int n) {
		int expected = 0;
		float S = 2/(1+_n);

		if (sensedValues.size() <= n) {
			vector<int>::iterator i;
			int k = 0;
			float sum = 0;
			for(i =sensedValues.begin(); i != sensedValues.end(); ++i) {
				k++;
				sum = sum + *i;
			}
			expected = sum/k;
		}
		else {
			expected = sensed * S + (prevExpectedValue * (1 - S));
		}

		return expected;
	}


	bool ChessModule::expressEncouragement(int sensed){

		if(_alreadySpoke){
			_alreadySpoke = false; //Reset this flag;
			return false;
		}
		if(sensed > 1000 && !alreadySpokeEncouragement[0]){
			this->SayTo("encouragement_winning_alot",companionName,LEFT);
			alreadySpokeEncouragement[0] = true;
			_alreadySpoke = true;
			return true;
		}

		if(sensed > 400 && !alreadySpokeEncouragement[1]){
			this->SayTo("encouragement_winning",companionName,LEFT);
			alreadySpokeEncouragement[1] = true;
			_alreadySpoke = true;
			return true;
		}

		if(sensed < -1000 && !alreadySpokeEncouragement[2]){
			this->SayTo("encouragement_loosing_alot",companionName,LEFT);
			alreadySpokeEncouragement[2] = true;
			_alreadySpoke = true;
			return true;
		}

		if(sensed < -400 && !alreadySpokeEncouragement[3]){
			this->SayTo("encouragement_loosing",companionName,LEFT);
			alreadySpokeEncouragement[3] = true;
			_alreadySpoke = true;
			return true;
		}
		return false;
	}

	int ChessModule::reactToMove()
	{

		float expected=0;
		float sensed=0;
		int reaction = THINK;

		//for any approximation

		sensed = _chessEngine->genHeurValue(MOVE_THINKING_TIME);

		//_chessEngine->takeBack();

		if (_chessConfiguration->getCalcExpValue() ==  MOVINGAVERAGES_APR) //moving averages
			_sensedValues.push_back(sensed); 

		//aproximaçao "moving averages"
		if (_chessConfiguration->getCalcExpValue() ==  MOVINGAVERAGES_APR) {
			int s=0;
			if (_sensedValues.size() >= 2)
				s = _sensedValues.at(_sensedValues.size()-2);
			expected = this->movingAverages(_sensedValues, /*sensed*/s, _prevExpectedValue, _n);
		}

		if (expected > 9999)
			expected = 9999;
		else
			if (expected < -9999)
				expected = -9999;

		int delta = expected - _prevExpectedValue;

		//calcular o threshold
		_mismatchValues.push_back(abs(expected - sensed));
		int threshold = this->movingAverages(_mismatchValues, abs(expected - sensed), _threshold, _n);
		_threshold = threshold;

		if (_chessConfiguration->getSensationModel() == EMOTIVECTOR4) {
			reaction = calcEmotivector4(delta, expected, sensed);
		}
		else { //EMOTIVECTOR9
			reaction = calcEmotivector9(delta, expected, sensed, threshold);
		}

		if (_chessConfiguration->getCalcExpValue() == YOANDRE_APR) /*|| (_chessConfiguration->getCalcExpValue() == MOVINGAVERAGES_APR))*/ //approach nº 1 e moving averages
			_sensedValues.push_back(sensed);
		else
			_expectedValue = sensed;  //simples 


		_prevExpectedValue = expected;

		SensedValue=sensed;
		LastAction= reaction;
		return reaction;
	} //reactToMove


	bool ChessModule::wonOpp()
	{
		if (_chessEngine->gameResult()==0)
			return false;

		else if (_chessEngine->gameResult()==1)
			_endingAnimation = _chessConfiguration->getRandomItem(_chessConfiguration->drawAnim);
		//_animation->Say("We Are Drawn by Stalemate!");
		else if (_chessEngine->gameResult()==2)
			_endingAnimation = _chessConfiguration->getRandomItem(_chessConfiguration->losingAnim);
		//_animation->Say("Congratualations, you Won!");
		else if (_chessEngine->gameResult()==3)
			_endingAnimation = _chessConfiguration->getRandomItem(_chessConfiguration->drawAnim);
		//_animation->Say("Draw By Repetition!");

		_animation->PlayAnimation(_endingAnimation.pszGetPointer(), ANIMATION_CHANNEL);

		return true;
	}

	bool ChessModule::wonIcat()
	{

		if (_chessEngine->gameResult()==0){
			if(_chessEngine->check()){
				_animation->Say(_chessConfiguration->getStringFromList(0, _chessConfiguration->checkSent));
			}
			//else{
			//if ((rand()%2) == 0) 
			//	_animation->Say(_chessConfiguration->getRandomItem(_chessConfiguration->confirMoveSent));
			//else
			//	_animation->PlayAnimation(_chessConfiguration->getRandomItem(_chessConfiguration->confirMoveAnim), ANIMATION_CHANNEL);
			/*int dice = rand()%TEXT_CONF_MOVE_SIZE;
			_animation->Say(textConfirmMove[dice]);*/
			//}
			return false;
		}

		else if (_chessEngine->gameResult()==1) {
			_endingAnimation = _chessConfiguration->getRandomItem(_chessConfiguration->drawAnim);
		}
		//_animation->Say("It's a draw, Stalemate!");
		else if (_chessEngine->gameResult()==2) {
			_endingAnimation = _chessConfiguration->getRandomItem(_chessConfiguration->winingAnim);
		}
		//_animation->Say("Checkmate!");
		else if (_chessEngine->gameResult()==3) {
			_endingAnimation = _chessConfiguration->getRandomItem(_chessConfiguration->drawAnim);
		}

		_animation->PlayAnimation(_endingAnimation.pszGetPointer(), ANIMATION_CHANNEL);

		//_animation->Say("Draw By Repetition!");
		return true;
	}


	bool ChessModule::verifyMove() {
		unsigned char * electronicB = _electronicBoard->getBoard(); 
		int * engineB = _chessEngine->getboardpos(); 
		int * engineBColor = _chessEngine->getboardcolor();

		char piece_char_B[7] = {'p', 'n', 'b', 'r', 'q', 'k','S'};
		int i=0, j=0;

		for(i=0;i<8;i++){
			for(j=0;j<8;j++){
				if (engineBColor[i*8+j]==0){  //0 = white
					if(piece_char[engineB[i*8+j]]!= Piececode[electronicB[i*8+j]])
					{
						break;
					}
				}
				else if(piece_char_B[engineB[i*8+j]]!=Piececode[electronicB[i*8+j]])  //black
				{
					break;
				}
			}
			if (j!=8)
				break;
		}
		_electronicBoard->getMove(); //discard this move
		if(i*8+j!=72)
		{
			return false;
		}
		return true;
	}


	void ChessModule::copyEBtoCE() {
		unsigned char * electronicB = _electronicBoard->getBoard(); 
		int engineB[64]; 
		int engineBColor[64];

		char piece_char_B[7] = {'p', 'n', 'b', 'r', 'q', 'k','S'};
		int i=0, j=0,z=0;

		for(i=0;i<8;i++){
			for(j=0;j<8;j++){

				for(z=0; z < 7; z++){
					if(piece_char[z]== Piececode[electronicB[i*8+j]]) //white
					{
						if(z==6){
							engineB[i*8+j]=z;
							engineBColor[i*8+j]=6;
							break;
						}
						else{
							engineB[i*8+j]=z;
							engineBColor[i*8+j]=0;
							z=7;
							break;
						}
					}
					else
						if(piece_char_B[z]== Piececode[electronicB[i*8+j]]) //white
						{
							if(z==6){
								engineB[i*8+j]=z;
								engineBColor[i*8+j]=6;
								break;
							}
							else{
								engineB[i*8+j]=z;
								engineBColor[i*8+j]=1;
								z=7;
								break;
							}
						}

				}
				//_formDebug->richTextBox1->Text += gcnew System::String(""+engineBColor[i*8+j]);
			}
			//_formDebug->richTextBox1->Text += gcnew System::String("\n");
		}
		_chessEngine->putposition(engineB, engineBColor, _chessConfiguration->getSideToMove(), _chessConfiguration->getCastle()); 

		//_formDebug->richTextBox1->Text += gcnew System::String("NO CHESS ENGINE: \n");
		int * engineBoard = _chessEngine->getboardcolor(); 
		for(i=0;i<8;i++){
			for(j=0;j<8;j++){
				//_formDebug->richTextBox1->Text += gcnew System::String(""+engineBoard[i*8+j]);
			}
			//_formDebug->richTextBox1->Text += gcnew System::String("\n");
		}
		/*if (verifyMove()) {
		_formDebug->richTextBox1->Text += gcnew System::String("Deu bem.\n");
		}
		else
		_formDebug->richTextBox1->Text += gcnew System::String("Nada\n");*/
	}


	//returns 1 if User didn't exist, 2 if it did and wants new game, 0 if a bug ocurred
	int ChessModule::openorcreatefile(char * playerName) {
		ULARGE_INTEGER uli;
		ifstream _inp;
		string myFileName;
		String^ message;
		String^ caption;


		/*_inp.open(myFileName.c_str(), ifstream::in);
		_inp.close();
		if(_inp.fail())
		{
		_inp.clear(ios::failbit);*/
		if (!_out.is_open()){
			GetSystemTime(&_st);
			SystemTimeToFileTime( &_st, &_fileTime );
			uli.LowPart = _fileTime.dwLowDateTime;
			uli.HighPart = _fileTime.dwHighDateTime;
			ULONGLONG systemTimeIn_ms( uli.QuadPart/10000 );
			_globSystemTimeIn_ms = systemTimeIn_ms;

			if (_fileCreated == false)
			{
				char * temp;
				temp = (char *) (void*) Marshal::StringToHGlobalAnsi(gcnew System::String(gcnew System::String(playerName) + "_" + _st.wDay + "-" + _st.wMonth + "-" + _st.wYear + "_" + _st.wHour + "-" + _st.wMinute + "-" + _st.wSecond + ".txt"));
				myFileName = temp;
			}

			_out.open(myFileName.c_str(), ofstream::out);
			_fileCreated = true;
			_formDebug->richTextBox1->Text += "Nº" + "\t" + "Time" + "\t" + "User" + "\t" + "Take" + "\t" + "Emotivector" + "\t\t" + "Eval" + "\t" + "Mood" + "\t" + "iCat" + "\t" + "Take";
			_out << "Nº" << "\t" << "Time" << "\t" << "User" << "\t" << "Take" << "\t" << "Emotivector" << "\t\t" << "Eval" << "\t" << "Mood" << "\t" << "iCat" << "\t" << "Take";
		}
		/*}
		else
		{
		message = "This user already has a file. Start a new line(game)?";
		caption = "New game?";
		MessageBoxButtons buttons = MessageBoxButtons::YesNo;
		System::Windows::Forms::DialogResult result;
		result = MessageBox::Show(message, caption, buttons);
		if ( result == ::DialogResult::Yes )
		{
		// Closes the parent form.
		return 2;
		}
		else {
		return 0;
		}
		}*/
		return 1;
	}

	ULONGLONG ChessModule::currentTime() {
		GetSystemTime(&_st);
		SystemTimeToFileTime( &_st, &_fileTime );
		ULARGE_INTEGER uli;
		uli.LowPart = _fileTime.dwLowDateTime;
		uli.HighPart = _fileTime.dwHighDateTime;
		ULONGLONG temp_ulong( uli.QuadPart/10000 );
		temp_ulong = temp_ulong - _globSystemTimeIn_ms;
		return temp_ulong;
	}
