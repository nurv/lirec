package FAtiMA.memory.eventQuery;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Hashtable;

import FAtiMA.memory.ActionDetail;

public class CCQuery {
	
	private Hashtable<Integer, Float> _evaluations;
	private ActionDetail _actionDetail;
	private int _numField;
	
	private final PropertyChangeSupport changes  = new PropertyChangeSupport( this );
	 
	public CCQuery(){
		this._evaluations = new Hashtable<Integer, Float>();
		this._numField = 4;
	}
	
	public ActionDetail getActionDetail(){
    	return this._actionDetail;
    }
    
	public int getNumField(){
		return this._numField;
	}
	
    public Hashtable<Integer, Float> getEvaluation()
	{
    	return this._evaluations;
	}
    
	public void setQuery(ActionDetail actionDetail){
		
		this._actionDetail = actionDetail;
	}
	
    public void setEvaluation(int id, float evaluation){
    	if (this._evaluations == null || !this._evaluations.containsKey(id))
	  	{
	  		this._evaluations.put(id, evaluation);
	  	}
	  	/*else
	  	{	  		
	  		this._evaluations.put(id, evaluation);
	  	}*/
    }
    
	public void addPropertyChangeListener(final PropertyChangeListener l) {
        this.changes.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener(final PropertyChangeListener l) {
        this.changes.removePropertyChangeListener( l );
    }
}
