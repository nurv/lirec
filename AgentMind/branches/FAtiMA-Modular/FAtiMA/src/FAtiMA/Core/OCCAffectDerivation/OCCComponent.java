package FAtiMA.Core.OCCAffectDerivation;

import java.util.ArrayList;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IAffectDerivationComponent;
import FAtiMA.Core.IComponent;
import FAtiMA.Core.IModelOfOtherComponent;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.emotionalState.EmotionDisposition;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.enumerables.EmotionType;
import FAtiMA.Core.wellFormedNames.Name;

public class OCCComponent implements IAffectDerivationComponent, IModelOfOtherComponent {
	
	public static final String NAME = "OCC";
	public static final String LIKE = "like";
	public static final String DESIRABILITY = "desirability";
	public static final String PRAISEWORTHINESS = "praiseworthiness";
	public static final String GOALSTATUS = "GoalStatus";
	public static final String GOALCONDUCIVENESS = "GoalConduciveness";
	public static final String SUCCESSPROBABILITY = "SuccessProbability";
	public static final String FAILUREPROBABILITY = "FailureProbability";
	public static final String DESFOROTHER = "DesFor-";
	public static final int GOALCONFIRMED = 1;
	public static final int GOALUNCONFIRMED = 0;
	public static final int GOALDISCONFIRMED = 2;
	
	public OCCComponent()
	{
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void initialize(AgentModel am) {
	}

	@Override
	public void reset() {
	}

	@Override
	public void updateCycle(AgentModel am, long time) {
	}

	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return null;
	}
	
	public static String getHopeKey(Event e)
	{
		return e + "-" + GOALCONDUCIVENESS + "-" + SUCCESSPROBABILITY;
	}
	
	public static String getFearKey(Event e)
	{
		return e + "-" + GOALCONDUCIVENESS + "-" + FAILUREPROBABILITY;
	}
	
	
	@Override
	public ArrayList<BaseEmotion> deriveEmotions(AgentModel am, String appraisalVariable, AppraisalFrame af) {
		
		ArrayList<BaseEmotion> emotions = new ArrayList<BaseEmotion>();
		Event event = af.getEvent();
		ActiveEmotion fear;
		ActiveEmotion hope;
		float status;
		
		float value = af.getAppraisalVariable(appraisalVariable);
		
		if(appraisalVariable.equals(DESIRABILITY))
		{
			if(value!=0)
			{
				emotions.add(OCCAppraiseWellBeing(event, value));
				String other;
				float desirabilityForOther;
				
				for(String variable : af.getAppraisalVariables())
				{
					if(variable.startsWith(DESFOROTHER))
					{
						other = variable.substring(DESFOROTHER.length());
						desirabilityForOther = af.getAppraisalVariable(variable);
						if(desirabilityForOther != 0)
						{
							emotions.add(OCCAppraiseFortuneOfOthers(event, value, desirabilityForOther, other));
						}
						
					}
				}
			}
		}
		else if(appraisalVariable.equals(PRAISEWORTHINESS))
		{
			if(value!=0)
			{
				emotions.add(OCCAppraisePraiseworthiness(am.getName(), event, value));
			}
		}
		else if(appraisalVariable.equals(LIKE))
		{
			if(value!=0)
			{
				emotions.add(OCCAppraiseAttribution(event, value));
			}
		}
		else if(appraisalVariable.equals(SUCCESSPROBABILITY))
		{
			status = af.getAppraisalVariable(GOALSTATUS);
			float goalConduciveness = af.getAppraisalVariable(GOALCONDUCIVENESS);
			if(goalConduciveness != 0)
			{
				if(status == GOALUNCONFIRMED)
				{
					emotions.add(AppraiseGoalSuccessProbability(am, event, goalConduciveness, value));
				}
			}
		}
		else if(appraisalVariable.equals(FAILUREPROBABILITY))
		{
			status = af.getAppraisalVariable(GOALSTATUS);
			float goalConduciveness = af.getAppraisalVariable(GOALCONDUCIVENESS);
			if(goalConduciveness != 0)
			{
				if(status == GOALUNCONFIRMED)
				{
					emotions.add(AppraiseGoalFailureProbability(am, event, goalConduciveness, value));
				}
			}
		}
		else if(appraisalVariable.equals(GOALCONDUCIVENESS))
		{
			if(value!=0)
			{
				status = af.getAppraisalVariable(GOALSTATUS);
				if(status == GOALUNCONFIRMED)
				{
					float prob = af.getAppraisalVariable(SUCCESSPROBABILITY);
					if(prob != 0)
					{
						emotions.add(AppraiseGoalSuccessProbability(am, event, value, prob));
					}
					
					prob = af.getAppraisalVariable(FAILUREPROBABILITY);
					if(prob != 0)
					{
						emotions.add(AppraiseGoalFailureProbability(am, event, value, prob));
					}
				}
				else if(status == GOALCONFIRMED)
				{
					float conduciveness = af.getAppraisalVariable(GOALCONDUCIVENESS);
					if(conduciveness != 0)
					{
						fear = am.getEmotionalState().GetEmotion(getFearKey(event));
						hope = am.getEmotionalState().GetEmotion(getHopeKey(event));
						if(fear!= null || hope != null)
						{
							emotions.add(AppraiseGoalSuccess(am, hope, fear, conduciveness,af.getEvent()));
						}
					}
					
				}
				else if(status == GOALDISCONFIRMED)
				{
					float conduciveness = af.getAppraisalVariable(GOALCONDUCIVENESS);
					if(conduciveness != 0)
					{
						fear = am.getEmotionalState().GetEmotion(getFearKey(event));
						hope = am.getEmotionalState().GetEmotion(getHopeKey(event));
						if(fear!= null || hope != null)
						{
							emotions.add(AppraiseGoalFailure(am, hope, fear, conduciveness,af.getEvent()));
						}
					}
				}
			}
			else if(appraisalVariable.startsWith(DESFOROTHER))
			{
				if(value!=0)
				{
					String other = appraisalVariable.substring(DESFOROTHER.length());
					float desirability = af.getAppraisalVariable(DESIRABILITY);
					
					emotions.add(OCCAppraiseFortuneOfOthers(event, desirability, value, other));
				}
			}
		}
		
		
		return emotions;
	}
	
	@Override
	public void inverseDeriveEmotions(AgentModel am, BaseEmotion em, AppraisalFrame af)
	{
		//ignoring mood for now
		EmotionDisposition disposition = am.getEmotionalState().getEmotionDispositions()[em.GetType()];
		
		int threshold = disposition.GetThreshold();
		float potentialValue = em.GetPotential() + threshold; 
		
		if(em.GetType() == EmotionType.LOVE)
		{
			af.SetAppraisalVariable("", (short)1, LIKE, potentialValue * 1.43f);
		}
		else if(em.GetType() == EmotionType.HATE)
		{
			af.SetAppraisalVariable("", (short)1, LIKE, -potentialValue * 1.43f);
		}
		else if(em.GetType() == EmotionType.JOY)
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, potentialValue);
		}
		else if(em.GetType() == EmotionType.DISTRESS)
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, -potentialValue);
		}
		else if(em.GetType() == EmotionType.PRIDE || em.GetType() == EmotionType.ADMIRATION)
		{
			af.SetAppraisalVariable("", (short)1, PRAISEWORTHINESS, potentialValue);
		}
		else if(em.GetType() == EmotionType.SHAME || em.GetType() == EmotionType.REPROACH)
		{
			af.SetAppraisalVariable("", (short)1, PRAISEWORTHINESS, -potentialValue);
		}
		else if(em.GetType() == EmotionType.GLOATING)
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, potentialValue);
			//vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, -potentialValue);
		}
		else if(em.GetType() == EmotionType.HAPPYFOR)
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, potentialValue);
			//vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, potentialValue);
		}
		else if(em.GetType() == EmotionType.PITTY)
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, -potentialValue);
			//vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, -potentialValue);
		}
		else if(em.GetType() == EmotionType.RESENTMENT)
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, -potentialValue);
			//vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, potentialValue);
		}
		else if(em.GetType() == EmotionType.GRATIFICATION || em.GetType() == EmotionType.GRATITUDE)
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, potentialValue);
			af.SetAppraisalVariable("", (short)1, PRAISEWORTHINESS, potentialValue);
		}
		else if(em.GetType() == EmotionType.REGRET || em.GetType() == EmotionType.ANGER)
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, -potentialValue);
			af.SetAppraisalVariable("", (short)1, PRAISEWORTHINESS, -potentialValue);
		}
	}
	
	private static BaseEmotion OCCAppraiseAttribution(Event event, float like)
	{
		BaseEmotion em;
		ArrayList<String> appraisalVariables = new ArrayList<String>();
		appraisalVariables.add(LIKE);
		
		if(like >= 0) {
			em = new BaseEmotion(EmotionType.LOVE, like*0.7f, appraisalVariables, event, Name.ParseName(event.GetTarget()));
		}
		else {
			em = new BaseEmotion(EmotionType.HATE, -like*0.7f, appraisalVariables, event, Name.ParseName(event.GetTarget()));
		}
		
		return em;
	}
	
	private static BaseEmotion OCCAppraiseWellBeing(Event event, float desirability) {
		BaseEmotion em;
		ArrayList<String> appraisalVariables = new ArrayList<String>();
		appraisalVariables.add(DESIRABILITY);
		
		if(desirability >= 0) {
			em = new BaseEmotion(EmotionType.JOY, desirability, appraisalVariables, event, null);
		}
		else {
			em = new BaseEmotion(EmotionType.DISTRESS, -desirability, appraisalVariables, event, null);
		}
		return em;
	}
	
	private static BaseEmotion OCCAppraisePraiseworthiness(String appraisingAgent, Event event, float praiseworthiness) {
		BaseEmotion em;
		ArrayList<String> appraisalVariables = new ArrayList<String>();
		appraisalVariables.add(PRAISEWORTHINESS);
		
		if(praiseworthiness >= 0) {
			if(event.GetSubject().equals(appraisingAgent)) {
				em = new BaseEmotion(EmotionType.PRIDE, praiseworthiness, appraisalVariables, event, Name.ParseName("SELF"));
			}
			else {
				em = new BaseEmotion(EmotionType.ADMIRATION, praiseworthiness, appraisalVariables, event, Name.ParseName(event.GetSubject()));
			}
		}
		else {
			if(event.GetSubject().equals(appraisingAgent)) {
				em = new BaseEmotion(EmotionType.SHAME, -praiseworthiness, appraisalVariables, event, Name.ParseName("SELF"));
			}
			else {
				em = new BaseEmotion(EmotionType.REPROACH, -praiseworthiness, appraisalVariables, event, Name.ParseName(event.GetSubject()));
			}
		}
		
		return em;
	}
	
	/*private static BaseEmotion GenerateEmotionForOther(AgentModel am, Event event, float desirability, AppraisalFrame appraisalOfOther, String other)
	{
		float desirabilityForOther = appraisalOfOther.getAppraisalVariable(DESIRABILITY);
			
		
		if(desirabilityForOther == 0) return null;
		
		//TODO move this code to the social relations component appraisal 
		/*targetBias = LikeRelation.getRelation(Constants.SELF,other).getValue(am.getMemory()) * desirabilityForOther/10;
		bias = targetBias;
		if(!subject.equals(Constants.SELF))
		{
				subjectBias = LikeRelation.getRelation(Constants.SELF, subject).getValue(am.getMemory());
				bias = (bias + subjectBias)/2;
		}
		
		newDesirability = (desirability + bias)/2;*/

		
		//return OCCAppraiseFortuneOfOthers(event, desirability, desirabilityForOther, other);
	//}
	
	/**
	 * Appraises a Goal's likelihood of failure
	 * @param g - the goal
	 * @param probability - the probability of the goal to fail
	 * @return - the emotion created
	 */
	public static BaseEmotion AppraiseGoalFailureProbability(AgentModel am , Event e, float goalConduciveness, float prob)
	{
		float potential;
		potential = prob * goalConduciveness;
		ArrayList<String> appraisalVariables = new ArrayList<String>();
		appraisalVariables.add(GOALCONDUCIVENESS);
		appraisalVariables.add(FAILUREPROBABILITY);
		
		BaseEmotion em = new  BaseEmotion(EmotionType.FEAR, potential, appraisalVariables, e, null);
		
		return em;
	}
	
	/**
	 * Appraises a Goal's likelihood of succeeding
	 * @param g - the goal
	 * @param probability - the probability of the goal to succeed
	 * @return - the BaseEmotion created
	 */
	public static BaseEmotion AppraiseGoalSuccessProbability(AgentModel am, Event e, float goalConduciveness, float prob) {
	
		float potential;
		potential = prob * goalConduciveness;
		ArrayList<String> appraisalVariables = new ArrayList<String>();
		appraisalVariables.add(GOALCONDUCIVENESS);
		appraisalVariables.add(SUCCESSPROBABILITY);
		
		BaseEmotion em = new BaseEmotion(EmotionType.HOPE, potential, appraisalVariables, e, null);
	
		return em;
	}
	
	/**
	 * Appraises a Goal's Failure according to the emotions that the agent is experiencing
	 * @param hopeEmotion - the emotion of Hope for achieving the goal that the character feels
	 * @param fearEmotion - the emotion of Fear for not achieving the goal that the character feels
	 * @param g - the Goal that failed
	 */
	public static BaseEmotion AppraiseGoalFailure(AgentModel am, ActiveEmotion hopeEmotion, ActiveEmotion fearEmotion, float conduciveness, Event e) {
		return AppraiseGoalEnd(EmotionType.DISAPPOINTMENT,EmotionType.FEARSCONFIRMED,hopeEmotion,fearEmotion,conduciveness,e);
	}
	
	

	/**
	 * Appraises a Goal's success according to the emotions that the agent is experiencing
	 * @param hopeEmotion - the emotion of Hope for achieving the goal that the character feels
	 * @param fearEmotion - the emotion of Fear for not achieving the goal that the character feels
	 * @param g - the Goal that succeeded
	 * @return - the emotion created
	 */
	public static BaseEmotion AppraiseGoalSuccess(AgentModel am, ActiveEmotion hopeEmotion, ActiveEmotion fearEmotion, float conduciveness, Event e) {
		return AppraiseGoalEnd(EmotionType.SATISFACTION,EmotionType.RELIEF,hopeEmotion,fearEmotion,conduciveness,e);
	}

	
	
	private static BaseEmotion OCCAppraiseFortuneOfOthers(Event event, float desirability, float desirabilityForOther, String target) {
		BaseEmotion em;
		float potential;
		ArrayList<String> appraisalVariables = new ArrayList<String>();
		appraisalVariables.add(DESIRABILITY);
		appraisalVariables.add(DESFOROTHER + target);
		
		
		potential = (Math.abs(desirabilityForOther) + Math.abs(desirability)) / 2.0f;
		
		if(desirability >= 0) {
			if(desirabilityForOther >= 0) {
				em = new BaseEmotion(EmotionType.HAPPYFOR, potential, appraisalVariables, event, Name.ParseName(target));	
			}
			else {
				em = new BaseEmotion(EmotionType.GLOATING, potential, appraisalVariables, event, Name.ParseName(target));
			}
		}
		else {
			if(desirabilityForOther >= 0) {
				em = new BaseEmotion(EmotionType.RESENTMENT, potential, appraisalVariables, event, Name.ParseName(target));
			}
			else {
				em = new BaseEmotion(EmotionType.PITTY, potential, appraisalVariables, event, Name.ParseName(target));
			}
		}
		
		return em;
	}
	
	private static BaseEmotion AppraiseGoalEnd(short hopefullOutcome, short fearfullOutcome, 
			ActiveEmotion hopeEmotion, ActiveEmotion fearEmotion, float goalConduciveness, Event e) {
	
		short finalEmotion;
		float potential = 0;
		
		ArrayList<String> appraisalVariables = new ArrayList<String>();
		appraisalVariables.add(GOALCONDUCIVENESS);
		appraisalVariables.add(GOALSTATUS);
		
		if(hopeEmotion != null) {
			if(fearEmotion != null && fearEmotion.GetIntensity() > hopeEmotion.GetIntensity()) {
				potential = fearEmotion.GetPotential();
				finalEmotion = fearfullOutcome;
			}
			else {
				potential = hopeEmotion.GetPotential();
				finalEmotion = hopefullOutcome;
			}
		}
		else if(fearEmotion != null) {
			potential = fearEmotion.GetPotential();
			finalEmotion = fearfullOutcome;
		}
		else return null;
		
		//Change, the goal importance now affects 66% of the final potential value for the emotion
		potential = (potential +  2* goalConduciveness) / 3;
		
		return new BaseEmotion(finalEmotion, potential, appraisalVariables, e, null);
	}

	@Override
	public IComponent createModelOfOther() {
		return new OCCComponent();
	}
}
