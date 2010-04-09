#include "stdafx.h"
#include "LanguageEngineMaster.h"

#include <stdlib.h> //new to run java
#using <mscorlib.dll>

using namespace System;
using namespace System::Collections;
using namespace System::Threading;
using namespace System::IO;
using namespace System::Text;


using namespace System::Runtime::InteropServices;



void ThreadJava(Object ^ frm)
{
	system("java -cp .;\"Program Files\\OPPR\\src\\iCatChess\\language\";\"Program Files\\OPPR\\src\\iCatChess\\language\\Language.jar\" LanguageServerSlave 5000");


/*	ofstream myfile;
	myfile.open ("threadFunciona.txt");
	myfile << "iolanda sera que da?";
	myfile.close();   */
};

LanguageEngineMaster::LanguageEngineMaster(void) {
	_languageServer = NULL;
	_languageSocket = NULL;
}

LanguageEngineMaster::~LanguageEngineMaster(void){
}

void LanguageEngineMaster::start() {
	printf("Creating Socket...\n" );
	_languageServer = socket(AF_INET, SOCK_STREAM, 0);
	if (_languageServer == INVALID_SOCKET)
		printf("Could not open server socket...\n" );
	else
		printf("Socket created!");

	sockaddr_in *addrserver = new sockaddr_in();
	addrserver->sin_addr.s_addr = htonl(INADDR_ANY);
	addrserver->sin_family = AF_INET;
	addrserver->sin_port = htons(5000);

	if (bind(_languageServer,
		(struct sockaddr *) addrserver,
		sizeof(*addrserver)) < 0) {
			printf("cannot bind socket");
			exit(1);
	}

	listen(_languageServer, 5);

	Thread^ t2 = gcnew Thread(gcnew ParameterizedThreadStart(ThreadJava));
	t2->Start(gcnew Object());

	//if (initialized == false) {
	sockaddr *addr = NULL;
	int *size = NULL;
	_languageSocket = accept(_languageServer, addr, size);
	if (_languageSocket == NULL)
		exit(1);
	printf( "Connection established\n" );
	closesocket(_languageServer);

}

string LanguageEngineMaster::say(SpeechAct sa) {
	//string speechAct = "Say <SpeechAct type=\"SpeechAct\"><Sender>iCat</Sender><Receiver>Iolanda</Receiver><Type>greeting</Type><Context id=\"user\">Obama</Context><Utterance>blank</Utterance></SpeechAct>";
	//char *buff = new char[speechAct.toXML().size()];

	string speechAct = sa.toXML();



/*
	char *buff = new char[speechAct.size()];
	strcpy(buff, speechAct.c_str());
	send(_languageSocket, buff, speechAct.size(), 0);
*/


	UTF8Encoding^ utf8 = gcnew UTF8Encoding;
	
	
	/*char *buff formStringTbox = (char *) (void*) Marshal::StringToHGlobalAnsi(_formDebug->tboxName->Text);
			
	char *buff = new char[encodedBytes->Length];
	memcpy(buff, (const void *)encodedBytes, encodedBytes->Length);
*/


	//char *buff formStringTbox = (char *) (void*) Marshal::StringToHGlobalUni(_formDebug->tboxName->Text);
	

	char *buff = new char[speechAct.size()];
	strcpy(buff, speechAct.c_str());
	send(_languageSocket, buff, speechAct.size(), 0);

	

	char result[1024]; //limpar antes
	recv(_languageSocket, result, sizeof(result), 0);


	String^ unicodeString = gcnew System::String(result);
	array<Byte>^ encodedBytes = utf8->GetBytes( unicodeString );
	String^ decodedString = utf8->GetString(encodedBytes);
   Console::WriteLine( "Decoded bytes:" );
   Console::WriteLine( decodedString );


	Console::Write("JONAS1**");
    Console::Write(gcnew System::String(result));
	Console::WriteLine("**MARIA");
	
/*
String* words = S"this is a list of words, with: a bit of punctuation.";
   Char chars[] = {' ', ', ', '->', ':'};
   String* split[] = words->Split(chars);
//

	if (result != NULL){
		string delimiters[] = {};

		                string[] delimiters = new string[2];
                delimiters[0] = "<Utterance>";
                delimiters[1] = "</Utterance";

	}*/


	ofstream myfile;
	myfile.open ("example.txt");
	myfile << string(result);
	myfile.close();


	ifstream cin("example.txt");
	string resultado;
	getline(cin,resultado);
	cin.close();

//gcnew System::String(gcnew System::String(playerName)

	/*Console::Write("JONAS**");
    Console::Write(gcnew System::String(resultado.c_str()));
	Console::WriteLine("**MARIA");*/
	return resultado;
}
