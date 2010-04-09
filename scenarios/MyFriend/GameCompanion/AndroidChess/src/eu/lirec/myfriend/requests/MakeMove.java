package eu.lirec.myfriend.requests;

import eu.lirec.myfriend.Location;
import eu.lirec.myfriend.Piece;
import ion.Meta.Request;

public class MakeMove extends Request {
	
	public final Location from;
	public final Location to;
	public final Piece replacement;

	public MakeMove(Location from, Location to){
		this.from = from;
		this.to = to;
		this.replacement = null;
	}
	
	public MakeMove(Location from, Location to, Piece replacement){
		this.from = from;
		this.to = to;
		this.replacement = replacement;
	}
}
