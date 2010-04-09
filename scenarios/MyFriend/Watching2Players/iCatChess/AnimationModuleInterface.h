#ifndef __ANIMATIONINTERFACEMODULE_H__
#define __ANIMATIONINTERFACEMODULE_H__

#ifndef _EiC
#include <stdio.h>
#include <stdlib.h>
#endif

#ifdef _EiC
#define WIN32
#endif

//#define ANIMATION_CHANNEL 0

#include "Module.h"
#include "OutputPortTypes.h"
#include "InputPortTypes.h"
#include "DMLString.h"

/*
List of iCat Variables taken from page 64 of the User Guide fo the OPPR 1.2
With index from the Animation Editor array
---------------------------------------------------------------------------
iCat.Head.LeftEar.Light.Red		0
iCat.Head.LeftEar.Light.Green	1
iCat.Head.LeftEar.Light.Blue	2
iCat.Head.LeftEar.Touch			
iCat.Head.RightEar.Light.Red	3
iCat.Head.RightEar.Light.Green	4
iCat.Head.RightEar.Light.Blue	5
iCat.Head.RightEar.Touch
iCat.Head.LeftEyeBrow			6
iCat.Head.RightEyeBrow			7
iCat.Head.LeftEyeLid			8
iCat.Head.RightEyeLid			9
iCat.Head.LeftEyeHorizontal		10
iCat.Head.RightEyeHorizontal	11
iCat.Head.BothEyesVertical		12
iCat.Head.UpperLeftLip			13
iCat.Head.BottomLeftLip			14
iCat.Head.UpperRightLip			15
iCat.Head.BottomRightLip		16
iCat.Neck						17
iCat.Body						18
iCat.Base.LeftPaw.Light.Red		19
iCat.Base.LeftPaw.Light.Green	20
iCat.Base.LeftPaw.Light.Blue	21
iCat.Base.LeftPaw.Touch
iCat.Base.LeftPaw.Proximity
iCat.Base.RightPaw.Light.Red	22
iCat.Base.RightPaw.Light.Green	23
iCat.Base.RightPaw.Light.Blue	24
iCat.Base.RightPaw.Touch		
iCat.Speech						25
iCat.Speech						
iCat.Sound.Channel1				26
iCat.Sound.Channel2				27
---------------------------------------------------- */
enum iCatVariableIDs {
HeadLeftEarLightRed, HeadLeftEarLightGreen, HeadLeftEarLightBlue,
HeadRightEarLightRed, HeadRightEarLightGreen, HeadRightEarLightBlue,
HeadLeftEyeBrow, HeadRightEyeBrow,
HeadLeftEyeLid, HeadRightEyeLid,
HeadLeftEyeHorizontal, HeadRightEyeHorizontal, HeadBothEyesVertical,
HeadUpperLeftLip, HeadBottomLeftLip,
HeadUpperRightLip, HeadBottomRightLip,
Neck, Body,
BaseLeftPawLightRed, BaseLeftPawLightGreen, BaseLeftPawLightBlue,
BaseRightPawLightRed, BaseRightPawLightGreen, BaseRightPawLightBlue,
Speech, SoundChannel1, SoundChannel2};

class AnimationModuleInterface
{
public:
	// Constants
	static const char *VariableStrings[];

	static const enum VariableIDs; //tava comentada


private:

	// DML Stuff
	OutputPortLastString m_Output; 
	InputPortLastString Status_Input;
	

public:
	AnimationModuleInterface(t_EventHandler command_callback);
	~AnimationModuleInterface(void);

public:
	int Initialize(Module *mod);
	DMLString GetStatus(/*char *str*/);
	int GetEvent();
	int Stop(int channel);
	int Load(int channel, char* animation);
	int Start(int channel, int number_of_cycles);
	int SetVar(const char* name, int value, int number_of_frames);
	int SetVar(int name, int value, int number_of_frames);
	int Say(const char *utterance);

	//novos metodos
	void PlayAnimation(char *utterance, int channel);
	void update(int newState[28]);
	int CreateGlobalVar(const char *name, const char* type);
	int SetGlobalVar(const char *name, int value, int number_of_frames);
	int MergeLogic(const char* variable, const char* expr);

};

#endif //__ANIMATIONINTERFACEMODULE_H__
