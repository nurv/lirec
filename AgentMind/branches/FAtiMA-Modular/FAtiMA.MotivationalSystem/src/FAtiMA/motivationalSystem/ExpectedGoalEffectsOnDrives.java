package FAtiMA.motivationalSystem;

import java.util.ArrayList;
import java.util.Collection;

public class ExpectedGoalEffectsOnDrives {
	
	private String _goalName;
	private ArrayList<EffectOnDrive2> _effects;
	
	public ExpectedGoalEffectsOnDrives(String goalName)
	{
		this._goalName = goalName;
		_effects = new ArrayList<EffectOnDrive2>();
	}
	
	public String getGoalName()
	{
		return _goalName;
	}
	
	public void AddEffect(EffectOnDrive2 e)
	{
		_effects.add(e);
	}
	
	public Collection<EffectOnDrive2> getEffects()
	{
		return _effects;
	}

}
