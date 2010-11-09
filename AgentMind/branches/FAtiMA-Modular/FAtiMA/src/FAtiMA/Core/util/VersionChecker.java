package FAtiMA.Core.util;


/** contains code to check which platform (pc/android) this instance of fatima is running on*/
public class VersionChecker 
{

	/** returns true if this instance of fatima runs on android, else false*/
	public static boolean runningOnAndroid()
	{
        if (System.getProperties().get("java.vm.name").equals("Dalvik")) 
        	return true;
        else
        	return false;
	}
	

	
}
