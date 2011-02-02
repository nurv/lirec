package cmion.addOns.samgar.playerProxy;

public class Point {
	public double x;
	public double y;
	
	public Point()
	{
		x = 0;
		y = 0;
	}
	
	public Point(final double newX, final double newY)
	{
		x = newX;
		y = newY;
	}
	
	public void set(final double newX, final double newY)
	{
		x = newX;
		y = newY;
	}
	
	public static double distance(Point X, Point Y)
	{
		return Math.sqrt(Math.pow(X.x-Y.x,2) + Math.pow(X.y - Y.y,2));
	}
}
