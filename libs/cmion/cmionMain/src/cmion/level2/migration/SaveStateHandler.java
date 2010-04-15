package cmion.level2.migration;

import org.w3c.dom.Element;

import cmion.level2.competencies.Migration;
import ion.Meta.EventHandler;
import ion.Meta.IEvent;

public class SaveStateHandler extends EventHandler {

	private Migration competency;
	private Migrating component;
	
	public SaveStateHandler(Migration competency, Migrating component){
		super(MigrationStart.class);
		this.competency = competency;
		this.component = component;
	}
	
	@Override
	public void invoke(IEvent evt) {
		MigrationStart migrationStart = (MigrationStart) evt;
		Element state = component.saveState(migrationStart.document);
		
		if(state != null){
			competency.addMigrationData(state);
		}
	}

	public Migration getCompetency(){
		return this.competency;
	}
	
	public Migrating getComponent(){
		return this.component;
	}
}
