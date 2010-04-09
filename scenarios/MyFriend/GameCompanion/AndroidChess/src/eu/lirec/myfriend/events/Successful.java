package eu.lirec.myfriend.events;

import ion.Meta.Request;

public class Successful extends Ended {

	public Successful(){
	}
	
	public Successful(Request request){
		super(request);
	}
}
