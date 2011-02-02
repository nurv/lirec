package cmion.addOns.samgar.playerProxy;

import cmion.addOns.samgar.playerProxy.Coordinate;

public class Hypoth implements Comparable<Hypoth> {
	public Coordinate coord;
	public double coeff; // point coeficence;
	
	public Hypoth()
	{
		coord = new Coordinate();
		coeff = 0;
	}
	
	public Hypoth(Coordinate newCoord)
	{
		coord = newCoord;
		coeff = 1;
	}	
	
	public Hypoth(Coordinate newCoord, double newCoeff)
	{
		coord = newCoord;
		coeff = newCoeff;
	}
	
	public void set(Coordinate newCoord)
	{
		coord = newCoord;
		coeff = 1;
	}	
	
	public void set(Coordinate newCoord, double newCoeff)
	{
		coord = newCoord;
		coeff = newCoeff;
	}

	@Override
	public int compareTo(Hypoth o) {
        return Double.compare(coeff, o.coeff);
	}
}
