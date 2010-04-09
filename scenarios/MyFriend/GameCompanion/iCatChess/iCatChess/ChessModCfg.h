#pragma once
#include <vector>
using namespace std;

typedef vector<char *> SentencesList, AnimationsList;

class ChessModCfg
{
	char * languageFile;

	int personality;
	int sensationModel;
	int calcExpValue;

	int randomness;
	int searchDepth;
	int sidetomove;
	int icatside;
	int castle;

	int port;

public:
		
	SentencesList confirMoveSent, notMyMoveSent, illegalMoveSent, playAgainSent, checkSent;
	AnimationsList confirMoveAnim, notMyMoveAnim, illegalMoveAnim, winingAnim, losingAnim, drawAnim, lookAtBoard;

	ChessModCfg(void);
	~ChessModCfg(void);

	void toString();
	void setStringVar(char **var, const char *w);
	void addStringToList(const char *o, vector <char *> * sl);
	//bool isPresent(const char *o);
	//int getObjectsCount() { return (int)objectsPresent.size(); }
	char *getStringFromList(int i, vector <char *> list) ;
	char *getRandomItem(vector <char *> sl);
	void setLanguageFile(const char *l) { setStringVar(&languageFile, l); }
	void setSensationModel(int value);
	void setCalcExpValue(int value);
	void setRandomness(int value);
	void setSearchDepth(int value);
	void setPersonality(int value);
	void setPort(int value);
	void setIcatSide(int value);
	void setSideToMove(int value);
	void setCastle(int value);
	char *getLanguageFile(){ return languageFile; }
	int getSensationModel();
	int getCalcExpValue();
	int getRandomness();
	int getSearchDepth();
	int getPersonality();
	int getPort();
	int getSideToMove();
	int getIcatSide();
	int getCastle();
	int loadFromFile(const char* filename);
	int loadFromFile();

};
