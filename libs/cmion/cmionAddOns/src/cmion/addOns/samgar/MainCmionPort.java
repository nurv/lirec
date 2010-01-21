package cmion.addOns.samgar;

import java.util.ArrayList;

import cmion.level2.SamgarModuleInfo;

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
		while (Network.getNameServerName().toString().equals("/global"))
		{}   // a loop to wait until the network is on the local and not global server
		open("/Main_"+SamgarConnector.MODULE_NAME); // open the port, this will be the main module port you'll get updates on
		while(getInputCount()<1){Thread.yield();} // you might want a line like this to know that the samgar gui 
								   // has actually connected to the maim module port
	}
	
	/** the call back for receiving bottles on this port */
	private BottleCallback myBottleCallback = new BottleCallback()
	{
		
		/** in here we read received bottles */
		@Override
		public void onRead(Bottle datum) 
		{
			// every bottle starts with an int to identify whats inside (samgar convention)
			int msgType = datum.get(0).asInt();
			// 105 signifies information about connected modules
			if (msgType == 105)
			{
				// modules are list by name , category and subcategory behind each other 
				// in the bottle
				ArrayList<SamgarModuleInfo> modules = new ArrayList<SamgarModuleInfo>();
				int counter = 1;
				while (counter < datum.size())
				{
					String name = datum.get(counter).asString().toString();
					counter++;
					String category = datum.get(counter).asString().toString();
					counter++;
					String subcategory = datum.get(counter).asString().toString();
					counter++;
					
					if (!name.equals(SamgarConnector.MODULE_NAME)) 
						modules.add(new SamgarModuleInfo(name,category,subcategory));
				}
				// now that we have read the whole bottle update the Samgar connector,
				// about what we have found
				parent.updateModules(modules);			
			}
		}	
	};

	/** with this method the port can send its identification to 
	 *  SAMGAR (its name, category and subCategory) */
	public void sendId() 
	{
		Bottle b = prepare();
		b.addInt(10);
		b.addString(SamgarConnector.MODULE_NAME);
		b.addString("Control");
		b.addString("Control");
		System.out.println(b);
		write();
	}
	
}
