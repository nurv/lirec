package cmion.TeamBuddy.competencies;

import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;

import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;

/**
 * competency connected to the cereproc samgar module that synthesizes and plays
 * back text, the samgar module also provides lipsynch data, that we can read
 * but we usually prefer to route that directly into the samgar module for
 * animation to reduce the delay
 */
public class Cereproc extends SamgarCompetency {

	private boolean finished;
	int timeoutcounter = 0;

	public Cereproc(IArchitecture architecture) {
		super(architecture);

		//name and type of the competence
		this.competencyName = "TTS";
		this.competencyType = "TTS";
	}

	@Override
	public void onRead(Bottle bottleIn) {
		//if (bottleIn.get(0).isInt())
		//{
		if (bottleIn.get(0).asInt() == 1) // end signal
		{
			finished = true;
			System.out.println("TTS finished");

		}
		// we dont need the lip synch code here
		/*
		else if (bottleIn.get(0).asInt() == 2) // phoneme info 
		{
			ArrayList<Phoneme> phonemes = new ArrayList<Phoneme>();				
			for (int i=1; i<bottleIn.size(); i+=3)
			{
				Phoneme p = new Phoneme(bottleIn.get(i).asString().c_str(),
						bottleIn.get(i+1).asDouble(), bottleIn.get(i+2).asDouble());
				phonemes.add(p);
			}
			architecture.getBlackBoard().requestSetProperty("phonemes", phonemes);
		}*/
		//}
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) {
		finished = false;
		timeoutcounter = 0;

		String text = parameters.get("text");
		if (text == null)
			return false;
		else
			System.out.println(text);
		
		// check the text for variables (words strings starting with $)
		StringTokenizer st = new StringTokenizer(text);
		while (st.hasMoreTokens()) {
			String word = st.nextToken();
			if (word.startsWith("$")) {
				// we found a variable, see if we have a parameter for it, if yes replace
				if (parameters.containsKey(word))
					text = text.replace(word, parameters.get(word));
			}

			if (word.startsWith("@")) {
				// replace the word starting with @ with current time
				text = text.replace(word, getStrRealTime());
			}
		}

		//add xml tags
		text = "<parent>" + text + "</parent>";

		// send text to connected tts
		Bottle b = this.prepareBottle();
		b.addString(text);
		this.sendBottle();
		
		// wait until finished or timed out (currently set to 100 sec  = 1000*100 ms sleep)
		while (!finished) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			timeoutcounter++;
			if (timeoutcounter > 70)
				return true;
		}
		
		return true;
	}

	@Override
	public boolean runsInBackground() {
		return false;
	}

	public String getStrRealTime() {
		Calendar cal = Calendar.getInstance();
		System.out.println("Cereproc.getStrRealTime(): " + cal );
		String strRealTime = "";
		if (cal.get(Calendar.HOUR_OF_DAY) >= 0 && cal.get(Calendar.HOUR_OF_DAY) < 6)
			strRealTime = "Night";
		else if (cal.get(Calendar.HOUR_OF_DAY) >= 6 && cal.get(Calendar.HOUR_OF_DAY) < 12)
			strRealTime = "Morning";
		else if (cal.get(Calendar.HOUR_OF_DAY) >= 12 && cal.get(Calendar.HOUR_OF_DAY) < 18)
			strRealTime = "Afternoon";
		else if (cal.get(Calendar.HOUR_OF_DAY) >= 18 && cal.get(Calendar.HOUR_OF_DAY) < 24)
			strRealTime = "Evening";
		return strRealTime;
	}
}
