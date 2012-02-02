/*
 * LactException.java
 *
 * Created on 14 October 2004, 17:12
 */

package Language;

/**
 * The exception type thrown when there is a problem with a 
 * language act database.
 * Is currently never thrown by the language engine, just there for compatibility.
 *
 * @author  Steve Grand
 */
@SuppressWarnings("serial")
public class LactException extends Exception {
    
  
     
    
    /** Blank LactException */
    public LactException() {
          Log.Warning("LACT EXCEPTION");
   }
    
    /**
     * Construct an exception passing a message back 
     * 
     * @param msg message
     */
    public LactException(String msg) {
          super(msg);
          Log.Warning("LACT EXCEPTION: " + msg);
    }

    /**
     * Construct an exception passing a message back, overriding original exception.
     * 
     * @param msg message
     * @param ex overridden error/exception
     */
    public LactException(String msg, Throwable ex) {
          super(msg, ex);
          Log.Warning("LACT EXCEPTION: " + msg + " - " + ex.getMessage());
    }

    /**
     * Construct an exception overriding original exception.
     * 
     * @param ex overridden error/excetpion
     */
    public LactException(Throwable ex) {
          super(ex);
          Log.Warning("LACT EXCEPTION: " + ex.getMessage());
    }
  
    
   
    
}
