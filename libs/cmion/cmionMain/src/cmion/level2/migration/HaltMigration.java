package cmion.level2.migration;

import ion.Meta.Request;

public class HaltMigration extends Request {

	public final Object lockingObject;
	
	public HaltMigration(Object lockingObject){
		this.lockingObject = lockingObject;
	}
}
