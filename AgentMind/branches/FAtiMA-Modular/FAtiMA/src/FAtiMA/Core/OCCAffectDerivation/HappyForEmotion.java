package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class HappyForEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.DESIRABILITY,OCCComponent.DESFOROTHER};
 
   // Private constructor prevents instantiation from other classes
   private HappyForEmotion() {
	   super("Happy-For",appraisalVariables,EmotionValence.POSITIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final HappyForEmotion INSTANCE = new HappyForEmotion();
   }
 
   public static HappyForEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
