package eu.lirec.myfriend.synchronization.events;

import eu.lirec.myfriend.synchronization.requests.Reply;
import ion.Meta.Event;

public class ReplyFailed extends Event {

	public final Reply reply;
	
	public ReplyFailed(Reply reply) {
		this.reply = reply;
	}
}
