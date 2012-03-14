package FAtiMA.AdvancedMemory;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;

/**
 *  Parses a SACondition in Actions.xml file
 * 
 *  @author Meiyii Lim
 *  2010-04-18
 */

public class SACondition extends Condition {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String TIME_ONTOLOGY = "timeOntology";
	public static final String TARGET_ONTOLOGY = "targetOntology";
	public static final String OBJECT_ONTOLOGY = "objectOntology";
	public static final String LOCATION_ONTOLOGY = "locationOntology";
	public static int TIME_DIFFERENCE = 9;
	
	/**
	 * Parses a SpreadActivateCondition given a XML attribute list
	 * @param attributes - A list of XML attributes
	 * @return - the SpreadActivateCondition
	 */
	public static SACondition ParseSA(Attributes attributes) {
		String query;
		Symbol value;

		query = attributes.getValue("query");		
		value = new Symbol(attributes.getValue("value"));
		//System.out.println("query " + query + " " + value);
		return new SACondition(query,value);
	}
	
	protected Hashtable<String, Symbol> _knownVariables;
	protected Hashtable<String, String> _ontology;
	protected String _query;
	protected Symbol _value;
	
	private SACondition()
	{
	}
	
	private SACondition(String query, Symbol value)
	{
		super(Name.ParseName("SACondition(" + query + "," + value +")"),Constants.UNIVERSAL);
		this._query = query;
		this._value = value;
		this._knownVariables = new Hashtable<String, Symbol>();
		this._ontology = new Hashtable<String, String>();
	}
	
	private SACondition(SACondition sac)
	{
		super(sac);
		this._query = sac._query;
		this._value = (Symbol) sac._value.clone();
		
		this._knownVariables = new Hashtable<String, Symbol>();
		Iterator<String> it = sac._knownVariables.keySet().iterator();
		while (it.hasNext())
		{
			String key = it.next();
			this._knownVariables.put(key, (Symbol) sac._knownVariables.get(key).clone());
		}
		
		this._ontology = new Hashtable<String, String>();
		it = sac._ontology.keySet().iterator();
		while (it.hasNext())
		{
			String key = it.next();
			this._ontology.put(key, (String) sac._ontology.get(key));
		}
	}
	
	public void AddKnownVariables(String name, Symbol value)
	{
		this._knownVariables.put(name, value);
	}
	
	public void AddOntology(String name, String value)
	{
		this._ontology.put(name, value);
	}
	
	@Override
	public float CheckCondition(AgentModel am) {
		
		if(!this._value.isGrounded()) return 0;
		
		String resultStr = this.SpreadActivate(am);
		
		if(resultStr!=null && resultStr.equalsIgnoreCase(this._value.toString()))
		{
			return 1;
		}
		else return 0;
			
	}

	@Override
	public Name getValue() {
		return this._value;
	}

	public String GetQuery() {
		return this._query;
	}
	
	public Hashtable<String, Symbol> GetKnownVariables() {
		return this._knownVariables;
	}
	
	@Override
	protected ArrayList<Substitution> GetValueBindings(AgentModel am) {
		return null;
	}
	
	public ArrayList<SubstitutionSet> GetValidBindings(AgentModel am) {
		SubstitutionSet sset;
		ArrayList<SubstitutionSet> subs = new ArrayList<SubstitutionSet>();
		
		String resultStr = this.SpreadActivate(am);
		if(resultStr != null)
		{	
			sset = new SubstitutionSet();
			sset.AddSubstitution(new Substitution(this._value, new Symbol(resultStr)));
			subs.add(sset);
		}	
		
		return subs;
	}
	
	private String SpreadActivate(AgentModel am)
	{
		int timeDifference = 0;
		
		String saPerception = "";
		
		 IComponent c = am.getComponent(AdvancedMemoryComponent.NAME);
		 if(c == null)
		 {
			 throw new RuntimeException("AvancedMemory Component not initialized");
		 }
		 
		 AdvancedMemoryComponent mc = (AdvancedMemoryComponent) c;
		
		 saPerception += this._query + "$";
		 
		Iterator<String> it = this._knownVariables.keySet().iterator();
		while (it.hasNext())
		{
			String key = it.next();
			Symbol value = this._knownVariables.get(key);
			if(!value.isGrounded()) 
			{
				return null;
			}
			else
			{
				if(value.toString().equals("Exit")){
					timeDifference = TIME_DIFFERENCE;
				}
				saPerception += key.trim() + " " + value.toString().trim();
				if (it.hasNext())
					saPerception += "*";
				else
					saPerception +="$";
				//System.out.println("SAKnown " + key + " " + value.toString());
			}
		}
		
		if(this._ontology.size() > 0){
			if (this._ontology.get(TIME_ONTOLOGY) != null)
				saPerception += this._ontology.get(TIME_ONTOLOGY) + "$";
			else
				saPerception += "0$";
			
			if (this._ontology.get(TARGET_ONTOLOGY) != null)
				saPerception += this._ontology.get(TARGET_ONTOLOGY) + "$";
			else
				saPerception += "0$";
			
			if (this._ontology.get(OBJECT_ONTOLOGY) != null)
				saPerception += this._ontology.get(OBJECT_ONTOLOGY) + "$";
			else
				saPerception += "0$";
			
			if (this._ontology.get(LOCATION_ONTOLOGY) != null)
				saPerception += this._ontology.get(LOCATION_ONTOLOGY) + "$";
			else
				saPerception += "0";
		}
		
		// execute Spreading Activation mechanism
		SpreadingActivation spreadingActivation = new SpreadingActivation();
		Object saResult = mc.processSAPerception(am, saPerception, spreadingActivation);
		if (saResult  != null)
		{
			String resultStr = (String) saResult;
			if (_query.equals("time")){
					int resultValue = Integer.valueOf(resultStr);
					if (resultValue < timeDifference)
						resultValue += 24;
					resultValue -= timeDifference;
				resultStr = String.valueOf(resultValue);
			}
			//System.out.println("saPerception " + saPerception);
			//System.out.println("saResult " + resultStr);
			
			return resultStr;
		}		
		return null;
	}

	@Override
	public Object clone() {
		return new SACondition(this);
	}

	public void MakeGround(ArrayList<Substitution> bindings) {
		super.MakeGround(bindings);
		this._value.MakeGround(bindings);
		Iterator<Symbol> it = this._knownVariables.values().iterator();
		while (it.hasNext())
		{
			it.next().MakeGround(bindings);
		}
	}

	@Override
	public void MakeGround(Substitution subst) {
		super.MakeGround(subst);
		this._value.MakeGround(subst);
		Iterator<Symbol> it = this._knownVariables.values().iterator();
		while (it.hasNext())
		{
			it.next().MakeGround(subst);
		}
	}

	@Override
	public void ReplaceUnboundVariables(int variableID) {
		super.ReplaceUnboundVariables(variableID);
		this._value.ReplaceUnboundVariables(variableID);
		Iterator<Symbol> it = this._knownVariables.values().iterator();
		while (it.hasNext())
		{
			it.next().ReplaceUnboundVariables(variableID);
		}
	}

	@Override
	public boolean isGrounded() {
		if(!this._value.isGrounded()) return false;
		Iterator<Symbol> it = this._knownVariables.values().iterator();
		while (it.hasNext())
		{
			if(!it.next().isGrounded()) return false;
		}
		return true;
	}
}
