package cmion.emysTK;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SendCommandThread extends Thread {

	private String host;
	private int port;
	private String command;
	private String parameters;
	
	public SendCommandThread(String host, int port, String command,
			String parameters) {
		this.host = host;
		this.port = port;
		this.command = command;
		this.parameters = parameters;
	}

	@Override
	public void run()
	{
		boolean sent = false;
		while (!sent)
		{
			String msg = command + " " + parameters;
			msg = msg.trim();
			msg += "$";
			try {
				Socket s = new Socket(host,port);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
				writer.write(msg);
				writer.flush();
				s.close();
				sent = true;
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return;
			} catch (IOException e) {
				System.err.println("ERRROR! host = "+host +", port = "+port);
				System.err.println(msg);
				e.printStackTrace();
			}
		}		
	}
	
}
