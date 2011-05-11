package cmion.inTheWild.competencies;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;
import cmion.inTheWild.datastructures.TrackingInfo;
import cmion.inTheWild.datastructures.TrackingInfoCollection;
import cmion.level2.migration.MigrationAware;
import cmion.storage.CmionStorageContainer;
import cmion.storage.EventPropertyChanged;
import cmion.storage.EventSubContainerAdded;

public class VirtualEmysConnector extends SamgarCompetency implements MigrationAware 
{
	
	private ArrayList<Integer> userIDs; // ids of present users
	private HashMap<Integer,TrackingInfo> userLocations; // locations of users
	private HashMap<Integer, Long> userDurations; // number of seconds a certain user was continuously seen 
	private ArrayList<Integer> handIDs;
	private ArrayList<Integer> disappearedHandIDs;
	private HashMap<Integer,TrackingInfo> handLocations; // locations of hands
	private HashMap<Integer, Long> handDurations; // number of ms a certain user was continuously seen 
	private int focusUserId; // the id of the user to focus on
	private long lastUserUpdate;
	private long lastHandUpdate;
	private CmionStorageContainer binaryChoiceContainer;
	
	public VirtualEmysConnector(IArchitecture architecture) {
		super(architecture);
	
		//name and type of the competence
		this.competencyName ="VirtualEmys";
		this.competencyType ="VirtualEmys";
	
		// create storage classes
		userIDs = new ArrayList<Integer>(); 
		userLocations = new HashMap<Integer,TrackingInfo>();
		userDurations = new HashMap<Integer, Long>(); 
		handIDs = new ArrayList<Integer>();
		disappearedHandIDs = new ArrayList<Integer>();
		handLocations = new HashMap<Integer,TrackingInfo>(); 
		handDurations = new HashMap<Integer, Long>();
		
		focusUserId = -1;
		lastUserUpdate = System.currentTimeMillis();
		lastHandUpdate = System.currentTimeMillis();
		
		binaryChoiceContainer = null;
	}	
	
	@Override
	public void registerHandlers()
	{
		super.registerHandlers();

		// listen to property changes on the blackboard
		HandleBlackBoardProperty propChangeHandler = new HandleBlackBoardProperty();
		architecture.getBlackBoard().getEventHandlers().add(propChangeHandler);
		
		// listen to sub containers being added to the black board
		HandleBlackBoardAddSC scAddedHandler = new HandleBlackBoardAddSC();
		architecture.getBlackBoard().getEventHandlers().add(scAddedHandler);
	}
		
	/** internal event handler class for listening to blackboard sub containers being added */
	private class HandleBlackBoardAddSC extends EventHandler {

	    public HandleBlackBoardAddSC() {
	        super(EventSubContainerAdded.class);
	    }

		@Override
	    public void invoke(IEvent evt) 
	    {
	    	if (evt instanceof EventSubContainerAdded)
	    	{
	    		EventSubContainerAdded evt1 = (EventSubContainerAdded) evt;
	    		if (evt1.getSubContainer().getContainerName().equals(EmysAskBinaryQuestion.CONTAINER_NAME))
	    		{
	    			binaryChoiceContainer = evt1.getSubContainer();
	    			String choice1 = evt1.getInitialProperties().get(EmysAskBinaryQuestion.CHOICE1).toString();
	    			String choice2 = evt1.getInitialProperties().get(EmysAskBinaryQuestion.CHOICE2).toString();
	    			sendBinaryChoiceRequest(choice1,choice2);	
	    		}
	    	}
	    }
	}
	
	/** internal event handler class for listening to black board property changes */
	private class HandleBlackBoardProperty extends EventHandler {

	    public HandleBlackBoardProperty() {
	        super(EventPropertyChanged.class);
	    }

		@Override
	    public void invoke(IEvent evt) 
	    {
	    	if (evt instanceof EventPropertyChanged)
	    	{
	    		EventPropertyChanged evt1 = (EventPropertyChanged) evt;
	    		if (evt1.getPropertyName().equals("UserTracking"))
	    		{
	    			if (evt1.getPropertyValue() instanceof TrackingInfoCollection)
	    				updateUserLocations((TrackingInfoCollection) evt1.getPropertyValue());			
	    		}	    		
	    		else if (evt1.getPropertyName().equals("HandTracking"))
	    		{
	    			if (evt1.getPropertyValue() instanceof TrackingInfoCollection)
	    				updateHandLocations((TrackingInfoCollection) evt1.getPropertyValue());			
	    		}	    		
	    		else if (evt1.getPropertyName().equals(EmysEmotion.EMOTION))
	    		{
	    			sendEmotion(evt1.getPropertyValue().toString());			
	    		}	    			    		
	    	}
	    }
	}
	
	
	private synchronized void updateUserLocations(TrackingInfoCollection tracker) 
	{
		// calculate time elapsed
		long newTime =  System.currentTimeMillis();
		long timeElapsed = newTime - lastUserUpdate;
		lastUserUpdate = newTime;
		
		// check if any previously tracked user has disappeard
		Iterator<Integer> it = userIDs.iterator();
		while (it.hasNext())
		{
			int userID = it.next();
			if (!tracker.hasID(userID)) 
			{	// found a user to remove
				it.remove();
				userLocations.remove(userID);
				userDurations.remove(userID);
			}
		}
			
		// add or update found users
		for (int userID :tracker.getAllIDs())
		{
			if (!userIDs.contains(userID)) // new user
			{
				userIDs.add(userID);
				userLocations.put(userID, tracker.getTrackingInfo(userID));
				userDurations.put(userID, new Long(0));	
			}
			else	// update user
			{
				userLocations.put(userID, tracker.getTrackingInfo(userID));
				long newDuration = userDurations.get(userID) + timeElapsed; 
				userDurations.put(userID, newDuration);
			}
		}
		
		updateFocus();
		
		sendUser();
		
	}
	
	private synchronized void updateHandLocations(TrackingInfoCollection tracker) 
	{
		// calculate time elapsed
		long newTime =  System.currentTimeMillis();
		long timeElapsed = newTime - lastHandUpdate;
		lastHandUpdate = newTime;
		
		// check if any previously tracked hand has disappeared
		Iterator<Integer> it = handIDs.iterator();
		while (it.hasNext())
		{
			int handID = it.next();
			if (!tracker.hasID(handID)) 
			{	// found a hand to remove
				it.remove();
				handLocations.remove(handID);
				handDurations.remove(handID);
				disappearedHandIDs.add(handID);
			}
		}
			
		// add or update found hands
		for (int handID :tracker.getAllIDs())
		{
			if (!handIDs.contains(handID)) // new hand
			{
				handIDs.add(handID);
				handLocations.put(handID, tracker.getTrackingInfo(handID));
				handDurations.put(handID, new Long(0));	
			}
			else	// update hand
			{
				handLocations.put(handID, tracker.getTrackingInfo(handID));
				long newDuration = handDurations.get(handID) + timeElapsed; 
				handDurations.put(handID, newDuration);
			}
		}
		
		sendHands();
	}
	
	private void updateFocus()
	{
		// if the current user is existent and was seen for less than 5 seconds,
		// don't change anything
		if (userIDs.contains(focusUserId) && userDurations.get(focusUserId)<5000) return; 
		
		// if no users are currently present focus on no one
		if (userIDs.size()==0)
		{
			focusUserId = -1;
			return;
		}
		
		int bestScore = -1;
		
		// we can or have to refocus, so focus on user with best score
		for (int userID : userIDs)
		{
			// score for this particular user 
			int score = 0;
			
			// for each hand of the user that is tracked, add the inverse of the duration
			for (int handID : handIDs)
			{
				if (handLocations.get(handID).getUserID() == userID)
				{	
					if (handDurations.get(handID)==0) // avoid division by zero
						score += 10000;
					else
						score += 10000 / handDurations.get(handID);
				}
			}
			
			// if you want other factors influencing the score add them below
						
			
			// now that we have the final score compare it to best score and refocus if necessary
			if (score > bestScore)
			{
				bestScore = score;
				focusUserId = userID;
			}
			
		}
				
	}
	
	private void sendUser()
	{
		Bottle b = prepareBottle();
		if (focusUserId == -1)
			b.addInt(1); // id 1 signifies no user to focus on
		else
		{ 				
			b.addInt(0);  // id 0 signifies x y z user coordinates
			TrackingInfo t = userLocations.get(focusUserId);
			b.addDouble(t.getX());
			b.addDouble(t.getY());
			b.addDouble(t.getZ());			
		}
		sendBottle();
	}
	
	private void sendHands()
	{
		Bottle b = prepareBottle();
		if (handIDs.size() == 0)
		{
			System.out.println("sending all hands disappeared");
			b.addInt(3); // id 3 signifies no hands
			disappearedHandIDs.clear();
		}
		else
		{ 				
			b.addInt(2);  // id 2 signifies hands to follow
			//first we write the number of disappeared hands, followed by their ids
			b.addInt(disappearedHandIDs.size());
			for (int dHandid : disappearedHandIDs)
			{	
				System.out.println("sending hand id " + dHandid + " disappeared");
				b.addInt(dHandid);
			}
			disappearedHandIDs.clear();
			
			// now write hands that are there, each as a tuple of id,x,y,z
			for (int handId : handIDs)
			{
				b.addInt(handId);
				TrackingInfo t = handLocations.get(handId);
				b.addDouble(t.getX());
				b.addDouble(t.getY());
				b.addDouble(t.getZ());
			}
		}
		sendBottle();
	}
	
	public void sendEmotion(String emotion)
	{
		Bottle b = prepareBottle();
		b.addInt(5); // id 5 signifies emotion 
		b.addString(emotion);
		sendBottle();		
	}
	
	private void sendVisible(boolean visible) 
	{
		Bottle b = prepareBottle();
		b.addInt(6); // id 6 signifies setVisible 
		if (visible)
			b.addInt(1);
		else
			b.addInt(0);
		sendBottle();		
	}
	
	public void sendBinaryChoiceRequest(String choice1, String choice2)
	{
		Bottle b = prepareBottle();
		b.addInt(4); // id 4 signifies binary choice request
		b.addString(choice1);
		b.addString(choice2);
		sendBottle();
	}
	
	/** messages we get back from virtual emys are received here */
	@Override
	public void onRead(Bottle bottleIn) 
	{
		if (bottleIn.size()>0)
		{
			int msgType = bottleIn.get(0).asInt();
			if (msgType == 4) // type 4 signifies the user has chosen an answer to the question he was asked
			{
				String answer = bottleIn.get(1).asString().toString();
				if (binaryChoiceContainer!=null)
				{
					binaryChoiceContainer.requestSetProperty(EmysAskBinaryQuestion.ANSWER, answer);
					// we dont want the reference to the container anymore
					binaryChoiceContainer = null;
				}
			}
			
		}
	}


	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		// we just sleep in the main thread, we react on blackboard and yarp callbacks instead
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

	@Override
	public void onMigrationIn() 
	{
		sendVisible(true);
	}

	@Override
	public void onMigrationOut() 
	{
		sendVisible(false);		
	}

	@Override
	public void onMigrationSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMigrationFailure() {
		// TODO Auto-generated method stub
		
	}

}
