package cmion.TeamBuddy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import cmion.architecture.CmionComponent;
import cmion.architecture.IArchitecture;
import cmion.level2.migration.Migrating;
import cmion.level2.migration.MigrationAware;

public class Migrater extends CmionComponent implements Migrating, MigrationAware
{

	public Migrater(IArchitecture architecture) 
	{
		super(architecture);
	}

	@Override
	public void registerHandlers() 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getMessageTag() 
	{
		return "simpleMind";
	}

	@Override
	public void restoreState(Element message) 
	{
		// read emotion
		String emotionalState = message.getElementsByTagName("emotion").item(0).getChildNodes().item(0).getNodeValue();
		System.out.println("incoming migration, emotion: "+emotionalState);	
	}

	@Override
	public Element saveState(Document doc) 
	{
		String emotionalState = architecture.getBlackBoard().getRTPropertyValue("emotion").toString();
		if (emotionalState.equals("happy")) emotionalState = "joy";
		if (emotionalState.equals("sad")) emotionalState = "sadness";
		
		Element parent = doc.createElement(getMessageTag());
		
		Element emotion = doc.createElement("emotion");
		Node emotionNode = doc.createTextNode(emotionalState);
		emotion.appendChild(emotionNode);
		parent.appendChild(emotion);
		
		return parent;
	}

	@Override
	public void onMigrationFailure() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMigrationIn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMigrationOut() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMigrationSuccess() {
		// TODO Auto-generated method stub
		
	}

}
