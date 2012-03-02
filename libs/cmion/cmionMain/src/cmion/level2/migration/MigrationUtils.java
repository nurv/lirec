package cmion.level2.migration;

import cmion.level2.competencies.Migration;
import ion.Meta.Element;
import ion.Meta.EventHandler;
import ion.Meta.Simulation;

public class MigrationUtils {

	public static Migration getMigration(Simulation simulation){
		for(Element element : simulation.getElements()){
			if(element instanceof Migration){
				return (Migration) element;
			}
		}
		
		return null;
	}
	
	public static void registerAllComponents(){
		registerAllComponents(Simulation.instance);
	}
	
	public static void registerAllComponents(Simulation simulation){
		registerMigratingComponents(simulation);
		registerMigrationAwareComponents(simulation);
	}
	
	public static void registerMigratingComponents(){
		registerMigratingComponents(Simulation.instance);
	}
	
	public static void registerMigratingComponents(Simulation simulation){

		Migration competency = getMigration(simulation);
		registerMigratingComponents(simulation, competency);

	}
	
	public static void registerMigratingComponents(Migration competency){
		registerMigratingComponents(Simulation.instance, competency);
	}
	
	public static void registerMigratingComponents(Simulation simulation, Migration competency){
		
		for(Element migratingElement : simulation.getElements()){
			if(migratingElement instanceof Migrating){
				Migrating component = (Migrating) migratingElement;
				
				registerMigratingComponent(competency, component);
			}
		}
	}
	
	public static void registerMigratingComponent(Migration competency, Migrating component){

		if (competency!= null)
		{
			EventHandler handler = new SaveStateHandler(competency, component);
			competency.getEventHandlers().add(handler);

			handler = new RestoreStateHandler(competency, component);
			competency.getEventHandlers().add(handler);
		}
	}
	
	public static void registerMigrationAwareComponents(){
		registerMigrationAwareComponents(Simulation.instance);
	}
	
	public static void registerMigrationAwareComponents(Simulation simulation){

		Migration competency = getMigration(simulation);
		registerMigrationAwareComponents(simulation, competency);

	}
	
	public static void registerMigrationAwareComponents(Migration competency){
		registerMigrationAwareComponents(Simulation.instance, competency);
	}
	
	public static void registerMigrationAwareComponents(Simulation simulation, Migration competency){
		
		for(Element migratingElement : simulation.getElements()){
			if(migratingElement instanceof MigrationAware){
				MigrationAware component = (MigrationAware) migratingElement;
				
				registerMigrationAwareComponent(competency, component);
			}
		}
	}
	
	public static void registerMigrationAwareComponent(Migration competency, MigrationAware component){
		if (competency!= null)
		{
			EventHandler handler = new MigrationReceivedHandler(competency, component);
			competency.getEventHandlers().add(handler);

			handler = new SendingMigrationHandler(competency, component);
			competency.getEventHandlers().add(handler);

			handler = new MigrationSucceededHandler(competency, component);
			competency.getEventHandlers().add(handler);

			handler = new MigrationFailedHandler(competency, component);
			competency.getEventHandlers().add(handler);
		}
	}
	
	public static void haltMigration(Object lockingObject){
		haltMigration(getMigration(Simulation.instance), lockingObject);
	}
	
	public static void haltMigration(Migration competency, Object lockingObject){
		competency.schedule(new HaltMigration(lockingObject));
	}
	
	public static void resumeMigration(Object lockingObject){
		resumeMigration(getMigration(Simulation.instance), lockingObject);
	}
	
	public static void resumeMigration(Migration competency, Object lockingObject){
		competency.schedule(new ResumeMigration(lockingObject));
	}
	
	public static void addMigrationData(org.w3c.dom.Element data){
		addMigrationData(getMigration(Simulation.instance), data);
	}
	
	public static void addMigrationData(Migration competency, org.w3c.dom.Element data){
		competency.addMigrationData(data);
	}
}
