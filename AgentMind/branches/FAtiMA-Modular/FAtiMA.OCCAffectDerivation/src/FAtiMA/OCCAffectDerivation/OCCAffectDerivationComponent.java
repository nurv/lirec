package FAtiMA.OCCAffectDerivation;

import java.io.Serializable;
import java.util.ArrayList;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAffectDerivationComponent;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.componentTypes.IModelOfOtherComponent;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.emotionalState.EmotionDisposition;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;

public class OCCAffectDerivationComponent implements Serializable, IAffectDerivationComponent, IModelOfOtherComponent {
	
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "OCC";
	public static final int GOALCONFIRMED = 1;
	public static final int GOALUNCONFIRMED = 0;
	public static final int GOALDISCONFIRMED = 2;
	
	private static OCCBaseEmotion AppraiseGoalEnd(OCCEmotionType hopefullOutcome, OCCEmotionType fearfullOutcome, 
			ActiveEmotion hopeEmotion, ActiveEmotion fearEmotion, float goalConduciveness, Event e) {
	
		OCCEmotionType finalEmotion;
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
		
		return new OCCBaseEmotion(finalEmotion, potential, e);
	}

	private static OCCBaseEmotion OCCAppraiseAttribution(Event event, float like)
	{
		OCCBaseEmotion em;
		
		if(like >= 0) {
			em = new OCCBaseEmotion(OCCEmotionType.LOVE, like*0.7f, event, Name.ParseName(event.GetTarget()));
		}
		else {
			em = new OCCBaseEmotion(OCCEmotionType.HATE, -like*0.7f, event, Name.ParseName(event.GetTarget()));
		}
		
		return em;
	}

	private static OCCBaseEmotion OCCAppraiseFortuneOfOthers(Event event, float desirability, float desirabilityForOther, String target) {
		OCCBaseEmotion em;
		float potential;
		
		potential = (Math.abs(desirabilityForOther) + Math.abs(desirability)) / 2.0f;
		
		if(desirability >= 0) {
			if(desirabilityForOther >= 0) {
				em = new OCCBaseEmotion(OCCEmotionType.HAPPY_FOR, potential, event, Name.ParseName(target));	
			}
			else {
				em = new OCCBaseEmotion(OCCEmotionType.GLOATING,potential, event, Name.ParseName(target));
			}
		}
		else {
			if(desirabilityForOther >= 0) {
				em = new OCCBaseEmotion(OCCEmotionType.RESENTMENT, potential, event, Name.ParseName(target));
			}
			else {
				em = new OCCBaseEmotion(OCCEmotionType.PITTY, potential, event, Name.ParseName(target));
			}
		}
		
		return em;
	}

	private static OCCBaseEmotion OCCAppraisePraiseworthiness(Event event, float praiseworthiness) {
		OCCBaseEmotion em;
		
		if(praiseworthiness >= 0) {
			if(event.GetSubject().equals(Constants.SELF)) {
				em = new OCCBaseEmotion(OCCEmotionType.PRIDE, praiseworthiness, event, Name.ParseName("SELF"));
			}
			else {
				em = new OCCBaseEmotion(OCCEmotionType.ADMIRATION, praiseworthiness, event, Name.ParseName(event.GetSubject()));
			}
		}
		else {
			if(event.GetSubject().equals(Constants.SELF)) {
				em = new OCCBaseEmotion(OCCEmotionType.SHAME, -praiseworthiness, event, Name.ParseName("SELF"));
			}
			else {
				em = new OCCBaseEmotion(OCCEmotionType.REPROACH, -praiseworthiness, event, Name.ParseName(event.GetSubject()));
			}
		}
		
		return em;
	}

	private static OCCBaseEmotion OCCAppraiseWellBeing(Event event, float desirability) {
		OCCBaseEmotion em;
		
		if(desirability >= 0) {
			em = new OCCBaseEmotion(OCCEmotionType.JOY, desirability, event);
		}
		else {
			em = new OCCBaseEmotion(OCCEmotionType.DISTRESS, -desirability, event);
		}
		return em;
	}
	
	/**
	 * Appraises a Goal's Failure according to the emotions that the agent is experiencing
	 * @param hopeEmotion - the emotion of Hope for achieving the goal that the character feels
	 * @param fearEmotion - the emotion of Fear for not achieving the goal that the character feels
	 * @param g - the Goal that failed
	 */
	public static OCCBaseEmotion AppraiseGoalFailure(AgentModel am, ActiveEmotion hopeEmotion, ActiveEmotion fearEmotion, float conduciveness, Event e) {
		return AppraiseGoalEnd(OCCEmotionType.DISAPPOINTMENT,OCCEmotionType.FEARS_CONFIRMED,hopeEmotion,fearEmotion,conduciveness,e);
	}

	/**
	 * Appraises a Goal's likelihood of failure
	 * @param g - the goal
	 * @param probability - the probability of the goal to fail
	 * @return - the emotion created
	 */
	public static OCCBaseEmotion AppraiseGoalFailureProbability(AgentModel am , Event e, float goalConduciveness, float prob)
	{
		float potential;
		potential = prob * goalConduciveness;
		
		OCCBaseEmotion em = new  OCCBaseEmotion(OCCEmotionType.FEAR, potential, e);
		
		return em;
	}
	
	/**
	 * Appraises a Goal's success according to the emotions that the agent is experiencing
	 * @param hopeEmotion - the emotion of Hope for achieving the goal that the character feels
	 * @param fearEmotion - the emotion of Fear for not achieving the goal that the character feels
	 * @param g - the Goal that succeeded
	 * @return - the emotion created
	 */
	public static OCCBaseEmotion AppraiseGoalSuccess(AgentModel am, ActiveEmotion hopeEmotion, ActiveEmotion fearEmotion, float conduciveness, Event e) {
		return AppraiseGoalEnd(OCCEmotionType.SATISFACTION,OCCEmotionType.RELIEF,hopeEmotion,fearEmotion,conduciveness,e);
	}
	
	/**
	 * Appraises a Goal's likelihood of succeeding
	 * @param g - the goal
	 * @param probability - the probability of the goal to succeed
	 * @return - the BaseEmotion created
	 */
	public static OCCBaseEmotion AppraiseGoalSuccessProbability(AgentModel am, Event e, float goalConduciveness, float prob) {
	
		float potential;
		potential = prob * goalConduciveness;
	
		OCCBaseEmotion em = new  OCCBaseEmotion(OCCEmotionType.HOPE, potential, e);
	
		return em;
	}
	
	public OCCAffectDerivationComponent()
	{
	}
	
	@Override
	public ArrayList<BaseEmotion> affectDerivation(AgentModel am, AppraisalFrame af) {
		
		ArrayList<BaseEmotion> emotions = new ArrayList<BaseEmotion>();
		Event event = af.getEvent();
		ActiveEmotion fear;
		ActiveEmotion hope;
		float status;
		
		if(af.containsAppraisalVariable(OCCAppraisalVariables.DESIRABILITY.name()))
		{
			float desirability = af.getAppraisalVariable(OCCAppraisalVariables.DESIRABILITY.name());
			if(desirability!=0)
			{
				emotions.add(OCCAppraiseWellBeing(event, desirability));
				String other;
				float desirabilityForOther;
				
				for(String variable : af.getAppraisalVariables())
				{
					if(variable.startsWith(OCCAppraisalVariables.DESFOROTHER.name()))
					{
						other = variable.substring(OCCAppraisalVariables.DESFOROTHER.name().length());
						desirabilityForOther = af.getAppraisalVariable(variable);
						if(desirabilityForOther != 0)
						{
							emotions.add(OCCAppraiseFortuneOfOthers(event, desirability, desirabilityForOther, other));
						}
						
					}
				}
			}
		}
		if(af.containsAppraisalVariable(OCCAppraisalVariables.PRAISEWORTHINESS.name()))
		{
			float praiseworthiness = af.getAppraisalVariable(OCCAppraisalVariables.PRAISEWORTHINESS.name());
			if(praiseworthiness!=0)
			{
				emotions.add(OCCAppraisePraiseworthiness(event, praiseworthiness));
			}
		}
		if(af.containsAppraisalVariable(OCCAppraisalVariables.LIKE.name()))
		{
			float like = af.getAppraisalVariable(OCCAppraisalVariables.LIKE.name());
			if(like!=0)
			{
				emotions.add(OCCAppraiseAttribution(event, like));
			}
		}
		if(af.containsAppraisalVariable(OCCAppraisalVariables.GOALCONDUCIVENESS.name()))
		{
			float goalConduciveness = af.getAppraisalVariable(OCCAppraisalVariables.GOALCONDUCIVENESS.name());
			if(goalConduciveness!=0)
			{
				status = af.getAppraisalVariable(OCCAppraisalVariables.GOALSTATUS.name());
				if(status == GOALUNCONFIRMED)
				{
					float prob = af.getAppraisalVariable(OCCAppraisalVariables.SUCCESSPROBABILITY.name());
					if(prob != 0)
					{
						emotions.add(AppraiseGoalSuccessProbability(am, event, goalConduciveness, prob));
					}
					
					prob = af.getAppraisalVariable(OCCAppraisalVariables.FAILUREPROBABILITY.name());
					if(prob != 0)
					{
						emotions.add(AppraiseGoalFailureProbability(am, event, goalConduciveness, prob));
					}
				}
				else 
				{
		 
					fear = am.getEmotionalState().GetEmotion(new OCCBaseEmotion(OCCEmotionType.FEAR, 0, event));		 
					hope = am.getEmotionalState().GetEmotion(new OCCBaseEmotion(OCCEmotionType.HOPE,0, event));
					
					if(fear!= null || hope != null)
					{
						if(status == GOALCONFIRMED)
						{
							emotions.add(AppraiseGoalSuccess(am, hope, fear, goalConduciveness,af.getEvent()));
						}
						else if (status == GOALDISCONFIRMED)
						{
							emotions.add(AppraiseGoalFailure(am, hope, fear, goalConduciveness,af.getEvent()));
						}	
					}	
				}
			}
		}
		
		
		return emotions;
	}
	
	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return null;
	}

	
	@Override
	public IComponent createModelOfOther() {
		return new OCCAffectDerivationComponent();
	}
	
	@Override
	public String[] getComponentDependencies() {
		String[] dependencies = {};
		return dependencies;
	}
	
	@Override
	public void initialize(AgentModel am) {
	}
	
	

	@Override
	public void inverseAffectDerivation(AgentModel am, BaseEmotion em, AppraisalFrame af)
	{
		//ignoring mood for now
		
		EmotionDisposition emotionDisposition = am.getEmotionalState().getEmotionDisposition(em.getType());
		int threshold = emotionDisposition.getThreshold();
		float potentialValue = em.GetPotential() + threshold; 
		
		OCCEmotionType emotionType = OCCEmotionType.valueOf(em.getType());
		switch(emotionType){
			case LOVE: af.SetAppraisalVariable("", (short)1, OCCAppraisalVariables.LIKE.name(), potentialValue * 1.43f);
					   break;
			case HATE: af.SetAppraisalVariable("", (short)1, OCCAppraisalVariables.LIKE.name(), -potentialValue * 1.43f);
					   break;
			case JOY:  af.SetAppraisalVariable("", (short)1, OCCAppraisalVariables.DESIRABILITY.name(), potentialValue);
					   break;
			case DISTRESS:af.SetAppraisalVariable("", (short)1, OCCAppraisalVariables.DESIRABILITY.name(), -potentialValue);
					   break;
			case PRIDE: af.SetAppraisalVariable("", (short)1, OCCAppraisalVariables.PRAISEWORTHINESS.name(), potentialValue);
					   break;
			case SHAME: af.SetAppraisalVariable("", (short)1, OCCAppraisalVariables.PRAISEWORTHINESS.name(), -potentialValue);
					   break;
			case GLOATING: af.SetAppraisalVariable("", (short)1, OCCAppraisalVariables.DESIRABILITY.name(), potentialValue);
						   //vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, -potentialValue);
					   break;
			
			case HAPPY_FOR: af.SetAppraisalVariable("", (short)1, OCCAppraisalVariables.DESIRABILITY.name(), potentialValue);
							//vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, potentialValue);
					   break;
			case PITTY: af.SetAppraisalVariable("", (short)1, OCCAppraisalVariables.DESIRABILITY.name(), -potentialValue);
						//vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, -potentialValue);´
	   				   break;
			
			case RESENTMENT: //vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, potentialValue);
					   break;
			case GRATIFICATION: //af.SetAppraisalVariable("", (short)1, DESIRABILITY, potentialValue);
								//af.SetAppraisalVariable("", (short)1, PRAISEWORTHINESS, potentialValue);
					   break;
			case ANGER:// af.SetAppraisalVariable("", (short)1, DESIRABILITY, -potentialValue);
					   //af.SetAppraisalVariable("", (short)1, PRAISEWORTHINESS, -potentialValue);
					   break;
		}
	}

	
	
	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public void reset() {
	}

	@Override
	public void update(AgentModel am, Event e)
	{
	}
	
	@Override
	public void update(AgentModel am, long time) {
	}
}
