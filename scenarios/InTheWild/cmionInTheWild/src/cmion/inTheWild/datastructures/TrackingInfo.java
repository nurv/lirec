package cmion.inTheWild.datastructures;

/** Kinect Tracking info for a single user or hand */
public class TrackingInfo 
{

	// coordinates
	private double x;
	private double y; 
	private double z;
	
	// user id
	private int userID;
	
	// hand id
	private int handID;
	
	public TrackingInfo(int userID, double x, double y, double z)
	{
		this.userID = userID;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public TrackingInfo(int handID, int userID, double x, double y, double z)
	{
		this(userID,x,y,z);
		this.handID = handID;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public int getUserID() {
		return userID;
	}
	
	public int getHandID() {
		return handID;
	}
}
