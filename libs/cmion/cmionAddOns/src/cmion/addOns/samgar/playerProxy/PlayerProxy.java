package cmion.addOns.samgar.playerProxy;

public class PlayerProxy{
	public enum Types {
		Position2d, Localize, Planner, Laser, Map, Sonar, Bumper
	}
	
	public enum Fields{
		TYPE, CMD
	}
	
	public enum CMD{
		SET_REQ
	}
	
	public enum DATA{
		ERROR, ACK
	}
}

