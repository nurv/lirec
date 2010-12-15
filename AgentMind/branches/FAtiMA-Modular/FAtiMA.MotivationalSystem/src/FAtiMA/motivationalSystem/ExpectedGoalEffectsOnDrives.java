package FAtiMA.motivationalSystem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class ExpectedGoalEffectsOnDrives implements Serializable{
	
	private String _goalName;
	private ArrayList<EffectOnDrive> _effects;
	
	public ExpectedGoalEffectsOnDrives(String goalName)
	{
		this._goalName = goalName;
		_effects = new ArrayList<EffectOnDrive>();
	}
	
	public String getGoalName()
	{
		return _goalName;
	}
	
	public void AddEffect(EffectOnDrive e)
	{
		_effects.add(e);
	}
	
	public Collection<EffectOnDrive> getEffects()
	{
		return _effects;
	}

}
