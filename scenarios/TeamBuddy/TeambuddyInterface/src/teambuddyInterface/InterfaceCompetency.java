package teambuddyInterface;

import java.util.HashMap;

import org.eclipse.jetty.server.Server;

import cmion.architecture.IArchitecture;
import cmion.level2.CompetencyCancelledException;

public class InterfaceCompetency extends cmion.level2.Competency {

	public InterfaceCompetency(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "InterfaceCompetency";
		this.competencyType = "InterfaceCompetency";		
	}

	@Override
	public void initialize() 
	{
		this.available = true;
	}	
	
	@Override
	public boolean runsInBackground() {
		return true;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters)
			throws CompetencyCancelledException {
		try {
			Server server = new Server(8080);
			server.setHandler(new InterfaceHandler(this));
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}



}
