package eu.lirec.myfriend.events;

import ion.Meta.Event;

public class UndoAvailable extends Event {
	
	public final int moves;
	
	public UndoAvailable(){
		this.moves = -1;
	}

}
