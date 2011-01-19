package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class HopeEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.GOALCONDUCIVENESS,OCCComponent.SUCCESSPROBABILITY};
 
   // Private constructor prevents instantiation from other classes
   private HopeEmotion() {
	   super("Hope",appraisalVariables,EmotionValence.POSITIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final HopeEmotion INSTANCE = new HopeEmotion();
   }
 
   public static HopeEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
