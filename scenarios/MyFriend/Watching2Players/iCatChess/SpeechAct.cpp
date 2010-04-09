#include "stdafx.h"
#include "SpeechAct.h"

SpeechAct::SpeechAct(string type) {
	_type = type;
}

SpeechAct::~SpeechAct(void){
}

void SpeechAct::addContextVariable(string name, string value){
	_contextVariables.insert(pair<string, string>(name, value));
}

string SpeechAct::toXML() {
	 string aux;

	 aux = "Say <SpeechAct type=\"SpeechAct\"><Sender>iCat</Sender><Receiver>user</Receiver><Type>"
		 + _type + "</Type>";

	 if (!_contextVariables.empty()){
		 	 map<string,string>::iterator it;
			 for ( it=_contextVariables.begin() ; it != _contextVariables.end(); it++ ) {
				 aux = aux + "<Context id=\"" + (*it).first + "\">" + (*it).second + "</Context>";
			 }
	 }

	aux = aux + "</SpeechAct>";
	// aux = aux + "<Utterance>blank</Utterance></SpeechAct>";

	return aux;
}
