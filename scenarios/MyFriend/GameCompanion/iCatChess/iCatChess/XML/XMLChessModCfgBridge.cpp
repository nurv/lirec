#include "stdafx.h"

#include "XMLChessModCfgBridge.h"
  #include <stdlib.h>

const char* XMLChessModCfgBridge::XML_VALUE_STRING = "ChessModCfg";
const char* XMLChessModCfgBridge::XML_ATTRIBUTE_LANGUAGE = "file";
const char* XMLChessModCfgBridge::XML_ATTRIBUTE_VALUE= "value";
const char* XMLChessModCfgBridge::XML_ATTRIBUTE_SENSMODEL= "sensationmodel";
const char* XMLChessModCfgBridge::XML_ATTRIBUTE_CALCEXPVALUE= "calcexpectedvalue";
const char* XMLChessModCfgBridge::XML_ATTRIBUTE_SEARCHDEPTH= "searchdepth";
const char* XMLChessModCfgBridge::XML_ATTRIBUTE_RANDOMNESS= "randomness";
const char* XMLChessModCfgBridge::XML_ATTRIBUTE_PORT= "port";
const char* XMLChessModCfgBridge::XML_ATTRIBUTE_SIDETOMOVE= "sidetomove";
const char* XMLChessModCfgBridge::XML_ATTRIBUTE_ICATSIDE= "icatside";
const char* XMLChessModCfgBridge::XML_ATTRIBUTE_CASTLE= "castle";

const char* XMLChessModCfgBridge::XML_CHILDREN_LANGUAGE = "language";
const char* XMLChessModCfgBridge::XML_CHILDREN_PERSONALITY = "personality";
const char* XMLChessModCfgBridge::XML_CHILDREN_CHESSENGINE= "chessengine";
const char* XMLChessModCfgBridge::XML_CHILDREN_EMOTIVECTOR= "emotivector";
const char* XMLChessModCfgBridge::XML_CHILDREN_DESCRIPTION = "description";
const char* XMLChessModCfgBridge::XML_CHILDREN_ELECTRONICBOARD = "electronicboard";

const char* XMLChessModCfgBridge::XML_CHILDREN_OBJECTIVEWORD = "objectiveWord";
//const char* XMLChessModCfgBridge::XML_CHILDREN_OBJECTSPRESENT = "objectsPresent";
//const char* XMLChessModCfgBridge::XML_CHILDREN_OBJECTPRESENT = "object";


XMLChessModCfgBridge::XMLChessModCfgBridge(void)
{
}

XMLChessModCfgBridge::~XMLChessModCfgBridge(void)
{
}

int XMLChessModCfgBridge::readValuesFromDOMDocument(ChessModCfg *cMC, TiXmlNode* pNode) {
	if (!pNode) return 1;
	if (pNode->Type() != TiXmlNode::ELEMENT)
		return 2;
	return readValuesFromDOMElement(cMC, pNode->ToElement());
}

int XMLChessModCfgBridge::readValuesFromDOMElement(ChessModCfg *cMC, TiXmlElement* pElement) {
	TiXmlNode *pChild = NULL;
	pChild = pElement->FirstChild(XML_CHILDREN_LANGUAGE);
	if ( !pElement ) return 1;
	if (_stricmp(pElement->Value(), XML_VALUE_STRING))
		return 11;
	//--------
	///// Read Attributes
	///// Read Children Nodes
	//printf("ta aqui- %s\n", pElement->FirstChild(XML_CHILDREN_LANGUAGE)->Value()->Attribute(XML_ATTRIBUTE_LANGUAGE));
	cMC->setLanguageFile(pChild->ToElement()->Attribute(XML_ATTRIBUTE_LANGUAGE));
	
	pChild = pElement->FirstChild(XML_CHILDREN_PERSONALITY);

	cMC->setPersonality(atoi(pChild->ToElement()->Attribute(XML_ATTRIBUTE_VALUE)));

	pChild = pElement->FirstChild(XML_CHILDREN_EMOTIVECTOR);

	cMC->setSensationModel(atoi(pChild->ToElement()->Attribute(XML_ATTRIBUTE_SENSMODEL)));

	cMC->setCalcExpValue(atoi(pChild->ToElement()->Attribute(XML_ATTRIBUTE_CALCEXPVALUE)));

	pChild = pElement->FirstChild(XML_CHILDREN_CHESSENGINE);

	cMC->setRandomness(atoi(pChild->ToElement()->Attribute(XML_ATTRIBUTE_RANDOMNESS)));

	cMC->setSearchDepth(atoi(pChild->ToElement()->Attribute(XML_ATTRIBUTE_SEARCHDEPTH)));

	cMC->setCastle(atoi(pChild->ToElement()->Attribute(XML_ATTRIBUTE_CASTLE)));

	cMC->setSideToMove(atoi(pChild->ToElement()->Attribute(XML_ATTRIBUTE_SIDETOMOVE)));

	cMC->setIcatSide(atoi(pChild->ToElement()->Attribute(XML_ATTRIBUTE_ICATSIDE)));

	pChild = pElement->FirstChild(XML_CHILDREN_ELECTRONICBOARD);

	cMC->setPort(atoi(pChild->ToElement()->Attribute(XML_ATTRIBUTE_PORT)));


	//if(NULL != (pChild = pElement->FirstChild(XML_CHILDREN_TITLE))) {
	//	cMC->setTitle(pChild->ToElement()->GetText());
	//}
	return 0;
}

/*int XMLChessModCfgBridge::readObjectsPresentFromDOMElement(ChessModCfg *cMC, TiXmlElement* pElement) {
	TiXmlNode *pObject = NULL;
	if ( !pElement ) return 10;
	if (_stricmp(pElement->Value(), XML_CHILDREN_OBJECTSPRESENT))
		return 11;
	// Run all object tags in objects list
	while( pObject = pElement->IterateChildren( XML_CHILDREN_OBJECTPRESENT, pObject ) ) {
		cMC->addObject(pObject->ToElement()->Attribute(XML_ATTRIBUTE_ID));
	}
	return 0;
}*/