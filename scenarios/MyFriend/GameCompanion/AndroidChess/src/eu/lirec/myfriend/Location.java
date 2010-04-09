package eu.lirec.myfriend;

public class Location {
	
	public final int x;
	public final int y;

	public Location(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean equals(Object o) {

		if(o instanceof Location){
			Location loc = (Location) o;
			
			return loc.x == this.x && loc.y == this.y;
		}

		return false;
	}
}
