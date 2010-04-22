package FAtiMA.conditions;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.xml.sax.Attributes;

import FAtiMA.AgentModel;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.SubstitutionSet;
import FAtiMA.wellFormedNames.Symbol;

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
				
		am.getSpreadActivate().Spread(_query, knownInfo, am.getMemory().getEpisodicMemory());
		String result = am.getSpreadActivate().getSABestResult();
		System.out.println("Result " + result);
		sset = new SubstitutionSet();
		sset.AddSubstitution(new Substitution(this._value,new Symbol(result)));
		subs.add(sset);
		
		return subs;
	}

	@Override
	public Object clone() {
		SACondition sac = new SACondition();
		sac._query = this._query;
		sac._value = (Symbol) this._value.clone();
		sac._knownVariables = (Hashtable<String, Symbol>) this._knownVariables.clone();
		return sac;
	}

	public Object GenerateName(int id) {
		SACondition sa = (SACondition) this.clone();
		sa.ReplaceUnboundVariables(id);
		return sa;
	}

	@Override
	public Object Ground(ArrayList<Substitution> bindingConstraints) {
		SACondition sa = (SACondition) this.clone();
		sa.MakeGround(bindingConstraints);
		return sa;
	}

	public Object Ground(Substitution subst) {
		SACondition sa = (SACondition) this.clone();
		sa.MakeGround(subst);
		return sa;
	}

	public void MakeGround(ArrayList<Substitution> bindings) {
		this._value.MakeGround(bindings);
		Iterator<String> it = this._knownVariables.keySet().iterator();
		while (it.hasNext())
		{
			String key = it.next();
			this._knownVariables.get(key).MakeGround(bindings);
		}
	}

	@Override
	public void MakeGround(Substitution subst) {
		this._value.MakeGround(subst);
		Iterator<String> it = this._knownVariables.keySet().iterator();
		while (it.hasNext())
		{
			String key = it.next();
			this._knownVariables.get(key).MakeGround(subst);
		}
	}

	@Override
	public void ReplaceUnboundVariables(int variableID) {
		this._value.ReplaceUnboundVariables(variableID);
		Iterator<String> it = this._knownVariables.keySet().iterator();
		while (it.hasNext())
		{
			String key = it.next();
			this._knownVariables.get(key).ReplaceUnboundVariables(variableID);
		}
	}

	@Override
	public boolean isGrounded() {
		if(!this._value.isGrounded()) return false;
		Iterator<String> it = this._knownVariables.keySet().iterator();
		while (it.hasNext())
		{
			String key = it.next();
			if(!this._knownVariables.get(key).isGrounded()) return false;
		}
		return true;
	}

}
