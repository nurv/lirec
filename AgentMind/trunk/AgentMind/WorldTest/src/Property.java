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
public class Property {
	
	String _name;
	String _value;
	
	public Property(String name, String value) {
		_name = name;
		_value = value;
	}
	
	public String GetName() {
		return _name;
	}
	
	public String GetValue() {
		return _value;
	}
	
	public void SetValue(String newValue) {
		_value = newValue;
	}
}
