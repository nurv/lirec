/** 
 * AppraisalVector.java - Class that represent a vector with OCC's appraisal variable
 *  
 * Copyright (C) 2009 GAIPS/INESC-ID 
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
 * Created: 2009 
 * @author: João Dias
 * Email to: joao.dias@gaips.inesc-id.pt
 * 
 * History: 
 */
package FAtiMA.emotionalState;

import java.util.ArrayList;
import java.util.HashMap;

public class AppraisalStructure {
	
	
	private class Pair
	{
		protected float _value;
		protected short _weight;
		
		public Pair(short weight, float value)
		{
			this._value = value;
			this._weight = weight;
		}
	}
	
	public static final short LIKE = 0;
	public static final short DESIRABILITY = 1;
	public static final short DESIRABILITY_FOR_OTHER = 2;
	public static final short PRAISEWORTHINESS = 3;
	
	private short[] _weights = {0,0,0,0};
	private ArrayList<HashMap<String,Pair>> _appraisal;
	
	
	private boolean _changed;
	
	public AppraisalStructure()
	{
		_changed = true;
		_appraisal = new ArrayList<HashMap<String,Pair>>(4);
	}
	
	public void SetAppraisalVariable(String component, short weight, short appraisalVariable, float value)
	{
		HashMap<String,Pair> a = _appraisal.get(appraisalVariable);
		//replacing or setting up a new value?
		if(a.containsKey(component))
		{
			//replacing an existing value
			Pair p = a.get(component);
			if(p._value != value)
			{
				//if the values are different, replace them
				p._value = value;
				_changed = true;
				//nothing else needs to be done in this case
			}
			return;
		}
		Pair p = new Pair(weight, value);
		_weights[appraisalVariable] += weight;
		a.put(component,p);
		_changed = true;
	}
	
	
	public float getAppraisalVariable(short appraisalVariable)
	{
		short totalWeight = _weights[appraisalVariable];
		short appraisalValue = 0;
		
		if(totalWeight > 0)
		{
			HashMap<String,Pair> a = _appraisal.get(appraisalVariable);
			
			for(Pair p : a.values())
			{
				appraisalValue += (p._weight/totalWeight) * p._value;
			}
		}
		
		return appraisalValue;
	}
	
	public boolean hasChanged()
	{
		boolean aux = _changed;
		_changed = false;
		return aux;
	}
}
