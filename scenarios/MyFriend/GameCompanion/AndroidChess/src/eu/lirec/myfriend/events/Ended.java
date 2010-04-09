package eu.lirec.myfriend.events;

import ion.Meta.Event;
import ion.Meta.Request;

public class Ended extends Event {

	public final Request request;
	
	public Ended(){
		this.request = null;
	}
	
	public Ended(Request request){
		this.request = request;
	}
}
