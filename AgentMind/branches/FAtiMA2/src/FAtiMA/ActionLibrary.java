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
package FAtiMA;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.deliberativeLayer.plan.Step;
import FAtiMA.exceptions.ActionsParsingException;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.parsers.StripsOperatorsLoaderHandler;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Unifier;

/**
 * @author João Dias
 * Class that stores the STRIPS definition of the domain actions
 * You cannot create an ActionLibrary since there is one and only instance 
 * for the agent. If you want to access it use AgentLibrary.GetInstance() method.
 */
public class ActionLibrary implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Singleton pattern 
	 */
	private static ActionLibrary _actionLibraryInstance = null;
	
	/**
	 * Gets a the library with all actions specified in a STRIPS-like fashion
	 * 
	 * @return an ActionLibrary
	 */
	public static ActionLibrary GetInstance()
	{
		if(_actionLibraryInstance == null)
		{
			_actionLibraryInstance = new ActionLibrary();
		}
		return _actionLibraryInstance;
	}
	
	/**
	 * Saves the state of the current ActionLibrary to a file,
	 * so that it can be later restored from file
	 * @param fileName - the name of the file where we must write
	 * 		             the state of the timer
	 */
	public static void SaveState(String fileName)
	{
		try 
		{
			FileOutputStream out = new FileOutputStream(fileName);
	    	ObjectOutputStream s = new ObjectOutputStream(out);
	    	
	    	s.writeObject(_actionLibraryInstance);
        	s.flush();
        	s.close();
        	out.close();
		}
		catch(Exception e)
		{
			AgentLogger.GetInstance().logAndPrint("Exception: " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads a specific state of the ActionLibrary from a previously
	 * saved file
	 * @param fileName - the name of the file that contains the stored
	 * 					 timer
	 */
	public static void LoadState(String fileName)
	{
		try
		{
			FileInputStream in = new FileInputStream(fileName);
        	ObjectInputStream s = new ObjectInputStream(in);
        	_actionLibraryInstance = (ActionLibrary) s.readObject();
        	
        	s.close();
        	in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private ArrayList _actions;
	
	/**
	 * Creates a new timer for the Agent's simulation
	 *
	 */
	private ActionLibrary()
	{
		_actions = new ArrayList();
	}
	
	public void LoadActionsFile(String xmlFile, AgentModel am) throws ActionsParsingException
	{
		StripsOperatorsLoaderHandler op = LoadOperators(xmlFile, am);
		_actions = op.getOperators();
	}
	
	private StripsOperatorsLoaderHandler LoadOperators(String xmlFile, AgentModel am) throws ActionsParsingException {
		AgentLogger.GetInstance().logAndPrint("LOAD: " + xmlFile);
		
		StripsOperatorsLoaderHandler op = new StripsOperatorsLoaderHandler(am);
		
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
	
	public ArrayList GetActions()
	{
		return _actions;
	}
	
	public Step GetAction(int id, Name actionName)
	{
		Step s;
		ArrayList subst;
		
		for(ListIterator li = _actions.listIterator(); li.hasNext();)
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
}
