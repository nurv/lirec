package cmion.TeamBuddy.competencies;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.storage.CmionStorageContainer;

public class GoogleCalendarChecker extends Competency {

	/** the URL for this calendar feed*/
	private String feedURL;
	
	/** google account login name */
	private String login;
	
	/** google account password */
	private String pw;
	
	/** person who owns this calendar */
	private String owner;
	
	/** list of events to keep track when events disappear / expire */
	private ArrayList<String> eventList;
	
	public GoogleCalendarChecker(IArchitecture architecture, String owner, String login, String pw, String feedURL) 
	{
		super(architecture);
		this.login = login;
		this.pw = pw;
		this.owner = owner;
		this.feedURL = feedURL;
		this.competencyName = "GoogleCalendarChecker";
		this.competencyType = "GoogleCalendarChecker";
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
				
				// check for events between 30 minutes from now and 35 minutes from now
				GregorianCalendar cal = new GregorianCalendar();
				cal.add(Calendar.MINUTE, 30);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				System.out.println(sdf.format(cal.getTime()));
			
				myQuery.setMinimumStartTime(DateTime.parseDateTime(	sdf.format(cal.getTime())));
				cal.add(Calendar.MINUTE, 5);				
				myQuery.setMaximumStartTime(DateTime.parseDateTime(	sdf.format(cal.getTime())));

				CalendarService myService = new CalendarService("exampleCo-exampleApp-1");
				myService.setUserCredentials(login, pw);

				// Send the request and receive the response:
				CalendarEventFeed myFeed = myService.query(myQuery, CalendarEventFeed.class);
				
				// check if any old events have expired, i.e. are not present anymore
				for (Iterator<String> it = eventList.iterator(); it.hasNext();)
				{
					String eventName = it.next();
					if (! myFeed.getEntries().contains(eventName))
					{
						// remove event as it is not present in our current query anymore
						it.remove();
						// remove from world model
						String propName = getPropName(eventName);
						architecture.getWorldModel().getAgent(owner).requestRemoveProperty(propName);
					}
				}
				
				// add new events
				System.out.println("events:");
				for (int i = 0; i < myFeed.getEntries().size(); i++) {
					CalendarEventEntry entry = myFeed.getEntries().get(i);
					System.out.println("\t" + entry.getTitle().getPlainText());
					String eventName = entry.getTitle().getPlainText();
					if (!eventList.contains(eventName))
					{
						eventList.add(eventName);
						String propName = getPropName(entry.getTitle().getPlainText());
						String propValue = "True";

						if (architecture.getWorldModel().hasAgent(owner))
						{
							CmionStorageContainer person = architecture.getWorldModel().getAgent(owner);
							person.requestSetProperty(propName, propValue);
						}
						else 
						{
							HashMap<String,Object> initialProperties = new HashMap<String,Object>();
							initialProperties.put(propName, propValue);
							architecture.getWorldModel().requestAddAgent(owner, initialProperties);
						}
					}
				
				}
				

			} catch (Exception e) {
				e.printStackTrace();
			}
			// sleep 5 minutes after every query
			try {
				Thread.sleep(300000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String getPropName(String eventName)
	{
		return "Event(" + eventName + ")";
	}

	@Override
	public void initialize() 
	{
		eventList = new ArrayList<String>();
		available = true;
	}

}
