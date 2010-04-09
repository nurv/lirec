package eu.lirec.myfriend.competences;

import eu.lirec.myfriend.events.Ended;
import ion.Meta.Element;
import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Request;

public class EndListener extends EventHandler {

	private OnCompetenceCompletion callback;
	private Element competence;
	private Request request;
	private Ended endedEvent;
	
	public EndListener(OnCompetenceCompletion callback, Element competence) {
		super(Ended.class);
		this.callback = callback;
		this.competence = competence;
		this.competence.getEventHandlers().add(this);
	}
	
	public EndListener(OnCompetenceCompletion callback, Element competence, Request request) {
		this(callback, competence);
		this.request = request;
	}
	
	@Override
	public void invoke(IEvent evt) {
		endedEvent = (Ended) evt;
		
		if(this.request != null && endedEvent != null 
				&& !this.request.equals(endedEvent.request)){
			return;
		}
		
		this.callback.competenceCompleted(this);
		this.competence.getEventHandlers().remove(this);
	}
	
	public Ended getEndedEvent(){
		return this.endedEvent;
	}
	
	public Element getCompetence(){
		return this.competence;
	}

}
