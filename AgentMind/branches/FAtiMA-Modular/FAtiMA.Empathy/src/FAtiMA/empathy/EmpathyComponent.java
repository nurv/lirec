package FAtiMA.empathy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.ValuedAction;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAffectDerivationComponent;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.componentTypes.IBehaviourComponent;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.Core.util.Constants;
import FAtiMA.ReactiveComponent.ActionTendencies;
import FAtiMA.ReactiveComponent.ReactiveComponent;
import FAtiMA.socialRelations.LikeRelation;
import FAtiMA.socialRelations.SocialRelationsComponent;

public class EmpathyComponent  implements IAppraisalDerivationComponent, IAffectDerivationComponent, IBehaviourComponent{

	final String NAME = "EmpathyComponent";

	private static final long serialVersionUID = 1L;
	public static final long IGNOREDURATION = 30000;
	public static final long REACTION_DELAY_MS = 600;
	
	private enum AppraisalVariables{
		SIMILARITY,
		AFFECTIVE_LINK
	}

	private long _currentTime;
	private EmpathicActions _empathicActions;
	private ArrayList<EmpathicAppraisal> _empathicAppraisals;
	private HashMap<String,BaseEmotion> _elicitedEmotions;

	
	@Override
	public String name(){
		return NAME;
	}

	@Override
	public void initialize(AgentModel am) {
		_empathicActions = new EmpathicActions();
		_empathicAppraisals = new ArrayList<EmpathicAppraisal>();
		_elicitedEmotions = new HashMap<String,BaseEmotion>();
		
		AgentLogger.GetInstance().log("Adding Reactive Empathic Actions in the EmpathyComponent:");
		EmpathyLoaderHandler empathyLoader = new EmpathyLoaderHandler(this);
		
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(new File(ConfigurationManager.getPersonalityFile()), empathyLoader);
		} catch (Exception e) {
			throw new RuntimeException(
					"Error reading the agent XML Files:" + e);
		}
		
	}
	

	
	
	@Override
	public void update(AgentModel am, long time) {
		FacialExpressionSimulator.gI().updateFacialExpression(am);
		this._currentTime = time;
	}
	
	@Override
	public void update(AgentModel am, Event e) {
	}

	
	
	@Override
	public void appraisal(AgentModel am, Event e, AppraisalFrame af) {
		ArrayList<String> empathicTargets = determineEmpathicTargets(e,am);
		BaseEmotion elicitedEmotion;
		float affectiveLink; 
		
		for(String empathicTarget : empathicTargets){
		
			elicitedEmotion = this.selfProjectionAppraisal(empathicTarget,e,am);	
			
			this._elicitedEmotions.put(e + empathicTarget, elicitedEmotion);
			
			affectiveLink = determineAffectiveLink(empathicTarget, am.getMemory());
			
			af.SetAppraisalVariable(NAME, AppraisalVariables.AFFECTIVE_LINK + empathicTarget, affectiveLink);
			
			
			_empathicAppraisals.add(new EmpathicAppraisal(empathicTarget,af,elicitedEmotion,_currentTime));
		}	
	}
	
	@Override
	public AppraisalFrame reappraisal(AgentModel am) {
		FacialExpressionType targetFacialExpression;
		float similarity;
		EmpathicAppraisal eAppraisal;
		
		if(_empathicAppraisals.size()>0){
			eAppraisal = _empathicAppraisals.get(0);
			if((_currentTime - eAppraisal.getStartTime()) > REACTION_DELAY_MS){
				_empathicAppraisals.remove(0);
			
				targetFacialExpression = FacialExpressionSimulator.gI().determineTargetFacialExpression(eAppraisal.getEmpathicTarget(), am);
				similarity = determineSimilarity(eAppraisal.getElicitedEmotion(),targetFacialExpression);
				eAppraisal.getAppraisalFrame().SetAppraisalVariable(NAME, AppraisalVariables.SIMILARITY + eAppraisal.getEmpathicTarget(), similarity);
			
				return eAppraisal.getAppraisalFrame();
			}
		}
		return null;
	}
	
	private float determineSimilarity(BaseEmotion elicitedEmotion,
			FacialExpressionType targetFacialExpression) {

		FacialExpressionType projectedFacialExpression = FacialExpressionSimulator.gI().CalculateFacialExpression(elicitedEmotion);
		
		if(projectedFacialExpression == targetFacialExpression){
			return 1;
		}else{
			return 0;
		}
	}

	@Override
	public ArrayList<BaseEmotion> affectDerivation(AgentModel am,
			AppraisalFrame af) {
		
		EmpathicEmotion empathicEmotion;
		FacialExpressionType targetFacialExpression;
		ArrayList<BaseEmotion> potentialEmpathicEmotions = new ArrayList<BaseEmotion>();
		ArrayList<String> empathicTargets = determineEmpathicTargets(af.getEvent(),am);
		
		for(String empathicTarget : empathicTargets){
			if(af.containsAppraisalVariable(AppraisalVariables.AFFECTIVE_LINK + empathicTarget)){
				if(af.containsAppraisalVariable(AppraisalVariables.SIMILARITY + empathicTarget)){
					
					float similarity = af.getAppraisalVariable(AppraisalVariables.SIMILARITY + empathicTarget);
					float affectiveLink = af.getAppraisalVariable(AppraisalVariables.AFFECTIVE_LINK + empathicTarget);
					
					if(similarity > 0){
						BaseEmotion elicitedEmotion = _elicitedEmotions.get(af.getEvent() + empathicTarget);
						if(elicitedEmotion != null){
							empathicEmotion = new EmpathicEmotion(elicitedEmotion,af.getEvent());
							empathicEmotion.increasePotential(empathicEmotion.GetPotential() * ((affectiveLink/10)*2));
							potentialEmpathicEmotions.add(empathicEmotion);	
						}
					}else if(affectiveLink > 0){
						targetFacialExpression = FacialExpressionSimulator.gI().determineTargetFacialExpression(empathicTarget, am);				
						BaseEmotion recognizedEmotion = FacialExpressionSimulator.gI().recognizeEmotion(targetFacialExpression,af.getEvent());
						if(recognizedEmotion != null){
							empathicEmotion = new EmpathicEmotion(recognizedEmotion,af.getEvent());
							empathicEmotion.setPotential(empathicEmotion.GetPotential() * (affectiveLink/10));
							potentialEmpathicEmotions.add(empathicEmotion);	
						}
					}
				}
			}
		}
		
		return potentialEmpathicEmotions;
	}
	
	
	
	private ArrayList<String> determineEmpathicTargets(Event e, AgentModel am){
		
		String[] possibleEmpathicTargets = {e.GetSubject(),e.GetTarget()};
		
		ArrayList<String> empathicTargets = new ArrayList<String>();
		
		for(String possibleEmpathicTarget : possibleEmpathicTargets){
			
			if(possibleEmpathicTarget != null && !possibleEmpathicTarget.equalsIgnoreCase(Constants.SELF)){			
			
				if(FacialExpressionSimulator.gI().determineTargetFacialExpression(possibleEmpathicTarget, am) != null){
					empathicTargets.add(possibleEmpathicTarget);
				}else{
					//If it has no facial expression then the agent assumes it's an object
				}	
			}
		}
	
		return empathicTargets;
	}
	
	
	private float determineAffectiveLink(String empathicTarget, Memory agentMemory){
		LikeRelation affectiveLink = new LikeRelation (Constants.SELF, empathicTarget);
		return affectiveLink.getValue(agentMemory);
	}
	
	private BaseEmotion selfProjectionAppraisal(String empathicTarget, Event e, AgentModel am){
		Event selfProjectedEvent;
		
		if(e.GetTarget().equalsIgnoreCase(Constants.SELF) || e.GetSubject().equalsIgnoreCase(Constants.SELF)){   
			//special situation where the self is already involved in the event
			//in order to prevent appraising twice the same event we switch SELF with empathicTarget
			selfProjectedEvent = new Event(e.GetTarget(),e.GetAction(),e.GetSubject());		
		}else{
		   selfProjectedEvent = e.ApplyPerspective(empathicTarget);			
		}
		
		EmotionalState eS = am.simulateEmotionalState(selfProjectedEvent, this);
		if(eS!=null){
			return eS.GetStrongestEmotion();
		}else{
			return null;
		}
	}	


	public ActionTendencies getEmpathicActions() {
		return _empathicActions;
	}

	@Override
	public ValuedAction actionSelection(AgentModel am) {
		return _empathicActions.SelectAction(am);
	}
	
	
	@Override
	public void reset() {	
		_empathicActions = new EmpathicActions();
	}



	
	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return new EmpathicEmotionsPanel();		
	}

	

	@Override
	public void inverseAppraisal(AgentModel am, AppraisalFrame af) {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void actionSelectedForExecution(ValuedAction va) {
		/*
		 * Temporarily removes the action selected for execution. This means 
		 * that when a action is executed it should not be selected again for a while,
		 * or else we will have a character reacting in the same way several times
		 */
		_empathicActions.IgnoreActionForDuration(va,IGNOREDURATION);
		
	}

	@Override
	public String[] getComponentDependencies() {
		return new String[]{ReactiveComponent.NAME,SocialRelationsComponent.NAME};
	}

	

	@Override
	public void inverseAffectDerivation(AgentModel am, BaseEmotion em,
			AppraisalFrame af) {
		// TODO Auto-generated method stub
		
	}

	
}
