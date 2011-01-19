package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class SatisfactionEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.GOALCONDUCIVENESS,OCCComponent.GOALCONDUCIVENESS};
 
   // Private constructor prevents instantiation from other classes
   private SatisfactionEmotion() {
	   super("Satisfaction",appraisalVariables,EmotionValence.POSITIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final SatisfactionEmotion INSTANCE = new SatisfactionEmotion();
   }
 
   public static SatisfactionEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
