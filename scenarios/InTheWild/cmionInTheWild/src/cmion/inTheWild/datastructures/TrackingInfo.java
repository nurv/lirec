package cmion.inTheWild.datastructures;

/** Kinect Tracking info for a single user */
public class TrackingInfo 
{

	// coordinates
	private double x;
	private double y; 
	private double z;
	
	
	public TrackingInfo(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
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
}
