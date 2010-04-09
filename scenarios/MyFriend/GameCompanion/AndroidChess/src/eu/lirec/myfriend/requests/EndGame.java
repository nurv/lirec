package eu.lirec.myfriend.requests;

import eu.lirec.myfriend.Piece.Colour;
import ion.Meta.Request;

public class EndGame extends Request {

	public final Colour winner;
	
	public EndGame() {
		winner = null;
	}
	
	public EndGame(Colour winner){
		this.winner = winner;
	}
}
