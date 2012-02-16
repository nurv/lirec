package cmion.emysTK;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;

public class SimpleTalk extends Competency {

	private String host;	
	private int port;	
	
	public SimpleTalk(IArchitecture architecture, String host, String port) {
		super(architecture);
		this.host = host;
		this.port = Integer.parseInt(port);
		this.competencyName = "SimpleTalk";
		this.competencyType = "SimpleTalk";
	}

	@Override
	public void initialize() {
		this.available = true;
	}	
	
	@Override
	public boolean runsInBackground() {
		return false;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters)
			throws CompetencyCancelledException {
		String text = parameters.get("text");
		if (text == null) return false;
		new SendCommandThread(host, port, "Speak" , text).start();
		// calculate a rough estimate of how long the text takes to speak:
		int length = 1500; // allow for 1500 ms initial length (delay for synthesis)
		length += 65*text.length(); // add 65 ms per char in the string to synthesize
		// add 200 ms for every , and 400 ms for every .? or !
		length +=  200 * countOccurrences(text,',');
		length +=  400 * countOccurrences(text,'.');
		length +=  400 * countOccurrences(text,'?');
		length +=  400 * countOccurrences(text,'!');
		
		// wait for the calculated length
		try {
			Thread.sleep(length);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

	private int countOccurrences(String haystack, char needle)
	{
	    int count = 0;
	    for (int i=0; i < haystack.length(); i++)
	    {
	        if (haystack.charAt(i) == needle)
	        {
	             count++;
	        }
	    }
	    return count;
	}


}
