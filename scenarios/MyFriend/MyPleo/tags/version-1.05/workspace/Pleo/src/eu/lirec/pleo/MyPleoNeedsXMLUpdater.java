/** 
 * MyPleoNeedsXMLUpdater.java - Updates the needs' xml according to a given set of need values.
 *  
 * Copyright (C) 2011 GAIPS/INESC-ID 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * Company: GAIPS/INESC-ID
 * Project: Pleo Scenario
 * @author: Paulo F. Gomes
 * Email to: pgomes@gaips.inesc-id.pt
 */

package eu.lirec.pleo;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.util.Log;

public class MyPleoNeedsXMLUpdater {
	private static final String LOG_TAG = "MyPleoNeedsXMLUpdater";
	
	private static final String XML_NEED_NAME_ATTRIBUTE = "name";
	private static final String XML_NEED_VALUE_ATTRIBUTE = "value";
	private static final String XML_NEED_NAME_CLEANLINESS = "cleanliness";
	private static final String XML_NEED_NAME_ENERGY = "energy";
	private static final String XML_NEED_NAME_PETTING = "petting";
	private static final String XML_NEED_NAME_SKILLS = "skills";
	private static final String XML_NEED_NAME_WATER = "water";
	
	private MyPleoNeedsVerifiable _myPleoNeedsVerifiable;
	private File _xmlFile;

	MyPleoNeedsXMLUpdater(File xmlFile,
			MyPleoNeedsVerifiable myPleoNeedsVerifiable) {
		_myPleoNeedsVerifiable = myPleoNeedsVerifiable;
		_xmlFile = xmlFile;
	}

	void loadMyPleoNeedsToXML() {
		try {
			Document xmlDocument = loadXMLDocument();
			updateXMLDocument(xmlDocument);
			writeXMLDocument(xmlDocument);

		} catch (ParserConfigurationException e) {
			Log.e(LOG_TAG, e.getMessage());
		} catch (SAXException e) {
			Log.e(LOG_TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage());
		} catch (TransformerConfigurationException e) {
			Log.e(LOG_TAG, e.getMessage());
		} catch (TransformerFactoryConfigurationError e) {
			Log.e(LOG_TAG, e.getMessage());
		} catch (TransformerException e) {
			Log.e(LOG_TAG, e.getMessage());
		}
	}

	private void writeXMLDocument(Document xmlDocument)
			throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, TransformerException {
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource sourceDOMXML = new DOMSource(xmlDocument);
		StreamResult xmlStreamResult = new StreamResult(_xmlFile);
		transformer.transform(sourceDOMXML, xmlStreamResult);
	}

	private Document loadXMLDocument() throws ParserConfigurationException,
			SAXException, IOException {
		Document xmlDocument;
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
		xmlDocument = documentBuilder.parse(_xmlFile);
		return xmlDocument;
	}

	private void updateXMLDocument(Document xmlDocument) {
		Node needsNode = xmlDocument.getFirstChild();
		Node needNode = needsNode.getFirstChild();
		while(needNode != null){
			NamedNodeMap needAttributes = needNode.getAttributes();
			if(needAttributes != null){
				Node needNameAttribute = needAttributes.getNamedItem(XML_NEED_NAME_ATTRIBUTE);
				String needName = needNameAttribute.getNodeValue();
				Node needValueAttribute = needAttributes.getNamedItem(XML_NEED_VALUE_ATTRIBUTE);
				
				updateNeedValue(needName, needValueAttribute);
			}

			needNode = needNode.getNextSibling();
		}
	}

	private void updateNeedValue(String needName, Node needValueAttribute) {
		int newNeedValue = -1;
		boolean valueAvailable = true;
		
		if(needName.equalsIgnoreCase(XML_NEED_NAME_CLEANLINESS) && _myPleoNeedsVerifiable.isNeedCleanlinessAvailable()){
			newNeedValue = _myPleoNeedsVerifiable.getNeedCleanliness();
		}else if(needName.equalsIgnoreCase(XML_NEED_NAME_ENERGY) && _myPleoNeedsVerifiable.isNeedEnergyAvailable()){
			newNeedValue = _myPleoNeedsVerifiable.getNeedEnergy();
		}else if(needName.equalsIgnoreCase(XML_NEED_NAME_PETTING) && _myPleoNeedsVerifiable.isNeedPettingAvailable()){
			newNeedValue = _myPleoNeedsVerifiable.getNeedPetting();
		}else if(needName.equalsIgnoreCase(XML_NEED_NAME_SKILLS) && _myPleoNeedsVerifiable.isNeedSkillsAvailable()){
			newNeedValue = _myPleoNeedsVerifiable.getNeedSkills();
		}else if(needName.equalsIgnoreCase(XML_NEED_NAME_WATER) && _myPleoNeedsVerifiable.isNeedWaterAvailable()){
			newNeedValue = _myPleoNeedsVerifiable.getNeedWater();
		}else{
			valueAvailable = false;
		}
		
		if(valueAvailable){
			String newValueString = Integer.toString(newNeedValue);
			needValueAttribute.setNodeValue(newValueString);
		}
	}
}
