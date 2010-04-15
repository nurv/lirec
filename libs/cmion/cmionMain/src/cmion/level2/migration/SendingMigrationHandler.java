package cmion.level2.migration;

import cmion.level2.competencies.Migration;
import ion.Meta.EventHandler;
import ion.Meta.IEvent;

public class SendingMigrationHandler extends EventHandler {

	private Migration competency;
	private MigrationAware component;
	
	public SendingMigrationHandler(Migration competency, MigrationAware component){
		super(MigrationStart.class);
		this.competency = competency;
		this.component = component;
	}
	
	@Override
	public void invoke(IEvent evt) {
		component.onMigrationOut();
	}

	public Migration getCompetency(){
		return this.competency;
	}
	
	public MigrationAware getComponent(){
		return this.component;
	}
}
