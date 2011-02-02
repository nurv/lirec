package cmion.addOns.samgar.playerProxy;

import yarp.Bottle;
import cmion.addOns.samgar.playerProxy.PlayerProxy;

public class PlayerPosition2d {
	public enum CMD{
		SET_REQ, SET_SPEED, SET_GOAL, SET_ODOM, SET_MOTOR
	}
	
	public enum DATA{
		ERROR, ACK, SPEED, POSE, SIZE, STALL
	}
	
	public Coordinate goal;
	public Coordinate pose;
	public double trans;
	public double ang;
	public double width;
	public double length;
	public boolean stall;
	
	public boolean updated;
	
	public PlayerPosition2d(){
		stall = false;
		goal = new Coordinate();
		pose = new Coordinate();
	}
	
	public void enable(Bottle b){
		setMotor(b,true);
	}
	
	public void setSpeed(Bottle b, final double translational, final double anglular){
		b.clear();
		b.addInt(PlayerProxy.Types.Position2d.ordinal());
		b.addInt(CMD.SET_SPEED.ordinal());
		b.addDouble(translational);
		b.addDouble(anglular);
	}
	
	public void setGoal(Bottle b, final Coordinate coord){
		b.clear();
		b.addInt(PlayerProxy.Types.Position2d.ordinal());
		b.addInt(CMD.SET_GOAL.ordinal());
		b.addDouble(coord.x);
		b.addDouble(coord.y);
		b.addDouble(coord.a);
	}
	
	public void setOdom(Bottle b, final Coordinate coord){
		b.clear();
		b.addInt(PlayerProxy.Types.Position2d.ordinal());
		b.addInt(CMD.SET_ODOM.ordinal());
		b.addDouble(coord.x);
		b.addDouble(coord.y);
		b.addDouble(coord.a);
	}
	
	public void setMotor(Bottle b, final boolean motorEnable){
		b.clear();
		b.addInt(PlayerProxy.Types.Position2d.ordinal());
		b.addInt(CMD.SET_MOTOR.ordinal());
		b.addInt(motorEnable?1:0);
	}
	

	public void setReq(Bottle b, final boolean speed, final boolean pose,
						  final boolean size,  final boolean stall){
		b.clear();
		b.addInt(PlayerProxy.Types.Position2d.ordinal());
		b.addInt(CMD.SET_REQ.ordinal());
		b.addInt(speed?1:0); 	// SPEED
		b.addInt(pose?1:0); 	// POSITION
		b.addInt(size?1:0);		// SIZE
		b.addInt(stall?1:0);	// STALL
	}
	
	public void update(Bottle b){
		if(b.get(1).asInt() == DATA.SPEED.ordinal())
		{
			this.trans = b.get(2).asDouble();
			this.ang = b.get(3).asDouble();
			updated=true;
		} // SPEED	
		if(b.get(1).asInt() == DATA.POSE.ordinal())
		{
			final int X_FIELD=2;
			final int Y_FIELD=3;
			final int A_FIELD=4;
			pose.set(b.get(X_FIELD).asDouble(),
					 b.get(Y_FIELD).asDouble(),
					 b.get(A_FIELD).asDouble());
			updated=true;
		} // POSE	
		if(b.get(1).asInt() == DATA.SIZE.ordinal())
		{
			this.width = b.get(2).asDouble();
			this.length = b.get(3).asDouble();
			updated=true;
		} // SIZE
		if(b.get(1).asInt() == DATA.STALL.ordinal())
		{
			this.stall = b.get(2).asInt()>0;
			updated=true;
		} // STALL
	}
}
