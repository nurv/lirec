package eu.lirec.myfriend.competences;

import java.util.ArrayList;
import java.util.List;

import eu.lirec.myfriend.PlayedGame;
import eu.lirec.myfriend.requests.AcceptMigration;
import eu.lirec.myfriend.requests.AnnounceVictory;
import eu.lirec.myfriend.requests.ChangeMood;
import eu.lirec.myfriend.requests.DeclareDraw;
import eu.lirec.myfriend.requests.ExecuteSequence;
import eu.lirec.myfriend.requests.Greet;
import eu.lirec.myfriend.requests.MakeMove;
import eu.lirec.myfriend.requests.RecallGame;
import eu.lirec.myfriend.requests.RecognizeDefeat;
import eu.lirec.myfriend.requests.Say;
import eu.lirec.myfriend.requests.Animate;

import ion.Meta.Element;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;

public class Manager extends Element {
	
	public static enum SayIntent { Greet, IllegalMove, WrongTurn, LostPiece, AtePiece,
		PlayAgain, Check, AcceptMigration, WonGame, LostGame, RecallDraw, RecallWon,
		RecallLost, Text }
	
	public static enum AnimationIntent { IllegalMove, WrongTurn, Winning, Losing,
		Draw, MigrationOut, MigrationIn, MoreExcited, Excited, LessExcited, GoodSurprise, 
		Think, BadSurprise, MoreUnhappy, Unhappy, LessUnhappy }
	
	private ExecuteMonitor execution;
	
	public Manager(ExecuteMonitor execution){
		this.execution = execution;
		
		this.getRequestHandlers().add(new SayManager());
		this.getRequestHandlers().add(new PlayManager());
		this.getRequestHandlers().add(new AnimationManager());
		this.getRequestHandlers().add(new GreetManager());
		this.getRequestHandlers().add(new AcceptMigrationManager());
		this.getRequestHandlers().add(new GameOverManager());
		this.getRequestHandlers().add(new MoodManager());
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
	}
	
	private class SayManager extends RequestHandler{

		protected SayManager() {
			super(new TypeSet(Say.class));
		}

		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			for (Say request : requests.get(Say.class)) {
				Say newRequest;

				if(request.intent.equals(SayIntent.Text)){
					newRequest = new Say(request.text);
				} else {
					newRequest = new Say(request.intent);
				}
				
				execution.schedule(newRequest);
			}
		}
		
	}
	
	private class PlayManager extends RequestHandler{

		protected PlayManager() {
			super(new TypeSet(MakeMove.class));
		}

		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			for (MakeMove request : requests.get(MakeMove.class)) {
				execution.schedule(request);
			}
		}
		
	}
	
	private class AnimationManager extends RequestHandler{

		protected AnimationManager() {
			super(new TypeSet(Animate.class));
		}

		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			for (Animate request : requests.get(Animate.class)) {
				execution.schedule(request);
			}
		}
		
	}

	private class MoodManager extends RequestHandler {

		public MoodManager() {
			super(new TypeSet(ChangeMood.class));
		}

		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			ChangeMood request = requests.get(ChangeMood.class).getFirst();
			
			execution.schedule(request);
		}
	}
	
	private class GreetManager extends RequestHandler{

		protected GreetManager() {
			super(new TypeSet(Greet.class, RecallGame.class));
		}

		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			ExecuteSequence sequence = new ExecuteSequence();
			
			if(!requests.get(Greet.class).isEmpty()){
				List<Request> step = new ArrayList<Request>();
				step.add(new Say(SayIntent.Greet));
				sequence.appendStep(step);
			}
			
			if(!requests.get(RecallGame.class).isEmpty()){
				PlayedGame game = requests.get(RecallGame.class).getFirst().game;
				
				if(game != null){
					List<Request> step = new ArrayList<Request>();
					switch (game.getResult()) {
						case Won:
							step.add(new Say(SayIntent.RecallWon));
							break;
						case Lost:
							step.add(new Say(SayIntent.RecallLost));
							break;
						case Drawn:
							step.add(new Say(SayIntent.RecallDraw));
							break;
						default:
							break;
					}
					
					sequence.appendStep(step);
				}
			}
			
			execution.schedule(sequence);
		}
		
	}

	private class AcceptMigrationManager extends RequestHandler {
		
		public AcceptMigrationManager() {
			super(new TypeSet(AcceptMigration.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			System.out.println("Manager accept migration");
			
			AcceptMigration request = requests.get(AcceptMigration.class).getFirst();
			
			ExecuteSequence sequence = new ExecuteSequence();
			List<Request> step = new ArrayList<Request>();
			
			step.add(new Say(SayIntent.AcceptMigration));
			step.add(new Animate(AnimationIntent.Winning));
			sequence.appendStep(step);
			
			step = new ArrayList<Request>();
			step.add(request.migrationRequest);
			sequence.appendStep(step);
			
			execution.schedule(sequence);
			System.out.println("end of manager migration");
		}
	}

	private class GameOverManager extends RequestHandler {

		public GameOverManager() {
			super(new TypeSet(RecognizeDefeat.class, AnnounceVictory.class, DeclareDraw.class));
		}

		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			ExecuteSequence sequence = new ExecuteSequence();
			List<Request> step = new ArrayList<Request>();
			
			if(!requests.get(AnnounceVictory.class).isEmpty()){
				step.add(new Say(SayIntent.WonGame));
				step.add(new Animate(AnimationIntent.Winning));
				sequence.appendStep(step);
			} else if (!requests.get(RecognizeDefeat.class).isEmpty()){
				step.add(new Say(SayIntent.LostGame));
				step.add(new Animate(AnimationIntent.Losing));
				sequence.appendStep(step);
			} else if (!requests.get(DeclareDraw.class).isEmpty()){
//				step.add(new Say(SayIntent.DrawGame));
				step.add(new Animate(AnimationIntent.Draw));
				sequence.appendStep(step);
			}
			
			execution.schedule(sequence);
		}
	}
}
