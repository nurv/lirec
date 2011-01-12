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

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.sensorEffector.Event;

public class AppraisalFrame implements Serializable {
	
	
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
	
	private class AppraisalVariable
	{
		protected HashMap<String,Pair> _values;
		protected short _weight;
		
		public AppraisalVariable(String name)
		{
			_values = new HashMap<String,Pair>();
		}
	}
	
	private Event _event;
	private AgentModel _am;
	private HashMap<String,AppraisalVariable> _appraisal;
	
	
	private boolean _changed;
	private boolean _empty;
	
	public AppraisalFrame(AgentModel am, Event e)
	{
		_event = e;
		_changed = true;
		_empty = true;
		_am = am;
		_appraisal = new HashMap<String,AppraisalVariable>();
	}
	
	public Event getEvent()
	{
		return _event;
	}
	
	public void SetAppraisalVariable(String component, short weight, String appraisalVariable, float value)
	{
		AppraisalVariable av;
		
		if(_appraisal.containsKey(appraisalVariable))
		{
			av = _appraisal.get(appraisalVariable);
		}
		else
		{
			av = new AppraisalVariable(appraisalVariable);
			_appraisal.put(appraisalVariable, av);
		}
		
		//replacing or setting up a new value?
		if(av._values.containsKey(component))
		{
			//replacing an existing value
			Pair p = av._values.get(component);
			if(p._value != value)
			{
				//if the values are different, replace them
				p._value = value;
				_changed = true;
				if(_am != null)
				{
					_am.updateEmotions(appraisalVariable, this);
				}
				//nothing else needs to be done in this case
			}
		}
		else
		{
			//setting up a new value
			Pair p = new Pair(weight, value);
			av._weight += weight;
			
			av._values.put(component,p);
			_empty = false;
			if(_am != null)
			{
				_am.updateEmotions(appraisalVariable, this);
			}
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
