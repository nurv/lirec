package eu.lirec.myfriend;

public class Piece {
	
	public static enum Colour {White, Black}
	public static enum Type { Pawn , Rook, Knight, Bishop, Queen, King }
	
	private Colour colour;
	private Type type;
	
	public Piece(Colour colour, Type type){
		this.colour = colour;
		this.type = type;
	}

	public Colour getColour() {
		return colour;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
