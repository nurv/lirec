package cmion.level2.migration;

public interface MigrationAware {

	public void onMigrationIn();
	
	public void onMigrationOut();
	
	public void onMigrationSuccess();
	
	public void onMigrationFailure();
	
}
