#include "StateMachine.h"
#include "stdafx.h"

/* valores anteriores
int idle_front_weights[7]={60,60,10,10,10,10,5};
int idle_left_weights[6]={60,60,10,10,10,10};
int idle_right_weights[6]={60,60,10,10,10,10};
int interactive_weights[6]={60,30,45,5,10,10};
*/

//novos pesos - empatia

int idle_front_weights[7]={20,40,10,10,10,10,5};
int idle_left_weights[6]={60,60,60,60,60,60}; //LADO DO COMPANHEIRO?
int idle_right_weights[6]={30,30,10,10,10,10};
int interactive_weights[6]={60,30,15,5,20,5};

const char *idle_front_animations[]={"\\STANDARD\\Modes\\Idle\\IdleFrontStill.raf",    "\\STANDARD\\Modes\\Idle\\IdleFrontBlink.raf",
                 "\\STANDARD\\Modes\\Idle\\IdleFrontLookDown.raf", "\\STANDARD\\Modes\\Idle\\IdleFrontLookUp.raf",
                 "\\STANDARD\\Modes\\Idle\\IdleFrontLookLeft.raf", "\\STANDARD\\Modes\\Idle\\IdleFrontLookRight.raf",
                 "\\STANDARD\\Modes\\Idle\\IdleFrontYawn.raf"};
const char *idle_left_animations[]={"\\STANDARD\\Modes\\Idle\\IdleLeftStill.raf",    "\\STANDARD\\Modes\\Idle\\IdleLeftBlink.raf",
                 "\\STANDARD\\Modes\\Idle\\IdleLeftLookDown.raf", "\\STANDARD\\Modes\\Idle\\IdleLeftLookUp.raf",
                 "\\STANDARD\\Modes\\Idle\\IdleLeftLookLeft.raf", "\\STANDARD\\Modes\\Idle\\IdleLeftLookRight.raf"};
const char *idle_right_animations[]={"\\STANDARD\\Modes\\Idle\\IdleRightStill.raf",    "\\STANDARD\\Modes\\Idle\\IdleRightBlink.raf",
                 "\\STANDARD\\Modes\\Idle\\IdleRightLookDown.raf", "\\STANDARD\\Modes\\Idle\\IdleRightLookUp.raf",
                 "\\STANDARD\\Modes\\Idle\\IdleRightLookLeft.raf", "\\STANDARD\\Modes\\Idle\\IdleRightLookRight.raf"};

const char *interactive_animations[]={"\\STANDARD\\Empathy\\IntrStill.raf",    "\\STANDARD\\Empathy\\IntrBlink.raf",
                 "\\STANDARD\\Empathy\\IntrLookFront.raf", "\\STANDARD\\Empathy\\IntrLookUp.raf",
                 "\\STANDARD\\Empathy\\IntrLookLeft.raf", "\\STANDARD\\Empathy\\IntrLookRight.raf"};

//AnimationTable idle_front_table;

//idle_front_table.weights=idle_front_weights;

//idle_front_table.weights[7]={60,60,10,10,10,10,5};
/*idle_front_table.weights= {60, 60, 10, 10, 10, 10, 5};
idle_front_table.animations = {"\\STANDARD\\Modes\\Idle\\IdleFrontStill.raf",    "\\STANDARD\\Modes\\Idle\\IdleFrontBlink.raf",
                 "\\STANDARD\\Modes\\Idle\\IdleFrontLookDown.raf", "\\STANDARD\\Modes\\Idle\\IdleFrontLookUp.raf",
                 "\\STANDARD\\Modes\\Idle\\IdleFrontLookLeft.raf", "\\STANDARD\\Modes\\Idle\\IdleFrontLookRight.raf",
                 "\\STANDARD\\Modes\\Idle\\IdleFrontYawn.raf"};
*/

StateMachine::StateMachine(){
	_currentState=INTRO01;
	_nextState=0;
	_doTransition=false;
	_sleepingNods=0;
	_timeout=0;
	_idle_front_table.size=7;
	_idle_front_table.animations=idle_front_animations;
	_idle_front_table.weights=idle_front_weights;
	_idle_left_table.size=6;
	_idle_left_table.animations=idle_left_animations;
	_idle_left_table.weights=idle_left_weights;
	_idle_right_table.size=6;
	_idle_right_table.animations=idle_right_animations;
	_idle_right_table.weights=idle_right_weights;
	_interactive_table.size=6;
	_interactive_table.animations=interactive_animations;
	_interactive_table.weights=interactive_weights;
	_prevChessState=99;
	//idle_front_table.weights[7];
	//={60,60,10,10,10,10,5};
}


void StateMachine::Reset(){
	_currentAnimation="";
	_currentState=INTERACTIVE;
	_sleepingNods=0;
	_timeout=0;
	_prevChessState=99;
}

bool StateMachine::ThrowDice(int threeshold){
	if ((rand()%1000)<threeshold)
		return true;
	else
		return false;
}

const char * StateMachine::SelectRandom(AnimationTable at){
	int total_w=0, dice, sum,i;

	for (i=0; i < at.size; i++)
		total_w= total_w + at.weights[i];

	dice= rand()%total_w;

	sum=at.weights[0];
	i=0;
	while(sum<dice){
		i++;
		sum=sum+at.weights[i];	
	}

	return at.animations[i];
}

void StateMachine::GotoState(int nextstate, bool timeout)
{
	if (_currentState!= nextstate){
		_doTransition=true;
		_nextState=nextstate;
		
		if(timeout== true)
			_timeout=0;
	}

}

void StateMachine::setTimeOut(int value)
{
	_timeout=value;
}

int StateMachine::getTimeOut()
{
	return _timeout;
}

int StateMachine::getState(){
	return _currentState;
}

int StateMachine::getNextState(){
	return _nextState;
}

void StateMachine::update(int state, AnimationModuleInterface * _animation, int channel){

	if(_prevChessState!=state){
		GotoState(INTERACTIVE,true);
		_currentAnimation=(char *)GetNextAnimation().pszGetPointer();
		_animation->PlayAnimation((char *)_currentAnimation.pszGetPointer() ,channel);
	}
	else {
	setTimeOut(getTimeOut() + 1);
	if (getState() == INTERACTIVE){
		if (getTimeOut() > 300)
			GotoState(IDLE_LOOK_FRONT, true);
	}

	else if ((getState() == IDLE_LOOK_FRONT) || 
          (getState() == IDLE_LOOK_LEFT) || 
          (getState() == IDLE_LOOK_RIGHT)) 
      if (getTimeOut() > 300) 
		  GotoState(SLEEPING_BY_NODDING, true);
	}
	_prevChessState=state;
}

DMLString StateMachine::GetTransitionAnimation()
{
	DMLString animation;
	animation="";
	//DMLString animation;
   // Check whether a transition needs to be done
	if (_doTransition)
	{	
		if (_nextState == SLEEPING_BY_NODDING){
			if (_currentState == IDLE_LOOK_FRONT){
            animation = "\\STANDARD\\Empathy\\Transitions\\Idle2Sleep.raf";
			_currentState = SLEEPING_BY_NODDING;
			_doTransition = false;
			}

			else if (_currentState == INTERACTIVE){
            animation = "\\STANDARD\\Empathy\\Transitions\\Interactive2Sleep.raf";
            _currentState = SLEEPING_BY_NODDING;
			_doTransition = false;
			}

			else if (_currentState == IDLE_LOOK_LEFT){
            animation = "\\STANDARD\\Modes\\Idle\\IdleTurnLeftReturn.raf";
            _currentState = IDLE_LOOK_FRONT;
            GotoState(SLEEPING_BY_NODDING, true);
			}
            
			else if (_currentState == IDLE_LOOK_RIGHT){
            animation = "\\STANDARD\\Modes\\Idle\\IdleTurnRightReturn.raf";
            _currentState = IDLE_LOOK_FRONT;
            GotoState(SLEEPING_BY_NODDING, true);
			}
		}
     
		else if (_nextState == SLEEPING_BY_FLASHING){
         animation = "\\STANDARD\\Modes\\Sleeping\\SleepFlash.raf";
         _currentState = SLEEPING_BY_FLASHING;
         _doTransition = false;
		}
         
		else if (_nextState == IDLE_LOOK_FRONT){
         if (_currentState == SLEEPING_BY_NODDING ||
			 _currentState == SLEEPING_BY_FLASHING){
            animation = "\\STANDARD\\Empathy\\Transitions\\Sleep2Idle.raf";
            _currentState = IDLE_LOOK_FRONT;
            _doTransition = false;  
		 }
		else if (_currentState == INTERACTIVE){
            animation = "\\STANDARD\\Empathy\\Transitions\\Interactive2Idle.raf";
            _currentState = IDLE_LOOK_FRONT;
            _doTransition = false;
		}
 
		else if (_currentState == IDLE_LOOK_LEFT){
            animation = "\\STANDARD\\Modes\\Idle\\IdleTurnLeftReturn.raf";
            _currentState = IDLE_LOOK_FRONT;
            _doTransition = false;           
		}
		else if (_currentState == IDLE_LOOK_RIGHT){
            animation = "\\STANDARD\\Modes\\Idle\\IdleTurnRightReturn.raf";
            _currentState = IDLE_LOOK_FRONT;
            _doTransition = false;                       
		}
		}   
		else if (_nextState == INTERACTIVE){
			if (_currentState == INTRO03){
            animation = ""; // no transition animation is needed
            _currentState = INTERACTIVE;
            _doTransition = false;
			}
         else if ((_currentState == SLEEPING_BY_NODDING)||
			 (_currentState == SLEEPING_BY_FLASHING)){
            animation = "\\STANDARD\\Empathy\\Transitions\\Sleep2Interactive.raf";
            _currentState = INTERACTIVE;
            _doTransition = false;
			}

		 else if (_currentState == IDLE_LOOK_FRONT){
            animation = "\\STANDARD\\Empathy\\Transitions\\Idle2Interactive.raf";
            _currentState = INTERACTIVE;
			GotoState(INTERACTIVE, true);
		 }
            
		 else if (_currentState == IDLE_LOOK_LEFT){
            animation = "\\STANDARD\\Modes\\Idle\\IdleTurnLeftReturn.raf";
            _currentState = IDLE_LOOK_FRONT;
            GotoState(INTERACTIVE, true);
			}
            
		 else if (_currentState == IDLE_LOOK_RIGHT){ 
            animation = "\\STANDARD\\Modes\\Idle\\IdleTurnRightReturn.raf";
            _currentState = IDLE_LOOK_FRONT;
			GotoState(INTERACTIVE, true);
		 }
		}
		else if (_nextState == IDLE_LOOK_LEFT){
         animation = "\\STANDARD\\Modes\\Idle\\IdleTurnLeft.raf";
         _currentState = IDLE_LOOK_LEFT;
		 _doTransition = false;
		}                      
         
		else if (_nextState == IDLE_LOOK_RIGHT){
         animation = "\\STANDARD\\Modes\\Idle\\IdleTurnRight.raf";
         _currentState = IDLE_LOOK_RIGHT;
		 _doTransition = false;
		}                      
		else{
         animation = "";
         _doTransition = false;
         _currentState = _nextState;
		}         
       
      if (_currentState == SLEEPING_BY_NODDING)
		  _sleepingNods = 0;
   }
   return animation;
}


DMLString StateMachine::GetNextAnimation()
{
   DMLString animation = "";


   // first check whether a transition animation needs to be played
   // if so, play this animation before playing the next action animation
   animation = GetTransitionAnimation();
   if (animation != ""){
	   _currentAnimation=animation;
	   return animation;
   }
      
   // If no transition animation needs to be played then select the next
   // animation to be played in the current state
   if (_currentState == INTRO01){
      animation = "\\STANDARD\\Demo\\Intro01.raf";
	  GotoState (INTRO02, true);
   }

   else if (_currentState == INTRO02){
      animation = "\\STANDARD\\Demo\\Intro02.raf";
      GotoState (INTRO03, true);
   }
   else if (_currentState == INTRO03){
      animation = "\\STANDARD\\Demo\\Intro03.raf";
 	  GotoState (INTERACTIVE, true);
   }
   else if (_currentState == SLEEPING_BY_NODDING){
      animation = "\\STANDARD\\Modes\\Sleeping\\SleepNod.raf";      
      // the transition to an other sleeping animation is
      // done autonomously and by time
      _sleepingNods = _sleepingNods + 1;
      if (_sleepingNods >= 4)
         GotoState (SLEEPING_BY_FLASHING, false);
   }
   else if (_currentState == SLEEPING_BY_FLASHING){
	   animation = "\\STANDARD\\Modes\\Sleeping\\SleepFlash.raf";
   }
   else if (_currentState == IDLE_LOOK_FRONT){
      animation = SelectRandom (_idle_front_table);	  
      // the transition to looking left or right is done autonomously
      // and randomly
      if (ThrowDice(100)) 
         GotoState (IDLE_LOOK_LEFT, false);
      else if (ThrowDice(100))
         GotoState (IDLE_LOOK_RIGHT, false);
   }
   else if (_currentState == IDLE_LOOK_LEFT){
      animation = SelectRandom (_idle_left_table);
      // the transition to looking to the front is done autonomously
      if (ThrowDice(300)) 
         GotoState (IDLE_LOOK_FRONT, false);
   }	     
   else if (_currentState == IDLE_LOOK_RIGHT){ 
      animation = SelectRandom (_idle_right_table);
      // the transition to looking to the front is done autonomously
      if (ThrowDice(300)) 
         GotoState (IDLE_LOOK_FRONT, false);
   }     
   else if (_currentState == INTERACTIVE)
      animation = SelectRandom (_interactive_table);
  
   _currentAnimation=animation;
   return animation;
}

DMLString StateMachine::getCurrentAnimation(){
	return _currentAnimation;
}