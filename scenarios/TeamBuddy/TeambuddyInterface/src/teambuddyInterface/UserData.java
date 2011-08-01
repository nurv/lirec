package teambuddyInterface;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

public class UserData {

	private long nextId;

	private ArrayList<InformationType> informationTypes;

	private ArrayList<Role> roles;

	private ArrayList<User> users;

	private User guestUser;

	public UserData() {
		informationTypes = new ArrayList<InformationType>();
		roles = new ArrayList<Role>();
		users = new ArrayList<User>();
	}

	public long getNextId() {
		return nextId;
	}

	public void setNextId(long nextId) {
		this.nextId = nextId;
	}

	public ArrayList<InformationType> getInformationTypes() {
		return informationTypes;
	}

	public void setInformationTypes(ArrayList<InformationType> informationTypes) {
		this.informationTypes = informationTypes;
	}

	public ArrayList<Role> getRoles() {
		return roles;
	}

	public void setRoles(ArrayList<Role> roles) {
		this.roles = roles;
	}

	public ArrayList<User> getUsers() {
		return users;
	}

	public void setUsers(ArrayList<User> users) {
		this.users = users;
	}

	public User getGuestUser() {
		return guestUser;
	}

	public void setGuestUser(User guestUser) {
		this.guestUser = guestUser;
	}

	public String getUserRealname(String username) {
		if (guestUser.getUsername().equals(username)) {
			return guestUser.getRealname();
		} else {
			for (User user : users) {
				if (user.getUsername().equals(username)) {
					return user.getRealname();
				}
			}
		}
		return null;
	}

	public String getUserRolename(String username) {
		if (guestUser.getUsername().equals(username)) {
			return guestUser.getRolename();
		} else {
			for (User user : users) {
				if (user.getUsername().equals(username)) {
					return user.getRolename();
				}
			}
		}
		return null;
	}

	public String getRoleRealname(String rolename) {
		for (Role role : roles) {
			if (role.getRolename().equals(rolename)) {
				return role.getRealname();
			}
		}
		return null;
	}

	public String getTypeRealname(String typename) {
		for (InformationType informationType : informationTypes) {
			if (informationType.getTypename().equals(typename)) {
				return informationType.getRealname();
			}
		}
		return null;
	}

	public InformationType getType(String typename) {
		for (InformationType informationType : informationTypes) {
			if (informationType.getTypename().equals(typename)) {
				return informationType;
			}
		}
		return null;
	}

	public boolean provideInformationItem(String username, String typename, String content, String[] authorisedRoles, String[] authorisedUsers) {
		boolean added = false;
		// check if username exists
		for (User user : users) {
			if (user.getUsername().equals(username)) {
				// create information item
				InformationItem informationItem = new InformationItem();
				informationItem.setId(nextId++);
				informationItem.setTypename(typename);
				informationItem.setTimeProvided(Calendar.getInstance().getTimeInMillis());
				// authorised roles
				if (authorisedRoles != null) {
					for (String authorisedRole : authorisedRoles) {
						informationItem.getAuthorisedRoles().add(authorisedRole);
					}
				}
				// authorised users
				if (authorisedUsers != null) {
					for (String authorisedUser : authorisedUsers) {
						informationItem.getAuthorisedUsers().add(authorisedUser);
					}
				}
				// content
				informationItem.setContent(content);
				// add information item
				user.getInformationItems().add(informationItem);
				added = true;
			}
		}
		return added;
	}

	public LinkedList<InformationItem> requestInformationItems(String username, String typename, String loginUsername) {
		LinkedList<InformationItem> informationItems = new LinkedList<InformationItem>();
		for (User user : users) {
			if (user.getUsername().equals(username)) {
				for (InformationItem informationItem : user.getInformationItems()) {
					if (informationItem.getTypename().equals(typename)) {
						// add request
						Request request = new Request();
						request.setTime(Calendar.getInstance().getTimeInMillis());
						request.setUsername(loginUsername);
						request.setAuthorised(false);
						informationItem.getRequests().add(request);
						// check for authorisation
						boolean authorised = false;
						if (username.equals(loginUsername)) {
							authorised = true;
						} else {
							for (String authorisedRole : informationItem.getAuthorisedRoles()) {
								if (authorisedRole.equals(getUserRolename(loginUsername))) {
									authorised = true;
								}
							}
							for (String authorisedUser : informationItem.getAuthorisedUsers()) {
								if (authorisedUser.equals(loginUsername)) {
									authorised = true;
								}
							}
						}
						if (authorised) {
							informationItems.add(informationItem);
							request.setAuthorised(true);
						}
					}
				}
			}
		}
		return informationItems;
	}

	public int deleteInformationItems(String loginUsername, String username, LinkedList<Long> deleteIds) {
		int deletedCount = 0;
		if (username.equals(loginUsername)) {
			for (User user : users) {
				if (user.getUsername().equals(username)) {
					for (int i = user.getInformationItems().size() - 1; i >= 0; i--) {
						InformationItem informationItem = user.getInformationItems().get(i);
						if (deleteIds.contains(informationItem.getId())) {
							user.getInformationItems().remove(informationItem);
							deletedCount++;
						}
					}
				}
			}
		}
		return deletedCount;
	}

	public LinkedList<InformationItem> getInformationItems(String username, String typename, String loginUsername) {
		LinkedList<InformationItem> informationItems = new LinkedList<InformationItem>();
		if (username.equals(loginUsername)) {
			for (User user : users) {
				if (user.getUsername().equals(username)) {
					for (InformationItem informationItem : user.getInformationItems()) {
						if (informationItem.getTypename().equals(typename)) {
							informationItems.add(informationItem);
						}
					}
				}
			}
		}
		return informationItems;
	}

}
