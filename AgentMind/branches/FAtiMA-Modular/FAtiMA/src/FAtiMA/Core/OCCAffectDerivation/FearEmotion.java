package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class FearEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.GOALCONDUCIVENESS,OCCComponent.FAILUREPROBABILITY};
 
   // Private constructor prevents instantiation from other classes
   private FearEmotion() {
	   super("Fear",appraisalVariables,EmotionValence.NEGATIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final FearEmotion INSTANCE = new FearEmotion();
   }
 
   public static FearEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
