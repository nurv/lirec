/*
 * Context.java
 *
 * Created on 22 September 2004, 18:15
 * @author  Steve Grand
 */

package Language;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;


/**
 * Holds either the global or agent-specific context information for the conversation.
 * The context information enables the agents to know
 * useful facts about the conversation, especially regarding user input but also to store the values
 * of synonyms.
 * Each Context object holds a list of String pairs, consisting of a context variable name 
 * and its associated value.
 * <p>
 * 
 * Each LanguageEngine has a Context object, pre-loaded with strings
 * reflecting instance-related context variables (such as the person being spoken to).
 * There is also one static LanguageEngine.globalContext object, whose
 * context variable names reflect information relevant to the conversation
 * at large, for example the topic, or the person being spoken about as a 
 * third party, and also the synonym values.
 * <p>
 * Except for synonyms, context variables can be referenced in the template files by types of their 
 * name, with the value in a value slot, e.g.:
 * <br>
 * bully(value:Luke) <br>
 * The name of the context variable has to be defined in the spin type ontology (types.xml):<br>
 * &lt;class name="bully" extends="globalContext"/>
 * <p>
 * Synonyms don't need to be defined in the type ontology, but they have to be used (for referencing
 * and setting) in a type called "Synonym", where the semCat slot holds the name of the synonym and
 * the value slot holds the value of the synonym, e.g.:<br>
 * Synonym(semCat:idiot,value:nerd)
 * 
 * @author Steve Grand
 * @author Thurid Vogt
 */
class Context
{
    /** The keyword/value pairs.  */
    private HashMap variable = new HashMap();
    
    /** Creates a new instance of Context */
    public Context()
    {
    }
  
    /**
     * Replace this context with new information. 
     * If the variable doesn't already exist, add it to the list.
     * 
     * @param keyword The name of the variable (me, you, topic, idiot...)
     * @param value The new context
     */
   @SuppressWarnings("unchecked")
public synchronized void Add(String keyword, String value)
    {
        Log.Detail("        Adding context: " + keyword + " = " + value);
        variable.put(keyword.toLowerCase(), value);
    }
   
   
   
   /**
    * Remove this context variable from the list.
    * 
    * @param keyword The name of the variable (me, you, topic, idiot...)
    */
  public synchronized void Remove(String keyword) {
	   if (variable.containsKey(keyword.toLowerCase())) {
		   Log.Detail("        Removing context: " + keyword + " with value " + variable.get(keyword));
		   variable.remove(keyword.toLowerCase());
	   }
	   else 
		   Log.Detail("No context removal operation performed. Context "+keyword+" was not there anyway.");
   }
    
  
   /** 
    * Replace this context with new information.
    * If the keyword is not yet in the context variable list, return false.
    * Use this when agents try to send context information, because
    * unrecognised keywords signify an error on the agent's part.
    * 
    * @param keyword the name of the context variable
    * @param value the new value of the context variable
    * @return true if keyword exists and value is successfully changed <br> 
    * false otherwise
    */
   @SuppressWarnings("unchecked")
public synchronized boolean Set(String keyword, String value)
   {
       keyword = keyword.toLowerCase();
       if (variable.containsKey(keyword))
       {
           Log.Detail("        Setting context: " + keyword + " = " + value);
           variable.put(keyword, value);
           return true;
       }
       else
           return false;
   }
   
   /**
    * Static method to set a variable in one of two contexts.
    * The variable is set in the first context that contains a variable of that name. 
    * The contexts will normally be the global and the local context of a language engine object.
    * 
    * @param keyword The context variable
    * @param value The new value of the context variable
    * @param global The first (global) context
    * @param local The second (local) context
    * @return true if the variable was found in one of the contexts and was successfully set<br>
    * false if the variable was not found in either context.
    */ 
   public static boolean Set(String keyword, String value, Context global, Context local)
   {
        boolean success = global.Set(keyword, value);
        if (success==false)
            success = local.Set(keyword, value);
        return success;
   }
   
   /**
    * Fetch the current value of the given context key.
    * If the variable doesn't already exist, add it to the list and set it with the defaultValue
    * 
    * @param keyword The context variable
    * @param defaultValue The value to set if the variable does not already exist
    * @return The value of the context variable, or the defaultValue, if the variable didn't 
    * exist before
    */
   @SuppressWarnings("unchecked")
public String Get(String keyword, String defaultValue)
   {
       keyword = keyword.toLowerCase();
       String value = (String)variable.get(keyword);
       if (value==null)
       {
           variable.put(keyword, defaultValue);
           value = defaultValue;
       }
       return value;
   }
    
   
   /**
    * Fetch the current value of the given context key.
    * 
    * @param keyword The context variable
    * @return The value of the context variable<br>
    *  Null if this keyword not found
    */
   public String Get(String keyword)
   {
       return (String)variable.get(keyword.toLowerCase());
   }
   
   
   /**
    * get all names of context variables in this Context object.
    * 
    * @return String array of all names; null if Context is empty
    * 
    */
   //author Thurid 31.08.2006
   public String[] getKeys() {
	   Object[] keysObject = variable.keySet().toArray();
	   String[] keys = new String[keysObject.length];
	   System.arraycopy(keysObject, 0, keys, 0, keys.length);

	   return keys;
   }
   
   
   /**
    * print the current set of name/value pairs to the log for debugging.
    */
   public void Print()
   {
       Set set = variable.keySet();
       for (Iterator it = set.iterator(); it.hasNext(); ) 
       {
           String name = (String)it.next();
           Log.Detail(name + " = " + variable.get(name));
       }
   }
    
}
