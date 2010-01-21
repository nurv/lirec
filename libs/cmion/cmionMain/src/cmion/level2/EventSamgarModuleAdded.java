package cmion.level2;

import cmion.architecture.CmionEvent;

/** This event is raised by the SamgarConnector in the add ons package, when it
 * finds a new module in the Samgar World */
public class EventSamgarModuleAdded extends CmionEvent 
{
	/** the description of the module that was added */
	private SamgarModuleInfo moduleInfo;
	
	public EventSamgarModuleAdded(SamgarModuleInfo modInfo)
	{
		moduleInfo = modInfo;
	}
	
	/** returns information about the samgar module that was added */
	public SamgarModuleInfo getModuleInfo()
	{
		return moduleInfo;
	}
	
}