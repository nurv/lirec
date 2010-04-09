package eu.lirec.myfriend;

import java.util.Date;

public class PlayedGame implements Comparable<PlayedGame> {
	
	public static enum Result {Won, Lost, Drawn}
	
	private Date begin;
	private Date end;
	private Result result;
	
	public PlayedGame(Date begin, Date end, Result result){
		this.begin = begin;
		this.end = end;
		this.result = result;
	}
	
	public Date getBegin(){
		return begin;
	}
	
	public Date getEnd(){
		return end;
	}
	
	public Result getResult(){
		return result;
	}

	@Override
	public int compareTo(PlayedGame another) {
		return end.compareTo(another.end);
	}

}
