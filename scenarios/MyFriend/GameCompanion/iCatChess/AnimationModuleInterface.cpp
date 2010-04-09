// 
// Translated to C++ from the OPPR 1.2 lua script "AnimationModuleInterface.lua" in Demo

//
//		João Gonçalves, IST, 30th November 2006

#include "AnimationModuleInterface.h"
#include "DMLString.h"
#include "stdafx.h"

const char *AnimationModuleInterface::VariableStrings[] = {
"iCat.Head.LeftEar.Light.Red", "iCat.Head.LeftEar.Light.Green", "iCat.Head.LeftEar.Light.Blue",
"iCat.Head.RightEar.Light.Red", "iCat.Head.RightEar.Light.Green", "iCat.Head.RightEar.Light.Blue",
"iCat.Head.LeftEyeBrow", "iCat.Head.RightEyeBrow",
"iCat.Head.LeftEyeLid", "iCat.Head.RightEyeLid",
"iCat.Head.LeftEyeHorizontal", "iCat.Head.RightEyeHorizontal", "iCat.Head.BothEyesVertical",
"iCat.Head.UpperLeftLip", "iCat.Head.BottomLeftLip",
"iCat.Head.UpperRightLip", "iCat.Head.BottomRightLip",
"iCat.Neck", "iCat.Body",
"iCat.Base.LeftPaw.Light.Red", "iCat.Base.LeftPaw.Light.Green", "iCat.Base.LeftPaw.Light.Blue", 
"iCat.Base.RightPaw.Light.Red", "iCat.Base.RightPaw.Light.Green", "iCat.Base.RightPaw.Light.Blue",
"iCat.Speech", "iCat.Sound.Channel1", "iCat.Sound.Channel2" 	};

AnimationModuleInterface::AnimationModuleInterface(t_EventHandler status_callback) : m_Output("AniMod_CommandOutput",1)
, Status_Input("AniMod_StatusInput",1,NO_MAX_CONNECTIONS,Push_e,status_callback){ 
	//setState(Frustrated);
}

AnimationModuleInterface::~AnimationModuleInterface() {
}


int AnimationModuleInterface::Initialize(Module *mod) {
	m_Output.fActivate(mod);
	m_Output.fWrite(new DMLString("show-info on"));
	Status_Input.fActivate(mod);
	Status_Input.fRead(new DMLString("show-info on"));
	return 0;
}

// read and return current data item from status input port
// data item is removed from port buffer
// nil value is returned when buffer is empty
DMLString AnimationModuleInterface::GetStatus(/*char *str*/) {
	DMLString aux;
	Status_Input.fRead(&aux);
	return aux;
	 //TODO
	////return AniMod_StatusInput:Receive ()
}

// read and return current data item from event input port
// data item is removed from port buffer
// nil value is returned when buffer is empty
int AnimationModuleInterface::GetEvent() {
	return 0; //TODO
	////return AniMod_EventInput:Receive ()
}

// Animation Module command: stop
//function AnimationModule.Stop (channel)
int AnimationModuleInterface::Stop(int channel) {
	DMLString string;
	string = "stop ";
	string += channel;
	return (int)m_Output.fWrite(&string);
}

// Animation Module command: load
//function AnimationModule.Load (channel, animation) 
int AnimationModuleInterface::Load(int channel, char* animation) {
	DMLString string;
	string = "load ";
	string += channel;
	string += " ";
	string += animation;
	return (int)m_Output.fWrite(&string);
	//return (int)m_Output.fWrite(new DMLString("load 0 \\STANDARD\\Actions\\Functional\\NotUnderstand.raf"));
}

// Animation Module command: start
//function AnimationModule.Start (channel, number_of_cycles)
int AnimationModuleInterface::Start(int channel, int number_of_cycles) {
	DMLString string;
	string = "start ";
	string += channel;
	string += " ";
	string += number_of_cycles;
	return (int)m_Output.fWrite(&string);
	//return (int)m_Output.fWrite(new DMLString("start 0 1"));
}

// Animation Module command: set variable
//function AnimationModule.SetVar (name, value, number_of_frames)
int AnimationModuleInterface::SetVar(const char* name, int value, int number_of_frames) {
	DMLString string;
	string = "set-var ";
	string += name;
	string += " ";
	string += value;
	string += " ";
	string += number_of_frames;
	return (int)m_Output.fWrite(&string);
}

int AnimationModuleInterface::SetVar(int name, int value, int number_of_frames) {
	return SetVar(VariableStrings[name], value, number_of_frames);
}

// Speak
int AnimationModuleInterface::Say(const char *utterance) {
	DMLString string;
	string = "set-var icat.Speech.Text \"";
	string += utterance;
	string += "\" 1";
	return (int)m_Output.fWrite(&string);
}

// Speak
void AnimationModuleInterface::PlayAnimation(char *utterance, int channel) {
	Load(channel, utterance);
	Start(channel, 1);
}

//novos metodos

void AnimationModuleInterface::update(int newState[28]) {
	int i;
	for (i =0; i < 25; i++) { 
		SetVar(i, newState[i], -1);
	}
}

int AnimationModuleInterface::CreateGlobalVar(const char *name, const char* type) {
	DMLString string;
	string = "create-global-var ";
	string += name;
	string += " ";
	string += type;
	return (int)m_Output.fWrite(&string);
}

int AnimationModuleInterface::SetGlobalVar(const char *name, int value, int number_of_frames) {  //para inteiras
	DMLString string;
	string = "set-var global.";
	string += name;
	string += " ";
	string += value;
	string += " ";
	string += number_of_frames;
	return (int)m_Output.fWrite(&string);
}

int AnimationModuleInterface::MergeLogic(const char* variable, const char* expr) {
	DMLString string;
	string = "set-merge-logic ";
	string += variable;
	string += " ";
	string += expr;
	return (int)m_Output.fWrite(&string);
}


