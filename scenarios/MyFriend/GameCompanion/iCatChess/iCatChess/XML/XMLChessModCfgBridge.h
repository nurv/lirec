#ifndef __XMLCHESSMODCFGBRIDGE_H__
#define __XMLCHESSMODCFGBRIDGE_H__

// Where de XMLObject->ScenarioDefinition mapping is defined
//	See: readValuesFromDOMObject() method for mapping

#include <string.h>
#include "..\ChessModCfg.h"
#include "TinyXML252\tinyxml.h"


class XMLChessModCfgBridge
{
public:
	static const char* XML_VALUE_STRING;

	static const char* XML_ATTRIBUTE_LANGUAGE;
	static const char* XML_ATTRIBUTE_VALUE;
	static const char* XML_ATTRIBUTE_SENSMODEL;
	static const char* XML_ATTRIBUTE_CALCEXPVALUE;
	static const char* XML_ATTRIBUTE_SEARCHDEPTH;
	static const char* XML_ATTRIBUTE_RANDOMNESS;
	static const char* XML_ATTRIBUTE_PORT;
	static const char* XML_ATTRIBUTE_SIDETOMOVE;
	static const char* XML_ATTRIBUTE_ICATSIDE;
	static const char* XML_ATTRIBUTE_CASTLE;

	static const char* XML_CHILDREN_LANGUAGE;
	static const char* XML_CHILDREN_EMOTIVECTOR;
	static const char* XML_CHILDREN_PERSONALITY;
	static const char* XML_CHILDREN_CHESSENGINE;
	static const char* XML_CHILDREN_DESCRIPTION;
	static const char* XML_CHILDREN_OBJECTIVEWORD;
	static const char* XML_CHILDREN_OBJECTSPRESENT;
	static const char* XML_CHILDREN_OBJECTPRESENT;
	static const char* XML_CHILDREN_ELECTRONICBOARD;

public:
	XMLChessModCfgBridge(void);
	~XMLChessModCfgBridge(void);

	// method implementing the mapping from DOM Element
	int readValuesFromDOMDocument(ChessModCfg *cMC, TiXmlNode* pNode);

	// method implementing the mapping from DOM Node
	int readValuesFromDOMElement(ChessModCfg *cMC, TiXmlElement* pElement);

	// method implementing the mapping from the objects present list
	//int readObjectsPresentFromDOMElement(ChessModCfg *cMC, TiXmlElement* pElement);

};

#endif 