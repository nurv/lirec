/*
 * SactException.java
 *
 * Created on 31 August 2004, 16:49
 */

package Language;

/**
 * The exception type thrown when there is a problem with the agent's speech act.
 *
 * @author  Steve Grand
 */
@SuppressWarnings("serial")
public class SactException extends Exception {
    
    
    /** Creates a new instance of SactException */
    public SactException() {
         Log.Warning("UNKNOWN SACT EXCEPTION");
   }
    
    /**
     * Construct an exception passing a message back 
     * @param msg message
     */
    public SactException(String msg) {
          super(msg);
          Log.Warning("SACT EXCEPTION: " + msg);
    }

    /**
     * @param msg message
     * @param ex wrapped error/exception
     */
    public SactException(String msg, Throwable ex) {
          super(msg, ex);
          Log.Warning("SACT EXCEPTION: " + msg + " - " + ex.getMessage());
    }

    /**
     * @param ex wrapped error/exception
     */
    public SactException(Throwable ex) {
          super(ex);
          Log.Warning("SACT EXCEPTION: " + ex.getMessage());
    }

}

