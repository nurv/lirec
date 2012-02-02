/*
 * LanguageEngine.java
 * Created on 31 August 2004, 16:30
 */

/* 
 * TODO:
 * - spell checking immer oder nur wenn spin kein ergebnis lieferte (ist kein Zeitproblem)?
 * ISSUES:
 * - spell checking -> language engine braucht lï¿½nger zum Starten
 */

package Language;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.util.LinkedList;
//import java.util.Arrays;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import de.dfki.spin.Spin;
import de.dfki.spin.SpinException;
import de.dfki.spin.SpinInitOptions;
import de.dfki.spin.SpinType;
import de.dfki.spin.SpinTypeSystem;
import de.dfki.spin.ParseException;
import de.dfki.spin.TopNode;
import de.dfki.spin.ObjectNode;
import de.dfki.spin.SlotNode;
import de.dfki.spin.TreeObject;
import de.dfki.spin.ValueLeaf; 
//import de.dfki.spin.TreeNode;



/**
 * The main language engine object - each agent should contain an instance of this class, 
 * including the user.
 * 
 * <p> Note: An optional Log of the conversation or detailed processing performed 
 * by all the language engines can be found in language_log.txt.
 * The logging level can be set by calling the static SetLogging() method.
 *
 * <p>TASKS:
 *
 * <p>AGENT SPEAKS: Pass a speech act to Say() containing the name of the speaker, 
 * the name of the recipient, the speech act type, plus any context variables required
 * by that speech act. The method returns the same speech act with an &lt;Utterance&gt;
 * tag added.
 *
 * <p> AGENT RECEIVES A SPEECH ACT FROM ANOTHER AGENT: Pass it (complete with utterance) 
 * to Hear(). The method checks for and &lt;utterance&gt; tag and extracts context information. 
 * No action needed on the part of the agent.
 *
 * <p> VICTIM AGENT TELLS EPISODE SUMMARY: Pass an extract of the autobiographical memory in an 
 * &lt;ABMemory&gt; tag containing place, location, a list of actions having each a subject 
 * and optionally a target, an additional parameter and an emotion associated. Returns the 
 * same &lt;ABMemory&gt; with a &lt;Summary&gt; tag added containing the textual representation
 * of the autobiographical memory extract. This is only possible for the victim agent.
 *
 * <p> USER AGENT RECEIVES KEYBOARD INPUT: Pass it to Input() as the &gt;Utterance&lt;
 * element of a speech act, along with the name of the speaker and any context variables
 * you want to know the state of. The method returns the same speech act with a
 * &lt;Type&gt; element added, plus the current state of any requested context variables.
 *
 * @author Steve Grand
 * @author Thurid Vogt
 */
public class LanguageEngine { 
    
    
    /**
     * Switch debugging information
     */
	public static boolean debug = false;
    
    /** 
     * References to all the engines in the system.
     * This allows one language
     * engine to interrogate another by name, e.g. to establish whether agent YouName is
     * male or female, given the agent's name in a speech act
     */
    private static HashMap engines = new HashMap();
    
    
    /** 
     * A list of all the SPIN objects in the system, indexed by filespec.
     * This allows to keep a single copy of any SPIN objects that are shared
     * by several agents, thus saving memory and load time
     */
    private static HashMap spins = new HashMap();
 
    /** 
     * A list of all the SPIN type systems in the system, indexed by filespec.
     * This allows to keep a single copy of any SPIN type systems that are shared
     * by several agents, thus saving memory and load time
     */
    private static HashMap stss = new HashMap();
    
    /**
     * The GLOBAL conversation context.
     * Initially, contains two variables:<br>
     * "him" with value "him"<br>
     * "it" with value "it"
     */
    private static Context globalContext = new Context();
     static 
    {
        globalContext.Add("him", "");                // name of the person we're all speaking about
        globalContext.Add("it", "");                  // name of the object we're all speaking about
        // TODO: ****** Add more global context variables here *******
        // (local context variables are defined in constructor)
    }

     /** 
     * The AGENT-SPECIFIC conversation context.
     */
    private Context localContext = null;
    
    /**
     * The name of the agent who belongs this language engine.
     *  Used as an ID to identify whose engine this is.
     */
    private String name = null;
    
    /** 
     * directory of SPIN definition files (templates, lexicon, type system) of this language engine.
     */
    private File dir = null;
    
    /**
     * SPeech INterpretation component for utterance generation and utterance reception (agent)
     * resp. input analysis (user)
     */
    private Spin spin = null;
    
    /**
     * Only for the victim agent: SPeech INterpretation component for generation of episode summaries from
     * autobiographical memory extracts.
     */
    private Spin summary=null;
    
    /** 
     * SPIN type system.
     */
    private SpinTypeSystem sts =null;
    
    /**
     * A list of the last {@link LanguageEngine#HISTORY_LENGTH} utterances.
     * This is used to avoid repetitious utterances.
     * Utterances that have just been generated receive a higher weight.
     * 
     * 
     */
	static private LinkedList<String> lastUtterances = new LinkedList<String>();
	
	/**
	 * The maximum length of the remembered utterances in {@link LanguageEngine#lastUtterances}.
	 * A constant set to <code>5</code>.
	 */
	static private final int HISTORY_LENGTH = 5;

	/**
	 * A switch to add more random noise.
	 * This is used because subsequent generated random numbers can be very similar if there has
	 * been not much activity. 
	 */
	private boolean randomSwitch =false;
	
	/**
	 * Spell checker, only available for the user language engine.
	 * Used to correct faulty user input.
	 */
	private static SpellCheck spellcheck = null;

     
    /** Creates a new instance of the LanguageEngine. Each agent in the system,
     * including the user, should have a LanguageEngine. Several agents can share
     * a single LACT database or individual agents can have their own.
     * 
     * @param agentname The personal name of the agent (as it should be spoken)
     * @param sex       The sex of the agent - must be "m" or "f" or "n"
     * @param role      The part this agent plays ("victim", "bully", "friend", "user"...)
     * @param filespec  The full path to the directory containing the SPIN definition files for
     * this agent
     * @throws SactException is never thrown
     * @throws LactException is never thrown
    */
	@SuppressWarnings("unchecked")
	public LanguageEngine(String agentname, String sex, String role, File filespec) 
	throws SactException, LactException {
    	Log.Info("Creating LanguageEngine: " + agentname + "," + sex + "," + role + "," + filespec);
    	name = agentname;                   // store my name for later identification      	
            	
    	Log.Info("Setting up language engine for agent "+agentname+" with spin");
    	Log.Detail("        Loading spin databases from directory "+filespec.getName());
    	
    	dir=filespec;
    	
    	File[] lexFiles =  new File[]{new File(filespec,"lexicon.ldl")};
    	SpinInitOptions spinInitOptions = new SpinInitOptions();
    	File[] ruleFiles = new File[]{new File(filespec,"templates.tdl")};
  	
    	if (spins.containsKey(filespec)) {
            Log.Detail("    Using existing spin");
            spin = (Spin) spins.get(filespec);    // use the existing copy
            sts = (SpinTypeSystem) stss.get(filespec);
        }
        else {
        	sts = new SpinTypeSystem(new File(filespec,"types.xml"));
       		try {
       			spin = new Spin(ruleFiles, sts, lexFiles, spinInitOptions);
       			spin.setMinScoreForScoreClassComplete(0.01); // mind. 1/100 der Woerter muessen verarbeitet werden
       			if (debug)
       				spin.startHtmlInterface(new File(filespec, "spin_"+agentname+".html"));
       		} catch (SpinException se) {
        		Log.Warning("processing error: "+se.getMessage());
        		se.printStackTrace();
        	} catch (ParseException pe) {
        		Log.Warning(pe.getMessage()); 
        	}
            synchronized (spins) {
            	spins.put(filespec, spin);
            }
            synchronized (stss) {
            	stss.put(filespec, sts);
            }
        }

    	if (role.equalsIgnoreCase("user"))
    		spellcheck = new SpellCheck(dir+"/fearnot.dic");
    	
    	try {
    		
            	File[] summaryFiles = new File[]{new File(filespec,"summaries.tdl")};
    			summary = new Spin(summaryFiles,sts,lexFiles,spinInitOptions);
    			summary.setMaxProcessingTime(10000);
       			if (debug)
       				summary.startHtmlInterface(new File(filespec, "summary_"+agentname+".html"));

    	} catch (ParseException pe) {
    		Log.Warning(pe.getMessage());     		
    	}
              
    	Log.Detail("    Defining local context variables:");
    	localContext = new Context();

    	// And add the system-defined variables
    	localContext.Add("me",agentname);       // name of the speaker
    	localContext.Add("sex",sex);            // sex of the speaker
    	localContext.Add("role",role);          // the part this agent plays (victim, bully, friend, user...)
       	localContext.Add("you","you");          // name of the person I'm speaking to
       	localContext.Add("yourole","role");          // name of the person I'm speaking to
       	localContext.Add("yousex",sex);          // name of the person I'm speaking to
    	// TODO: ******** add more local context variables here ********
      
       	if (role.equals("bystander"))
       		globalContext.Add("bystander",agentname);
       	else if (role.equals("defender"))
       		globalContext.Add("defender",agentname);
       	
    	Attach(this);                       // Finally, attach myself by name to the static hashtable of engines
    	Log.Info("Language Engine created");
       
    }
    
    /**
     * An opportunity for an agent to remove itself from the static list of active engines before 
     * being destroyed.
     */
    protected void finalize() throws Throwable {
    	try {
            Detach(this);
        } 
    	finally {
            super.finalize();
        }
     }
    
    /**
     * Attach {@link LanguageEngine} to the static list of all existent engines
     * in a threadsafe manner
     * 
     * @param eng The {@link LanguageEngine} to attach
     */
    @SuppressWarnings("unchecked")
	private static synchronized void Attach(LanguageEngine eng)
    {
        engines.put(eng.name, eng);      // keep a ref to each engine in a hashtable
    }
    
    /**
     * Detach {@link LanguageEngine} from the static list of all existent engines
     * in a threadsafe manner
     * 
     * @param eng The {@link LanguageEngine} to detach
     */
    private static synchronized void Detach(LanguageEngine eng)
    {
        engines.remove(eng.name);
    }
    
   
    /** changes a context variable's value - or adds the variable, if it does not exist yet
     * 
     * @param key	The name of the context variable
     * @param value	The new value of the context variable
     */
    public void setLocalContext(String key, String value) {
    	if (key.equals("me"))
    		name=value;
    	localContext.Add(key,value);    	
    }
    
      
     /**
      * Static method to discover a fact about a named agent's conversational
      * context. Used internally to allow one agent's language system to
      * discover things like the sex/gender of another agent in order to
      * use the right grammatical form in utterances. 
      *
      * @param name The name of the agent to be interrogated.
      * @param key  The LOCAL context variable to be examined (sex, me...)
      * @return     The value of that context variable, or null if the agent or
      *             context variable can't be found
      */
     static String GetLocalContext(String name, String key)
     {
         LanguageEngine engine = (LanguageEngine)engines.get(name);
         if (engine==null)
             return null;
          return engine.localContext.Get(key);
     }
    

     
     /**
      * Victim agent narrates an extract of the autobiographical memory at the beginning of
      * the user interaction.
      * Takes an <code>ABMemory</code> node as input and adds a textual summary to it
      * 
      * <p>The <code>ABmemory</code> contains a list of <code>Events</code> which can have the
      * following parameters:
      * <PRE>
      * &lt;ABMemory&gt; 
      *     &lt;Event&gt; 
      *     	&lt;Time count="number"&gt; month/week/day/hour ; optional, usually only in first event&lt;/Time&gt;
      *     	&lt;Location&gt; e. g. classroom, playground, etc.; optional, usually only in first event&lt;/Location&gt;
      *     	&lt;Subject&gt; the name of the acting person, or "I" for victim&lt;/Subject&gt;
      *     	&lt;Action&gt; what has been done&lt;/Action&gt;
      *     	&lt;Target&gt; the target (person/object) of the action; optional&lt;/Target&gt;
      *     	&lt;Param&gt; additional parameters; optional&lt;/Param&gt;
      *     	&lt;Emotion intensity="little/normal/high" direction="a person"&gt; ; direction is optional; optional &lt;/Emotion&gt;
      *     &lt;/Event&gt;
      * &lt;/ABMemory&gt;
      * </PRE>
      * The returned speech act will contain the following new element:
      * <PRE>
      *     &lt;Summary&gt;the textual representation of the episode summary&lt;/Summary&gt;
      * </PRE>
      *
      * @param abMemory The XML string containing the autobiographical memory extract
      * @return The same speech act, with an added &lt;Summary&gt; node
      * @throws SactException if there's a problem with the XML syntax of the
      * </code>ABMemory</code>
       * @throws LactException is never thrown
      */
     public String Narrate(String abMemory) throws SactException, LactException {
    	 String you = "";
    	 
    	 if (summary==null) {
    		 Log.Warning("This agent cannot tell episode summaries!");
    		 return abMemory;
    	 }
    	 
    	 Vector<ObjectNode> generationVector = new Vector<ObjectNode>();
    	 DOM dom;
    	 ObjectNode on, eon=null;
    	 int i;
       
    	 if (abMemory==null || abMemory.equals(""))
    		 return "";
    	 
        // Create a DOM object from the XML.  
        try {
        	dom = new DOM(abMemory);
        } catch (SAXParseException saxp) {
            throw new SactException("Error in autobiographical memory at line" + saxp.getLineNumber(), saxp);
        } catch (IOException ioe) {
            throw new SactException("IOException parsing autobiographical memory", ioe);
        }
 
        Log.Detail(name + " is asked to narrate: " + abMemory);
        Log.Detail("    Parsing ABMemory XML...");
        
        // Enter the root <SpeechAct> node
        // If this is not a speech act, simply return it unaltered
        Node node = dom.EnterRootNode("ABMemory");
        if (node==null)
            return abMemory;
        
        boolean foundEvent = false;
        String lastSubject ="";
        // Extract the tags I'm interested in
        do  {
        	if (node.getNodeType()==Node.ELEMENT_NODE) {         // only interested in elements
        		String tag = node.getNodeName();
                
        		if (tag.equalsIgnoreCase("Receiver")) {
        			you = DOM.GetTextNode(node);
        		}
        		else if (tag.equalsIgnoreCase("Event")) {
        			on = new ObjectNode(sts.getType("Event"));
        			if (foundEvent == false) 
        				foundEvent=true;
        			else
        				generationVector.add(new ObjectNode(sts.getType("Connection")));
        			
        			boolean foundAction=false;
        			NodeList nl = node.getChildNodes();
        			for (int c=0;c<nl.getLength();c++) {
        				Node n = nl.item(c);
        				String nt = n.getNodeName().toLowerCase();
        				String nv = DOM.GetTextNode(n);
        				if (nt.equals("time")) {
        					String count = DOM.GetAttribute(n,"count");
        					ObjectNode ton = new ObjectNode(sts.getType("Time"));
        					ton.appendChildNodes(new SlotNode[]{
        							new SlotNode("count",new ValueLeaf(count)),new SlotNode("value",new ValueLeaf(nv))});
        					on.appendChildNode(new SlotNode("time",ton));
        				}
        				else if (nt.equals("emotion")) {
    						String intensity = DOM.GetAttribute(n,"intensity");
    						eon = new ObjectNode(sts.getType("Emotion"));
    						eon.appendChildNodes(new SlotNode[]{new SlotNode("intensity",new ValueLeaf(intensity)),new SlotNode("value",new ValueLeaf(nv))});
    						String direction=null;
    						try {
    							direction = DOM.GetAttribute(n,"direction");
    							if(direction.equals(you))
    							{
    								direction = "you";
    							}
    							eon.appendChildNode(new SlotNode("direction",new ValueLeaf(direction)));
    						}
    						catch (SactException se) {
              						
    						}
              						//              						on.appendChildNode(new SlotNode("emotion",eon));        							
    					}
    					else {
        					if (nt.equals("action") || nt.equals("intention"))
        							foundAction=true;
        					if(nt.equals("target"))
    						{
    							if(nv.equals(you))
    							{
    								nv = "you";
    							}
    						}
        					else if(nt.equals("subject")) {
        						if(nv.equals(you))
        						{
        							nv = "you";
        						}
        						else if (nv.equals(lastSubject))
        							on.appendChildNode(new SlotNode("sameSubject",new ValueLeaf("yes")));
        						lastSubject=nv;
        					}
        					on.appendChildNode(new SlotNode(nt,new ValueLeaf(nv)));
        				}	
        			}
        				
        			String gender = localContext.Get("sex");
        			if (gender != null)
        				on.appendChildNode(new SlotNode("gender",new ValueLeaf(gender)));

        			if (!foundAction) {
        				Log.Warning("No action or intention was found!");
        				return abMemory;
        			}
        			generationVector.add(on);
        			if (eon!=null) {
        				generationVector.add(eon);
        				eon=null;
        			}
        		}
        
                
        		// Add any other top-level node types here
                
        	}   // All unwanted nodes are ignored
                                 
            node = node.getNextSibling();                // and on to the next node
            
        } while (node != null);

		if (!foundEvent) {
			Log.Warning("No event given!");
			return abMemory;
		}

		for (i=generationVector.size()-1;i>=0;i--) {
			if (generationVector.elementAt(i).isObjectNodeOfType("Connection")) {
				generationVector.elementAt(i).appendChildNode(new SlotNode("isfinal",new ValueLeaf("yes")));
				break;
			}
		}
		
		Vector<TreeObject> summaryObjects = new Vector<TreeObject>();
		TreeObject[] temp;
		String resultSummary="";
		TopNode[] result;
		
		for (i=0;i<generationVector.size();i++) {
			on = generationVector.get(i);
        	Log.Detail(on.toString());
			result = summary.process(new ObjectNode[]{on});
			Log.Detail("resulting top nodes: ");
			for (int j=0;j<result.length;j++)
				Log.Detail("TN["+j+"]: "+result[j]);
			Log.Detail("");
			temp=chooseSummary(result);
			for (int j=0;j<temp.length;j++) 
				summaryObjects.add(temp[j]);
		}
		
		if (summaryObjects.size()==0) {
			Log.Warning("No summary was generated.");
			return dom.ToXML();
		}
		 
		resultSummary = postProcessSummary(summaryObjects.toArray(new TreeObject[]{}));
		
   	 	dom.AddTopLevelElement("Summary", resultSummary);

        // Convert tree back into XML text  
        return dom.ToXML();
     }
     
     
     /**
      * choose a summary from the list of possible summaries as generated by SPIN.
      * First, summaries that have more than one FinalSummary element or have other elements
      * except for FinalSummary are sorted out.
      * From the rest, one summary is chosen randomly. 
      * 
      * @param result the list of possible summaries
      * @return a list of all value slots of the chosen summary
      */
     TreeObject[] chooseSummary(TopNode[] result) {
     	 int i;
    	 Vector<ObjectNode> goodSummaries = new Vector<ObjectNode>();
    	 
    	 Debug(result.length+" results were found...");
    	 for (i=0;i<result.length;i++) {
    		 ObjectNode[] summaries = result[i].getObjectNodesOfType(sts.getType("FinalSummary"));
    		 if (summaries.length ==1) {
    			 ObjectNode[] events = result[i].getObjectNodesOfType(sts.getType("Event"));
    			 boolean noChild=true;
    			 int j=0;
    			 while (j<events.length && noChild) {
    				 if (events[j].getChildNodes().length > 0)
    					 noChild=false;
    				 j++;
    			 }
    				 
    			 // if all Event nodes are empty and there are no other nodes except for Events or FinalSummaries,
    			 // add this summary to the list 
    			 if (noChild && (1 + events.length == result[i].getChildNodes().length))
    				 goodSummaries.add(summaries[0]);
    		 }
    		 
    	 }
    	 
    	 Debug("... "+goodSummaries.size()+" of them were useful.");
    	 if (goodSummaries.size()==0) {
    		 Log.Warning("No suitable summary was found!");
    		 return new TreeObject[]{};
    	 }
    	 
    	 Log.Detail("Possible summary nodes:");
    	 for (i=0;i<goodSummaries.size();i++)
    		 Log.Detail("\t"+goodSummaries.get(i));
    	 
    	 Random r = new Random();    	 
    	 int rndIndex = r.nextInt(goodSummaries.size());

    	 //sonst zu wenig random
    	 if (randomSwitch)
    		 rndIndex=goodSummaries.size()-rndIndex-1;
    	 randomSwitch=!randomSwitch;
    	 ObjectNode chosenSummary = goodSummaries.get(rndIndex);
    	 TreeObject[] summaryValues = chosenSummary.getSlotValues("value");

//    	 summary = postProcessSummary(summaryValues);
    	 
    	 return summaryValues;    	 
     }
     
     /**
      * Transform all the constituents of the result summaries into text.
      * Furthermore some post processing of the text, e. g. upper case at the start and after a full
      * stop, etc.
      * 
      * @param summaryValues The value slots of the result summary.
      * @return The final textual representation
      */
     String postProcessSummary(TreeObject[] summaryValues) {
    	 int i;
    	 String summary = getWords(summaryValues);
    	 
//    	 for (i=0;i<summaryValues.length;i++) {
//   		 summary+=summaryValues[i];
//    	 }
   	 
    	 summary = summary.replaceAll("  +"," ");
    	 summary = summary.replaceAll("([,.!?])([^ ])","$1 $2");
    	 summary = summary.replaceAll(" ([,.!?])","$1");
    	 summary=summary.trim();
    	 
    	 String[] summaryTokens = summary.split(" ");
    	 String last="",current;
    	 summary="";
    	 
    	 for (i=0;i<summaryTokens.length;i++) {
    		 current=summaryTokens[i];
    		 if (last.equals("") || last.endsWith(".") || last.endsWith("!") || last.endsWith("?"))
    			 if (Character.isLowerCase(current.charAt(0)))
    				 summaryTokens[i]=current.substring(0,1).toUpperCase() + current.substring(1);
    		 last=current;
    	 }
    	 for (i=0;i<summaryTokens.length-1;i++)
    		 summary+=summaryTokens[i]+" ";
    	 summary+=summaryTokens[summaryTokens.length-1];
    	 
    	 if (!(summary.endsWith(".") || summary.endsWith("!") || summary.endsWith("?")))
    	   	 	summary+=".";

    	 return summary;
     }
     
     
     
        
    /**
     * The agent wishes to say something - takes a speech act as input and
     * adds an utterance to it.
     *
     * <p>The minimal supplied speech act consists of the following:
     * <PRE>
     * &lt;SpeechAct&gt; 
     *     &lt;Sender&gt; agent_name_of_the_speaker &lt;/Sender&gt;
     *     &lt;Receiver&gt; agent_name_of_the_recipient &lt;/Receiver&gt;
     *     &lt;Type&gt; speech_act_name &lt;/Type&gt;
     *     &lt;!-- optional list of context variables to be set --&gt;
     *     &ltContext id=varname&gt; value &lt;/Context&gt;
     * &lt;/SpeechAct&gt;
     * </PRE>
     * The returned speech act will contain the following new element:
     * <PRE>
     *     &lt;Utterance&gt;the text to be spoken&lt;/Utterance&gt;
     * </PRE>
     *
     * @param speechAct The XML string containing the speech act
     * @return The same speech act, with an added &lt;utterance&gt; node
     * @throws SactException if there's a problem processing the speech act XML or a speech act
     * of this type could not be generated
     * @throws LactException is never thrown
     */
    public String Say(String speechAct) throws SactException, LactException {
    	DOM dom = null;
    	final String unknownType = "unknown";
    	final String value= "value";
    	String type = null;
    	String utterance;
    	Vector<ObjectNode> generationVector = new Vector<ObjectNode>();
       
        // Create a DOM object from the XML.  
        try {
        	dom = new DOM(speechAct);
        } catch (SAXParseException saxp) {
            throw new SactException("Error in speech act at line" + saxp.getLineNumber(), saxp);
        } catch (IOException ioe) {
            throw new SactException("IOException parsing speech act", ioe);
        }
 
        Log.Detail(name + " is asked to say: " + speechAct);
        Log.Detail("    Parsing SACT XML...");
        
        // Enter the root <SpeechAct> node
        // If this is not a speech act, simply return it unaltered
        Node node = dom.EnterRootNode("SpeechAct");
        if (node==null)
            return speechAct;
        
        boolean foundType = false;
        // Extract the tags I'm interested in
        do  {
        	if (node.getNodeType()==Node.ELEMENT_NODE) {         // only interested in elements
        		String tag = node.getNodeName();
                
                // We can ignore <sender> since we already know who we are!
                // <Sender> agent_name </Sender>
                
                // <Receiver> agent_name </Receiver>
                // Defines [YOU] local context
        		if (tag.equalsIgnoreCase("Receiver")) {
        			String you= DOM.GetTextNode(node);
                    localContext.Set("you",you);
                    LanguageEngine youEngine = (LanguageEngine) engines.get(you);
                    if (youEngine != null)	{
                    	localContext.Set("yourole",youEngine.localContext.Get("role"));
                    	localContext.Set("yousex",youEngine.localContext.Get("sex"));
                    }
                }
                // <Type> LACT_name </Type>
                // Defines LACT type
        		else 
        			if (tag.equalsIgnoreCase("Type")) {
        				try {
        					type = DOM.GetTextNode(node);
        				} catch (NullPointerException ne) {
        					Log.Warning("No speech act type given!");
        					return speechAct;
        				}
        				foundType = true;
        			}
        		// <Context id="variable"> context </Context>
        		// Sets an arbitrary context variable
        			else 
        				if (tag.equalsIgnoreCase("Context")) {
        					String var = DOM.GetAttribute(node, "id").toLowerCase();
        					String val = DOM.GetTextNode(node);
       					
        					SpinType st = sts.getType(var);
        					if (st==null) {
        						Debug("Type "+var+" is null");
                    			Log.Warning("A context variable of type "+var+" does not exist!");
        					}
        					else {
        						Debug("var "+var+" val "+val);
        						if (value !=null) {
        							if (st.inheritsFrom(sts.getType("localContext"))) 
        								localContext.Add(var,val);
        							else
        								if (st.inheritsFrom(sts.getType("globalContext"))) 
        									globalContext.Add(var,val);
        						}
        						else
        							Log.Warning("no value was given for context variable "+var+"!");
        					}

        				}
                
        		// Add any other top-level node types here
                
        	}   // All unwanted nodes are ignored
                                 
            node = node.getNextSibling();                // and on to the next node
            
        } while (node != null);
       
        localContext.Print(); 
        globalContext.Print();
        
        if (!foundType)
        	type=unknownType;
        // Kontext in Form fï¿½r SPIN bringen
//        if (type.trim().contains(" ")) {
//        	String[] events = type.trim().split(",");
//        	for (String event : events) {
//            	ObjectNode on = new ObjectNode("event",sts);
//        		String[] parts = event.trim().split(" ");
//        		if (parts.length <2)
//        			Log.Warning("no valid event for episode summary: '"+event+"'!");
//        		int i=0;
//        		on.appendChildNode(new SlotNode("agent",new ValueLeaf(parts[i++])));
//        		if (parts[i].equals("failed")) {
//        			on.appendChildNode(new SlotNode("failed",new ValueLeaf("true")));
//        			i++;
//        		}
//        		on.appendChildNode(new SlotNode("action",new ValueLeaf(parts[i++])));
//        		if (i<parts.length)
//        			on.appendChildNode(new SlotNode("object",new ValueLeaf(parts[i])));
//            	generationVector.add(on);
//        	}
//        }
//        else
        	generationVector.add(new ObjectNode(sts.getType("Type"),new SlotNode(value, new ValueLeaf(type))));
        String[] contextKeys = localContext.getKeys();
        for (int i=0;i<contextKeys.length;i++) 
        	if (sts.getType(contextKeys[i])!=null)
        		generationVector.add(new ObjectNode(sts.getType(contextKeys[i]),new SlotNode(value,new ValueLeaf(localContext.Get(contextKeys[i])))));
        	else
        		generationVector.add(new ObjectNode(sts.getType("Synonym"),new TreeObject[]{new SlotNode("semCat",new ValueLeaf(contextKeys[i])),new SlotNode(value,new ValueLeaf(localContext.Get(contextKeys[i])))}));
        contextKeys = globalContext.getKeys();
        for (int i=0;i<contextKeys.length;i++)
        	if (sts.getType(contextKeys[i])!=null)
        		generationVector.add(new ObjectNode(sts.getType(contextKeys[i]),new SlotNode(value,new ValueLeaf(globalContext.Get(contextKeys[i])))));
        	else
        		generationVector.add(new ObjectNode(sts.getType("Synonym"),new TreeObject[]{new SlotNode("semCat",new ValueLeaf(contextKeys[i])),new SlotNode(value,new ValueLeaf(globalContext.Get(contextKeys[i])))}));

        // Create the utterance & update any context
        Log.Detail("    Creating the utterance...");
    	Log.Info("    Uttering language act: " + type);
    	ObjectNode[] generationTrees = generationVector.toArray(new ObjectNode[generationVector.size()]);
    	for (int i=0;i<generationTrees.length;i++)
    		Log.Detail(generationTrees[i].toString());
       	TopNode[] result = spin.process(generationTrees);

       	try {
       		utterance = chooseUtterance(result);
       	} catch (SactException se) {
       		throw new SactException("Couldn't locate a language act of type=" + type);
       	}
       	
       	Log.Conversation(name + " says \"" + utterance + "\"\n");
       
        // Add utterance to the DOM tree, in the form
        // <Utterance>the text to be uttered</Utterance>
        // (don't put white space around the text in case the agent fails to
        // remove it)
        dom.AddTopLevelElement("Utterance", utterance);

        // Convert tree back into XML text  
        return dom.ToXML();
    }
     
    
    /**
     * Get the position of <code>utterance </code> in {@link LanguageEngine#lastUtterances}. 
     * The more recently the utterance has occured, the higher the value is.
     * 
     * @param utterance The utterance (Synonyms and lexical objects have not yet been replaced)
     * @return the position of <code>utterance </code> in {@link LanguageEngine#lastUtterances},
     * or 0 if it can't be found.
     */
    int getHistoryPosition(String utterance) {
    	int pos = lastUtterances.lastIndexOf(utterance); 
        if (pos >-1)
        	pos+=HISTORY_LENGTH-lastUtterances.size()+1;
        else
        	pos++;
    	return pos;
    }
    
    
    /**
     * Choose an utterance of a list of generated utterances by the {@link LanguageEngine#Say(String)}
     * method.
     * Computes a score for each utterance based on the number of elements of Type "Internal", 
     * of Type "Utterance", of Type "Context", on the history position and some random noise.
     * Furthermore some post-processing of the utterance to look nicer (e. g. upper case at start
     * sentence)
     * 
     * @param result The list of possible utterance nodes
     * @return The textual representation of the finally chosen utterance
     * @throws SactException If no suitable utterance could be found
     */
    String chooseUtterance(TopNode[] result) throws SactException {
       	double best_score=Double.POSITIVE_INFINITY,score;
       	int history, i;
       	ObjectNode utterance;
       	ObjectNode resultUtterance=null;
       	
       	// score ist 100*(# interne Spin-generierte Elemente) + (# Kontext-Elemente) + 10*(# Utterance-Elemente) - (# Lex-Elemente) - 0.9^history + Zufallszahl 
       	// history = 5 - (# Schritte, als die Aeusserung das letzte Mal gesagt wurde)
       	
       	for (i=0; i<result.length;i++) {
      		ObjectNode[] utteranceNodes = result[i].getObjectNodesOfType(sts.getType("Utterance"));
   			if (utteranceNodes.length >0) {
   				int contextElements = result[i].getObjectNodesOfType(sts.getType("Context")).length;
   				int internalElements = result[i].getObjectNodesOfType(sts.getType("Internal")).length;
   				utterance=utteranceNodes[0];
   				history=getHistoryPosition(utterance.toString());
   				double rand = Math.random();
   				score = 100*internalElements + 1*contextElements + 10*utteranceNodes.length - 1 +((double) history/HISTORY_LENGTH) + rand;
   				Log.Detail("Score for "+ result[i]+" is:");
   				Log.Detail(score+"= 100 * "+internalElements+"(intElem) + "+contextElements+"(conElem) + "+utteranceNodes.length+ "(uttElem) - " + (1 -((double) history/HISTORY_LENGTH))  +" (hist) + "+rand+"(rnd)");
   				if (score < best_score) {
   					if (utteranceNodes.length >0)
   						//TODO: nicht den ersten, sondern den "besten" auswaehlen, oder alle
   						// zusammen ausgeben
   						resultUtterance = utteranceNodes[0];
   					best_score=score;
   				}       	
   			}
       	}

       	if (resultUtterance==null)
       		throw new SactException();       	
       	
    	Log.Detail("result utterance node: "+resultUtterance);
       	lastUtterances.addLast(resultUtterance.toString());
       	if (lastUtterances.size() > HISTORY_LENGTH)
       		lastUtterances.removeFirst();
       	
       	//post-process:
       	String finalUtterance=morph(resultUtterance);
		//a -> an
		//Klein-/Grossschreibung
       	finalUtterance=finalUtterance.replaceAll("  +"," ");
       	String[] utteranceTokens = finalUtterance.split(" ");
    	String last="",current;
       	finalUtterance="";
    	for (i=0;i<utteranceTokens.length;i++) {
    		current=utteranceTokens[i];
    		// nur im Englischen!!
    		if (last.equals("a") && current.substring(0,1).matches("[aeiouAEIOU]"))
    			utteranceTokens[i-1]="an";
    		if (last.equals("A") && current.substring(0,1).matches("[aeiouAEIOU]"))
    			utteranceTokens[i-1]="An";

    		if (last.equals("") || last.endsWith(".") || last.endsWith("!") || last.endsWith("?"))
    			if (current.length()>0 && Character.isLowerCase(current.charAt(0)))
    				utteranceTokens[i]=current.substring(0,1).toUpperCase() + current.substring(1);
    		last=current;
    	}
    	for (i=0;i<utteranceTokens.length-1;i++)
    		finalUtterance+=utteranceTokens[i]+" ";
		finalUtterance+=utteranceTokens[utteranceTokens.length-1];
       	return finalUtterance;
    }
    
    
    /**
     * Convert this TreeObject into a textual representation. 
     * In particular, choose a value for Synonyms from the lexicon, and choose correct 
     * case/number/gender information for a Lex object.
     * 
     * @param value The TreeObject to convert (a value slot of chosen Summary/Utterance node)
     * @return the textual representation
     */
    String getWord(TreeObject value) {
    	String semCat=null, semCatName=null, gender="mask", word="";
    	String[][] temp;
    	boolean useSemCat=true;
    	Vector<String> semCatType= new Vector<String>();
    	Vector<String> morph = new Vector<String>();
    	ObjectNode oValue = (ObjectNode) value;
    	Vector<ObjectNode> children = new Vector<ObjectNode>();
    	if (oValue.hasSlot("stem")) {
    		semCatName=oValue.getSlotValueAsString("stem");
    		useSemCat=false;
    	}
    	else {
    		semCat=oValue.getSlotValueAsString("semCat");
    		String tmp[] = oValue.getSlotValuesAsStrings("type");
    		Random r = new Random();
    		semCatType.add("");
    		for (int i=0;i<tmp.length;i++) {
    			int rand = r.nextInt(semCatType.size()+1);
    			semCatType.insertElementAt(tmp[i],rand);
    			
    		}
//			semCatName=semCat;
//			if (rand!=tmp.length)
//				semCatName+=tmp[rand];
    	}
    	
    	for (TreeObject child : oValue.getChildNodes()) {     					
    		if (child instanceof SlotNode) {
    			String nodeName=((SlotNode) child).getName();
    			if (nodeName.equals("number") || nodeName.equals("case") || nodeName.equals("tense")) 
    				morph.add(oValue.getSlotValueAsString(nodeName));
    			else if (nodeName.equals("gender") || nodeName.equals("person")) {
    				gender = oValue.getSlotValueAsString(nodeName);
    				if (gender.equals("m"))
        					gender="mask";
        				else if (gender.equals("f"))
        					gender="fem";
            			morph.add(gender);
    			}
//    			else if (nodeName.equals("semCat") || nodeName.equals("stem"))
//    				continue;
    			else {
    				TreeObject to = ((SlotNode) child).getFirstChild();
    				if (to instanceof ObjectNode) {
    					ObjectNode on = (ObjectNode) to;
    					if (on.isObjectNodeOfType("Lex") || on.isObjectNodeOfType("Synonym")) 
    						children.add(on);
    				}    				
//   				else 
//    					globalContext.Add(nodeName,oValue.getValue(nodeName));
    			}
    		}
    	}
		
    	if (useSemCat) {
    	
    		temp = new String[0][0];
    		for (int i=0;i<semCatType.size();i++) { 
    			semCatName=semCat+semCatType.elementAt(i);
    			temp=spin.getLexDB().getWords(semCatName,(morph.size() >0)? morph.toArray(new String[morph.size()]):null,useSemCat);
    			if (temp.length>0)
    				break;
    		}
    	}
    	else {
        	temp=spin.getLexDB().getWords(semCatName,(morph.size() >0)? morph.toArray(new String[morph.size()]):null,useSemCat);
   		
    	}
    	
//    	Debug(semCat+" (semCat)");
    	if (temp.length >0) {
	    	int rand=(int) (Math.random()*temp.length);
	    	String morphology = (value.isObjectNodeOfType("Synonym"))?temp[rand][1]:temp[0][1];
	    	
	    	for (int i=0;i<children.size();i++) {
	    		ObjectNode on = children.get(i);
	    		on.addToSlot("number",new ValueLeaf(morphology.split("-")[0]));
	    		on.addToSlot("gender",new ValueLeaf(morphology.split("-")[1]));
	    		on.addToSlot("case",new ValueLeaf(morphology.split("-")[2]));
	    		word+=getWords(new TreeObject[]{on})+" ";
	    	}
	    	
	    	
			if (value.isObjectNodeOfType("Synonym")) {
				String synonym = temp[rand][0]; 
				word+= synonym;
				synonym=spin.getLexDB().getLex(synonym,semCatName).getStem();
				if (!(Context.Set(semCat,synonym,globalContext,localContext)))
					globalContext.Add(semCat,synonym);
			}
			else
				word+=temp[0][0];
			return word.trim();
		}
		else {
			Log.Warning("No suitable word found for semantic category "+ (useSemCat?semCat:semCatName) +" and morphology "+morph+"!");
			return "";
		}
    	
    }
    
    /**
     * Convert a list of value slot nodes into their textual representation.
     * 
     * @param valueNodes The TreeObjects to convert
     * @return The concatenated text representations
     */
    String getWords(TreeObject[] valueNodes) {
    	String returnUtterance="";
    	for (de.dfki.spin.TreeObject value : valueNodes) {
     		if (value.isObjectNodeOfType("Synonym") || value.isObjectNodeOfType("Lex")) {
     			returnUtterance+=getWord(value);
    		}
    		else
    			returnUtterance+=value;
    	}    	
    	return returnUtterance;
    }
    
    /**
     * Extract the value slots of an ObjectNode and convert 
     * them into their textual representation.
     * 
     * @param utteranceNode The ObjectNode with the value slots
     * @return the textual representation
     */
    String morph(ObjectNode utteranceNode) {
    	return getWords(utteranceNode.getSlotValues("value"));
    	    	    	
    }
    
    /**
     * The agent is the recipient of somebody else's speech act - use it to update
     * the conversational context.
     * <p>The input is the full speech act (in other words, a speech act including
     * an utterance). 
     * Then both the local context of the language engine and global context are updated with the 
     * specified context attributes, so only the actual recipient of the SACT currently needs to 
     * call this method.
     *
     * @param speechAct The speech act, complete with utterance
     * @return          The unaltered speech act (for consistency with Say())
     * @throws          SactException if there's something wrong with the input speech act XML, 
     * 					  the speech act doesn't contain an utterance
     *                    or there's a problem with setting the context
     * @throws LactException is never thrown
     */
     public String Hear(String speechAct)
     throws SactException, LactException
     {
        DOM dom = null;
        String utterance = null;
         
        Log.Detail(name + " hears: " + speechAct);
        Log.Detail("    Parsing SACT XML...");
         
        // Create a DOM object from the XML.  
        try
        {
            dom = new DOM(speechAct);
        }
        catch (Exception e)
        {
            throw new SactException("Could not parse the speech act XML");
        }
        
        // Enter the root <SpeechAct> node
        // If this is not a speech act, simply return it unaltered
        Node node = dom.EnterRootNode("SpeechAct");
        if (node==null)
            return speechAct;
        
        // Extract the tags I'm interested in
        do {
            if (node.getNodeType()==Node.ELEMENT_NODE) {         // only interested in elements
                String tag = node.getNodeName();
                
                // <Utterance> the spoken text </Utterance>
                if (tag.equals("Utterance")) {
                    utterance = DOM.GetTextNode(node);
                }
                 
                // <Sender> agent_name </Sender>
                // Defines [YOU] local context for the recipient
                // (we can ignore the <Receiver> tag because we already know who we are
                if (tag.equals("Sender")) {
                    localContext.Set("you", DOM.GetTextNode(node));
                }
                else 
                	if (tag.equalsIgnoreCase("context")) {          // <Context id=xxx>
                		String var=null, value=null;
                		SpinType st=null;
                		try {
                			var = DOM.GetAttribute(node, "id").toLowerCase();
                			st = sts.getType(var);
                		} catch (NullPointerException ne) {
                			throw new SactException("text missing for context attribute");
                		}
                		try {
                			value = DOM.GetTextNode(node);
                		} catch (NullPointerException ne) {
                			Log.Warning("No value given for context variable of type "+var+"!");
                		}
                		if(value !=null) {
                			if (st.inheritsFrom(sts.getType("localContext")))
                				localContext.Add(var,value);
                			else
                				if (st.inheritsFrom(sts.getType("globalContext")))
                					globalContext.Add(var,value);
                			Log.Detail("Context: name: "+var+", value: "+value);
                		}
                	}
               
                // Extract any other top-level node types here
            } 
             node = node.getNextSibling();                // and on to the next node
        } while (node != null);
       
        if (utterance==null)
            throw new SactException("Couldn't find an utterance in the speech act");
        
        Log.Detail("    Utterance is \"" + utterance + "\"");
        Log.Conversation(name + " hears \""+utterance+"\"");
//        Log.Detail("    Standardising to establish context...");
//        // Standardise the utterance in order to extract context information
//        // from the synonym list
//        database.Standardise(utterance, globalContext, localContext);
        Log.Detail("Done\n");
        
        return speechAct;
     }
     
     /**
      * Given a speech act containing user input in an UTTERANCE element, 
      * convert this into the most appropriate speech act type.
      * Also, update the context and add
      * a Type element and any other useful information to the speech act to 
      * signify what the user is saying.
      * This method should be called on the USER agent's language engine only.
      * 
      * The supplied speech act MUST contain the user input in an Utterance element.
      * <p>It MAY contain empty or default Type, Sender and Receiver tags, although these
      * will always be added if not present (if they exist and are not empty
      * then they are assumed to contain a default value to return if the
      * user input is not recognised).
      * <p>It MAY contain arbitrary Context elements. Any that are supplied will
      * be filled in from the user agent's context variables. This is the means
      * by which the agent mind requests any facts about the conversational context.
      *
      * <p>The minimal supplied speech act consists of the following:
      * <PRE>
      * &lt;SpeechAct&gt; 
      *     &lt;Utterance&gt; the user input string &lt;/Utterance&gt;
      *     &lt;!-- optional list of context variables to be returned --&gt;
      *     &ltContext id=varname&gt; value &lt;/Context&gt;
      * &lt;/SpeechAct&gt;
      * </PRE>
      * The minimal returned speech act will contain:
      * <PRE>
      * &lt;SpeechAct&gt; 
      *     &lt;Utterance&gt; the user input string &lt;/Utterance&gt;
      *     &lt;Sender&gt; agent_name_of_the_user &lt;/Sender&gt;
      *     &lt;Type&gt; best_fit_language_act_name (or "unknown") &lt;/Type&gt;
      *     &lt;!-- optional list of answers to context variable requests --&gt;
      *     &ltContext id=varname&gt; value &lt;/Context&gt;
      * &lt;/SpeechAct&gt;
      * </PRE>
      * Note: there will be no leading or trailing white space around the utterance,
      * which can be printed as is.
      *
      * @param speechAct    The speech act containing at least an Utterance tag
      * @return             The speech act with added or updated &lt;Type&gt;, 
      *                     &lt;Sender&gt; and &lt;Receiver&gt; elements.
      *                     The type will be "unknown" if it wasn't recognised.
      * @throws SactException if there's a problem processing the speech act
      */
     @SuppressWarnings("unchecked")
	public String Input(String speechAct) throws SactException {
    	 DOM dom = null;
    	 String unknown = "unknown", value="value";
    	 String utterance =null, saType=unknown;
    	 TopNode result=null;
         
         Log.Detail(name + " is processing user input: " + speechAct);
         Log.Detail("    Parsing SACT XML...");
     
         try {
             dom = new DOM(speechAct);
         } catch (Exception e) {
             throw new SactException("Could not parse the speech act XML: "+speechAct);
         }
         
         // Enter the root <SpeechAct> node
         // If this is not a speech act, simply return it unaltered
         Node node = dom.EnterRootNode("SpeechAct");
         if (node==null)
             return speechAct;
         
         // Extract the utterance tag
         // context tags are ignored!!
         do {
             if (node.getNodeType()==Node.ELEMENT_NODE) {         // only interested in elements
            	 String tag = node.getNodeName();
            	 // <Utterance> the spoken text </Utterance>
                 if (tag.equalsIgnoreCase("utterance")) {
                	 try {
                		 utterance = DOM.GetTextNode(node);
                	 } catch (NullPointerException ne) {
                		Log.Warning("User input is empty!");
                		return speechAct;
                	 }                	 
                 }
//        		else 
//        			if (tag.equalsIgnoreCase("Context")) {
//        				String var = DOM.GetAttribute(node, "id").toLowerCase();
//        				String val = DOM.GetTextNode(node);
//       						
//        				SpinType st = sts.getType(var);
//        				if (st==null) {
//        					Debug("Type "+var+" is null");
//        					Log.Warning("A context variable of type "+var+" does not exist!");
//        				}
//        				else {
//        					Debug("var "+var+" val "+val);
//        					if (value !=null) {
//        						if (st.inheritsFrom(sts.getType("localContext"))) 
//        							localContext.Add(var,val);
//        						else
//        							if (st.inheritsFrom(sts.getType("globalContext"))) 
//        								globalContext.Add(var,val);
//        					}
//        					else
//        						Log.Warning("no value was given for context variable "+var+"!");
//        				}
//        			}
             } 
             node = node.getNextSibling();                // and on to the next node
         } while (node != null);
        
         if (utterance==null)
             throw new SactException("Couldn't find an utterance in the speech act");
         
         Log.Conversation("    User said \"" + utterance + "\"");
 
         int size =	localContext.getKeys().length + globalContext.getKeys().length;
         ObjectNode[] contextObjects = new ObjectNode[size];
         String[] contextKeys = localContext.getKeys();
         int i;
         for (i=0;i<contextKeys.length;i++)
         	if (sts.getType(contextKeys[i])!=null)
         		contextObjects[i]= new ObjectNode(sts.getType(contextKeys[i]),new SlotNode(value,new ValueLeaf(localContext.Get(contextKeys[i]))));
         	else
         		contextObjects[i]= new ObjectNode(sts.getType("Synonym"),new TreeObject[]{new SlotNode("semCat",new ValueLeaf(contextKeys[i])),new SlotNode("value",new ValueLeaf(localContext.Get(contextKeys[i])))});
         contextKeys = globalContext.getKeys();
         for (int j=0;j<contextKeys.length && i<size;j++) {
          	if (sts.getType(contextKeys[j])!=null)
          		contextObjects[i++]= new ObjectNode(sts.getType(contextKeys[j]),new SlotNode(value,new ValueLeaf(globalContext.Get(contextKeys[j]))));
         	else
         		contextObjects[i++]= new ObjectNode(sts.getType("Synonym"),new TreeObject[]{new SlotNode("semCat",new ValueLeaf(contextKeys[j])),new SlotNode("value",new ValueLeaf(globalContext.Get(contextKeys[j])))});
        }
         	
         Log.Detail("spin is analysing: ");
         Log.Detail("words: "+utterance);
         Log.Detail("contexts: ");
         for (i=0;i<contextObjects.length;i++)
        	 Log.Detail(i+" "+contextObjects[i].toString());
         
         
         if (!(utterance.equals(""))) {
        	 // always do spell checking now
        	 utterance=preprocessUtterance(utterance);
        	 Debug("proc. utterances: "+utterance);
//        	 TopNode[] results = spin.process(utterance+context);
        	 TopNode[] results = spin.process(utterance,contextObjects);
        	 Log.Detail(Arrays.toString(results));
        	 if (results.length>0) {
        		 result = getBestResult(results);
        		 //TODO: den nehmen mit hoechstem Score und kuerzester Verarbeitungszeit
//        		 result=results[0];
        		 Log.Detail("Spin: score of result: "+result.getSpinScore());
        		 Log.Detail("Spin analysis: "+result);
        		 ObjectNode[] typeNodes = result.getObjectNodesOfType(sts.getType("Type"));
        		 if (typeNodes.length >0)
        			 saType=typeNodes[0].getSlotValueAsString(value);
        		 Log.Detail("Updating internal context...");
        		 ObjectNode[] context = result.getObjectNodesOfType(sts.getType("Synonym"));
        		 for (i=0;i<context.length;i++)  {
        			 String var = context[i].getSlotValueAsString("semCat");
        			 String val = context[i].getSlotValueAsString(value);
       		 //Debug("Synonym: "+context[i]+" semCat: "+var+" value: "+val);
        			 globalContext.Add(var,val);
        		 }
        		 context = result.getObjectNodesOfType(sts.getType("localContext"));
        		 for (i=0;i<context.length;i++) 
        			 localContext.Add(context[i].getType().getName(),context[i].getSlotValueAsString(value));
        		 context = result.getObjectNodesOfType(sts.getType("globalContext"));
        		 for (i=0;i<context.length;i++) 
        			 globalContext.Add(context[i].getType().getName(),context[i].getSlotValueAsString(value));
        	 }
         }
         
			
         Log.Detail("Speech act type: "+saType);
         
         Log.Detail("    Updating SACT XML...");
         boolean foundType = false;
         boolean foundSender = false;
         node = dom.EnterRootNode("SpeechAct");
         do {
        	 if (node.getNodeType()==Node.ELEMENT_NODE) {        // only interested in elements
        		 String tag = node.getNodeName();
	                
        		 // <Type> 
        		 if (tag.equalsIgnoreCase("type")) {
        			 DOM.ReplaceTextNode(node, saType);
        			 foundType = true;
        		 }
        		 else 
        			 if (tag.equalsIgnoreCase("sender")) {         		 // <Sender> 
        				 DOM.ReplaceTextNode(node, name);
        				 foundSender = true;
        			 }
        			 else 
        				 if (tag.equalsIgnoreCase("context")) {          // <Context id=xxx>
        					 String var = DOM.GetAttribute(node, "id"), varValue=null;
        					 var=var.toLowerCase();
        					 /* ObjectNode[] contextNodes = result.findObjectNodes(sts.getType(var),false);
        					 // contextNodes = result.findObjectNodes(sts.getType(var),false);
        					 if (contextNodes.length >0) {
        						 varValue = contextNodes[0].getValue("value");
        						 // TODO: soll context weiterhin gelten oder nur dieses eine Mal? 
        						 localContext.Add(var,varValue);
        						 Log.Detail("Context: name: "+var+", value: "+varValue);
        					 }
        					 // Find the named context variable and set the element text
        					 // to its current value
        					  * */
//        					 if (varValue==null) {
        						 varValue = globalContext.Get(var);
        						 if (varValue==null)
        							 varValue = localContext.Get(var);
 //       					 }
        						 
    						 if (varValue==null)
    							 Log.Warning("The value of this context variable could not be recognised: " + var+"\nin utterance "+utterance);
    						 else {
    							 node.setTextContent(varValue);
//   							 DOM.ReplaceTextNode(node, varValue);
    						 }
        				 }
        	 } 
        	 node = node.getNextSibling();                // and on to the next node
         } while (node != null);
	       
         // If we didn't find a ready-mode <Type> and/or <Sender> then add these now
         if (!foundType)
        	 dom.AddTopLevelElement("Type", saType);
         if (!foundSender)
        	 dom.AddTopLevelElement("Sender", name);
	        
         // Convert tree back into XML text and return it     
         speechAct = dom.ToXML();
         Log.Info("Speech act: "+speechAct);
         globalContext.Print();
         localContext.Print();
			
         return speechAct;
     }
     
     
     /**
      * Preprocesses a user input utterance.
      * This included converting Umlauts and other non-standard characters, as well as removing 
      * punctuation marks. Furthermore, the utterance is converted completely to lower case.
      * Finally, the spell checker is executed on the utterance.<p>
      * Note that through the spell checking, words may be again converted to upper case.
      * 
      * @param utterance The utterance as originally input by the user
      * @return The transformed utterance after text processing and spell checking
      */
     String preprocessUtterance(String utterance) {
    	 utterance=utterance.replaceAll("ß","ss");
    	 utterance=utterance.replaceAll("ü","ue");
    	 utterance=utterance.replaceAll("ö","oe");
    	 utterance=utterance.replaceAll("ä","ae");
    	 utterance=utterance.replaceAll("Ü","Ue");
    	 utterance=utterance.replaceAll("Ö","Oe");
    	 utterance=utterance.replaceAll("Ä","Ae");
    	 utterance=utterance.replaceAll("'","");
    	 utterance=utterance.replaceAll("[.,:;-]"," ");
    	 utterance=utterance.replaceAll("([a-zA-Z])\\1\\1+","$1");
    	 utterance=utterance.replaceAll("([^a-zA-Z 0-9])"," $1 ");
    	 utterance=utterance.replaceAll("  +"," ");

    	 String lowerCaseUtterance = utterance.toLowerCase(); 

    	 boolean onefalse=false;
   		 String correctedUtterance = "";
   		 StringTokenizer st = new StringTokenizer(lowerCaseUtterance, " ,.!:;?-()<>[]");
   		 String corrected=null;
   		 while(st.hasMoreTokens()){
   			 String word = st.nextToken();
   			 spellcheck.setText(word);
   			 try{
   				 spellcheck.checkSpelling();
   				 corrected = spellcheck.getText();
   				 if (!word.equals(corrected)) {
   					 if (!onefalse) {
   						 Debug("");
   						 onefalse=true;
   					 }
   					 Debug(word+" corrected to "+corrected);
   				 }
   			 } catch(Exception e){
   				 Debug("... not in dictionary!");
   				 e.printStackTrace();
   			 }
   			 correctedUtterance=correctedUtterance + " " + corrected;
   		 }
   		 correctedUtterance = correctedUtterance.trim();
   		 if (onefalse) {
   			 Debug("Original Utterance: "+utterance);
   			 Debug("Corrected Utterance: "+correctedUtterance);
   			 Log.Conversation("Corrected Utterance: "+correctedUtterance);
   		 }

    	 
    	 return correctedUtterance;
     }
     
     
     /**
      * Sets the logging level to monitor the conversation, the detailed
      * language processing or nothing. The log can be found in language_log.txt
      * in the current working directory.
      * <p> The logging level can be set to any of the following values:
      * <PRE>
      * Level.OFF   Log is not produced
      * Level.FINE  Only the utterances (and any errors) are recorded.
      *             This is useful for recording the conversation
      * Level.ALL   Detailed language processing is logged.
      * </PRE>
      * The default value is Level.FINE
      *
      * @param  level - one of the above java.util.logging.Level constants
      * @throws SactException if there's a problem setting the level
      */
     public static void SetLoggingLevel(java.util.logging.Level level)
     throws SactException
     {	
         try
         {
             Log.SetLevel(level);
         }
         catch (Exception e)
         {
            throw new SactException("Problem setting logging level", e);
         }
     }
     
     
     /**
      * Return a list of all the speech act types available to this agent.
      * 
      * @return The list of the speech act types
      */
     public String[] ListLacts() {
    	 TreeSet<String> types = new TreeSet<String>();
    	 String type = "Type";
    		 
    	 try {
    		 BufferedReader br = new BufferedReader(new FileReader(dir+"/templates.tdl"));
    		 String line = br.readLine();
    		 while (line != null) {
    			 if (line.contains(type)) {
    				 int type_start = line.indexOf(type); //+"(value:")+11;
    				 int comment = line.indexOf("#");
    				 if (comment!=-1 && comment<type_start) {
    					 line = br.readLine();
    					 continue;
    				 }
    				 int start = line.indexOf(":",type_start)+1;
    				 if (start < type_start) {
    					 line=br.readLine();
    					 continue;
    				 }
    				 int end = line.indexOf(")",start);
    				 String typevalue = line.substring(start,end);
    				 typevalue = typevalue.trim();
    				 typevalue = typevalue.replaceAll("\"","");
    				 types.add(typevalue);
    			 }
    			 line=br.readLine();
    		 }
    	 } catch (FileNotFoundException fnfe) {
    		Log.Warning("File "+dir+"/templates.tdl not found - no list of LACTs available!");
    		return new String[]{};
    	 } catch (IOException ioe) {
    		 ioe.printStackTrace();
    	 }
    	 return types.toArray(new String[]{});
    	 
     }
     
     /**
      * Output a debugging message.
      * Only active, if {@link LanguageEngine#debug} is set to <code>true</code>
      * @param s The message
      */
     static void Debug(String s) {
    	 if (debug)
    		 System.out.println(s);
     }
     
     
     /**
      * Shut down the Language Engine.
      * This includes stopping the SPIN html interface if {@link LanguageEngine#debug} 
      * is set to <code>true</code> and removing the SPIN object {@link LanguageEngine#spin} 
      * and the SPIN type system {@link LanguageEngine#sts} from the lists 
      * {@link LanguageEngine#spins} and  {@link LanguageEngine#stss} 
      *  if they are not used any more by any other agent.
      * 
      * @param le The LanguageEngine to shut down
      */
     @SuppressWarnings("unchecked")
	public static void shutDown(LanguageEngine le) {
    	 Detach(le);
    	 if (debug)
    		 le.spin.stopHtmlInterface();
    	 boolean isThere=false;
    	 for (LanguageEngine l : (Collection<LanguageEngine>) engines.values()) {
    		 if (l.dir.getAbsolutePath().equals(le.dir.getAbsolutePath())) {
    			 isThere=true;
    			 break;
    		 }
    	 }
    	 if (!isThere) {
    		 synchronized(spins) {
    		 spins.remove(le.dir);
    		 }
    		 synchronized (stss) {
    		 stss.remove(le.dir);
    		 }
    	 }
     }
     
     TopNode getBestResult(TopNode[] results) {
    	 int i;
    	 double score = results[0].getSpinScore();
    	 double time = results[0].getProcessingTime();
    	 TopNode bestresult = results[0];

    	 for (i=1;i<results.length;i++) {
    		 double new_time = results[i].getProcessingTime();
        	 double new_score = results[i].getSpinScore();
    		 if (new_time < time && new_score >= score) {
    			 time = new_time;
    			 score = new_score;
    			 bestresult = results[i];
    		 }
    	 }    		 
    	 return bestresult;
     }
}
