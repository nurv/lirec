/** 
 * CompoundCueSimple.java - A class to perform the compound cue mechanism through matching and returning 
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

package FAtiMA.advancedMemoryComponent;

import java.util.ArrayList;
import java.util.Hashtable;

import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.EpisodicMemory;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;

public class CompoundCueSimple {

	//private static final double FACTOR_SAME = 1.0;

	private static final double FACTOR_DIFFERENT = 0.8;

	private Hashtable<Integer, Double> evaluationValues;

	public CompoundCueSimple() {
		evaluationValues = new Hashtable<Integer, Double>();
	}

	public void calculateEvaluationValues(ActionDetail actionDetail, EpisodicMemory episodicMemory) {

		ArrayList<ActionDetail> actionDetails = new ArrayList<ActionDetail>();

		for (MemoryEpisode episode : episodicMemory.getAM().GetAllEpisodes()) {
			actionDetails.addAll(episode.getDetails());
		}
		actionDetails.addAll(episodicMemory.getSTEM().getDetails());

		evaluationValues = new Hashtable<Integer, Double>();

		for (int i = 0; i < actionDetails.size(); i++) {

			double evaluationValue = 1.0;

			ActionDetail actionDetailCurrent = actionDetails.get(i);

			// comparison of attribute values

			// subject
			if (actionDetail.getSubject() == null) {
				//if (actionDetailCurrent.getSubject() != null) {
				evaluationValue *= FACTOR_DIFFERENT;
				//}
			} else {
				if (!actionDetail.getSubject().equals(actionDetailCurrent.getSubject())) {
					evaluationValue *= FACTOR_DIFFERENT;
				}
			}

			// target
			if (actionDetail.getTarget() == null) {
				//if (actionDetailCurrent.getTarget() != null) {
				evaluationValue *= FACTOR_DIFFERENT;
				//}
			} else {
				if (!actionDetail.getTarget().equals(actionDetailCurrent.getTarget())) {
					evaluationValue *= FACTOR_DIFFERENT;
				}
			}

			// action
			if (actionDetail.getAction() == null) {
				//if (actionDetailCurrent.getAction() != null) {
				evaluationValue *= FACTOR_DIFFERENT;
				//}
			} else {
				if (!actionDetail.getAction().equals(actionDetailCurrent.getAction())) {
					evaluationValue *= FACTOR_DIFFERENT;
				}
			}

			// location
			if (actionDetail.getLocation() == null) {
				//if (actionDetailCurrent.getLocation() != null) {
				evaluationValue *= FACTOR_DIFFERENT;
				//}
			} else {
				if (!actionDetail.getLocation().equals(actionDetailCurrent.getLocation())) {
					evaluationValue *= FACTOR_DIFFERENT;
				}
			}

			// emotion
			if (actionDetail.getEmotion() == null) {
				//if (actionDetailCurrent.getEmotion() != null) {
				evaluationValue *= FACTOR_DIFFERENT;
				//}
			} else {
				if (!actionDetail.getEmotion().equals(actionDetailCurrent.getEmotion())) {
					evaluationValue *= FACTOR_DIFFERENT;
				}
			}

			// intention
			if (actionDetail.getIntention() == null) {
				//if (actionDetailCurrent.getIntention() != null) {
				evaluationValue *= FACTOR_DIFFERENT;
				//}
			} else {
				if (!actionDetail.getIntention().equals(actionDetailCurrent.getIntention())) {
					evaluationValue *= FACTOR_DIFFERENT;
				}
			}

			// status
			if (actionDetail.getStatus() == null) {
				//if (actionDetailCurrent.getStatus() != null) {
				evaluationValue *= FACTOR_DIFFERENT;
				//}
			} else {
				if (!actionDetail.getStatus().equals(actionDetailCurrent.getStatus())) {
					evaluationValue *= FACTOR_DIFFERENT;
				}
			}

			// speech act meaning
			if (actionDetail.getSpeechActMeaning() == null) {
				//if (actionDetailCurrent.getSpeechActMeaning() != null) {
				evaluationValue *= FACTOR_DIFFERENT;
				//}
			} else {
				if (!actionDetail.getSpeechActMeaning().equals(actionDetailCurrent.getSpeechActMeaning())) {
					evaluationValue *= FACTOR_DIFFERENT;
				}
			}

			// multimedia path
			if (actionDetail.getMultimediaPath() == null) {
				//if (actionDetailCurrent.getMultimediaPath() != null) {
				evaluationValue *= FACTOR_DIFFERENT;
				//}
			} else {
				if (!actionDetail.getMultimediaPath().equals(actionDetailCurrent.getMultimediaPath())) {
					evaluationValue *= FACTOR_DIFFERENT;
				}
			}

			// object
			if (actionDetail.getObject() == null) {
				//if (actionDetailCurrent.getObject() != null) {
				evaluationValue *= FACTOR_DIFFERENT;
				//}
			} else {
				if (!actionDetail.getObject().equals(actionDetailCurrent.getObject())) {
					evaluationValue *= FACTOR_DIFFERENT;
				}
			}

			// no matching for: praiseworthiness, desirability, time

			evaluationValues.put(actionDetailCurrent.getID(), evaluationValue);

			// DEBUG
			System.out.println("ID " + actionDetailCurrent.getID() + " evaluation " + evaluationValue);
		}

	}

	public Double getEvaluationValue(int id) {
		return evaluationValues.get(id);
	}

}
