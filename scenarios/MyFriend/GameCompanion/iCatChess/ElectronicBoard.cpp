#include "stdafx.h"
#include "ElectronicBoard.h"


ElectronicBoard::ElectronicBoard(int port){
	DMLString str;
	initialpos();
	str = "COM";
	str += port;
	ComInit(str.pszGetPointer(),INITANDFASTSCAN,ProcessScanEvents);
	//printf("abriu a função com o valor : %d\n", ComInit("COM4",INITANDFASTSCAN,ProcessScanEvents));
}

void ElectronicBoard::Update(){
	serveDGTBoard();
	Sleep(150);
}

char* ElectronicBoard::getMove(){
	resetNewMove();
	//printf("%s\n", retLtlauxbuff()); 
	return retLtlauxbuff();
}

unsigned char* ElectronicBoard::getBoard(){
	//printf("%s\n", retLtlauxbuff()); 
	return retBoard();
}

int ElectronicBoard::newMove(){
	return retNewMove();
}

int ElectronicBoard::stableBoard(){
	return retStableBoard();
}

void ElectronicBoard::sendClockMessage(char * message){
	int i=0;
	char sendMessage[6];
	sendMessage[0]=message[1];
	sendMessage[1]=message[0];
	sendMessage[2]='.';
	sendMessage[3]=message[3];
	sendMessage[4]=message[2];
	sendMessage[5]='\0';
	for(i=0;i<5;i++)
		SendDisplayMessage(sendMessage, false,false);
	//SendDisplayMessageEnd();
}
