package cmion.TeamBuddy.competencies;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import yarp.Bottle;

import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;
import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;
import cmion.storage.BlackBoard;
import cmion.storage.WorldModel;

public class PhoneRing extends SamgarCompetency {

	private long timePhoneReturned;

	private int phoneStatus;

	private int phoneStatusOld;

	private static int callId = 0;

	private static final String WM_CURRENT_PLATFORM = "CurrentPlatform";

	private static final String WM_PHONE_STATUS = "phoneStatus";

	private static final String[] PHONE_STATUS = { "Idle", "Ringing", "PickedUp" };

	private static final String WM_AGENT_NAME = "Spirit";

	private static final String WM_USERS_DETECTED = "usersDetected";

	private static final String BB_PHONE_CALLS = "PhoneCalls";

	private static final String WM_PHONE_CALLS = "PhoneCalls";

	public PhoneRing(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "PhoneRing";
		this.competencyType = "PhoneRing";
		phoneStatus = 0;
		phoneStatusOld = 0;
		setWMObjectProperty(WM_CURRENT_PLATFORM, WM_PHONE_STATUS, PHONE_STATUS[phoneStatus]);
		timePhoneReturned = System.currentTimeMillis();
	}

	@Override
	public void onRead(Bottle bottleIn) {
		// 0 no call
		// 1 phone rings (LED is lit)
		// 2 phone picked up

		phoneStatusOld = phoneStatus;
		phoneStatus = 0;//bottleIn.get(0).asInt();

		setWMObjectProperty(WM_CURRENT_PLATFORM, WM_PHONE_STATUS, PHONE_STATUS[phoneStatus]);

		// when phone is returned, status changes from 2 to 0 (LED is dark first)
		if (phoneStatusOld == 2 && (phoneStatus == 0 || phoneStatus == 1)) {
			timePhoneReturned = System.currentTimeMillis();
			/*
			ArrayList<String> actionParameters = new ArrayList<String>();
			actionParameters.add("SELF");
			MindAction ma = new MindAction("Unknown", "sendHome", actionParameters);
			this.raise(new EventRemoteAction(ma));
			*/
		}

		// if phone status changes from 0 to 1, we receive a phone call
		long timePhoneReturnedDiff = System.currentTimeMillis() - timePhoneReturned;
		if (phoneStatusOld == 0 && phoneStatus == 1 && timePhoneReturnedDiff > 10000) {

			callId++;

			BlackBoard bb = architecture.getBlackBoard();
			WorldModel wm = architecture.getWorldModel();

			// remember missed calls (if nobody is in the lab)
			Object usersDetected = null;
			if (wm.hasAgent(WM_AGENT_NAME)) {
				usersDetected = wm.getAgent(WM_AGENT_NAME).getPropertyValue(WM_USERS_DETECTED);
			}
			if (usersDetected != null && usersDetected instanceof String && ((String) usersDetected).equals("False")) {

				String callIdStr = new Integer(callId).toString();

				// write date and time to BlackBoard
				SimpleDateFormat sdf = new SimpleDateFormat();
				String strDateTime = sdf.format(Calendar.getInstance().getTime());
				if (bb.hasSubContainer(BB_PHONE_CALLS))
					bb.getSubContainer(BB_PHONE_CALLS).requestSetProperty(callIdStr, strDateTime);
				else {
					HashMap<String, Object> properties = new HashMap<String, Object>();
					properties.put(callIdStr, strDateTime);
					bb.requestAddSubContainer(BB_PHONE_CALLS, BB_PHONE_CALLS, properties);
				}

				// write status to WorldModel
				if (wm.hasObject(WM_PHONE_CALLS))
					wm.getObject(WM_PHONE_CALLS).requestSetProperty(callIdStr, new String("Missed"));
				else {
					HashMap<String, Object> properties = new HashMap<String, Object>();
					properties.put(callIdStr, new String("Missed"));
					wm.requestAddObject(WM_PHONE_CALLS, properties);
				}

			}

			// raise mind action
			ArrayList<String> actionParameters = new ArrayList<String>();
			actionParameters.add("Unknown"); // unknown target
			actionParameters.add(String.valueOf(callId)); // call id
			MindAction ma = new MindAction("SELF", "receivePhoneCall", actionParameters);
			this.raise(new EventRemoteAction(ma));
		}

		bottleIn.clear();
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean runsInBackground() {
		return true;
	}

	private void setWMObjectProperty(String objectName, String propertyName, Object propertyValue) {
		WorldModel wm = architecture.getWorldModel();
		if (wm.hasObject(objectName)) {
			wm.getObject(objectName).requestSetProperty(propertyName, propertyValue);
		} else {
			HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.put(propertyName, propertyValue);
			wm.requestAddObject(objectName, properties);
		}
	}

}
