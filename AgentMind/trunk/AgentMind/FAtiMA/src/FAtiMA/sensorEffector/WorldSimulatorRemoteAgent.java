package FAtiMA.sensorEffector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import FAtiMA.Agent;
import FAtiMA.culture.SymbolTranslator;
import FAtiMA.knowledgeBase.KnowledgeBase;
import FAtiMA.util.AgentLogger;
import FAtiMA.wellFormedNames.Name;

public class WorldSimulatorRemoteAgent extends RemoteAgent {

	
	public WorldSimulatorRemoteAgent(String host, int port, Agent agent, Map properties) throws IOException
	{
		super(host,port,agent,properties);
	}
	
	
	public String getInitializationMessage(Map arguments) {
		
		String msg;
		
		/* sends the properties of the agent to the virtual world **/
		
		msg = _agent.name() + " " + _agent.role() + " " + _agent.displayName();
		Set s = arguments.keySet();
		Iterator it = s.iterator();
		Object property;
		
		while (it.hasNext())  {
			property = it.next();
			
			msg = msg + " " + property + ":" + arguments.get(property);
		}
		
		return msg; 
	}


	protected boolean SendAction(RemoteAction ra) {
		
		String msg = ra.toPlainStringMessage();	
		AgentLogger.GetInstance().log("Sending action for execution: " + msg);
		return Send(msg);
	}


	public void ReportInternalPropertyChange(Name property, Object value) {
		String msg = PROPERTY_CHANGED + " " + property + " " + value;
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

		if( st.countTokens() == 3 ){
			String subject = st.nextToken();
			String property = st.nextToken();
			propertyName = Name.ParseName(subject + "(" + property + ")");
		}
		else if( st.countTokens() == 2 ){
			String subjectWithProperty = st.nextToken();
			propertyName = Name.ParseName(subjectWithProperty);
		}
		
		String value = st.nextToken();
		KnowledgeBase.GetInstance().Tell(propertyName, value);
		
		/*String value = null;
		Name propertyName = null;
		
		propertyName = Name.ParseName(st.nextToken());	
		value = st.nextToken();

		KnowledgeBase.GetInstance().Tell(propertyName, value);*/	
		
		/*String subject = st.nextToken();
		String property = st.nextToken();
		String value = st.nextToken();
		Name propertyName = Name.ParseName(subject + "(" + property + ")");
		KnowledgeBase.GetInstance().Tell(propertyName, value);*/

		/*Event event = null;
		event = new Event(propertyName.GetFirstLiteral().toString(),
				PROPERTY_CHANGED,
				propertyName.GetLiteralList().get(1).toString());
		event.AddParameter(new Parameter("param",value));

		_agent.PerceiveEvent(event);*/
	}
	
	protected void PropertyRemovedPerception(String perc){
		AgentLogger.GetInstance().log("WARNING: Code entered a non-predicted area -> Class: WorldSimulatorRemoteAgent -  Method: PropertyRemovedPerception(String perc)");			
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

		action = SymbolTranslator.GetInstance().translateSymbolToAction(symbol);


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
					ArrayList context = speechAct.getContextVariables();
					Parameter p;
					for(ListIterator li = context.listIterator();li.hasNext();)
					{
						p = (Parameter) li.next();
						if(p.GetName().equals("copingstrategy"))
						{
							speechAct.AddParameter(p.GetValue().toString());
						}
					}


				}
				event = speechAct.toEvent();
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

			event = speechAct.toEvent();

			//TODO change this test
			if(speechAct.getSender().equals(_agent.name()) &&
					speechAct.getMeaning().equals("acceptreason"))
			{
				//the agent accepts the coping strategy
				Object coping = KnowledgeBase.GetInstance().AskProperty(
						Name.ParseName(_agent.name()+"(copingStrategy)"));
				if(coping != null)
				{
					_agent.EnforceCopingStrategy(coping.toString());
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
			event = new Event(subject,action,target);
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
		if(subject.equals(_agent.name())) {
	
			AgentLogger.GetInstance().log("can act now!");
			_canAct = true;
		}
		
	}

	protected void ActionFailedPerception(String perc) {
		StringTokenizer st = new StringTokenizer(perc," ");
		String subject = st.nextToken();
		//TODO o agente tb tem de perceber quando a acção falhou..
		//the agent last action failed
		if(subject.equals(_agent.name()))
		{
			_canAct = true;
		}	
	}


	public void handleSocketException() {
		// TODO Auto-generated method stub
		
	}
}
