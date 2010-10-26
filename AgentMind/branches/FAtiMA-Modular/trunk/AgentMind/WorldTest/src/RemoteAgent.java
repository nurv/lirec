import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;
import java.util.StringTokenizer;

import FAtiMA.memory.Memory;
import FAtiMA.conditions.Condition;
import FAtiMA.deliberativeLayer.plan.Effect;
import FAtiMA.deliberativeLayer.plan.Step;
import FAtiMA.sensorEffector.SpeechAct;
import FAtiMA.util.parsers.SocketListener;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.Symbol;
import FAtiMA.wellFormedNames.Unifier;

/*
 * Created on 4/Fev/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author João Dias
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RemoteAgent extends SocketListener {

	final static int MIN_ACTION_DELAY_MS = 500;
	final static int MAX_ACTION_DELAY_MS = 500;
	protected Random _generator; 
	private ArrayList _properties;
	private String _name;
	private String _role;
	private String _displayName;
	private WorldTest _world;
	private Random _r;
	private boolean _readyForNextStep = false;

	public RemoteAgent(WorldTest world, Socket s) {
		_generator = new Random();
		_properties = new ArrayList();
		_world = world;
		_r = new Random();
		//Memory.GetInstance().setSelf(_name);
		
		this.socket = s;

		this.initializeSocket();
		int nBytes;
		try {
			sleep(100);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		try
		{
			nBytes = this.socket.getInputStream().available();
		}
		catch (Exception e)
		{
			nBytes = 0;
		}

		if(nBytes > 0) {
			byte[] buffer = new byte[nBytes];
			try {
				this.socket.getInputStream().read(buffer);
			}
			catch (java.io.IOException ex) {
				ex.printStackTrace(); 
			}

			String msg = new String(buffer);
			StringTokenizer st = new StringTokenizer(msg,"\n");
			st = new StringTokenizer(st.nextToken()," ");
			StringTokenizer st2;
			_name = st.nextToken();
			_role = st.nextToken();
			_displayName = st.nextToken();
			while(st.hasMoreTokens()) {
				st2 = new StringTokenizer(st.nextToken(),":");
				_properties.add(new Property(st2.nextToken(),st2.nextToken()));
			}
		}
		Send("OK");
	}

	public String Name() {
		return _name;
	}

	public void AddProperty(Property p) {
		_properties.add(p);
	}

	public String GetPropertiesList() {
		String properties = "";
		Property p;

		ListIterator li = _properties.listIterator();
		while(li.hasNext()) {
			p = (Property) li.next();
			properties = properties + p.GetName() + ":" + p.GetValue() + " "; 
		}

		return properties;
	}

	protected boolean Send(String msg) {
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
			try {
				this.socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
		}
	}

	public void processMessage(String msg) {
		//System.out.println(msg);
		
		StringTokenizer st = new StringTokenizer(msg," ");

		String type = st.nextToken();

		if(type.startsWith("<EmotionalState")) {

		}
		else if (type.startsWith("<Relations"))
		{

		}
		else if (type.startsWith("PROPERTY-CHANGED"))
		{			
			
		}
		else if (type.equals("look-at")) {
			String target = st.nextToken();

			_world.GetUserInterface().WriteLine(_name + " looks at " + target);
			String properties = _world.GetPropertiesList(target);

			synchronized(this){
				this.Send("LOOK-AT " + target + " " + properties);
			}

			synchronized(_world){
				_world.SendPerceptionToAll("ACTION-FINISHED " + _name + " " + new String(msg));
			}
		}
		else
		{
			int randomDelayTime = _generator.nextInt(MAX_ACTION_DELAY_MS - MIN_ACTION_DELAY_MS + 1) + MIN_ACTION_DELAY_MS;
			//the +1 is just for the MAX = MIN situation
			
			ActionSimulator as = new ActionSimulator(_world,_name,msg,randomDelayTime,this);
			as.start();
			
		}	
	}

	protected Name ConvertToActionName(String action)
	{
		StringTokenizer st = new StringTokenizer(action," ");
		String actionName = st.nextToken() + "(";
		while(st.hasMoreTokens())
		{
			actionName += st.nextToken() + ",";
		}
		if(actionName.endsWith(","))
		{
			actionName = actionName.substring(0,actionName.length()-1);
		}

		actionName = actionName + ")";
		return Name.ParseName(actionName);
	}

	protected void UpdateActionEffects(Name action) 
	{
		ListIterator li = this._world.GetActions().listIterator();
		ArrayList bindings;
		Step s;
		Step gStep;

		while(li.hasNext()) 
		{
			s = (Step) li.next();
			bindings = new ArrayList();
			bindings.add(new Substitution(new Symbol("[SELF]"), new Symbol(this._name)));
			bindings.add(new Substitution(new Symbol("[AGENT]"), new Symbol(this._name)));
			if(Unifier.Unify(s.getName(),action, bindings))
			{
				gStep = (Step) s.clone();
				gStep.MakeGround(bindings);
				PropertiesChanged(gStep.getEffects());
			}
		}

	}

	private void PropertiesChanged(ArrayList effects)
	{
		ListIterator li = effects.listIterator();
		Condition c;
		Effect e;
		String msg;

		while(li.hasNext())
		{
			e = (Effect) li.next();
			c = e.GetEffect();
			String name = c.getName().toString();
			if(!name.startsWith("EVENT") && !name.startsWith("SpeechContext"))
			{
				if(e.GetProbability(null) > _r.nextFloat())
				{
					
					msg = "PROPERTY-CHANGED " + c.getToM() + " " + name + " " + c.GetValue();

					_world.GetUserInterface().WriteLine(msg);
					this._world.SendPerceptionToAll(msg);
				}
			}

		}
	}

	@Override
	public void handleSocketException() {
		_world.removeAgent(this);
	}

}


class ActionSimulator extends Thread{
	WorldTest _world;
	String _msg;
	int _delay;
	String _agentName;
	RemoteAgent _ra;

	ActionSimulator(WorldTest wt, String agentName, String msg, int delay, RemoteAgent ra){
		this._world = wt;
		this._msg = new String(msg);
		this._delay = delay;
		this._agentName = new String(agentName);
		this._ra = ra;

	}

	public void run(){
		
		StringTokenizer st = new StringTokenizer(_msg," ");

		String type = st.nextToken();
			
		//Simulate the time to complete the action
		try {
			Thread.sleep(_delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (type.equals("say")) {

			String aux = _msg.substring(3);
			SpeechAct say = (SpeechAct) SpeechAct.ParseFromXml(aux);
			if(say != null)
			{
				String actionName = say.getActionType() + "(";
				actionName += say.getReceiver() + ",";
				actionName += say.getMeaning();
				for(ListIterator li = say.GetParameters().listIterator(); li.hasNext();)
				{
					actionName += "," + li.next();
				}
				actionName += ")";

				synchronized(_ra){
					_ra.UpdateActionEffects(Name.ParseName(actionName));
				}

				String utterance = null;
				synchronized(_world){
					utterance = _world.Say(say.toLanguageEngine());
				}

				String receiver = say.getReceiver();
				String perception = "ACTION-FINISHED " + _agentName + " " + _msg;
				if (utterance != null)
				{
					_world.GetUserInterface().WriteLine(_agentName + " says to " + receiver + ": " + utterance);
					if (_world.GetGreta() != null)
						_world.GetGreta().Send(utterance);
				}
				else
				{
					_world.GetUserInterface().WriteLine(_agentName + " says to " + receiver + ": " + say.getMeaning());
				}

				synchronized(_world){
					_world.SendPerceptionToAll(perception);
				}
			}
		}
		else if (type.equals("UserSpeech"))
		{
			String perception = "ACTION-FINISHED " + _agentName + " " + _msg;
			synchronized(_world){
				_world.GetUserInterface().WriteLine(_agentName + " " + _msg);
				_world.SendPerceptionToAll(perception);
			}
		}
		else {
			//Corresponds to an action
			String aux = _agentName + " " + type;
			String target = null;
			String aux2 = type;
			if(st.hasMoreTokens()) {
				target = st.nextToken();
				aux = aux + " " + target;
				aux2 = type + " " + target;
			}

			while(st.hasMoreTokens())
			{
				String x = st.nextToken();
				aux = aux + " " + x;
				aux2= aux2 + " " + x;
			}

			synchronized(_ra){
				_ra.UpdateActionEffects(_ra.ConvertToActionName(new String (aux2)));
			}

			String perception = "ACTION-FINISHED " + aux;
			synchronized(_world){
				_world.GetUserInterface().WriteLine(aux);
				_world.SendPerceptionToAll(perception);
			}	
		}
	}
}
