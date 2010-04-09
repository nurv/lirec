#pragma once
#include <string.h>
#include "..\ChessModCfg.h"
#include "TinyXML252\tinyxml.h"


class XMLLanguageCfgBridge
{
public:
	static const char* XML_VALUE_STRING;

	static const char* XML_ATTRIBUTE_TEXT;
	static const char* XML_ATTRIBUTE_RAF;

	static const char* XML_CHILDREN_CONFMOVE;
	static const char* XML_CHILDREN_SENTPRESENT;
	static const char* XML_CHILDREN_ANIMPRESENT;
	static const char* XML_CHILDREN_NOTMYMOVE;
	static const char* XML_CHILDREN_ILLEGALMOVE;
	static const char* XML_CHILDREN_PLAYAGAIN;
	static const char* XML_CHILDREN_CHECK;
	static const char* XML_CHILDREN_WINNING;
	static const char* XML_CHILDREN_LOSING;
	static const char* XML_CHILDREN_DRAW;
	static const char* XML_CHILDREN_LOOKATBOARD;

public:
	XMLLanguageCfgBridge(void);
	~XMLLanguageCfgBridge(void);

	// method implementing the mapping from DOM Element
	int readValuesFromDOMDocumentLang(ChessModCfg *cMC, TiXmlNode* pNode);

	// method implementing the mapping from DOM Node
	int readValuesFromDOMElementLang(ChessModCfg *cMC, TiXmlElement* pElement);

	// method implementing the mapping from the objects present list
	//int readObjectsPresentFromDOMElement(ChessModCfg *cMC, TiXmlElement* pElement);

};
