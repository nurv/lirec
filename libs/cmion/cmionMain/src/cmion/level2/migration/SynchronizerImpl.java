package cmion.level2.migration;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class SynchronizerImpl extends Synchronizer {
	
	public SynchronizerImpl(int listenPort) throws IOException, ParserConfigurationException {
		super(listenPort);
	}

	@Override
	protected void writeXML(Document doc, OutputStream stream){
		Transformer transformer = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
		
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		try {
			transformer.transform(new DOMSource(doc), new StreamResult(stream));
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
}
