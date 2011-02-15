package FAtiMA.empathy;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.ValuedAction;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.componentTypes.IBehaviourComponent;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.ReactiveComponent.ActionTendencies;
import FAtiMA.ReactiveComponent.ReactiveComponent;

public class EmpathyComponent  implements IAppraisalDerivationComponent, IBehaviourComponent{

	final String NAME = "EmpathyComponent";

	private static final long serialVersionUID = 1L;
	public static final long IGNOREDURATION = 30000;
	
	ActionTendencies _empatheticActions;
	
	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void initialize(AgentModel am) {
		_empatheticActions = new ActionTendencies();
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
	public void appraisal(AgentModel am, Event e, AppraisalFrame af) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public AppraisalFrame reappraisal(AgentModel am) {
		// TODO Auto-generated method stub
		return null;
	}


	public ActionTendencies getEmpatheticActions() {
		return _empatheticActions;
	}

	@Override
	public ValuedAction actionSelection(AgentModel am) {
		return _empatheticActions.SelectAction(am);
	}
	
	
	@Override
	public void reset() {	
		_empatheticActions = new ActionTendencies();
	}

	@Override
	public void update(AgentModel am, long time) {
		FacialExpressionSimulator.gI().updateFacialExpression(am);
	}

	@Override
	public void update(AgentModel am, Event e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		// TODO Auto-generated method stub
		return null;
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
		_empatheticActions.IgnoreActionForDuration(va,IGNOREDURATION);
		
	}

	@Override
	public String[] getComponentDependencies() {
		return new String[]{ReactiveComponent.NAME};
	}

	
}
