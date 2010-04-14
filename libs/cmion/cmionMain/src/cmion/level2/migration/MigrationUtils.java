package cmion.level2.migration;

import cmion.level2.competencies.Migration;
import ion.Meta.Element;
import ion.Meta.EventHandler;
import ion.Meta.Simulation;

public class MigrationUtils {

	public static void registerAllComponents(Simulation simulation){
		
		for(Element element : simulation.getElements()){
			if(element instanceof Migration){
				Migration competency = (Migration) element;
				
				registerAllComponents(simulation, competency);
				
				break;
			}
		}
		
	}
	
	public static void registerAllComponents(Simulation simulation, Migration competency){
		
		for(Element migratingElement : simulation.getElements()){
			if(migratingElement instanceof Migrating){
				Migrating component = (Migrating) migratingElement;
				
				registerComponent(competency, component);
			}
		}
	}
	
	public static void registerComponent(Migration competency, Migrating component){
		EventHandler handler = new SaveStateHandler(competency, component);
		competency.getEventHandlers().add(handler);
		
		handler = new RestoreStateHandler(competency, component);
		competency.getEventHandlers().add(handler);
		
		handler = new MigrationReceivedHandler(competency, component);
		competency.getEventHandlers().add(handler);
		
		handler = new MigrationSucceededHandler(competency, component);
		competency.getEventHandlers().add(handler);
		
		handler = new MigrationFailedHandler(competency, component);
		competency.getEventHandlers().add(handler);
	}
}
