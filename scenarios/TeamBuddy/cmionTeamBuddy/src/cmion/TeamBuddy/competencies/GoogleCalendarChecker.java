package cmion.TeamBuddy.competencies;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.storage.CmionStorageContainer;

public class GoogleCalendarChecker extends Competency {

	public static final String EVENT_CONTAINER_WORLD_MODEL = "CalendarEvents";
	public static final String EVENT_CONTAINER_BLACKBOARD = "CalendarEvents";
	
	/** the URL for this calendar feed*/
	private String feedURL;
	
	/** google account login name */
	private String login;
	
	/** google account password */
	private String pw;
	
	/** person who owns this calendar */
	private String owner;
	
	/** map of events to keep track when events disappear / expire, key is event name, value is event id */
	private HashMap<String,String> eventList;
	
	/** running id for events */
	private static int id;
	
	public GoogleCalendarChecker(IArchitecture architecture, String owner, String login, String pw, String feedURL) 
	{
		super(architecture);
		this.login = login;
		this.pw = pw;
		this.owner = owner;
		this.feedURL = feedURL;
		this.competencyName = "GoogleCalendarChecker";
		this.competencyType = "GoogleCalendarChecker";
		id = 0;
	}

	@Override
	public boolean runsInBackground() 
	{
		return true;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		while(true)
		{
			try {
				
				// Set up the URL and the object that will handle the connection:
				URL feedUrl = new URL(feedURL);

				CalendarQuery myQuery = new CalendarQuery(feedUrl);
				
				// check for events between now and 20 minutes from now
				GregorianCalendar cal = new GregorianCalendar();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			
				myQuery.setMinimumStartTime(DateTime.parseDateTime(	sdf.format(cal.getTime())));
				cal.add(Calendar.MINUTE, 20);				
				myQuery.setMaximumStartTime(DateTime.parseDateTime(	sdf.format(cal.getTime())));

				CalendarService myService = new CalendarService("exampleCo-exampleApp-1");
				myService.setUserCredentials(login, pw);

				// Send the request and receive the response:
				CalendarEventFeed myFeed = myService.query(myQuery, CalendarEventFeed.class);
				
				// check if any old events have expired, i.e. are not present anymore
				for (Iterator<Entry<String,String>> it = eventList.entrySet().iterator(); it.hasNext();)
				{
					// get the name and id of an event we have remembered
					Entry<String,String> pair = it.next();
					String eventName = pair.getKey();
					String eventID = pair.getValue();
					// assume it is not anymore valid
					boolean stillValid = false;
					// iterate over all events from the calendar query to see if it is still there
					// if so, it's still valid
					for (int i = 0; i < myFeed.getEntries().size(); i++) 
					{
						CalendarEventEntry entry = myFeed.getEntries().get(i);
						entry.getTitle().getPlainText();
						if (entry.getTitle().getPlainText().equals(eventName))
						{
							stillValid = true;
							break;
						}
					}

					if (! stillValid)
					{
						// remove event as it is not present in our current query anymore
						it.remove();
						// remove from world model
						architecture.getWorldModel().getObject(EVENT_CONTAINER_WORLD_MODEL).requestRemoveProperty(eventID);
						// remove from black board
						architecture.getBlackBoard().getSubContainer(EVENT_CONTAINER_BLACKBOARD).requestRemoveProperty(eventID);
						System.out.println("GoogleCalendarChecker: event expired ("+ eventID +")for " + owner + ": "+ eventName );					
					}
				}
				
				// add new events
				for (int i = 0; i < myFeed.getEntries().size(); i++) {
					CalendarEventEntry entry = myFeed.getEntries().get(i);
					String eventName = entry.getTitle().getPlainText();
					if (!eventList.containsKey(eventName))
					{
						addNewEvent(sdf.format(cal.getTime()),eventName);
					}
				
				}
				

			} catch (Exception e) {
				//e.printStackTrace();
				System.err.println("PROBLEM WITH GOOGLE CALENDAR");
			}
			// sleep 1 minute after every query
			try {
				Thread.sleep(1 * 60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void initialize() 
	{
		eventList = new HashMap<String,String>();
		available = true;
	}
	
	private void addNewEvent(String timeNow, String eventName)
	{	
		// get a new id for the event
		id ++;
		String eventId = new Integer(id).toString();

		eventList.put(eventName,eventId);

		System.out.println("GoogleCalendarChecker: new event ("+ eventId +")for " + owner + ": "+ eventName + ", current time: " + timeNow );
		
		// write event name to blackboard
		if (architecture.getBlackBoard().hasSubContainer(EVENT_CONTAINER_BLACKBOARD))
			architecture.getBlackBoard().getSubContainer(EVENT_CONTAINER_BLACKBOARD).requestSetProperty(eventId, eventName);
		else
		{
			HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.put(eventId, eventName);
			architecture.getBlackBoard().requestAddSubContainer(EVENT_CONTAINER_BLACKBOARD, EVENT_CONTAINER_BLACKBOARD, properties);
		}
		
		// post event information to world model		
		if (architecture.getWorldModel().hasObject(EVENT_CONTAINER_WORLD_MODEL))
		{
			CmionStorageContainer eventContainer = architecture.getWorldModel().getObject(EVENT_CONTAINER_WORLD_MODEL);
			eventContainer.requestSetProperty(eventId, owner);
		}
		else 
		{
			HashMap<String,Object> initialProperties = new HashMap<String,Object>();
			initialProperties.put(eventId, owner);
			architecture.getWorldModel().requestAddObject(EVENT_CONTAINER_WORLD_MODEL, initialProperties);
		}		
	}

}
