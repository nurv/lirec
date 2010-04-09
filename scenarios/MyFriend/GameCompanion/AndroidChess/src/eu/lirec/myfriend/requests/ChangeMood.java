package eu.lirec.myfriend.requests;

import ion.Meta.Request;

public class ChangeMood extends Request {
	
	public final double mood;
	
	public ChangeMood(double mood) {
		this.mood = mood;
	}

}
