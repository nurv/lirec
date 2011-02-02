package cmion.addOns.samgar.playerProxy;
import java.util.ArrayList;

import yarp.Bottle;
import cmion.addOns.samgar.playerProxy.PlayerProxy;

public class PlayerPlanner {
	public enum CMD{
		SET_REQ, SET_GOAL, SET_ENABLE
	}
	
	public enum DATA{
		ERROR, ACK, GOAL, POSE, CURR_WAYPOINT, PATH, WAYPOINTS
	}
	
	public Coordinate goal;
	public Coordinate pose;
	public Coordinate currWaypoint;
	public int currWaypointId;
	public boolean validPath;
	public boolean donePath;
	public ArrayList<Coordinate> waypoints;
	public int waypointsCount;
	public boolean updated;
	
	public PlayerPlanner(){
		waypoints = new ArrayList<Coordinate>();
		waypointsCount = 0;
		validPath = false;
		donePath = false;
	}
	
	public void enable(Bottle b){
		b.clear();
		b.addInt(PlayerProxy.Types.Planner.ordinal());
		b.addInt(CMD.SET_ENABLE.ordinal());
		b.addInt(1);
	}
	
	public void setGoal(Bottle b, final Coordinate coord){
		b.clear();
		b.addInt(PlayerProxy.Types.Planner.ordinal());
		b.addInt(CMD.SET_GOAL.ordinal());
		b.addDouble(coord.x);
		b.addDouble(coord.y);
		b.addDouble(coord.a);
	}
	
	public void setReq(Bottle b, final boolean goal, final boolean pose,
						  final boolean curr_waypoint,  final boolean path,
						  final boolean waypoints){
		b.clear();
		b.addInt(PlayerProxy.Types.Planner.ordinal());
		b.addInt(CMD.SET_REQ.ordinal());
		b.addInt(goal?1:0); 	// GOAL
		b.addInt(pose?1:0); 	// POSE
		b.addInt(curr_waypoint?1:0);	// CURR_WAYPOINT
		b.addInt(path?1:0);	// PATH
		b.addInt(waypoints?1:0);	// WAYPOINTS
	}
	
	public void update(Bottle b){
		if(b.get(1).asInt() == PlayerPlanner.DATA.GOAL.ordinal())
		{
			final int X_FIELD=2;
			final int Y_FIELD=3;
			final int A_FIELD=4;
			goal.set(b.get(X_FIELD).asDouble(),
					 b.get(Y_FIELD).asDouble(),
					 b.get(A_FIELD).asDouble());
			updated=true;
		} // GOAL	
		if(b.get(1).asInt() == PlayerPlanner.DATA.POSE.ordinal())
		{
			final int X_FIELD=2;
			final int Y_FIELD=3;
			final int A_FIELD=4;
			pose.set(b.get(X_FIELD).asDouble(),
					 b.get(Y_FIELD).asDouble(),
					 b.get(A_FIELD).asDouble());
			updated=true;
		} // POSE	
		if(b.get(1).asInt() == PlayerPlanner.DATA.CURR_WAYPOINT.ordinal())
		{
			final int X_FIELD=2;
			final int Y_FIELD=3;
			final int A_FIELD=4;
			final int ID_FIELD=5;
			currWaypoint.set(b.get(X_FIELD).asDouble(),
							 b.get(Y_FIELD).asDouble(),
							 b.get(A_FIELD).asDouble());
			currWaypointId = b.get(ID_FIELD).asInt();
			updated=true;
		} // CURR_WAYPOINT
		if(b.get(1).asInt() == PlayerPlanner.DATA.PATH.ordinal())
		{
			validPath = b.get(2).asInt()>0;
			donePath = b.get(3).asInt()>0;
			//if (donePath)
			//{
				//waypointsCount = 0;
				//waypoints.clear();
			//}
			updated=true;
		} // PATH
		if(b.get(1).asInt() == PlayerPlanner.DATA.WAYPOINTS.ordinal())
		{
			final int COUNT_FIELD=2;
			final int FIELDS=3;
			final int X_FIELD=1;
			final int Y_FIELD=2;
			final int A_FIELD=3;
			waypointsCount = b.get(COUNT_FIELD).asInt();
			for (int i=0; i<waypointsCount; i++)
			{
				waypoints.add(new Coordinate(b.get(COUNT_FIELD+i*FIELDS+X_FIELD).asDouble(),
											 b.get(COUNT_FIELD+i*FIELDS+Y_FIELD).asDouble(),
											 b.get(COUNT_FIELD+i*FIELDS+A_FIELD).asDouble()));
			}
			updated=true;
		} // WAYPOINTS
	}
}
