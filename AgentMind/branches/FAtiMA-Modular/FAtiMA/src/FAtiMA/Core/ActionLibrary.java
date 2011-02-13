/** 
 * ActionLibrary.java - Class that stores the STRIPS definition of the domain operators,
 * 						also called actions
 *  
 * Copyright (C) 2008 GAIPS/INESC-ID 
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
 * Created: 12/03/2008 
 * @author: João Dias
 * Email to: joao.dias@gaips.inesc-id-pt
 * 
 * History: 
 * João Dias: 12/03/2008 - File created
 */
package FAtiMA.Core;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.exceptions.ActionsParsingException;
import FAtiMA.Core.exceptions.UnknownSpeechActException;
import FAtiMA.Core.exceptions.UnspecifiedVariableException;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.parsers.ActionsLoaderHandler;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Unifier;

/**
 * @author João Dias
 * Class that stores the STRIPS definition of the domain actions
 */
public class ActionLibrary implements Serializable {

	private static final long serialVersionUID = 1L;	
	
	private ArrayList<Step> _actions;
	

	public ActionLibrary()
	{
		_actions = new ArrayList<Step>();
	}
	
	public void addAction(Step action)
	{
		_actions.add(action);
	}
	
	/**
	 * Checks the integrity of the Planner operators/Steps/actions.
	 * For instance it checks if a operator references a SpeechAct not 
	 * defined, or if it uses a unbound variable (in effects or preconditions)
	 * not used in the operator's name 
	 * @param val - the IntegrityValidator used to detect problems
	 * @throws UnspecifiedVariableException - thrown when the operator uses a unbound
	 * 										  variable in the effects or preconditions
	 * 									      without using the same variable in the 
	 * 									      step's name
	 * @throws UnknownSpeechActException - thrown when the operator references a 
	 * 									   SpeechAct not defined
	 */
	public void checkIntegrity(IntegrityValidator val) throws UnspecifiedVariableException, UnknownSpeechActException {
	    ListIterator<Step> li = _actions.listIterator();
	    
	    while(li.hasNext()) {
	         li.next().CheckIntegrity(val);
	    }
	}
	
	public Step getAction(int id, Name actionName)
	{
		Step s;
		ArrayList<Substitution> subst;
		
		for(ListIterator<Step> li = _actions.listIterator(); li.hasNext();)
		{
			s = (Step) li.next();
			s = (Step)s.clone();
			s.ReplaceUnboundVariables(id);
			
			subst = Unifier.Unify(s.getName(), actionName);
			if(subst != null)
			{
				s = (Step) s.clone();
				s.MakeGround(subst);
				return s;
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the operator that corresponds to the given name
	 * @param name - the name of the step to get
	 * @return the searched step if it is found, null otherwise
	 */
	public Step getAction(String name) {
		
		for(Step s : _actions)
		{
			if(s.getName().toString().equals(name))
			{
				return s;
			}
		}
		
		return null;
	}
	
	public ArrayList<Step> getActions()
	{
		return _actions;
	}
	
	public void LoadActionsFile(String xmlFile, AgentModel am) throws ActionsParsingException
	{
		ActionsLoaderHandler op = LoadOperators(xmlFile, am);
		_actions = op.getOperators();
	}
	
	private ActionsLoaderHandler LoadOperators(String xmlFile, AgentModel am) throws ActionsParsingException {
		AgentLogger.GetInstance().logAndPrint("LOAD: " + xmlFile);
		
		ActionsLoaderHandler op = new ActionsLoaderHandler(am);
		
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(new File(xmlFile), op);
			return op;
		}
		catch (Exception ex) {
			throw new ActionsParsingException("Error parsing the actions file.",ex);
		}	
	}
}
