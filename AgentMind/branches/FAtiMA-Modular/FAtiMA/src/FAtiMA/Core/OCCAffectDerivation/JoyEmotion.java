package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.util.enumerables.EmotionValence;

public class JoyEmotion extends EmotionType {
	
   private static final String[] appraisalVariables = {OCCComponent.DESIRABILITY};
 
   // Private constructor prevents instantiation from other classes
   private JoyEmotion() {
	   super("Joy",appraisalVariables,EmotionValence.POSITIVE);
   }
 
   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder { 
     public static final JoyEmotion INSTANCE = new JoyEmotion();
   }
 
   public static JoyEmotion getInstance() 
   {
     return SingletonHolder.INSTANCE;
   }	 
}
