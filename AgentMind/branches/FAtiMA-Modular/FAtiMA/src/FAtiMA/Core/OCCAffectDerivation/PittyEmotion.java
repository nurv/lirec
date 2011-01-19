package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class PittyEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.DESIRABILITY,OCCComponent.DESFOROTHER};
 
   // Private constructor prevents instantiation from other classes
   private PittyEmotion() {
	   super("Pitty",appraisalVariables,EmotionValence.NEGATIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final PittyEmotion INSTANCE = new PittyEmotion();
   }
 
   public static PittyEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
