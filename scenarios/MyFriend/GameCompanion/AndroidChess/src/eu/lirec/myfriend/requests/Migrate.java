package eu.lirec.myfriend.requests;

import ion.Meta.Request;

public class Migrate extends Request {

	public final String deviceName;
	
	public Migrate(String deviceName){
		this.deviceName = deviceName;
	}
}
