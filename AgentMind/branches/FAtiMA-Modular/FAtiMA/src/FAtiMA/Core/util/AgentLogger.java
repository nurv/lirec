package FAtiMA.Core.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Samuel
 * 
 */
public class AgentLogger {
	
	private final int LOGGING_INTERVAL = 500; 
	private int _counter = 0;
	private boolean _initialized;
	private BufferedWriter outFile;
	private boolean _debugMode;
	
	/**
	 * Singleton pattern 
	 */
	private static AgentLogger _agentLoggerInstance;
	
	public static AgentLogger GetInstance()
	{
		if(_agentLoggerInstance == null)
		{
			_agentLoggerInstance = new AgentLogger();
		}
		
		return _agentLoggerInstance;
	} 
	
	private AgentLogger(){
		this._initialized = false;
	}
	
	public void initialize(String logName,boolean debugMode) throws IOException{	
		
		if (VersionChecker.runningOnAndroid())
			outFile = new BufferedWriter(new FileWriter("/sdcard/"+logName+"-Log.txt"));		
		else
			outFile = new BufferedWriter(new FileWriter(logName+"-log.txt"));
		
		this._debugMode = debugMode;
		this._initialized = true;
	}

	public void log(String msg) {
		if(!_debugMode) return;
		if(this._initialized){
			try{
				outFile.write("\n"+msg+"\n");
				outFile.flush();
			}catch(IOException e){
				System.out.println("WARNING: IOexception when writing to the log");	
			}   
		}else{
			System.out.println("WARNING: Attempt to write in the log without initializing it first! Nothing will be written");	
		}   
	}
	
	
	public void logAndPrint(String msg) {
		if(!_debugMode) return;
		System.out.println(msg);
		this.log(msg);
	}	

	
	public void close() {
		try{
			outFile.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void intermittentLog(String msg){
		if(!_debugMode) return;
		_counter++;
		if(_counter == 1){
			this.log(msg);	
		}
		if(_counter == LOGGING_INTERVAL){
			_counter = 0;
		}
	}
}
