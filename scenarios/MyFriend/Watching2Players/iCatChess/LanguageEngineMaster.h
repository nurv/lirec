#ifndef __LANGUAGEENGINEMASTER_H__
#define __LANGUAGEENGINEMASTER_H__

#include <stdio.h>

#include "SpeechAct.h"

using namespace System;

class LanguageEngineMaster {

private:  
	  SOCKET _languageServer;
	  SOCKET _languageSocket;

public:
	   LanguageEngineMaster (void);
      ~LanguageEngineMaster (void);
	  void start();
	  string say(SpeechAct sa);
 
};

#endif