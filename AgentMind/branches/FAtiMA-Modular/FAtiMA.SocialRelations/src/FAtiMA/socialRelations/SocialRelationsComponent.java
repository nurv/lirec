package FAtiMA.socialRelations;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IAppraisalDerivationComponent;
import FAtiMA.Core.IComponent;
import FAtiMA.Core.IModelOfOtherComponent;
import FAtiMA.Core.IProcessEmotionComponent;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.OCCAffectDerivation.OCCAppraisalVariables;
import FAtiMA.Core.OCCAffectDerivation.OCCComponent;
import FAtiMA.Core.OCCAffectDerivation.OCCEmotionType;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.Core.util.Constants;


public class SocialRelationsComponent implements Serializable, 

IAppraisalDerivationComponent, IModelOfOtherComponent, 

IProcessEmotionComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "SocialRelations";
	private ArrayList<String> _parsingFiles;

	public SocialRelationsComponent(ArrayList<String> 

	extraParsingFiles)
	{
		_parsingFiles = new ArrayList<String>();
		_parsingFiles.add(ConfigurationManager.getGoalsFile());
		_parsingFiles.add

		(ConfigurationManager.getPersonalityFile());
		_parsingFiles.add(ConfigurationManager.getActionsFile

				());
		_parsingFiles.addAll(extraParsingFiles);
	}

	private void loadRelations(AgentModel aM){

		AgentLogger.GetInstance().log("LOADING Social Relations:");
				RelationsLoaderHandler relationsLoader = new 

				RelationsLoaderHandler(aM);

				try{
					SAXParserFactory factory = 

						SAXParserFactory.newInstance();
					SAXParser parser = factory.newSAXParser();

					for(String file : _parsingFiles)
					{
						parser.parse(new File(file), 

								relationsLoader);
					}			

				}catch(Exception e){
					throw new RuntimeException("Error on Loading the Social Relations XML Files:" + e);
				}
	}

	@Override
	public String name() {
		return SocialRelationsComponent.NAME;
	}

	@Override
	public void initialize(AgentModel am) {
		this.loadRelations(am);
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
	public void appraisal(AgentModel am, Event e, AppraisalFrame as) 

	{
		if(e.GetSubject().equals(Constants.SELF) && e.GetAction

				().equals("look-at"))
		{
			int relationShip = Math.round

			(LikeRelation.getRelation(Constants.SELF, e.GetTarget()).getValue

					(am.getMemory()));
			if(relationShip != 0)
			{
				as.SetAppraisalVariable(NAME, (short)7, 

						OCCAppraisalVariables.LIKE.name(), relationShip);
			}	
		}
	}

	@Override
	public void inverseAppraisal(AgentModel am, AppraisalFrame af)
	{
		float like;
		Event e;
		like = af.getAppraisalVariable

		(OCCAppraisalVariables.LIKE.name());

		if(like != 0)
		{
			e = af.getEvent();
			LikeRelation.getRelation(Constants.SELF, 

					e.GetTarget()).setValue(am.getMemory(), like);
		}
	}

	//updating other's emotional reactions


	@Override
	public void emotionActivation(AgentModel am, ActiveEmotion em) {
		Memory m = am.getMemory();
		if(em.getType().equalsIgnoreCase

				(OCCEmotionType.ADMIRATION.name()))
		{
			if(em.GetDirection() != null)
			{
				LikeRelation.getRelation

				(Constants.SELF,em.GetDirection().toString()).increment

				(m,em.GetIntensity());
				RespectRelation.getRelation

				(Constants.SELF,em.GetDirection().toString()).increment(m, 

						em.GetIntensity());		
			}
		}
		else if(em.getType().equalsIgnoreCase

				(OCCEmotionType.REPROACH.name()))
		{
			if(em.GetDirection() != null)
			{
				LikeRelation.getRelation

				(Constants.SELF,em.GetDirection().toString()).decrement(m, 

						em.GetIntensity());
				RespectRelation.getRelation

				(Constants.SELF,em.GetDirection().toString()).decrement(m, 

						em.GetIntensity());
			}
		}
		else if(em.getType().equalsIgnoreCase

				(OCCEmotionType.HAPPY_FOR.name()))
		{
			if(em.GetDirection() != null)
			{
				LikeRelation.getRelation

				(Constants.SELF,em.GetDirection().toString()).increment(m, 

						em.GetIntensity());
			}
		}
		else if(em.getType().equalsIgnoreCase

				(OCCEmotionType.GLOATING.name()))
		{
			if(em.GetDirection() != null)
			{
				LikeRelation.getRelation

				(Constants.SELF,em.GetDirection().toString()).decrement(m, 

						em.GetIntensity());
			}
		}
		else if(em.getType().equalsIgnoreCase

				(OCCEmotionType.PITTY.name()))
		{
			if(em.GetDirection() != null)
			{
				LikeRelation.getRelation

				(Constants.SELF,em.GetDirection().toString()).increment(m, 

						em.GetIntensity());
			}
		}
		else if(em.getType().equalsIgnoreCase

				(OCCEmotionType.RESENTMENT.name()))
		{
			if(em.GetDirection() != null)
			{
				LikeRelation.getRelation

				(Constants.SELF,em.GetDirection().toString()).decrement(m, 

						em.GetIntensity());
			}
		}
		else if(em.getType().equalsIgnoreCase

				(OCCEmotionType.JOY.name()))
		{
			if(em.GetCause().GetTarget() != null && 

					em.GetCause().GetTarget().equals(Constants.SELF))
			{
				LikeRelation.getRelation

				(Constants.SELF,em.GetCause().GetSubject()).increment(m, 

						em.GetIntensity());
			}
		}
		else if(em.getType().equalsIgnoreCase

				(OCCEmotionType.DISTRESS.name()))
		{
			if(em.GetCause().GetTarget() != null && 

					em.GetCause().GetTarget().equals(Constants.SELF))
			{
				LikeRelation.getRelation

				(Constants.SELF,em.GetCause().GetSubject()).decrement(m, 

						em.GetIntensity());
			}
		}
	}


	@Override
	public IComponent createModelOfOther() {
		return new SocialRelationsComponent(new 

				ArrayList<String>());
	}

	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return new SocialRelationsPanel();
	}

	@Override
	public AppraisalFrame reappraisal(AgentModel am) {
		return null;
	}
}
