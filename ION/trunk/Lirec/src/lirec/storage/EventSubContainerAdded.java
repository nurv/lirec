package lirec.storage;

import ion.Meta.Event;

/** an event informing that a new subcontainer has been added to a LirecStorageContainer
 *  This will be raised by the owner container*/
public class EventSubContainerAdded extends Event{

/** the sub container that has been added */
private LirecStorageContainer subContainer;

/** the parent container that has added the sub container*/
private LirecStorageContainer parentContainer;


/** create a new event sub container added */
public EventSubContainerAdded(LirecStorageContainer parentContainer, LirecStorageContainer subContainer)
{
	this.parentContainer = parentContainer;
	this.subContainer =  subContainer;
}

/** returns the sub container that has been added */
public LirecStorageContainer getSubContainer()
{
	return subContainer;
}

/** returns the container that has added the sub container */
public LirecStorageContainer getParentContainer()
{
	return parentContainer;
}


}
