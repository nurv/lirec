package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class GloatingEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.DESIRABILITY,OCCComponent.DESFOROTHER};
 
   // Private constructor prevents instantiation from other classes
   private GloatingEmotion() {
	   super("Gloating",appraisalVariables,EmotionValence.POSITIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final GloatingEmotion INSTANCE = new GloatingEmotion();
   }
 
   public static GloatingEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
