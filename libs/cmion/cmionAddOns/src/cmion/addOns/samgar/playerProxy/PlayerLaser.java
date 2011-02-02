package cmion.addOns.samgar.playerProxy;

import java.util.ArrayList;

import yarp.Bottle;

public class PlayerLaser {
	public enum CMD{
		SET_REQ, SET_CONFIG
	}
	
	public enum DATA{
		ERROR, ACK, CONFIG, SIZE, POSE, ROBOT_POSE, POINTS, RANGES
	}
	
	public double min_angle;
	public double max_angle;
	public double scan_res;
	public double range_res;
	public double width;
	public double length;
	public Coordinate pose;
	public Coordinate robot_pose;
	public ArrayList<Point> points;
	public int pointsCount;
	public ArrayList<PolarPoint> polarpoints;
	public int polarpointsCount;
	
	public boolean updated;
	
	public PlayerLaser(){
		//goal = new Coordinate();
		//pose = new Coordinate();
		points = new ArrayList<Point>();
	}
	
	public void enable(Bottle b){
	}
	
	public void setConfig(Bottle b, final double min_angle, final double max_angle,
									final double scan_res, final double range_res){
		b.clear();
		b.addInt(PlayerProxy.Types.Laser.ordinal());
		b.addInt(CMD.SET_CONFIG.ordinal());
		b.addDouble(min_angle);
		b.addDouble(max_angle);
		b.addDouble(scan_res);
		b.addDouble(range_res);
	}
	public void setReq(Bottle b, final boolean config, final boolean size,
						  final boolean pose,  final boolean robot_pose, 
						  final boolean points, final boolean ranges){
		b.clear();
		b.addInt(PlayerProxy.Types.Laser.ordinal());
		b.addInt(CMD.SET_REQ.ordinal());
		b.addInt(config?1:0); 	// SPEED
		b.addInt(size?1:0); 	// POSITION
		b.addInt(pose?1:0);		// SIZE
		b.addInt(robot_pose?1:0);	// STALL
		b.addInt(points?1:0);		// SIZE
		b.addInt(ranges?1:0);	// STALL	
	}
	
	public void update(Bottle b){
		if(b.get(1).asInt() == DATA.CONFIG.ordinal())
		{
			min_angle =  b.get(2).asDouble();
			max_angle = b.get(3).asDouble();
			scan_res = b.get(4).asDouble();
			range_res = b.get(5).asDouble();
			updated=true;
		} // CONFIG	
		if(b.get(1).asInt() == DATA.SIZE.ordinal())
		{
			width =  b.get(2).asDouble();
			length = b.get(3).asDouble();
			updated=true;
		} // SIZE	
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
		if(b.get(1).asInt() == DATA.ROBOT_POSE.ordinal())
		{
			final int X_FIELD=2;
			final int Y_FIELD=3;
			final int A_FIELD=4;
			robot_pose.set(b.get(X_FIELD).asDouble(),
					 b.get(Y_FIELD).asDouble(),
					 b.get(A_FIELD).asDouble());
			updated=true;
		} // ROBOT_POSE
		if(b.get(1).asInt() == DATA.POINTS.ordinal())
		{
			final int COUNT_FIELD=2;
			final int FIELDS=2;
			final int X_FIELD=1;
			final int Y_FIELD=2;
			pointsCount = b.get(COUNT_FIELD).asInt();
			for (int i=0; i<pointsCount; i++)
			{
				points.add(new Point(b.get(COUNT_FIELD+i*FIELDS+X_FIELD).asDouble(),
											 b.get(COUNT_FIELD+i*FIELDS+Y_FIELD).asDouble()));
			}
			updated=true;
		} // POINTS
		if(b.get(1).asInt() == DATA.RANGES.ordinal())
		{
			final int COUNT_FIELD=2;
			final int FIELDS=2;
			final int RANGE_FIELD=1;
			final int ANGLE_FIELD=2;
			polarpointsCount = b.get(COUNT_FIELD).asInt();
			for (int i=0; i<polarpointsCount; i++)
			{
				polarpoints.add(new PolarPoint(b.get(COUNT_FIELD+i*FIELDS+RANGE_FIELD).asDouble(),
											 b.get(COUNT_FIELD+i*FIELDS+ANGLE_FIELD).asDouble()));
			}
			updated=true;
		} // RANGES
	}
}
