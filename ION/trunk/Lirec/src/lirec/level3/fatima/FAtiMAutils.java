package lirec.level3.fatima;

import java.util.ArrayList;
import java.util.StringTokenizer;

import lirec.level3.MindAction;
import lirec.storage.LirecStorageContainer;

/** a collection of utility methods for handling the communication via the FAtiMA
 *  protocol */
public class FAtiMAutils {
	
	/** returns a String listing all properties of a storage container in the format
	 * specified by the FAtiMA protocol, which is for example "prop1:value1 prop2:value2 prop3:value3" */
	public static String getPropertiesString(LirecStorageContainer entityContainer) {
		String propString="";
		for (String propName : entityContainer.getPropertyNames())
		{
			propString += " " + propName + ":" + entityContainer.getPropertyValue(propName);
		}
		return propString;
	}
	
	/** returns a FAtiMA message that represents the provided mind action */
	public static String mindActiontoFatimaMessage(MindAction mindAction)
	{
		String result = mindAction.getName();
		for (String parameter : mindAction.getParameters()) result += " " + parameter;
		return result;
	}
	
	/** creates a new mindAction out of a FAtiMA string describing that action */
	public static MindAction fatimaMessageToMindAction(String subject,String msg)
	{
		if (msg.trim().equals("")) return null;
		StringTokenizer st = new StringTokenizer(msg);
		String name = st.nextToken();
		ArrayList<String> parameters = new ArrayList<String>();
		while (st.hasMoreTokens()) parameters.add(st.nextToken());	
		return new MindAction(subject,name,parameters);
	}
	

}
