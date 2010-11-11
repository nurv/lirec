package FAtiMA.motivationalSystem;

import java.util.ArrayList;
import java.util.Collection;

import FAtiMA.Core.wellFormedNames.Name;

public class ActionEffectsOnDrives {
	
	private Name _actionName;
	private ArrayList<EffectOnDrive2> _effects;
	
	public ActionEffectsOnDrives(String actionName)
	{
		this._actionName = Name.ParseName(actionName);
		_effects = new ArrayList<EffectOnDrive2>();
	}
	
	public Name getActionName()
	{
		return _actionName;
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
