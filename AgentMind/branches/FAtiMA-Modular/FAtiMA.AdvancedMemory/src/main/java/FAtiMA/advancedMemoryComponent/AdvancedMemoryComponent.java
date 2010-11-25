/** 
 * GeneralMemory.java - The agent's general memory, that is memory that stores 
 * abstraction of events / memory schemata of which events can be reconstructed.
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
 * Company: HWU
 * Project: LIREC
 * Created: 23/09/10
 * @author: Meiyii Lim
 * Email to: M.Lim@hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 23/09/10 - File created
 * 
 * **/

package FAtiMA.advancedMemoryComponent;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.AgentSimulationTime;
import FAtiMA.Core.IComponent;
import FAtiMA.Core.IProcessExternalRequestComponent;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.emotionalState.AppraisalStructure;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.EpisodicMemory;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.ConfigurationManager;



public class AdvancedMemoryComponent implements Serializable, IComponent, IProcessExternalRequestComponent {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "AdvancedMemory";
	
	private static final String SA_MEMORY = "SA-MEMORY";
	private static final String CC_MEMORY = "CC-MEMORY";
	private static final String G_MEMORY = "G-MEMORY";
	
	
	
	private ArrayList<GER> _gers;
	private Generalisation _generalisation;
	private CompoundCue _compoundCue;
	private SpreadActivate _spreadActivate;
	private Commonalities _commonalities;
	private EpisodicMemory _episodicMemory;
	private long _lastTime;
	
	public AdvancedMemoryComponent()
	{
		this._gers = new ArrayList<GER>();
		this._generalisation = new Generalisation();
		this._compoundCue = new CompoundCue();
		this._spreadActivate = new SpreadActivate();
		this._commonalities = new Commonalities();
		this._lastTime = AgentSimulationTime.GetInstance().Time();
	}
	
	public CompoundCue getCompoundCue()
	{
		return _compoundCue;
	}
	
	public SpreadActivate getSpreadActivate()
	{
		return _spreadActivate;
	}
	
	/*
	 * Performs generalisation and update the GeneralMemory with frequent item sets
	 */
	public void generalise(EpisodicMemory episodicMemory)
	{
		ArrayList<AttributeItemSet> itemSet = this._generalisation.generalise(_episodicMemory);
		this.AddGER(itemSet);		
	}
	
	private void loadMemoryProcessesConditions(AgentModel ag){

		AgentLogger.GetInstance().log("LOADING GeneralMemory: ");
		
		ActionsLoaderHandler generalMemoryLoader = new ActionsLoaderHandler(ag);
		
		try{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(new File(ConfigurationManager.getActionsFile()),generalMemoryLoader);
			

		}catch(Exception e){
			throw new RuntimeException("GeneralMemory Error on Loading the Actions XML File:" + e);
		}
	}
	
	public void AddGER(ArrayList<AttributeItemSet> itemSet)
	{
		for (int i = 0; i < itemSet.size(); i++)
		{
			AttributeItemSet attrItemSet = itemSet.get(i);
			GER ger = new GER();
			
			// set the coverage (frequency of occurrence) of the item set
			ger.setCoverage(attrItemSet.getCoverage());			
			for (int j = 0; j < attrItemSet.getCandidateItemSet().size(); j++)
			{
				AttributeItem attrItem = attrItemSet.getCandidateItemSet().get(j);				
				
				if (attrItem.getAttrName() == "subject")
					ger.setSubject(attrItem.getAttrValue());
				else if (attrItem.getAttrName() == "action")
					ger.setAction(attrItem.getAttrValue());
				else if (attrItem.getAttrName() == "target")
					ger.setTarget(attrItem.getAttrValue());
				else if (attrItem.getAttrName() == "desirability")
					ger.setDesirability(attrItem.getAttrValue());
				else if (attrItem.getAttrName() == "praiseworthiness")
					ger.setPraiseworthiness(attrItem.getAttrValue());
				else if (attrItem.getAttrName() == "time")
					ger.setTime(attrItem.getAttrValue());
				
			}		
			this._gers.add(ger);
		}
	}
	
	/**
	 * Extract known information
	 * @param 
	 * @return
	 * added by Meiyii 19/11/09
	 */
	private ArrayList<String> ExtractKnownInfo(String known)
	{
		ArrayList<String> knownInfo = new ArrayList<String>();
			
		StringTokenizer st = new StringTokenizer(known, "*");
		while(st.hasMoreTokens())
		{
			String knownStr = st.nextToken();
			knownInfo.add(knownStr);
			System.out.println("Known String " + knownStr);
		}
		return knownInfo;
	}
	
	public ArrayList<GER> getAllGERs()
	{
		return this._gers;
	}
	
	/*
	 * TODO
	 */
	public String toXML()
	{
		String gm  = "<GeneralMemory>";
		for(ListIterator<GER> li = this._gers.listIterator();li.hasNext();)
		{
			
		}
		gm += "</GeneralMemory>";
		return gm; 
	}

	@Override
	public String name() {
		return AdvancedMemoryComponent.NAME;
	}

	@Override
	public void initialize(AgentModel am) {
		_episodicMemory = am.getMemory().getEpisodicMemory();
		loadMemoryProcessesConditions(am);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateCycle(AgentModel am, long time) {
		if (time >= _lastTime + 86400000) {
			_lastTime = time;
			
			generalise(am.getMemory().getEpisodicMemory());
		}
	}

	@Override
	public void perceiveEvent(AgentModel am, Event e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void appraisal(AgentModel am, Event e, AppraisalStructure as) {
		
		//appraisal from memory
		ActionDetail ad = new ActionDetail(0,e.GetSubject(),
					e.GetAction(), 
					e.GetTarget(),
					e.GetParameters(),null,null,null,null);
			
		_compoundCue.Match(ad,am.getMemory().getEpisodicMemory());
			
		ActionDetail result = _compoundCue.getStrongestResult();
		//float eval = _compoundCue.getEvaluation();
		if(result != null)
		{
			float desirability = result.getDesirability();
			if(desirability != 0)
			{
				as.SetAppraisalVariable(AdvancedMemoryComponent.NAME, (short)3, AppraisalStructure.DESIRABILITY, desirability);
			}	
		}
	}

	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return new GeneralMemoryPanel(this);
	}

	@Override
	public void processExternalRequest(String requestMsg) {
		
		StringTokenizer st = new StringTokenizer(requestMsg," ");
		String msgType = st.nextToken();
		
		String perception = "";
		
		while(st.hasMoreTokens())
		{
			perception = perception + st.nextToken() + " ";
		}
		
		perception = perception.trim();
		
	    if(msgType.equals(SA_MEMORY))
		{
			
			st = new StringTokenizer(perception, "$");
			String question = st.nextToken();
			String known = "";
			while(st.hasMoreTokens())
			{
				known = known + st.nextToken();
			}					
			System.out.println("question " + question);
			ArrayList<String> knownInfo = ExtractKnownInfo(known);
			_spreadActivate.Spread(question, knownInfo, _episodicMemory);
			
			
			Hashtable<String, Integer> saResult = _spreadActivate.getSAResults();
			
			for(String result : saResult.keySet())
			{
				System.out.println(question + " " + result + " frequency " + saResult.get(result));
			}
			
			_commonalities.eventCommonalities(_spreadActivate.getDetails());
			Hashtable<ArrayList<Integer>,Hashtable<String, String>> gResult = _commonalities.getMatch();
		
			for(ArrayList<Integer> result : gResult.keySet())
			{
				System.out.println("id " + result);
				Hashtable<String, String> match = gResult.get(result);
				
				for(String matchingValues : match.keySet())
				{
					System.out.println("match in Remote Agent " + matchingValues);
				}
			}
			
			System.out.println("\n\n");
		}
		else if(msgType.equals(CC_MEMORY))
		{
			/*int index = Math.min(8, (int) (Math.random()*10));
			ActionDetail event = _episodicMemory.getDetails().get(index);
			_compoundCue.Match(event, _episodicMemory);
			System.out.println("\nEvent ID to match on " + event.getID());
			
			Hashtable<Integer, Float>  results = _compoundCue.getCCResults();
			Iterator<E> it = results.keySet().iterator();
			while (it.hasNext())
			{
				int id = (Integer) it.next();
				System.out.println("ID " + id + " evaluation " + results.get(id));
			}
			System.out.println("\n\n");*/
		}
		else if(msgType.equals(G_MEMORY))
		{
			generalise(_episodicMemory);
		}
	}
	
	
}
