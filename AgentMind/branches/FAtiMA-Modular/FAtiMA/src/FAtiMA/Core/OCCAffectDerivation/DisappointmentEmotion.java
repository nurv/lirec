package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class DisappointmentEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.GOALCONDUCIVENESS,OCCComponent.GOALSTATUS};
 
   // Private constructor prevents instantiation from other classes
   private DisappointmentEmotion() {
	   super("Disappointment",appraisalVariables,EmotionValence.NEGATIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final DisappointmentEmotion INSTANCE = new DisappointmentEmotion();
   }
 
   public static DisappointmentEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
