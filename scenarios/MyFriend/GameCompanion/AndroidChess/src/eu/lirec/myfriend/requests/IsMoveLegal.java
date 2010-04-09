package eu.lirec.myfriend.requests;

import eu.lirec.myfriend.ChessBoard;
import eu.lirec.myfriend.ChessEngineCallback;
import eu.lirec.myfriend.Location;
import ion.Meta.Request;

public class IsMoveLegal extends Request {

	public final ChessBoard board;
	public final Location from;
	public final Location to;
	public final ChessEngineCallback callback;
	
	public IsMoveLegal(ChessBoard board, Location from, Location to, ChessEngineCallback callback) {
		this.board = board;
		this.from = from;
		this.to = to;
		this.callback = callback;
	}
}
