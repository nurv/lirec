package cmion.addOns.samgar.playerProxy;

import java.util.ArrayList;

import yarp.Bottle;

public class PlayerSonar {
	public enum CMD{
		SET_REQ
	}
	
	public enum DATA{
		ERROR, ACK, POSES, RANGES
	}
	
	public ArrayList<Coordinate> poses;
	public int posesCount;
	public ArrayList<Double> ranges;
	public int rangesCount;
	
	public boolean updated;
	
	public PlayerSonar(){
		//goal = new Coordinate();
		//pose = new Coordinate();
		ranges = new ArrayList<Double>();
		poses = new ArrayList<Coordinate>();
	}
	
	public void enable(Bottle b){
	}
	
	public void setReq(Bottle b, final boolean poses, final boolean ranges){
		b.clear();
		b.addInt(PlayerProxy.Types.Sonar.ordinal());
		b.addInt(CMD.SET_REQ.ordinal());
		b.addInt(poses?1:0); 	// POSES
		b.addInt(ranges?1:0); 	// RANGES
	}
	
	public void update(Bottle b){
		if(b.get(1).asInt() == DATA.POSES.ordinal())
		{
			final int COUNT_FIELD=2;
			final int FIELDS=3;
			final int X_FIELD=1;
			final int Y_FIELD=2;
			final int A_FIELD=3;
			posesCount = b.get(COUNT_FIELD).asInt();
			for (int i=0; i<posesCount; i++)
			{
				poses.add(new Coordinate(b.get(COUNT_FIELD+i*FIELDS+X_FIELD).asDouble(),
										  b.get(COUNT_FIELD+i*FIELDS+Y_FIELD).asDouble(),
										  b.get(COUNT_FIELD+i*FIELDS+A_FIELD).asDouble()));
			}
			updated=true;
		} // POINTS
		if(b.get(1).asInt() == DATA.RANGES.ordinal())
		{
			final int COUNT_FIELD=2;
			final int DATA_FIELD = 3;
			rangesCount = b.get(COUNT_FIELD).asInt();
			for (int i=0; i<rangesCount; i++)
			{
				ranges.add(b.get(DATA_FIELD+i).asDouble());
			}
			updated=true;
		} // RANGES
	}
}
