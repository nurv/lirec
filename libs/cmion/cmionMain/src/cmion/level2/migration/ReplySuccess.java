package cmion.level2.migration;

import ion.Meta.Event;

public class ReplySuccess extends Event {

	public final Reply reply;
	
	public ReplySuccess(Reply reply) {
		this.reply = reply;
	}
}
