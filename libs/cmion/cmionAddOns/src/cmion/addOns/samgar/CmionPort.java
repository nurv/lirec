package cmion.addOns.samgar;

import yarp.Bottle;
import yarp.BottleCallback;
import yarp.BufferedPortBottle;

/** a buffered yarp port for either sending to or receiving bottles from a Samgar module. 
 *  Id used by every SamgarCompetency (each Samgar Competency opens 2 CmionPorts for read
 *  and write) */
public class CmionPort extends BufferedPortBottle {

	/** defines what types of ports are available (output or input) */
	public enum PortType {Out,In};

	/** defines the type of this port (output or input) */	
	private PortType portType;
	
	/** the samgar competency this port is connected to */
	private SamgarCompetency parent;
	
	/** create a new main Cmion port */
	public CmionPort(PortType port_type,SamgarCompetency parent)
	{
		this.portType = port_type;
		this.parent = parent;
		if (portType==PortType.Out)
			open("/Port_"+SamgarConnector.MODULE_NAME+"_out"+parent.getCompetencyName()); // open the port, 
		else if (portType==PortType.In)
		{
			open("/Port_"+SamgarConnector.MODULE_NAME+"_in"+parent.getCompetencyName()); // open the port, 			
			useCallback(myBottleCallback); // set the callback for reading data on this port
		}
	}
	
	/** the call back for receiving bottles on this port */
	private BottleCallback myBottleCallback = new BottleCallback()
	{
		
		/** in here we read received bottles */
		@Override
		public void onRead(Bottle datum) 
		{
			// forward the bottle to the competency that owns us
			parent.onRead(datum);
		}	
	};
	
	
	
	
}
