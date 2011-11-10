package FAtiMA.emotionalIntelligence;

import java.util.ArrayList;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.parsers.ReflectXMLHandler2;
import FAtiMA.DeliberativeComponent.DeliberativeComponent;
import FAtiMA.OCCAffectDerivation.OCCAffectDerivationComponent;
import FAtiMA.ReactiveComponent.Action;
import FAtiMA.ReactiveComponent.ReactiveComponent;
import FAtiMA.ToM.ToMComponent;

public class EmotionalIntelligenceComponent implements IComponent {
	
	public static final String NAME = "EmotionalIntelligence";
	
	//private ArrayList<String> _parsingFiles;
	private EmotionalConditionsLoaderHandler _parser;
	
	public EmotionalIntelligenceComponent(ArrayList<String> extraFiles)
	{
		/*_parsingFiles = new ArrayList<String>();
		_parsingFiles.add(ConfigurationManager.getGoalsFile());
		_parsingFiles.add(ConfigurationManager.getActionsFile());
		_parsingFiles.addAll(extraFiles);*/
	}
	
	

	/*private void LoadOperators(AgentModel am)
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
	}*/

	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return null;
	}

	@Override
	public String[] getComponentDependencies() {
		String[] dependencies = {
				ReactiveComponent.NAME, 
				DeliberativeComponent.NAME,
				OCCAffectDerivationComponent.NAME,
				//MotivationalComponent.NAME,
				ToMComponent.NAME//,
				//SocialRelationsComponent.NAME};
		};
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
		
		this._parser = new EmotionalConditionsLoaderHandler(am);
		//LoadOperators(am);
	}

	@Override
	public String name() {
		return EmotionalIntelligenceComponent.NAME; 
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



	@Override
	public ReflectXMLHandler2 getActionsParser(AgentModel am) {
		return this._parser;
	}

	@Override
	public ReflectXMLHandler2 getGoalsParser(AgentModel am) {
		return this._parser;
	}



	@Override
	public ReflectXMLHandler2 getPersonalityParser(AgentModel am) {
		return null;
	}



	@Override
	public void parseAdditionalFiles(AgentModel am) {
	}
}
