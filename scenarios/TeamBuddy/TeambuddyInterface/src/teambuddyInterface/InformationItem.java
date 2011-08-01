package teambuddyInterface;

import java.util.ArrayList;
import java.util.LinkedList;

public class InformationItem {

	private long id;

	private String typename;

	private long timeProvided;

	private ArrayList<String> authorisedRoles;

	private ArrayList<String> authorisedUsers;

	private String content;

	private LinkedList<Request> requests;

	public InformationItem() {
		authorisedRoles = new ArrayList<String>();
		authorisedUsers = new ArrayList<String>();
		requests = new LinkedList<Request>();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTypename() {
		return typename;
	}

	public void setTypename(String typename) {
		this.typename = typename;
	}

	public long getTimeProvided() {
		return timeProvided;
	}

	public void setTimeProvided(long timeProvided) {
		this.timeProvided = timeProvided;
	}

	public ArrayList<String> getAuthorisedRoles() {
		return authorisedRoles;
	}

	public void setAuthorisedRoles(ArrayList<String> authorisedRoles) {
		this.authorisedRoles = authorisedRoles;
	}

	public ArrayList<String> getAuthorisedUsers() {
		return authorisedUsers;
	}

	public void setAuthorisedUsers(ArrayList<String> authorisedUsers) {
		this.authorisedUsers = authorisedUsers;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LinkedList<Request> getRequests() {
		return requests;
	}

	public void setRequests(LinkedList<Request> requests) {
		this.requests = requests;
	}

}
