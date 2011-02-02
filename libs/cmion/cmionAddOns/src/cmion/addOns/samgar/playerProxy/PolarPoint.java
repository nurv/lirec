package cmion.addOns.samgar.playerProxy;

public class PolarPoint {
	public double range;
	public double angle;
	
	public PolarPoint()
	{
		range = 0;
		angle = 0;
	}
	
	public PolarPoint(final double newRange, final double newAngle)
	{
		range = newRange;
		angle = newAngle;
	}
	
	public void set(final double newRange, final double newAngle)
	{
		range = newRange;
		angle = newAngle;
	}
}
