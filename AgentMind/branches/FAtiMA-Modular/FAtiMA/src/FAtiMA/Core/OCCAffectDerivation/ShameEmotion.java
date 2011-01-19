package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class ShameEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.PRAISEWORTHINESS};
 
   // Private constructor prevents instantiation from other classes
   private ShameEmotion() {
	   super("Shame",appraisalVariables,EmotionValence.NEGATIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final ShameEmotion INSTANCE = new ShameEmotion();
   }
 
   public static ShameEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
