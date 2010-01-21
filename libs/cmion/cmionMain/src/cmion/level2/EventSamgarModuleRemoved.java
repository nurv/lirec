package cmion.level2;

import cmion.architecture.CmionEvent;

/** This event is raised by the SamgarConnector in the add ons package, when 
  * a module disappears from the Samgar World */
public class EventSamgarModuleRemoved extends CmionEvent 
{
	/** the description of the module that was removed */
	private SamgarModuleInfo moduleInfo;
	
	public EventSamgarModuleRemoved(SamgarModuleInfo modInfo)
	{
		moduleInfo = modInfo;
	}
	
	/** returns information about the samgar module that was removed */
	public SamgarModuleInfo getModuleInfo()
	{
		return moduleInfo;
	}
	
}
