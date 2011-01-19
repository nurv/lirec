package FAtiMA.Core.OCCAffectDerivation;

import java.io.Serializable;
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
import FAtiMA.Core.emotionalState.EmotionType;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.wellFormedNames.Name;

public class OCCComponent implements Serializable, IAffectDerivationComponent, IModelOfOtherComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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
	public void update(AgentModel am, long time) {
	}
	
	@Override
	public void update(AgentModel am, Event e)
	{
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
	public ArrayList<BaseEmotion> affectDerivation(AgentModel am, AppraisalFrame af) {
		
		ArrayList<BaseEmotion> emotions = new ArrayList<BaseEmotion>();
		Event event = af.getEvent();
		ActiveEmotion fear;
		ActiveEmotion hope;
		float status;
		
		if(af.containsAppraisalVariable(DESIRABILITY))
		{
			float desirability = af.getAppraisalVariable(DESIRABILITY);
			if(desirability!=0)
			{
				emotions.add(OCCAppraiseWellBeing(event, desirability));
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
							emotions.add(OCCAppraiseFortuneOfOthers(event, desirability, desirabilityForOther, other));
						}
						
					}
				}
			}
		}
		if(af.containsAppraisalVariable(PRAISEWORTHINESS))
		{
			float praiseworthiness = af.getAppraisalVariable(PRAISEWORTHINESS);
			if(praiseworthiness!=0)
			{
				emotions.add(OCCAppraisePraiseworthiness(am.getName(), event, praiseworthiness));
			}
		}
		if(af.containsAppraisalVariable(LIKE))
		{
			float like = af.getAppraisalVariable(LIKE);
			if(like!=0)
			{
				emotions.add(OCCAppraiseAttribution(event, like));
			}
		}
		if(af.containsAppraisalVariable(GOALCONDUCIVENESS))
		{
			float goalConduciveness = af.getAppraisalVariable(GOALCONDUCIVENESS);
			if(goalConduciveness!=0)
			{
				status = af.getAppraisalVariable(GOALSTATUS);
				if(status == GOALUNCONFIRMED)
				{
					float prob = af.getAppraisalVariable(SUCCESSPROBABILITY);
					if(prob != 0)
					{
						emotions.add(AppraiseGoalSuccessProbability(am, event, goalConduciveness, prob));
					}
					
					prob = af.getAppraisalVariable(FAILUREPROBABILITY);
					if(prob != 0)
					{
						emotions.add(AppraiseGoalFailureProbability(am, event, goalConduciveness, prob));
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
		}
		
		
		return emotions;
	}
	
	@Override
	public void inverseAffectDerivation(AgentModel am, BaseEmotion em, AppraisalFrame af)
	{
		//ignoring mood for now
		
		EmotionDisposition emotionDisposition = am.getEmotionalState().getEmotionDisposition(em.getType().getName());
		int threshold = emotionDisposition.getThreshold();
		float potentialValue = em.GetPotential() + threshold; 
		
		if(em.getType().equals(LoveEmotion.getInstance()))
		{
			af.SetAppraisalVariable("", (short)1, LIKE, potentialValue * 1.43f);
		}
		else if(em.getType().equals(HateEmotion.getInstance()))
		{
			af.SetAppraisalVariable("", (short)1, LIKE, -potentialValue * 1.43f);
		}
		else if(em.getType().equals(JoyEmotion.getInstance()))
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, potentialValue);
		}
		else if(em.getType().equals(DistressEmotion.getInstance()))
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, -potentialValue);
		}
		else if(em.getType().equals(PrideEmotion.getInstance()) || em.getType().equals(AdmirationEmotion.getInstance()))
		{
			af.SetAppraisalVariable("", (short)1, PRAISEWORTHINESS, potentialValue);
		}
		else if(em.getType().equals(ShameEmotion.getInstance()) || em.getType().equals(ReproachEmotion.getInstance()))
		{
			af.SetAppraisalVariable("", (short)1, PRAISEWORTHINESS, -potentialValue);
		}
		else if(em.getType().equals(GloatingEmotion.getInstance()))
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, potentialValue);
			//vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, -potentialValue);
		}
		else if(em.getType().equals(HappyForEmotion.getInstance()))
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, potentialValue);
			//vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, potentialValue);
		}
		else if(em.getType().equals(PittyEmotion.getInstance()))
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, -potentialValue);
			//vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, -potentialValue);
		}
		else if(em.getType().equals(ResentmentEmotion.getInstance()))
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, -potentialValue);
			//vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, potentialValue);
		}
		/*else if(em.getType() == GratificationEmotioEmotionType.GRATIFICATION || em.GetType() == EmotionType.GRATITUDE)
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, potentialValue);
			af.SetAppraisalVariable("", (short)1, PRAISEWORTHINESS, potentialValue);
		}
		else if(em.GetType() == EmotionType.REGRET || em.GetType() == EmotionType.ANGER)
		{
			af.SetAppraisalVariable("", (short)1, DESIRABILITY, -potentialValue);
			af.SetAppraisalVariable("", (short)1, PRAISEWORTHINESS, -potentialValue);
		}*/
	}
	
	private static BaseEmotion OCCAppraiseAttribution(Event event, float like)
	{
		BaseEmotion em;
		
		if(like >= 0) {
			em = new BaseEmotion(LoveEmotion.getInstance(), like*0.7f, event, Name.ParseName(event.GetTarget()));
		}
		else {
			em = new BaseEmotion(HateEmotion.getInstance(), -like*0.7f, event, Name.ParseName(event.GetTarget()));
		}
		
		return em;
	}
	
	private static BaseEmotion OCCAppraiseWellBeing(Event event, float desirability) {
		BaseEmotion em;
		
		if(desirability >= 0) {
			em = new BaseEmotion(JoyEmotion.getInstance(), desirability, event);
		}
		else {
			em = new BaseEmotion(DistressEmotion.getInstance(), -desirability, event);
		}
		return em;
	}
	
	private static BaseEmotion OCCAppraisePraiseworthiness(String appraisingAgent, Event event, float praiseworthiness) {
		BaseEmotion em;
		
		if(praiseworthiness >= 0) {
			if(event.GetSubject().equals(appraisingAgent)) {
				em = new BaseEmotion(PrideEmotion.getInstance(), praiseworthiness, event, Name.ParseName("SELF"));
			}
			else {
				em = new BaseEmotion(AdmirationEmotion.getInstance(), praiseworthiness, event, Name.ParseName(event.GetSubject()));
			}
		}
		else {
			if(event.GetSubject().equals(appraisingAgent)) {
				em = new BaseEmotion(ShameEmotion.getInstance(), -praiseworthiness, event, Name.ParseName("SELF"));
			}
			else {
				em = new BaseEmotion(ReproachEmotion.getInstance(), -praiseworthiness, event, Name.ParseName(event.GetSubject()));
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
		
		BaseEmotion em = new  BaseEmotion(FearEmotion.getInstance(), potential, e);
		
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
	
		BaseEmotion em = new BaseEmotion(HopeEmotion.getInstance(), potential, e);
	
		return em;
	}
	
	/**
	 * Appraises a Goal's Failure according to the emotions that the agent is experiencing
	 * @param hopeEmotion - the emotion of Hope for achieving the goal that the character feels
	 * @param fearEmotion - the emotion of Fear for not achieving the goal that the character feels
	 * @param g - the Goal that failed
	 */
	public static BaseEmotion AppraiseGoalFailure(AgentModel am, ActiveEmotion hopeEmotion, ActiveEmotion fearEmotion, float conduciveness, Event e) {
		return AppraiseGoalEnd(DisappointmentEmotion.getInstance(),FearsConfirmedEmotion.getInstance(),hopeEmotion,fearEmotion,conduciveness,e);
	}
	
	

	/**
	 * Appraises a Goal's success according to the emotions that the agent is experiencing
	 * @param hopeEmotion - the emotion of Hope for achieving the goal that the character feels
	 * @param fearEmotion - the emotion of Fear for not achieving the goal that the character feels
	 * @param g - the Goal that succeeded
	 * @return - the emotion created
	 */
	public static BaseEmotion AppraiseGoalSuccess(AgentModel am, ActiveEmotion hopeEmotion, ActiveEmotion fearEmotion, float conduciveness, Event e) {
		return AppraiseGoalEnd(SatisfactionEmotion.getInstance(),ReliefEmotion.getInstance(),hopeEmotion,fearEmotion,conduciveness,e);
	}

	
	
	private static BaseEmotion OCCAppraiseFortuneOfOthers(Event event, float desirability, float desirabilityForOther, String target) {
		BaseEmotion em;
		float potential;
		
		potential = (Math.abs(desirabilityForOther) + Math.abs(desirability)) / 2.0f;
		
		if(desirability >= 0) {
			if(desirabilityForOther >= 0) {
				em = new BaseEmotion(HappyForEmotion.getInstance(), potential, event, Name.ParseName(target));	
			}
			else {
				em = new BaseEmotion(GloatingEmotion.getInstance(), potential, event, Name.ParseName(target));
			}
		}
		else {
			if(desirabilityForOther >= 0) {
				em = new BaseEmotion(ResentmentEmotion.getInstance(), potential, event, Name.ParseName(target));
			}
			else {
				em = new BaseEmotion(PittyEmotion.getInstance(), potential, event, Name.ParseName(target));
			}
		}
		
		return em;
	}
	
	private static BaseEmotion AppraiseGoalEnd(EmotionType hopefullOutcome, EmotionType fearfullOutcome, 
			ActiveEmotion hopeEmotion, ActiveEmotion fearEmotion, float goalConduciveness, Event e) {
	
		EmotionType finalEmotion;
		float potential = 0;
		
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
		
		return new BaseEmotion(finalEmotion, potential, e);
	}

	@Override
	public IComponent createModelOfOther() {
		return new OCCComponent();
	}
}
