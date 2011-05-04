/** 
 * ConditionsPanel.java - Graphical Swing Panel used to show conditions 
 * (Preconditions in goals, effects, step's preconditions, etc) 
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
 * Created: 17/01/2006 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 17/01/2006 - File created
 */

package FAtiMA.Core.Display;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.util.ConfigurationManager;


public class ConditionsPanel extends JPanel {
	private static Icon VERIFIED_ICON = new ImageIcon(ConfigurationManager.getMindPath() + "/icons/verifiedIcon.png");
	private static Icon NOT_VERIFIED_ICON = new ImageIcon(ConfigurationManager.getMindPath() + "/icons/notVerifiedIcon.png");

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConditionsPanel(String title, ArrayList<Condition> conditions) {

		super(new GridBagLayout());
		this.setBorder(BorderFactory.createTitledBorder(title));
		JLabel lbl;

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.WEST;	

		
		for(Condition c : conditions){
			lbl = new JLabel(c.toString());
			lbl.setHorizontalAlignment(SwingConstants.LEFT);

			if(c.isVerifiable()){
				lbl.setIcon(VERIFIED_ICON);
			}else{
				lbl.setIcon(NOT_VERIFIED_ICON);	
			}
			this.add(lbl,constraints);
			constraints.gridy++;
		}

		if(conditions.size()==0){
			this.add(new JLabel("-"),constraints);
		}

	}
}
