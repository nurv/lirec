package cmion.inTheWild.competencies;

import java.util.HashMap;
import java.util.StringTokenizer;

import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;


/** competency connected to the cereproc samgar module that synthesizes 
 *  and plays back text, teh samgar module also provides lipsynch data,
 *  that we can read but we usually prefer to route that directly into
 *  the samgar module for animation to reduce the delay*/
public class Cereproc extends SamgarCompetency {
	
	
	private volatile boolean finished;
	
	public Cereproc(IArchitecture architecture) {
		super(architecture);
	
		//name and type of the competence
		this.competencyName ="Cereproc-TTS";
		this.competencyType ="TTS";
	}	

	@Override
	public void onRead(Bottle bottleIn) 
	{
		if (bottleIn.get(0).isInt())
		{
			if (bottleIn.get(0).asInt() == 1) // end signal
				finished = true;
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
		}
	}
		

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		finished = false;
		int timeoutcounter = 0;

		// text to synthesize can be passed to this competency either plain or 
		// using xml markup (this allows for greater control, i.e. emphasize certain words),
		// the markup documentation can be found in the cereproc sdk
		String plaintext = parameters.get("text");
		String xmltext = parameters.get("xmltext");

		// check if either type of input is provided
		if ((plaintext==null) && (xmltext==null)) return false;
		
		// we ignore the plain text if the xml text is provided 
		String text = null;
		if (xmltext!=null)
			text = xmltext;
		else
			text = "<parent>" + plaintext + "</parent>";
	
		
		// send text to connected tts
		Bottle b = this.prepareBottle();
		b.addString(text);
		this.sendBottle();
		
		// wait until finished or timed out (currently set to 50 sec  = 500*100 ms sleep)
		while(! finished)
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			timeoutcounter++;
			if (timeoutcounter>500) return false;
		}
		return true;
	}

	@Override
	public boolean runsInBackground() {
		return false;
	}

}
