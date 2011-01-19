package FAtiMA.Core.emotionalState;


public class EmotionType {
	
	protected String _name;
	protected String[] _appraisalVariables;
	protected byte _valence;
	
	public EmotionType(String name,  String[] appraisalVariables, byte valence)
	{
		this._name = name;
		this._appraisalVariables = appraisalVariables;
		this._valence = valence;
	}
	
	
	public String getName() 
	{
		return _name;
	}
	
	public byte getValence()
	{
		return _valence;
	}
	
	public String[] getAppraisalVariables() {
		return _appraisalVariables;
	}
	
	public boolean equals(EmotionType type)
	{
		return _name.equals(type._name);
	}
}
