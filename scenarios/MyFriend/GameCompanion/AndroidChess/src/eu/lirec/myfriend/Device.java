package eu.lirec.myfriend;

public class Device {

	private String name;
	private String host;
	private int port;
	private boolean isAvailable;
	
	public Device(String name, String host, int port){
		this.name = name;
		this.host = host;
		this.port = port;
	}
	
	public boolean isAvailable(){
		return isAvailable;
	}

	public String getName() {
		return name;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
}
