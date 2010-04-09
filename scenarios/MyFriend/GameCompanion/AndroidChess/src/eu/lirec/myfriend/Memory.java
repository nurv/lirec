package eu.lirec.myfriend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class Memory {

	public final String userName;
	private List<PlayedGame> games;
	private int played;
	private int won;
	private int lost;
	private int drawn;
	
	public Memory(String userName){
		this.userName = userName;
		this.games = new ArrayList<PlayedGame>();
	}
	
	public void addGame(PlayedGame game){
		games.add(game);
		played++;
		
		switch (game.getResult()) {
		case Won:
			won++;
			break;
		case Lost:
			lost++;
			break;
		case Drawn:
			drawn++;
			break;
		}
	}
	
	public PlayedGame getLastGame(){
		
		try {
			return Collections.max(games);
		} catch (NoSuchElementException e){
		}
		
		return null;
	}
	
	public List<PlayedGame> getGames(){
		return Collections.unmodifiableList(games);
	}
	
}
