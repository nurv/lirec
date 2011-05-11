package cmion.fatima;

import java.util.ArrayList;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.DeliberativeComponent.DeliberativeComponent;
import FAtiMA.OCCAffectDerivation.OCCAffectDerivationComponent;
import FAtiMA.ReactiveComponent.ReactiveComponent;
import cmion.architecture.CmionComponent;
import cmion.architecture.IArchitecture;

public class FatimaLauncher extends CmionComponent
{
	private	String scenarioFile;
	private	String scenarioName;
	private	String agentName;	
	
	public FatimaLauncher(IArchitecture architecture, String scenarioFile, String scenarioName, String agentName) 
	{
		super(architecture);
		this.scenarioFile = scenarioFile;
		this.scenarioName = scenarioName;
		this.agentName = agentName;
		new LaunchFatimaThread().start();
	}

	@Override
	public void registerHandlers() 
	{
	}
	
	private class LaunchFatimaThread extends Thread
	{
		
		@Override
		public void run()
		{
			// wait a while before starting FAtiMA
			try {
				Thread.sleep(15000);
		
			FAtiMA.Core.AgentCore agent = new AgentCore(agentName);
			agent.initialize(scenarioFile,scenarioName,agentName);
			ArrayList<String> extraFiles = new ArrayList<String>();
			String cultureFile = ConfigurationManager.getMindPath() + ConfigurationManager.getOptionalConfigurationValue("cultureName") + ".xml";

			if (!agent.getAgentLoad())
			{
				extraFiles.add(cultureFile);
				
				
				//FAtiMA Light
				agent.addComponent(new ReactiveComponent());
				agent.addComponent(new OCCAffectDerivationComponent());
				agent.addComponent(new DeliberativeComponent());
				
				//FAtiMA Advanced Components
				//aG.addComponent(new MotivationalComponent(extraFiles));
				//aG.addComponent(new SocialRelationsComponent(extraFiles));
				//aG.addComponent(new ToMComponent(ConfigurationManager.getName()));
				//aG.addComponent(new CulturalDimensionsComponent(cultureFile));
				//aG.addComponent(new AdvancedMemoryComponent());
				//aG.addComponent(new EmotionalIntelligenceComponent(EIFile, extraFiles));
				
			}
			agent.StartAgent();
			} catch (Exception e) 
			{
				System.err.println("Error Launching Fatima");
				e.printStackTrace();
			}

			
		}
	
	}	

}
