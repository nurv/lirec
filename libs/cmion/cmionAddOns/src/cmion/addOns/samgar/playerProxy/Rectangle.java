package cmion.addOns.samgar.playerProxy;

public class Rectangle {
	public double x_min;
	public double x_max;
	public double y_min;
	public double y_max;
	
	public Rectangle()
	{
		x_min = 0;
		x_max = 0;
		y_min = 0;
		y_max = 0;
	}
	
	public Rectangle(final double newXMin, final double newXMax, final double newYMin, final double newYMax)
	{
		x_min = newXMin;
		x_max = newXMax;
		y_min = newYMin;
		y_max = newYMax;
	}
	
	public void set(final double newXMin, final double newXMax, final double newYMin, final double newYMax)
	{
		x_min = newXMin;
		x_max = newXMax;
		y_min = newYMin;
		y_max = newYMax;
	}
	
	public boolean contains(Coordinate point)
	{
		return ((point.x > x_min && point.x < x_max) && (point.y>y_min && point.y<y_max) ); 
	}
}
