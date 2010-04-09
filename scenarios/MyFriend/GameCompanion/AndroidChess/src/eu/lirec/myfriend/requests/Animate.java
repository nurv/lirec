package eu.lirec.myfriend.requests;

import eu.lirec.myfriend.competences.Manager.AnimationIntent;
import ion.Meta.Request;

public class Animate extends Request {

	public final AnimationIntent intent;
	
	public Animate(AnimationIntent intent){
		this.intent = intent;
	}
}
