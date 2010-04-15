package cmion.level2.migration;

import ion.Meta.Request;

public class ResumeMigration extends Request {

	public final Object lockingObject;
	
	public ResumeMigration(Object lockingObject){
		this.lockingObject = lockingObject;
	}
}
