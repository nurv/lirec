/** 
 * AdvancedMemoryWriter.java - Writer for saving advanced memory results
 * to an XML file. 
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
 * Created: 05/07/2011
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 */

package FAtiMA.AdvancedMemory.writers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;

import org.znerd.xmlenc.LineBreak;
import org.znerd.xmlenc.XMLOutputter;

import FAtiMA.AdvancedMemory.AdvancedMemoryComponent;
import FAtiMA.AdvancedMemory.AttributeItem;
import FAtiMA.AdvancedMemory.CompoundCue;
import FAtiMA.AdvancedMemory.GER;
import FAtiMA.AdvancedMemory.Generalisation;
import FAtiMA.AdvancedMemory.SpreadingActivation;
import FAtiMA.AdvancedMemory.ontology.TreeOntology;
import FAtiMA.AdvancedMemory.ontology.NounOntology;
import FAtiMA.AdvancedMemory.ontology.TimeOntology;

public class AdvancedMemoryWriter {

	private static final String ENCODING = "iso-8859-1";

	public static void write(AdvancedMemoryComponent advancedMemoryComponent, String fileName) {
		try {

			FileWriter fileWriter = new FileWriter(fileName);
			XMLOutputter xmlOutputter = new XMLOutputter(fileWriter, ENCODING);
			xmlOutputter.setLineBreak(LineBreak.DOS);
			xmlOutputter.setIndentation("    ");

			xmlOutputter.startTag("AdvancedMemory");

			for (Object result : advancedMemoryComponent.getResults()) {
				write(result, xmlOutputter);
			}

			xmlOutputter.endTag(); //AdvancedMemory			
			xmlOutputter.endDocument();
			xmlOutputter.getWriter().close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void write(Object result, XMLOutputter xmlOutputter) {
		try {

			if (result instanceof CompoundCue) {
				CompoundCue compoundCue = (CompoundCue) result;

				xmlOutputter.startTag("CompoundCue");
				xmlOutputter.attribute("numDaysProvided", String.valueOf(compoundCue.getNumDaysProvided()));
				xmlOutputter.attribute("numWorkingDaysProvided", String.valueOf(compoundCue.getNumWorkingDaysProvided()));
				xmlOutputter.attribute("targetID", String.valueOf(compoundCue.getTargetID()));

				xmlOutputter.startTag("Time");
				xmlOutputter.attribute("narrativeTime", String.valueOf(compoundCue.getTime().getNarrativeTime()));
				xmlOutputter.attribute("realTime", String.valueOf(compoundCue.getTime().getRealTime()));
				xmlOutputter.attribute("eventSequence", String.valueOf(compoundCue.getTime().getEventSequence()));
				xmlOutputter.endTag(); //Time

				xmlOutputter.startTag("FilterAttributes");
				for (String filterAttribute : compoundCue.getFilterAttributes()) {
					xmlOutputter.startTag("FilterAttribute");
					String[] attributeSplitted = filterAttribute.split(" ");
					String name = attributeSplitted[0];
					String value = "";
					// check if a value was given
					if (attributeSplitted.length == 2) {
						value = attributeSplitted[1];
					}
					xmlOutputter.attribute("name", name);
					xmlOutputter.attribute("value", value);
					xmlOutputter.endTag(); //FilterAttribute			
				}
				xmlOutputter.endTag(); //FilterAttributes

				TimeOntology timeOntology = compoundCue.getTimeOntology();
				if (timeOntology != null) {
					xmlOutputter.startTag("TimeOntology");
					xmlOutputter.attribute("abstractionMode", String.valueOf(timeOntology.getAbstractionMode()));
					xmlOutputter.endTag(); //TimeOntology						
				}

				NounOntology targetOntology = compoundCue.getTargetOntology();
				if (targetOntology != null) {
					xmlOutputter.startTag("TargetOntology");
					xmlOutputter.attribute("depthMax", String.valueOf(targetOntology.getDepthMax()));
					xmlOutputter.endTag(); //TargetOntology
				}

				NounOntology objectOntology = compoundCue.getObjectOntology();
				if (objectOntology != null) {
					xmlOutputter.startTag("ObjectOntology");
					xmlOutputter.attribute("depthMax", String.valueOf(objectOntology.getDepthMax()));
					xmlOutputter.endTag(); //ObjectOntology
				}

				TreeOntology locationOntology = compoundCue.getLocationOntology();
				if (locationOntology != null) {
					xmlOutputter.startTag("LocationOntology");
					xmlOutputter.attribute("depthMax", String.valueOf(locationOntology.getDepthMax()));
					xmlOutputter.attribute("filename", locationOntology.getFilename());
					xmlOutputter.endTag(); //LocationOntology
				}

				xmlOutputter.startTag("EvaluationValues");
				for (Integer id : compoundCue.getEvaluationValues().keySet()) {
					xmlOutputter.startTag("EvaluationValue");
					xmlOutputter.attribute("id", String.valueOf(id));
					Double evaluationValue = compoundCue.getEvaluationValues().get(id);
					xmlOutputter.attribute("value", String.valueOf(evaluationValue));
					xmlOutputter.endTag(); //EvaluationValue
				}
				xmlOutputter.endTag(); //EvaluationValues

				xmlOutputter.endTag(); //CompoundCue

			} else if (result instanceof SpreadingActivation) {
				SpreadingActivation spreadingActivation = (SpreadingActivation) result;

				xmlOutputter.startTag("SpreadingActivation");
				xmlOutputter.attribute("numDaysProvided", String.valueOf(spreadingActivation.getNumDaysProvided()));
				xmlOutputter.attribute("numWorkingDaysProvided", String.valueOf(spreadingActivation.getNumWorkingDaysProvided()));
				xmlOutputter.attribute("targetAttributeName", spreadingActivation.getTargetAttributeName());

				xmlOutputter.startTag("Time");
				xmlOutputter.attribute("narrativeTime", String.valueOf(spreadingActivation.getTime().getNarrativeTime()));
				xmlOutputter.attribute("realTime", String.valueOf(spreadingActivation.getTime().getRealTime()));
				xmlOutputter.attribute("eventSequence", String.valueOf(spreadingActivation.getTime().getEventSequence()));
				xmlOutputter.endTag(); //Time

				xmlOutputter.startTag("FilterAttributes");
				for (String filterAttribute : spreadingActivation.getFilterAttributes()) {
					xmlOutputter.startTag("FilterAttribute");
					String[] attributeSplitted = filterAttribute.split(" ");
					String name = attributeSplitted[0];
					String value = "";
					// check if a value was given
					if (attributeSplitted.length == 2) {
						value = attributeSplitted[1];
					}
					xmlOutputter.attribute("name", name);
					xmlOutputter.attribute("value", value);
					xmlOutputter.endTag(); //FilterAttribute			
				}
				xmlOutputter.endTag(); //FilterAttributes

				TimeOntology timeOntology = spreadingActivation.getTimeOntology();
				if (timeOntology != null) {
					xmlOutputter.startTag("TimeOntology");
					xmlOutputter.attribute("abstractionMode", String.valueOf(timeOntology.getAbstractionMode()));
					xmlOutputter.endTag(); //TimeOntology						
				}

				NounOntology targetOntology = spreadingActivation.getTargetOntology();
				if (targetOntology != null) {
					xmlOutputter.startTag("TargetOntology");
					xmlOutputter.attribute("depthMax", String.valueOf(targetOntology.getDepthMax()));
					xmlOutputter.endTag(); //TargetOntology
				}

				HashMap<String, HashSet<String>> targetAttributeHypernyms = spreadingActivation.getTargetAttributeHypernyms();
				if (targetAttributeHypernyms != null) {
					xmlOutputter.startTag("TargetAttributeHypernyms");
					for (String value : targetAttributeHypernyms.keySet()) {
						xmlOutputter.startTag("TargetAttributeHypernymSet");
						xmlOutputter.attribute("value", value);
						HashSet<String> targetHypernymSet = targetAttributeHypernyms.get(value);
						for (String hypernym : targetHypernymSet) {
							xmlOutputter.startTag("TargetAttributeHypernym");
							xmlOutputter.attribute("value", hypernym);
							xmlOutputter.endTag(); //TargetAttributeHypernym								
						}
						xmlOutputter.endTag(); //TargetAttributeHypernymSet							
					}
					xmlOutputter.endTag(); //TargetAttributeHypernyms
				}

				NounOntology objectOntology = spreadingActivation.getObjectOntology();
				if (objectOntology != null) {
					xmlOutputter.startTag("ObjectOntology");
					xmlOutputter.attribute("depthMax", String.valueOf(objectOntology.getDepthMax()));
					xmlOutputter.endTag(); //ObjectOntology
				}

				TreeOntology locationOntology = spreadingActivation.getLocationOntology();
				if (locationOntology != null) {
					xmlOutputter.startTag("LocationOntology");
					xmlOutputter.attribute("depthMax", String.valueOf(locationOntology.getDepthMax()));
					xmlOutputter.attribute("filename", locationOntology.getFilename());
					xmlOutputter.endTag(); //LocationOntology
				}

				xmlOutputter.startTag("Frequencies");
				for (String value : spreadingActivation.getFrequencies().keySet()) {
					xmlOutputter.startTag("Frequency");
					xmlOutputter.attribute("value", value);
					xmlOutputter.attribute("frequency", String.valueOf(spreadingActivation.getFrequencies().get(value)));
					xmlOutputter.endTag(); //Frequency						
				}
				xmlOutputter.endTag(); //Frequencies

				xmlOutputter.endTag(); //SpreadingActivation

			} else if (result instanceof Generalisation) {
				Generalisation generalisation = (Generalisation) result;

				xmlOutputter.startTag("Generalisation");
				xmlOutputter.attribute("numDaysProvided", String.valueOf(generalisation.getNumDaysProvided()));
				xmlOutputter.attribute("numWorkingDaysProvided", String.valueOf(generalisation.getNumWorkingDaysProvided()));
				xmlOutputter.attribute("minimumCoverage", String.valueOf(generalisation.getMinimumCoverage()));

				xmlOutputter.startTag("Time");
				xmlOutputter.attribute("narrativeTime", String.valueOf(generalisation.getTime().getNarrativeTime()));
				xmlOutputter.attribute("realTime", String.valueOf(generalisation.getTime().getRealTime()));
				xmlOutputter.attribute("eventSequence", String.valueOf(generalisation.getTime().getEventSequence()));
				xmlOutputter.endTag(); //Time

				xmlOutputter.startTag("FilterAttributes");
				for (String filterAttribute : generalisation.getFilterAttributes()) {
					xmlOutputter.startTag("FilterAttribute");
					String[] attributeSplitted = filterAttribute.split(" ");
					String name = attributeSplitted[0];
					String value = "";
					// check if a value was given
					if (attributeSplitted.length == 2) {
						value = attributeSplitted[1];
					}
					xmlOutputter.attribute("name", name);
					xmlOutputter.attribute("value", value);
					xmlOutputter.endTag(); //FilterAttribute
				}
				xmlOutputter.endTag(); //FilterAttributes

				TimeOntology timeOntology = generalisation.getTimeOntology();
				if (timeOntology != null) {
					xmlOutputter.startTag("TimeOntology");
					xmlOutputter.attribute("abstractionMode", String.valueOf(timeOntology.getAbstractionMode()));
					xmlOutputter.endTag(); //TimeOntology						
				}

				NounOntology targetOntology = generalisation.getTargetOntology();
				if (targetOntology != null) {
					xmlOutputter.startTag("TargetOntology");
					xmlOutputter.attribute("depthMax", String.valueOf(targetOntology.getDepthMax()));
					xmlOutputter.endTag(); //TargetOntology
				}

				NounOntology objectOntology = generalisation.getObjectOntology();
				if (objectOntology != null) {
					xmlOutputter.startTag("ObjectOntology");
					xmlOutputter.attribute("depthMax", String.valueOf(objectOntology.getDepthMax()));
					xmlOutputter.endTag(); //ObjectOntology
				}

				TreeOntology locationOntology = generalisation.getLocationOntology();
				if (locationOntology != null) {
					xmlOutputter.startTag("LocationOntology");
					xmlOutputter.attribute("depthMax", String.valueOf(locationOntology.getDepthMax()));
					xmlOutputter.attribute("filename", locationOntology.getFilename());
					xmlOutputter.endTag(); //LocationOntology
				}

				xmlOutputter.startTag("AttributeNames");
				for (String attributeName : generalisation.getAttributeNames()) {
					xmlOutputter.startTag("AttributeName");
					xmlOutputter.attribute("name", attributeName);
					xmlOutputter.endTag(); //AttributeName
				}
				xmlOutputter.endTag(); //AttributeNames

				xmlOutputter.startTag("GERs");
				for (GER ger : generalisation.getGers()) {
					xmlOutputter.startTag("GER");
					xmlOutputter.attribute("coverage", String.valueOf(ger.getCoverage()));
					xmlOutputter.startTag("AttributeItemSet");
					for (AttributeItem attributeItem : ger.getAttributeItemSet().getAttributeItems()) {
						xmlOutputter.startTag("AttributeItem");
						xmlOutputter.attribute("name", attributeItem.getName());
						xmlOutputter.attribute("value", String.valueOf(attributeItem.getValue()));
						HashSet<String> hypernymSet = attributeItem.getHypernymSet();
						if (hypernymSet != null) {
							xmlOutputter.startTag("HypernymSet");
							for (String hypernym : hypernymSet) {
								xmlOutputter.startTag("Hypernym");
								xmlOutputter.attribute("value", hypernym);
								xmlOutputter.endTag(); //Hypernym									
							}
							xmlOutputter.endTag(); //HypernymSet
						}
						xmlOutputter.endTag(); //AttributeItem
					}
					xmlOutputter.endTag(); //AttributeItemSet						
					xmlOutputter.endTag(); //GER
				}
				xmlOutputter.endTag(); //GERs

				xmlOutputter.endTag(); //Generalisation

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getUnformattedXML(Object result) {
		StringWriter stringWriter = new StringWriter();

		try {

			XMLOutputter xmlOutputter = new XMLOutputter(stringWriter, ENCODING);
			write(result, xmlOutputter);
			xmlOutputter.endDocument();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return stringWriter.toString();
	}

}
