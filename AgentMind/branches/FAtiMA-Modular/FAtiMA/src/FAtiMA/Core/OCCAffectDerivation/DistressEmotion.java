package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class DistressEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.DESIRABILITY};
 
   // Private constructor prevents instantiation from other classes
   private DistressEmotion() {
	   super("Distress",appraisalVariables,EmotionValence.NEGATIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final DistressEmotion INSTANCE = new DistressEmotion();
   }
 
   public static DistressEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
