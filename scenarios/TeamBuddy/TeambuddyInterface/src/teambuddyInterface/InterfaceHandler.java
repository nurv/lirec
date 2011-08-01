package teambuddyInterface;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

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

	public static final String MSG_BLACKBOARD_CONTAINER = "messagesToDeliver";

	private static int msgID = 0;

	public static final String INTERFACE_UTTERANCE = "INTERFACE_UTTERANCE";

	private static String CURRENT_PLATFORM = "CurrentPlatform";

	private static String CHARGING = "charging";

	private static String INTERFACE_REQUEST_TIME = "INTERFACE_REQUEST_TIME";

	private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	private final static String XML_FILENAME = "UserData.xml";

	private XMLAccess xmlAccess;

	private UserData userData;

	private User loginUser;

	private ArrayList<String> talkPresets;

	private ArrayList<String> talkPresetSymbols;

	private final static boolean ENABLE_QUICK_TALK = false;

	private final static boolean LOGIN_USERNAME_SELECT = true;

	public InterfaceHandler(InterfaceCompetency interfaceCompetency) {
		this.interfaceCompetency = interfaceCompetency;

		xmlAccess = new XMLAccess();
		userData = xmlAccess.loadXML(XML_FILENAME);

		talkPresets = new ArrayList<String>();
		talkPresetSymbols = new ArrayList<String>();

		talkPresets.add("I am fine.");
		talkPresetSymbols.add("IAmFine");

		talkPresets.add("I feel bad.");
		talkPresetSymbols.add("IFeelBad");

		talkPresets.add("I am glad to see you.");
		talkPresetSymbols.add("GladToSeeYou");

		talkPresets.add("Please leave me alone.");
		talkPresetSymbols.add("LeaveMeAlone");
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		// write current time to blackboard
		setBBProperty(INTERFACE_REQUEST_TIME, Calendar.getInstance().getTimeInMillis());

		// process action
		String action = request.getParameter("action");

		if (action == null) {
			// do nothing

		} else if (action.equals("login")) {
			// login

			String loginUsername = request.getParameter("loginUsername");
			String loginPassword = request.getParameter("loginPassword");
			if (userData.getGuestUser().getUsername().equals(loginUsername)) {
				// guest login
				if (userData.getGuestUser().getPassword().equals(loginPassword)) {
					loginUser = userData.getGuestUser();
				}
			} else {
				// user login
				for (User user : userData.getUsers()) {
					if (user.getUsername().equals(loginUsername)) {
						if (user.getPassword().equals(loginPassword)) {
							loginUser = user;
						}
					}
				}
			}
			if (loginUser != null) {
				loginUser.setTimeLastLogin(Calendar.getInstance().getTimeInMillis());
				xmlAccess.saveXML(userData, XML_FILENAME);
				ArrayList<String> parameters = new ArrayList<String>();
				parameters.add("SELF");
				raiseMindAction(loginUser.getUsername(), "login", parameters);
			}

		} else if (action.equals("logout")) {
			// logout

			// force wait to stop
			setBBProperty(INTERFACE_REQUEST_TIME, 0);
			loginUser.setTimeLastLogout(Calendar.getInstance().getTimeInMillis());
			xmlAccess.saveXML(userData, XML_FILENAME);
			ArrayList<String> parameters = new ArrayList<String>();
			parameters.add("SELF");
			raiseMindAction(loginUser.getUsername(), "logout", parameters);
			loginUser = null;

		} else if (action.equals("talk")) {
			// talk to Teambuddy

			//String talkUsername = request.getParameter("talkUsername");
			String talkText = request.getParameter("talkText");
			String talkSymbol = request.getParameter("talkSymbol");
			talkReceived(talkText, talkSymbol);

		} else if (action.equals("leaveMessage")) {
			// leave a message

			//String messageFromRealname = request.getParameter("messageFromRealname");
			String messageFromUsername = request.getParameter("messageFromUsername");
			String messageToUsername = request.getParameter("messageToUsername");
			String messageText = request.getParameter("messageText");
			if (messageText == null || messageText.trim().equals("")) {
				ArrayList<String> parameters = new ArrayList<String>();
				parameters.add(messageToUsername);
				raiseMindAction(messageFromUsername, "leaveEmptyMessage", parameters);
			} else {
				if (messageFromUsername.equals(userData.getGuestUser().getUsername())) {
					// problem with FAtiMA event when name contains spaces
					//msgReceived(messageFromRealname, messageToUsername, messageText);
					msgReceived(messageFromUsername, messageToUsername, messageText);
				} else {
					msgReceived(messageFromUsername, messageToUsername, messageText);
				}
			}

		} else if (action.equals("requestInfo")) {
			// request information

			String infoUsername = request.getParameter("infoUsername");
			String infoTypename = request.getParameter("infoTypename");
			LinkedList<InformationItem> informationItems = userData.requestInformationItems(infoUsername, infoTypename, loginUser.getUsername());
			String utterance = "";
			if (informationItems.size() > 0) {
				utterance = "'" + userData.getTypeRealname(infoTypename) + "' for '" + userData.getUserRealname(infoUsername) + "': ";
				for (InformationItem informationItem : informationItems) {
					if (informationItem != informationItems.get(0)) {
						utterance += ". ";
					}
					utterance += informationItem.getContent();
				}
			} else {
				if (infoUsername.equals(loginUser.getUsername())) {
					utterance = "I have not remembered any information of type '" + userData.getTypeRealname(infoTypename) + "' for you.";
				} else {
					utterance = "I am not authorised to give this information.";
				}
			}
			// write utterance to blackboard
			setBBProperty(INTERFACE_UTTERANCE, utterance);
			// raise remote action
			ArrayList<String> parameters = new ArrayList<String>();
			parameters.add("SELF");
			parameters.add(infoUsername);
			raiseMindAction(loginUser.getUsername(), "requestInformation", parameters);
			xmlAccess.saveXML(userData, XML_FILENAME);

		} else if (action.equals("provideInfo")) {
			// provide information

			String infoUsername = request.getParameter("infoUsername");
			String infoTypename = request.getParameter("infoTypename");
			String infoContent = request.getParameter("infoContent");
			String[] infoAuthorisedRoles = request.getParameterValues("infoAuthorisedRoles");
			String[] infoAuthorisedUsers = request.getParameterValues("infoAuthorisedUsers");
			String utterance = "";
			// check if content is empty
			if (infoContent == null || infoContent.trim().equals("")) {
				utterance = "This information did not contain any content.";
			} else {
				// try to add the information item
				boolean added = userData.provideInformationItem(infoUsername, infoTypename, infoContent, infoAuthorisedRoles, infoAuthorisedUsers);
				if (added) {
					utterance = "I have remembered this information of type '" + userData.getTypeRealname(infoTypename) + "'.";
					xmlAccess.saveXML(userData, XML_FILENAME);
				} else {
					utterance = "I could not remember this information.";
				}
			}
			// write utterance to blackboard
			setBBProperty(INTERFACE_UTTERANCE, utterance);
			// raise remote action
			ArrayList<String> parameters = new ArrayList<String>();
			parameters.add("SELF");
			parameters.add(infoUsername);
			raiseMindAction(loginUser.getUsername(), "provideInformation", parameters);

		} else if (action.equals("deleteInfo")) {
			// delete information

			String infoUsername = request.getParameter("infoUsername");
			String infoTypename = request.getParameter("infoTypename");
			String[] infoIds = request.getParameterValues("infoId");
			String utterance = "";
			if (infoIds != null) {
				LinkedList<Long> deleteIds = new LinkedList<Long>();
				for (String infoId : infoIds) {
					deleteIds.add(Long.valueOf(infoId));
				}
				int deletedCount = userData.deleteInformationItems(loginUser.getUsername(), infoUsername, deleteIds);
				if (deletedCount > 0) {
					utterance = "I have forgotten " + deletedCount + " items of type '" + userData.getTypeRealname(infoTypename) + "'.";
					xmlAccess.saveXML(userData, XML_FILENAME);
				} else {
					utterance = "I have not forgotten any information.";
				}
			} else {
				utterance = "I have not forgotten any information.";
			}
			// write utterance to blackboard
			setBBProperty(INTERFACE_UTTERANCE, utterance);
			// raise remote action
			ArrayList<String> parameters = new ArrayList<String>();
			parameters.add("SELF");
			parameters.add(infoUsername);
			raiseMindAction(loginUser.getUsername(), "deleteInformation", parameters);

		} else if (action.equals("charging")) {
			// set charging status

			String chargingStatus = request.getParameter("chargingStatus");
			if (chargingStatus.equals("true")) {
				setWMObjectProperty(CURRENT_PLATFORM, CHARGING, "True");
			} else if (chargingStatus.equals("false")) {
				setWMObjectProperty(CURRENT_PLATFORM, CHARGING, "False");
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
			raiseMindAction(loginUser.getUsername(), "setCharging", parameters);
		}

		// start html response
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println(getHeader());
		out.println(getLoginForm());

		// process page
		if (loginUser == null) {
			out.println(getLoginRequestPage());
		} else {
			String page = request.getParameter("page");
			if (page == null) {
				out.println(getStartPage());
			} else if (page.equals("start")) {
				out.println(getStartPage());
			} else if (page.equals("talk")) {
				out.println(getTalkPage());
			} else if (page.equals("talked")) {
				String talkText = request.getParameter("talkText");
				out.println(getTalkedPage(talkText));
			} else if (page.equals("message")) {
				out.println(getMessagePage());
			} else if (page.equals("messageLeft")) {
				String messageFromRealname = request.getParameter("messageFromRealname");
				String messageToUsername = request.getParameter("messageToUsername");
				String messageToRealname = userData.getUserRealname(messageToUsername);
				String messageText = request.getParameter("messageText");
				out.println(getMessageLeftPage(messageFromRealname, messageToRealname, messageText));
			} else if (page.equals("requestUser")) {
				out.println(getRequestUserPage());
			} else if (page.equals("requestType")) {
				String infoUsername = request.getParameter("infoUsername");
				out.println(getRequestTypePage(infoUsername));
			} else if (page.equals("requestInfo")) {
				String infoUsername = request.getParameter("infoUsername");
				String infoTypename = request.getParameter("infoTypename");
				out.println(getRequestInfoPage(infoUsername, infoTypename));
			} else if (page.equals("provide")) {
				out.println(getProvidePage());
			} else if (page.equals("provideInfo")) {
				String infoUsername = request.getParameter("infoUsername");
				String infoTypename = request.getParameter("infoTypename");
				out.println(getProvideInfoPage(infoUsername, infoTypename));
			} else if (page.equals("providedInfo")) {
				String infoUsername = request.getParameter("infoUsername");
				String infoTypename = request.getParameter("infoTypename");
				out.println(getProvidedInfoPage(infoUsername, infoTypename));
			} else if (page.equals("delete")) {
				out.println(getDeletePage());
			} else if (page.equals("deleteInfo")) {
				String infoUsername = request.getParameter("infoUsername");
				String infoTypename = request.getParameter("infoTypename");
				out.println(getDeleteInfoPage(infoUsername, infoTypename));
			} else if (page.equals("deletedInfo")) {
				String infoUsername = request.getParameter("infoUsername");
				String infoTypename = request.getParameter("infoTypename");
				out.println(getDeletedInfoPage(infoUsername, infoTypename));
			} else if (page.equals("history")) {
				out.println(getHistoryPage());
			} else if (page.equals("historyInfo")) {
				String infoUsername = request.getParameter("infoUsername");
				String infoTypename = request.getParameter("infoTypename");
				out.println(getHistoryInfoPage(infoUsername, infoTypename));
			} else if (page.equals("charging")) {
				out.println(getChargingPage());
			}
		}

		out.println(getFooter());
		out.close();
	}

	private String getHeader() {
		String html = "";
		html += "<html>\n";
		html += "  <head>\n";
		html += "    <title>Teambuddy Information Sharing Interface</title>\n";
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
		if (loginUser == null) {
			html += "      <form action=\"\" method=\"POST\">\n";

			if (LOGIN_USERNAME_SELECT) {
				html += "        <a class=\"small\">Username: </a>\n";
				html += "        <select class=\"small\" name=\"loginUsername\">\n";
				for (User user : userData.getUsers()) {
					html += "          <option class=\"small\" value=\"" + user.getUsername() + "\">" + user.getUsername() + "</option>\n";
				}
				html += "        </select>\n";
			} else {
				html += "        <a class=\"small\">Username: </a><input class=\"small\" name=\"loginUsername\" type=\"text\" />\n";
			}

			html += "        <a class=\"small\">Password: </a><input class=\"small\" name=\"loginPassword\" type=\"password\" />\n";
			html += "        <input class=\"small\" name=\"submit\" type=\"submit\" value=\"Login\" />\n";
			html += "        <input class=\"small\" name=\"action\" type=\"hidden\" value=\"login\" />\n";
			html += "        <input class=\"small\" name=\"page\" type=\"hidden\" value=\"start\" />\n";
			html += "      </form>\n";
		} else {
			html += "      <form action=\"\" method=\"POST\">\n";
			html += "        <a class=\"small\">logged in as: " + loginUser.getRealname() + " (" + userData.getRoleRealname(loginUser.getRolename()) + ")</a>\n";
			html += "        <input class=\"small\" name=\"submit\" type=\"submit\" value=\"Logout\" />\n";
			html += "        <input class=\"small\" name=\"action\" type=\"hidden\" value=\"logout\" />\n";
			html += "        <input class=\"small\" name=\"page\" type=\"hidden\" value=\"start\" />\n";
			html += "      </form>\n";
		}
		html += "    </td>\n";
		html += "    <td valign=\"bottom\">\n";
		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input class=\"small\" name=\"submit\" type=\"submit\" value=\"Go to start page\" />\n";
		html += "        <input class=\"small\" name=\"page\" type=\"hidden\" value=\"start\" />\n";
		html += "      </form>\n";
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

		html += "<p>Welcome to the Teambuddy iPad interface!</p>\n";
		html += "<p>In order to interact with the Teambuddy, please log in with your username and password!</p>\n";

		html += "<p>\n";
		html += "  <form action=\"\" method=\"POST\">\n";
		html += "    For a guest login, please click here: \n";
		html += "    <input name=\"submit\" type=\"submit\" value=\"Guest Login\" />\n";
		html += "    <input name=\"action\" type=\"hidden\" value=\"login\" />\n";
		html += "    <input name=\"page\" type=\"hidden\" value=\"start\" />\n";
		html += "    <input name=\"loginUsername\" type=\"hidden\" value=\"" + userData.getGuestUser().getUsername() + "\" /></td>\n";
		html += "    <input name=\"loginPassword\" type=\"hidden\" value=\"" + userData.getGuestUser().getPassword() + "\" /></td>\n";
		html += "  </form>\n";
		html += "</p>\n";

		if (ENABLE_QUICK_TALK) {
			html += "<p>For quickly talking to the Teambuddy, please click here:</p>\n";
			html += "<table>\n";
			html += "  <tr>\n";
			html += "    <td valign=\"top\">\n";

			for (int i = 0; i < talkPresets.size(); i++) {
				String talkPreset = talkPresets.get(i);
				String talkPresetSymbol = talkPresetSymbols.get(i);

				if (i == Math.round((double) talkPresets.size() / 2)) {
					html += "    </td>\n";
					html += "    <td valign=\"top\">\n";
				}

				html += "      <form action=\"\" method=\"POST\">\n";
				html += "        <input name=\"submit\" type=\"submit\" value=\"" + talkPreset + "\" />\n";
				html += "        <input name=\"talkText\" type=\"hidden\" value=\"" + talkPreset + "\">\n";
				html += "        <input name=\"talkSymbol\" type=\"hidden\" value=\"" + talkPresetSymbol + "\">\n";
				html += "        <input name=\"action\" type=\"hidden\" value=\"talk\" />\n";
				html += "        <input name=\"page\" type=\"hidden\" value=\"talked\" />\n";
				html += "      </form>\n";
			}

			html += "    </td>\n";
			html += "  </tr>\n";
			html += "</table>\n";
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
		html += "        <input name=\"submit\" type=\"submit\" value=\"Talk to Teambuddy\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"talk\" />\n";
		html += "      </form>\n";

		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input name=\"submit\" type=\"submit\" value=\"Leave a message\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"message\" />\n";
		html += "      </form>\n";

		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input name=\"submit\" type=\"submit\" value=\"Request information\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"requestUser\" />\n";
		html += "      </form>\n";

		html += "    </td>\n";

		if (loginUser != userData.getGuestUser()) {

			html += "    <td valign=\"top\">\n";

			html += "      <form action=\"\" method=\"POST\">\n";
			html += "        <input name=\"submit\" type=\"submit\" value=\"View history of requests\" />\n";
			html += "        <input name=\"page\" type=\"hidden\" value=\"history\" />\n";
			html += "      </form>\n";

			html += "      <form action=\"\" method=\"POST\">\n";
			html += "        <input name=\"submit\" type=\"submit\" value=\"Provide information\" />\n";
			html += "        <input name=\"page\" type=\"hidden\" value=\"provide\" />\n";
			html += "      </form>\n";

			html += "      <form action=\"\" method=\"POST\">\n";
			html += "        <input name=\"submit\" type=\"submit\" value=\"Delete information\" />\n";
			html += "        <input name=\"page\" type=\"hidden\" value=\"delete\" />\n";
			html += "      </form>\n";

			html += "      <form action=\"\" method=\"POST\">\n";
			html += "        <input name=\"submit\" type=\"submit\" value=\"Set charging status\" />\n";
			html += "        <input name=\"page\" type=\"hidden\" value=\"charging\" />\n";
			html += "      </form>\n";

			html += "    </td>\n";

		}

		html += "  </tr>\n";
		html += "</table>\n";

		return html;
	}

	private String getTalkPage() {
		String html = "";

		html += "<p>What do you want to tell the Teambuddy?</p>\n";

		html += "<form action=\"\" method=\"POST\">\n";
		html += "  <textarea name=\"talkText\" rows=\"2\" cols=\"40\"></textarea><br/>\n";
		html += "  <input name=\"talkSymbol\" type=\"hidden\" value=\"FreeTalk\">\n";
		html += "  <input name=\"submit\" type=\"submit\" value=\"Talk to Teambuddy\" />\n";
		html += "  <input name=\"action\" type=\"hidden\" value=\"talk\" />\n";
		html += "  <input name=\"page\" type=\"hidden\" value=\"talked\" />\n";
		html += "  <input name=\"talkUsername\" type=\"hidden\" value=\"" + loginUser.getUsername() + "\" />\n";
		html += "</form>\n";

		html += "<p>Presets:</p>\n";
		html += "<table>\n";
		html += "  <tr>\n";
		html += "    <td valign=\"top\">\n";

		for (int i = 0; i < talkPresets.size(); i++) {
			String talkPreset = talkPresets.get(i);
			String talkPresetSymbol = talkPresetSymbols.get(i);

			if (i == Math.round((double) talkPresets.size() / 2)) {
				html += "    </td>\n";
				html += "    <td valign=\"top\">\n";
			}

			html += "      <form action=\"\" method=\"POST\">\n";
			html += "        <input name=\"submit\" type=\"submit\" value=\"" + talkPreset + "\" />\n";
			html += "        <input name=\"talkText\" type=\"hidden\" value=\"" + talkPreset + "\">\n";
			html += "        <input name=\"talkSymbol\" type=\"hidden\" value=\"" + talkPresetSymbol + "\">\n";
			html += "        <input name=\"action\" type=\"hidden\" value=\"talk\" />\n";
			html += "        <input name=\"page\" type=\"hidden\" value=\"talked\" />\n";
			html += "        <input name=\"talkUsername\" type=\"hidden\" value=\"" + loginUser.getUsername() + "\" />\n";
			html += "      </form>\n";
		}

		html += "    </td>\n";
		html += "  </tr>\n";
		html += "</table>\n";

		return html;
	}

	private String getTalkedPage(String talkText) {
		String html = "";
		html += "<p>You told the Teambuddy:</p>\n";
		//html += "<p>" + talkText + "</p>\n";
		html += "<p>" + talkText.replaceAll("\n", "<br/>") + "</p>\n";
		html += "<form action=\"\" method=\"POST\">\n";
		html += "  <input name=\"submit\" type=\"submit\" value=\"Back\" />\n";
		html += "  <input name=\"page\" type=\"hidden\" value=\"talk\" />\n";
		html += "</form>\n";
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
			html += "      <input name=\"messageFromRealname\" type=\"text\" value=\"" + loginUser.getRealname() + "\" readonly=\"readonly\" />\n";
		}
		html += "      <input name=\"messageFromUsername\" type=\"hidden\" value=\"" + loginUser.getUsername() + "\" />\n";
		html += "    </td>\n";
		html += "  </tr>\n";
		html += "  <tr>\n";
		html += "    <td>to:</td>\n";
		html += "    <td>\n";
		html += "      <select name=\"messageToUsername\">\n";
		for (User user : userData.getUsers()) {
			if (user != loginUser) {
				String username = user.getUsername();
				String realname = user.getRealname();
				html += "        <option value=\"" + username + "\">" + realname + "</option>\n";
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

	private String getRequestUserPage() {
		String html = "";

		html += "<p>For which person do you want to request information?</p>\n";

		html += "<table>\n";
		html += "  <tr>\n";
		html += "    <td valign=\"top\">\n";

		for (int i = 0; i < userData.getUsers().size(); i++) {
			User user = userData.getUsers().get(i);

			if (i == Math.round((double) userData.getUsers().size() / 2)) {
				html += "    </td>\n";
				html += "    <td valign=\"top\">\n";
			}

			html += "      <form action=\"\" method=\"POST\">\n";
			html += "        <input name=\"submit\" type=\"submit\" value=\"" + user.getRealname() + "\" />\n";
			html += "        <input name=\"page\" type=\"hidden\" value=\"requestType\" />\n";
			html += "        <input name=\"infoUsername\" type=\"hidden\" value=\"" + user.getUsername() + "\" />\n";
			html += "      </form>\n";
		}

		html += "    </td>\n";
		html += "  </tr>\n";
		html += "</table>\n";

		return html;
	}

	private String getRequestTypePage(String username) {
		String html = "";

		html += "<p>Which type of information do you want to request?</p>\n";

		html += "<table>\n";
		html += "  <tr>\n";
		html += "    <td>\n";

		for (int i = 0; i < userData.getInformationTypes().size(); i++) {
			InformationType informationType = userData.getInformationTypes().get(i);

			if (i == Math.round((double) userData.getInformationTypes().size() / 2)) {
				html += "    </td>\n";
				html += "    <td valign=\"top\">\n";
			}

			html += "      <form action=\"\" method=\"POST\">\n";
			html += "        <input name=\"submit\" type=\"submit\" value=\"" + informationType.getRealname() + "\" />\n";
			html += "        <input name=\"action\" type=\"hidden\" value=\"requestInfo\" />\n";
			html += "        <input name=\"page\" type=\"hidden\" value=\"requestInfo\" />\n";
			html += "        <input name=\"infoUsername\" type=\"hidden\" value=\"" + username + "\" />\n";
			html += "        <input name=\"infoTypename\" type=\"hidden\" value=\"" + informationType.getTypename() + "\" />\n";
			html += "      </form>\n";
		}

		html += "    </td>\n";
		html += "  </tr>\n";
		html += "</table>\n";

		return html;
	}

	private String getRequestInfoPage(String username, String typename) {
		String html = "";
		html += "<p>Requesting <i>" + userData.getTypeRealname(typename) + "</i> for <i>" + userData.getUserRealname(username) + "</i>...</p>\n";
		html += "<form action=\"\" method=\"POST\">\n";
		html += "  <input name=\"submit\" type=\"submit\" value=\"Back\" />\n";
		html += "  <input name=\"page\" type=\"hidden\" value=\"requestType\" />\n";
		html += "  <input name=\"infoUsername\" type=\"hidden\" value=\"" + username + "\" />\n";
		html += "</form>\n";
		return html;
	}

	private String getProvidePage() {
		String html = "";

		html += "<p>Which type of information do you want to provide?</p>\n";

		html += "<table>\n";
		html += "  <tr>\n";
		html += "    <td>\n";

		for (int i = 0; i < userData.getInformationTypes().size(); i++) {
			InformationType informationType = userData.getInformationTypes().get(i);

			if (i == Math.round((double) userData.getInformationTypes().size() / 2)) {
				html += "    </td>\n";
				html += "    <td valign=\"top\">\n";
			}

			html += "      <form action=\"\" method=\"POST\">\n";
			html += "        <input name=\"submit\" type=\"submit\" value=\"" + informationType.getRealname() + "\" />\n";
			html += "        <input name=\"page\" type=\"hidden\" value=\"provideInfo\" />\n";
			html += "        <input name=\"infoUsername\" type=\"hidden\" value=\"" + loginUser.getUsername() + "\" />\n";
			html += "        <input name=\"infoTypename\" type=\"hidden\" value=\"" + informationType.getTypename() + "\" />\n";
			html += "      </form>\n";
		}

		html += "    </td>\n";
		html += "  </tr>\n";
		html += "</table>\n";

		return html;
	}

	private String getProvideInfoPage(String username, String typename) {
		String html = "";

		html += "<p>Which information do you want to provide?</p>\n";

		html += "<form action=\"\" method=\"POST\">\n";
		html += "  <textarea name=\"infoContent\" rows=\"2\" cols=\"40\"></textarea><br/>\n";
		html += "<table>\n";
		html += "  <tr>\n";
		html += "    <td>Authorised roles:</td>\n";
		html += "    <td width=\"50\"></td>\n";
		html += "    <td>Authorised users:</td>\n";
		html += "  </tr>\n";
		html += "  <tr>\n";
		html += "    <td valign=\"top\">\n";
		for (Role role : userData.getRoles()) {
			if (userData.getType(typename).getAuthorisedRolesDefault().contains(role.getRolename())) {
				html += "  <input name=\"infoAuthorisedRoles\" type=\"checkbox\" value=\"" + role.getRolename() + "\" checked=\"checked\" />" + role.getRealname() + "<br/>\n";
			} else {
				html += "  <input name=\"infoAuthorisedRoles\" type=\"checkbox\" value=\"" + role.getRolename() + "\" />" + role.getRealname() + "<br/>\n";
			}
		}
		html += "    </td>\n";
		html += "    <td>\n";
		html += "    </td>\n";
		html += "    <td valign=\"top\">\n";
		for (User user : userData.getUsers()) {
			if (!user.getUsername().equals(loginUser.getUsername())) {
				html += "  <input name=\"infoAuthorisedUsers\" type=\"checkbox\" value=\"" + user.getUsername() + "\" />" + user.getRealname() + "<br/>\n";
			}
		}
		html += "    </td>\n";
		html += "  </tr>\n";
		html += "</table>\n";
		html += "<table>\n";
		html += "  <tr>\n";
		html += "    <td valign=\"top\">\n";
		html += "  <input name=\"submit\" type=\"submit\" value=\"Provide information\" />\n";
		html += "  <input name=\"action\" type=\"hidden\" value=\"provideInfo\" />\n";
		html += "  <input name=\"page\" type=\"hidden\" value=\"providedInfo\" />\n";
		html += "  <input name=\"infoUsername\" type=\"hidden\" value=\"" + username + "\" />\n";
		html += "  <input name=\"infoTypename\" type=\"hidden\" value=\"" + typename + "\" />\n";
		html += "</form>\n";
		html += "    </td>\n";
		html += "    <td valign=\"top\">\n";
		html += "      <form action=\"\" method=\"POST\">\n";
		html += "        <input name=\"submit\" type=\"submit\" value=\"Back\" />\n";
		html += "        <input name=\"page\" type=\"hidden\" value=\"provide\" />\n";
		html += "      </form>\n";
		html += "    </td>\n";
		html += "  </tr>\n";
		html += "</table>\n";
		return html;
	}

	private String getProvidedInfoPage(String username, String typename) {
		String html = "";
		html += "<p>Provided information of type <i>" + userData.getTypeRealname(typename) + "</i> for <i>" + userData.getUserRealname(username) + "</i>.</p>\n";
		html += "<form action=\"\" method=\"POST\">\n";
		html += "  <input name=\"submit\" type=\"submit\" value=\"Back\" />\n";
		html += "  <input name=\"page\" type=\"hidden\" value=\"provide\" />\n";
		html += "</form>\n";
		return html;
	}

	private String getDeletePage() {
		String html = "";

		html += "<p>From which type of information do you want to delete?</p>\n";

		html += "<table>\n";
		html += "  <tr>\n";
		html += "    <td>\n";

		for (int i = 0; i < userData.getInformationTypes().size(); i++) {
			InformationType informationType = userData.getInformationTypes().get(i);

			if (i == Math.round((double) userData.getInformationTypes().size() / 2)) {
				html += "    </td>\n";
				html += "    <td valign=\"top\">\n";
			}

			html += "      <form action=\"\" method=\"POST\">\n";
			html += "        <input name=\"submit\" type=\"submit\" value=\"" + informationType.getRealname() + "\" />\n";
			html += "        <input name=\"page\" type=\"hidden\" value=\"deleteInfo\" />\n";
			html += "        <input name=\"infoUsername\" type=\"hidden\" value=\"" + loginUser.getUsername() + "\" />\n";
			html += "        <input name=\"infoTypename\" type=\"hidden\" value=\"" + informationType.getTypename() + "\" />\n";
			html += "      </form>\n";
		}

		html += "    </td>\n";
		html += "  </tr>\n";
		html += "</table>\n";

		return html;
	}

	private String getDeleteInfoPage(String username, String typename) {
		String html = "";
		LinkedList<InformationItem> informationItems = userData.getInformationItems(username, typename, loginUser.getUsername());
		if (informationItems.size() > 0) {
			html += "<p>Which information do you want to delete?</p>\n";
			html += "<form action=\"\" method=\"POST\">\n";
			html += "<table>\n";
			for (InformationItem informationItem : informationItems) {
				html += "  <tr>\n";
				html += "    <td>\n";
				html += "  <input name=\"infoId\" type=\"checkbox\" value=\"" + informationItem.getId() + "\" />\n";
				html += "    </td>\n";
				//html += "    <td>" + informationItem.getContent() + "</td>\n";
				html += "    <td>" + informationItem.getContent().replaceAll("\n", "<br/>") + "</td>\n";
				html += "  </tr>\n";
			}
			html += "</table>\n";
			html += "<table>\n";
			html += "  <tr>\n";
			html += "    <td valign=\"top\">\n";
			html += "  <input name=\"submit\" type=\"submit\" value=\"Delete information\" />\n";
			html += "  <input name=\"action\" type=\"hidden\" value=\"deleteInfo\" />\n";
			html += "  <input name=\"page\" type=\"hidden\" value=\"deletedInfo\" />\n";
			html += "  <input name=\"infoUsername\" type=\"hidden\" value=\"" + username + "\" />\n";
			html += "  <input name=\"infoTypename\" type=\"hidden\" value=\"" + typename + "\" />\n";
			html += "</form>\n";
			html += "    </td>\n";
			html += "    <td valign=\"top\">\n";
			html += "      <form action=\"\" method=\"POST\">\n";
			html += "        <input name=\"submit\" type=\"submit\" value=\"Back\" />\n";
			html += "        <input name=\"page\" type=\"hidden\" value=\"delete\" />\n";
			html += "      </form>\n";
			html += "    </td>\n";
			html += "  </tr>\n";
			html += "</table>\n";

		} else {
			html += "<p>No corresponding information exists.</p>\n";
			html += "<form action=\"\" method=\"POST\">\n";
			html += "  <input name=\"submit\" type=\"submit\" value=\"Back\" />\n";
			html += "  <input name=\"page\" type=\"hidden\" value=\"delete\" />\n";
			html += "</form>\n";
		}

		return html;
	}

	private String getDeletedInfoPage(String username, String typename) {
		String html = "";
		html += "<p>Deleted information of type <i>" + userData.getTypeRealname(typename) + "</i> for <i>" + userData.getUserRealname(username) + "</i>.</p>\n";
		html += "<form action=\"\" method=\"POST\">\n";
		html += "  <input name=\"submit\" type=\"submit\" value=\"Back\" />\n";
		html += "  <input name=\"page\" type=\"hidden\" value=\"delete\" />\n";
		html += "</form>\n";
		return html;
	}

	private String getHistoryPage() {
		String html = "";

		html += "<p>For which type of information do you want to view the history of requests?</p>\n";

		html += "<table>\n";
		html += "  <tr>\n";
		html += "    <td>\n";

		for (int i = 0; i < userData.getInformationTypes().size(); i++) {
			InformationType informationType = userData.getInformationTypes().get(i);

			if (i == Math.round((double) userData.getInformationTypes().size() / 2)) {
				html += "    </td>\n";
				html += "    <td valign=\"top\">\n";
			}

			html += "      <form action=\"\" method=\"POST\">\n";
			html += "        <input name=\"submit\" type=\"submit\" value=\"" + informationType.getRealname() + "\" />\n";
			html += "        <input name=\"page\" type=\"hidden\" value=\"historyInfo\" />\n";
			html += "        <input name=\"infoUsername\" type=\"hidden\" value=\"" + loginUser.getUsername() + "\" />\n";
			html += "        <input name=\"infoTypename\" type=\"hidden\" value=\"" + informationType.getTypename() + "\" />\n";
			html += "      </form>\n";
		}

		html += "    </td>\n";
		html += "  </tr>\n";
		html += "</table>\n";

		return html;
	}

	private String getHistoryInfoPage(String username, String typename) {
		String html = "";
		html += "<p>History of requests for information of type <i>" + userData.getTypeRealname(typename) + "</i>:</p>\n";
		LinkedList<InformationItem> informationItems = userData.getInformationItems(username, typename, loginUser.getUsername());
		if (informationItems.size() > 0) {
			for (InformationItem informationItem : informationItems) {
				html += "<p>\n";
				//html += informationItem.getContent() + "<br/>\n";
				html += informationItem.getContent().replaceAll("\n", "<br/>") + "<br/>\n";
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(informationItem.getTimeProvided());
				html += "<a class=\"small\">\n";
				html += "  Provided at: " + SIMPLE_DATE_FORMAT.format(calendar.getTime()) + "<br/>\n";
				html += "  Authorised roles: ";
				for (String authorisedRole : informationItem.getAuthorisedRoles()) {
					if (authorisedRole != informationItem.getAuthorisedRoles().get(0)) {
						html += ", ";
					}
					html += userData.getRoleRealname(authorisedRole);
				}
				html += "<br/>\n";
				html += "  Authorised users: ";
				for (String authorisedUser : informationItem.getAuthorisedUsers()) {
					if (authorisedUser != informationItem.getAuthorisedUsers().get(0)) {
						html += ", ";
					}
					html += userData.getUserRealname(authorisedUser);
				}
				html += "<br/>\n";
				html += "  Requests:\n";
				html += "</a>\n";
				html += "<table>\n";
				for (teambuddyInterface.Request request : informationItem.getRequests()) {
					html += "  <tr>\n";
					calendar.setTimeInMillis(request.getTime());
					html += "    <td class=\"small\">&raquo; " + SIMPLE_DATE_FORMAT.format(calendar.getTime()) + "</td>\n";
					html += "    <td class=\"small\">" + userData.getUserRealname(request.getUsername());
					if (!request.isAuthorised()) {
						html += "<i class=\"small\"> (not authorised)</i>";
					}
					html += "</td>\n";
					html += "  </tr>\n";
				}
				html += "</table>\n";
				html += "</p>\n";
			}
		} else {
			html += "<p>No corresponding information exists.</p>\n";
		}
		html += "<form action=\"\" method=\"POST\">\n";
		html += "  <input name=\"submit\" type=\"submit\" value=\"Back\" />\n";
		html += "  <input name=\"page\" type=\"hidden\" value=\"history\" />\n";
		html += "</form>\n";
		return html;
	}

	private String getChargingPage() {
		String html = "";
		Object chargingObject = getWMObjectProperty(CURRENT_PLATFORM, CHARGING);
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
		html += "  For charging please first select <i class=\"small\">Charging</i> and then plug the cable.<br/>\n";
		html += "  For releasing please first unplug the cable and then select <i class=\"small\">Not charging</i>.<br/>\n";
		html += "</p>\n";
		html += "<form action=\"\" method=\"POST\">\n";
		html += "  <input name=\"submit\" type=\"submit\" value=\"Back\" />\n";
		html += "  <input name=\"page\" type=\"hidden\" value=\"start\" />\n";
		html += "</form>\n";
		return html;
	}

	private void msgReceived(String fromUsername, String toUsername, String messageText) {
		if (interfaceCompetency != null) {
			// obtain an id for the message
			msgID++;
			String id = new Integer(msgID).toString();

			// write msg to blackboard
			BlackBoard bb = interfaceCompetency.getArchitecture().getBlackBoard();
			if (bb.hasSubContainer(MSG_BLACKBOARD_CONTAINER))
				bb.getSubContainer(MSG_BLACKBOARD_CONTAINER).requestSetProperty(id, messageText);
			else {
				HashMap<String, Object> properties = new HashMap<String, Object>();
				properties.put(id, messageText);
				bb.requestAddSubContainer(MSG_BLACKBOARD_CONTAINER, MSG_BLACKBOARD_CONTAINER, properties);
			}

			// raise remote action
			ArrayList<String> parameters = new ArrayList<String>();
			parameters.add(toUsername);
			parameters.add(id);
			raiseMindAction(fromUsername, "leaveMessage", parameters);
		}
	}

	private void talkReceived(String talkText, String talkSymbol) {
		if (interfaceCompetency != null) {
			String talkUsername = null;
			if (loginUser != null) {
				talkUsername = loginUser.getUsername();
			}
			ArrayList<String> parameters = new ArrayList<String>();
			parameters.add("SELF");
			//parameters.add(talkText);
			parameters.add(talkSymbol);
			raiseMindAction(talkUsername, "talk", parameters);
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

}
