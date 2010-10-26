package cmion.addOns.samgar;

import ion.Meta.EventHandler;

import ion.Meta.IEvent;
import ion.Meta.Simulation;

import yarp.Network;
import yarp.Time;

import cmion.architecture.CmionComponent;
import cmion.architecture.EventCmionReady;
import cmion.architecture.IArchitecture;
import cmion.level2.EventSamgarModuleReady;


/** this class provides a connector to SAMGAR. It handles the dynamic
 *  construction of SAMGAR competencies */
public class SamgarConnector extends CmionComponent implements Runnable {

	/** the name of the cmion module within Samgar */
	public static final String MODULE_NAME = "/CMion"; 
	
	/** the main yarp port needed in order to represent Cmion as a Samgar module */
	private MainCmionPort mainPort;

	
	public SamgarConnector(IArchitecture architecture) 
	{
		super(architecture);
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
		// raise an event that the cmion Samgar module is ready
		this.raise(new EventSamgarModuleReady());
		
		String os = System.getProperty("os.name").toLowerCase();
		boolean linuxSystem = os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0;
		while(linuxSystem) {
		    Time.delay(1);
		}
	}

}

	