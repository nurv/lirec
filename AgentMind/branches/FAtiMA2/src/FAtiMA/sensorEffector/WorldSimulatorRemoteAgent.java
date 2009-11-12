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
import FAtiMA.util.AgentLogger;
import FAtiMA.util.Constants;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Symbol;

public class WorldSimulatorRemoteAgent extends RemoteAgent {

	
	public WorldSimulatorRemoteAgent(String host, int port, Agent agent, Map<String,String> properties) throws IOException
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
		
		String msg = ra.toPlainStringMessage();	
		AgentLogger.GetInstance().log("Sending action for execution: " + msg);
		return Send(msg);
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
		String subject = null;
		String property = null;
		String value = null;

		if( st.countTokens() == 3 ){
			subject = st.nextToken();
			property = st.nextToken();
			value = st.nextToken();
			_agent.PerceivePropertyChanged(subject, property, value);
			
		}
		else if( st.countTokens() == 2 ){
			
			String subjectWithProperty = st.nextToken();
			propertyName = Name.ParseName(subjectWithProperty);
			value = st.nextToken();
			_agent.PerceivePropertyChanged(propertyName, value);
		}
		
		
		
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
			if(speechAct.getSender().equals(_agent.getName()) &&
					speechAct.getMeaning().equals("acceptreason"))
			{
				//the agent accepts the coping strategy
				Object coping = _agent.getMemory().getSemanticMemory().AskProperty(
						Name.ParseName(_agent.getName()+"(copingStrategy)"));
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
		if(subject.equals(_agent.getName())) {
	
			AgentLogger.GetInstance().log("can act now!");
			_canAct = true;
		}
		
	}

	protected void ActionFailedPerception(String perc) {
		StringTokenizer st = new StringTokenizer(perc," ");
		String subject = st.nextToken();
		//TODO o agente tb tem de perceber quando a acção falhou..
		//the agent last action failed
		if(subject.equals(_agent.getName()))
		{
			_canAct = true;
		}	
	}


	public void handleSocketException() {
		// TODO Auto-generated method stub
		
	}
}
