package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class AdmirationEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.PRAISEWORTHINESS};
 
   // Private constructor prevents instantiation from other classes
   private AdmirationEmotion() {
	   super("Admiration",appraisalVariables,EmotionValence.POSITIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final AdmirationEmotion INSTANCE = new AdmirationEmotion();
   }
 
   public static AdmirationEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
