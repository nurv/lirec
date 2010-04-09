package eu.lirec.myfriend.synchronization.events;

public class ConnectionFailed extends SynchronizationFailed {
	
	public final String host;
	public final int port;
	
	 public ConnectionFailed(String host, int port) {
		this.host = host;
		this.port = port;
	}

}
