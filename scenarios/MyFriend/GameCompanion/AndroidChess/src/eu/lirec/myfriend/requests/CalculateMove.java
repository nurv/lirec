package eu.lirec.myfriend.requests;

import eu.lirec.myfriend.ChessEngineCallback;
import eu.lirec.myfriend.Piece;
import eu.lirec.myfriend.Piece.Colour;
import ion.Meta.Request;

public class CalculateMove extends Request {
	
	public final Piece[][] board;
	public final Colour pieceToMove;
	public final ChessEngineCallback callback;
	
	public CalculateMove(Piece[][] board, Colour pieceToMove, ChessEngineCallback callback) {
		this.board = board;
		this.pieceToMove = pieceToMove;
		this.callback = callback;
	}

}
