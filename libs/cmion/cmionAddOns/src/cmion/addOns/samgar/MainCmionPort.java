package cmion.addOns.samgar;

import java.util.ArrayList;

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
		open("/Main_"+parent.MODULE_NAME); // open the port, this will be the main module port you'll get updates on
		while(getInputCount()<1){Thread.yield();} // you might want a line like this to know that the samgar gui 
								   // has actually connected to the maim module port
	}
	
	/** the call back for receiving bottles on this port */
	private BottleCallback myBottleCallback = new BottleCallback()
	{
		
		@Override
		public void onRead(Bottle datum) 
		{
			int msgType = datum.get(0).asInt();
			// 105 signifies information about connected modules
			if (msgType == 105)
			{
				ArrayList<ModuleInfo> modules = new ArrayList<ModuleInfo>();
				int counter = 1;
				while (counter < datum.size())
				{
					String name = datum.get(counter).asString().toString();
					counter++;
					String category = datum.get(counter).asString().toString();
					counter++;
					String subcategory = datum.get(counter).asString().toString();
					counter++;
					
					if (!name.equals(parent.MODULE_NAME)) 
						modules.add(new ModuleInfo(name,category,subcategory));
				}
				
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
		b.addString(parent.MODULE_NAME);
		b.addString("Control");
		b.addString("Control");
		System.out.println(b);
		write();
	}
	
}
