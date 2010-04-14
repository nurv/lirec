package cmion.level2.migration;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import cmion.level2.competencies.Migration;

public class MigrationReceivedHandler extends EventHandler {

	private Migration competency;
	private Migrating component;
	
	public MigrationReceivedHandler(Migration competency, Migrating component){
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
	
	public Migrating getComponent(){
		return this.component;
	}

}
