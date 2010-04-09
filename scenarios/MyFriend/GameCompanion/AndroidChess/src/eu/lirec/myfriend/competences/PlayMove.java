package eu.lirec.myfriend.competences;

import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;
import eu.lirec.myfriend.ChessBoard;
import eu.lirec.myfriend.events.Successful;
import eu.lirec.myfriend.requests.MakeMove;

public class PlayMove extends Competence {

	private ChessBoard board;
	
	public PlayMove(ChessBoard board){
		this.board = board;
		
		this.getRequestHandlers().add(new MoveHandler());
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
	}
	
	private class MoveHandler extends RequestHandler{
		
		public MoveHandler() {
			super(new TypeSet(MakeMove.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			for (MakeMove request : requests.get(MakeMove.class)) {
				board.schedule(request);
				raise(new Successful(request));
			}
		}
	}

}
