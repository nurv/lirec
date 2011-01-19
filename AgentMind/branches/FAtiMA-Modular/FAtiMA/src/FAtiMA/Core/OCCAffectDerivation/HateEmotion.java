package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class HateEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.LIKE};
 
   // Private constructor prevents instantiation from other classes
   private HateEmotion() {
	   super("Hate",appraisalVariables,EmotionValence.NEGATIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final HateEmotion INSTANCE = new HateEmotion();
   }
 
   public static HateEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
