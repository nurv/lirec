package cmion.addOns.samgar.playerProxy;

public class Coordinate{
	public double x;
	public double y;
	public double a;
	
	public Coordinate()
	{
		x = 0;
		y = 0;
		a = 0;
	}
	
	public Coordinate(final double newX, final double newY, final double newA)
	{
		x = newX;
		y = newY;
		a = newA;
	}
	
	public void set(final double newX, final double newY, final double newA)
	{
		x = newX;
		y = newY;
		a = newA;
	}
	
	public static double distance(Coordinate X, Coordinate Y)
	{
		return Math.sqrt(Math.pow(X.x-Y.x,2) + Math.pow(X.y - Y.y,2));
	}
}