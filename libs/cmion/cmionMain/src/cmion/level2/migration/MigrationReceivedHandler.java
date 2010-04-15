package cmion.level2.migration;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import cmion.level2.competencies.Migration;

public class MigrationReceivedHandler extends EventHandler {

	private Migration competency;
	private MigrationAware component;
	
	public MigrationReceivedHandler(Migration competency, MigrationAware component){
		super(IncomingMigration.class);
		this.competency = competency;
		this.component = component;
	}
	
	@Override
	public void invoke(IEvent evt) {
		component.onMigrationIn();
	}
	
	public Migration getCompetency(){
		return this.competency;
	}
	
	public MigrationAware getComponent(){
		return this.component;
	}

}
