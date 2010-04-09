package eu.lirec.myfriend.requests;

import eu.lirec.myfriend.competences.Manager.SayIntent;
import ion.Meta.Request;

public class Say extends Request {

	public final SayIntent intent;
	public final String text;

	public Say(String text) {
		this.text = text;
		this.intent = SayIntent.Text;
	}
	
	public Say(SayIntent intent){
		this.text = null;
		this.intent = intent;
	}
}
