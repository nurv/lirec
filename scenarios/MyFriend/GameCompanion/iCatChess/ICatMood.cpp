#include "stdafx.h"
//#include "ICatMood.h"
using namespace System;

const int ICatMood::MOOD_HAPPY[] = {0, 0, 0, 0, 0, 0,
/*"iCat.Head.LeftEyeBrow"*/80, /*"iCat.Head.RightEyeBrow"*/80,
/*"iCat.Head.LeftEyeLid"*/95, /*"iCat.Head.RightEyeLid"*/95,
/*"iCat.Head.LeftEyeHorizontal"*/0, /*"iCat.Head.RightEyeHorizontal"*/0, /*"iCat.Head.BothEyesVertical"*/0,
/*"iCat.Head.UpperLeftLip"*/-20, /*"iCat.Head.BottomLeftLip"*/-100,
/*"iCat.Head.UpperRightLip"*/-20, /*"iCat.Head.BottomRightLip"*/-100,
/*"iCat.Neck"*/70, /*"iCat.Body"*/0,
/*"iCat.Base.LeftPaw.Light.Red"*/0, /*"iCat.Base.LeftPaw.Light.Green"*/0, /*"iCat.Base.LeftPaw.Light.Blue"*/0, 
/*"iCat.Base.RightPaw.Light.Red"*/0, /*"iCat.Base.RightPaw.Light.Green"*/0, /*"iCat.Base.RightPaw.Light.Blue"*/0,
/*"iCat.Speech"*/0, /*"iCat.Sound.Channel1"*/0, /*"iCat.Sound.Channel2"*/0 	};

const int ICatMood::MOOD_FRUSTRATED[] = {0, 0, 0, 0, 0, 0,
/*"iCat.Head.LeftEyeBrow"*/-30, /*"iCat.Head.RightEyeBrow"*/-30,
/*"iCat.Head.LeftEyeLid"*/65, /*"iCat.Head.RightEyeLid"*/65,
/*"iCat.Head.LeftEyeHorizontal"*/0, /*"iCat.Head.RightEyeHorizontal"*/0, /*"iCat.Head.BothEyesVertical"*/0,
/*"iCat.Head.UpperLeftLip"*/100, /*"iCat.Head.BottomLeftLip"*/100,
/*"iCat.Head.UpperRightLip"*/100, /*"iCat.Head.BottomRightLip"*/100,
/*"iCat.Neck"*/40, /*"iCat.Body"*/0,
/*"iCat.Base.LeftPaw.Light.Red"*/0, /*"iCat.Base.LeftPaw.Light.Green"*/0, /*"iCat.Base.LeftPaw.Light.Blue"*/0, 
/*"iCat.Base.RightPaw.Light.Red"*/0, /*"iCat.Base.RightPaw.Light.Green"*/0, /*"iCat.Base.RightPaw.Light.Blue"*/0,
/*"iCat.Speech"*/0, /*"iCat.Sound.Channel1"*/0, /*"iCat.Sound.Channel2"*/0 	};

ICatMood::ICatMood(AnimationModuleInterface *anim) {
	_animation = anim;
	_valence = 1;
	_preValence = 1;
	_decay = 0;
	setState(MOOD_HAPPY);
	//_animation.update(_variableState);
}

ICatMood::~ICatMood(void){
}

void ICatMood::createGlobalVars(void){
	_animation->CreateGlobalVar(EYEBROWS, "i");
	_animation->CreateGlobalVar(UPPER_LIP, "i");
	_animation->CreateGlobalVar(BOTTOM_LIP, "i");
	_animation->CreateGlobalVar(EYELIDS, "i");
}

void ICatMood::setState(const int newState[28]){
	int i;
	for (i =0; i < 28; i++) {
		_variableState[i] = newState[i];
	}
}

void ICatMood::calcEmotionEyeBrows() {
	int i =0;
	double aux = 0.0;

	for (i = 6; i <= 7; i++) {
		if (_valence >0) 
			aux = MOOD_HAPPY[i]*(_valence/100.0);
		else 
			aux = MOOD_FRUSTRATED[i]*(_valence/100.0)*(-1);
		_variableState[i] = (int)aux;
	}

	_animation->SetGlobalVar(EYEBROWS, _variableState[6], 1);
}

void ICatMood::calcEmotionEyeLids() {
	int i =0;
	double aux = 0.0;

	for (i = 8; i <= 9; i++) {
		if (_valence >= 0) 
			aux = _valence* ((MOOD_HAPPY[i] - 65.0)/100.0) + 65.0;
		else {
			aux = _valence*0.2 + 65.0;
		}
		_variableState[i] = (int)aux;
	}
	_animation->SetGlobalVar(EYELIDS, _variableState[8], 1);
}


void ICatMood::calcEmotionLips() {
	int i =0;
	double aux = 0.0;

	for (i = 13; i < 17; i++) {
		if (_valence >0) 
			aux = MOOD_HAPPY[i]*(_valence/100.0);
		else 
			aux = MOOD_FRUSTRATED[i]*(_valence/100.0)*(-1);

		_variableState[i] = (int)aux;
	}

	_animation->SetGlobalVar(UPPER_LIP, _variableState[13], 1);
	_animation->SetGlobalVar(BOTTOM_LIP, _variableState[14], 1);

}

void ICatMood::updateEmotion(){

	if (_preValence == 0 && _decay > MAX_DECAY) {
		Console::WriteLine(L"*decay*");
		if (_valence > 0) 
			_valence--;
		else
			_valence++;
		_decay = 0;
	}
	else {
		//Console::WriteLine(L"ANTES: preValence: " + _preValence + "valence: " + _valence);

		if (_preValence > 0 && _valence >= 0) {
			_valence++;
			_preValence--;
		}
		else if (_preValence < 0 && _valence < 0) {  //valence < 0
			_valence--;
			_preValence++;
		}
		else if (_preValence < 0 && _valence >= 0) {
			_valence--;
			_preValence++;
		}
		else if (_preValence > 0 && _valence < 0) {
			_valence++;
			_preValence--;
		}
		_decay++;
	}

	Console::WriteLine(L"preValence: " + _preValence + "  valence: " + _valence);
	//	Console::WriteLine(L"****************");

	if (_valence > 100)
		_valence = 100;
	if (_valence < -100)
		_valence = -100;

	calcEmotionEyeBrows();
	calcEmotionEyeLids();
	calcEmotionLips();
}

void ICatMood::setValence(int newValence) {
	//_preValence = valence; **PROBLEMA AQUI!**
	Console::WriteLine(L"newValence: " + newValence); 
	if (_valence > newValence) {
		_preValence = - (_valence - newValence);
	}
	else if (_valence < newValence) {
		_preValence = newValence - _valence;
	}
	_decay = 0;
}

int ICatMood::getValence() {
	return _valence;
}

void ICatMood::incValence(int points) {
	_preValence = _preValence + points;
	_decay = 0;
}

