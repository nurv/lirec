package eu.lirec.myfriend;


public interface ChessEngineCallback {
	
	public void isMoveLegal(boolean isLegal, boolean outOfMoves);
	public void calculateMove(Location from, Location to);

}
