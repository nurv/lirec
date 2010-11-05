package FAtiMA.socialRelations;

import FAtiMA.AgentCore;
import FAtiMA.AgentModel;
import FAtiMA.IComponent;
import FAtiMA.Display.AgentDisplayPanel;
import FAtiMA.emotionalState.ActiveEmotion;
import FAtiMA.emotionalState.AppraisalStructure;
import FAtiMA.memory.Memory;
import FAtiMA.reactiveLayer.Reaction;
import FAtiMA.sensorEffector.Event;
import FAtiMA.util.Constants;
import FAtiMA.util.enumerables.EmotionType;
import FAtiMA.wellFormedNames.Name;

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
	public void initialize(AgentCore am) {	
		
		SocialRelationsPanel panel = new SocialRelationsPanel();
		am.getAgentDisplay().AddPanel(panel, "Social Relations", "displays the character's relations' state");		
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
	public AppraisalStructure appraisal(Event e, AgentModel am) {
		if(e.GetSubject().equals(Constants.SELF) && e.GetAction().equals("look-at"))
		{
			int relationShip = Math.round(LikeRelation.getRelation(Constants.SELF, e.GetTarget()).getValue(am.getMemory()));
			AppraisalStructure v = new AppraisalStructure();
			v.setAppraisalVariable(AppraisalStructure.LIKE, relationShip);
			return v;
		}
		else return null;
	}

	@Override
	public AppraisalStructure composedAppraisal(Event e, AppraisalStructure v,
			AgentModel am) {
		return null;
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
