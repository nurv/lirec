package cmion.personalguide.competencies;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Message;

import lirec.personalguide.events.EventChangeEmotion;
import lirec.personalguide.events.EventTalk;
import lirec.personalguide.events.EventUserSubmit;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;

public class TalkCompetency extends Competency {

	public TalkCompetency(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "TalkCompetency";
		this.competencyType = "TalkCompetency";	
	}

	@Override
	public void initialize() 
	{
		this.available = true;
	}
	
	@Override
	public void registerHandlers()
	{
		super.registerHandlers();
		Simulation.instance.getEventHandlers().add(new UserTalkHandler());
	}
	
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		if (parameters.containsKey("utterance"))
		{
			String utterance = parameters.get("utterance");
			ArrayList<String> userOptions = new ArrayList<String>();
			if (utterance.toLowerCase().contains("when"))
			{
				userOptions.add("tomorrow");
				userOptions.add("next week");
				userOptions.add("in 2 weeks");	
				userOptions.add("I can't say now");
			}
			else if (utterance.toLowerCase().contains("ask"))
			{
				userOptions.add("yes");
				userOptions.add("no");
			}
			this.raise(new EventTalk(utterance,userOptions));
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {}
		}
		return true;
	}

	@Override
	public boolean runsInBackground() 
	{
		return false;
	}

	/** if the user replys we handle this by creating a remote action to be perceived
	 *  by the mind */
	private class UserTalkHandler extends EventHandler
	{
		public UserTalkHandler() {
			super(EventUserSubmit.class);
		}

		@Override
		public void invoke(IEvent evt) 
		{
			EventUserSubmit evtus = (EventUserSubmit) evt;
			ArrayList<String> parameters = new ArrayList<String>();
			parameters.add(evtus.getUserOption());
			MindAction ma = new MindAction("User","Reply",parameters);		
			TalkCompetency.this.raise(new EventRemoteAction(ma));
		}
	}
	
}
