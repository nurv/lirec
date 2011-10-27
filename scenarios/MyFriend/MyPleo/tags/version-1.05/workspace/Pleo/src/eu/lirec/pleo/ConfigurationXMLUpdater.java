/** 
 * ConfigurationXMLUpdater.java - Activates, or deactivates, the automatic migration from ViPleo to
 * PhyPleo by editing the configuration xml.
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

public class ConfigurationXMLUpdater {
	private static final String LOG_TAG = "ConfigurationXMLUpdater";
	private static final String MYPLEO_TIMER_TAG = "myPleoTimer";
	private static final String MYPLEO_TIMER_ACTIVE = "active";
	
	private boolean _myPleoTimerActive;
	private File _xmlFile;

	ConfigurationXMLUpdater(File xmlFile,boolean myPleoTimerActive)
	{
		_myPleoTimerActive = myPleoTimerActive;
		_xmlFile = xmlFile;
	}

	void update() {
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
		Node rootNode = xmlDocument.getFirstChild();
		
		Node configurationNode = rootNode.getFirstChild();
		String configurationNodeName = configurationNode.getNodeName();
		while(true){
			if(configurationNodeName != null){
				if(configurationNodeName.compareTo(MYPLEO_TIMER_TAG)==0){
					break;
				}
			}
			configurationNode = configurationNode.getNextSibling();
			configurationNodeName = configurationNode.getNodeName();
		}
	
		NamedNodeMap myPleoTimerAttributes = configurationNode.getAttributes();
		
		if(myPleoTimerAttributes != null){
			Node activeAttribute = myPleoTimerAttributes.getNamedItem(MYPLEO_TIMER_ACTIVE);
			String newActiveAttributeValue = new Boolean(_myPleoTimerActive).toString();
			activeAttribute.setNodeValue(newActiveAttributeValue);
			
		}else{
			Log.e(LOG_TAG,"MyPleoTimerAttribute Bug");
		}
	}
}

