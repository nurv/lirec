package cmion.level2.migration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Xml;

public class AndroidSynchronizer extends Synchronizer {

	private Context ctx;
	
	public AndroidSynchronizer(int listenPort, Context ctx) throws IOException, ParserConfigurationException {
		super(listenPort);
		this.ctx = ctx;
	}
	
	@Override
	protected void writeXML(Document doc, OutputStream stream) {
	
		writeXML2(doc, stream);
		
		// writes the xml document to a file for debugging purposes
//		try {
//			writeXML2(doc, ctx.openFileOutput("message.xml", Context.MODE_WORLD_WRITEABLE));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	protected void writeXML2(Document doc, OutputStream stream) {
		XmlSerializer serializer = Xml.newSerializer();
		
		try {
			serializer.setOutput(stream, "UTF-8");
			
			serializer.startDocument("UTF-8", Boolean.valueOf(true));
			serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
			
			Element root = doc.getDocumentElement();
			if (root != null){
				writeElement(doc.getDocumentElement(), serializer);
			}
			
			serializer.endDocument();
			serializer.flush();
			
		} catch (IllegalArgumentException e) {
			System.err.println("Could not write the XML Document.");
		} catch (IllegalStateException e) {
			System.err.println("Could not write the XML Document.");
		} catch (IOException e) {
			System.err.println("Could not write the XML Document.");
		}
	}
	
	private void writeElement(Element element, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException{

		serializer.startTag(null, element.getTagName());
		
		NamedNodeMap attributes = element.getAttributes();
		
		for(int i=0 ; i < attributes.getLength() ; i++){
			Attr attribute = (Attr) attributes.item(i);
			
			serializer.attribute(null, attribute.getName(), attribute.getValue());
		}
		
		NodeList children = element.getChildNodes();
		
		for(int i=0 ; i < children.getLength() ; i++){
			Node child = children.item(i);
			
			switch(child.getNodeType()){
				case Node.ELEMENT_NODE:
					writeElement((Element) child, serializer);
					break;
				
				case Node.TEXT_NODE:
					serializer.text(child.getNodeValue());
					break;
			}
		}
		
		serializer.endTag(null, element.getTagName());
	}
}
