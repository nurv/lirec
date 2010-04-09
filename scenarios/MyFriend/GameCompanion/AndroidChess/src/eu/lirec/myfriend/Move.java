package eu.lirec.myfriend;

public class Move {

	public enum MoveType {Normal, Rook, PawnReplace }
	
	private Location from;
	private Location to;
	private MoveType type;
	private int heuristic;
	private Piece replacement;
	
	public Move(Location from, Location to) {
		this.from = from;
		this.to = to;
		this.type = MoveType.Normal;
	}
	
	public Move(Location from, Location to, Piece piece){
		this.from = from;
		this.to = to;
		this.type = MoveType.PawnReplace;
		this.replacement = piece;
	}

	public Location getFrom() {
		return from;
	}

	public void setFrom(Location from) {
		this.from = from;
	}

	public Location getTo() {
		return to;
	}

	public void setTo(Location to) {
		this.to = to;
	}

	public int getHeuristic() {
		return heuristic;
	}

	public void setHeuristic(int heuristic) {
		this.heuristic = heuristic;
	}
	
	public MoveType getType(){
		return this.type;
	}
	
	public Piece getReplacementPiece(){
		return this.replacement;
	}
	
	public void setReplacementPiece(Piece piece){
		this.replacement = piece;
		this.type = MoveType.PawnReplace;
	}
}
