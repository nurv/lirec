#include "stdafx.h"
#include <string>
#include <ctype.h>
#include "stdio.h"
#using <mscorlib.dll>

using namespace System;
using namespace System::Runtime::InteropServices;

#define STRONGER_R 0
#define WEAKER_R 1
#define STRONGER_P 2
#define WEAKER_P 3
#define THINK 4
#define EXPECTED_R 5
#define UNEXPECTED_R 6
#define UNEXPECTED_P 7
#define EXPECTED_P 8

#define ACTIONS_PATH_SIZE 9
const char *ChessModule::emotionalActionsPath[] = {"\\STANDARD\\Actions\\Emotional\\Excited.raf",
"\\STANDARD\\Actions\\Emotional\\Happy_Iolanda.raf",
"\\STANDARD\\Actions\\Emotional\\Scared.raf",
"\\STANDARD\\Actions\\Emotional\\Weaker_P.raf",
"\\STANDARD\\Actions\\Functional\\Think.raf",
"\\STANDARD\\Actions\\Emotional\\Expected_R.raf",
"\\STANDARD\\Actions\\Emotional\\Arrogant.raf",
"\\STANDARD\\Actions\\Emotional\\Unexpected_P.raf",
"\\STANDARD\\Actions\\Emotional\\Angry.raf"};

const char *ChessModule::moodBehaviourPath = "\\STANDARD\\CatMood.rbf";

const char *ChessModule::letsPlayPath = "\\STANDARD\\Actions\\Social\\vamosJogar.raf";
const char *ChessModule::giveUpPath = "\\STANDARD\\Actions\\Social\\desisto.raf";


IntPtr firstPtr;
char* formStringTbox;
const __wchar_t * str1;

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

	_electronicBoard = new ElectronicBoard(_chessConfiguration->getPort());

	_chessEngine = new ChessEngine();
	//_chessEngine->thinkmoves(_chessConfiguration->getSearchDepth());
	_chessEngine->thinktime(10);

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

	fStart ();
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
		_endingAnimation = "\\STANDARD\\Actions\\Social\\empate.raf";
		_animation->PlayAnimation(_endingAnimation.pszGetPointer(), ANIMATION_CHANNEL);
		_gameState = ENDING;
		return;
	}

		if (_formDebug->buttdesisto->Enabled == false) {
		_formDebug->buttdesisto->Enabled = true;
			//_animation->Say("Desisto, sei que me vais ganhar... Parabéns!");
		_endingAnimation = "\\STANDARD\\Actions\\Social\\desisto.raf";
		_animation->PlayAnimation(_endingAnimation.pszGetPointer(), ANIMATION_CHANNEL);
		_gameState = ENDING;
		return;
	}
	
	if (_formDebug->button4->Enabled == false) {
		_animation->Say("Proponho um empate.");
		//_animation->Say("I would like to propose a draw... do you accept it?");
		_formDebug->button4->Enabled = true;
		return;
	}

	if (_formDebug->newGameBut->Enabled == false) {
		
		formStringTbox = (char *) (void*) Marshal::StringToHGlobalAnsi(_formDebug->textBox3->Text);
		_chessConfiguration->setSideToMove(atoi(formStringTbox));
		formStringTbox = (char *) (void*) Marshal::StringToHGlobalAnsi(_formDebug->textBox4->Text);
		_chessConfiguration->setIcatSide(atoi(formStringTbox));
		formStringTbox = (char *) (void*) Marshal::StringToHGlobalAnsi(_formDebug->textBox5->Text);
		_chessConfiguration->setCastle(atoi(formStringTbox));
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

		_gameState = INITIAL_BOARD;
		_formDebug->newGameBut->Enabled = true;
		return;
	}

	if (_formDebug->button5->Enabled == false) {
		_formDebug->button5->Enabled = true;
		_endingAnimation = "\\STANDARD\\Actions\\Social\\empate2.raf";
		_animation->PlayAnimation(_endingAnimation.pszGetPointer(), ANIMATION_CHANNEL);
		_gameState = ENDING;
		return;
	}


	_iCatMood->updateEmotion();

} // vDoAction

void ChessModule::stateInitBoard(){
	/*if(_boardPresent){
		if(verifyMove())
			_gameState=ICAT_TURN;
	}
	else{
		_gameState=ICAT_TURN;
	}*/
	copyEBtoCE();

	if(_chessConfiguration->getSideToMove()!= _chessConfiguration->getIcatSide())
		_gameState=OPP_TURN;
	else
		_gameState=ICAT_TURN;
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
		_animation->PlayAnimation((char *)letsPlayPath, ANIMATION_CHANNEL);
		_wait=-1;
		_boardPresent = true;
		_gameState = INITIAL_BOARD;
	}
	else
		_wait++;

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
	

	if(_chessConfiguration->getSideToMove()== _chessConfiguration->getIcatSide())
		_move_number++;
	if (rand()%100 < _chessConfiguration->getRandomness()){
		_icat_move= _chessEngine->playRandom();
		_chessEngine->generateMoves();
		_formDebug->label1->Text=gcnew System::String (_icat_move.pszGetPointer());
	}
	else{
		if (_icat_move == "")
			_icat_move = _chessEngine->play();

		//senao _icat_move ja tem a jogada (ver reactToMove)
	}

	_formDebug->textBox1->Text=gcnew System::String (_icat_move.pszGetPointer());



	_animation->Say(prepareToSay(_icat_move.pszGetPointer(), _icat_move.unLength()));
	  if(_boardPresent)
	_electronicBoard->sendClockMessage(_icat_move.pszGetPointer());
	//_animation->Say(_icat_move.pszGetPointer());
	_gameState = WAIT_FOR_MOVE;
}


void ChessModule::stateWaitForMove(){
	_stateMachine->update(_gameState,_animation, STATEM_CHANNEL); 
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
					_formDebug->label1->Text=gcnew System::String (_last_move.pszGetPointer());
					_chessEngine->insertmove(_last_move.pszGetPointer());
				}

				if(!verifyMove()){ 
					if ((rand()%2) == 0) 
						_animation->Say(_chessConfiguration->getRandomItem(_chessConfiguration->notMyMoveSent));
					else 
						_animation->PlayAnimation(_chessConfiguration->getRandomItem(_chessConfiguration->notMyMoveAnim), ANIMATION_CHANNEL);
				}
				_formDebug->label2->Text=gcnew System::String (_icat_move.pszGetPointer());
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

void ChessModule::stateOppTurn(){
	if(_chessConfiguration->getSideToMove()!= _chessConfiguration->getIcatSide())
		_move_number++;
	_stateMachine->update(_gameState,_animation, STATEM_CHANNEL);  
	if(_boardPresent){
		if (_electronicBoard->newMove()){
			if (_chessEngine->insertmove(_electronicBoard->getMove())) {
				_last_move=_electronicBoard->getMove();
				if(wonOpp())
					_gameState=ENDING;
				else{
					_lookAtBoardActionPath = _chessConfiguration->getRandomItem(_chessConfiguration->lookAtBoard);
					_animation->PlayAnimation(_lookAtBoardActionPath, ANIMATION_CHANNEL);
					//_formDebug->label2->Text=gcnew System::String (_lookAtBoardActionPath);
					_wait = 6;
					_gameState = THINK_BEFORE_MOVE;
				}
			}
			else {
				if (!verifyMove()){
					if ((rand()%2) == 0) 
						_animation->Say(_chessConfiguration->getRandomItem(_chessConfiguration->illegalMoveSent));
					else 
						_animation->PlayAnimation(_chessConfiguration->getRandomItem(_chessConfiguration->illegalMoveAnim), ANIMATION_CHANNEL);
				}
			}
		}
	}
	else {
		oppTurnDebug();
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

		if (_chessConfiguration->getPersonality() == 1) { //emoçoes random
			int tmp;
			while ((tmp = rand()%ACTIONS_PATH_SIZE) == action) {}
			action = tmp;
		}
		_animation->PlayAnimation((char *)emotionalActionsPath[action], ANIMATION_CHANNEL);
		_currentEmotion = action;
	}

	if (_wait==0) {
		_wait = -1;
		_gameState = ICAT_TURN;
	}
	else
		_wait--;

}

void ChessModule::stateGameOver(){
	_animation->PlayAnimation("\\STANDARD\\ModeTransition\\Interactive2Sleep.raf", ANIMATION_CHANNEL);
	_gameState = -1;
}

gcroot <Form1^> ChessModule::getForm1(){
	return _formDebug;
}

void ChessModule::oppTurnDebug() {
	if (_formDebug->button1->Enabled == false)
	{
		formStringTbox = (char *) (void*) Marshal::StringToHGlobalAnsi(_formDebug->textBox2->Text);
		if (_chessEngine->insertmove(formStringTbox-1))
		{
			if(wonOpp())
				_gameState=ENDING;
			else{
				_lookAtBoardActionPath = _chessConfiguration->getRandomItem(_chessConfiguration->lookAtBoard);
				_animation->PlayAnimation(_lookAtBoardActionPath, ANIMATION_CHANNEL);
				_wait = 6;
				_gameState = THINK_BEFORE_MOVE;
			}
		}
		else{
				if ((rand()%2) == 0) 
						_animation->Say(_chessConfiguration->getRandomItem(_chessConfiguration->illegalMoveSent));
					else 
						_animation->PlayAnimation(_chessConfiguration->getRandomItem(_chessConfiguration->illegalMoveAnim), ANIMATION_CHANNEL);
		}
		_formDebug->button1->Enabled=true;
		_formDebug->textBox2->Enabled=true;
	}
}

void ChessModule::OnAniModStatus(ChessModule * d){
	DMLString status = d->getAnimationModule()->GetStatus();

	if(d->_gameState==WAIT_FOR_MOVE || d->_gameState==OPP_TURN){
		DMLString auxstr;
		auxstr="stopped ";
		auxstr+=STATEM_CHANNEL;
		auxstr+= " ";
		auxstr += d->getStateMachine()->getCurrentAnimation();

		if (d->strCompare(status, auxstr)){	
			d->getStateMachine()->GetNextAnimation();	
			if (d->getStateMachine()->getCurrentAnimation() != ""){
				d->getAnimationModule()->PlayAnimation(d->getStateMachine()->getCurrentAnimation().pszGetPointer(), STATEM_CHANNEL);
			}
		}
	} 

	if (d->_gameState == THINK_BEFORE_MOVE) {
		DMLString auxstr;
		auxstr="stopped ";
		auxstr+=ANIMATION_CHANNEL;
		auxstr+= " ";
		auxstr += d->_lookAtBoardActionPath;

		if (d->strCompare(status, auxstr)) {
			if (d->_chessConfiguration->getPersonality() == 0) {
				d->_icat_move = d->_chessEngine->play();
				d->_gameState = ICAT_TURN;
				float sensed =d->_chessEngine->getvalue();
				d->_formDebug->richTextBox1->Text += gcnew System::String("sensed value:  " + sensed + "\n");
			}
			else {
				d->_gameState = REACT_TO_MOVE;
			}
		}
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

	Console::WriteLine(L"delta: " + delta + "expected: " + (float)expected + ".  sensed: " + sensed);

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

int ChessModule::calcEmotivector9(int delta, int expected, int sensed, int threshold) {

	int reaction=THINK;

	calcChessMood(sensed);

	_formDebug->richTextBox1->Text += gcnew System::String("threshold:  " + threshold + "\n");

	//negligible
	if ((delta < 3) && (delta > -3)) {
		if (sensed > (expected + threshold)) {
			reaction = UNEXPECTED_R;
			_formDebug->richTextBox1->Text += gcnew System::String("UNEXPECTED_R  ");
		} else if (sensed < (expected - threshold)) {
			reaction = UNEXPECTED_P;
			_formDebug->richTextBox1->Text += gcnew System::String("UNEXPECTED_P  ");
		} else {
			_formDebug->richTextBox1->Text += gcnew System::String("THINK  ");
			reaction = THINK;
		}
	}
	else if (delta > 0) {  //expected R
		if (sensed > (expected + threshold)) {
			reaction = STRONGER_R;
			//_iCatMood->incValence(30);
			_formDebug->richTextBox1->Text += gcnew System::String("STRONGER_R  ");
		}
		else if (sensed < (expected - threshold)) {
			reaction = WEAKER_R;
			//_iCatMood->incValence(10);
			_formDebug->richTextBox1->Text += gcnew System::String("WEAKER_R  ");
		} else {
			reaction = EXPECTED_R;
			//_iCatMood->incValence(20);
			_formDebug->richTextBox1->Text += gcnew System::String("EXPECTED_R  ");
		}
	}
	else if (delta < 0) {  //expected P
		if (sensed > (expected + threshold)) {
			reaction = WEAKER_P;
			//_iCatMood->incValence(-10);
			_formDebug->richTextBox1->Text += gcnew System::String("WEAKER_P  ");
		}
		else if (sensed < (expected - threshold)) {
			reaction = STRONGER_P;
			//_iCatMood->incValence(-30);
			_formDebug->richTextBox1->Text += gcnew System::String("STRONGER_P  ");
		} else {
			reaction = EXPECTED_P;
			//_iCatMood->incValence(-20);
			_formDebug->richTextBox1->Text += gcnew System::String("EXPECTED_P  ");
		}
	}
	_formDebug->richTextBox1->Text += gcnew System::String("delta:" + delta + " expected:" + (float)expected + " sensed:" + sensed + "\n");

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

int ChessModule::reactToMove()
{

	float expected=0;
	float sensed=0;
	int reaction = THINK;

	//for any approximation
	_icat_move = _chessEngine->play();
	sensed =_chessEngine->getvalue();
	//_chessEngine->takeBack();

	if (_chessConfiguration->getCalcExpValue() ==  MOVINGAVERAGES_APR) //moving averages
		_sensedValues.push_back(sensed); 

	//aproximaçao simples
	if (_chessConfiguration->getCalcExpValue() ==  SIMPLE_APR) {
		expected = _expectedValue;
	}

	//aproximaçao nº1
	if (_chessConfiguration->getCalcExpValue() ==  YOANDRE_APR) {
		vector<int>::iterator i;
		int prev = 0, k = 0;
		float aux = 0;
		for(i =_sensedValues.begin(); i != _sensedValues.end(); ++i) {
			k++;
			if (i == _sensedValues.begin()) {
				prev = *i;
				continue; //o 1º n faz nada
			}
			aux = aux + ((*i) - prev);
			prev = *i;
		}

		expected = prev + aux/k;
	}

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
		if(_chessEngine->check())
			_animation->Say(_chessConfiguration->getStringFromList(0, _chessConfiguration->checkSent));
			//_animation->Say("Check!");
		else{
			if ((rand()%2) == 0) 
				_animation->Say(_chessConfiguration->getRandomItem(_chessConfiguration->confirMoveSent));
			else
				_animation->PlayAnimation(_chessConfiguration->getRandomItem(_chessConfiguration->confirMoveAnim), ANIMATION_CHANNEL);
			/*int dice = rand()%TEXT_CONF_MOVE_SIZE;
			_animation->Say(textConfirmMove[dice]);*/
		}
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