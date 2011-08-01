package teambuddyInterface;

import java.util.ArrayList;

public class User {

	private String username;

	private String password;

	private String realname;

	private String rolename;

	private long timeLastLogin;

	private long timeLastLogout;

	private ArrayList<InformationItem> informationItems;

	public User() {
		informationItems = new ArrayList<InformationItem>();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getRolename() {
		return rolename;
	}

	public void setRolename(String rolename) {
		this.rolename = rolename;
	}

	public long getTimeLastLogin() {
		return timeLastLogin;
	}

	public void setTimeLastLogin(long timeLastLogin) {
		this.timeLastLogin = timeLastLogin;
	}

	public long getTimeLastLogout() {
		return timeLastLogout;
	}

	public void setTimeLastLogout(long timeLastLogout) {
		this.timeLastLogout = timeLastLogout;
	}

	public ArrayList<InformationItem> getInformationItems() {
		return informationItems;
	}

	public void setInformationItems(ArrayList<InformationItem> informationItems) {
		this.informationItems = informationItems;
	}

}
