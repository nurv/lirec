package FAtiMA.util;

/*
*  
* A read-only singleton used to get the path of the configuration files 
* author: samuel
*
*/

public class FileLocator {
	private String saveDirectory, actionsFile, goalsFile, personalityFile;
	
	//singleton pattern
	private FileLocator(){}
	private static FileLocator soleInstance = new FileLocator();
	

	private static FileLocator getInstance(){
		return soleInstance;
	}
	
	public static void initialize(String mindPath, String saveDirectory, String actionsFile, String goalsFile, String role){
		getInstance().saveDirectory = saveDirectory;
		getInstance().actionsFile = mindPath + actionsFile + ".xml";
		getInstance().goalsFile = mindPath + goalsFile + ".xml";
		getInstance().personalityFile = mindPath + "roles/" + role + "/" + role + ".xml";
	}
	
	
	public static String getSaveDirectory(){
		return getInstance().saveDirectory;
	}
	
	public static String getActionsFile(){
		return getInstance().actionsFile;
	}
	
	public static String getGoalsFile(){
		return getInstance().goalsFile;
	}
	
	public static String getPersonalityFile(){
		return getInstance().personalityFile;
	}
}
