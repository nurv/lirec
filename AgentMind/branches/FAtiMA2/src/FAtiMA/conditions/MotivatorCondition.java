package FAtiMA.conditions;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import FAtiMA.util.AgentLogger;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.Symbol;

public class MotivatorCondition extends Condition {
	
	/**
	 * Parses a Motivator given a XML attribute list
	 * @param attributes - A list of XML attributes
	 * @return - the Motivator Parsed
	 */
	public static MotivatorCondition ParseMotivator(Attributes attributes) {
		MotivatorCondition cond;
		String drive;
		Name target;
		float value;

		drive = attributes.getValue("drive");
		target = Name.ParseName(attributes.getValue("target"));
		value = Float.parseFloat(attributes.getValue("value"));

		return new MotivatorCondition(drive,target,value);
			
	}

	protected String _drive;
	protected Name _target;
	protected float _value;
	
	private MotivatorCondition()
	{
	}

	/**
	 * Creates a new Motivator
	 * @param drive - the motivator's drive
	 * @param target - the agent of whom the drive applied to
	 * @param value - the motivator's value
	 */
	public MotivatorCondition(String drive, Name target, float value) {
		this._drive = drive;
		this._target = target;
		this._value = value;
	}

	/**
	 * Checks if the Motivator Condition is verified in the agent's memory (KB + AM + Motivational System)
	 * @return true if the condition is verified, false otherwise
	 */
	public boolean CheckCondition() {
		//TODO: implment this based on the motivational system
		return true;
		
	}

	/**
	 * Gets the Motivator's test value
	 * @return the test value of the property
	 */
	public Name GetValue() {
		return new Symbol(Float.toString(this._value));
	}
	
	/**
	 * Gets the drive
	 * @return the name of the drive
	 */
	public String GetDrive()
	{
		return _drive;
	}
	
	/**
	 * Gets the effect on a drive
	 * @return the value of the drive
	 */
	public float GetEffect()
	{
		return _value;
	}
	
	/**
	 * Gets the target of a drive
	 * @return the value of the drive
	 */
	public Name GetTarget()
	{
		return _target;
	}
	
	/**
     * @deprecated use ReplaceUnboundVariables(int) instead.
	 * Replaces all unbound variables in the object by applying a numeric
	 * identifier to each one.
	 * Example: the variable [X] becomes [X4] if the received ID is 4.
	 * @param variableID - the identifier to be applied
	 * @return a new Property with the variables changed 
	 */
	public Object GenerateName(int id)
	{
		MotivatorCondition aux = (MotivatorCondition) this.clone();
		aux.ReplaceUnboundVariables(id);
		return aux;
	}
	
	
	/**
	 * Replaces all unbound variables in the object by applying a numeric 
     * identifier to each one. For example, the variable [x] becomes [x4]
     * if the received ID is 4. 
     * Attention, this method modifies the original object.
     * @param variableID - the identifier to be applied
	 */
    public void ReplaceUnboundVariables(int variableID) 
    {
    	this._target.ReplaceUnboundVariables(variableID);
    }
    
    /**
     * @deprecated use the method MakeGround(ArrayList) instead
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)".
	 * @param bindings - A list of substitutions of the type "[Variable]/value"
	 * @return a new Property with the substitutions applied
	 * @see Substitution
	 */
	public Object Ground(ArrayList bindingConstraints)
	{
		MotivatorCondition aux = (MotivatorCondition) this.clone();
		aux.MakeGround(bindingConstraints);
		return aux;
	}

	/**
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)". 
	 * Attention, this method modifies the original object.
	 * @param bindings - A list of substitutions of the type "[Variable]/value"
	 * @see Substitution
	 */
    public void MakeGround(ArrayList bindings)
    {
    	this._target.MakeGround(bindings);
    }
    
   
    /**
     * @deprecated use the method MakeGround(Substitution) instead
	 * Applies a substitution to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)".
	 * @param subst - a substitution of the type "[Variable]/value"
	 * @return a new Property with the substitution applied
	 * @see Substitution
	 */
	public Object Ground(Substitution subst)
	{
		MotivatorCondition aux = (MotivatorCondition) this.clone();
		aux.MakeGround(subst);
		return aux;
	}

	/**
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)". 
	 * Attention, this method modifies the original object.
	 * @param subst - a substitution of the type "[Variable]/value"
	 * @see Substitution
	 */
    public void MakeGround(Substitution subst)
    {
    	this._target.MakeGround(subst);
    }

	/**
	 * Indicates if the condition is grounded (no unbound variables in it's WFN)
	 * Example: Stronger(Luke,John) is grounded while Stronger(John,[X]) is not.
	 * @return true if the condition is grounded, false otherwise
	 */
	public boolean isGrounded() {
		return _target.isGrounded(); 
	}

	/**
	 * Prints the Motivator to the Standard Output
	 */
	public void Print() {
		AgentLogger.GetInstance().logAndPrint("Motivator=" + _drive + " target=" + _target + " value=" + _value + "\n");
	}
	
	protected ArrayList GetBindings(Name groundValue, Name value) {
		return null;
		//TODO: implement using motivational component
	}

	/**
	 * Find a set of Substitutions for the second part of the condition, which will 
	 * make it become true. With this method it is possible to test conditions that
	 * have unbound variables in the second part such as: 
     * "Owner(Ball) = [x]" 
     * this condition will be true if there is anyone in the world that owns a Ball.
     * If John owns the ball, the method returns [x]/John
     * @return returns all set of Substitutions that make the condition valid.
     */
	protected ArrayList GetValueBindings() {
		return null;
		//TODO: implement using motivational component
	}

	/**
	 * Clones this MotivatorCondition, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The MotivatorCondition's copy.
	 */
	public Object clone()
	{
		MotivatorCondition mc = new MotivatorCondition();
		mc._drive = this._drive;
		mc._value = this._value;
		mc._target = (Name) this._target.clone();
	    
		return mc;
	}
}
