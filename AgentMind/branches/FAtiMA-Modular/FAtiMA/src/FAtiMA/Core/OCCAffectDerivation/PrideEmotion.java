package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class PrideEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.PRAISEWORTHINESS};
 
   // Private constructor prevents instantiation from other classes
   private PrideEmotion() {
	   super("Pride",appraisalVariables,EmotionValence.POSITIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final PrideEmotion INSTANCE = new PrideEmotion();
   }
 
   public static PrideEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
