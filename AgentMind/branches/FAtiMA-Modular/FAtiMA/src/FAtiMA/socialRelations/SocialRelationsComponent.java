package FAtiMA.socialRelations;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IComponent;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.AppraisalStructure;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.reactiveLayer.Reaction;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.enumerables.EmotionType;
import FAtiMA.Core.wellFormedNames.Name;

public class SocialRelationsComponent implements IComponent {
	
	public static final String NAME = "SocialRelations";
	
	public SocialRelationsComponent()
	{	
	}

	@Override
	public String name() {
		return SocialRelationsComponent.NAME;
	}

	@Override
	public void initialize(AgentModel am) {			
	}

	@Override
	public void reset() {
	}

	@Override
	public void decay(long time) {	
	}

	@Override
	public void update(AgentModel am) {
	}
	
	@Override
	public void update(Event e, AgentModel am)
	{
	}

	@Override
	public void appraisal(Event e, AppraisalStructure as, AgentModel am) {
		if(e.GetSubject().equals(Constants.SELF) && e.GetAction().equals("look-at"))
		{
			int relationShip = Math.round(LikeRelation.getRelation(Constants.SELF, e.GetTarget()).getValue(am.getMemory()));
			as.SetAppraisalVariable(NAME, (short)7, AppraisalStructure.LIKE, relationShip);
		}
	}

	@Override
	public void emotionActivation(Event e, ActiveEmotion em, AgentModel am) {
		Memory m = am.getMemory();
		switch(em.GetType())
		{
			case EmotionType.ADMIRATION:
			{
				if(em.GetDirection() != null)
				{
					LikeRelation.getRelation(Constants.SELF,em.GetDirection().toString()).increment(m,em.GetIntensity());
					RespectRelation.getRelation(Constants.SELF,em.GetDirection().toString()).increment(m, em.GetIntensity());
					break;		
				}
			}
			case EmotionType.REPROACH:
			{
				if(em.GetDirection() != null)
				{
					LikeRelation.getRelation(Constants.SELF,em.GetDirection().toString()).decrement(m, em.GetIntensity());
					RespectRelation.getRelation(Constants.SELF,em.GetDirection().toString()).decrement(m, em.GetIntensity());
					break;
				}
			}
			case EmotionType.HAPPYFOR:
			{
				if(em.GetDirection() != null)
				{
					LikeRelation.getRelation(Constants.SELF,em.GetDirection().toString()).increment(m, em.GetIntensity());
					break;
				}
			}
			case EmotionType.GLOATING:
			{
				if(em.GetDirection() != null)
				{
					LikeRelation.getRelation(Constants.SELF,em.GetDirection().toString()).decrement(m, em.GetIntensity());
					break;
				}
			}
			case EmotionType.PITTY:
			{
				if(em.GetDirection() != null)
				{
					LikeRelation.getRelation(Constants.SELF,em.GetDirection().toString()).increment(m, em.GetIntensity());
					break;
				}
			}
			case EmotionType.RESENTMENT:
			{
				if(em.GetDirection() != null)
				{
					LikeRelation.getRelation(Constants.SELF,em.GetDirection().toString()).decrement(m, em.GetIntensity());
					break;
				}
			}			
			case EmotionType.JOY:
			{
				if(e.GetTarget() != null && e.GetTarget().equals(Constants.SELF))
				{
					LikeRelation.getRelation(Constants.SELF,e.GetSubject()).increment(m, em.GetIntensity());
				}
				break;
			}
			case EmotionType.DISTRESS:
			{
				if(e.GetTarget() != null && e.GetTarget().equals(Constants.SELF))
				{
					LikeRelation.getRelation(Constants.SELF,e.GetSubject()).decrement(m, em.GetIntensity());
				}
				break;
			}
		}
	}

	@Override
	public void coping(AgentModel am) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propertyChangedPerception(String ToM, Name propertyName,
			String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void lookAtPerception(AgentCore ag, String subject, String target) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entityRemovedPerception(String entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IComponent createModelOfOther() {
		return new SocialRelationsComponent();
	}

	@Override
	public AgentDisplayPanel createComponentDisplayPanel(AgentModel am) {
		return new SocialRelationsPanel();
	}

}
