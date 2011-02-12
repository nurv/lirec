package FAtiMA.motivationalSystem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import FAtiMA.Core.wellFormedNames.Name;

public class ActionEffectsOnDrives implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private Name _actionName;
	private ArrayList<EffectOnDrive> _effects;
	
	public ActionEffectsOnDrives(String actionName)
	{
		this._actionName = Name.ParseName(actionName);
		_effects = new ArrayList<EffectOnDrive>();
	}
	
	public Name getActionName()
	{
		return _actionName;
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
