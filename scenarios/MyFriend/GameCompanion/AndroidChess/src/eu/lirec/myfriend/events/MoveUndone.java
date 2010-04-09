package eu.lirec.myfriend.events;

import eu.lirec.myfriend.ChessBoard;

public class MoveUndone extends BoardChanged {

	public MoveUndone(ChessBoard newBoard, ChessBoard oldBoard) {
		super(newBoard, oldBoard);
	}

}
