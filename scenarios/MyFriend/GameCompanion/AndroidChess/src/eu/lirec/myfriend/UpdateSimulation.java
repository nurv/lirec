package eu.lirec.myfriend;

import java.util.TimerTask;

import ion.Meta.Simulation;

public class UpdateSimulation extends TimerTask {

	private Simulation simulation;
	
	public UpdateSimulation(Simulation simulation) {
		this.simulation = simulation;
	}
	
	@Override
	public void run() {
		
		try {
			simulation.update();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
