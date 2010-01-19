package cmion.addOns.samgar;

import java.util.ArrayList;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;

import yarp.Network;

import cmion.architecture.CmionComponent;
import cmion.architecture.EventCmionReady;
import cmion.architecture.IArchitecture;

/** this class provides a connector to SAMGAR. It handles the dynamic
 *  construction of SAMGAR competencies */
public class SamgarConnector extends CmionComponent implements Runnable {

	/** the name of the cmion module within Samgar */
	public final String MODULE_NAME = "CMion"; 
	
	/** the main yarp port needed in order to represent Cmion as a Samgar module */
	private MainCmionPort mainPort;
	
	/** storing the list of avilable Samgar modules*/
	ArrayList<ModuleInfo> availableModules;
	
	
	public SamgarConnector(IArchitecture architecture) 
	{
		super(architecture);
		availableModules = new ArrayList<ModuleInfo>();
	}

	@Override
	public void registerHandlers() 
	{
		// register a handler to get a signal when the architecture is ready
		Simulation.instance.getEventHandlers().add(new HandleCmionReady());
	}

	
	/** internal event handler class for listening to competency succeeded events */
	private class HandleCmionReady extends EventHandler 
	{

	    public HandleCmionReady() 
	    {
	        super(EventCmionReady.class);
	    }

	    @Override
	    public void invoke(IEvent evt) 
	    {     
	    	// start a thread for building up the Samgar connections
	    	new Thread(SamgarConnector.this).start();
	    }
	}

	/** main function of the Samgar connector (thread) */
	@Override
	public void run() 
	{
		// load jyarp.dll (and indirectly also ACE.dll)
		System.loadLibrary("jyarp");
		// initialize yarp network
		Network.init();
		// create new main port
		mainPort = new MainCmionPort(this);
		// wait a bit
		yarp.Time.delay(2);
		// now the main port should identify itself
		mainPort.sendId();
		
		
	}

	/** update the list of available Samgar modules*/
	public void updateModules(ArrayList<ModuleInfo> modules) 
	{
		
		//for (ModuleInfo modInfo:modules)
		//{
		//
		//
		//			
		//}
		//
	}
}
