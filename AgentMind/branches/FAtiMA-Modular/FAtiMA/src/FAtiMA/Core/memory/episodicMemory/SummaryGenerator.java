/** 
 * SummaryGenerator.java - Abstract class that acts as a method repository of methods
 * used to generate Memory Summaries 
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
 * Created: 04/April/2007 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 04/April/2007 - File created
 * **/

package FAtiMA.Core.memory.episodicMemory;

import java.util.ArrayList;

import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.sensorEffector.Parameter;
import FAtiMA.Core.sensorEffector.SpeechAct;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;

/**
 * Abstract class that acts as a method repository of methods
 * used to generate Memory Summaries
 * 
 * @author Jovem Engenheiro
 */
public abstract class SummaryGenerator {
	 
	public static String GenerateActionSummary(Memory m, ActionDetail action)
	{
		
		String actionSummary = "<Subject>";
		String actionName;
		String status;
		
		if(action.getSubject().equals(Constants.SELF))
		{
			actionSummary += "I";
		}
		else
		{
			actionSummary += translateNameToDisplayName(m, action.getSubject());
		}
		
		actionSummary += "</Subject>"; 
		
		actionName = action.getAction();
		
		actionSummary += "<Action>";
		
		if(actionName != null)
		{
			
			//normal action
			if(SpeechAct.isSpeechAct(actionName))
			{
				ArrayList<Parameter> params = action.getParameters();
				actionSummary += params.get(0);
				
				if(actionName.equals(SpeechAct.Reply))
				{
					actionSummary += params.get(1);
				}
					
					/*if(action.getTarget().equals(AutobiographicalMemory.GetInstance().getSelf()))
					{
						description += "my ";
					}
					else
					{
						description += action.getTarget() + "'s ";
					}
					
					description += params.get(0);
					
					return description;
				*/
			}
			else 
			{
				actionSummary += actionName;
			}
			
		}
		else
		{
			actionSummary += action.getIntention();
		}
		actionSummary += "</Action>";
		
		status = action.getStatus();
		
		if(status != null)
		{
			actionSummary += "<Status>" + status + "</Status>";
		}
		
		if(action.getTarget() != null)
		{
			actionSummary += "<Target>";
			
			if(action.getTarget().equals(Constants.SELF))
			{
				actionSummary += "me";
			}
			else
			{
				Object aux = action.getTargetDetails("type");
				if(aux != null)
				{
					if(aux.equals("object"))
					{
						Object aux2 = action.getTargetDetails("owner");
						if(aux2 != null)
						{
							if(Constants.SELF.equals(aux2))
							{
								actionSummary += "my ";
							
							}
							else
							{
								actionSummary += translateNameToDisplayName(m, aux2.toString()) + "'s "; 
							}
						}
					}
				}
				
				actionSummary += translateNameToDisplayName(m, action.getTarget());
			}
			
			actionSummary += "</Target>";
		}
		
		if(action.getParameters().size() > 0)
		{
			actionSummary += "<Param>" + 
				translateNameToDisplayName(m, action.getParameters().get(0).toString()) +
				"</Param>";
		}
		
		return actionSummary;
	}
	
	public static String GenerateEmotionSummary(Memory m, BaseEmotion em)
	{
		String EMSummary = "<Emotion intensity=\"";
		
		if(em.GetPotential() > 5) 
		{
			EMSummary += "high";
		}
		else if(em.GetPotential() > 3)
		{
			EMSummary += "normal";
		}
		else
		{			
			EMSummary += "little";
		}
		EMSummary += "\" ";
		
		if(em.GetDirection() != null)
		{
			String direction = translateNameToDisplayName(m, em.GetDirection().toString());
			
			EMSummary += "direction=\"" + direction + "\"";
		}
		
		EMSummary += ">"+em.getType() + "</Emotion>";
		
		return EMSummary;
	}
	
	public static String generateTimeDescription(long time)
	{
		int months = Math.round(time/259200000); //months
		
		if(months > 0)
		{
			return "<Time count=\"" + months + "\">month</Time>";
		}
		
		int weeks = Math.round(time/60480000); //weeks
		
		if(weeks > 0)
		{
			return "<Time count=\"" + weeks + "\">week</Time>";
		}
		
		int days = Math.round(time/8640000); //days
		if(days > 0)
		{
			return "<Time count=\"" + days + "\">day</Time>";
		}
		
		int hours = Math.round(time/360000); //hours 
		return "<Time count=\"" + hours + "\">hour</Time>";
	}
	
	public static String translateNameToDisplayName(Memory m, String name)
	{
		Object displayName = m.getSemanticMemory().AskProperty(Name.ParseName(name + "(displayName)"));
		if(displayName != null)
		{
			return displayName.toString();
		}
		else return name;
	}

}
