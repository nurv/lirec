package eu.lirec.myfriend.synchronization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import eu.lirec.myfriend.synchronization.events.ConnectionFailed;
import eu.lirec.myfriend.synchronization.events.MessageDelivered;
import eu.lirec.myfriend.synchronization.events.MessageDeliveryFailed;
import eu.lirec.myfriend.synchronization.events.MessageFailed;
import eu.lirec.myfriend.synchronization.events.MessageReceived;
import eu.lirec.myfriend.synchronization.events.MessageSent;
import eu.lirec.myfriend.synchronization.events.ReplyFailed;
import eu.lirec.myfriend.synchronization.events.ReplySuccess;
import eu.lirec.myfriend.synchronization.events.SynchronizationStart;
import eu.lirec.myfriend.synchronization.requests.Reply;
import eu.lirec.myfriend.synchronization.requests.Synchronize;

import ion.Meta.Element;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;

public class Synchronizer extends Element {
	
	private ServerSocket incomingConnections;
	private Socket receivedConnection;
	private Thread listenerThread;
	private Document fullMessage;

	public Synchronizer(int listenPort) throws IOException{
		this.getRequestHandlers().add(new SynchronizationManager());
		this.getRequestHandlers().add(new MessageSender());
		this.getRequestHandlers().add(new ReplyHandler());
		
		this.incomingConnections = new ServerSocket(listenPort);
		this.listenerThread = new Thread(new Listener(), "Sync Listener");
		this.listenerThread.start();
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
	
	public void packMessage(org.kxml2.kdom.Element message){
		fullMessage.getRootElement().addChild(Node.ELEMENT, message);
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
			e.printStackTrace();
		} catch (SocketException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return socket;
	}
	
	private void sendXMLMessage(Document message, Socket socket) throws IOException{
		
		XmlSerializer serializer = new KXmlSerializer();
		WrappedOutputStream outStream = new WrappedOutputStream(socket.getOutputStream());
		
		serializer.setOutput(outStream, message.getEncoding());
		message.write(serializer);
		outStream.close();
	}
	
	private class Listener implements Runnable {
		
		@Override
		public void run() {

			try {
				System.out.println("waiting for connections");
				receivedConnection = incomingConnections.accept();

				System.out.println("receiving message");
				receiveMessage(receivedConnection.getInputStream());

//				System.out.println("acknowledging message");
//				acknowledgeMessage(receivedConnection.getOutputStream());
//
//				System.out.println("closing socket");
//				receivedConnection.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void acknowledgeMessage(OutputStream stream) throws IOException{
			
			WrappedOutputStream outStream = new WrappedOutputStream(stream);
			XmlSerializer serializer = new KXmlSerializer();

			Document document = new Document();
			org.kxml2.kdom.Element element = document.createElement("", "acknowledge");
			element.addChild(Node.TEXT, "Message was received correctly.");
			document.addChild(0, Node.ELEMENT, element);
			
			serializer.setOutput(outStream, document.getEncoding());

			document.write(serializer);
			outStream.close();
		}
		
		private void receiveMessage(InputStream stream) throws IOException{

			WrappedInputStream inStream = new WrappedInputStream(stream);
			XmlPullParser parser = new KXmlParser();
			Document document = new Document();
			
			
			try {
				parser.setInput(inStream, document.getEncoding());
				document.parse(parser);
				org.kxml2.kdom.Element root = document.getRootElement();
				
				for(int i=0 ; i < root.getChildCount() ; i++){
					org.kxml2.kdom.Element message = root.getElement(i);
					
					if (message != null){
						raise(new MessageReceived(message));
					}
				}
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			XmlPullParser parser = new KXmlParser();
			Document document = new Document();
			WrappedInputStream stream = null;
			
			System.out.println("Going to wait for ack.");
			
			try {
				stream = new WrappedInputStream(socket.getInputStream());
				parser.setInput(stream, document.getEncoding());
				document.parse(parser);
				
				System.out.println("Answer received.");
				raise(new MessageDelivered(document));
				stream.close();
				return;
				
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("Answer not received.");
			
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
				fullMessage = new Document();
				org.kxml2.kdom.Element root = fullMessage.createElement("", "synchronize");
				fullMessage.addChild(0, Node.ELEMENT, root);
				schedule(new SendMessage(request.host, request.port));
				raise(new SynchronizationStart(request.host, request.port, root));
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
				
				System.out.println("Connecting to destination device.");
				Socket socket = connect(request.host, request.port); 
				
				if(socket == null){
					System.out.println("connection failed");
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
				
				try {
					sendXMLMessage(request.message, receivedConnection);
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
