package FAtiMA.emotionalIntelligence;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.ReactiveComponent.Action;
import FAtiMA.ReactiveComponent.ReactiveComponent;
import FAtiMA.advancedMemoryComponent.AdvancedMemoryComponent;

public class EmotionalIntelligence implements IComponent {
	
	public static final String NAME = "EmotionalIntelligence";
	
	private ArrayList<String> _parsingFiles;
	
	public EmotionalIntelligence(ArrayList<String> extraFiles)
	{
		_parsingFiles = new ArrayList<String>();
		_parsingFiles.addAll(extraFiles);
	}

	private void LoadOperators(AgentModel am)
	{
		AgentLogger.GetInstance().log("LOADING EI Operators: ");
		EmotionalConditionsLoaderHandler emotionsLoader = new EmotionalConditionsLoaderHandler(am);
		
		try{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			
			for(String file : _parsingFiles)
			{
				parser.parse(new File(file), emotionsLoader);
			}	

		}catch(Exception e){
			throw new RuntimeException("Error on Loading EI Operators from XML Files:" + e);
		}
	}

	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return null;
	}

	@Override
	public String[] getComponentDependencies() {
		String[] dependencies = {ReactiveComponent.NAME,AdvancedMemoryComponent.NAME};
		return dependencies;
	}

	@Override
	public void initialize(AgentModel am) {
		
		ReactiveComponent reactiveComponent = (ReactiveComponent) am.getComponent(ReactiveComponent.NAME);

		ArrayList<Step> occRules = OCCAppraisalRules.GenerateOCCAppraisalRules(am);
		for(Step s : occRules)
		{
			am.getActionLibrary().addAction(s);
		}
		
		for(Action at: reactiveComponent.getActionTendencies().getActions())
		{
			am.getActionLibrary().addAction(ActionTendencyOperatorFactory.CreateATOperator(am, at));
		}
		
		LoadOperators(am);
	}

	@Override
	public String name() {
		return EmotionalIntelligence.NAME; 
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
		// TODO Auto-generated method stub
	}
}
