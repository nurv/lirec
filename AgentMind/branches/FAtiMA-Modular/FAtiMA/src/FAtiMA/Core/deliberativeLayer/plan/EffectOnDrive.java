/** 
 * EffectOnDrive.java - Represents an operator's effect on the motivators
 * 
 */

package FAtiMA.Core.deliberativeLayer.plan;

import java.io.Serializable;
import java.util.ArrayList;

import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.wellFormedNames.IGroundable;
import FAtiMA.Core.wellFormedNames.Substitution;

/**
 * Represents an operator's effect on the motivators
 *  
 * @author Meiyii Lim
 */

public class EffectOnDrive implements IGroundable, Cloneable, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Condition _effectOnDrive;
	
	/**
	 * Creates a new Effect
	 * @param stepName - the name of the step that this effect bellongs to
	 * @param prob - the effect's probability
	 * @param effect - the condition that represents the effect
	 */
	public EffectOnDrive(Condition effectOnDrive) {
		this._effectOnDrive = effectOnDrive;
	}
		
	
	public EffectOnDrive() {
	}
	
	/**
	 * Gets the condition that represents the Effect
	 * @return the effect (represented as a condition)
	 */
	public Condition GetEffectOnDrive() {
		return _effectOnDrive;
	}
	
	/**
	 * @deprecated use ReplaceUnboundVariables instead
	 * Replaces all unbound variables in the Effect by applying a numeric
	 * identifier to each one.
	 * Example: the variable [X] becomes [X4] if the received ID is 4.
	 * @param id - the identifier to be applied
	 * @return the new Effect with the variables changed 
	 */
	public Object GenerateName(int id) {
		EffectOnDrive aux = (EffectOnDrive) this.clone();
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
    	this._effectOnDrive.ReplaceUnboundVariables(variableID);
    }
	
	/**
	 * @deprecated use MakeGround(ArrayList) instead
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)".
	 * @param bindings - A list of substitutions of the type "[Variable]/value"
	 * @see Substitution
	 * @return a new Effect with its variables grounded
	 */
	public Object Ground(ArrayList<Substitution> substs) {
		Effect aux = (Effect) this.clone();
		aux.MakeGround(substs);
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
    public void MakeGround(ArrayList<Substitution> bindings)
    {
    	this._effectOnDrive.MakeGround(bindings);
    }
    
    /**
     * @deprecated use the method MakeGround(Substitution) instead
	 * Applies a substitution to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)".
	 * @param subst - a substitution of the type "[Variable]/value"
	 * @return a new Effect with the substitution applied
	 * @see Substitution
	 */
	public Object Ground(Substitution subst)
	{
		EffectOnDrive aux = (EffectOnDrive) this.clone();
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
    	this._effectOnDrive.MakeGround(subst);
    }
    
    /**
	 * Indicates if the name is grounded (no unbound variables in it's WFN)
	 * Example: Stronger(Luke,John) is grounded while Stronger(John,[X]) is not.
	 * @return true if the name is grounded, false otherwise
	 */
    public boolean isGrounded()
    {
    	return this._effectOnDrive.isGrounded();
    }
	
	/**
	 * Clones this effect on drive, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The effects's copy.
	 */
	public Object clone()
	{
	    EffectOnDrive ed = new EffectOnDrive();
	    ed._effectOnDrive = (Condition) this._effectOnDrive.clone();
	    
	    return ed;
	}
	
	/**
	 * Converts the Effect to a String
	 * @return the converted String
	 */
	public String toString() {
		return "EffectOnDrive: " + _effectOnDrive;
	}
}
