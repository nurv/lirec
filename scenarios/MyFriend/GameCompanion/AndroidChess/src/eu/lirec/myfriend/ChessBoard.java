package eu.lirec.myfriend;

import java.util.ArrayList;
import java.util.List;

import org.kxml2.kdom.Node;

import eu.lirec.myfriend.Piece.Colour;
import eu.lirec.myfriend.Piece.Type;
import eu.lirec.myfriend.competences.Migration;
import eu.lirec.myfriend.events.BoardChanged;
import eu.lirec.myfriend.events.GameOver;
import eu.lirec.myfriend.events.GameStarted;
import eu.lirec.myfriend.events.MigrationStart;
import eu.lirec.myfriend.events.MovePlayed;
import eu.lirec.myfriend.events.MoveUndone;
import eu.lirec.myfriend.events.PieceEaten;
import eu.lirec.myfriend.events.UndoAvailable;
import eu.lirec.myfriend.events.UndoUnavailable;
import eu.lirec.myfriend.requests.EndGame;
import eu.lirec.myfriend.requests.MakeMove;
import eu.lirec.myfriend.requests.StartGame;
import eu.lirec.myfriend.requests.UndoMove;
import eu.lirec.myfriend.requests.UserUndo;
import eu.lirec.myfriend.synchronization.events.MessageReceived;
import ion.Meta.Element;
import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;

public class ChessBoard extends Element implements Cloneable{
	
	private List<ChessBoard> oldStates;
	private Piece[][] board;
	private Colour piecesToMove;
	private boolean gameEnded;
	private Migration migration;
	private OnMigrationOut onMigrationOut;
	private OnMigrationIn onMigrationIn;
	private final int historySize = 2;

	public ChessBoard(){
		board = new Piece[8][8];
		piecesToMove = Colour.White;
		getRequestHandlers().add(new MoveHandler());
		getRequestHandlers().add(new GameStateHandler());
		gameEnded = false;
		oldStates = new ArrayList<ChessBoard>(historySize);
		onMigrationOut = new OnMigrationOut();
		onMigrationIn = new OnMigrationIn();
		setupNewGame();
	}
	
	private void setupNewGame(){
		piecesToMove = Colour.White;
		clearHistory();
		
		board[0][0] = new Piece(Colour.White, Type.Rook);
		board[1][0] = new Piece(Colour.White, Type.Knight);
		board[2][0] = new Piece(Colour.White, Type.Bishop);
		board[3][0] = new Piece(Colour.White, Type.Queen);
		board[4][0] = new Piece(Colour.White, Type.King);
		board[5][0] = new Piece(Colour.White, Type.Bishop);
		board[6][0] = new Piece(Colour.White, Type.Knight);
		board[7][0] = new Piece(Colour.White, Type.Rook);
		
		for (int i = 0; i < 8; i++) {
			board[i][1] = new Piece(Colour.White, Type.Pawn);
		}
		
		board[0][7] = new Piece(Colour.Black, Type.Rook);
		board[1][7] = new Piece(Colour.Black, Type.Knight);
		board[2][7] = new Piece(Colour.Black, Type.Bishop);
		board[3][7] = new Piece(Colour.Black, Type.Queen);
		board[4][7] = new Piece(Colour.Black, Type.King);
		board[5][7] = new Piece(Colour.Black, Type.Bishop);
		board[6][7] = new Piece(Colour.Black, Type.Knight);
		board[7][7] = new Piece(Colour.Black, Type.Rook);
		
		for (int i = 0; i < 8; i++) {
			board[i][6] = new Piece(Colour.Black, Type.Pawn);
		}
		
		for (int i = 0; i < board.length; i++) {
			for (int j = 2; j < 6; j++) {
				board[i][j] = null;
			}
		}
	}
	
	public void setupFakeGame(){
		
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < 8; j++) {
				board[i][j] = null;
			}
		}
		
		board[0][0] = new Piece(Colour.White, Type.Rook);
		board[0][1] = new Piece(Colour.White, Type.Pawn);
		board[0][2] = new Piece(Colour.White, Type.Bishop);
		board[1][3] = new Piece(Colour.White, Type.Pawn);
		board[2][1] = new Piece(Colour.White, Type.Pawn);
		board[2][2] = new Piece(Colour.White, Type.King);
		board[3][2] = new Piece(Colour.White, Type.Pawn);
		board[4][1] = new Piece(Colour.White, Type.Knight);
		
		board[0][6] = new Piece(Colour.Black, Type.Pawn);
		board[0][7] = new Piece(Colour.Black, Type.Rook);
		board[2][6] = new Piece(Colour.Black, Type.Pawn);
		board[3][6] = new Piece(Colour.Black, Type.King);
		board[4][6] = new Piece(Colour.Black, Type.Knight);
		board[5][5] = new Piece(Colour.Black, Type.Pawn);
		board[5][6] = new Piece(Colour.White, Type.Queen);
		board[5][7] = new Piece(Colour.Black, Type.Bishop);
		board[6][4] = new Piece(Colour.Black, Type.Pawn);
		board[7][6] = new Piece(Colour.Black, Type.Pawn);
		board[7][7] = new Piece(Colour.Black, Type.Rook);
		
		
	}
	
	public Piece getPieceAt(Location location){
		return board[location.x][location.y];
	}
	
	public Piece getPieceAt(int x, int y){
		return board[x][y];
	}
	
	private void setPieceAt(Piece piece, Location location){
		board[location.x][location.y] = piece;
	}
	
	private void updateHistory(){
		if(oldStates.isEmpty()){
			raise(new UndoAvailable());
		}
		
		if(oldStates.size() == historySize){
			oldStates.remove(0);
		}
		
		oldStates.add(this.clone());
	}
	
	private boolean revertHistory(int moves){
		
		if(oldStates.isEmpty() || moves > oldStates.size()){
			return false;
		}
		
		ChessBoard revertState = null;
		for(int i=0 ; i<moves ; i++){
			revertState = oldStates.remove(oldStates.size()-1);
		}
		
		board = revertState.toArray();
		piecesToMove = revertState.piecesToMove;
		
		if(oldStates.isEmpty()){
			raise(new UndoUnavailable());
		}
		
		return true;
	}
	
	public void clearHistory(){
		oldStates.clear();
		raise(new UndoUnavailable());
	}
	
	public void changeTurns(){
		if(piecesToMove == Colour.White){
			piecesToMove = Colour.Black;
		}else {
			piecesToMove = Colour.White;
		}
	}
	
	public Colour nextPieceToMove(){
		return this.piecesToMove;
	}
	
	public boolean isGameFinished(){
		return gameEnded;
	}
	
	private Piece movePiece(Location from, Location to){
		Piece movingPiece = getPieceAt(from);
		setPieceAt(null, from);
		Piece eatenPiece = getPieceAt(to);
		setPieceAt(movingPiece, to);
		
		return eatenPiece;
	}
	
	public Piece[][] toArray(){
		Piece[][] boardcopy = new Piece[this.board.length][];
		
		for (int i = 0; i < this.board.length; i++) {
			boardcopy[i] = new Piece[this.board[i].length];
			
			for (int j = 0; j < this.board.length; j++) {
				if(this.board[i][j] != null){
					boardcopy[i][j] = new Piece(this.board[i][j].getColour(), this.board[i][j].getType());
				} else {
					boardcopy[i][j] = null;
				}
			}
		}
		
		return boardcopy;
	}
	
	@Override
	public ChessBoard clone(){
		ChessBoard board = null;
		try {
			board = (ChessBoard) super.clone();
		} catch (CloneNotSupportedException e) {
		}
		board.board = this.toArray();
		return board;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
	}
	
	public void setMigrationCompetence(Migration migration){
		if(this.migration != null){
			this.migration.getEventHandlers().remove(onMigrationOut);
			this.migration.getEventHandlers().remove(onMigrationIn);
		}
		
		this.migration = migration;
		if(this.migration != null){
			this.migration.getEventHandlers().add(onMigrationOut);
			this.migration.getEventHandlers().add(onMigrationIn);
		}
	}
	
	private Piece[][] xmlBoardToArray(org.kxml2.kdom.Element boardState){
		
		Piece[][] board = new Piece[8][8];
		
		for (int i=0; i < boardState.getChildCount(); i++) {
			org.kxml2.kdom.Element xmlPiece = boardState.getElement(i);
			
			if(xmlPiece != null && xmlPiece.getName().equals("piece")){
				Colour colour;
				Type type;
				Piece piece;
				int coordX;
				int coordY;
				
				colour = Colour.valueOf(xmlPiece.getAttributeValue("", "colour"));
				type = Type.valueOf(xmlPiece.getAttributeValue("", "type"));
				piece = new Piece(colour, type);
				
				coordX = Integer.parseInt(xmlPiece.getAttributeValue("", "positionh"));
				coordY = Integer.parseInt(xmlPiece.getAttributeValue("", "positionv"));
				board[coordX][coordY] = piece;
			}
		}
		
		return board;
	}
	
	private class MoveHandler extends RequestHandler{

		protected MoveHandler() {
			super(new TypeSet(MakeMove.class, UndoMove.class, UserUndo.class));
		}

		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {

			if(gameEnded){
				return;
			}
			
			for (UserUndo request : requests.get(UserUndo.class)) {
				
				ChessBoard oldBoard = ChessBoard.this.clone();

				if(oldStates.isEmpty()){
					return;
				}
				
				revertHistory(request.moves);
				
				System.out.println("Move Undone by user");
				raise(new MoveUndone(ChessBoard.this, oldBoard));
				return;
			}
			
			for (UndoMove request : requests.get(UndoMove.class)) {
				
				revertHistory(1);
				ChessBoard oldBoard = ChessBoard.this.clone();
				
				if(request.from == null || request.to == null){
					board = request.oldBoard.toArray();
					piecesToMove = request.oldBoard.piecesToMove;
				} else {
					movePiece(request.from, request.to);
					piecesToMove = request.oldTurn;
					//TODO Restore eaten piece if the case.
				}
				
				System.out.println("Move Undone");
				raise(new BoardChanged(ChessBoard.this, oldBoard));
				return;
			}
			
			for (MakeMove request : requests.get(MakeMove.class)) {
				
				//TODO Remove debug print
				System.out.println("Move: ("+request.from.x+","+request.from.y+") -> ("+
						request.to.x+","+request.to.y+")");
				
				if(getPieceAt(request.from) != null){
					Piece pieceEaten;
					ChessBoard oldBoard = ChessBoard.this.clone();
					
					updateHistory();
					pieceEaten = movePiece(request.from, request.to);
					
					if(request.replacement != null){
						setPieceAt(request.replacement, request.to);
					}
					
					changeTurns();
					if(pieceEaten != null){
						raise(new PieceEaten(pieceEaten, request.from, request.to, ChessBoard.this, oldBoard));
					} else {
						raise(new MovePlayed(request.from, request.to, ChessBoard.this, oldBoard));
					}
				}
			}
		}
	}
	
	private class GameStateHandler extends RequestHandler{
		
		public GameStateHandler() {
			super(new TypeSet(EndGame.class, StartGame.class));
		}

		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {

			for(EndGame request : requests.get(EndGame.class)) {
				System.out.println("Game has ended.");
				gameEnded = true;
				raise(new GameOver(request.winner));
				break;
			}
			
			for(StartGame request : requests.get(StartGame.class)) {
				System.out.println("Game has started.");
				gameEnded = false;
				ChessBoard old = ChessBoard.this.clone();
				setupNewGame();
				raise(new GameStarted());
				raise(new BoardChanged(ChessBoard.this, old));
				break;
			}
		}
	}

	private class OnMigrationOut extends EventHandler{
		
		public OnMigrationOut() {
			super(MigrationStart.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			org.kxml2.kdom.Element boardState;
			org.kxml2.kdom.Element nextMove;
			
			nextMove = ((MigrationStart) evt).newElement("", "nextmove");
			nextMove.setAttribute("", "colour", piecesToMove.name());
			migration.addMigrationData(nextMove);
			
			boardState = ((MigrationStart) evt).newElement("", "boardstate");
			
			for (int i = 0; i < board.length; i++) {
				for (int j = 0; j < board[i].length; j++) {
					Piece piece = board[i][j];
					
					if(piece != null){
						org.kxml2.kdom.Element pieceElement = boardState.createElement("", "piece");
						
						pieceElement.setAttribute("", "type", piece.getType().toString());
						pieceElement.setAttribute("", "colour", piece.getColour().toString());
						pieceElement.setAttribute("", "positionh", Integer.toString(i));
						pieceElement.setAttribute("", "positionv", Integer.toString(j));
						
						boardState.addChild(Node.ELEMENT, pieceElement);
					}
				}
			}
			migration.addMigrationData(boardState);
		}
	}
	
	private class OnMigrationIn extends EventHandler {
		
		public OnMigrationIn() {
			super(MessageReceived.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			MessageReceived messageEvent = (MessageReceived) evt;
			
			if(messageEvent.type.equals("boardstate")){
				ChessBoard oldBoard = ChessBoard.this.clone();
				board = xmlBoardToArray(messageEvent.message);
				raise(new BoardChanged(ChessBoard.this, oldBoard));
			}
			
			if(messageEvent.type.equals("nextmove")){
				piecesToMove = Colour.valueOf(messageEvent.message.getAttributeValue("", "colour"));
			}
		}
	}
}
