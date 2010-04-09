package eu.lirec.myfriend.events;

import eu.lirec.myfriend.ChessBoard;
import eu.lirec.myfriend.Location;
import eu.lirec.myfriend.Piece;

public class PieceEaten extends MovePlayed {
	
	public final Piece piece;
	
	public PieceEaten(Piece piece, Location from, Location to, ChessBoard board, ChessBoard oldBoard) {
		super(from, to, board, oldBoard);
		this.piece = piece;
	}

}
