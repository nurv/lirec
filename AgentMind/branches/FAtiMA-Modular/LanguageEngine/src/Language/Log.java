/*
 * Log.java
 * @author  Steve
 * Created on 28 September 2004, 19:24
 */

package Language;

import java.util.logging.*;


/**
 * Static logger class
 * Currently set up to log to a text file - language_log.txt
 * Can be used for debug logs and also to record a conversation
 * 
 * @author Steve Grand
 */
class Log
{
	/**
	 * The logger from {@link java.util.logging.Logger}.
	 */
    private static Logger log;
    
    /**
     * The file handler of the output file.
     */
    private static FileHandler fh;
    static
    {
        try
        {
            log = Logger.getLogger("VICTEC.Language");
            fh = new FileHandler("language_log.txt");
            // We'll have the results in plain text not xml
            fh.setFormatter(new MyFormatter());
            // Send logger output to our FileHandler.
            log.addHandler(fh);
            log.setUseParentHandlers(true);
            // Log messages >= this level:
            // Level.OFF  switch off logging
            // Level.SEVERE - errors
            // Level.WARNING - recoverable problems
            // Level.FINE - only the conversation and warnings/errors
            // Level.FINER - key information about language processing
            // Level.FINEST - detailed information about language processing
            // Level.ALL - all messages
            log.setLevel(Level.FINE);
        }
        catch (Exception e)
        {
        }
    }
    
    /**
     * This is a static-only class, so the constructor is empty
     */
    private Log() { }
    
    
    /**
     * Log fine details of the language process
     */
    public static void Detail(String msg)
    {
        log.log(Level.FINEST, msg);
    }
    
    /**
     * Log main events in the language process
     */
    public static void Info(String msg)
    {
        log.log(Level.FINER, msg);
    }
    
    /**
     * Log only the conversation itself, as a record of who said what and when
     */
    public static void Conversation(String msg)
    {
        log.log(Level.FINE, msg);
    }
    
    /**
     * Log warnings and errors
     */
    public static void Warning(String msg)
    {
        log.log(Level.WARNING, msg);
    }
    
    /**
     * Set logging level at runtime
     */
    public static void SetLevel(Level level)
    {
       log.setLevel(level);
    }
     
}

/**
 * Formatter class for simpler and more readable logs (skipping the date and time)
 */
class MyFormatter extends Formatter
{
    /**
     * format logging message with an ending newline/carriage return
     * 
     * @param record the original logging message
     * @return the textual output of the logging message
     */
    public String format(LogRecord record)
    {
        return (formatMessage(record) + "\r\n");
    }
    
}
