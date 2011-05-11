package cmion.inTheWild.datastructures;

public class Phoneme 
{

	private String type;
	private double start;
	private double end;
	
	public Phoneme(String type, double start, double end)
	{
		this.type = type;
		this.start = start;
		this.end = end;
	}
	
	public String getType() {
		return type;
	}

	public double getStart() {
		return start;
	}

	public double getEnd() {
		return end;
	}	
	
}
