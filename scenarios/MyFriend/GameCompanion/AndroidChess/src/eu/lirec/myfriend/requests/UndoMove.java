package eu.lirec.myfriend.requests;

import eu.lirec.myfriend.ChessBoard;
import eu.lirec.myfriend.Location;
import eu.lirec.myfriend.Piece.Colour;
import ion.Meta.Request;

public class UndoMove extends Request {
	
	public final ChessBoard oldBoard;
	public final Location from;
	public final Location to;
	public final Colour oldTurn;
	
	public UndoMove(ChessBoard oldBoard) {
		this.oldBoard = oldBoard;
		this.from = null;
		this.to = null;
		this.oldTurn = null;
	}
	
	public UndoMove(Location from, Location to, Colour oldTurn) {
		this.oldBoard = null;
		this.from = from;
		this.to = to;
		this.oldTurn = oldTurn;
	}
}
