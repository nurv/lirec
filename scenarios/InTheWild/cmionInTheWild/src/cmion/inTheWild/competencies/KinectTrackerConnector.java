package cmion.inTheWild.competencies;

import java.util.HashMap;
import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;
import cmion.inTheWild.datastructures.TrackingInfo;
import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;

/** background competency that connects to the kinect samgar module and reads user and hand
 *  positions */
public class KinectTrackerConnector extends SamgarCompetency {
		
	private long lastTimeClose = 0;
	
	public KinectTrackerConnector(IArchitecture architecture) {
		super(architecture);
	
		//name and type of the competence
		this.competencyName ="Kinect";
		this.competencyType ="Kinect";
	}	
	
	@Override
	public void registerHandlers()
	{
		super.registerHandlers();
	}

	
	@Override
	public synchronized void onRead(Bottle bottleIn) 
	{
		if (bottleIn.size()>0)
		{
			if (bottleIn.get(0).asInt()==0) // user tracking information
			{
				// read the xyz for user
				double x = bottleIn.get(1).asDouble();
				double y = bottleIn.get(2).asDouble();
				double z = bottleIn.get(3).asDouble();
				TrackingInfo userInfo = new TrackingInfo(x,y,z);
				if (z<1200)
				{
					if (timeElapsed()>7000)
					{
						lastTimeClose = System.currentTimeMillis();
						MindAction ma = new MindAction("Anonymous","comeClose",null);
						this.raise(new EventRemoteAction(ma));
					}
				}					
				architecture.getBlackBoard().requestSetProperty("UserTracking", userInfo);					
			}
			else if (bottleIn.get(0).asInt()==1) // user has disappeared
			{
			}

		}
	
	}
	
	private synchronized long timeElapsed()
	{
		return System.currentTimeMillis() - lastTimeClose;
	}
	

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		// we just sleep in the main thread, we react on samgar callbacks instead
		while(true)
		{
			try {
				Thread.sleep(100000);
			} catch (InterruptedException e) {}
		}
	}

	@Override
	public boolean runsInBackground() {
		return true;
	}

}
