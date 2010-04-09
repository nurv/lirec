package eu.lirec.myfriend.events;

import eu.lirec.myfriend.ChessBoard;
import ion.Meta.Event;

public class BoardChanged extends Event {
	
	public final ChessBoard newBoard;
	public final ChessBoard oldBoard;
	
	public BoardChanged(ChessBoard newBoard, ChessBoard oldBoard){
		this.newBoard = newBoard;
		this.oldBoard = oldBoard;
	}

}
