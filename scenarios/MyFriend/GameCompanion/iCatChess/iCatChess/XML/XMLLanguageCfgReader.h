#pragma once
#include "TinyXML252\tinyXML.h"
#include "..\ChessModCfg.h"

class XMLLanguageCfgReader
{
public:
	static const char* DEFAULT_FILENAME;

	static const char* XML_VALUE_STRING; // objects

	XMLLanguageCfgReader(void);
	~XMLLanguageCfgReader(void);

	int readFromFileLang(ChessModCfg *cmc);
	int readFromFileLang(ChessModCfg *cmc, const char* filename);

	int readFromDOMDocumentLang(ChessModCfg *cmc, TiXmlNode *pDoc);
	int readFromDOMList(ChessModCfg *cmc, TiXmlNode *pChess);
};