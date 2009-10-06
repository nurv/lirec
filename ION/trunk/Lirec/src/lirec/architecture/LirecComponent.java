package lirec.architecture;

import ion.Meta.Element;

/** parent class for all Lirec architecture components */
public abstract class LirecComponent extends Element {
	
/** reference to the architecture, through which references to other components can be obtained */
protected Architecture architecture;

/** create a new Lirec Component */
protected LirecComponent(Architecture architecture)
{
	this.architecture = architecture;
}

/** returns a reference to the Lire architecture object */
public Architecture getArchitecture() {
	return architecture;
}

/** every lirec component has to implement this method and register its event and
 * 	request handlers in here */
public abstract void registerHandlers();

/** Lirec Compoenents that do require a socket connection to an external program, such
 *  as a fatima mind, various competencies, etc.  can override this method and in it
 *  return the connection status */
public boolean isConnected()
{
	return true;
}

@Override
public void onDestroy() {
	// TODO Auto-generated method stub
	
}

}
