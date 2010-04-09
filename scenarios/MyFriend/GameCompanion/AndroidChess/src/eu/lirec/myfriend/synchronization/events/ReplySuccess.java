package eu.lirec.myfriend.synchronization.events;

import eu.lirec.myfriend.synchronization.requests.Reply;
import ion.Meta.Event;

public class ReplySuccess extends Event {

	public final Reply reply;
	
	public ReplySuccess(Reply reply) {
		this.reply = reply;
	}
}
