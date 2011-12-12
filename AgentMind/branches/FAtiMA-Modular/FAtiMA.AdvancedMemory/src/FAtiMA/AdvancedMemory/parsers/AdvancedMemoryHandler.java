/** 
 * AdvancedMemoryHandler.java - Handler for loading advanced memory results
 * from an XML file.
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
 * Created: 21/11/11
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History: 
 * Matthias Keysermann: 21/11/11 - File created
 * 
 * **/

package FAtiMA.AdvancedMemory.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.xml.sax.Attributes;

import FAtiMA.AdvancedMemory.AdvancedMemoryComponent;
import FAtiMA.AdvancedMemory.AttributeItem;
import FAtiMA.AdvancedMemory.AttributeItemSet;
import FAtiMA.AdvancedMemory.CompoundCue;
import FAtiMA.AdvancedMemory.GER;
import FAtiMA.AdvancedMemory.Generalisation;
import FAtiMA.AdvancedMemory.SpreadingActivation;
import FAtiMA.AdvancedMemory.ontology.TreeOntology;
import FAtiMA.AdvancedMemory.ontology.NounOntology;
import FAtiMA.AdvancedMemory.ontology.TimeOntology;
import FAtiMA.Core.memory.episodicMemory.Time;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;

public class AdvancedMemoryHandler extends ReflectXMLHandler {

	// General
	private AdvancedMemoryComponent advancedMemoryComponent;
	private ArrayList<Object> results;
	private Object result;
	// Common
	private ArrayList<String> filterAttributes;
	// Compound Cue
	private CompoundCue compoundCue;
	private HashMap<Integer, Double> evaluationValues;
	// Spreading Activation
	private SpreadingActivation spreadingActivation;
	private HashMap<String, Integer> frequencies;
	private HashMap<String, HashSet<String>> targetAttributeHypernyms;
	private HashSet<String> targetAttributeHypernymSet;
	// Generalisation
	private Generalisation generalisation;
	private ArrayList<String> attributeNames;
	private ArrayList<GER> gers;
	private GER ger;
	private AttributeItemSet attributeItemSet;
	private AttributeItem attributeItem;
	private HashSet<String> hypernymSet;

	public AdvancedMemoryHandler(AdvancedMemoryComponent advancedMemoryComponent) {
		this.advancedMemoryComponent = advancedMemoryComponent;
	}

	// General

	public void AdvancedMemory(Attributes attributes) {
		results = advancedMemoryComponent.getResults();
		results.clear();
	}

	// Common

	public void Time(Attributes attributes) {
		long narrativeTime = Long.parseLong(attributes.getValue("narrativeTime"));
		long realTime = Long.parseLong(attributes.getValue("realTime"));
		long eventSequence = Long.parseLong(attributes.getValue("eventSequence"));
		Time time = new Time(narrativeTime, realTime, eventSequence);
		if (result instanceof CompoundCue) {
			compoundCue.setTime(time);
		} else if (result instanceof SpreadingActivation) {
			spreadingActivation.setTime(time);
		} else if (result instanceof Generalisation) {
			generalisation.setTime(time);
		}
	}

	public void FilterAttributes(Attributes attributes) {
		filterAttributes = new ArrayList<String>();
		if (result instanceof CompoundCue) {
			compoundCue.setFilterAttributes(filterAttributes);
		} else if (result instanceof SpreadingActivation) {
			spreadingActivation.setFilterAttributes(filterAttributes);
		} else if (result instanceof Generalisation) {
			generalisation.setFilterAttributes(filterAttributes);
		}
	}

	public void FilterAttribute(Attributes attributes) {
		String name = attributes.getValue("name");
		String value = attributes.getValue("value");
		filterAttributes.add(name + " " + value);
	}

	public void TimeOntology(Attributes attributes) {
		TimeOntology timeOntology = new TimeOntology();
		short abstractionMode = Short.parseShort(attributes.getValue("abstractionMode"));
		timeOntology.setAbstractionMode(abstractionMode);
		if (result instanceof CompoundCue) {
			compoundCue.setTimeOntology(timeOntology);
		} else if (result instanceof SpreadingActivation) {
			spreadingActivation.setTimeOntology(timeOntology);
		} else if (result instanceof Generalisation) {
			generalisation.setTimeOntology(timeOntology);
		}
	}

	public void TargetOntology(Attributes attributes) {
		NounOntology targetOntology = new NounOntology();
		int depthMax = Integer.parseInt(attributes.getValue("depthMax"));
		targetOntology.setDepthMax(depthMax);
		if (result instanceof CompoundCue) {
			compoundCue.setTargetOntology(targetOntology);
		} else if (result instanceof SpreadingActivation) {
			spreadingActivation.setTargetOntology(targetOntology);
		} else if (result instanceof Generalisation) {
			generalisation.setTargetOntology(targetOntology);
		}
	}

	public void ObjectOntology(Attributes attributes) {
		NounOntology objectOntology = new NounOntology();
		int depthMax = Integer.parseInt(attributes.getValue("depthMax"));
		objectOntology.setDepthMax(depthMax);
		if (result instanceof CompoundCue) {
			compoundCue.setObjectOntology(objectOntology);
		} else if (result instanceof SpreadingActivation) {
			spreadingActivation.setObjectOntology(objectOntology);
		} else if (result instanceof Generalisation) {
			generalisation.setObjectOntology(objectOntology);
		}
	}

	public void LocationOntology(Attributes attributes) {
		TreeOntology locationOntology = new TreeOntology();
		int depthMax = Integer.parseInt(attributes.getValue("depthMax"));
		locationOntology.setDepthMax(depthMax);
		if (result instanceof CompoundCue) {
			compoundCue.setLocationOntology(locationOntology);
		} else if (result instanceof SpreadingActivation) {
			spreadingActivation.setLocationOntology(locationOntology);
		} else if (result instanceof Generalisation) {
			generalisation.setLocationOntology(locationOntology);
		}
	}

	// Compound Cue

	public void CompoundCue(Attributes attributes) {
		compoundCue = new CompoundCue();
		int targetID = Integer.parseInt(attributes.getValue("targetID"));
		compoundCue.setTargetID(targetID);
		result = compoundCue;
		results.add(compoundCue);
	}

	public void EvaluationValues(Attributes attributes) {
		evaluationValues = new HashMap<Integer, Double>();
		compoundCue.setEvaluationValues(evaluationValues);
	}

	public void EvaluationValue(Attributes attributes) {
		int id = Integer.parseInt(attributes.getValue("id"));
		double value = Double.parseDouble(attributes.getValue("value"));
		evaluationValues.put(id, value);
	}

	// Spreading Activation

	public void SpreadingActivation(Attributes attributes) {
		spreadingActivation = new SpreadingActivation();
		String targetAttributeName = attributes.getValue("targetAttributeName");
		spreadingActivation.setTargetAttributeName(targetAttributeName);
		result = spreadingActivation;
		results.add(spreadingActivation);
	}

	public void Frequencies(Attributes attributes) {
		frequencies = new HashMap<String, Integer>();
		spreadingActivation.setFrequencies(frequencies);
	}

	public void Frequency(Attributes attributes) {
		String value = attributes.getValue("value");
		Integer frequency = Integer.parseInt(attributes.getValue("frequency"));
		frequencies.put(value, frequency);
	}

	public void TargetAttributeHypernyms(Attributes attributes) {
		targetAttributeHypernyms = new HashMap<String, HashSet<String>>();
		spreadingActivation.setTargetAttributeHypernyms(targetAttributeHypernyms);
	}

	public void TargetAttributeHypernymSet(Attributes attributes) {
		String value = attributes.getValue("value");
		targetAttributeHypernymSet = new HashSet<String>();
		targetAttributeHypernyms.put(value, targetAttributeHypernymSet);
	}

	public void TargetAttributeHypernym(Attributes attributes) {
		String value = attributes.getValue("value");
		targetAttributeHypernymSet.add(value);
	}

	// Generalisation

	public void Generalisation(Attributes attributes) {
		generalisation = new Generalisation();
		int minimumCoverage = Integer.parseInt(attributes.getValue("minimumCoverage"));
		generalisation.setMinimumCoverage(minimumCoverage);
		result = generalisation;
		results.add(generalisation);
	}

	public void AttributeNames(Attributes attributes) {
		attributeNames = new ArrayList<String>();
		generalisation.setAttributeNames(attributeNames);
	}

	public void AttributeName(Attributes attributes) {
		String name = attributes.getValue("name");
		attributeNames.add(name);
	}

	public void GERs(Attributes attributes) {
		gers = new ArrayList<GER>();
		generalisation.setGers(gers);
	}

	public void GER(Attributes attributes) {
		ger = new GER();
		int coverage = Integer.parseInt(attributes.getValue("coverage"));
		ger.setCoverage(coverage);
		gers.add(ger);
	}

	public void AttributeItemSet(Attributes attributes) {
		attributeItemSet = new AttributeItemSet();
		ger.setAttributeItemSet(attributeItemSet);
	}

	public void AttributeItem(Attributes attributes) {
		attributeItem = new AttributeItem();
		String name = attributes.getValue("name");
		String value = attributes.getValue("value");
		attributeItem.setName(name);
		attributeItem.setValue(value);
		attributeItemSet.addToSet(attributeItem);
	}

	public void HypernymSet(Attributes attributes) {
		hypernymSet = new HashSet<String>();
		attributeItem.setHypernymSet(hypernymSet);
	}

	public void Hypernym(Attributes attributes) {
		String value = attributes.getValue("value");
		hypernymSet.add(value);
	}

}
