package eu.lirec.myfriend.requests;

import eu.lirec.myfriend.PlayedGame;
import ion.Meta.Request;

public class RecallGame extends Request {
	
	public final PlayedGame game;
	
	public RecallGame(PlayedGame game) {
		this.game = game;
	}
}
