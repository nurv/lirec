package eu.lirec.myfriend.competences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kxml2.kdom.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import eu.lirec.myfriend.competences.Manager.SayIntent;
import eu.lirec.myfriend.events.Ended;
import eu.lirec.myfriend.events.Failed;
import eu.lirec.myfriend.requests.ChangeMood;
import eu.lirec.myfriend.requests.ExecuteSequence;
import eu.lirec.myfriend.requests.MakeMove;
import eu.lirec.myfriend.requests.Migrate;
import eu.lirec.myfriend.requests.Say;
import eu.lirec.myfriend.requests.Animate;
import eu.lirec.myfriend.requests.StartTalking;
import eu.lirec.myfriend.requests.StopTalking;
import ion.Meta.Element;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;

public class ExecuteMonitor extends Element {
	
	private HashMap<Class<? extends Request>, ArrayList<Competence>> competencies;
	private HashMap<SayIntent, List<String>> sentences;

	public ExecuteMonitor(){
		this.competencies = new HashMap<Class<? extends Request>, ArrayList<Competence>>();
		this.sentences = new HashMap<SayIntent, List<String>>();
		
		for (SayIntent intent : SayIntent.values()) {
			sentences.put(intent, new ArrayList<String>());
		}
		
		this.getRequestHandlers().add(new SayManager());
		this.getRequestHandlers().add(new PlayManager());
		this.getRequestHandlers().add(new AnimationManager());
		this.getRequestHandlers().add(new SequenceManager());
		this.getRequestHandlers().add(new MigrationManager());
		this.getRequestHandlers().add(new MoodManager());
	}
	
	public boolean registerCompetence(Competence competence, Class<? extends Request> type){
		
		if(competencies.get(type) == null){
			competencies.put(type, new ArrayList<Competence>());
		} else if(competencies.get(type).contains(competence)){
			return false;
		}
		
		//TODO Apenas para teste da migração. Remover.
		/*
		if (type.equals(Migrate.class)){
			competence.getEventHandlers().add(new MigrationTest());
		}
		*/
		
		return competencies.get(type).add(competence);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
	}
	
	public void clearSentences(){
		for (SayIntent intent : SayIntent.values()) {
			sentences.get(intent).clear();
		}
	}
	
	public void importSentences(XmlPullParser parser) throws IOException, XmlPullParserException{
		Document document = new Document();
		document.parse(parser);
		
		org.kxml2.kdom.Element root = document.getRootElement();
		
		for (int i=0; i < root.getChildCount() ; i++) {
			org.kxml2.kdom.Element group = root.getElement(i);
			
			if(group != null){
				List<String> sentenceList;
				
				if(group.getName().equals("greet")){
					sentenceList = sentences.get(SayIntent.Greet);
				} else if (group.getName().equals("illegalmove")){
					sentenceList = sentences.get(SayIntent.IllegalMove);
				} else if (group.getName().equals("wrongturn")){
					sentenceList = sentences.get(SayIntent.WrongTurn);
				} else if (group.getName().equals("lostpiece")){
					sentenceList = sentences.get(SayIntent.LostPiece);
				} else if (group.getName().equals("atepiece")){
					sentenceList = sentences.get(SayIntent.AtePiece);
				} else if (group.getName().equals("playagain")){
					sentenceList = sentences.get(SayIntent.PlayAgain);
				} else if (group.getName().equals("check")){
					sentenceList = sentences.get(SayIntent.Check);
				} else if (group.getName().equals("acceptmigration")){
					sentenceList = sentences.get(SayIntent.AcceptMigration);
				} else if (group.getName().equals("recall-won")){
					sentenceList = sentences.get(SayIntent.RecallWon);
				} else if (group.getName().equals("recall-lost")){
					sentenceList = sentences.get(SayIntent.RecallLost);
				} else if (group.getName().equals("recall-draw")){
					sentenceList = sentences.get(SayIntent.RecallDraw);
				} else if (group.getName().equals("won-game")){
					sentenceList = sentences.get(SayIntent.WonGame);
				} else if (group.getName().equals("lost-game")){
					sentenceList = sentences.get(SayIntent.LostGame);
				} else {
					SayIntent intent = null;
					try {
						intent = SayIntent.valueOf(group.getName());
						sentenceList = sentences.get(intent);
					} catch (RuntimeException e) {
						continue;
					}
				}
				
				for (int j=0; j < group.getChildCount(); j++) {
					org.kxml2.kdom.Element sentence = group.getElement(j);
					
					if(sentence != null){
						sentenceList.add(sentence.getAttributeValue("", "text"));
					}
				}
			}
		}
	}

	private class SayManager extends RequestHandler implements OnCompetenceCompletion{
		
		private List<Say> requestQueue;
		private Set<EndListener> pendingCompetences;

		public SayManager() {
			super(new TypeSet(Say.class));
			this.requestQueue = new ArrayList<Say>();
			this.pendingCompetences = new HashSet<EndListener>(2);
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			for (Say request : requests.get(Say.class)) {
				System.out.println("Say Request with intent "+request.intent);
				boolean add = true;
				
				if(this.requestQueue.isEmpty()){
					add = executeRequest(request);
				}
				
				if(add){
					this.requestQueue.add(request);
				}
			}
		}
		
		@Override
		public void competenceCompleted(EndListener handler) {
			this.pendingCompetences.remove(handler);
			
			System.out.println("Say competence completed: "+handler.getCompetence()+" "+handler.getEndedEvent().request);
			
			/*
			if(handler.getCompetence() instanceof SpeechPlayback){
				for (Competence competence : competencies.get(Animate.class)){
					competence.schedule(new StopTalking());
				}
			}
			*/
			
			if(this.pendingCompetences.isEmpty()){
				Say request = this.requestQueue.remove(0);
				
				while(!this.requestQueue.isEmpty()){
					if(executeRequest(this.requestQueue.get(0))){
						return;
					} else {
						this.requestQueue.remove(0);
					}
				}
				
				raise(new Ended(request));
			}
		}
		
		private boolean executeRequest(Say request){
			Say mappedRequest;
			
			if(request.intent.equals(SayIntent.Text)){
				mappedRequest = request;
			} else {
				List<String> intentSentences = sentences.get(request.intent);
				
				if(intentSentences.isEmpty()){
					raise(new Failed(request));
					return false;
				}
				
				int index =(int) (Math.random() * intentSentences.size());
				mappedRequest = new Say(intentSentences.get(index));
			}
			
			for (Competence competence : competencies.get(Say.class)) {
				competence.schedule(mappedRequest);
				EndListener completionHandler = new EndListener(this, competence);
				this.pendingCompetences.add(completionHandler);
			}
			
			/*
			for (Competence competence : competencies.get(Animate.class)){
				competence.schedule(new StartTalking());
			}
			*/
			
			return true;
		}
		
	}
	
	private class PlayManager extends RequestHandler implements OnCompetenceCompletion{

		protected PlayManager() {
			super(new TypeSet(MakeMove.class));
		}

		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			for (MakeMove request : requests.get(MakeMove.class)) {
				competencies.get(MakeMove.class).get(0).schedule(request);
				new EndListener(this,competencies.get(MakeMove.class).get(0), request);
			}
		}
		
		@Override
		public void competenceCompleted(EndListener handler) {
			raise(handler.getEndedEvent());
		}
	}
	
	private class AnimationManager extends RequestHandler implements OnCompetenceCompletion{

		protected AnimationManager() {
			super(new TypeSet(Animate.class));
		}

		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			for (Animate request : requests.get(Animate.class)) {
				
				for (Competence competence : competencies.get(Animate.class)) {
					competence.schedule(request);
					new EndListener(this, competence, request);
				}
			}
		}
		
		@Override
		public void competenceCompleted(EndListener handler) {
			raise(handler.getEndedEvent());
			System.out.println("Re-raised success of animation!!!");
		}
	}

	private class MoodManager extends RequestHandler {

		public MoodManager() {
			super(new TypeSet(ChangeMood.class));
		}

		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			ChangeMood request = requests.get(ChangeMood.class).getFirst();
			
			for (Competence competence : competencies.get(Animate.class)) {
				competence.schedule(request);
			}
		}
	}
	
	private class SequenceManager extends RequestHandler {
		
		public SequenceManager() {
			super(new TypeSet(ExecuteSequence.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {

			System.out.println("Sequencing or not...");
			
			for (ExecuteSequence request : requests.get(ExecuteSequence.class)) {
				SequenceExecuter executer = new SequenceExecuter(request);
				executer.start();
			}
		}
		
		private class SequenceExecuter implements OnCompetenceCompletion{
			
			private ExecuteSequence request;
			private List<List<Request>> queue;
			private List<EndListener> pendingTermination;
			private boolean started;
			
			public SequenceExecuter(ExecuteSequence request){
				this.queue = request.getSequence();
				this.pendingTermination = new ArrayList<EndListener>();
				this.started = false;
				this.request = request;
			}
			
			public void start(){
				if(!started){
					started = true;
					executeStep(this.queue.get(0));
					System.out.println("Starting sequence execution");
				}
			}
			
			@Override
			public void competenceCompleted(EndListener handler) {
				pendingTermination.remove(handler);
				
				if(this.pendingTermination.isEmpty()){
					this.queue.remove(0);
					if(!this.queue.isEmpty()){
						executeStep(this.queue.get(0));
					} else {
						raise(new Ended(this.request));
					}
				}
			}
			
			private void executeStep(List<Request> step){
				for (Request request : step) {
					schedule(request);
					pendingTermination.add(new EndListener(this, ExecuteMonitor.this, request));
				}
			}
		}
	}
	
	private class MigrationManager extends RequestHandler {
		
		public MigrationManager() {
			super(new TypeSet(Migrate.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			System.out.println("Migration request on Execute Monitor");
			
			Migrate request = requests.get(Migrate.class).getFirst();
			
			Competence competence = competencies.get(Migrate.class).get(0);
			
			competence.schedule(request);
			
			//TODO Migration events should inherit from the basic Ended/Successful/Failed to use the EndListener
			raise(new Ended(request));
		}
	}

	//TODO delete after testing done
	/*
	private class MigrationTest extends EventHandler{
		
		public MigrationTest() {
			super(MessageReceived.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			MessageReceived message = (MessageReceived) evt;
			System.out.println("Recebi mensagem " + message.type);
			ExecuteMonitor.this.schedule(new Say("Recebi mensagem " + message.type));
		}
	}
	*/
}
