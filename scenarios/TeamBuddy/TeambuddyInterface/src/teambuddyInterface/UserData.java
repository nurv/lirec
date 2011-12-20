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

	public User getUser(String username) {
		if (guestUser.getUsername().equals(username)) {
			return guestUser;
		} else {
			for (User user : users) {
				if (user.getUsername().equals(username)) {
					return user;
				}
			}
		}
		return null;
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

	// return user who provided informationItem
	public User getUserProvided(InformationItem informationItem) {

		// loop over users
		for (User user : users) {
			if (user.getInformationItems().contains(informationItem)) {
				return user;
			}
		}

		return null;
	}

	// request information items of typename provided by username for which loginUser is authorised (creates a request) 
	public LinkedList<InformationItem> requestAuthorisedInformationItems(String username, String typename, String loginUsername) {
		LinkedList<InformationItem> authorisedInformationItems = new LinkedList<InformationItem>();

		for (InformationItem informationItem : getUser(username).getInformationItems()) {
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
					authorisedInformationItems.add(informationItem);
					request.setAuthorised(true);
				}
			}
		}

		return authorisedInformationItems;
	}

	// provide information item for username
	public boolean provideInformationItem(String username, String typename, String content, String[] authorisedRoles, String[] authorisedUsers) {
		boolean added = false;

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
		getUser(username).getInformationItems().add(informationItem);
		added = true;

		return added;
	}

	// delete information items for username 
	public int deleteInformationItems(String username, LinkedList<Long> deleteIds) {
		int deletedCount = 0;

		User user = getUser(username);
		for (int i = user.getInformationItems().size() - 1; i >= 0; i--) {
			InformationItem informationItem = user.getInformationItems().get(i);
			if (deleteIds.contains(informationItem.getId())) {
				user.getInformationItems().remove(informationItem);
				deletedCount++;
			}
		}

		return deletedCount;
	}

	// get information items of typename for username
	public LinkedList<InformationItem> getInformationItems(String username, String typename) {
		LinkedList<InformationItem> informationItems = new LinkedList<InformationItem>();

		for (InformationItem informationItem : getUser(username).getInformationItems()) {
			if (informationItem.getTypename().equals(typename)) {
				informationItems.add(informationItem);
			}
		}

		return informationItems;
	}

	// return information items provided since timeSince for which loginUsername is authorised
	public LinkedList<InformationItem> getAuthorisedInformationItemsProvidedSince(String loginUsername, long timeSince) {
		LinkedList<InformationItem> authorisedInformationItemsSince = new LinkedList<InformationItem>();

		// loop over users
		for (User user : users) {
			String username = user.getUsername();

			// loop over information items			
			for (InformationItem informationItem : user.getInformationItems()) {

				// check if information was provided after timeSince
				if (informationItem.getTimeProvided() > timeSince) {

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
						authorisedInformationItemsSince.add(informationItem);
					}

				}
			}

		}

		return authorisedInformationItemsSince;
	}

	// return information items requested since timeSince which were provided by username
	public LinkedList<InformationItem> getInformationItemsRequestedSince(String username, long timeSince) {
		LinkedList<InformationItem> informationItemsRequestedSince = new LinkedList<InformationItem>();

		// loop over information items
		for (InformationItem informationItem : getUser(username).getInformationItems()) {

			// loop over requests
			for (Request request : informationItem.getRequests()) {

				// check if requests happened after timeSince
				if (request.getTime() > timeSince) {
					informationItemsRequestedSince.add(informationItem);
					// add information item only once
					break;
				}

			}
		}

		return informationItemsRequestedSince;
	}

	// return information item with id provided by username
	public InformationItem getInformationItem(String username, long id) {

		// loop over information items
		for (InformationItem informationItem : getUser(username).getInformationItems()) {

			if (informationItem.getId() == id) {
				return informationItem;
			}
		}

		return null;
	}

	// return information items which are authorised to all roles
	public LinkedList<InformationItem> getInformationItemsPublic() {
		LinkedList<InformationItem> informationItemsPublic = new LinkedList<InformationItem>();

		// loop over users
		for (User user : users) {

			// loop over information items
			for (InformationItem informationItem : user.getInformationItems()) {

				boolean authorised = true;

				// loop over roles 
				for (Role role : roles) {
					if (!informationItem.getAuthorisedRoles().contains(role.getRolename())) {
						authorised = false;
						break;
					}
				}

				if (authorised) {					
					informationItemsPublic.add(informationItem);
				}

			}

		}

		return informationItemsPublic;
	}

	// returns the user for a specific information item
	public User getUser(InformationItem informationItem) {
		for (User user : users) {
			for (InformationItem informationItemCurrent : user.getInformationItems()) {
				if (informationItemCurrent == informationItem) {
					return user;
				}
			}
		}
		return null;
	}

	// request information item with id provided by username for which loginUsername is authorised (creates a request)
	public InformationItem requestAuthorisedInformationItem(String loginUsername, String username, long id) {

		// loop over information items
		for (InformationItem informationItem : getUser(username).getInformationItems()) {

			if (informationItem.getId() == id) {

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
					request.setAuthorised(true);
					return informationItem;
				}

			}

		}

		return null;
	}

	// return requests since timeSince for informationItem
	public LinkedList<Request> getRequestsSince(InformationItem informationItem, long timeSince) {
		LinkedList<Request> requestsSince = new LinkedList<Request>();

		// loop over requests
		for (Request request : informationItem.getRequests()) {

			// check if requests happened after timeSince
			if (request.getTime() > timeSince) {
				requestsSince.add(request);
			}
		}

		return requestsSince;
	}

	// return request count since timeSince for informationItems
	public int countRequestsSince(LinkedList<InformationItem> informationItems, long timeSince) {
		int count = 0;

		// loop over information items
		for (InformationItem informationItem : informationItems) {

			// loop over requests
			for (Request request : informationItem.getRequests()) {

				// check if requests happened after timeSince
				if (request.getTime() > timeSince) {
					count++;
				}
			}
		}

		return count;
	}

}
