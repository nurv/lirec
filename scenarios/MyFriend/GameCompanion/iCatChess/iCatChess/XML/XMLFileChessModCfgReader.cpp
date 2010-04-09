#include "stdafx.h"

#include "XMLFileChessModCfgReader.h"

#include "XMLChessModCfgBridge.h"

const char* XMLFileChessModCfgReader::DEFAULT_FILENAME = "ChessModCfg.xml";

const char* XMLFileChessModCfgReader::XML_VALUE_STRING = "chessModCfg";


XMLFileChessModCfgReader::XMLFileChessModCfgReader(void) {
}

XMLFileChessModCfgReader::~XMLFileChessModCfgReader(void) {
}

int XMLFileChessModCfgReader::readFromFile(ChessModCfg *cmc) {
	return readFromFile(cmc, DEFAULT_FILENAME);
}

int XMLFileChessModCfgReader::readFromFile(ChessModCfg *cmc, const char* filename) {
	TiXmlDocument doc(filename);
	bool loadOkay = doc.LoadFile();
	if (!loadOkay) {
		printf("Failed to load file \"%s\"\n", filename);
		doc.SetTabSize(8);
		printf("ERROR: %s at %d, %d\n",doc.ErrorDesc(), doc.ErrorRow(), doc.ErrorCol());
		return 1;
	}
	readFromDOMDocument(cmc, &doc );
	return 0;
}


int XMLFileChessModCfgReader::readFromDOMDocument(ChessModCfg *cmc, TiXmlNode *pDoc) {
	TiXmlNode *pChessModCfgDOM = NULL;
	// search for Objects element
	pChessModCfgDOM = pDoc->FirstChild(XML_VALUE_STRING);
	if(pChessModCfgDOM==NULL) return 2;
	return readFromDOMChessModCfg(cmc, pChessModCfgDOM);
}

int XMLFileChessModCfgReader::readFromDOMChessModCfg(ChessModCfg *cmc, TiXmlNode *pChess) {
	XMLChessModCfgBridge bridge;
	bridge.readValuesFromDOMDocument(cmc, pChess);
	return 0;
}

 
