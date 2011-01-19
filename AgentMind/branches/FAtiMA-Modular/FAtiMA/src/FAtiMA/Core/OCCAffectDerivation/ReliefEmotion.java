package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class ReliefEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.GOALCONDUCIVENESS,OCCComponent.GOALSTATUS};
 
   // Private constructor prevents instantiation from other classes
   private ReliefEmotion() {
	   super("Relief",appraisalVariables,EmotionValence.POSITIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final ReliefEmotion INSTANCE = new ReliefEmotion();
   }
 
   public static ReliefEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
