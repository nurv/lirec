#pragma once
#include "TinyXML252\tinyXML.h"
#include "..\ChessModCfg.h"

class XMLFileChessModCfgReader
{
public:
	static const char* DEFAULT_FILENAME;

	static const char* XML_VALUE_STRING; // objects

	XMLFileChessModCfgReader(void);
	~XMLFileChessModCfgReader(void);

	int readFromFile(ChessModCfg *cmc);
	int readFromFile(ChessModCfg *cmc, const char* filename);

	int readFromDOMDocument(ChessModCfg *cmc, TiXmlNode *pDoc);
	int readFromDOMChessModCfg(ChessModCfg *cmc, TiXmlNode *pChess);
};