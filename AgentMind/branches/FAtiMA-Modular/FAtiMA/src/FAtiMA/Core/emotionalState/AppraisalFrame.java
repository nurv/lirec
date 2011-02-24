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
package FAtiMA.Core.emotionalState;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

import FAtiMA.Core.sensorEffector.Event;

public class AppraisalFrame implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final short DEFAULTCOMPONENTWEIGHT = 5;

	private class Pair implements Serializable
	{
		private static final long serialVersionUID = 1L;
		
		protected float _value;
		protected short _weight;
		
		public Pair(short weight, float value)
		{
			this._value = value;
			this._weight = weight;
		}
	}
	
	private class AppraisalVariable implements Serializable
	{

		private static final long serialVersionUID = 1L;
		
		protected HashMap<String,Pair> _values;
		protected short _weight;
		
		public AppraisalVariable(String name)
		{
			_values = new HashMap<String,Pair>();
		}
	}
	
	private Event _event;
	private HashMap<String,AppraisalVariable> _appraisal;
	
	
	private boolean _changed;
	private boolean _empty;
	
	public AppraisalFrame(Event e)
	{
		_event = e;
		_changed = false;
		_empty = true;
		_appraisal = new HashMap<String,AppraisalVariable>();
	}
	
	public Event getEvent()
	{
		return _event;
	}
	
	public void SetAppraisalVariable(String componentName, String appraisalVariableName, float value)
	{
		SetAppraisalVariable(componentName, DEFAULTCOMPONENTWEIGHT, appraisalVariableName, value);
	}
	
	public void SetAppraisalVariable(String componentName, short componentWeight, String appraisalVariableName, float value)
	{
		AppraisalVariable av;
		
		if(_appraisal.containsKey(appraisalVariableName))
		{
			av = _appraisal.get(appraisalVariableName);
		}
		else
		{
			av = new AppraisalVariable(appraisalVariableName);
			_appraisal.put(appraisalVariableName, av);
		}
		
		//replacing or setting up a new value?
		if(av._values.containsKey(componentName))
		{
			//replacing an existing value
			Pair p = av._values.get(componentName);
			if(p._value != value)
			{
				//if the values are different, replace them
				p._value = value;
				_changed = true;
			
				//nothing else needs to be done in this case
			}
		}
		else
		{
			//setting up a new value
			Pair p = new Pair(componentWeight, value);
			av._weight += componentWeight;
			
			av._values.put(componentName,p);
			_empty = false;
		
			_changed = true;	
		}	
	}
	
	public Collection<String> getAppraisalVariables()
	{
		return _appraisal.keySet();
	}
	
	public float getAppraisalVariable(String appraisalVariable)
	{
		float appraisalValue = 0;
		if(_appraisal.containsKey(appraisalVariable))
		{
			AppraisalVariable av = _appraisal.get(appraisalVariable);
			if(av._weight > 0)
			{			
				for(Pair p : av._values.values())
				{
					appraisalValue += (p._weight/av._weight) * p._value;
				}
			}	
		}
		
		return appraisalValue;
	}
	
	public boolean containsAppraisalVariable(String appraisalVariable)
	{
		return _appraisal.containsKey(appraisalVariable);
	}
	
	public boolean hasChanged()
	{
		boolean aux = _changed;
		_changed = false;
		
		return aux;
	}
	
	public boolean isEmpty()
	{
		return _empty;
	}
}
