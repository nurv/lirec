package FAtiMA.motivationalSystem;

import java.io.Serializable;

import FAtiMA.Core.wellFormedNames.Symbol;



public class EffectOnDrive implements Serializable {
	
	private float _value;
	private short _type;
	private String _driveName;
	private Symbol _target;
	
	public EffectOnDrive(Short type, String driveName, Symbol target, float value)
	{
		this._type = type;
		this._target = target;
		this._value = value;
		this._driveName = driveName;
	}
	
	public short getType()
	{
		return this._type;
	}
	
	public Symbol getTarget()
	{
		return this._target;
	}
	
	public float getValue()
	{
		return this._value;
	}
	
	public String getDriveName()
	{
		return this._driveName;
	}

}
