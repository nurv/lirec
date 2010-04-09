package eu.lirec.myfriend;

import ion.Meta.Element;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;

import java.util.Vector;

import cz.chess.mind.Minimax;
import cz.chess.rules.Position;
import eu.lirec.myfriend.Piece.Colour;
import eu.lirec.myfriend.Piece.Type;
import eu.lirec.myfriend.requests.IsMoveLegal;

public class ChessEngine extends Element {

	private Position position;
	
	public ChessEngine() {
		this.position = new Position();
		//this.getRequestHandlers().add(new HandleMoveCalculation());
		this.getRequestHandlers().add(new HandleMoveValidation());
	}
	
	public boolean isMoveLegal(ChessBoard board, Location from, Location to){
		updatePositions(board.toArray());
		position.white = board.nextPieceToMove() == Colour.White;
		Vector<Integer> legalMoves = position.findMoves();
		
		int fromCode = Position.a1 + from.x + 10 * from.y;
		int toCode = Position.a1 + to.x + 10 * to.y;
		
		return position.isThere2(legalMoves, fromCode, toCode);
	}
	
	public boolean isInCheck(ChessBoard board, Colour colour){
		updatePositions(board.toArray());
		return position.isInCheck(colour == Colour.White);
	}
	
	public void printLegalMoves(ChessBoard board){
		updatePositions(board.toArray());
		position.white = board.nextPieceToMove() == Colour.White;
		Vector<Integer> legalMoves = position.findMoves();
		
		for (Integer moveCode : legalMoves) {
			Move move = decodeMove(moveCode);
			System.out.println("("+move.getFrom().x+","+move.getFrom().y+") -> ("
					+move.getTo().x+","+move.getTo().y+")");
		}
	}
	
	public Move calculateMove(Piece[][] board, Colour pieceToMove){
		updatePositions(board);
		position.white = pieceToMove == Colour.White;
		
		/*
		Thread t = new ThinkingThread(callback);
		t.start();
		*/
		
		position.findMoves();
		int[] result = Minimax.minimax(position, 2000);
		int move = result[0];
		
		return decodeMove(move);
	}
	
	public Colour checkWinner(ChessBoard board){
		
		if(!gameEnded(board)){
			return null;
		}
		
		updatePositions(board.toArray());
		position.white = board.nextPieceToMove() == Colour.White;
		
		if(position.isInCheck(true)){
			return Colour.Black;
		} else if(position.isInCheck(false))  {
			return Colour.White;
		} else {
			return null;
		}
	}
	
	public boolean gameEnded(ChessBoard board){
		updatePositions(board.toArray());
		position.white = board.nextPieceToMove() == Colour.White;
		Vector<Integer> legalMoves = position.findMoves();
		
		return legalMoves.isEmpty();
	}
	
	private void updatePositions(Piece[][] board){
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				int index = Position.a1 + i + j*10;
				this.position.sch[index] = convertPiece(board[i][j]);
			}
		}
	}
	
	private Move decodeMove(int move){
	    int to;
	    int from;
	    
	    if(move == 0){
	    	return null;
	    }
		
		if ((move>>15) == 0){
			from = move>>7;
			to = move&127;
		    
		    return new Move(decodeLocation(from), decodeLocation(to));
		}
	    
		if ((move>>12)==12){
			from = (Position.a7+((move>>7)&7));
			to = (Position.a8+((move>>4)&7));
			byte piece = (byte)(2+((move>>10)&3));
			
			return new Move(decodeLocation(from), decodeLocation(to), decodePiece(piece));
		}
		
		if ((move >> 12) == 13) {
			from = (byte) (Position.a2 + ((move >> 7) & 7));
			to = (byte) (Position.a1 + ((move >> 4) & 7));
			byte piece = (byte) (-(2 + ((move >> 10) & 3)));
			
			return new Move(decodeLocation(from), decodeLocation(to), decodePiece(piece));
		}
	
		return null;
	}
	
	private Location decodeLocation(int coords){
		int x = (coords - Position.a1) % 10;
		int y = (coords - Position.a1) / 10;
		
		return new Location(x, y);
	}
	
	private Piece decodePiece(byte piece){

		Colour colour = Colour.White;
		
		if(piece < 0){
			colour = Colour.Black;
			piece *= -1;
		}
		
		switch(piece){
			case 1:
				return new Piece(colour, Type.Pawn);
			case 2:
				return new Piece(colour, Type.Knight);
			case 3:
				return new Piece(colour, Type.Bishop);
			case 4:
				return new Piece(colour, Type.Rook);
			case 5:
				return new Piece(colour, Type.Queen);
			case 6:
				return new Piece(colour, Type.King);
				
			default:
				return null;
		}
	}
	
	private byte convertPiece(Piece piece){
		if (piece == null) {
			return 0;
		}

		byte pieceCode;

		switch (piece.getType()) {
		case Pawn:
			pieceCode = 1;
			break;

		case Knight:
			pieceCode = 2;
			break;

		case Bishop:
			pieceCode = 3;
			break;

		case Rook:
			pieceCode = 4;
			break;

		case Queen:
			pieceCode = 5;
			break;

		case King:
			pieceCode = 6;
			break;

		default:
			return 0;
		}
		
		switch (piece.getColour()) {
		case White:
			return pieceCode;

		case Black:
			return (byte) (pieceCode * -1);

		default:
			return 0;
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
	}
	
	private class HandleMoveValidation extends RequestHandler{
		
		public HandleMoveValidation() {
			super(new TypeSet(IsMoveLegal.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			for (IsMoveLegal request : requests.get(IsMoveLegal.class)) {
				updatePositions(request.board.toArray());
				position.white = request.board.nextPieceToMove() == Colour.White;
				Vector<Integer> legalMoves = position.findMoves();
				
				int from = Position.a1 + request.from.x + 10 * request.from.y;
				int to = Position.a1 + request.to.x + 10 * request.to.y;
				
				boolean isMoveLegal = position.isThere2(legalMoves, from, to);
				request.callback.isMoveLegal(isMoveLegal, legalMoves.isEmpty());
			}
		}
		
	}
	
	/*
	private class HandleMoveCalculation extends RequestHandler{
		
		public HandleMoveCalculation() {
			super(new TypeSet(CalculateMove.class));
		}

		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			for (CalculateMove request : requests.get(CalculateMove.class)) {
				updatePositions(request.board);
				position.white = request.pieceToMove == Colour.White;
				
				Thread t = new ThinkingThread(request.callback);
				t.start();
			}
		}
	}*/
	
	/*
	private class ThinkingThread extends Thread{
		
		private ChessEngineCallback callback;
		
		public ThinkingThread(ChessEngineCallback callback) {
			this.callback = callback;
		}
		
		@Override
		public void run() {
			position.findMoves();
			final int move;
			move = Minimax.minimax(position, 5000);
			
			if(move != 0){
				int to = move&127;
			    int from = move>>7;
			    int fromX = (from - Position.a1) % 10;
			    int fromY = (from - Position.a1) / 10;
			    int toX = (to - Position.a1) % 10;
			    int toY = (to - Position.a1) / 10;
			    
				this.callback.calculateMove(new Location(fromX, fromY), new Location(toX, toY));
			} else {
				this.callback.calculateMove(null, null);
			}
		}
	}
	*/

}
