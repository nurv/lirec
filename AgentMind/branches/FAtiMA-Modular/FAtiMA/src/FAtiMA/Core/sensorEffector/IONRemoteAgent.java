/** 
 * IONRemoteAgent.java - Connection to ION's virtual world as a RemoteAgent. Implements 
 * the architecture Sensors and Effectors
 *  
 * Copyright (C) 2006 GAIPS/INESC-ID 
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: GAIPS/INESC-ID
 * Project: FAtiMA
 * Created: 07/12/2006 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 07/12/2006 - File created from the version of the RemoteAgent in FAtiMA-ION
 * 
 * Differences between IONRemoteAgent and RemoteAgent:
 * 	- The remote agent doesn't receive as argument the agent's properties in
 * 	  its constructor 
 *  - When the remote agent establishes connection with the RemoteCharacter in
 * 	  ION Framework, it only sends its name and waits for a confirmation message
 *  - The format of message actions that are sent for execution has changed in order
 * 	  to communicate with the new ION Framework. Now, we send the action's 
 * 	  xml descrition as the message.
 * 	- The description of Actions in ACTION-FINISHED perceptions is now received in XML
 * 	  USER-SPEECH messages are now parsed differently. You do not need to begin the
 * 	  message with ACTION-FINISHED
 * 	- The temporarly small change made in 28/09/2006 (see RemoteAgent file) was removed in
 *    order to properly parse PROPERTY-CHANGED perceptions sent by ION-Framework
 *    
 * João Dias: 12/02/2007 - Added the PropertyRemoved perception
 * João Dias: 22/02/2007 - Removed the logfile that stored the events happening in the virtual world
 * João Dias: 03/04/2007 - The running attribute is now initialized as false
 * João Dias: 20/07/2007 - Removed the LanguageEngine from the agent, the LE is not used in the Framework
 *    					   
 */

package FAtiMA.Core.sensorEffector;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.enumerables.ActionEvent;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;

/**
 * Connection to the ION Framework's virtual world as a RemoteAgent. Implements 
 * the architecture Sensors and Effectors
 * 
 * @author João Dias
 */
public class IONRemoteAgent extends RemoteAgent {
	
	/**
	 * Creates the IONRemoteAgent which tries to connect to the ION Framework
	 * @param host - the host where the virtual world is running
	 * @param port - the socket port where the agent should try to connect
	 * @param language - the languageEngine
	 * @param userLanguage - the name of the file that contains the user's LanguageDatabase
	 * @param agent - a reference to the agent that needs this remote connection
	 * @throws UnknownHostException - thrown if the agent cannot connect to the server
	 * @throws IOException - thrown if there are problems reading or writting to the 
	 * 						 connection socket
	 */
	//public IONRemoteAgent(String host, int port, LanguageEngine language, String userLanguage, Agent agent) throws UnknownHostException, IOException
	public IONRemoteAgent(String host, int port, AgentCore agent) throws UnknownHostException, IOException
	{
		super(host,port,agent, null);
	}
	
	public String getInitializationMessage(Map<String,String> arguments)
	{
		return this._agent.getName();
	}
		
	protected boolean SendAction(RemoteAction ra)
	{
		ra = _processActionStrategy.ProcessActionToWorld(ra);
		
		String msg = ra.toXML();
		
		
		AgentLogger.GetInstance().log("Sending action for execution: " + ra._actionType);
		return Send(msg);
	}
	
	@Override
	protected void sendCancelActionMsg(RemoteAction ra)
	{
		ra = _processActionStrategy.ProcessActionToWorld(ra);
		
		String msg = CANCEL_ACTION + " " + ra.toXML();
		
		
		AgentLogger.GetInstance().log("Canceling action: " + ra._actionType);
		Send(msg);
	}
	
	public void ReportInternalPropertyChange(String agentName, Name property, Object value)
	{
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
		
		String msg = PROPERTY_CHANGED + " " + entity + " " + prop + " " + value;
		
		AgentLogger.GetInstance().log("Reporting property changed: " + msg);
		Send(msg);
	}
	
	
	/*
	 * Methods for handling perceptions
	 */
	
	protected void PropertyChangedPerception(String perc)
	{
		StringTokenizer st = new StringTokenizer(perc," ");
		String visibility;
		//a object/agent has one of its properties changed
		//the perception specifies which property was changed and its new value
		//percept-type object property newvalue 
		//Ex: PROPERTYCHANGED Luke pose onfloor
		
		if(st.countTokens() == 4){
			visibility = st.nextToken();	
		}else{
			visibility = "*";
		}
		
		String subject = st.nextToken();
		String property = st.nextToken();
		String value = st.nextToken();
		
		_agent.PerceivePropertyChanged(visibility,subject, property, value);
		
		/*Event event;
		event = new Event(subject,PROPERTY_CHANGED,property);
		event.AddParameter(new Parameter("param",value));
		_agent.PerceiveEvent(event);*/
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
	
	protected void UserSpeechPerception(String perc)
	{
		SpeechAct speechAct;
		
		try {
	        /*_userName = (String) KnowledgeBase.GetInstance().AskProperty(Name.ParseName("User(displayName)"));
	        String gender = (String) KnowledgeBase.GetInstance().AskProperty(Name.ParseName("User(sex)"));
	        
	        System.out.println("UserName: " + _userName);
	        System.out.println("gender: " + gender);
		    
		    String utterance = "<SpeechAct type=\"SpeechAct\"><Sender>"+ _userName + "</Sender><Receiver>" + _agent.name() + "</Receiver><Utterance>" + perc + "</Utterance>" 
		    + "<Context id=\"copingstrategy\">null</Context></SpeechAct>";
		    
		    */
		    
		    /*if(_userLanguageEngine == null) {
		        _userLanguageEngine = new LanguageEngine(_userName,gender,"User",new File(_userLanguageDataBase));
		    }*/
		    /*
		    System.out.println("User Utterance to LE: " + utterance);
		    String utt = _userLanguageEngine.Input(utterance);
		    speechAct = (SpeechAct) SpeechAct.ParseFromXml(utt);
		    */
			speechAct = (SpeechAct) SpeechAct.ParseFromXml(perc);
		    speechAct.setActionType(SpeechAct.UserSpeech);
		    speechAct.setSender("User");
		    
		    /*if(speechAct.getMeaning().equals("unknown"))
		    {
		    	//System.out.println("UNKNOWN ____ UNKNOWN ____ UNKNOWN _____ ");
		    	speechAct = new SpeechAct(SpeechAct.Speech,_agent.name(),"User","replytounknown");
		    	try {
					String aux = _languageEngine.Say(speechAct.toLanguageEngine());
					String aux2 = aux.split("<Utterance>")[1].split("</Utterance")[0];
					speechAct.setUtterance(aux2);
				}
				catch (Exception e) {
					System.out.println("Could not generate the requested SpeechAct: ");
					e.printStackTrace();
					return;
				}
				
				SendAction(speechAct);
				return;
		    }*/
		    
		    if(speechAct.getMeaning().equals("suggestcopingstrategy") || 
		    		speechAct.getMeaning().equals("yes"))
		    {
		    	ArrayList<Parameter> context = speechAct.getContextVariables();
		    	Parameter p;
		    	for(ListIterator<Parameter> li = context.listIterator();li.hasNext();)
		    	{
		    		p = (Parameter) li.next();
		    		if(p.GetName().equals("copingstrategy"))
		    		{
		    			speechAct.AddParameter(p.GetValue().toString());
		    		}
		    	}
		    }
		    
		    //_agent.UpdateDialogState(speechAct);
		    Event event = speechAct.toEvent(ActionEvent.SUCCESS);
		    AgentLogger.GetInstance().logAndPrint("Parsed Speech Act Event: " + event);
			_agent.PerceiveEvent(event);
			
		    /*try {
			    _fileWriter.write(event.toString() + "\n");
			    _fileWriter.flush();
			}
			catch(Exception e) {
			    e.printStackTrace();
			}*/
	    }
	    catch (Exception e) {
	    	AgentLogger.GetInstance().logAndPrint("Exception: " + e);
	        e.printStackTrace();
	        return;
	    }
	}
	
	protected void ActionStartedPerception(String perc)
	{
		//TODO reimplement this
		/*if(perc.startsWith("<SpeechAct"))
		{
			_agent.SpeechStarted();
		}*/
	}
	
	protected void ActionFinishedPerception(String perc)
	{
		Event event;
		
		AgentLogger.GetInstance().log("IONRemoteAgent.ActionFinishPerception(perc) perc:" + perc);
		if(perc.startsWith("<SpeechAct"))
	    {
	    	SpeechAct speechAct = (SpeechAct) SpeechAct.ParseFromXml(perc);
	    	//_agent.UpdateDialogState(speechAct);
	 
	    	//TODO change this test
	    	if(speechAct.getSender().equals(_agent.getName()) &&
	    			speechAct.getMeaning().equals("acceptreason"))
	    	{
	    		//the agent accepts the coping strategy
	    		Object coping = _agent.getMemory().getSemanticMemory().AskProperty(
	    				Name.ParseName(_agent.getName()+"(copingStrategy)"));
	    		if(coping != null)
	    		{
	    			AgentLogger.GetInstance().logAndPrint("");
	    			AgentLogger.GetInstance().logAndPrint("Selected Coping Strategy: " + coping);
	    			AgentLogger.GetInstance().logAndPrint("");
	    			//TODO implement this
	    			//_agent.EnforceCopingStrategy(coping.toString());
	    		}
	    	}
	    	event = speechAct.toEvent(ActionEvent.SUCCESS);
	    }
	    else
	    {
	    	RemoteAction rmAction = RemoteAction.ParseFromXml(perc);
	    	
	    	
	    	rmAction = _processActionStrategy.ProcessActionFromWorld(rmAction);
	    	
	    	if(rmAction.getActionType().equals("look-at"))
	    	{
	    		_agent.PerceiveLookAt(rmAction.getSubject(), rmAction.getTarget());
	    	}
	    	event = rmAction.toEvent(ActionEvent.SUCCESS);
	    }
		
		/*try {
		    _fileWriter.write(event.toString() + "\n");
		    _fileWriter.flush();
		}
		catch(Exception e) {
		    e.printStackTrace();
		}*/
		
		//System.out.println("Perceiving and storing event in Memory: " + event);
		_agent.PerceiveEvent(event);
		
		//the agent last action suceeded!
		if(_currentAction != null && event.GetSubject().equals(_agent.getName()))
		{
			if(_currentAction.getAction().GetFirstLiteral().toString().equals(event.GetAction()))
			{
				_currentAction = null;
				_canAct = true;
			}
		}
	}
	
	protected void ActionFailedPerception(String perc)
	{
		RemoteAction rmAction;
		if(perc.startsWith("<SpeechAct"))
	    {
			rmAction = SpeechAct.ParseFromXml(perc);
	    }
		else{
			rmAction = RemoteAction.ParseFromXml(perc);
			rmAction = _processActionStrategy.ProcessActionFromWorld(rmAction);
		}
		
		//the agent last action failed
		if(rmAction.getSubject().equals(_agent.getName()))
		{
			AgentLogger.GetInstance().logAndPrint("Self action failed, agent can act again");
			_canAct = true;
		}
		
		_agent.PerceiveActionFailed(rmAction.toEvent(ActionEvent.FAILURE));
	}

	public void handleSocketException() {
		ShutDown();
	}
}