package cmion.level2.migration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface Migrating {

	/**
	 * This method returns a string that is used to identify the
	 * state that is encoded by the implementing class and to select the
	 * messages that will be delivered to it.
	 * 
	 * @return the object signature.
	 */
	public String getMessageTag();
	
	/**
	 * This method parses the XML element provided and restores the
	 * state of the object.
	 * 
	 * @param message An XML element that contains the state to be parsed
	 */
	public void restoreState(Element message);
	
	/**
	 * This method encodes the current state of the object and other relevant
	 * information in an XML element in order to be migrated.
	 * 
	 * @param message The element in which the state of the object should be encoded.
	 */
	public Element saveState(Document document);
	
	public void onMigrationIn();
	
	public void onMigrationOut();
	
	public void onMigrationSuccess();
	
	public void onMigrationFailure();
	
}
