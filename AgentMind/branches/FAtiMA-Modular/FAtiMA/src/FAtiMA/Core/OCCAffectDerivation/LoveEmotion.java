package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class LoveEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.LIKE};
 
   // Private constructor prevents instantiation from other classes
   private LoveEmotion() {
	   super("Love",appraisalVariables,EmotionValence.POSITIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final LoveEmotion INSTANCE = new LoveEmotion();
   }
 
   public static LoveEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
