package eu.lirec.myfriend.requests;

import ion.Meta.Request;

public class AcceptMigration extends Request {

	public final Migrate migrationRequest;
	
	public AcceptMigration(Migrate migrationRequest) {
		this.migrationRequest = migrationRequest;
	}
}
