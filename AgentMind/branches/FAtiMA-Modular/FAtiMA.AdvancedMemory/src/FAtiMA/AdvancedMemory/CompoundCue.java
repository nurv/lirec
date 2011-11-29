/** 
 * CompoundCue.java - A class to perform the compound cue mechanism through matching and returning 
 * of the most relevant events for the current situation, i.e. calculating a similarity score.
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
 * Company: HWU
 * Project: LIREC
 * Created: 18/06/11
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History:
 * Matthias Keysermann: 18/06/11 - File created
 * 
 * **/

package FAtiMA.AdvancedMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.EpisodicMemory;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;
import FAtiMA.Core.memory.episodicMemory.Time;

public class CompoundCue implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "Compound Cue";

	private static final double FACTOR_SAME = 1.0;
	private static final double FACTOR_DIFFERENT = 0.8;

	private Time time;
	private ArrayList<String> filterAttributes;
	private int targetID;
	private HashMap<Integer, Double> evaluationValues;

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
	}

	public ArrayList<String> getFilterAttributes() {
		return filterAttributes;
	}

	public void setFilterAttributes(ArrayList<String> filterAttributes) {
		this.filterAttributes = filterAttributes;
	}

	public int getTargetID() {
		return targetID;
	}

	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}

	public HashMap<Integer, Double> getEvaluationValues() {
		return evaluationValues;
	}

	public void setEvaluationValues(HashMap<Integer, Double> evaluationValues) {
		this.evaluationValues = evaluationValues;
	}

	private ArrayList<ActionDetail> filterActionDetails(ArrayList<ActionDetail> actionDetails, String attributeName, Object attributeValue) {

		ArrayList<ActionDetail> actionDetailsFiltered = new ArrayList<ActionDetail>();

		for (ActionDetail actionDetail : actionDetails) {
			Object attributeValueCurrent = actionDetail.getValueByName(attributeName);

			if (attributeValueCurrent != null && attributeValueCurrent.equals(attributeValue)) {
				actionDetailsFiltered.add(actionDetail);
			}
		}

		return actionDetailsFiltered;
	}

	private double getMultiplicationFactor(Object object1, Object object2) {
		if (object1 == null) {
			// null is always treated as a difference
			return FACTOR_DIFFERENT;
			/*
			if (object2 == null) {
				return FACTOR_SAME;
			} else {
				return FACTOR_DIFFERENT;
			}
			*/
		} else {
			if (object1.equals(object2)) {
				return FACTOR_SAME;
			} else {
				return FACTOR_DIFFERENT;
			}
		}
	}

	public ActionDetail execute(EpisodicMemory episodicMemory, ActionDetail actionDetailTarget) {
		return execute(episodicMemory, null, actionDetailTarget);
	}

	public ActionDetail execute(ArrayList<ActionDetail> actionDetails, ActionDetail actionDetailTarget) {
		return execute(actionDetails, null, actionDetailTarget);
	}

	public ActionDetail execute(EpisodicMemory episodicMemory, String filterAttributesStr, ActionDetail actionDetailTarget) {

		ArrayList<ActionDetail> actionDetails = new ArrayList<ActionDetail>();
		for (MemoryEpisode memoryEpisode : episodicMemory.getAM().GetAllEpisodes()) {
			actionDetails.addAll(memoryEpisode.getDetails());
		}
		for (ActionDetail actionDetail : episodicMemory.getSTEM().getDetails()) {
			actionDetails.add(actionDetail);
		}

		return execute(actionDetails, filterAttributesStr, actionDetailTarget);
	}

	public ActionDetail execute(ArrayList<ActionDetail> actionDetails, String filterAttributesStr, ActionDetail actionDetailTarget) {

		// initialise
		ActionDetail actionDetailMax = null;
		double evaluationValueMax = 0;
		Time time = new Time();

		// extract filter attributes
		ArrayList<String> filterAttributes = new ArrayList<String>();
		if (filterAttributesStr != null) {
			StringTokenizer stringTokenizer = new StringTokenizer(filterAttributesStr, "*");
			while (stringTokenizer.hasMoreTokens()) {
				String knownStr = stringTokenizer.nextToken();
				filterAttributes.add(knownStr);
			}
		}

		// filter action details		
		ArrayList<ActionDetail> actionDetailsFiltered = new ArrayList<ActionDetail>();
		actionDetailsFiltered.addAll(actionDetails);
		for (String filterAttribute : filterAttributes) {
			String[] attributeSplitted = filterAttribute.split(" ");
			String name = attributeSplitted[0];
			String value = "";
			if (attributeSplitted.length == 2) {
				value = attributeSplitted[1];
			}
			actionDetailsFiltered = filterActionDetails(actionDetailsFiltered, name, value);
		}
		// calculate evaluation values

		HashMap<Integer, Double> evaluationValues = new HashMap<Integer, Double>();

		for (ActionDetail actionDetail : actionDetailsFiltered) {

			double evaluationValue = 1.0;

			// comparison of attribute values
			evaluationValue += evaluationValue * getMultiplicationFactor(actionDetailTarget.getSubject(), actionDetail.getSubject());
			evaluationValue += evaluationValue * getMultiplicationFactor(actionDetailTarget.getAction(), actionDetail.getAction());
			evaluationValue += evaluationValue * getMultiplicationFactor(actionDetailTarget.getTarget(), actionDetail.getTarget());
			evaluationValue += evaluationValue * getMultiplicationFactor(actionDetailTarget.getObject(), actionDetail.getObject());
			evaluationValue += evaluationValue * getMultiplicationFactor(actionDetailTarget.getLocation(), actionDetail.getLocation());
			evaluationValue += evaluationValue * getMultiplicationFactor(actionDetailTarget.getIntention(), actionDetail.getIntention());
			evaluationValue += evaluationValue * getMultiplicationFactor(actionDetailTarget.getStatus(), actionDetail.getStatus());
			evaluationValue += evaluationValue * getMultiplicationFactor(actionDetailTarget.getSpeechActMeaning(), actionDetail.getSpeechActMeaning());
			// no matching for: emotion, multimedia path, praiseworthiness, desirability, time 

			evaluationValues.put(actionDetail.getID(), evaluationValue);

			// update maximum
			if (evaluationValue > evaluationValueMax) {
				evaluationValueMax = evaluationValue;
				actionDetailMax = actionDetail;
			}

		}

		// update attributes
		this.time = time;
		this.filterAttributes = filterAttributes;
		this.targetID = actionDetailTarget.getID();
		this.evaluationValues = evaluationValues;

		return actionDetailMax;
	}

}
