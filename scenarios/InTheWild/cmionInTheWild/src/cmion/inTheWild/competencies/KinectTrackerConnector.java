package cmion.inTheWild.competencies;

import java.util.ArrayList;
import java.util.HashMap;
import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;
import cmion.inTheWild.datastructures.TrackingInfoCollection;
import cmion.inTheWild.datastructures.TrackingInfo;
import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;

/** background competency that connects to the kinect samgar module and reads user and hand
 *  positions */
public class KinectTrackerConnector extends SamgarCompetency {
	
	private ArrayList<Integer> userIds;
	private int userCounter; 
	
	public KinectTrackerConnector(IArchitecture architecture) {
		super(architecture);
	
		//name and type of the competence
		this.competencyName ="Kinect";
		this.competencyType ="Kinect";
		userIds = new ArrayList<Integer>(); 
		userCounter = 0;
	}	
	
	@Override
	public void registerHandlers()
	{
		super.registerHandlers();
	}

	
	@Override
	public void onRead(Bottle bottleIn) 
	{
		/*
		if (bottleIn.size()>0)
		{
			if (bottleIn.get(0).asInt()==0) // user tracking information
			{
				// read 2nd value (number of users)
				int noUsers = bottleIn.get(1).asInt();
				// data structure for storing tracking data
				TrackingInfoCollection trackingInfo = new TrackingInfoCollection();
				
				ArrayList<Integer> newIds = new ArrayList<Integer>();
				
				// read the 4 values (id and xyz) for each user
				for (int i=0; i<noUsers; i++)
				{
					int id = bottleIn.get(2+i*4).asInt();
					double x = bottleIn.get(3+i*4).asDouble();
					double y = bottleIn.get(4+i*4).asDouble();
					double z = bottleIn.get(5+i*4).asDouble();
					TrackingInfo userInfo = new TrackingInfo(id,x,y,z);
					trackingInfo.addUserTrackingInfo(userInfo);
					newIds.add(id);
					if (!userIds.contains(id))
					{
						// this id was not present when we received our last bottle
						userCounter++;
						MindAction ma = new MindAction("Anonymous"+userCounter,"enterScene",null);
						this.raise(new EventRemoteAction(ma));
					}
				}	
				userIds = newIds;
				
				architecture.getBlackBoard().requestSetProperty("UserTracking", trackingInfo);				
			
			}
			else if (bottleIn.get(0).asInt()==1) // hand tracking information
			{
				// read 2nd value (number of hands)
				int noHands = bottleIn.get(1).asInt();
				// data structure for storing tracking data
				TrackingInfoCollection trackingInfo = new TrackingInfoCollection();
				
				// read the 5 values (hand id, user id and xyz) for each hand
				for (int i=0; i<noHands; i++)
				{
					int handid = bottleIn.get(2+i*5).asInt();
					int userid = bottleIn.get(3+i*5).asInt();
					double x = bottleIn.get(4+i*5).asDouble();
					double y = bottleIn.get(5+i*5).asDouble();
					double z = bottleIn.get(6+i*5).asDouble();
					TrackingInfo handInfo = new TrackingInfo(handid,userid,x,y,z);
					trackingInfo.addHandTrackingInfo(handInfo);
				}				
				architecture.getBlackBoard().requestSetProperty("HandTracking", trackingInfo);				
			}

		}
		*/
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
