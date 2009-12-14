/** 
 * RemoteAgent.java - Connection to the virtual world as a RemoteAgent. Implements 
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
 * Created: 08/11/2004 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 08/11/2004 - File created
 * João Dias: 23/05/2006 - Added comments to each public method's header
 * João Dias: 23/05/2006 - Several methods that were public (and not used externally)
 * 						   are now private:
 * 						     GetIfFromName, GetNameFromId, ProccessMessage, RegisterNewEntity
 * João Dias: 02/07/2006 - The received Stop and Start commands now Stop and Resume the agent's
 * 						   internal simulation timer
 * João Dias: 15/07/2006 - stopped using the agent's getKB() method and replaced it by
 * 					       the singleton used to access the KB
 * João Dias: 17/07/2006 - Added the fields userLanguageDataBase and LanguageEngine to the 
 *						   class (previously stored in the Agent class).
 * João Dias: 17/07/2006 - the field bullyName was removed. 
 * João Dias: 24/07/2006 - The method AddAction now receives a ValuedActions instead of 
 * 						   just the name of the action. Thus, the class now stores a list
 * 					       of ValuedActions. Whenever one of this actions is sent for 
 * 						   execution, the EpisodicMemory is informed
 * João Dias: 29/08/2006 - Changed StartAction method, now the method handles better
 * 						   the sending of actions with different parameters by using the
 * 					       new RemoteAction class. For instance, SpeechActs and conventional 
 * 						   actions are handled in almoust the same way.
 * João Dias: 28/09/2006 - Temporarly small change in the parsing of received PROPERTY-CHANGED messages
 * 						   so that FAtiMA can communicate with the simple WorldSimulator
 * João Dias: 14/10/2006 - We were forgetting to update the dialogstate after receiving a User SpeechAct
 * João Dias: 07/12/2006 - Added empty constructor so that we can extend the RemoteAgent class
 * 						 - Send method changed from private to protected so that it can be inherited and 
 * 						   used by IONRemoteAgent
 * 						 - Changed the class attributes from private to protected for the same reason
 * 						   as above
 * 						 - Reorganized the method ProcessMessage into several smaller methods each corresponding
 * 						   to a distinct perception. This makes easier to build the IONRemoteAgent that inherits
 * 						   most of the methods used to handle the perceptions, and changes a small number of them.
 * 						   For the same reason, we've introduced the method SendAction(RemoteAction) that is called
 * 						   in order to send a request to execute an action.
 * João Dias: 05/02/2007 - Removed the perspective module that was not being used anymore
 * João Dias: 12/02/2007 - Added the PropertyRemoved perception
 * João Dias: 13/02/2007 - Added the method ReportInternalPropertyChange that is used to report to the Framework
 * 						   that a property of the agent has changed because of an internal change
 * João Dias: 14/02/2007 - The summary of an episode in the Autobiographical Memory is now being genererated through
 * 						   the LanguageEngine
 * João Dias: 22/02/2007 - Removed the logfile that stored the events happening in the virtual world
 * João Dias: 04/02/2007 - Added a new CMD that orders the agent to save its state into harddrive
 * João Dias: 20/07/2007 - Removed the LanguageEngine from the agent, the LE is not used in the Framework
 */

package FAtiMA.sensorEffector;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

import FAtiMA.Agent;
import FAtiMA.AgentSimulationTime;
import FAtiMA.ValuedAction;
import FAtiMA.emotionalState.ActiveEmotion;
import FAtiMA.emotionalState.EmotionalState;
import FAtiMA.knowledgeBase.KnowledgeBase;
import FAtiMA.memory.Memory;
import FAtiMA.memory.autobiographicalMemory.AutobiographicalMemory;
import FAtiMA.memory.autobiographicalMemory.MemoryEpisode;
import FAtiMA.memory.eventQuery.CompoundCue;
import FAtiMA.memory.eventQuery.SpreadActivate;
import FAtiMA.memory.shortTermMemory.ShortTermMemory;
import FAtiMA.memory.shortTermMemory.WorkingMemory;
import FAtiMA.motivationalSystem.MotivationalState;
import FAtiMA.socialRelations.LikeRelation;
import FAtiMA.socialRelations.RespectRelation;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.parsers.SocketListener;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.SubstitutionSet;

/**
 * Connection to the virtual world as a RemoteAgent. Implements 
 * the architecture Sensors and Effectors
 * 
 * @author João Dias, Samuel Mascarenhas
 */
public abstract class RemoteAgent extends SocketListener {
	
	
	protected static final String SHUTDOWN = "SHUTDOWN";
	protected static final String CMD = "CMD";
	protected static final String CHANGE_IMPORTANCE_SUCCESS = "CIS";
	protected static final String CHANGE_IMPORTANCE_FAILURE = "CIF";
	protected static final String ADD_GOALS = "ADDGOALS";
	protected static final String REMOVE_GOAL = "REMOVEGOAL";
	protected static final String REMOVE_ALL_GOALS = "REMOVEALLGOALS";
	protected static final String AGENTS = "AGENTS";
	protected static final String LOOK_AT = "LOOK-AT";
	protected static final String ENTITY_ADDED = "ENTITY-ADDED";
	protected static final String ENTITY_REMOVED = "ENTITY-REMOVED";
	protected static final String PROPERTY_CHANGED = "PROPERTY-CHANGED";
	protected static final String PROPERTY_REMOVED = "PROPERTY-REMOVED";
	protected static final String USER_SPEECH = "USER-SPEECH";
	protected static final String ACTION_STARTED = "ACTION-STARTED";
	protected static final String ACTION_FINISHED = "ACTION-FINISHED";
	protected static final String ACTION_FAILED = "ACTION-FAILED";
	protected static final String ADVANCE_TIME = "ADVANCE-TIME";
	protected static final String STOP_TIME = "STOP-TIME";
	protected static final String RESUME_TIME = "RESUME-TIME";
	protected static final String READY_FOR_NEXT_STEP = "READY-FOR-NEXT-STEP";
	
	protected static final String QUERY_MEMORY = "QUERY-MEMORY";
	
	protected ArrayList _actions;
	
	protected Agent _agent;
	protected boolean _canAct;
	
	//protected FileWriter _fileWriter;
	//protected File _logFile;
	
	protected ArrayList _lookAtList;
	protected HashMap _objectIdentifiers;
	protected boolean _running;
	protected String _userName;
	
	private boolean _readyForNextStep;
	
	//protected LanguageEngine _languageEngine;
	//protected String _userLanguageDataBase;
	//protected LanguageEngine _userLanguageEngine;
	
	
	
	protected RemoteAgent(String host, int port, Agent agent, Map arguments) throws UnknownHostException, IOException
	{
		_agent = agent;
		_lookAtList = new ArrayList();
		_actions = new ArrayList();
		_objectIdentifiers = new HashMap();
		_canAct = true;
		_running = true;	
		_readyForNextStep = false;
		
		AgentLogger.GetInstance().log("Connecting to " + host + ":" + port);
		this.socket = new Socket(host, port);
		this.initializeSocket();
		this.Send(this.getInitializationMessage(arguments));
		this.waitForServerAck();
	}
	
	protected void waitForServerAck() throws IOException
	{
		byte[] buff = new byte[this.maxSize];
		if(this.socket.getInputStream().read(buff)<=0) {
			throw new IOException("Server Does not Confirm!");
		}
		
		String aux = new String(buff,"UTF-8");
		String aux2 = (aux.split("\n"))[0];
		if(!aux2.equals("OK")) {
			throw new IOException("Error: " + aux);
		}
	}
	
	
	public abstract String getInitializationMessage(Map arguments);

	/**
	 * Add an action to an execution list. The action will be executed as soon
	 * as possible.
	 * @param action - the ValuedAction to execute
	 */
	public final void AddAction(ValuedAction action) {
		_actions.add(action);
	}

	/**
	 * Clears all the actions that an agent has in the execution list
	 */
	public final void Clear() {
		_canAct = true;
		_actions.clear();
	}
	
	/**
	 * Sends for execution the next available action
	 */
	public final void ExecuteNextAction() {
		ValuedAction action;
		if(_actions.size() > 0) {
			action = (ValuedAction) _actions.remove(0);
			this.StartAction(action);
			
		}
	}
	
	/**
	 * indicates if the agent has finished the execution of the last action, 
	 * and thus it can perform another action
	 * @return true if the agent has finished execution and can perform another
	 * action, false otherwise
	 */
	public final boolean FinishedExecuting() {
		return _canAct;
	}
	
	/**
	 * indicates if the RemoteAgent is running normally or if it was 
	 * stopped by an explicit stop command send by the virtual world 
	 * 
	 * @return true if the agent is running normally, false if the agent 
	 * must pause its normal functioning 
	 */
	public final boolean isRunning() {
	    return _running;
	}
		
	public void processMessage(String msg) {
		
		String msgType;
		StringTokenizer st;
		
		try
		{
		
			AgentLogger.GetInstance().log(_agent.name() +": Processing message: " + msg);
					
			st = new StringTokenizer(msg," ");
			msgType = st.nextToken();
			
			String perception = "";
			
			while(st.hasMoreTokens())
			{
				perception = perception + st.nextToken() + " ";
			}
			
			perception = perception.trim();
			
			if(msgType.equals(SHUTDOWN))
			{
				ShutDownPerception(perception);
			}
			else if(msgType.equals(CMD))
			{
				CmdPerception(perception);
			}
			else if(msgType.equals(CHANGE_IMPORTANCE_SUCCESS) 
					|| msgType.equals(CHANGE_IMPORTANCE_FAILURE))
			{
				ChangeImportancePerception(msgType, perception);
			}
			else if(msgType.equals(ADD_GOALS)) {
				AddGoalsPerception(perception);
			}
			else if(msgType.equals(REMOVE_GOAL)) {
				RemoveGoalPerception(perception);
			}
			else if (msgType.equals(REMOVE_ALL_GOALS)) {
				RemoveAllGoalsPerception(perception);
			}
			else if(msgType.equals(AGENTS)) {
				AgentsPerception(perception);
			}
			else if(msgType.equals(LOOK_AT)) {
				LookAtPerception(perception);
			}
			else if(msgType.equals(ENTITY_ADDED)) {
				EntityAddedPerception(perception);
			}
			else if(msgType.equals(ENTITY_REMOVED)) {
				EntityRemovedPerception(perception);
			}
			else if(msgType.equals(PROPERTY_CHANGED)) {
				PropertyChangedPerception(perception);
			}
			else if(msgType.equals(PROPERTY_REMOVED))
			{
				PropertyRemovedPerception(perception);
			}
			else if(msgType.equals(USER_SPEECH))
			{
				UserSpeechPerception(perception);
			}
			else if(msgType.equals(ACTION_STARTED))
			{
				ActionStartedPerception(perception);
			}
			else if(msgType.equals(ACTION_FINISHED)) {
			    ActionFinishedPerception(perception);
			}
			else if(msgType.equals(ACTION_FAILED)) {
				ActionFailedPerception(perception);
			}
			else if(msgType.equals(ADVANCE_TIME))
			{
				AdvanceTimePerception(perception);
			}
			else if(msgType.equals(STOP_TIME))
			{
				StopTimePerception(perception);
			}
			else if(msgType.equals(RESUME_TIME))
			{
				ResumeTimePerception(perception);
			}
			else if(msgType.equals(READY_FOR_NEXT_STEP))
			{
				if (_readyForNextStep)
					_readyForNextStep = false;
					
				else
					_readyForNextStep = true;
				_agent.setReadyForNextStep(_readyForNextStep);	
						
			}
			else if(msgType.equals(QUERY_MEMORY))
			{
				SpreadActivate.GetInstance().Spread(AutobiographicalMemory.GetInstance().GetAllEpisodes(), ShortTermMemory.GetInstance().GetAllRecords());
				//CompoundCue.GetInstance().Match(AutobiographicalMemory.GetInstance().GetAllEpisodes(), ShortTermMemory.GetInstance().GetAllRecords());						
			}
			
			while(_lookAtList.size() > 0)
			{
				//there are still some objects/agents that the character hasn't looked yet
				AgentLogger.GetInstance().log("Sending Look-AT: " + _lookAtList.get(0));
				Send("look-at " + _lookAtList.remove(0));
			}
		}
		catch(Exception e)
		{
			AgentLogger.GetInstance().logAndPrint("Exception: " + e);
			AgentLogger.GetInstance().log("Error parsing a received message!");
			e.printStackTrace();
		}
	}
	
	protected final boolean Send(String msg) {
		try {
			String aux = msg + "\n";
			OutputStream out = this.socket.getOutputStream();
			out.write(aux.getBytes("UTF-8"));
			out.flush();
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			this.stopped = true;
			return false;
		}
	}
	
	protected abstract boolean SendAction(RemoteAction ra);
	
	public abstract void ReportInternalPropertyChange(Name property, Object value);
	
	
	public void ReportInternalState()
	{
		String msg = EmotionalState.GetInstance().toXml();
		Send(msg);
		
		LikeRelation like;
		RespectRelation respect;
		ArrayList relations = LikeRelation.getAllRelations(this._agent.name());
		msg="<Relations>";
		for(ListIterator li = relations.listIterator();li.hasNext();)
		{
			like = (LikeRelation) li.next();
			msg += "<Like>";
			msg += "<Subject>" + like.getSubject() + "</Subject>";
			msg += "<Target>" + like.getTarget() + "</Target>";
			msg += "<Value>" + like.getValue() + "</Value>";
			msg += "</Like>";
		}
		relations = RespectRelation.getAllRelations(this._agent.name());
		for(ListIterator li = relations.listIterator();li.hasNext();)
		{
			respect = (RespectRelation) li.next();
			msg += "<Respect>";
			msg += "<Subject>" + respect.getSubject() + "</Subject>";
			msg += "<Target>" + respect.getTarget() + "</Target>";
			msg += "<Value>" + respect.getValue() + "</Value>";
			msg += "</Respect>";
		}
		
		
		msg += "</Relations>";
		Send(msg);
	}
	
	/**
	 * Expresses a given emotion in the virtual world
	 * @param emotion - the emotion to be expressed
	 */
	public void ExpressEmotion(String emotion) {
	    String msg = "EmotionalState " + emotion;
	    Send(msg);
	}
	
	public void SendEmotionalState(String emotionalStateDescription)
	{
		
	}

	protected final void StartAction(ValuedAction vAction) {
		String actionName;
		RemoteAction rAction;
		
		actionName = vAction.GetAction().GetFirstLiteral().toString();
		
		//if the action corresponds to a speech act...
		if(SpeechAct.isSpeechAct(actionName)) {
			
			SpeechAct speechAct = new SpeechAct(vAction); 
			
			/*if(speechAct.getReceiver().equals("user"))
			{
				speechAct.setReceiver(_userName);
			}*/
			
			/* adding context variables to the SpeechAct */
			speechAct.AddContextVariable("sex", _agent.sex().toLowerCase());
			speechAct.AddContextVariable("me", _agent.displayName());
			speechAct.AddContextVariable("role", _agent.role().toLowerCase());
			
			/* for the next context variables we need to retrieve them from the KB */
			Memory memory = Memory.GetInstance();
			
			Object yourole = memory.AskProperty(Name.ParseName(speechAct.getReceiver() + "(role)"));
			if(yourole != null)
			{
				speechAct.AddContextVariable("yourole", yourole.toString().toLowerCase());
			}
			
			Object you = memory.AskProperty(Name.ParseName(speechAct.getReceiver() + "(displayName)"));
			if(you != null)
			{
				speechAct.AddContextVariable("you", you.toString());
			}
			
			Object episode = memory.AskProperty(Name.ParseName("Episode(name)"));
			if(episode != null)
			{
				speechAct.AddContextVariable("episode", episode.toString());
			}
			
			/* determining the context variables victim,bully,bystander and defender
			 * its harder but we can do it nonetheless
			 */
			SubstitutionSet ss;
			Name n1 = Name.ParseName("[x](role)");
			Name n2 = Name.ParseName("[x](displayName)");
			Name auxName;
			String displayName;
			String role;
			ArrayList binds = memory.GetPossibleBindings(n1);
			
			if(binds != null)
			{
				for(ListIterator li = binds.listIterator();li.hasNext();)
				{
					ss = (SubstitutionSet) li.next();
					auxName = (Name) n1.clone();
					auxName.MakeGround(ss.GetSubstitutions());
					role = (String) memory.AskProperty(auxName);
					
					auxName = (Name) n2.clone();
					auxName.MakeGround(ss.GetSubstitutions());
					displayName = (String) memory.AskProperty(auxName);
					
					if(displayName != null && role != null)
					{
						speechAct.AddContextVariable(role.toLowerCase(), displayName);
					}
				}
			}
			
			if(speechAct.getMeaning().equals("episodesummary"))
			{
				_agent.SaveAM(Memory.GetInstance().getSelf());
				String summaryInfo = "<ABMemory><Receiver>" + you + "</Receiver>";
				summaryInfo += AutobiographicalMemory.GetInstance().SummarizeLastEvent();
				summaryInfo += "</ABMemory>";
				AgentLogger.GetInstance().log(summaryInfo);
				speechAct.setAMSummary(summaryInfo);
			}
			
			rAction = speechAct;
			/*try {
				if(speechAct.getMeaning().equals("episodesummary"))
				{
					
					((IONAgent) this._agent).SaveAM(AutobiographicalMemory.GetInstance().getSelf());
					String summaryInfo = "<ABMemory><Receiver>" + you + "</Receiver>";
					summaryInfo += AutobiographicalMemory.GetInstance().SummarizeLastEvent();
					summaryInfo += "</ABMemory>";
					System.out.println(summaryInfo);
					String aux = _languageEngine.Narrate(summaryInfo);
					String[] aux2 = aux.split("<Summary>");
					if(aux2.length > 1)
					{
						String summary = aux2[1].split("</Summary")[0];
						speechAct.setUtterance(summary);
					}
					else
					{
						speechAct.setUtterance("");
					}
				}
				else
				{
					String aux = _languageEngine.Say(speechAct.toLanguageEngine());
				    String aux2 = aux.split("<Utterance>")[1].split("</Utterance")[0];
				    speechAct.setUtterance(aux2);  
				} 
			}
			catch (Exception e) {
				System.out.println("Could not generate the requested SpeechAct: " + speechAct.toLanguageEngine());
				e.printStackTrace();
				return;
			}
			*/
			
		}
		else {
			rAction = new RemoteAction(vAction);
		}
		
		_canAct = false;
		SendAction(rAction);

		AgentLogger.GetInstance().logAndPrint("Cannot act now!");
		
	}
	
	/**
	 * indicates if the remote agent is running properly or if has been shut down
	 * either by an explicit shut down command or by a closed socket connection
	 * 
	 * @return false if the remote agent is running properly and connected to 
	 * the virtual world, true otherwise
	 */
	public final boolean isShutDown() {
	    return this.stopped;
	}
	
	/**
	 * Orders the remote agent to shutdown and disconnect from the 
	 * virtual world
	 */
	public final void ShutDown() {
		
		/*if(_fileWriter != null)
		{
			try {
		        _fileWriter.flush();
		        _fileWriter.close();
		    }
		    catch(Exception e) {
		        e.printStackTrace();
		    }
		}
	    
	    _fileWriter = null;*/
	    this.stopped = true;
	}
	
	public void SaveState(String fileName)
	{
		try
		{
			FileOutputStream out = new FileOutputStream(fileName);
	    	ObjectOutputStream s = new ObjectOutputStream(out);
	    	
	    	s.writeObject(_actions);
	    	s.writeObject(_lookAtList);
	    	s.writeObject(_objectIdentifiers);
	    	s.writeObject(_userName);
	    	s.writeObject(new Boolean(_canAct));
	    	//s.writeObject(new Boolean(_running));
	    	
        	s.flush();
        	s.close();
        	out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void LoadState(String fileName)
	{
		try
		{
			FileInputStream in = new FileInputStream(fileName);
			ObjectInputStream s = new ObjectInputStream(in);
			
			this._actions = (ArrayList) s.readObject();
			this._lookAtList = (ArrayList) s.readObject();
			this._objectIdentifiers = (HashMap) s.readObject();
			this._userName = (String) s.readObject();
			this._canAct = ((Boolean) s.readObject()).booleanValue();
			//this._running = ((Boolean) s.readObject()).booleanValue();
			
			s.close();
			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/*
	 * Methods for handling perceptions
	 */
	
	protected abstract void PropertyChangedPerception(String perc);
	
	protected abstract void PropertyRemovedPerception(String perc);
	
	protected abstract void UserSpeechPerception(String perc);

	protected abstract void ActionStartedPerception(String perc);
		
	protected abstract void ActionFinishedPerception(String perc);
	
	protected abstract void ActionFailedPerception(String perc);
	
	
	protected void ShutDownPerception(String perc)
	{
		AgentLogger.GetInstance().log("SHUTTING DOWN!");
	    this.ShutDown();
	}
	
	protected void ChangeImportancePerception(String type, String perc)
	{
		StringTokenizer st = new StringTokenizer(perc," ");
		String goalName = st.nextToken();
		float importance = new Float(st.nextToken()).floatValue();
		_agent.getDeliberativeLayer().ChangeGoalImportance(goalName,importance,type);
	}
	
	protected void CmdPerception(String perc)
	{
		//corresponds to an external command from the stagemanager
		String action = perc;
		if(action.equals("Start")) 
		{
			_running = true;
			AgentSimulationTime.GetInstance().Resume();
		}
		else if(action.equals("Stop")) 
		{
			_running = false;
			AgentSimulationTime.GetInstance().Stop();
		}
		else if(action.equals("Reset")) 
		{
			_agent.Reset();
			_canAct = true;
			//_running = true;
		}
		else if(action.equals("Save"))
		{
			_agent.SaveAgentState(_agent.name());
		}
		else if(action.startsWith("DA_QUERY"))
		{
			String actionRepresentation;
			String param;
			int intensity;
			ArrayList parameters = new ArrayList();
			StringTokenizer st = new StringTokenizer(perc, " ");
			st.nextToken();
			String triggerID = st.nextToken();
			action = st.nextToken();
			actionRepresentation = action;
			String name = st.nextToken();
			actionRepresentation += " " + name;
			while(st.hasMoreTokens())
			{
				param = st.nextToken();
				actionRepresentation += " " + param; 
				parameters.add(param);
			}
			
			
			ActiveEmotion appraisal = _agent.simulateAppraisal(action, name, parameters);
			if(appraisal == null)
			{
				intensity = 0;
			}
			else
			{
				intensity = Math.round(appraisal.GetIntensity());
			}
			String output = "DAQueryResponse|" + actionRepresentation + ":" + triggerID + ":" + intensity;
			this.Send(output);
		}
	}
	
	protected void AddGoalsPerception(String perc)
	{
		String goalDescription;
		String goalName;
		float importance;
		float importance2;
		StringTokenizer st2;
		StringTokenizer st = new StringTokenizer(perc," ");
		
		while(st.hasMoreTokens()) {
			goalDescription = st.nextToken();
			st2 = new StringTokenizer(goalDescription,"|");
			goalName = st2.nextToken();
			importance = new Float(st2.nextToken()).floatValue();
			importance2 = new Float(st2.nextToken()).floatValue();
			try {
				_agent.getDeliberativeLayer().AddGoal(goalName,importance,importance2);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void RemoveGoalPerception(String perc)
	{
		_agent.getDeliberativeLayer().RemoveGoal(perc);	
	}
	
	protected void RemoveAllGoalsPerception(String perc)
	{
		_agent.getDeliberativeLayer().RemoveAllGoals();
	}
	
	protected void AgentsPerception(String perc)
	{
		StringTokenizer st = new StringTokenizer(perc," ");
		//the agent receives perceptions from others agents in the same place
		while(st.hasMoreTokens()) {
			//The agent will have to look at each of the agents (to detect their properties)
			_lookAtList.add(st.nextToken());
		}
	}
	
	protected void LookAtPerception(String perc)
	{
		Name propertyName;
		StringTokenizer st = new StringTokenizer(perc," ");
		//perception about the properties of a given object/character
		//the second word corresponds to the object/character
		String subject = st.nextToken();
		String[] properties;
		
		//the following ones correspond to ":" separated property value pairs
		while(st.hasMoreTokens()) {
			properties = st.nextToken().split(":");
			//property[0] corresponds to the property name, [1] to the property value
			//constructs something like Luke(Strength)
			
			
			propertyName = Name.ParseName(subject + "(" + properties[0] + ")");
			AgentLogger.GetInstance().log("Look-At:" + propertyName.toString());
			
			WorkingMemory.GetInstance().Tell(propertyName, properties[1]);
			
			//If the agent looks at another agent it initializes it's needs
			if(!subject.equalsIgnoreCase(_agent.displayName()) && 
		       properties[0].equalsIgnoreCase("isPerson") ||
		       (properties[0].equalsIgnoreCase("type") && properties[1].equalsIgnoreCase("character"))){
				
				MotivationalState.GetInstance().InitializeOtherAgentMotivators(subject);
				
			}
		}
		//Signals a lookat event to the Agent
		//Event event = new Event(_agent.name(), "look-at", subject);
		//_agent.PerceiveEvent(event);
	}
	
	protected void EntityAddedPerception(String perc)
	{
		//a new object/agent has been added to the world
		//the agent just looks at it
		
		_lookAtList.add(perc);
	}
	
	protected void EntityRemovedPerception(String perc)
	{
	}
	
	
	protected void AdvanceTimePerception(String perc)
	{
		/*Integer time = new Integer(perc);
		
		if(time != null)
		{
			AgentLogger.GetInstance().log("Advancing time " + time.intValue() + " seconds.");
			AgentSimulationTime.GetInstance().AdvanceTime(time.intValue());
		}*/
	}
	
	protected void StopTimePerception(String perc)
	{
		AgentSimulationTime.GetInstance().Stop();
	}
	
	protected void ResumeTimePerception(String perc)
	{
		AgentSimulationTime.GetInstance().Resume();
	}
}