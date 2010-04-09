#include "stdafx.h"

#include "XMLLanguageCfgBridge.h"


const char* XMLLanguageCfgBridge::XML_VALUE_STRING = "reactsto";
const char* XMLLanguageCfgBridge::XML_ATTRIBUTE_TEXT = "text";
const char* XMLLanguageCfgBridge::XML_ATTRIBUTE_RAF = "raf";

const char* XMLLanguageCfgBridge::XML_CHILDREN_CONFMOVE = "confirmmove";
const char* XMLLanguageCfgBridge::XML_CHILDREN_SENTPRESENT = "sentencespresent";
const char* XMLLanguageCfgBridge::XML_CHILDREN_ANIMPRESENT = "animationspresent"; 
const char* XMLLanguageCfgBridge::XML_CHILDREN_ILLEGALMOVE = "illegalmove";
const char* XMLLanguageCfgBridge::XML_CHILDREN_PLAYAGAIN = "playagain";
const char* XMLLanguageCfgBridge::XML_CHILDREN_NOTMYMOVE = "notmymove";
const char* XMLLanguageCfgBridge::XML_CHILDREN_CHECK = "check";
const char* XMLLanguageCfgBridge::XML_CHILDREN_WINNING = "winning";
const char* XMLLanguageCfgBridge::XML_CHILDREN_DRAW = "draw";
const char* XMLLanguageCfgBridge::XML_CHILDREN_LOSING = "losing";
const char* XMLLanguageCfgBridge::XML_CHILDREN_LOOKATBOARD = "lookatboard";



XMLLanguageCfgBridge::XMLLanguageCfgBridge(void)
{
}

XMLLanguageCfgBridge::~XMLLanguageCfgBridge(void)
{
}

int XMLLanguageCfgBridge::readValuesFromDOMDocumentLang(ChessModCfg *cmc, TiXmlNode* pNode) {
	if (!pNode) return 1;
	if (pNode->Type() != TiXmlNode::ELEMENT)
			return 2;
	return readValuesFromDOMElementLang(cmc, pNode->ToElement());
}

int XMLLanguageCfgBridge::readValuesFromDOMElementLang(ChessModCfg *cmc, TiXmlElement* pElement) {
	TiXmlNode *pChild = NULL;
	TiXmlNode *pChildAtrib = NULL;
	TiXmlElement *pListElement;
	TiXmlElement *pTextElement;
	TiXmlElement *pAnimElement;
	if ( !pElement ) return 1;
	if (_stricmp(pElement->Value(), XML_VALUE_STRING))
		return 11;
	
//--------CONFIRM MOVE----------
	pListElement = pElement->FirstChild(XML_CHILDREN_CONFMOVE)->ToElement();
	//--------Sentences----------
	pTextElement = pListElement->FirstChild(XML_CHILDREN_SENTPRESENT)->ToElement();
	while( pChild = pTextElement->IterateChildren( pChild ) ) {
		cmc->addStringToList(pChild->ToElement()->Attribute(XML_ATTRIBUTE_TEXT), &cmc->confirMoveSent);
	}
	//--------Animations----------
	pTextElement = pListElement->FirstChild(XML_CHILDREN_ANIMPRESENT)->ToElement();
	while( pChild = pTextElement->IterateChildren( pChild ) ) {
		cmc->addStringToList(pChild->ToElement()->Attribute(XML_ATTRIBUTE_RAF), &cmc->confirMoveAnim);
	}

//--------NOT MY MOVE----------
	pListElement = pElement->FirstChild(XML_CHILDREN_NOTMYMOVE)->ToElement();

	//--------Sentences----------
	pTextElement = pListElement->FirstChild(XML_CHILDREN_SENTPRESENT)->ToElement();
	while( pChild = pTextElement->IterateChildren( pChild ) ) {
		cmc->addStringToList(pChild->ToElement()->Attribute(XML_ATTRIBUTE_TEXT), &cmc->notMyMoveSent);
	}
	//--------Animations----------
	pTextElement = pListElement->FirstChild(XML_CHILDREN_ANIMPRESENT)->ToElement();
	while( pChild = pTextElement->IterateChildren( pChild ) ) {
		cmc->addStringToList(pChild->ToElement()->Attribute(XML_ATTRIBUTE_RAF), &cmc->notMyMoveAnim);
	}

//--------ILLEGAL MOVE----------
	pListElement = pElement->FirstChild(XML_CHILDREN_ILLEGALMOVE)->ToElement();

	//--------Sentences----------
	pTextElement = pListElement->FirstChild(XML_CHILDREN_SENTPRESENT)->ToElement();
	while( pChild = pTextElement->IterateChildren( pChild ) ) {
		cmc->addStringToList(pChild->ToElement()->Attribute(XML_ATTRIBUTE_TEXT), &cmc->illegalMoveSent);
	}
	//--------Animations----------
	pTextElement = pListElement->FirstChild(XML_CHILDREN_ANIMPRESENT)->ToElement();
	while( pChild = pTextElement->IterateChildren( pChild ) ) {
		cmc->addStringToList(pChild->ToElement()->Attribute(XML_ATTRIBUTE_RAF), &cmc->illegalMoveAnim);
	}

//--------PLAY AGAIN----------
	pListElement = pElement->FirstChild(XML_CHILDREN_PLAYAGAIN)->ToElement();

	//--------Sentences----------
	pTextElement = pListElement->FirstChild(XML_CHILDREN_SENTPRESENT)->ToElement();
	while( pChild = pTextElement->IterateChildren( pChild ) ) {
		cmc->addStringToList(pChild->ToElement()->Attribute(XML_ATTRIBUTE_TEXT), &cmc->playAgainSent);
	}

//--------CHECK----------
	pListElement = pElement->FirstChild(XML_CHILDREN_CHECK)->ToElement();

	//--------Sentences----------
	pTextElement = pListElement->FirstChild(XML_CHILDREN_SENTPRESENT)->ToElement();
	while( pChild = pTextElement->IterateChildren( pChild ) ) {
		cmc->addStringToList(pChild->ToElement()->Attribute(XML_ATTRIBUTE_TEXT), &cmc->checkSent);
	}

//--------WINNING----------
	pListElement = pElement->FirstChild(XML_CHILDREN_WINNING)->ToElement();

	//--------Animations----------
	pTextElement = pListElement->FirstChild(XML_CHILDREN_ANIMPRESENT)->ToElement();
	while( pChild = pTextElement->IterateChildren( pChild ) ) {
		cmc->addStringToList(pChild->ToElement()->Attribute(XML_ATTRIBUTE_RAF), &cmc->winingAnim);
	}

//--------LOSING----------
	pListElement = pElement->FirstChild(XML_CHILDREN_LOSING)->ToElement();

	//--------Animations----------
	pTextElement = pListElement->FirstChild(XML_CHILDREN_ANIMPRESENT)->ToElement();
	while( pChild = pTextElement->IterateChildren( pChild ) ) {
		cmc->addStringToList(pChild->ToElement()->Attribute(XML_ATTRIBUTE_RAF), &cmc->losingAnim);
	}

//--------DRAW----------
	pListElement = pElement->FirstChild(XML_CHILDREN_DRAW)->ToElement();

	//--------Animations----------
	pTextElement = pListElement->FirstChild(XML_CHILDREN_ANIMPRESENT)->ToElement();
	while( pChild = pTextElement->IterateChildren( pChild ) ) {
		cmc->addStringToList(pChild->ToElement()->Attribute(XML_ATTRIBUTE_RAF), &cmc->drawAnim);
	}

//--------DRAW----------
	pListElement = pElement->FirstChild(XML_CHILDREN_LOOKATBOARD)->ToElement();

	//--------Animations----------
	pTextElement = pListElement->FirstChild(XML_CHILDREN_ANIMPRESENT)->ToElement();
	while( pChild = pTextElement->IterateChildren( pChild ) ) {
		cmc->addStringToList(pChild->ToElement()->Attribute(XML_ATTRIBUTE_RAF), &cmc->lookAtBoard);
	}

	return 0;
}