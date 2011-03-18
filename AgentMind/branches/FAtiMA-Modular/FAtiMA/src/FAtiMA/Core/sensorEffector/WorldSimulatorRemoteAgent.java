package FAtiMA.Core.sensorEffector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


import FAtiMA.Core.AgentCore;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.enumerables.ActionEvent;
import FAtiMA.Core.util.enumerables.EventType;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;

public class WorldSimulatorRemoteAgent extends RemoteAgent {

	
	public WorldSimulatorRemoteAgent(String host, int port, AgentCore agent, Map<String,String> properties) throws IOException
	{
		super(host,port,agent,properties);
	}
	
	
	public String getInitializationMessage(Map<String,String> arguments) {
		
		String msg;
		
		/* sends the properties of the agent to the virtual world **/
		
		msg = _agent.getName() + " " + _agent.role() + " " + _agent.displayName();
		Set<String> s = arguments.keySet();
		Iterator<String> it = s.iterator();
		Object property;
		
		while (it.hasNext())  {
			property = it.next();
			
			msg = msg + " " + property + ":" + arguments.get(property);
		}
		
		return msg; 
	}
	
	@SuppressWarnings("deprecation")
	protected boolean SendAction(RemoteAction ra) {
		
		ra = _processActionStrategy.ProcessActionToWorld(ra);
		String msg = ra.toPlainStringMessage();	
		AgentLogger.GetInstance().log("Sending action for execution: " + msg);
		return Send(msg);
	}
	
	@Override @SuppressWarnings("deprecation")
	protected void sendCancelActionMsg(RemoteAction ra)
	{
		ra = _processActionStrategy.ProcessActionToWorld(ra);
		String msg = CANCEL_ACTION + " " + ra.toPlainStringMessage();
		AgentLogger.GetInstance().log("Canceling action: " + msg);
		Send(msg);
	}


	public void ReportInternalPropertyChange(String agentName, Name property, Object value) {
		
		String prop = "";
		ListIterator<Symbol> li = property.GetLiteralList().listIterator();
		String entity = li.next().toString();
		if(entity.equals(Constants.SELF))
		{
			entity = agentName;
		}
		
		if(li.hasNext())
		{
			prop = li.next().toString();
			while(li.hasNext())
			{
				prop = prop + "," + li.next().toString();
			}
		}
		
		String msg = PROPERTY_CHANGED + " " + entity + "(" + prop + ")" + value;
		AgentLogger.GetInstance().log("Reporting property changed: " + msg);
		Send(msg);
	}
	
	protected void PropertyChangedPerception(String perc)
	{
		StringTokenizer st = new StringTokenizer(perc," ");
		//an object/agent has one of its properties changed
		//the perception specifies which property was changed and its new value
		//percept-type object property newvalue 
		//Ex: PROPERTYCHANGED Luke pose onfloor
		
		Name propertyName = null;
		String ToM = null;
		String subject = null;
		String property = null;
		String value = null;

		if( st.countTokens() == 4 ){
			ToM = st.nextToken();
			subject = st.nextToken();
			property = st.nextToken();
			value = st.nextToken();
			_agent.PerceivePropertyChanged(ToM,subject, property, value);
			
		}
		else if( st.countTokens() == 3 ){
			ToM = st.nextToken();
			String subjectWithProperty = st.nextToken();
			propertyName = Name.ParseName(subjectWithProperty);
			value = st.nextToken();
			_agent.PerceivePropertyChanged(ToM, propertyName, value);
		}
		
		
		
	}
	
	protected void PropertyRemovedPerception(String perc)
	{
		StringTokenizer st = new StringTokenizer(perc," ");
		//a object/agent has one of its properties removed
		//the perception specifies which property was removed
		//percept-type object property 
		//Ex: PROPERTY-REMOVED Luke pose 
		String subject = st.nextToken();
		String property = st.nextToken();
	
		
		AgentLogger.GetInstance().logAndPrint("Removing Property: " + subject + " " + property);
		_agent.PerceivePropertyRemoved(subject, property);	
	}
	
	protected void UserSpeechPerception(String perc){
		AgentLogger.GetInstance().log("WARNING: Code entered a non-predicted area -> Class: WorldSimulatorRemoteAgent -  Method: UserSpeechPerception(String perc)");		
	}
	
	protected void ActionStartedPerception(String perc){
		AgentLogger.GetInstance().log("WARNING: Code entered a non-predicted area -> Class: WorldSimulatorRemoteAgent -  Method: ActionStartedPerception(String perc)");		
	}


	protected void ActionFinishedPerception(String perc) {
		String subject;
		String symbol;
		String action;
		String target=null;
		SpeechAct speechAct;
		Event event;
		StringTokenizer st = new StringTokenizer(perc," ");

/*
		subject = st.nextToken();
		symbol = new String(st.nextToken());
		if(st.hasMoreTokens()) {
			target = st.nextToken();
		}
		if(st.hasMoreTokens()){
			symbol += "(";
			while(st.hasMoreTokens()){
				symbol += st.nextToken();
				if(st.hasMoreTokens()){
					symbol += ",";
				}
			}
			
			symbol += ")";
		}*/

		
		subject = st.nextToken();
		symbol = st.nextToken();
		
		RemoteAction ra = new RemoteAction();
		ra.setActionType(symbol);
		ra = _processActionStrategy.ProcessActionFromWorld(ra);
		action = ra.getActionType();


		if(st.hasMoreTokens()) {
			target = st.nextToken();
		}
		if(action.equals("UserSpeech")) {
			try {
				String aux="";
				_userName = subject;
				while(st.hasMoreTokens()) {
					aux = aux + " " + st.nextToken();
				}

				speechAct = (SpeechAct) SpeechAct.ParseFromXml(aux);
				speechAct.setSender("User");
				speechAct.setActionType(SpeechAct.UserSpeech);
				if(speechAct.getMeaning().equals("suggestcopingstrategy") || 
						speechAct.getMeaning().equals("yes"))
				{
					ArrayList<Parameter> context = speechAct.getContextVariables();
					Parameter p;
					for(ListIterator<Parameter> li = context.listIterator();li.hasNext();)
					{
						p = li.next();
						if(p.GetName().equals("copingstrategy"))
						{
							speechAct.AddParameter(p.GetValue().toString());
						}
					}


				}
				event = speechAct.toEvent(ActionEvent.SUCCESS);
			}
			catch (Exception e) {
				AgentLogger.GetInstance().log("Error converting a speechAct");
				e.printStackTrace();
				return;
			}
		}
		else if(action.equals("say")) {
			while(st.hasMoreTokens()) {
				target = target + " " + st.nextToken();
			}
			speechAct = (SpeechAct) SpeechAct.ParseFromXml(target);
			//_agent.UpdateDialogState(speechAct);

			event = speechAct.toEvent(ActionEvent.SUCCESS);

			//TODO change this test
			if(speechAct.getSender().equals(_agent.getName()) &&
					speechAct.getMeaning().equals("acceptreason"))
			{
				//the agent accepts the coping strategy
				Object coping = _agent.getMemory().getSemanticMemory().AskProperty(
						Name.ParseName(_agent.getName()+"(copingStrategy)"));
				if(coping != null)
				{
					//TODO implement this
					//_agent.EnforceCopingStrategy(coping.toString());
				}
			}

		}
		else {
			if(action.equals("look-at"))
			{
				_agent.PerceiveLookAt(subject, target);	
			}
			/*event = new Event(subject,actionName,target);
			Iterator it = action.GetLiteralList().iterator();
			it.next();
			while(it.hasNext()) {
				Name param = (Name) it.next();
				event.AddParameter(new Parameter("param",param.toString()));
			}*/
			event = new Event(subject,action,target,EventType.ACTION,ActionEvent.SUCCESS);
			String aux;
			while(st.hasMoreTokens()) {
				aux = st.nextToken();
				event.AddParameter(new Parameter("param",aux));
			}
		}

		/*try {
	    _fileWriter.write(event.toString() + "\n");
	    _fileWriter.flush();
	}
	catch(Exception e) {
	    e.printStackTrace();
	}*/
		_agent.PerceiveEvent(event);

		//the agent last action suceeded!
		if(_currentAction != null && event.GetSubject().equals(_agent.getName()))
		{
			if(_currentAction.getAction().GetFirstLiteral().toString().equals(event.GetAction()))
			{
				AgentLogger.GetInstance().log("can act now!");
				_currentAction = null;
				_canAct = true;
			}
		}
	}

	protected void ActionFailedPerception(String perc) {
		String subject;
		String symbol;
		String action;
		String target=null;
		SpeechAct speechAct;
		Event event;
		StringTokenizer st = new StringTokenizer(perc," ");

/*
		subject = st.nextToken();
		symbol = new String(st.nextToken());
		if(st.hasMoreTokens()) {
			target = st.nextToken();
		}
		if(st.hasMoreTokens()){
			symbol += "(";
			while(st.hasMoreTokens()){
				symbol += st.nextToken();
				if(st.hasMoreTokens()){
					symbol += ",";
				}
			}
			
			symbol += ")";
		}*/

		
		subject = st.nextToken();
		symbol = st.nextToken();
		
		RemoteAction ra = new RemoteAction();
		ra.setActionType(symbol);
		ra = _processActionStrategy.ProcessActionFromWorld(ra);
		action = ra.getActionType();

		if(st.hasMoreTokens()) {
			target = st.nextToken();
		}
		if(action.equals("UserSpeech")) {
			try {
				String aux="";
				_userName = subject;
				while(st.hasMoreTokens()) {
					aux = aux + " " + st.nextToken();
				}

				speechAct = (SpeechAct) SpeechAct.ParseFromXml(aux);
				speechAct.setSender("User");
				speechAct.setActionType(SpeechAct.UserSpeech);
				if(speechAct.getMeaning().equals("suggestcopingstrategy") || 
						speechAct.getMeaning().equals("yes"))
				{
					ArrayList<Parameter> context = speechAct.getContextVariables();
					Parameter p;
					for(ListIterator<Parameter> li = context.listIterator();li.hasNext();)
					{
						p = li.next();
						if(p.GetName().equals("copingstrategy"))
						{
							speechAct.AddParameter(p.GetValue().toString());
						}
					}


				}
				event = speechAct.toEvent(ActionEvent.FAILURE);
			}
			catch (Exception e) {
				AgentLogger.GetInstance().log("Error converting a speechAct");
				e.printStackTrace();
				return;
			}
		}
		else if(action.equals("say")) {
			while(st.hasMoreTokens()) {
				target = target + " " + st.nextToken();
			}
			speechAct = (SpeechAct) SpeechAct.ParseFromXml(target);
			//_agent.UpdateDialogState(speechAct);

			event = speechAct.toEvent(ActionEvent.FAILURE);

			//TODO change this test
			if(speechAct.getSender().equals(_agent.getName()) &&
					speechAct.getMeaning().equals("acceptreason"))
			{
				//the agent accepts the coping strategy
				Object coping = _agent.getMemory().getSemanticMemory().AskProperty(
						Name.ParseName(_agent.getName()+"(copingStrategy)"));
				if(coping != null)
				{
					//TODO implement this
					//
					//_agent.EnforceCopingStrategy(coping.toString());
				}
			}

		}
		else {
			/*event = new Event(subject,actionName,target);
			Iterator it = action.GetLiteralList().iterator();
			it.next();
			while(it.hasNext()) {
				Name param = (Name) it.next();
				event.AddParameter(new Parameter("param",param.toString()));
			}*/
			event = new Event(subject,action,target,EventType.ACTION,ActionEvent.FAILURE);
			String aux;
			while(st.hasMoreTokens()) {
				aux = st.nextToken();
				event.AddParameter(new Parameter("param",aux));
			}
		}

		/*try {
	    _fileWriter.write(event.toString() + "\n");
	    _fileWriter.flush();
		}
		catch(Exception e) {
	    	e.printStackTrace();
		}*/
		
		if(subject.equals(_agent.getName()))
		{
			// we need to change the event subject to SELF, before we can appraise it
			event.SetSubject(Constants.SELF);
			_agent.PerceiveActionFailed(event);
			_canAct = true;
		}	
	}


	public void handleSocketException() {
		// TODO Auto-generated method stub
		
	}
}
