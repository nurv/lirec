package FAtiMA.advancedMemoryComponent;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.conditions.Condition;
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

public class SACondition extends Condition{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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
		System.out.println("query " + query + " " + value);
		return new SACondition(query,value);
	}
	
	protected Hashtable<String, Symbol> _knownVariables;
	protected String _query;
	protected Symbol _value;
	
	private SACondition()
	{
	}
	
	private SACondition(String query, Symbol value)
	{
		this._query = query;
		this._value = value;
		this._knownVariables = new Hashtable<String, Symbol>();
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
	}
	
	public void AddKnownVariables(String name, Symbol value)
	{
		this._knownVariables.put(name, value);
	}
	
	@Override
	public boolean CheckCondition(AgentModel am) {
		return this._value.isGrounded();
	}

	@Override
	public Name GetValue() {
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
		ArrayList<String> knownInfo = new ArrayList<String>();
		
		 IComponent c = am.getComponent(AdvancedMemoryComponent.NAME);
		 if(c == null)
		 {
			 throw new RuntimeException("AvancedMemory Component not initialized");
		 }
		 
		 AdvancedMemoryComponent mc = (AdvancedMemoryComponent) c;
		
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
				knownInfo.add(key.trim() + " " + value.toString().trim());
				System.out.println("SAKnown " + key + " " + value.toString());
			}
		}
		
		
		
		mc.getSpreadActivate().Spread(_query, knownInfo, am.getMemory().getEpisodicMemory());
		String result = mc.getSpreadActivate().getSABestResult();
		System.out.println("Result " + result);
		sset = new SubstitutionSet();
		sset.AddSubstitution(new Substitution(this._value, new Symbol(result)));
		subs.add(sset);
		
		return subs;
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
