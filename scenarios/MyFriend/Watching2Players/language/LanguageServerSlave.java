import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;


import Language.LanguageEngine;


public class LanguageServerSlave extends SocketListener {
	
	private LanguageEngine language;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length < 1) 
		{
			return;
		}
		
		try {
			LanguageServerSlave server = new LanguageServerSlave(Integer.parseInt(args[0]),new String("M"),new String("M"),new String("c:/PROGRA~1/OPPR/src/iCatchess/language/language-set-1/"));
			server.start();
			
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
/*		String outcome = null;
		String speechAct = null;
		LanguageEngine ln = null;
		
		try {
		ln = new LanguageEngine("name","M","M",new File("language-set-1/"));
		//ln = new LanguageEngine("name","M","M",new File("PROGRA~1/OPPR/src/iCatchess/language/language-set-1/"));
			
		LanguageEngine.debug = true;
		
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		try {
			FileReader fr = new FileReader("SpeechAct.xml");
			BufferedReader in = new BufferedReader(fr);
			speechAct = in.readLine();
		} catch (IOException e) { 
	        // catch io errors from FileInputStream or readLine() 
	        System.out.println("IOException error!" + e.getMessage()); 
		}
		
		String newSpeechAct = processAgreement(speechAct);
		
		try {
		outcome = ln.Say(newSpeechAct);
		System.out.println(outcome);
		} catch (Exception e)
		{
			e.printStackTrace();
		}*/
	}
	
	
	public static String processAgreement(String speechAct) {
		String s = new String();
		
		if (speechAct.indexOf("<Context id=\"number\">")>0) {	
			
			//to obtain number agreement
			String aux[] = speechAct.split("number\">");
			String number_str[] = aux[1].split("<");
			int number = Integer.parseInt(number_str[0]);
			
			aux = speechAct.split("</SpeechAct>");
			if (number>1){ //add agreement plural
				s = aux[0]+"<Context id=\"agr_number\">pl</Context></SpeechAct>";
				//System.out.println(s);
				return s;
			} else { //add agreement singular
				s = aux[0]+"<Context id=\"agr_number\">sg</Context></SpeechAct>";
				//System.out.println(s);
				return s;
			}
			}
		return speechAct;
		
	}
	
	public LanguageServerSlave(int port, String sex, String role, String languageActsFile) throws Exception
	{
		//String fileName = "data/characters/minds/" + languageActsFile;
		System.out.println("Initializing Language Engine...");
		System.out.println("Language File: " + languageActsFile);
		System.out.println("Sex: " + sex);
		System.out.println("Role: " + role);
		this.language = new LanguageEngine("name",sex,role,new File(languageActsFile));
		System.out.println("Finished initialization!");
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
			msg = new String(data,"ISO-8859-1");
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
		
		String newSpeechAct = processAgreement(speech);
		
		
		//processing language engine request:
		try
		{
			if(method.equals("Say"))
			{
				outcome = this.language.Say(newSpeechAct);
				
			}
			else if(method.equals("Input"))
			{
				System.out.println("Input received from user: " + speech);
				outcome = this.language.Input(speech);
			}
			else if(method.equals("Narrate"))
			{
				System.out.println("Narrating AM:" + speech);
				outcome = this.language.Narrate(speech);
			}
			else if(method.equals("Kill"))
			{
				this.stoped = true;
				return;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			outcome = null;
		}
		
		//to send only the utterance
		String aux[] = outcome.split("Utterance>");
		System.out.println("----AUX: " + aux[1]);
		String number_str[] = aux[1].split("<");
		outcome = number_str[0];
	
		System.out.println("Outcome :" + outcome);
		//sending output
		Send(outcome);
	}
	
	protected boolean Send(String msg) {
		try {
			String aux = new String (msg + "\n");
			OutputStream out = this.socket.getOutputStream();
			out.write(aux.getBytes("ISO-8859-1"));
		//	out.write(aux.getBytes("ISO8859_1"));
			
			out.flush();
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("encoding exception");
			this.stoped = true;
			return false;
		}
	}
}
