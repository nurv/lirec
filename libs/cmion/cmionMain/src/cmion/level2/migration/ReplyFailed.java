package cmion.level2.migration;

import ion.Meta.Event;

public class ReplyFailed extends Event {

	public final Reply reply;
	
	public ReplyFailed(Reply reply) {
		this.reply = reply;
	}
}
