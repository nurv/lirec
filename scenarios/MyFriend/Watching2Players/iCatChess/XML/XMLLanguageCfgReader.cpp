#include "stdafx.h"

#include "XMLLanguageCfgReader.h"

#include "XMLLanguageCfgBridge.h"

const char* XMLLanguageCfgReader::DEFAULT_FILENAME = "ChessModPortuguese.xml";

const char* XMLLanguageCfgReader::XML_VALUE_STRING = "reactsto";


XMLLanguageCfgReader::XMLLanguageCfgReader(void) {
}

XMLLanguageCfgReader::~XMLLanguageCfgReader(void) {
}

int XMLLanguageCfgReader::readFromFileLang(ChessModCfg *cmc) {
	return readFromFileLang(cmc, DEFAULT_FILENAME);
}

int XMLLanguageCfgReader::readFromFileLang(ChessModCfg *cmc , const char* filename) {
	TiXmlDocument doc(filename);
	bool loadOkay = doc.LoadFile();
	if (!loadOkay) {
		printf("Failed to load file \"%s\"\n", filename);
		doc.SetTabSize(8);
		printf("ERROR: %s at %d, %d\n",doc.ErrorDesc(), doc.ErrorRow(), doc.ErrorCol());
		return 1;
	}
	readFromDOMDocumentLang(cmc, &doc ); // defined later in the tutorial
	return 0;
}


int XMLLanguageCfgReader::readFromDOMDocumentLang(ChessModCfg *cmc, TiXmlNode *pDoc) {
	TiXmlNode *pLanguageFile = NULL;
	// search for Objects element
	pLanguageFile = pDoc->FirstChild(XML_VALUE_STRING);
	if(pLanguageFile==NULL) return 2;
	return readFromDOMList(cmc, pLanguageFile);
}

int XMLLanguageCfgReader::readFromDOMList(ChessModCfg *cmc, TiXmlNode *pChess) {
	//TiXmlNode *pChess = NULL;
	XMLLanguageCfgBridge bridge;
	bridge.readValuesFromDOMDocumentLang(cmc, pChess);
	// Run all object tags in objects list
	return 0;
}

 
