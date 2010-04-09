package eu.lirec.myfriend;

import ion.Meta.Element;

public class ChessPlayer extends Element {
	
	protected ChessBoard board;
	protected ChessPlayer opponent;

	public ChessPlayer(ChessBoard board, ChessPlayer opponent){
		this.board = board;
		this.opponent = opponent;
	}
	
	public ChessPlayer(ChessBoard board){
		this.board = board;
		this.opponent = null;
	}
	
	public void setOpponent(ChessPlayer opponent){
		this.opponent = opponent;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

	}

}
