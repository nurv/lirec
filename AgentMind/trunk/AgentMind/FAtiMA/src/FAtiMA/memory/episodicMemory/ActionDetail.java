/** 
 * ActionDetail.java - 
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
 * Created: 18/07/2006 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 18/07/2006 - File created
 * João Dias: 06/09/2006 - Changed everything about the evaluation
 * 						   used to determine the interpersonal relation between
 * 						   characters and objects. Now, the evaluation field is an arraylist
 * 						   of objects and characters and corresponding like values. These values
 * 						   are changed by emotions such as Pitty, HappyFor, Repproach
 * João Dias: 02/10/2006 - Changes in the way that parameters are compared for MemoryRetrieval
 * Bruno Azenha: 09/04/2007 - Reimplemented the method UpdateEmotionValues so that it uses the SocialRelation
 * 							  package
 * João Dias: 24/03/2008 - Added time to individual actions stored in AM
 * Meiyii Lim: 11/03/2009 - Added location to individual actions 
 * Meiyii Lim: 13/03/2009 - Moved the class from FAtiMA.autobiographicalMemory package
 */
package FAtiMA.memory.episodicMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.emotionalState.ActiveEmotion;
import FAtiMA.emotionalState.BaseEmotion;
import FAtiMA.memory.Memory;
import FAtiMA.memory.semanticMemory.KnowledgeSlot;
import FAtiMA.sensorEffector.Event;
import FAtiMA.sensorEffector.Parameter;
import FAtiMA.socialRelations.LikeRelation;
import FAtiMA.socialRelations.RespectRelation;
import FAtiMA.util.Constants;
import FAtiMA.util.enumerables.ActionEvent;
import FAtiMA.util.enumerables.EmotionType;
import FAtiMA.util.enumerables.EmotionValence;
import FAtiMA.util.enumerables.EventType;
import FAtiMA.util.enumerables.GoalEvent;


/**
 * @author João Dias
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ActionDetail implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int _id;
	
	private String _subject;
	private String _action;
	private String _target;	
	
	private KnowledgeSlot _subjectDetails = null;
	private KnowledgeSlot _targetDetails = null;
	private Time _time;
	private String _location;

	
	private BaseEmotion _emotion;
	
	private ArrayList<String> _evaluation;
	private ArrayList<Parameter> _parameters = null;
	
	// 06/01/10 - Meiyii
	private String _intention;
	private String _status;
	private String _speechActMeaning;
	private String _multimediaPath;
	private String _object;
	
	private float _desirability;
	private float _praiseworthiness;
	
		
	public ActionDetail(Memory m, int ID, Event e, String location)
	{  
		Parameter p;
		
		this._id = ID;
		
		this._subject = e.GetSubject();
		
		// Meiyii 07/01/10 separate events into intention and action
		if(e.GetType() == EventType.GOAL)
		{
			this._intention = e.GetAction();
			this._status = GoalEvent.GetName(e.GetStatus());
		}
		else if (e.GetType() == EventType.ACTION)
		{
			this._action = e.GetAction();
			this._status = ActionEvent.GetName(e.GetStatus());
		}
		
		this._target = e.GetTarget();
		this._location = location;
		this._time = new Time();
		
		if(this._subject != null)
		{
			_subjectDetails = m.getSemanticMemory().GetObjectDetails(this._subject);
		}
		
		if(this._target != null)
		{
			_targetDetails = m.getSemanticMemory().GetObjectDetails(this._target);
		}
		
		if(e.GetParameters() != null)
		{
			// Meiyii 07/01/10 separate the parameters into individual fields
			this._parameters = new ArrayList<Parameter>(e.GetParameters());
			ListIterator<Parameter> li = this._parameters.listIterator();
			while(li.hasNext())
			{
				p = li.next();
				if(p.GetName().equals("type"))
				{
					this._speechActMeaning = p.GetValue().toString();
				}				
				else if(p.GetName().equals("link"))
				{
					this._multimediaPath = p.GetValue().toString();
				}
				else if(p.GetName().equals("param"))
				{
					this._object = p.GetValue().toString();
				}
			}
		}
		
		this._emotion = new BaseEmotion(EmotionType.NEUTRAL,0,null,null);
		
		this._evaluation = new ArrayList<String>();
	}
	
	// not used currently
	public ActionDetail(int ID, String subject, String action, String target, ArrayList<Parameter> parameters, ArrayList<String> evaluation, Time time, String location, BaseEmotion emotion)
	{
		this._id = ID;
		
		this._subject = subject;
		this._action = action;
		this._target = target;
		this._location = location;
		
		this._time = time;
		this._emotion = emotion;
		
		this._evaluation = evaluation;
	}
	
	public String getSubject()
	{
		return this._subject;
	}
	
	public String getAction()
	{
		return this._action;
	}
	
	public String getTarget()
	{
		return this._target;
	}
	
	// Meiyii 11/03/09
	public String getLocation()
	{
		return this._location;
	}
	
	public ArrayList<Parameter> getParameters()
	{
		return this._parameters;
	}
	
	public int getID()
	{
		return this._id;
	}
	
	public Time getTime()
	{
		return this._time;
	}
	
	
	public Object getSubjectDetails(String property)
	{
		KnowledgeSlot aux;
		if(this._subjectDetails != null)
		{
			aux = this._subjectDetails.get(property);
			if(aux != null)
			{
				return aux.getValue();
			}
		}
		return null;
	}
	
	public Object getTargetDetails(String property)
	{
		KnowledgeSlot aux;
		if(this._targetDetails != null)
		{
			aux = this._targetDetails.get(property);
			if(aux != null)
			{
				return aux.getValue();
			}
		}
		return null;
	}
	
	public BaseEmotion getEmotion()
	{
		return this._emotion;
	}
	
	public ArrayList<String> getEvaluation()
	{
		return this._evaluation;
	}
	
	//Meiyii 07/01/10
	public String getIntention()
	{
		return this._intention;
	}
	
	public String getStatus()
	{
		return this._status;
	}
	
	public String getSpeechActMeaning()
	{
		return this._speechActMeaning;
	}
	
	public String getMultimediaPath()
	{
		return this._multimediaPath;
	}
	
	public String getObject()
	{
		return this._object;
	}
	
	public float getDesirability()
	{
		return this._desirability;
	}
	
	public float getPraiseworthiness()
	{
		return this._praiseworthiness;
	}
	
//	TODO em revisao 15.03.2007
	public boolean UpdateEmotionValues(Memory m, ActiveEmotion em)
	{
		boolean updated = false;
		if(em.GetIntensity() > this._emotion.GetPotential())
		{
			this._emotion = new BaseEmotion(em.GetType(),em.GetIntensity(),em.GetCause(),em.GetDirection());
			if(this._emotion.GetValence() == EmotionValence.POSITIVE)
			{
				this._desirability = (float) Math.floor(em.GetPotential());
			}
			else
			{
				this._desirability = (float) Math.floor(- em.GetPotential());
			}
			
			updated = true;
		}
		
		switch(em.GetType())
		{
			case EmotionType.ADMIRATION:
			{
				if(em.GetDirection() != null)
				{
					String aux = LikeRelation.getRelation(Constants.SELF,em.GetDirection().toString()).increment(m,em.GetIntensity());
					RespectRelation.getRelation(Constants.SELF,em.GetDirection().toString()).increment(m, em.GetIntensity());
					this._evaluation.add(aux);
					break;		
				}
			}
			case EmotionType.REPROACH:
			{
				if(em.GetDirection() != null)
				{
					String aux = LikeRelation.getRelation(Constants.SELF,em.GetDirection().toString()).decrement(m, em.GetIntensity());
					RespectRelation.getRelation(Constants.SELF,em.GetDirection().toString()).decrement(m, em.GetIntensity());
					this._evaluation.add(aux);
					break;
				}
			}
			case EmotionType.HAPPYFOR:
			{
				if(em.GetDirection() != null)
				{
					String aux = LikeRelation.getRelation(Constants.SELF,em.GetDirection().toString()).increment(m, em.GetIntensity());
					this._evaluation.add(aux);
					break;
				}
			}
			case EmotionType.GLOATING:
			{
				if(em.GetDirection() != null)
				{
					String aux = LikeRelation.getRelation(Constants.SELF,em.GetDirection().toString()).decrement(m, em.GetIntensity());
					this._evaluation.add(aux);
					break;
				}
			}
			case EmotionType.PITTY:
			{
				if(em.GetDirection() != null)
				{
					String aux = LikeRelation.getRelation(Constants.SELF,em.GetDirection().toString()).increment(m, em.GetIntensity());
					this._evaluation.add(aux);
					break;
				}
			}
			case EmotionType.RESENTMENT:
			{
				if(em.GetDirection() != null)
				{
					String aux = LikeRelation.getRelation(Constants.SELF,em.GetDirection().toString()).decrement(m, em.GetIntensity());
					this._evaluation.add(aux);
					break;
				}
			}			
			case EmotionType.JOY:
			{
				if(_target != null && _target.equals(Constants.SELF))
				{
					String aux = LikeRelation.getRelation(Constants.SELF,_subject).increment(m, em.GetIntensity());
					this._evaluation.add(aux);
				}
				break;
			}
			case EmotionType.DISTRESS:
			{
				if(_target != null && _target.equals(Constants.SELF))
				{
					String aux = LikeRelation.getRelation(Constants.SELF,_subject).decrement(m, em.GetIntensity());
					this._evaluation.add(aux);
				}
				break;
			}
		}
		
		return updated;
	}
	
	public boolean ReferencesEvent(Event e)
	{
		if(this._subject != null) {
			if(!this._subject.equals(e.GetSubject()))
			{
				return false;
			}
		}
		if(this._action != null)
		{
			if(!this._action.equals(e.GetAction()))
			{
				return false;
			}
		}
		if(this._target != null)
		{
			if(!this._target.equals(e.GetTarget()))
			{
				return false;
			}
		}
		if(this._parameters != null)
		{
			if(e.GetParameters() != null)
			{
				if(!this._parameters.toString().equals(e.GetParameters().toString()))
				{
					return false;
				}
			}
			else return false;
		}
		else if(e.GetParameters() != null)
		{
			return false;
		}
		return true;
	}
	
	/*public float AssessFamiliarity(Event e)
	{
		if(this._subject != null) {
			if(!this._subject.equals(e.GetSubject()))
			{
				return false;
			}
		}
		if(this._action != null)
		{
			if(!this._action.equals(e.GetAction()))
			{
				return false;
			}
		}
		if(this._target != null)
		{
			if(!this._target.equals(e.GetTarget()))
			{
				return false;
			}
		}
		if(this._parameters != null)
		{
			if(e.GetParameters() != null)
			{
				if(!this._parameters.toString().equals(e.GetParameters().toString()))
				{
					return false;
				}
			}
			else return false;
		}
		else if(e.GetParameters() != null)
		{
			return false;
		}
		return true;
		
	}*/
	
	@SuppressWarnings("unchecked")
	public boolean verifiesKey(SearchKey key)
	{
		if(key.getField() == SearchKey.ACTION) 
		{
			return key.getKey().equals(this._action);
		}
		else if(key.getField() == SearchKey.SUBJECT)
		{
			return key.getKey().equals(this._subject);
		}
		else if(key.getField() == SearchKey.TARGET)
		{
			return key.getKey().equals(this._target);
		}
		else if (key.getField() == SearchKey.MAXELAPSEDTIME)
		{
			long max = ((Long) key.getKey()).longValue();
			return _time.getElapsedNarrativeTime() <= max;
		}
		else if(key.getField() == SearchKey.PARAMETERS)
		{
			ArrayList<String> params = (ArrayList<String>) key.getKey();
			String aux;
			Parameter p;
			if(this._parameters.size() < params.size())
			{
				return false;
			}
			for(int i=0; i < params.size(); i++)
			{
				aux = params.get(i);
				p =  this._parameters.get(i);
				if(!aux.equals("*") && !aux.equals(p.GetValue()))
				{
					return false;
				}
			}
			return true;
		}
		else if(key.getField() == SearchKey.CONTAINSPARAMETER)
		{
			String param = (String) key.getKey();
			Parameter p;
			
			for(int i=0; i < this._parameters.size(); i++)
			{
				p = (Parameter) this._parameters.get(i);
				if(p.GetValue().equals(param))
				{
					return true;
				}
			}
			return false;
		}
		// Meiyii - additional search keys
		else if(key.getField() == SearchKey.INTENTION)
		{
			return key.getKey().equals(this._intention);
		}
		else if(key.getField() == SearchKey.STATUS)
		{
			if (this._intention != null)
				return key.getKey().equals(this._status);
			else 
				return key.getKey().equals(this._status);
		}
			
		else return false;
	}
	
	public boolean verifiesKeys(ArrayList<SearchKey> keys)
	{
		ListIterator<SearchKey> li = keys.listIterator();
		while(li.hasNext())
		{
			if(!this.verifiesKey(li.next()))
			{
				return false;
			}
		}
		return true;
	}

	public boolean equals(Object o)
	{
		ActionDetail action;
		
		if(!(o instanceof ActionDetail))
		{
			return false;
		}
		
		action = (ActionDetail) o;
		
		if(this._subject != null) {
			if(!this._subject.equals(action._subject))
			{
				return false;
			}
		}
		if(this._action != null)
		{
			if(!this._action.equals(action._action))
			{
				return false;
			}
		}
		if(this._target != null)
		{
			if(!this._target.equals(action._target))
			{
				return false;
			}
		}
		if(this._parameters != null)
		{
			if(action._parameters != null)
			{
				if(!this._parameters.toString().equals(action._parameters.toString()))
				{
					return false;
				}
			}
			else return false;
		}
		else if(action._parameters != null)
		{
			return false;
		}
		
		return true;
	}
	
	public String toXML()
	{
		String action = "<Event>";
		action += "<EventID>" + this.getID() + "</EventID>";
		action += "<Emotion>" + EmotionType.GetName(this.getEmotion().GetType()) + " " + this.getEmotion().GetPotential() + "</Emotion>";
		action += "<Subject>" + this.getSubject() + "</Subject>";
		action += "<Intention>" + this.getIntention() + "</Intention>";
		action += "<Status>" + this.getStatus() + "</Status>";
		action += "<Action>" + this.getAction() + "</Action>";
		action += "<Target>" + this.getTarget() + "</Target>";
		action += "<Parameters>" + this.getParameters() + "</Parameters>";
		action += "<SpeechActMeaning>" + this.getSpeechActMeaning() + "</SpeechActMeaning>";
		action += "<MultimediaPath>" + this.getMultimediaPath() + "</MultimediaPath>";
		action += "<Object>" + this.getObject() + "</Object>";
		action += "<Desirability>" + this.getDesirability() + "</Desirability>";
		action += "<Praiseworthiness>" + this.getPraiseworthiness() + "</Praiseworthiness>";
		//action += "<Evaluation>" + this.getEvaluation() + "</Evaluation>";
		action += "<Time>" + this.getTime().getNarrativeTime() + "</Time>";
		action += "<Location>" + this.getLocation() + "</Location>";
		
		action += "</Event>\n";
		
		return action;
	}
}
