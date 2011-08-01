package cmion.TeamBuddy.competencies;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;
import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;
import cmion.storage.WorldModel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class WebUIProxy extends Competency {

	public static final String TASK = "task";
	public static final String TASK_MSG = "msg";
	public static final String MSG_BODY = "msgBody";
	public static final String MSG_TO = "msgTo";
	public static final String MSG_FROM = "msgFrom";		
	public static final String MSG_BLACKBOARD_CONTAINER = "messagesToDeliver";
	public static final String TASK_GREET = "greet";
	public static final String GREET_LOCATION = "greetLocation";
	public static final String GREET_PHONE = "greetPhone";
	public static final String GREET_NAME = "greetName";		
	
	
	// running id for messages
	private int msgID;
	
	private class FormHandler extends AbstractHandler
	{
		HashMap<String,String> mFormData;
		
		public FormHandler() {
			this.mFormData = new HashMap<String, String>();
		}
		
	  
		public void handle(String target,
	                       Request baseRequest,
	                       HttpServletRequest request,
	                       HttpServletResponse response) 
	        throws IOException, ServletException
	    {
	        //clear previous data
			mFormData.clear();
			
	        response.setContentType("text/html");
	        PrintWriter out = response.getWriter();

	        out.println("<title>Message Sent</title>" +
	           "<body bgcolor=000000 text=FFFFFF>");

	        out.println("<h2>Message Sent</h2> <br> <form><input type=\"button\" value=\"&lt;-- BACK\" onclick=\"history.go(-1);return false;\"/></form>");
	        out.close();
	        
	        String task = request.getParameter(TASK);
			if (task == null) return;
				
			if (task.equals(TASK_MSG))
			{
				String body = request.getParameter(MSG_BODY);
				String to = request.getParameter(MSG_TO);
				String from = request.getParameter(MSG_FROM);
				if ((body==null) || (to==null) || (from == null)) return;
				msgReceived(to, from, body);
			}
			else if (task.equals(TASK_GREET))
			{
				String name = request.getParameter(GREET_NAME);
				String phone = request.getParameter(GREET_PHONE);
				String location = request.getParameter(GREET_LOCATION);
				if ((name==null) || (phone==null) || (location == null)) return;
				greetVisitor(name, phone, location);				
			}				
	    }
	}
	
	public WebUIProxy(IArchitecture architecture) 
	{
		super(architecture);
		this.competencyName = "WebUIProxy";
		this.competencyType = "WebUIProxy";		
	}

	private void msgReceived(String to, String from, String body) 
	{
		// obtain an id for the message
		msgID++;
		String id = new Integer(msgID).toString();
		
		// write msg to blackboard
		if (architecture.getBlackBoard().hasSubContainer(MSG_BLACKBOARD_CONTAINER))
			architecture.getBlackBoard().getSubContainer(MSG_BLACKBOARD_CONTAINER).requestSetProperty(id, body);
		else
		{
			HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.put(id, body);
			architecture.getBlackBoard().requestAddSubContainer(MSG_BLACKBOARD_CONTAINER, MSG_BLACKBOARD_CONTAINER, properties);
		}
		
		//raise remote action
		ArrayList<String> parameters = new ArrayList<String>();
		parameters.add(to);
		parameters.add(id);
		MindAction ma = new MindAction(from,"leaveMessage",parameters);
		this.raise(new EventRemoteAction(ma));
	}

	private void greetVisitor(String name, String phone, String location) 
	{		
		if (architecture.getWorldModel().hasSubContainer("CurrentTask"))
		{
			architecture.getWorldModel().getSubContainer("CurrentTask").requestSetProperty("phoneNo", phone);			
			architecture.getWorldModel().getSubContainer("CurrentTask").requestSetProperty("task", "WelcomeGuest");
		}
		else
		{
			HashMap<String,Object> initialProperties = new HashMap<String,Object>();
			initialProperties.put("phoneNo", "447758148591");		
			initialProperties.put("task", "WelcomeGuest");
			architecture.getWorldModel().requestAddSubContainer("CurrentTask", WorldModel.OBJECT_TYPE_NAME, initialProperties);
		}
		//raise remote action
		//ArrayList<String> parameters = new ArrayList<String>();
		//parameters.add(name);
		//parameters.add(phone);
		//parameters.add(location);
		//MindAction ma = new MindAction("User","giveTaskGreetVisitor",parameters);
		//this.raise(new EventRemoteAction(ma));
	}	
	
	@Override
	public boolean runsInBackground() 
	{
		return true;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters)
			throws CompetencyCancelledException 
	{
		// prepare blackboard, request setting container
		architecture.getBlackBoard().requestAddSubContainer(MSG_BLACKBOARD_CONTAINER, MSG_BLACKBOARD_CONTAINER);
				
        Server server = new Server(8080);
        server.setHandler(new FormHandler());
 
        try {
			server.start();
	        server.join(); //this blocks
        } catch (Exception e) { return false;}

		return true;
	}

	@Override
	public void initialize() 
	{
		msgID = 0;
		this.available = true;
	}
	
	

	
	
	
}
