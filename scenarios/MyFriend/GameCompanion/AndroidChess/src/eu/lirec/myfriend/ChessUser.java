package eu.lirec.myfriend;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import eu.lirec.myfriend.Piece.Colour;
import eu.lirec.myfriend.Piece.Type;
import eu.lirec.myfriend.requests.ForfeitGame;
import eu.lirec.myfriend.requests.MakeMove;
import eu.lirec.myfriend.requests.ProposeNewGame;

public class ChessUser extends ChessPlayer implements OnClickListener {
	
	private String name;
	private long id;
	private Activity activity;
	private Location from;
	private Location to;
	
	public ChessUser(ChessBoard board, Activity activity){
		super(board);
		this.name = "Player";
		this.activity = activity;
	}
	
	public void makeMove(Location from, Location to){
		
		if(to.y == 7 && board.getPieceAt(from).getType() == Type.Pawn){
			this.from = from;
			this.to = to;
			activity.showDialog(MyFriendChessGame.PIECE_DIALOG);
		} else {
			board.schedule(new MakeMove(from, to));
		}
	}
	
	public void proposeNewGame(){
		opponent.schedule(new ProposeNewGame());
	}
	
	public void forfeitGame(){
		opponent.schedule(new ForfeitGame());
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		String typeName = MyFriendChessGame.pieceList[which].toString();
		Type pieceType = Type.valueOf(typeName);
		board.schedule(new MakeMove(from, to, new Piece(Colour.White, pieceType)));
	}
}
