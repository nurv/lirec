package cmion.level2.migration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cmion.util.WrappedInputStream;
import cmion.util.WrappedOutputStream;


import ion.Meta.Element;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;

public class Synchronizer extends Element {
	
	public final static String SYNC_TAG = "synchronize";
	
	private ServerSocket incomingConnections;
	private Socket receivedConnection;
	private Thread listenerThread;
	private Document fullMessage;
	private DocumentBuilder docBuilder;

	public Synchronizer(int listenPort) throws IOException, ParserConfigurationException{
		this.getRequestHandlers().add(new SynchronizationManager());
		this.getRequestHandlers().add(new MessageSender());
		this.getRequestHandlers().add(new ReplyHandler());
		
		this.incomingConnections = new ServerSocket(listenPort);
		this.listenerThread = new Thread(new Listener(), "Sync Listener");
		this.listenerThread.start();
		
		this.docBuilder = createDocumentBuilder();
	}
	
	@Override
	public void onDestroy() {
		try {
			System.out.println("Closing SOCKET");
			this.incomingConnections.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void packMessage(org.w3c.dom.Element message){
		try {
			fullMessage.adoptNode(message);
			fullMessage.getElementsByTagName(SYNC_TAG).item(0).appendChild(message);
		} catch (DOMException e) {
			fullMessage.importNode(message, true);
		}
	}
	
	public void synchronize(String host, int port){
		this.schedule(new Synchronize(host, port));
	}
	
	private Socket connect(String host, int port){
		Socket socket = null;
		
		try {
			socket = new Socket(host, port);
			socket.setSoTimeout(60000);
		} catch (UnknownHostException e) {
			System.err.println(e);
		} catch (SocketException e){
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
		
		return socket;
	}
	
	private DocumentBuilder createDocumentBuilder() throws ParserConfigurationException{
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		return builder;
	}
	
	private void writeXML(Document doc, OutputStream stream){
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
	
	private void sendXMLMessage(Document message, Socket socket) throws IOException{

		WrappedOutputStream outStream = new WrappedOutputStream(socket.getOutputStream());
		
		writeXML(message, outStream);
		outStream.close();
	}
	
	private class Listener implements Runnable {
		
		@Override
		public void run() {

			try {
				receivedConnection = incomingConnections.accept();

				receiveMessage(receivedConnection.getInputStream());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void receiveMessage(InputStream stream) throws SAXException, IOException{

			WrappedInputStream inStream = new WrappedInputStream(stream);
			
			Document doc = docBuilder.parse(inStream);
			
			NodeList nodes = doc.getElementsByTagName(SYNC_TAG);
			org.w3c.dom.Element root = (org.w3c.dom.Element) nodes.item(0);
			
			if(root == null){
				return;
			}
			
			nodes = root.getChildNodes();
			for (int i=0 ; i<nodes.getLength() ; i++){
				Node node = nodes.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE){
					raise(new MessageReceived((org.w3c.dom.Element)node));
				}
			}
			
			inStream.close();
		}
	}

	private class AcknowledgeWait implements Runnable {
		
		private Socket socket;
		
		public AcknowledgeWait(Socket socket){
			this.socket = socket;
		}
		
		@Override
		public void run() {
			WrappedInputStream stream = null;
			
			Document doc;
			try {
				stream = new WrappedInputStream(socket.getInputStream());
				doc = docBuilder.parse(stream);
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			} catch (SAXException e1) {
				e1.printStackTrace();
				return;
			}
			
			NodeList nodes = doc.getElementsByTagName(SYNC_TAG);
			org.w3c.dom.Element root = (org.w3c.dom.Element) nodes.item(0);
			
			if(root == null){
				return;
			}
			
			nodes = root.getChildNodes();
			for (int i=0 ; i<nodes.getLength() ; i++){
				Node node = nodes.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE){
					raise(new MessageReceived((org.w3c.dom.Element)node));
					return;
				}
			}
			
			try {
				if(stream != null){
					stream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			raise(new MessageDeliveryFailed());
		}
	}
	
	private class SendMessage extends Request {
		
		public final String host;
		public final int port;
		
		public SendMessage(String host, int port){
			this.host = host;
			this.port = port;
		}
		
	}
	
	private class SynchronizationManager extends RequestHandler {
		
		public SynchronizationManager() {
			super(new TypeSet(Synchronize.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			for (Synchronize request : requests.get(Synchronize.class)) {
				System.out.println("Synchronization Manager");
				fullMessage = docBuilder.newDocument();
				org.w3c.dom.Element root = fullMessage.createElement(SYNC_TAG);
				fullMessage.appendChild(root);
				schedule(new SendMessage(request.host, request.port));
				raise(new SynchronizationStart(request.host, request.port, fullMessage));
			}
		}
	}
	
	private class MessageSender extends RequestHandler {
		
		public MessageSender(){
			super(new TypeSet(SendMessage.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			for (SendMessage request : requests.get(SendMessage.class)) {
				
				Socket socket = connect(request.host, request.port); 
				
				if(socket == null){
					raise(new ConnectionFailed(request.host, request.port));
					fullMessage = null;
					break;
				}
				
				try {
					System.out.println("Sending Message");
					sendXMLMessage(fullMessage, socket);
				} catch (IOException e) {
					System.out.println("Message sending failed.");
					raise(new MessageFailed());
					fullMessage = null;
					try {
						socket.close();
					} catch (IOException e1) {
					}
					break;
				}
				
				System.out.println("Message Sent");
				fullMessage = null;
				raise(new MessageSent());
				new Thread(new AcknowledgeWait(socket)).start();
			}
		}
	}

	private class ReplyHandler extends RequestHandler {
		
		public ReplyHandler() {
			super(new TypeSet(Reply.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			for (Reply request : requests.get(Reply.class)) {
				
				Document doc = docBuilder.newDocument();
				org.w3c.dom.Element root = doc.createElement(SYNC_TAG);
				doc.appendChild(root);
				
				Node messageNode = doc.adoptNode(request.message.getDocumentElement());
				root.appendChild(messageNode);
				
				writeXML(doc, System.out);
				
				try {
					sendXMLMessage(doc, receivedConnection);
				} catch (IOException e) {
					e.printStackTrace();
					raise(new ReplyFailed(request));
					continue;
				}
				
				raise(new ReplySuccess(request));

				try {
					receivedConnection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				receivedConnection = null;
				listenerThread = new Thread(new Listener(), "Sync Listener");
				listenerThread.start();
				
				break;
			}
		}
	}
}
