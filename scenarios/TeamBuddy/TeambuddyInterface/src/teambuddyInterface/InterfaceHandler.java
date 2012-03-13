package teambuddyInterface;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;
import cmion.storage.BlackBoard;
import cmion.storage.WorldModel;

public class InterfaceHandler extends AbstractHandler {
	
	private InterfaceCompetency interfaceCompetency;

	private static final String BB_INFORMATION_NEEDS = "InformationNeeds";

	private static final String WM_INFORMATION_REQUESTERS = "InformationRequesters";

	private static final String WM_INFORMATION_PROVIDERS = "InformationProviders";

	private static int informationNeedId = 0;

	private static final String BB_MESSAGE_TEXTS = "MessageTexts";

	private static final String WM_MESSAGE_SENDERS = "MessageSenders";

	private static final String WM_MESSAGE_RECIPIENTS = "MessageRecipients";

	private static int messageId = 0;

	private static final String BB_INTERFACE_UTTERANCE = "InterfaceUtterance";

	private static final String WM_CURRENT_PLATFORM = "CurrentPlatform";

	private static final String WM_CHARGING = "charging";

	private static final String WM_LOCATION = "location";

	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	private static final String XML_FILENAME = "UserData.xml";

	private XMLAccess xmlAccess;

	private UserData userData;

	//private User loginUser;
	private String loginUser;

	private static final String WM_USERDATA = "UserData";

	private static final boolean ENABLE_SEND_HOME = false;

	private static final boolean LOGIN_USERNAME_SELECT = true;

	private static final String USERNAME_UNKNOWN = "Unknown";

	private Timer timer;

	private static final long INTERACTION_TIMEOUT = 20000;
	private static final long NON_INTERACTION_TIMEOUT = 15000;

	private static final String WM_INTERFACE_INTERACTION = "interfaceInteraction";
	private static int nextID = 0;

	private Random random;
	
	private WorldModel wm;
	
	private String remarkUser;
	private String remark;
	private String remarkText;
	private long remarkTimeStamp;
	private ArrayList<Interaction> interactions;

	//private String interactionFile = "fatima-bin/data/characters/minds/Interactions.xml";
	
	public InterfaceHandler(InterfaceCompetency interfaceCompetency) {
		this.interfaceCompetency = interfaceCompetency;

		setWMObjectProperty(WM_CURRENT_PLATFORM, WM_INTERFACE_INTERACTION, "False");
		timer = new Timer();
		wm = interfaceCompetency.getArchitecture().getWorldModel();
		xmlAccess = new XMLAccess();
		interactions = new ArrayList<Interaction>();
		interactions = xmlAccess.loadInteractionsXML();
		nextID = xmlAccess.getMaxID() + 1;
		userData = new UserData(wm);	
		
		remarkUser = "";
		remark = "";
		remarkText = "";
		remarkTimeStamp = 0;
		loginUser = "";
		// write to WorldModel
		/*for (User user : userData.getUsers()) {
			for (InformationItem informationItem : user.getInformationItems()) {
				//setWMObjectProperty(WM_USERDATA, user.getUsername() + "," + informationItem.getTypename() + "," + informationItem.getId(), informationItem.getContent().replace(" ", "_"));
				setWMObjectProperty(user.getUsername(), informationItem.getTypename(), informationItem.getContent().replace(" ", "_"));
			}
		}*/
		// Make sure the container for the UserData exists in the WorldModel,
		// otherwise some items will not go to the WorldModel when loading sequentially.
		// Create container already in WorldModelInit.xml.

		random = new Random();
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		System.out.println("Interaction timer set ");
		// schedule timeout task
		timer.cancel();
		timer.purge();
		timer = new Timer();
		timer.schedule(new TimeoutTask(interfaceCompetency, WM_CURRENT_PLATFORM, WM_INTERFACE_INTERACTION), INTERACTION_TIMEOUT);
		setWMObjectProperty(WM_CURRENT_PLATFORM, WM_INTERFACE_INTERACTION, "True");

		// process action
		String action = request.getParameter("action");
		// page
		String page = request.getParameter("page");

		if (!loginUser.equals("")){
			if (action == null) {
				// do nothing
	
			} else if (action.equals("logout")) {
				// logout
	
				// raise remote action
				ArrayList<String> parameters = new ArrayList<String>();
				parameters.add("SELF");
				raiseMindAction(loginUser, "logout", parameters);
				loginUser = "";							
			} else if (action.equals("sendHome")) {
				// send Teambuddy back to home position
	
				// raise remote action
				ArrayList<String> parameters = new ArrayList<String>();
				parameters.add("SELF");
				raiseMindAction(loginUser, "sendHome", parameters);
	
			} else if (action.equals("leaveMessage")) {
				// leave a message
				String messageFromRealname = request.getParameter("messageFromRealname");
				String messageFromUsername = request.getParameter("messageFromUsername");
				String messageToUsername = request.getParameter("messageToUsername");
				String messageText = request.getParameter("messageText");
				if (messageText == null || messageText.trim().equals("")) {
					ArrayList<String> parameters = new ArrayList<String>();
					parameters.add("SELF");
					raiseMindAction(messageFromUsername, "leaveEmptyMessage", parameters);
				} else {
					if (messageFromUsername.equals(userData.getGuestUser())) {
						// problem with FAtiMA event when name contains spaces
						//msgReceived(messageFromRealname, messageToUsername, messageText);
						leaveMessage(messageFromRealname.replace(" ", "_"), messageToUsername, messageText);
					} else {
						leaveMessage(messageFromUsername, messageToUsername, messageText);
					}
				}
	
			} else if (action.equals("askIsAround")) {
				
				//check if the user is around
				String username = request.getParameter("username");
				String utterance = "";
	
				// check if the user is around
				if (userData.userIsAround(username)) {
					utterance = userData.getUsername(username) + " is around today.";
				} else {
					utterance = "I have not seen " + userData.getUsername(username) + " today.";
				}
	
				System.out.println(utterance);
				// write utterance to blackboard
				setBBProperty(BB_INTERFACE_UTTERANCE, utterance);
	
				// raise remote action
				ArrayList<String> parameters = new ArrayList<String>();
				parameters.add("SELF");
				parameters.add(username);
				raiseMindAction(loginUser, "askIsAround", parameters);
			} else if (action.equals("askWhereabout")) {
				
				//check for the user's location
				String username = request.getParameter("username");
				String utterance = "";
				String userWhereabout = userData.getUserWhereabout(username);
	
				// check if the user is around
				if (userWhereabout.equals("Unknown")){
					utterance = "I am sorry, I don't know where " + userData.getUsername(username) + " is.";
				}
				else if (userWhereabout.equals("Office")){
					utterance = userData.getUsername(username) + " is in the office.";
				}
				else {
					if (userWhereabout.equals("Others")) {
						utterance = "I am sorry, " + userData.getUsername(username) + " didn't leave a note.";
					}
					else {
						utterance = userData.getUsername(username) + " is at " + userWhereabout + ".";
					}
				}
	
				System.out.println(utterance);
				// write utterance to blackboard
				setBBProperty(BB_INTERFACE_UTTERANCE, utterance);
	
				// raise remote action
				ArrayList<String> parameters = new ArrayList<String>();
				parameters.add("SELF");
				parameters.add(username);
				raiseMindAction(loginUser, "askWhereabout", parameters);
			} else if (action.equals("informWhereabout")) {
				
				//check for the user's location
				String whereabout = request.getParameter("whereabout").toLowerCase();
				String utterance = "";
	
				// check if the user is around
				if (!whereabout.equals("")) {
					if (whereabout.equals("Others")) {
						utterance = "Thanks for telling me that you will be away.";
					}
					else {
						utterance = "Thanks for telling me that you will be at " + whereabout + ".";
					}
					userData.setUserWhereabout(loginUser,whereabout);
				}
	
				System.out.println(utterance);
				// write utterance to blackboard
				setBBProperty(BB_INTERFACE_UTTERANCE, utterance);
	
				// raise remote action
				ArrayList<String> parameters = new ArrayList<String>();
				parameters.add("SELF");
				parameters.add(whereabout);
				raiseMindAction(loginUser, "informWhereabout", parameters);
	
			} else if (action.equals("reply")) {
				// answer to team buddy's remark
				String reply = request.getParameter("answer");
				String utterance = "";
	
				// check if content is empty
				if (reply == null || reply.trim().equals("")) {
					utterance = "You didn't provide any comment.";		
				} else {
					utterance = "You said " + reply + ".";
				}
	
				// write utterance to blackboard
				setBBProperty(BB_INTERFACE_UTTERANCE, utterance);
	
				// create the interaction to be logged
				Interaction interaction = new Interaction(nextID, loginUser, remark, reply);
				interactions.add(interaction);
				
				// raise remote action
				ArrayList<String> parameters = new ArrayList<String>();
				parameters.add("SELF");
				parameters.add(remark);
				parameters.add(String.valueOf(nextID++));
				raiseMindAction(loginUser, "reply", parameters);			
				
				// reset the remark time stamp
				remarkTimeStamp = 0;
				xmlAccess.saveInteractionsXML(interactions);	
			} else if (action.equals("charging")) {
				// set charging status
	
				String chargingStatus = request.getParameter("chargingStatus");
				if (chargingStatus.equals("true")) {
					setWMObjectProperty(WM_CURRENT_PLATFORM, WM_CHARGING, "True");
				} else if (chargingStatus.equals("false")) {
					setWMObjectProperty(WM_CURRENT_PLATFORM, WM_CHARGING, "False");
				}
	
				// wait for property to be changed
				try {
					Thread.sleep(500);
				} catch (Exception e) {
					e.printStackTrace();
				}
	
				// raise remote action
				ArrayList<String> parameters = new ArrayList<String>();
				parameters.add("SELF");
				parameters.add(chargingStatus);
				raiseMindAction(loginUser, "setCharging", parameters);
	
			} else if (action.equals("sendDocking")) {
				// send to dock in docking station
	
				// raise remote action
				ArrayList<String> parameters = new ArrayList<String>();
				parameters.add("SELF");
				raiseMindAction(loginUser, "sendDocking", parameters);
	
			} else if (action.equals("sendUndocking")) {
				// send to undock from docking station
	
				// raise remote action
				ArrayList<String> parameters = new ArrayList<String>();
				parameters.add("SELF");
				raiseMindAction(loginUser, "sendUndocking", parameters);
			}
		} else {
			if (action == null) {
				// do nothing
			} else if (action.equals("login")) {
				// login
				
				String loginUsername = request.getParameter("loginUsername");
				String loginPassword = request.getParameter("loginPassword");
				
				//User login
				if (userData.getGuestUser().equals(loginUsername)) {
					// guest login
					if (userData.getPassword(userData.getGuestUser()).equals(loginPassword)) {
						loginUser = userData.getGuestUser();
					}
	
				} else {
					for (String user : userData.getUsers()) {
						if (user.equals(loginUsername)) {
							if (userData.getPassword(user).equals(loginPassword)) {
								loginUser = user;
							}
						}
					}
				}
			
				if (!(loginUser.equals(""))) {
					// a user is logged in
					// raise remote action
					ArrayList<String> parameters = new ArrayList<String>();
					parameters.add("SELF");
					raiseMindAction(loginUser, "login", parameters);
				}
			} else if (action.equals("sendHome")) {
					// send Teambuddy back to home position
					ArrayList<String> parameters = new ArrayList<String>();
					parameters.add("SELF");
					raiseMindAction(USERNAME_UNKNOWN, "sendHome", parameters);
		
			}
			else if (action.equals("charging")) {
				// set charging status
	
				String chargingStatus = request.getParameter("chargingStatus");
				if (chargingStatus.equals("true")) {
					setWMObjectProperty(WM_CURRENT_PLATFORM, WM_CHARGING, "True");
				} else if (chargingStatus.equals("false")) {
					setWMObjectProperty(WM_CURRENT_PLATFORM, WM_CHARGING, "False");
				}
	
				// wait for property to be changed
				try {
					Thread.sleep(500);
				} catch (Exception e) {
					e.printStackTrace();
				}
	
				// raise remote action
				ArrayList<String> parameters = new ArrayList<String>();
				parameters.add("SELF");
				parameters.add(chargingStatus);
				raiseMindAction(USERNAME_UNKNOWN, "setCharging", parameters);
	
			} else if (action.equals("sendDocking")) {
				// send to dock in docking station
	
				// raise remote action
				ArrayList<String> parameters = new ArrayList<String>();
				parameters.add("SELF");
				raiseMindAction(USERNAME_UNKNOWN, "sendDocking", parameters);
	
			} else if (action.equals("sendUndocking")) {
				// send to undock from docking station
	
				// raise remote action
				ArrayList<String> parameters = new ArrayList<String>();
				parameters.add("SELF");
				raiseMindAction(USERNAME_UNKNOWN, "sendUndocking", parameters);
			}
		} 
		
		// start html response
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println(getHeader());
		out.println(getLoginForm());

		// process page
		if (loginUser.equals("")){
			out.println(getLoginRequestPage());
		} else {
			if (page == null) {
				out.println(getLoginRequestPage());
				/*if (remarkTimeStamp > 0)
				{
					out.println(getDisplayRemarkPage());
				}
				else
				{
					out.println(getStartPage());
					//out.println(getLoginRequestPage());
				}*/
			} else if (page.equals("start")) {
				out.println(getStartPage());
			} else if (page.equals("message")) {
				out.println(getMessagePage());
			} else if (page.equals("messageLeft")) {
				String messageFromRealname = request.getParameter("messageFromRealname");
				String messageToUsername = request.getParameter("messageToUsername");
				String messageToRealname = userData.getUserRealname(messageToUsername);
				String messageText = request.getParameter("messageText");
				out.println(getMessageLeftPage(messageFromRealname, messageToRealname, messageText));
			} else if (page.equals("askIsAround")) {
				out.println(getUsernamesPage("askIsAround"));
			} else if (page.equals("askWhereabout")) {
				out.println(getUsernamesPage("askWhereabout"));
			} else if (page.equals("informWhereabout")) {
				out.println(getWhereaboutPage());
			} else if (page.equals("displayRemark")) {
				out.println(getDisplayRemarkPage());
			} else if (page.equals("reply")) {
				String reply = request.getParameter("answer");
				out.println(getReplyPage(reply));
			}else if (page.equals("charging")) {
				out.println(getChargingPage());
			} else if (page.equals("docking")) {
				out.println(getDockingPage());
			}
		}
		out.println(getFooter());
		out.close();
	}
	
	public void setRemark(String username, String remark, String remarkText) {
		this.remarkTimeStamp = System.currentTimeMillis();
		/*if (!(loginUser.equals("")) && (previousUser.equals("")))
			previousUser = loginUser;
		this.loginUser = username;*/
		this.remarkUser = username;
		this.remark = remark;
		this.remarkText = remarkText;
		userData.setMadeRemark(username,"True");
		
		System.out.println("Remark timer set ");
		timer.cancel();
		timer.purge();
		timer = new Timer();
		timer.schedule(new TimeoutTask(interfaceCompetency, WM_CURRENT_PLATFORM, WM_INTERFACE_INTERACTION), NON_INTERACTION_TIMEOUT);
		setWMObjectProperty(WM_CURRENT_PLATFORM, WM_INTERFACE_INTERACTION, "True");
	}

	// reset all values in case the previous user didn't log out
	public void resetAll(){
		System.out.println("Reset all!");
		timer.cancel();
		timer.purge();
		loginUser = "";
		remarkTimeStamp = 0;
		remarkUser = "";
		remark = "";
		remarkText = "";
	}
	
	private String getHeader() {
		String html = "";
		html += "<html>\n";
		html += "  <head>\n";
		//html += "<META HTTP-EQUIV=\"refresh\" CONTENT=\"8\">";
		html += "    <title>Teambuddy Information Sharing Interface</title>\n";
		// disable caching
		//html += "    <meta http-equiv=\"Pragma\" content=\"no-cache\">";
		//html += "    <meta http-equiv=\"Expires\" content=\"-1\">";
		// web app works fine but scaling is incorrect
		//html += "    <meta name=\"apple-mobile-web-app-capable\" content=\"yes\" />\n";
		//html += "    <meta name=\"apple-mobile-web-app-status-bar-style\" content=\"black\" />\n";
		//html += "    <meta name=\"viewport\" content=\"user-scalable=no, width=device-width\" />\n";
		html += "    <style type=\"text/css\">\n";
		html += "      * { font-family: sans-serif; font-size: 20pt; }\n";
		html += "      body { color: #FFFFFF; background-color: #000000; }\n";
		html += "      .small { font-size: 12pt; }\n";
		html += "    </style>\n";
		html += "  </head>\n";
		html += "  <body>\n";
		return html;
	}
	
	private String getLoginForm() {
		String html = "";
		html += "<table>\n";
		html += "  <tr>\n";
		html += "    <td valign=\"bottom\">\n";
		
		if (!loginUser.equals("")) {
			html += "      <form action=\"\" method=\"POST\">\n";
			if (!loginUser.equals(USERNAME_UNKNOWN)){
				html += "        <a class=\"small\">You are " + loginUser + " (" + userData.getUserRealname(loginUser).replace("_", " ") + ")</a>\n";
				html += "        <input class=\"small\" name=\"submit\" type=\"submit\" value=\"Back\" />\n";
				html += "        <input class=\"small\" name=\"action\" type=\"hidden\" value=\"logout\" />\n";
				html += "        <input class=\"small\" name=\"page\" type=\"hidden\" value=\"start\" />\n";
				html += "      </form>\n";
			}
		}
		html += "    </td>\n";
		html += "  </tr>\n";
		html += "</table>\n";
		
		return html;
	}

	private String getFooter() {
		String html = "";
		html += "</html>\n";
		return html;
	}

	private String getLoginRequestPage() {
		String html = "";
		
		html += "<p>In order to interact with me, please click on your name!</p>\n";
		html += "<table>\n";
		html += "  <tr>\n";
	
		for (int i = 0; i < userData.getUsers().size(); i++) {	
			if (i == userData.getUsers().size()/2){
				html += "  </tr>\n";
				html += "  <tr>\n";
			}
			String user = userData.getUsers().get(i);
			html += "    <td>\n";	
			html += "  <form action=\"\" method=\"POST\">\n";
			html += "    <input name=\"submit\" type=\"submit\" value=\"" + userData.getUserRealname(user).replace("_", " ") + "\" />\n";
			html += "    <input name=\"action\" type=\"hidden\" value=\"login\" />\n";
			html += "    <input name=\"page\" type=\"hidden\" value=\"start\" />\n";
			html += "    <input name=\"loginUsername\" type=\"hidden\" value=\"" + userData.getUsername(user) + "\" /></td>\n";
			html += "    <input name=\"loginPassword\" type=\"hidden\" value=\"" + userData.getPassword(user) + "\" /></td>\n";
			html += "  </form>\n";
			html += "    </td>\n";
		}		
		html += "  </tr>\n";
		html += "</table>\n";	
		
		html += "<p>\n";
		html += "  <form action=\"\" method=\"POST\">\n";
		html += "    If you are guest, please click here: \n";
		html += "    <input name=\"submit\" type=\"submit\" value=\"Guest Login\" />\n";
		html += "    <input name=\"action\" type=\"hidden\" value=\"login\" />\n";
		html += "    <input name=\"page\" type=\"hidden\" value=\"start\" />\n";
		html += "    <input name=\"loginUsername\" type=\"hidden\" value=\"" + userData.getGuestUser() + "\" /></td>\n";
		html += "    <input name=\"loginPassword\" type=\"hidden\" value=\"" + userData.getPassword(userData.getGuestUser()) + "\" /></td>\n";
		html += "  </form>\n";
		html += "</p>\n";
	
		if (ENABLE_SEND_HOME) {
			html += "<p>\n";
			html += "  <form action=\"\" method=\"POST\">\n";
			html += "    For sending the me back, please click here: \n";
			html += "    <input name=\"submit\" type=\"submit\" value=\"Send Home\" />\n";
			html += "    <input name=\"action\" type=\"hidden\" value=\"sendHome\" />\n";
			html += "    <input name=\"page\" type=\"hidden\" value=\"start\" />\n";
			html += "  </form>\n";
			html += "</p>\n";
		}

		return html;
	}

	private String getStartPage() {
		String html = "";

		html += "<p>What do you want to do?</p>\n";

		html += "<table>\n";
		html += "  <tr>\n";

		html += "    <td valign=\"top\">\n";

		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input name=\"submit\" type=\"submit\" value=\"Leave a message\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"message\" />\n";
		html += "      </form>\n";
		
		html += "    </td>\n";

		html += "    <td valign=\"top\">\n";

		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input name=\"submit\" type=\"submit\" value=\"Have you seen ...\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"askIsAround\" />\n";
		html += "      </form>\n";

		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input name=\"submit\" type=\"submit\" value=\"Where is ...\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"askWhereabout\" />\n";
		html += "      </form>\n";
		
		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input name=\"submit\" type=\"submit\" value=\"I will be at ...\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"informWhereabout\" />\n";
		html += "      </form>\n";
		
		html += "    </td>\n";

		html += "    <td valign=\"top\">\n";

		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input name=\"submit\" type=\"submit\" value=\"Set charging status\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"charging\" />\n";
		html += "      </form>\n";

		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input name=\"submit\" type=\"submit\" value=\"Send docking/undocking\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"docking\" />\n";
		html += "      </form>\n";

		html += "    </td>\n";

		html += "  </tr>\n";
		html += "</table>\n";
		
		html += "	<p>To reply to my remark, please click here: </p> ";
		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input name=\"submit\" type=\"submit\" value=\"Reply Me\" />\n";
		//html += "        <input name=\"action\" type=\"hidden\" value=\"interact\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"displayRemark\" />\n";
		html += "      </form>\n";

		return html;
	}

	private String getMessagePage() {
		String html = "";

		html += "<form action=\"\" method=\"POST\">\n";
		html += "<table>\n";
		html += "  <tr>\n";
		html += "    <td>from:</td>\n";
		if (loginUser == userData.getGuestUser()) {
			html += "    <td>\n";
			html += "      <input name=\"messageFromRealname\" type=\"text\" />\n";
		} else {
			html += "    <td>\n";
			html += "      <input name=\"messageFromRealname\" type=\"text\" value=\"" + userData.getUserRealname(loginUser).replace("_", " ") + "\" readonly=\"readonly\" />\n";
		}
		html += "      <input name=\"messageFromUsername\" type=\"hidden\" value=\"" + loginUser + "\" />\n";
		html += "    </td>\n";
		html += "  </tr>\n";
		html += "  <tr>\n";
		html += "    <td>to:</td>\n";
		html += "    <td>\n";
		html += "      <select name=\"messageToUsername\">\n";
		for (String user : userData.getUsers()) {
			if (!user.equals(loginUser)) {
				String username = userData.getUsername(user);
				String realname = userData.getUserRealname(user);
				html += "        <option value=\"" + username + "\">" + realname.replace("_", " ") + "</option>\n";
			}
		}
		html += "      </select>\n";
		html += "    </td>\n";
		html += "  </tr>\n";
		html += "  <tr>\n";
		html += "    <td valign=\"top\">text:</td>\n";
		html += "    <td>\n";
		html += "      <textarea name=\"messageText\" rows=\"5\" cols=\"40\"></textarea>\n";
		html += "    </td>\n";
		html += "  </tr>\n";
		html += "  <tr>\n";
		html += "    <td></td>\n";
		html += "    <td>\n";
		html += "      <input name=\"submit\" type=\"submit\" value=\"Leave message\" />\n";
		html += "      <input name=\"action\" type=\"hidden\" value=\"leaveMessage\" />\n";
		html += "      <input name=\"page\" type=\"hidden\" value=\"messageLeft\" />\n";
		html += "    </td>\n";
		html += "  </tr>\n";
		html += "</table>\n";
		html += "</form>\n";

		return html;
	}

	private String getMessageLeftPage(String messageFromRealname, String messageToRealname, String messageText) {
		String html = "";

		html += "<p>Message from <i>" + messageFromRealname + "</i> to <i>" + messageToRealname + "</i>:</p>\n";
		//html += "<p>" + messageText + "</p>\n";
		html += "<p>" + messageText.replaceAll("\n", "<br/>") + "</p>\n";
		html += "<form action=\"\" method=\"POST\">\n";
		html += "  <input name=\"submit\" type=\"submit\" value=\"Back\" />\n";
		html += "  <input name=\"page\" type=\"hidden\" value=\"message\" />\n";
		html += "</form>\n";

		return html;
	}

	private String getUsernamesPage(String question) {
		String html = "";

		html += "<p>Who are you looking for?</p>\n";

		html += "<table>\n";
		html += "  <tr>\n";
		html += "    <td valign=\"top\">\n";

		for (int i = 0; i < userData.getUsers().size(); i++) {
			String user = userData.getUsers().get(i);

			if (i == Math.round((double) userData.getUsers().size() / 2)) {
				html += "    </td>\n";
				html += "    <td valign=\"top\">\n";
			}

			html += "      <form action=\"\" method=\"POST\">\n";
			html += "        <input name=\"submit\" type=\"submit\" value=\"" + userData.getUserRealname(user).replace("_", " ") + "\" />\n";
			html += "        <input name=\"action\" type=\"hidden\" value=\"" + question + "\" />\n";
			html += "        <input name=\"page\" type=\"hidden\" value=\"start\" />\n";
			html += "        <input name=\"username\" type=\"hidden\" value=\"" + userData.getUsername(user) + "\" />\n";
			html += "      </form>\n";
		}

		html += "    </td>\n";
		html += "  </tr>\n";
		html += "</table>\n";

		return html;
	}

	private String getWhereaboutPage() {
		String html = "";
		ArrayList<String> whereabout = new ArrayList<String>();
		whereabout.add("A meeting");
		whereabout.add("Coffee");
		whereabout.add("Others");

		html += "<p>Where will you be?</p>\n";

		html += "<table>\n";
		html += "  <tr>\n";

		html += "    <td valign=\"top\">\n";

		for (String at: whereabout) {
			html += "      <form action=\"\" method=\"POST\">\n";
			html += "        <input name=\"submit\" type=\"submit\" value=\"" + at  + "\" />\n";
			html += "        <input name=\"page\" type=\"hidden\" value=\"start\" />\n";
			html += "        <input name=\"action\" type=\"hidden\" value=\"informWhereabout\" />\n";
			html += "        <input name=\"whereabout\" type=\"hidden\" value=\"" + at  + "\" />\n";
			html += "      </form>\n";
		}
		
		html += "    </td>\n";

		html += "  </tr>\n";
		html += "</table>\n";

		return html;
	}
	
	private String getDisplayRemarkPage() {
		String html = "";
	
		/*if (System.currentTimeMillis() - remarkTimeStamp > 5 * 60 * 1000){
			html += "<p>I have no pending question for you.</p>\n";
			html += "      <form action=\"\" method=\"POST\">\n";
			html += "        <input name=\"submit\" type=\"submit\" value=\"Back\" />\n";
			html += "        <input name=\"action\" type=\"hidden\" value=\"endInteract\" />\n";
			html += "        <input name=\"page\" type=\"hidden\" value=\"start\" />\n";
			html += "      </form>\n";
		} else {*/
		
		// Displaying the remark if there is one
		if (remarkTimeStamp > 0 && remarkUser.equals(loginUser))
			html += "<p>Hi " + loginUser + ", " + remarkText + "</p>\n";
		else
			html += "<p>Hi " + loginUser + ", what would you like to tell me? </p>\n";
		
		html += "<table>\n";
		html += "  <tr>\n";

		html += "    <td valign=\"top\">\n";
		
		// User's reply
		html += "      <form action=\"\" method=\"POST\">\n";

		html += "  <tr>\n";
		html += "    <td valign=\"top\">Reply:</td>\n";
		html += "    <td>\n";
		html += "      <textarea name=\"answer\" rows=\"5\" cols=\"40\"></textarea>\n";
		html += "    </td>\n";
		html += "  </tr>\n";
		html += "  <tr>\n";
		html += "    <td></td>\n";
		html += "    <td>\n";
		html += "      <input name=\"submit\" type=\"submit\" value=\"Submit\" />\n";
		html += "      <input name=\"action\" type=\"hidden\" value=\"reply\" />\n";
		html += "      <input name=\"page\" type=\"hidden\" value=\"start\" />\n";
		html += "    </td>\n";
		html += "  </tr>\n";
		html += "      </form>\n";
		
		html += "    </td>\n";

		html += "  </tr>\n";
		html += "</table>\n";
	
		return html;
	}
	
	private String getReplyPage(String reply) {
		String html = "";
	
		html += "<p>You said: </p>\n";
		html += "<p>" + reply + "</p>\n";
		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input name=\"submit\" type=\"submit\" value=\"Back\" />\n";
		html += "        <input name=\"action\" type=\"hidden\" value=\"logout\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"start\" />\n";
		html += "      </form>\n";
		loginUser = "";
		return html;
	}
	
	private String getChargingPage() {
		String html = "";

		Object chargingObject = getWMObjectProperty(WM_CURRENT_PLATFORM, WM_CHARGING);
		if (chargingObject != null && chargingObject instanceof String) {
			String charging = (String) chargingObject;
			if (charging.equals("True")) {
				html += "<p>The Teambuddy is currently set to <b>Charging</b></p>\n";
			} else if (charging.equals("False")) {
				html += "<p>The Teambuddy is currently set to <b>Not charging</b></p>\n";
			}
		} else {
			html += "<p>The current status could not be determined.</p>\n";
		}

		html += "<form action=\"\" method=\"POST\">\n";
		html += "  <input name=\"submit\" type=\"submit\" value=\"Check again\" />\n";
		html += "  <input name=\"page\" type=\"hidden\" value=\"charging\" />\n";
		html += "</form>\n";

		html += "<p>Please select the correct charging status:</p>\n";
		html += "<table>\n";
		html += "  <tr>\n";
		html += "    <td>\n";
		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input name=\"submit\" type=\"submit\" value=\"Charging\" />\n";
		html += "        <input name=\"action\" type=\"hidden\" value=\"charging\" />\n";
		html += "        <input name=\"chargingStatus\" type=\"hidden\" value=\"true\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"charging\" />\n";
		html += "      </form>\n";
		html += "    </td>\n";
		html += "    <td>\n";
		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input name=\"submit\" type=\"submit\" value=\"Not charging\" />\n";
		html += "        <input name=\"action\" type=\"hidden\" value=\"charging\" />\n";
		html += "        <input name=\"chargingStatus\" type=\"hidden\" value=\"false\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"charging\" />\n";
		html += "      </form>\n";
		html += "    </td>\n";
		html += "  </tr>\n";
		html += "</table>\n";
		html += "<p class=\"small\">\n";
		html += "  The charging status should only be set here if the robot fails to use the docking station for charing and needs to be charged with the cable.<br/>\n";
		html += "  For charging please first select <i class=\"small\">Charging</i> and then plug the cable.<br/>\n";
		html += "  For releasing please first unplug the cable and then select <i class=\"small\">Not charging</i>.<br/>\n";
		html += "</p>\n";

		html += "<form action=\"\" method=\"POST\">\n";
		html += "  <input name=\"submit\" type=\"submit\" value=\"Back\" />\n";
		html += "  <input name=\"page\" type=\"hidden\" value=\"start\" />\n";
		html += "</form>\n";

		return html;
	}

	private String getDockingPage() {
		String html = "";

		Object locationObject = getWMObjectProperty(WM_CURRENT_PLATFORM, WM_LOCATION);
		if (locationObject != null && locationObject instanceof String) {
			String location = (String) locationObject;
			html += "<p>The current location is <b>" + location + "</b></p>\n";
		}

		html += "<form action=\"\" method=\"POST\">\n";
		html += "  <input name=\"submit\" type=\"submit\" value=\"Check again\" />\n";
		html += "  <input name=\"page\" type=\"hidden\" value=\"docking\" />\n";
		html += "</form>\n";

		html += "<table>\n";
		html += "  <tr>\n";
		html += "    <td>\n";
		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input name=\"submit\" type=\"submit\" value=\"Send docking\" />\n";
		html += "        <input name=\"action\" type=\"hidden\" value=\"sendDocking\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"docking\" />\n";
		html += "      </form>\n";
		html += "    </td>\n";
		html += "  </tr>\n";
		html += "  <tr>\n";
		html += "    <td>\n";
		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input name=\"submit\" type=\"submit\" value=\"Send undocking\" />\n";
		html += "        <input name=\"action\" type=\"hidden\" value=\"sendUndocking\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"docking\" />\n";
		html += "      </form>\n";
		html += "    </td>\n";
		html += "  </tr>\n";
		html += "</table>\n";
		html += "<p class=\"small\">\n";
		html += "  Usually no <i class=\"small\">manual docking/undocking</i> is required as the robot should charge itself automatically.<br/>\n";
		html += "  Manual undocking will only work if a certain <i class=\"small\">minimum voltage</i> has been reached.<br/>\n";
		html += "</p>\n";

		html += "<form action=\"\" method=\"POST\">\n";
		html += "  <input name=\"submit\" type=\"submit\" value=\"Back\" />\n";
		html += "  <input name=\"page\" type=\"hidden\" value=\"start\" />\n";
		html += "</form>\n";

		return html;
	}

	private void leaveMessage(String fromUsername, String toUsername, String messageText) {
		if (interfaceCompetency != null) {

			// obtain an id for the message
			messageId++;
			String id = new Integer(messageId).toString();

			// write message to BlackBoard
			setBBObjectProperty(BB_MESSAGE_TEXTS, id, messageText);

			// write sender to WorldModel
			setWMObjectProperty(WM_MESSAGE_SENDERS, id, fromUsername);

			// write recipient to WorldModel
			setWMObjectProperty(WM_MESSAGE_RECIPIENTS, id, toUsername);

			// raise remote action
			ArrayList<String> parameters = new ArrayList<String>();
			parameters.add("SELF");
			parameters.add(toUsername);
			parameters.add(id);
			raiseMindAction(fromUsername, "leaveMessage", parameters);
		}
	}

	private void raiseMindAction(String subject, String action, ArrayList<String> parameters) {
		if (interfaceCompetency != null) {
			MindAction ma = new MindAction(subject, action, parameters);
			interfaceCompetency.raise(new EventRemoteAction(ma));
		}
	}

	private void setBBProperty(String propertyName, Object propertyValue) {
		if (interfaceCompetency != null) {
			interfaceCompetency.getArchitecture().getBlackBoard().requestSetProperty(propertyName, propertyValue);
		}
	}

	private void setBBObjectProperty(String objectName, String propertyName, Object propertyValue) {
		if (interfaceCompetency != null) {
			BlackBoard bb = interfaceCompetency.getArchitecture().getBlackBoard();
			if (bb.hasSubContainer(objectName))
				bb.getSubContainer(objectName).requestSetProperty(propertyName, propertyValue);
			else {
				HashMap<String, Object> properties = new HashMap<String, Object>();
				properties.put(propertyName, propertyValue);
				bb.requestAddSubContainer(objectName, objectName, properties);
			}
		}
	}

	private void setWMObjectProperty(String objectName, String propertyName, Object propertyValue) {
		if (interfaceCompetency != null) {
			WorldModel wm = interfaceCompetency.getArchitecture().getWorldModel();
			if (wm.hasObject(objectName)) {
				wm.getObject(objectName).requestSetProperty(propertyName, propertyValue);
			} else {
				HashMap<String, Object> properties = new HashMap<String, Object>();
				properties.put(propertyName, propertyValue);
				wm.requestAddObject(objectName, properties);
			}
		}
	}

	private Object getWMObjectProperty(String objectName, String propertyName) {
		if (interfaceCompetency != null) {
			WorldModel wm = interfaceCompetency.getArchitecture().getWorldModel();
			if (wm.hasObject(objectName)) {
				return wm.getObject(objectName).getPropertyValue(propertyName);
			}
		}
		return null;
	}

	private void removeWMObjectProperty(String objectName, String propertyName) {
		if (interfaceCompetency != null) {
			WorldModel wm = interfaceCompetency.getArchitecture().getWorldModel();
			if (wm.hasObject(objectName)) {
				wm.getObject(objectName).requestRemoveProperty(propertyName);
			}
		}
	}

	
}