#include "stdafx.h"
#define	_CRT_SECURE_NO_WARNINGS  // due to annoying warnings for strcpy unsafeness (strcpy_s)
//#ifdef _CRT_SECURE_CPP_OVERLOAD_STANDARD_NAMES
//#undef _CRT_SECURE_CPP_OVERLOAD_STANDARD_NAMES
//#endif
//#define _CRT_SECURE_CPP_OVERLOAD_STANDARD_NAMES 1
#include "ChessModCfg.h"
#include "XML\XMLFileChessModCfgReader.h"
#include "XML\XMLLanguageCfgReader.h"
#include <string.h>

ChessModCfg::ChessModCfg(void) {
	//objectsPresent.clear();
	languageFile=NULL;
	setLanguageFile("");
	setSensationModel(9);
	setCalcExpValue(2);
}

ChessModCfg::~ChessModCfg(void) {
	setLanguageFile(NULL);
}

void ChessModCfg::setStringVar(char **var, const char *w) {
	if((*var) != NULL) free((*var));
	(*var) = NULL;
	if(w==NULL)	return;
	if(NULL == ((*var) = (char*)malloc(strlen(w)+1)))
		return;
	strcpy((*var), w);
}

void ChessModCfg::addStringToList(const char *o, vector <char *> * sl) {
	char *tmp;
	if(o==NULL) return;
	tmp = (char*)malloc(strlen(o) + 1);
	strcpy(tmp, o);
	sl->push_back(tmp);
}

/*bool ScenarioDefinition::isPresent(const char *o) {
	for(ObjectIDList::iterator Iter = objectsPresent.begin(); Iter != objectsPresent.end(); Iter++ )
		if(_stricmp((*Iter), o)==0) return true;
	return false;
}*/

char *ChessModCfg::getStringFromList(int i, vector <char *> sl) {
	return (char*)sl.at(i);
}

char *ChessModCfg::getRandomItem(vector <char *> list){
	int dice =  rand()%list.size();
	return (char*)list.at(dice);
}


void ChessModCfg::setPort(int value) { port=value;}

void ChessModCfg::setSideToMove(int value) { sidetomove=value;}

void ChessModCfg::setCastle(int value) { castle=value;}

void ChessModCfg::setIcatSide(int value) { icatside=value;}

void ChessModCfg::setEmpathy(int value) { empathy=value;}

void ChessModCfg::setPersonality(int value) { personality=value;}

void ChessModCfg::setSensationModel(int value){sensationModel=value;}

void ChessModCfg::setCalcExpValue(int value) { calcExpValue=value;}

void ChessModCfg::setRandomness(int value) { randomness=value;}

void ChessModCfg::setSearchDepth(int value) { searchDepth=value;}
	
int ChessModCfg::getSensationModel() {return sensationModel;}

int ChessModCfg::getCalcExpValue() {return calcExpValue;}

int ChessModCfg::getRandomness() {return randomness;}

int ChessModCfg::getSearchDepth() {return searchDepth;}

int ChessModCfg::getPort() {return port;}

int ChessModCfg::getIcatSide() {return icatside;}

int ChessModCfg::getCastle() {return castle;}

int ChessModCfg::getSideToMove() {return sidetomove;}

int ChessModCfg::getEmpathy() {return empathy;}

int ChessModCfg::getPersonality() {return personality;}


void ChessModCfg::toString(void) {
	printf("language: %s\n\tsensationModel: %d\n\tcalcExpValue: %d\n\tsearchdepth: %d\n\trandomness: %d", 
		languageFile, getSensationModel(),getCalcExpValue(), getSearchDepth(), getRandomness());
	/*printf("\tObjects Present (%d)\n", getObjectsCount());
	for(int i=0;i<getObjectsCount();i++)
		printf("\t\t- %s\n", getObject(i));*/
}


int ChessModCfg::loadFromFile(const char* filename) {
	int temp=0;
	XMLFileChessModCfgReader reader;
	XMLLanguageCfgReader readerlang;
	if(reader.readFromFile(this, filename)){
		
	}
	return readerlang.readFromFileLang(this,getLanguageFile());
}
int ChessModCfg::loadFromFile() {
	XMLFileChessModCfgReader reader;
	return reader.readFromFile(this);
}