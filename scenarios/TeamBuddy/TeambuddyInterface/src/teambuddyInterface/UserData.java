package teambuddyInterface;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

import cmion.storage.WorldModel;

public class UserData {

	private long nextId;

	//private ArrayList<InformationType> informationTypes;

	//private ArrayList<Role> roles;

	//private ArrayList<User> users;

	//private User guestUser;
	private static final String GUEST = "Guest";
	//private static final String INFOID = "infoId";
	//private static final String NEXTID = "nextId";
	
	private ArrayList<String> informationTypes;
	private ArrayList<String> roles;
	private ArrayList<String> users;
	
	private WorldModel wm;
	
	public UserData(WorldModel wm) {
		this.wm = wm;
		informationTypes = new ArrayList<String>();
		roles = new ArrayList<String>();
		users = new ArrayList<String>();
		/*if (this.getWMObjectProperty(INFOID, NEXTID) != null)
			nextId = (Long)this.getWMObjectProperty(INFOID, NEXTID);
		else
			nextId = 0;*/
	}
	
	public ArrayList<String> getUsers() {
		ArrayList<String> agents = wm.getAgentNames();
		users = new ArrayList<String>();
		
		for (String agent : agents)
		{
			String isUser = (String)this.getWMAgentProperty(agent, "isUser");
			if(Boolean.valueOf(isUser) && !agent.equals("Guest"))
			{
				users.add(agent);
			}				
		}
		return users;
	}
	
	public ArrayList<String> getRoles() {
		ArrayList<String> objects = wm.getObjectNames();
		roles = new ArrayList<String>();
		
		for (String object: objects)
		{
			if (Boolean.valueOf((String)this.getWMObjectProperty(object, "isRole"))) {
				roles.add(object);
			}				
		}
		return roles;
	}
	
	public String getUsername(String user) {
		if (this.getWMAgentProperty(user, "username") != null) {
			return (String)this.getWMAgentProperty(user, "username");
		}
		return "";
	}

	public void setUsername(String user, String username) {
		this.setWMAgentProperty(user, "username", username);
	}

	public boolean userIsAround(String user) {
		if (this.getWMAgentProperty(user, "around") != null) {
			return Boolean.valueOf((String)this.getWMAgentProperty(user, "around"));
		}
		return false;
	}
	
	public String getUserWhereabout(String user) {
		if (this.getWMAgentProperty(user, "whereabout") != null) {
			return (String)this.getWMAgentProperty(user, "whereabout");
		}
		return "";
	}

	public void setUserWhereabout(String user, String whereabout) {
		this.setWMAgentProperty(user, "whereabout", whereabout);
	}

	public String getPassword(String user) {
		if (this.getWMAgentProperty(user, "password") != null) {
			return (String)this.getWMAgentProperty(user, "password");
		}
		return "";
	}
	
	public void setPassword(String user, String password) {
		this.setWMAgentProperty(user, "password", password);
	}

	public String getGuestUser() {
		return GUEST;
	}
	
	public String getUserRealname(String user) {
		if (this.getWMAgentProperty(user, "realname") != null) {
			return (String)this.getWMAgentProperty(user, "realname");
		}
		return "";
	}

	public void setUserRealname(String user, String realname) {
		this.setWMAgentProperty(user, "realname", realname);
	}

	public String getRolename(String user) {
		if (this.getWMAgentProperty(user, "rolename") != null) {
			return (String)this.getWMAgentProperty(user, "rolename");
		}
		return "";
	}

	public String getRoleRealname(String role) {
		if (this.getWMObjectProperty(role, "realname") != null) {
			return (String)this.getWMObjectProperty(role, "realname");
		}
		return "";
	}
	
	public void setMadeRemark(String user, String madeRemark) {
		this.setWMAgentProperty(user, "madeRemark", madeRemark);
	}

	public String getMadeRemark(String user) {
		if (this.getWMAgentProperty(user, "madeRemark") != null) {
			return (String)this.getWMAgentProperty(user, "madeRemark");
		}
		return "";
	}
	
	public long getTimeLastLogin(String user) {
		if (this.getWMAgentProperty(user, "timeLastLogin") != null){
			return (Long)this.getWMAgentProperty(user, "timeLastLogin");
		}
		return -1;
	}

	public void setTimeLastLogin(String user, long timeLastLogin) {
		this.setWMAgentProperty(user, "timeLastLogin", timeLastLogin);
	}

	public long getTimeLastLogout(String user) {
		if (this.getWMAgentProperty(user, "timeLastLogout") != null){
			return (Long)this.getWMAgentProperty(user, "timeLastLogout");
		}
		return -1;
	}

	public void setTimeLastLogout(String user, long timeLastLogout) {
		this.setWMAgentProperty(user, "timeLastLogout", timeLastLogout);
	}

	private Object getWMObjectProperty(String objectName, String propertyName) {
		if (wm.hasObject(objectName)) {
			return wm.getObject(objectName).getPropertyValue(propertyName);
		}
		return null;
	}
	
	private void setWMAgentProperty(String agentName, String propertyName, Object propertyValue) {
		if (wm.hasAgent(agentName)) {
			wm.getAgent(agentName).requestSetProperty(propertyName, propertyValue);
		} else {
			HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.put(propertyName, propertyValue);
			wm.requestAddAgent(agentName, properties);
		}		
	}
	
	private Object getWMAgentProperty(String agentName, String propertyName) {
		if (wm.hasAgent(agentName)) {
			return wm.getAgent(agentName).getPropertyValue(propertyName);
		}
		return null;
	}	
}