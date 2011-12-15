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
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;

import FAtiMA.AdvancedMemory.ontology.TreeOntology;
import FAtiMA.AdvancedMemory.ontology.NounOntology;
import FAtiMA.AdvancedMemory.ontology.TimeOntology;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.EpisodicMemory;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;
import FAtiMA.Core.memory.episodicMemory.Time;

public class CompoundCue implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "Compound Cue";

	private static final double FACTOR_SAME = 1.0;
	private static final double FACTOR_SIMILAR = 0.9;
	private static final double FACTOR_DIFFERENT = 0.8;

	// time when mechanism was executed
	private Time time;
	// number of days provided (before filtering)
	private int numDaysProvided;
	// number of working days provided (before filtering)
	private int numWorkingDaysProvided;
	// attributes used for action detail filtering	
	private ArrayList<String> filterAttributes;
	// time ontology used for attribute time (null if not used)	
	private TimeOntology timeOntology;
	// noun ontology used for attribute target (null if not used)
	private NounOntology targetOntology;
	// noun ontology used for attribute object (null if not used)
	private NounOntology objectOntology;
	// location ontology used for attribute location (null if not used)
	private TreeOntology locationOntology;
	// ID of action detail to be compared against	
	private int targetID;
	// IDs of action details and corresponding evaluation values
	private HashMap<Integer, Double> evaluationValues;

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
	}

	public int getNumDaysProvided() {
		return numDaysProvided;
	}

	public void setNumDaysProvided(int numDaysProvided) {
		this.numDaysProvided = numDaysProvided;
	}

	public int getNumWorkingDaysProvided() {
		return numWorkingDaysProvided;
	}

	public void setNumWorkingDaysProvided(int numWorkingDaysProvided) {
		this.numWorkingDaysProvided = numWorkingDaysProvided;
	}

	public ArrayList<String> getFilterAttributes() {
		return filterAttributes;
	}

	public void setFilterAttributes(ArrayList<String> filterAttributes) {
		this.filterAttributes = filterAttributes;
	}

	public TimeOntology getTimeOntology() {
		return timeOntology;
	}

	public void setTimeOntology(TimeOntology timeOntology) {
		this.timeOntology = timeOntology;
	}

	public NounOntology getTargetOntology() {
		return targetOntology;
	}

	public void setTargetOntology(NounOntology targetOntology) {
		this.targetOntology = targetOntology;
	}

	public NounOntology getObjectOntology() {
		return objectOntology;
	}

	public void setObjectOntology(NounOntology objectOntology) {
		this.objectOntology = objectOntology;
	}

	public TreeOntology getLocationOntology() {
		return locationOntology;
	}

	public void setLocationOntology(TreeOntology locationOntology) {
		this.locationOntology = locationOntology;
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

	private double getMultiplicationFactor(Object object1, Object object2) {
		return getMultiplicationFactor(object1, object2, null, null);
	}

	private double getMultiplicationFactor(Object object1, Object object2, NounOntology nounOntology, TreeOntology locationOntology) {

		if (object1 == null) {
			// null is always treated as a difference
			return FACTOR_DIFFERENT;

		} else {
			if (object1.equals(object2)) {
				return FACTOR_SAME;

			} else {

				// noun ontology
				if (nounOntology != null) {

					String[] nouns = new String[2];
					nouns[0] = String.valueOf(object1);
					nouns[1] = String.valueOf(object2);
					LinkedList<String> nounsGeneralised = nounOntology.generaliseNouns(nouns);

					if (nounsGeneralised.size() > 0) {
						return FACTOR_SIMILAR;
					}

				}

				// location ontology
				if (locationOntology != null) {

					String location1 = String.valueOf(object1);
					String location2 = String.valueOf(object2);
					LinkedList<String> locationsGeneralised = locationOntology.getClosestCommonAncestors(location1, location2);

					if (locationsGeneralised.size() > 0) {
						return FACTOR_SIMILAR;
					}

				}

				// no match was found
				return FACTOR_DIFFERENT;

			}
		}
	}

	public ActionDetail execute(EpisodicMemory episodicMemory, ActionDetail actionDetailTarget) {
		return execute(episodicMemory, null, actionDetailTarget, null, null, null, null);
	}

	public ActionDetail execute(ArrayList<ActionDetail> actionDetails, ActionDetail actionDetailTarget) {
		return execute(actionDetails, null, actionDetailTarget, null, null, null, null);
	}

	public ActionDetail execute(EpisodicMemory episodicMemory, String filterAttributesStr, ActionDetail actionDetailTarget, TimeOntology timeOntology, NounOntology targetOntology,
			NounOntology objectOntology, TreeOntology locationOntology) {

		ArrayList<ActionDetail> actionDetails = new ArrayList<ActionDetail>();
		for (MemoryEpisode memoryEpisode : episodicMemory.getAM().GetAllEpisodes()) {
			actionDetails.addAll(memoryEpisode.getDetails());
		}
		for (ActionDetail actionDetail : episodicMemory.getSTEM().getDetails()) {
			actionDetails.add(actionDetail);
		}

		// alternative:
		// create search keys from filter attributes string
		// use memory search to get list of action details (both past and recent)
		// call generalise with these action details and an empty filter attributes string (or null)
		// but: no ontology usage then

		return execute(actionDetails, filterAttributesStr, actionDetailTarget, timeOntology, targetOntology, objectOntology, locationOntology);
	}

	public ActionDetail execute(ArrayList<ActionDetail> actionDetails, String filterAttributesStr, ActionDetail actionDetailTarget, TimeOntology timeOntology, NounOntology targetOntology,
			NounOntology objectOntology, TreeOntology locationOntology) {

		// count number of days and number of working days provided
		HashSet<Calendar> daysProvided = new HashSet<Calendar>();
		HashSet<Calendar> workingDaysProvided = new HashSet<Calendar>();
		for (ActionDetail actionDetail : actionDetails) {
			// set date and time
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(actionDetail.getTime().getRealTime());
			// reset time
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			// add to sets
			daysProvided.add(calendar);
			if (TimeOntology.isWorkingDay(calendar.getTimeInMillis())) {
				workingDaysProvided.add(calendar);
			}
		}
		int numDaysProvided = daysProvided.size();
		int numWorkingDaysProvided = workingDaysProvided.size();

		// initialise
		ActionDetail actionDetailMax = null;
		double evaluationValueMax = 0;
		Time time = new Time();

		// extract filter attributes
		ArrayList<String> filterAttributes = new ArrayList<String>();
		if (filterAttributesStr != null) {
			StringTokenizer stringTokenizer = new StringTokenizer(filterAttributesStr, "*");
			while (stringTokenizer.hasMoreTokens()) {
				String filterAttribute = stringTokenizer.nextToken();
				filterAttributes.add(filterAttribute);
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
			actionDetailsFiltered = ActionDetailFilter.filterActionDetails(actionDetailsFiltered, name, value, timeOntology, targetOntology, objectOntology, locationOntology);
		}

		// calculate evaluation values

		HashMap<Integer, Double> evaluationValues = new HashMap<Integer, Double>();

		for (ActionDetail actionDetail : actionDetailsFiltered) {

			double evaluationValue = 1.0;

			// comparison of attribute values
			evaluationValue += evaluationValue * getMultiplicationFactor(actionDetailTarget.getSubject(), actionDetail.getSubject());
			evaluationValue += evaluationValue * getMultiplicationFactor(actionDetailTarget.getAction(), actionDetail.getAction());
			evaluationValue += evaluationValue * getMultiplicationFactor(actionDetailTarget.getTarget(), actionDetail.getTarget(), targetOntology, null);
			evaluationValue += evaluationValue * getMultiplicationFactor(actionDetailTarget.getObject(), actionDetail.getObject(), objectOntology, null);
			evaluationValue += evaluationValue * getMultiplicationFactor(actionDetailTarget.getLocation(), actionDetail.getLocation(), null, locationOntology);
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
		this.numDaysProvided = numDaysProvided;
		this.numWorkingDaysProvided = numWorkingDaysProvided;
		this.filterAttributes = filterAttributes;
		this.timeOntology = timeOntology;
		this.targetOntology = targetOntology;
		this.objectOntology = objectOntology;
		this.locationOntology = locationOntology;
		this.targetID = actionDetailTarget.getID();
		this.evaluationValues = evaluationValues;

		return actionDetailMax;
	}
}
