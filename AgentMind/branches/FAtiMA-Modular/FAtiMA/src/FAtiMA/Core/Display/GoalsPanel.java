/** 
 * GoalsPanel.java - Graphical Swing Panel that shows all of the agent's goals
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
 * Created: 02/11/2005 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 02/11/2005 - File created
 */

package FAtiMA.Core.Display;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.deliberativeLayer.DeliberativeProcess;
import FAtiMA.Core.deliberativeLayer.Intention;
import FAtiMA.Core.deliberativeLayer.goals.Goal;
import FAtiMA.Core.wellFormedNames.Name;


public class GoalsPanel extends AgentDisplayPanel {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private HashMap<Name,IntentionDisplay> _intentionDisplays;
    private ArrayList<GoalDisplay> _goalDisplays;
    private JPanel _goals;
    private JPanel _intentions;
    
    public GoalsPanel() {
        
        super();
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
        _intentionDisplays = new HashMap<Name, IntentionDisplay>();
        _goalDisplays = new ArrayList<GoalDisplay>();
		
		_goals = new JPanel();
		_goals.setBorder(BorderFactory.createTitledBorder("Goals"));
		_goals.setLayout(new BoxLayout(_goals,BoxLayout.Y_AXIS));
		
		_goals.setMaximumSize(new Dimension(350,400));
		_goals.setMinimumSize(new Dimension(350,400));
		
		JScrollPane goalsScrool = new JScrollPane(_goals);
		
		this.add(goalsScrool);
		
		_intentions = new JPanel();
		_intentions.setBorder(BorderFactory.createTitledBorder("Active Intentions"));
		_intentions.setLayout(new BoxLayout(_intentions,BoxLayout.Y_AXIS));
		
		JScrollPane intentionsScroll = new JScrollPane(_intentions);
		
		this.add(intentionsScroll);
    }
    
    public boolean Update(AgentModel am)
    {
    	return false;
    }
    
    
    public boolean Update(AgentCore ag) {
    	
    	boolean update = false;
        
        DeliberativeProcess deliberative = ag.getDeliberativeLayer();
        
        if(_goalDisplays.size() != deliberative.GetGoals().size()) {
        	update = true;
        	_goals.removeAll();
        	_goalDisplays.clear();
        	
        	Iterator<Goal> it = deliberative.GetGoals().iterator();
        	GoalDisplay gDisplay;
        	Goal g;
        	while(it.hasNext()) {
        		g = it.next();
        		gDisplay = new GoalDisplay(ag, g);
        		
        		_goals.add(gDisplay.getGoalPanel());
        		_goalDisplays.add(gDisplay);
        	}
        }
        
        if(_intentionDisplays.keySet().equals(deliberative.GetIntentionKeysSet())) {
            //in this case, we just have to update the values for the intensity of emotions
            //since the emotions displayed in the previous update are the same emotions
            //in the current update
             Iterator<Intention> it = deliberative.GetIntentionsIterator();
             IntentionDisplay iDisplay;
             Intention i;
             while(it.hasNext()) {
                 
                 i = it.next();     
                 iDisplay = (IntentionDisplay) _intentionDisplays.get(i.getGoal().getName().toString());
                 iDisplay.Update(ag, i);
             }    
        }
        else {
        	update = true;
            _intentions.removeAll(); //removes all displayed intentions from the panel
            _intentionDisplays.clear();
            
            Iterator<Intention> it = deliberative.GetIntentionsIterator();
            IntentionDisplay iDisplay;
            Intention i;
            while(it.hasNext()) {
                i = it.next();
                iDisplay = new IntentionDisplay(ag, i);
                
                _intentions.add(iDisplay.getIntentionPanel());
                _intentionDisplays.put(i.getGoal().getName(),iDisplay);
          
           }
        }   
        return update;
    }
}
