package cmion.emysTK;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;

public class SimpleAnimation extends Competency {

	private String host;	
	private int port;
	
	public SimpleAnimation(IArchitecture architecture, String host, String port) {
		super(architecture);
		this.host = host;
		this.port = Integer.parseInt(port);
		this.competencyName = "SimpleAnimation";
		this.competencyType = "SimpleAnimation";
	}

	@Override
	public boolean runsInBackground() {
		return false;
	}

	@Override
	public void initialize() {
		this.available = true;
	}
	
	
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters)
			throws CompetencyCancelledException {
		String animation = parameters.get("animation");
		if (animation == null) return false;
		new SendCommandThread(host, port, "PlayAnimationForced" , animation).start();
		// wait 3 seconds
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		return true;
	}


}
