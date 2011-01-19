package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class ReproachEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.PRAISEWORTHINESS};
 
   // Private constructor prevents instantiation from other classes
   private ReproachEmotion() {
	   super("Reproach",appraisalVariables,EmotionValence.NEGATIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final ReproachEmotion INSTANCE = new ReproachEmotion();
   }
 
   public static ReproachEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
