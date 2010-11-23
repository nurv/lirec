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
import java.util.ListIterator;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IComponent;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.AppraisalStructure;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.EpisodicMemory;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.Core.wellFormedNames.Name;


public class AdvancedMemoryComponent implements Serializable, IComponent {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "AdvancedMemory";
	
	private static final String SA_MEMORY = "SA-MEMORY";
	private static final String CC_MEMORY = "CC-MEMORY";
	private static final String G_MEMORY = "G-MEMORY";
	
	
	
	private ArrayList<GER> _gers;
	private Generalisation _generalisation;
	private CompoundCue _compoundCue;
	private SpreadActivate _spreadActivate;
	
	public AdvancedMemoryComponent()
	{
		this._gers = new ArrayList<GER>();
		this._generalisation = new Generalisation();
		this._compoundCue = new CompoundCue();
		this._spreadActivate = new SpreadActivate();
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
		ArrayList<AttributeItemSet> itemSet = this._generalisation.generalise(episodicMemory);
		this.AddGER(itemSet);		
	}
	
	private void loadGeneralMemoryConditions(AgentModel ag){

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
		loadGeneralMemoryConditions(am);
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void decay(long time) {
	}

	@Override
	public void update(AgentModel am) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Event e, AgentModel am) {
		// TODO Auto-generated method stub
	}

	@Override
	public IComponent createModelOfOther() {
		return null;
	}

	@Override
	public void appraisal(Event e, AppraisalStructure as, AgentModel am) {
		
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
	public void emotionActivation(Event e, ActiveEmotion em, AgentModel am) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void coping(AgentModel am) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propertyChangedPerception(String ToM, Name propertyName,
			String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void lookAtPerception(AgentCore ag, String subject, String target) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entityRemovedPerception(String entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AgentDisplayPanel createComponentDisplayPanel(AgentModel am) {
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
			//TODO ver isto depois com a refactorização
			/*st = new StringTokenizer(perception, "$");
			String question = st.nextToken();
			String known = "";
			while(st.hasMoreTokens())
			{
				known = known + st.nextToken();
			}					
			System.out.println("question " + question);
			ArrayList<String> knownInfo = ExtractKnownInfo(known);
			_agent.getSpreadActivate().Spread(question, knownInfo, _agent.getMemory().getEpisodicMemory());
			
			Hashtable<String, Integer> saResult = _agent.getSpreadActivate().getSAResults();
			
			for(String result : saResult.keySet())
			{
				System.out.println(question + " " + result + " frequency " + saResult.get(result));
			}
			
			/*_agent.getCommonalities().eventCommonalities(_agent.getSpreadActivate().getDetails());
			Hashtable<ArrayList<Integer>, Hashtable<String, String>> gResult = _agent.getCommonalities().getMatch();
		
			for(ArrayList<Integer> result : gResult.keySet())
			{
				System.out.println("id " + result);
				Hashtable<String, String> match = gResult.get(result);
				
				for(String matchingValues : match.keySet())
				{
					System.out.println("match in Remote Agent " + matchingValues);
				}
			}*/
			
			System.out.println("\n\n");
		}
		else if(msgType.equals(CC_MEMORY))
		{
			/*int index = Math.min(8, (int) (Math.random()*10));
			ActionDetail event = _agent.getMemory().getEpisodicMemory().getDetails().get(index);
			_agent.getCompoundCue().Match(event, _agent.getMemory().getEpisodicMemory());
			System.out.println("\nEvent ID to match on " + event.getID());
			
			Hashtable<Integer, Float>  results = _agent.getCompoundCue().getCCResults();
			Iterator it = results.keySet().iterator();
			while (it.hasNext())
			{
				int id = (Integer) it.next();
				System.out.println("ID " + id + " evaluation " + results.get(id));
			}
			System.out.println("\n\n");*/
		}
		else if(msgType.equals(G_MEMORY))
		{
			//generalise();
		}
	}
	
	
}
