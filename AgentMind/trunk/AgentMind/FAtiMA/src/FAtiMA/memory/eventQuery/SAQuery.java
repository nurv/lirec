package FAtiMA.memory.eventQuery;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import FAtiMA.memory.Time;
import java.util.ArrayList;
import java.util.Hashtable;

public class SAQuery {

	private String _question;
	private String _subject;
	private String _target;
	private String _action;
	private String _location;
	
	private int _numKnownVar;
	private Hashtable<String, Integer> _answers;
	
	private final PropertyChangeSupport changes  = new PropertyChangeSupport( this );
	 
	public SAQuery(){
		this._question = "";
		this._subject = "";
		this._target = "";
		this._action = "";
		this._location = "";
		this._numKnownVar = 0;
		
		this._answers = new Hashtable<String, Integer>();
	}
	    
	public void setQuery( ArrayList<String> info, String question ) {
		String[] knownInfo;
		String known;		
		
		for (int i = 0; i < info.size(); i++)
		{
			known = (String) info.get(i);
			knownInfo = known.split(" ");
			if (knownInfo[0].equals("subject"))
			{
				this._subject = knownInfo[1];
				this._numKnownVar++;
			}
			if (knownInfo[0].equals("target"))
			{
				this._target = knownInfo[1];
				this._numKnownVar++;
			}
			if (knownInfo[0].equals("action"))
			{
				this._action = knownInfo[1];
				this._numKnownVar++;
			}
			if (knownInfo[0].equals("location"))
			{
				this._location = knownInfo[1];	
				this._numKnownVar++;
			}
		}
		System.out.println("subject " + _subject + " target " + _target + " action " + _action + " location " + _location);
		_question = question;		
	}
	 
    public String getSubject(){
    	return this._subject;
    }
    
    public String getTarget(){
    	return this._target;
    }
    
    public String getAction(){
    	return this._action;
    }
    
    public String getLocation(){
    	return this._location;
    }
    
    public int getNumKnownVar(){
    	return this._numKnownVar;
    }
    
    public String getQuestion(){
    	return this._question;
    }
    
    public Hashtable<String, Integer> getAnswers()
	{
    	return this._answers;
	}
    
    public void setSubject(String subject){
    	this._subject = subject;
    }
    
    public void setTarget(String target){
    	this._target = target;
    }
    
    public void setAction(String action){
    	this._action = action;
    }
    
    public void setLocation(String location){
    	this._location = location;
    }
    
    public void setQuestion(String question){
    	this._question = question;
    }
	
    public void setAnswers(String answer)
	{
	  	if (this._answers == null || !this._answers.containsKey(answer))
	  	{
	  		this._answers.put(answer, new Integer(1));
	  	}
	  	else
	  	{
	  		Integer val = (Integer) _answers.get(answer);
	  		this._answers.put(answer, ++val);
	  	}
	}
    
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        this.changes.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener(final PropertyChangeListener l) {
        this.changes.removePropertyChangeListener( l );
    }
}
