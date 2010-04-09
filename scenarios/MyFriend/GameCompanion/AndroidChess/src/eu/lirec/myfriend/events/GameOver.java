package eu.lirec.myfriend.events;

import eu.lirec.myfriend.Piece.Colour;
import ion.Meta.Event;

public class GameOver extends Event {

	public final Colour winner;
	
	public GameOver(Colour winner){
		this.winner = winner;
	}
}
