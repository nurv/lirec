package eu.lirec.myfriend.events;

import eu.lirec.myfriend.ChessBoard;
import eu.lirec.myfriend.Location;

public class MovePlayed extends BoardChanged {
	
	public final Location from;
	public final Location to;

	public MovePlayed(Location from, Location to, ChessBoard newBoard, ChessBoard oldBoard) {
		super(newBoard, oldBoard);
		this.from = from;
		this.to = to;
	}

}
