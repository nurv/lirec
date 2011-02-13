package FAtiMA.culture.exceptions;

public class DuplicateSymbolTranslatorEntry extends Exception {
    
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateSymbolTranslatorEntry() {
         super("A symbol can only be associated to a single action and vice-versa");
    }
    
    /**
     * Construct an exception passing a message back 
     * @param msg message
     */
    public DuplicateSymbolTranslatorEntry(String msg) {
          super(msg);
    }

    /**
     * @param msg message
     * @param ex wrapped error/exception
     */
    public DuplicateSymbolTranslatorEntry(String msg, Throwable ex) {
          super(msg, ex);
    }

    /**
     * @param ex wrapped error/exception
     */
    public DuplicateSymbolTranslatorEntry(Throwable ex) {
          super(ex);
    }
}