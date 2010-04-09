package eu.lirec.myfriend.events;

import ion.Meta.Request;

public class Failed extends Ended {

	public Failed() {
	}
	
	public Failed(Request request){
		super(request);
	}
}
