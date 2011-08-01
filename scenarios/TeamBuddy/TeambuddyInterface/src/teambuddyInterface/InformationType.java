package teambuddyInterface;

import java.util.ArrayList;

public class InformationType {

	private String typename;

	private String realname;

	private ArrayList<String> authorisedRolesDefault;

	public InformationType() {
		authorisedRolesDefault = new ArrayList<String>();
	}

	public String getTypename() {
		return typename;
	}

	public void setTypename(String typename) {
		this.typename = typename;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public ArrayList<String> getAuthorisedRolesDefault() {
		return authorisedRolesDefault;
	}

	public void setAuthorisedRolesDefault(ArrayList<String> authorisedRolesDefault) {
		this.authorisedRolesDefault = authorisedRolesDefault;
	}

}
