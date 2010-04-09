package eu.lirec.myfriend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import ion.Meta.Element;
import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;
import eu.lirec.myfriend.Emotivector.MoodModel;
import eu.lirec.myfriend.Emotivector.Reactions;
import eu.lirec.myfriend.Piece.Colour;
import eu.lirec.myfriend.PlayedGame.Result;
import eu.lirec.myfriend.competences.Manager;
import eu.lirec.myfriend.competences.Migration;
import eu.lirec.myfriend.competences.Manager.AnimationIntent;
import eu.lirec.myfriend.competences.Manager.SayIntent;
import eu.lirec.myfriend.events.GameOver;
import eu.lirec.myfriend.events.IncomingMigration;
import eu.lirec.myfriend.events.MigrationStart;
import eu.lirec.myfriend.events.MovePlayed;
import eu.lirec.myfriend.events.MoveUndone;
import eu.lirec.myfriend.events.PieceEaten;
import eu.lirec.myfriend.requests.AcceptMigration;
import eu.lirec.myfriend.requests.Animate;
import eu.lirec.myfriend.requests.AnnounceVictory;
import eu.lirec.myfriend.requests.ChangeMood;
import eu.lirec.myfriend.requests.CheckTurn;
import eu.lirec.myfriend.requests.DeclareDraw;
import eu.lirec.myfriend.requests.EndGame;
import eu.lirec.myfriend.requests.ForfeitGame;
import eu.lirec.myfriend.requests.Greet;
import eu.lirec.myfriend.requests.MakeMove;
import eu.lirec.myfriend.requests.Migrate;
import eu.lirec.myfriend.requests.ProposeNewGame;
import eu.lirec.myfriend.requests.RecallGame;
import eu.lirec.myfriend.requests.RecognizeDefeat;
import eu.lirec.myfriend.requests.Say;
import eu.lirec.myfriend.requests.StartGame;
import eu.lirec.myfriend.requests.UndoMove;
import eu.lirec.myfriend.synchronization.events.MessageReceived;

public class ChessAgent extends ChessPlayer {
	
	private Manager manager;
	private double mood;
	private ChessEngine engine;
	private Move lastMove;
	private Migration migration;
	private Map<EventHandler, Element> eventHandlerList;
	private List<EventHandler> migrationHandlers;
	private Emotivector emotivector;
	private boolean emotivectorEnabled;
	private Map<Long, Memory> memory;
	private boolean memoryEnabled;
	private Date gameStart;
	private ChessUser opponent;
	private boolean firstMove;

	public ChessAgent(ChessBoard board, ChessEngine engine, Manager manager) {
		super(board);
		this.getRequestHandlers().add(new UserPropositionHandler());
		this.getRequestHandlers().add(new CheckTurnHandler());
		this.engine = engine;
		this.manager = manager;
		this.emotivector = new Emotivector();
		this.emotivectorEnabled = true;
		this.memory = new HashMap<Long, Memory>();
		this.memoryEnabled = true;
		this.firstMove = true;
		
		migrationHandlers = new ArrayList<EventHandler>();
		migrationHandlers.add(new OnMigrationIn());
		migrationHandlers.add(new OnMigrationOut());
		migrationHandlers.add(new MemoryMigrationIn());
		
		eventHandlerList = new HashMap<EventHandler, Element>();
		eventHandlerList.put(new MoveValidator(), board);
		eventHandlerList.put(new GameOverHandler(), board);
		eventHandlerList.put(new UndoListener(), board);
		addHandlers();
	}
	
	public ChessAgent(ChessBoard board, ChessEngine engine, Manager manager, boolean enableEmotivector) {
		this(board, engine, manager);
		this.emotivectorEnabled = enableEmotivector;
	}
	
	public void setOpponent(ChessUser user){
		this.opponent = user;
	}
	
	public void setEmotivectorState(boolean enabled){
		this.emotivectorEnabled = enabled;
	}
	
	public void setMemoryState(boolean enabled){
		this.memoryEnabled = enabled;
	}
	
	public void changeMoodModel(MoodModel model){
		emotivector.setMoodModel(model);
	}
	
	public void greet(){
		manager.schedule(new Greet());
		if(memoryEnabled){
			Memory userMemory = memory.get(opponent.getId());
			PlayedGame game = null;
			if(userMemory != null){
				game = userMemory.getLastGame();
			}
			manager.schedule(new RecallGame(game));
		}
	}
	
	public void setMigrationCompetence(Migration migration){
		if(this.migration != null){
			for (EventHandler handler : migrationHandlers) {
				this.migration.getEventHandlers().remove(handler);
			}
		}
		
		this.migration = migration;
		if(this.migration != null){
			for (EventHandler handler : migrationHandlers) {
				this.migration.getEventHandlers().add(handler);
			}
		}
	}
	
	public void kill(){
		removeHandlers();
	}
	
	public void clearMemory(){
		memory.clear();
	}
	
	public void saveMemory(OutputStream stream){
		Document memoryDoc = new Document();
		memoryDoc.setEncoding("UTF-8");
		
		memoryDoc.addChild(Node.ELEMENT, saveMemory());
		
		KXmlSerializer serializer = new KXmlSerializer();
		
		try {
			serializer.setOutput(stream, memoryDoc.getEncoding());
			memoryDoc.write(serializer);
		} catch (IOException e) {
			System.err.println("Failed to save memory.");
			e.printStackTrace();
		}
	}
	
	private org.kxml2.kdom.Element saveMemory(){
		SimpleDateFormat format = new SimpleDateFormat("d-M-yyyy_H-mm-ss");
		
		org.kxml2.kdom.Element memoryXml = new org.kxml2.kdom.Element();
		memoryXml.setName("memory");

		for (Memory userMemory : memory.values()) {

			org.kxml2.kdom.Element userXml = memoryXml.createElement("", "User");
			userXml.setAttribute("", "name", userMemory.userName);
			memoryXml.addChild(Node.ELEMENT, userXml);

			for (PlayedGame game : userMemory.getGames()) {

				org.kxml2.kdom.Element gameXml = userXml.createElement("","Game");
				userXml.addChild(Node.ELEMENT, gameXml);

				org.kxml2.kdom.Element dateXml = gameXml.createElement("","Date");
				dateXml.addChild(Node.TEXT, format.format(game.getEnd()));
				gameXml.addChild(Node.ELEMENT, dateXml);

				org.kxml2.kdom.Element resultXml = gameXml.createElement("","Result");
				switch (game.getResult()) {
					case Lost:
						resultXml.addChild(Node.TEXT, "0");
						break;
					case Won:
						resultXml.addChild(Node.TEXT, "1");
						break;
					case Drawn:
						resultXml.addChild(Node.TEXT, "2");
						break;
					default:
						break;
				}
				gameXml.addChild(Node.ELEMENT, resultXml);
			}
		}
		
		return memoryXml;
	}
	
	public void loadMemory(InputStream stream){
		
		Document xmlDoc = new Document();
		XmlPullParser parser = new KXmlParser();

		try {
			parser.setInput(stream, null);
			xmlDoc.parse(parser);
		} catch (XmlPullParserException e) {
			System.err.println("Could not load memory from XML.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Could not load memory from XML.");
			e.printStackTrace();
		}
		
		if(xmlDoc.getChildCount() > 0){
			org.kxml2.kdom.Element memoryXml = xmlDoc.getRootElement();
			loadMemory(memoryXml);
		}
	}
	
	private void loadMemory(org.kxml2.kdom.Element memoryXml){
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("d-M-yyyy_H-mm-ss");
		memory = new HashMap<Long, Memory>();
		
		for (int i = 0; i < memoryXml.getChildCount() ; i++) {
			org.kxml2.kdom.Element userXml = memoryXml.getElement(i);
			
			if(userXml != null){
				Memory userMemory = new Memory(userXml.getAttributeValue("", "name"));
				memory.put(0l, userMemory);

				for (int j = 0; j < userXml.getChildCount(); j++) {
					org.kxml2.kdom.Element gameXml = userXml.getElement(j);
					
					if(gameXml != null){
						String dateStr = gameXml.getElement("", "Date").getText(0).trim();
						String resultStr = gameXml.getElement("", "Result").getText(0).trim();
						
						Date date = null;
						try {
							date = dateFormat.parse(dateStr);
						} catch (ParseException e) {
							System.err.println("Date of memory of user "+userMemory.userName+" is unreadable.");
							e.printStackTrace();
						}
						
						Result result = null;
						switch (resultStr.charAt(0)) {
							case '0':
								result = Result.Lost;
								break;
							case '1':
								result = Result.Won;
								break;
							case '2':
								result = Result.Drawn;
								break;
							default:
								System.err.println("Result of memory of user "+userMemory.userName+" is unreadable.");
								break;
						}
						
						PlayedGame game = new PlayedGame(null, date, result);
						userMemory.addGame(game);
					}
				}
			}
		}
	}
	
	private void removeHandlers(){
		
		for (Entry<EventHandler, Element> pair : eventHandlerList.entrySet()) {
			pair.getValue().getEventHandlers().remove(pair.getKey());
		}
	}
	
	private void addHandlers(){
		for (Entry<EventHandler, Element> pair : eventHandlerList.entrySet()) {
			pair.getValue().getEventHandlers().add(pair.getKey());
		}
	}
	
	private boolean checkGameEnd(){
		boolean gameEnded = engine.gameEnded(board); 
		
		if(gameEnded){
			System.out.println("Game Ended");
			board.schedule(new EndGame(engine.checkWinner(board)));
		}
		
		return gameEnded;
	}
	
	private AnimationIntent translateEmotionalReaction(Reactions reaction){
		AnimationIntent animation;

		switch (reaction) {
		case StrongerReward:
			animation = AnimationIntent.MoreExcited;
			break;
		case ExpectedReward:
			animation = AnimationIntent.Excited;
			break;
		case WeakerReward:
			animation = AnimationIntent.LessExcited;
			break;
		case UnexpectedReward:
			animation = AnimationIntent.GoodSurprise;
			break;
		case Think:
			animation = AnimationIntent.Think;
			break;
		case UnexpectedPunishment:
			animation = AnimationIntent.BadSurprise;
			break;
		case WeakerPunishment:
			animation = AnimationIntent.LessUnhappy;
			break;
		case ExpectedPunishment:
			animation = AnimationIntent.Unhappy;
			break;
		case StrongerPunishment:
			animation = AnimationIntent.MoreUnhappy;
			break;
		default:
			animation = null;
			break;
		}
		
		return animation;
	}
	
	
	private class MoveValidator extends EventHandler{

		public MoveValidator() {
			super(MovePlayed.class);
		}

		@Override
		public void invoke(IEvent evt) {
			MovePlayed movePlayed = (MovePlayed) evt;
			
			//Filter own moves
			if(lastMove != null 
					&& movePlayed.from.equals(lastMove.getFrom())
					&& movePlayed.to.equals(lastMove.getTo())){
				
				if(checkGameEnd()){
					
				} else if(engine.isInCheck(board, Colour.White)){
					manager.schedule(new Say(SayIntent.Check));
				} else if(movePlayed instanceof PieceEaten){
					manager.schedule(new Say(SayIntent.AtePiece));
				}
				
				return;
			}
			
			if(firstMove){
				firstMove = false;
				gameStart = new Date();
			}

			//FIXME Lock occurs when the undo request is made after the agent made his move.
			if(movePlayed.oldBoard.nextPieceToMove() == Colour.Black){
				manager.schedule(new Say(SayIntent.WrongTurn));
				manager.schedule(new Animate(AnimationIntent.WrongTurn));
				
				board.schedule(new UndoMove(movePlayed.oldBoard));
				System.out.println("Current turn: "+board.nextPieceToMove() );
				System.out.println("Move out of turn detected.");
				return;
			}
			
			if(!engine.isMoveLegal(movePlayed.oldBoard, movePlayed.from, movePlayed.to)){
				manager.schedule(new Say(SayIntent.IllegalMove));
				manager.schedule(new Animate(AnimationIntent.IllegalMove));
				
				board.schedule(new UndoMove(movePlayed.oldBoard));
				System.out.println("Current turn: "+board.nextPieceToMove() );
				engine.printLegalMoves(movePlayed.oldBoard);
				System.out.println("Illegal move detected.");
				return;
			}
			
			if(movePlayed instanceof PieceEaten){
				manager.schedule(new Say(SayIntent.LostPiece));
			}
			
			new Thread(new MovePlayer()).start();
		}
	}
	
	
	private class UserPropositionHandler extends RequestHandler{
		
		public UserPropositionHandler() {
			super(new TypeSet(ProposeNewGame.class, ForfeitGame.class, Migrate.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			for (ProposeNewGame request : requests.get(ProposeNewGame.class)) {
				greet();
				board.schedule(new StartGame());
				gameStart = new Date();
				return;
			}
			
			for (ForfeitGame request : requests.get(ForfeitGame.class)) {
				manager.schedule(new Say("I was getting tired of this game too."));
				board.schedule(new EndGame());
				return;
			}
			
			for (Migrate request : requests.get(Migrate.class)) {
				System.out.println("Requesting migration to agent");
				manager.schedule(new AcceptMigration(request));
			}
		}
	}

	
	private class CheckTurnHandler extends RequestHandler{
		
		public CheckTurnHandler() {
			super(new TypeSet(CheckTurn.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			for (CheckTurn request : requests.get(CheckTurn.class)) {
				if(board.nextPieceToMove() == Colour.Black){
					new Thread(new MovePlayer()).start();
				}
			}
		}
	}
	
	
	private class MovePlayer implements Runnable {
		@Override
		public void run() {
			Move move;
			move = engine.calculateMove(board.toArray(), Colour.Black);
			
			if(move != null){
				lastMove = move;
				
				switch (move.getType()) {
					case Normal:
						manager.schedule(new MakeMove(move.getFrom(), move.getTo()));
						break;
					case PawnReplace:
						Piece replacement = move.getReplacementPiece();
						manager.schedule(new MakeMove(move.getFrom(), move.getTo(), replacement));
						break;

					default:
						System.err.println("Move Type not recognized.");
						break;
				}
				
				
				if(emotivectorEnabled){
					Reactions reaction;
					emotivector.updateValues(move.getHeuristic());
					reaction = emotivector.getReaction();
					mood = emotivector.calculateMood(move.getHeuristic());
					
					System.out.println("**Mood: "+mood);
					System.out.println("**Reaction: "+ reaction);
					
					if(!engine.gameEnded(board)){
						manager.schedule(new ChangeMood(mood));
						manager.schedule(new Animate(translateEmotionalReaction(reaction)));
					}
				}
			} else {
				checkGameEnd();
			}
		}
	}
	
	
	private class GameOverHandler extends EventHandler {
		
		public GameOverHandler() {
			super(GameOver.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			GameOver gameOver = (GameOver) evt;
			Result result;
			
			if(gameOver.winner == Colour.White){
				System.out.println("WON!!!");
				manager.schedule(new RecognizeDefeat());
				result = Result.Lost;
			} else if(gameOver.winner == Colour.Black){
				System.out.println("LOST!!!");
				manager.schedule(new AnnounceVictory());
				result = Result.Won;
			} else {
				System.out.println("DRAW!!!");
				result = Result.Drawn;
				manager.schedule(new DeclareDraw());
				System.err.println("Game has ended and no winner has been detected.");
			}
			
			Memory userMemory = memory.get(opponent.getId());
			
			if(userMemory == null){
				userMemory = new Memory(opponent.getName());
				memory.put(opponent.getId(), userMemory);
			}
			
			userMemory.addGame(new PlayedGame(gameStart, new Date(), result));
		}
	}

	
	private class OnMigrationOut extends EventHandler {

		public OnMigrationOut() {
			super(MigrationStart.class);
		}

		@Override
		public void invoke(IEvent evt) {
			removeHandlers();
			migration.addMigrationData(saveMemory());
			migration.addMigrationData(null);
		}
	}
	
	
	private class OnMigrationIn extends EventHandler {
		
		public OnMigrationIn() {
			super(IncomingMigration.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			//TODO What should the agent do on Migration?
			addHandlers();
			schedule(new CheckTurn());
		}
	}
	
	private class MemoryMigrationIn extends EventHandler{
		
		public MemoryMigrationIn() {
			super(MessageReceived.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			MessageReceived messageEvent = (MessageReceived) evt;
			
			if(messageEvent.type.equals("memory")){
				loadMemory(messageEvent.message);
			}
			
			if(messageEvent.type.equals("emotivector")){
			}
		}
	}
	
	private class UndoListener extends EventHandler {
		
		public UndoListener(){
			super(MoveUndone.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			manager.schedule(new Say(SayIntent.PlayAgain));
		}
	}
}
