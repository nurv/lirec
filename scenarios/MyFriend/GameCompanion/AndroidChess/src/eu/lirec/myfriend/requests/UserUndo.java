package eu.lirec.myfriend.requests;

import ion.Meta.Request;

public class UserUndo extends Request {
	
	public final int moves;
	
	public UserUndo(){
		this.moves = 2;
	}
	
	public UserUndo(int moves){
		this.moves = moves;
	}

}
