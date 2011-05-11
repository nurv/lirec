package cmion.addOns.samgar;

import java.util.ArrayList;

import cmion.level2.SamgarCompetencyInfo;

import yarp.Bottle;
import yarp.BottleCallback;
import yarp.BufferedPortBottle;
import yarp.Network;

/** the yarp port for communicating with the Samgar module control */
public class MainCmionPort extends BufferedPortBottle
{

	/** the Samgar Connector that has spawned this port */
	private SamgarConnector parent;
	
	/** create a new main Cmion port */
	public MainCmionPort(SamgarConnector parent)
	{
		this.parent = parent;
		useCallback(myBottleCallback); // set the callback for reading data on this port
		//while (Network.getNameServerName().toString().equals("/global"))
		//{}   // a loop to wait until the network is on the local and not global server
		open(SamgarConnector.MODULE_NAME);
		this.addOutput("/CONTROL");
		while(this.isWriting()){Thread.yield();} // you might want a line like this to know that the samgar gui 
		// has actually connected to the maim module port	
	}
	
	
	/** the call back for receiving bottles on this port */
	private BottleCallback myBottleCallback = new BottleCallback()
	{
		
		/** in here we read received bottles */
		@Override
		public void onRead(Bottle datum) 
		{
		}	
	};

	/** with this method the port can send its identification to 
	 *  SAMGAR (its name, category and subCategory) */
	public void sendId() 
	{
		Bottle b = prepare();
		b.addString("Add_Module");
		b.addString(SamgarConnector.MODULE_NAME);
		ArrayList<SamgarCompetencyInfo> scis = parent.getArchitecture().getCompetencyLibrary().getSamgarCompetencyInfos();
		for (SamgarCompetencyInfo sci: scis)
		{
			b.addString(sci.getPortName());
		}
		write();
	}
	
}
