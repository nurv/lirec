package FAtiMA.Core.emotionalState;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class NeutralEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {};
 
   // Private constructor prevents instantiation from other classes
   private NeutralEmotion() {
	   super("Neutral",appraisalVariables,EmotionValence.POSITIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final NeutralEmotion INSTANCE = new NeutralEmotion();
   }
 
   public static NeutralEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}

