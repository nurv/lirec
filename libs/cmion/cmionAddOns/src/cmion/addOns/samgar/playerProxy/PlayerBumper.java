package cmion.addOns.samgar.playerProxy;

import java.util.ArrayList;

import yarp.Bottle;

public class PlayerBumper {
	public enum CMD{
		SET_REQ
	}
	
	public enum DATA{
		ERROR, ACK, POSES, BUMPED
	}
	
	public class BumperPose{
		public Coordinate coord;
		public double length;
		public double radius;
		
		public BumperPose()
		{
			coord=new Coordinate();
			length = 0;
			radius = 0;
		}
		
		public BumperPose(final Coordinate newCoordinate, final double newLenght, final double newRadius)
		{
			coord=newCoordinate;
			length = newLenght;
			radius = newRadius;
		}
		
		public void set(final Coordinate newCoordinate, final double newLenght, final double newRadius)
		{
			coord=newCoordinate;
			length = newLenght;
			radius = newRadius;
		}
	}
	
	public ArrayList<BumperPose> poses;
	public int posesCount;
	public ArrayList<Boolean> bumped;
	public int rangesCount;
	
	public boolean updated;
	
	public PlayerBumper(){
		//goal = new Coordinate();
		//pose = new Coordinate();
		bumped = new ArrayList<Boolean>();
		poses = new ArrayList<BumperPose>();
	}
	
	public void enable(Bottle b){
	}
	
	public void setReq(Bottle b, final boolean poses, final boolean ranges){
		b.clear();
		b.addInt(PlayerProxy.Types.Bumper.ordinal());
		b.addInt(CMD.SET_REQ.ordinal());
		b.addInt(poses?1:0); 	// POSES
		b.addInt(ranges?1:0); 	// RANGES
	}
	
	public void update(Bottle b){
		if(b.get(1).asInt() == DATA.POSES.ordinal())
		{
			final int COUNT_FIELD=2;
			final int FIELDS=5;
			final int X_FIELD=1;
			final int Y_FIELD=2;
			final int A_FIELD=3;
			final int LENGTH_FIELD=4;
			final int RADIUS_FIELD=5;
			posesCount = b.get(COUNT_FIELD).asInt();
			for (int i=0; i<posesCount; i++)
			{
				poses.add(new BumperPose(new Coordinate(b.get(COUNT_FIELD+i*FIELDS+X_FIELD).asDouble(),
										  b.get(COUNT_FIELD+i*FIELDS+Y_FIELD).asDouble(),
										  b.get(COUNT_FIELD+i*FIELDS+A_FIELD).asDouble()),
										  b.get(COUNT_FIELD+i*FIELDS+LENGTH_FIELD).asDouble(),
										  b.get(COUNT_FIELD+i*FIELDS+RADIUS_FIELD).asDouble()));
			}
			updated=true;
		} // POINTS
		if(b.get(1).asInt() == DATA.BUMPED.ordinal())
		{
			final int COUNT_FIELD=2;
			final int DATA_FIELD = 3;
			rangesCount = b.get(COUNT_FIELD).asInt();
			for (int i=0; i<rangesCount; i++)
			{
				bumped.add( b.get(DATA_FIELD+i).asInt()==0 ? false : true );
			}
			updated=true;
		} // RANGES
	}
}
