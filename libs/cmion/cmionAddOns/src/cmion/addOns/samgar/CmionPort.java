package cmion.addOns.samgar;

import yarp.Bottle;
import yarp.BottleCallback;
import yarp.BufferedPortBottle;

/** a buffered yarp port for either sending to or receiving bottles from a Samgar module. 
 *  Id used by every SamgarCompetency (each Samgar Competency opens 2 CmionPorts for read
 *  and write) */
public class CmionPort extends BufferedPortBottle {
	
	/** the samgar competency this port is connected to */
	private SamgarCompetency parent;
	
	/** create a new main Cmion port */
	public CmionPort(SamgarCompetency parent)
	{
		this.parent = parent;
		open("/Port_"+SamgarConnector.MODULE_NAME+"_"+parent.getCompetencyName()); // open the port
		useCallback(myBottleCallback); // set the callback for reading data on this port
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
