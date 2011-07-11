package cmion.level2.migration;

import ion.Meta.Simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/** a thread for listening on the invite listen port for invitations */
public class InviteListener extends Thread{
	
	/** the port we listen for invites on*/
	private int port;
	
	/** constructs a new InviteListener that listens on the specified port*/
	public InviteListener(int port)
	{
		this.port = port;
	}

	/** main method of the listener*/
	public void run()
	{
		// if no valid port was specified we won't listen
		if (port <= 0) return;
		while (true)
		{
			try {
				// listen for invites on the specified port
				ServerSocket ss = new ServerSocket(port);
				Socket s = ss.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				// the only message we are expecting is the device name of the inviter followed by a line break
				String inviter = in.readLine();
				in.close();
				s.close();
				// raise an event that we have been invited, this event will be handled in the Migration class
				Simulation.instance.raise(new IncomingInvite(inviter));
			} catch (IOException e) {}
		}
	}

}
