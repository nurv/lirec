import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;

import Language.LanguageEngine;


public class LanguageServerSlave extends SocketListener {
	
	private LanguageEngine agentLanguageEngine;
	private LanguageEngine userLanguageEngine;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length < 4) 
		{
			return;
		}
		
		try {
			LanguageServerSlave server = new LanguageServerSlave(Integer.parseInt(args[0]),args[1],args[2],args[3],args[4]);
			server.start();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public LanguageServerSlave(int port, String agentSex, String userSex, String agentLanguageFile, String userLanguageFile) throws Exception
	{
		String agentFileName = "data/characters/minds/" + agentLanguageFile;
		System.out.println("Initializing Agent Language Engine(ALE)... ");
		System.out.println("Language File: " + agentFileName);
		System.out.println("Sex: " + agentSex);
		System.out.println("Role: Victim");
		this.agentLanguageEngine = new LanguageEngine("name",agentSex,"Victim",new File(agentFileName));
		System.out.println("Finished ALE initialization!");
		
		String userFileName = "data/characters/minds/" + userLanguageFile;
		System.out.println("Initializing User Language Engine(ULE)... ");
		System.out.println("Language File: " + userFileName);
		System.out.println("Sex: " + userSex);
		System.out.println("Role: User");
		this.userLanguageEngine = new LanguageEngine("name",userSex,"User",new File(userFileName));
		System.out.println("Finished ULE initialization!");
		
		System.out.println("Connecting to localhost:" + port);
		this.socket = new Socket(InetAddress.getLocalHost(), port);
	}
	
	/**
	 * Process a message received in the socket from the virtual world
	 * @param data - the data received from the socket
	 */
	public void processData(byte[] data) {
		String msg;
		StringTokenizer st;
		
		try
		{
			msg = new String(data,"UTF-8");
		}
		catch(UnsupportedEncodingException ex)
		{
			msg = new String(data);
		}
	
		st = new StringTokenizer(msg,"\n");
		while(st.hasMoreTokens()) {
			processMessage(st.nextToken());
		}
	}
	
	public void processMessage(String message)
	{
		String outcome = null;
		StringTokenizer st = new StringTokenizer(message," ");
		String method = st.nextToken();
		
		String speech = "";
		
		while(st.hasMoreTokens())
		{
			speech = speech + st.nextToken() + " ";
		}
		
		
		//processing language engine request:
		try
		{
			if(method.equals("Say"))
			{
				System.out.println("Generating SpeechAct:" + speech);
				outcome = this.agentLanguageEngine.Say(speech);
			}
			else if(method.equals("Input"))
			{
				System.out.println("");
				System.out.println("Input received from user: " + speech);
				outcome = this.userLanguageEngine.Input(speech);
			}
			else if(method.equals("Narrate"))
			{
				System.out.println("Narrating AM:" + speech);
				outcome = this.agentLanguageEngine.Narrate(speech);
			}
			else if(method.equals("Kill"))
			{
				System.out.println("Receiving a kill order, ...dying!");
				this.stoped = true;
				return;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			outcome = null;
		}
		
		System.out.println(outcome);
		//sending output
		Send(outcome);
	}
	
	protected boolean Send(String msg) {
		try {
			String aux = msg + "\n";
			OutputStream out = this.socket.getOutputStream();
			out.write(aux.getBytes("UTF-8"));
			out.flush();
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			this.stoped = true;
			return false;
		}
	}
}
