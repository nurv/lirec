package teambuddyInterface;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;

import java.util.HashMap;

import org.eclipse.jetty.server.Server;

import cmion.architecture.IArchitecture;
import cmion.level2.CompetencyCancelledException;
import cmion.storage.EventPropertyChanged;

public class InterfaceCompetency extends cmion.level2.Competency {

	private InterfaceHandler handler;
	
	public InterfaceCompetency(IArchitecture architecture) {
		super(architecture);
		handler = new InterfaceHandler(this);
		this.competencyName = "InterfaceCompetency";
		this.competencyType = "InterfaceCompetency";		
	}

	@Override
	public void initialize() 
	{
		this.available = true;
	}	
	
	@Override
	public void registerHandlers()
	{
		super.registerHandlers();
		HandleBlackBoardPropChange handler = new HandleBlackBoardPropChange();
		architecture.getBlackBoard().getEventHandlers().add(handler);
	}
	
	@Override
	public boolean runsInBackground() {
		return true;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters)
			throws CompetencyCancelledException {
		try {
			Server server = new Server(8080);
			server.setHandler(handler);
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public void resetAll(){
		handler.resetAll();
	}
	
	/** internal event handler class for listening to blackboard property changes */
	 private class HandleBlackBoardPropChange extends EventHandler 
	 {

	     public HandleBlackBoardPropChange() {
	         super(EventPropertyChanged.class);
	     }

	     @Override
	     public void invoke(IEvent evt) 
	     {
	      if (evt instanceof EventPropertyChanged)
	      {
	    	  EventPropertyChanged evt1 = (EventPropertyChanged) evt;
	    	  if (evt1.getPropertyName().equals(DisplayRemark.PROPERTY_NAME))
	    	  {
	    		  HashMap<String,String> parameters = (HashMap<String, String>) evt1.getPropertyValue(); 
	    		  String target = parameters.get(DisplayRemark.PARAMETER_TARGET);
	    		  String remark = parameters.get(DisplayRemark.PARAMETER_REMARK);	
	    		  String remarkText = parameters.get(DisplayRemark.PARAMETER_REMARKTEXT);
	    		  handler.setRemark(target,remark,remarkText);
	    	  }
	      }
	    }	
	 }
}
