import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

/*
 * Created on 4/Fev/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author João Dias
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class sObject {
	
	private static int buffsize = 250;
	
	private ArrayList<Property> _properties;
	private String _objectName;
	
	public sObject(String name) {
		_properties = new ArrayList<Property>();
		_objectName = name;
	}
	
	public String Name() {
		return _objectName;
	}
	
	public void AddProperty(Property p) {
		_properties.add(p);
	}
	
	public String GetPropertiesList() {
		String properties = "";
		
		for(Property p : _properties)
		{
			properties = properties + p.GetName() + ":" + p.GetValue() + " "; 
		}
		
		
		return properties;
	}
	
	public static sObject ParseFile(String name) {
		sObject obj = new sObject(name);
		byte[] buffer = new byte[buffsize];
		String data = "";
		int readCharacters;
		
		try {
			FileInputStream f = new FileInputStream(name + ".txt");
			while((readCharacters=f.read(buffer))>0) {
				data = data + new String(buffer,0,readCharacters);
			}
			ParseData(obj, data);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return obj;
	}
	
	private static void ParseData(sObject obj, String data) {
		StringTokenizer st;
		
		st = new StringTokenizer(data,"\r\n");
		while(st.hasMoreTokens()) {
			ParseLine(obj, st.nextToken());
		}
	}
	
	private static void ParseLine(sObject obj, String line) {
		StringTokenizer st;
		
		st = new StringTokenizer(line," ");
		obj.AddProperty(new Property(st.nextToken(),st.nextToken()));
	}
}
