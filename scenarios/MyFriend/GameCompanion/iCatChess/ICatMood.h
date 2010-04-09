#ifndef __ICAT_MOOD_H__
#define __ICAT_MOOD_H__

#ifndef _EiC
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#endif

#ifdef _EiC
#define WIN32
#endif

#include "AnimationModuleInterface.h"

#define EYEBROWS "Eyebrows"
#define EYELIDS "Eyelids"
#define UPPER_LIP "UpperLip"
#define BOTTOM_LIP "BottomLip"

#define MAX_DECAY 20 //number od cycles without "decreasing" expression

class ICatMood
{
private:
	AnimationModuleInterface *_animation;

	//constants
	static const int MOOD_HAPPY[];
	static const int MOOD_FRUSTRATED[];

	int _valence;
	int _preValence;
	int _decay;
	
	int _variableState[28];


public:
	ICatMood(AnimationModuleInterface *anim);
	~ICatMood(void);

protected:
	void setState(const int newState[28]);
	
public:
	void createGlobalVars(void);
	void updateEmotion(void);
	void calcEmotionEyeBrows(void);
	void calcEmotionEyeLids(void);
	void calcEmotionLips(void);
	void setValence(int newValence);  //por private dps
	int getValence();
	void incValence(int points);
};

#endif __ICAT_MOOD_H__
