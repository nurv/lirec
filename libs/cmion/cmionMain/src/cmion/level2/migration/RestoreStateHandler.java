package cmion.level2.migration;

import cmion.level2.competencies.Migration;
import ion.Meta.EventHandler;
import ion.Meta.IEvent;

public class RestoreStateHandler extends EventHandler {

	private Migration competency;
	private Migrating component;
	
	public RestoreStateHandler(Migration competency, Migrating component){
		super(MessageReceived.class);
		this.competency = competency;
		this.component = component;
	}
	
	@Override
	public void invoke(IEvent evt) {
		MessageReceived messageReceived = (MessageReceived) evt;
		if(messageReceived.type.equals(component.getMessageTag())){
			component.restoreState(messageReceived.message);
		}
	}
	
	public Migration getCompetency(){
		return this.competency;
	}
	
	public Migrating getComponent(){
		return this.component;
	}

}
