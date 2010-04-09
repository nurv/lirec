#ifndef __SPEECHACT_H__
#define __SPEECHACT_H__

#include <map>

class SpeechAct {

private:  
	string _type;
	string _utterance;
	map<string,string> _contextVariables;
	 

public:
	   SpeechAct (string type);
      ~SpeechAct (void);
	  void addContextVariable(string name, string value);
	  string toXML();
};

#endif