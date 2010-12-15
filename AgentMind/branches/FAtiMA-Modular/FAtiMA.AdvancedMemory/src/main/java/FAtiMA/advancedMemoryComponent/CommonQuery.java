package FAtiMA.advancedMemoryComponent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;


public class CommonQuery implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int _maxNumMatch;
	private Hashtable<ArrayList<Integer>, Hashtable<String, String>> _match;

	private final PropertyChangeSupport changes  = new PropertyChangeSupport( this );
	 
	public CommonQuery(){
		this._maxNumMatch = 0;	
		this._match = new Hashtable<ArrayList<Integer>, Hashtable<String, String>>();		
	}
	
	public void setMaxNumMatch(int numMatch)
	{
		this._maxNumMatch = numMatch;
	}
	
	public int getMaxNumMatch()
	{
		return this._maxNumMatch;
	}
    
    public Hashtable<ArrayList<Integer>, Hashtable<String, String>> getMatch()
    {
    	return this._match;
    }
    
    public void setMatch(ArrayList<Integer> ids, Hashtable<String, String> match)
    {	
    	this._match.put(ids, match);
    }
    
	public void addPropertyChangeListener(final PropertyChangeListener l) {
        this.changes.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener(final PropertyChangeListener l) {
        this.changes.removePropertyChangeListener( l );
    }
}
