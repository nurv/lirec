package cmion.level2.migration;

import cmion.level2.competencies.Migration;
import ion.Meta.EventHandler;
import ion.Meta.IEvent;

public class MigrationSucceededHandler extends EventHandler {

	private Migration competency;
	private Migrating component;
	
	public MigrationSucceededHandler(Migration competency, Migrating component){
		super(MigrationComplete.class);
		this.competency = competency;
		this.component = component;
	}
	
	@Override
	public void invoke(IEvent evt) {
		component.onMigrationSuccess();
	}
	
	public Migration getCompetency(){
		return this.competency;
	}
	
	public Migrating getComponent(){
		return this.component;
	}

}
