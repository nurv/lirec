/*
 * DOMBuilder.java
 * Created on 02 September 2004, 17:44
 *
 *
 */

package Language;

import java.io.*;

import org.xml.sax.SAXException;  
import org.xml.sax.SAXParseException;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.parsers.*;


/**
 * Encapsulates a DOM document built from an XML file.
 * Contains the code necessary to convert XML into a DOM tree, which is
 * stored in the .document member. DOM objects are walked to extract
 * a more useable tree of objects. The DOM can then be deleted. 
 *
 * @author  Steve Grand
 */
public class DOM {
    
    /** The DOM document*/
    public Document document; 
    
    /* Strings representing the values returned by NodeType() */
    private final String[] typenames =
    {
        "",
        "ELEMENT",
        "ATTRIBUTE",
        "TEXT",
        "CDATA",
        "ENTITYREFERENCE",
        "ENTITY",
        "PROCESSINGINSTRUCTION",
        "COMMENT",
        "DOCUMENT",
        "DOCUMENTTYPE",
        "DOCUMENTFRAGMENT",
        "NOTATION"
    };

    
    /**
     * Create a DOM tree from an XML file.
     * 
     * @param filespec The full path and filename
     * @throws SaxParseException if there's something wrong with the XML
     * @throws FileNotFoundException if the file can't be found
     * @throws IOException if there's a problem closing the file
     */
    public DOM(File filespec)
    throws SAXParseException, FileNotFoundException, IOException
    {
        try
        {
            // Create the factory and configure it
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // TODO: DTD's don't allow arbitrary attribute names! Schemas do, but
            // we don't use setValidating() for this - use setSchema()
            // and a schema factory
            ///////////factory.setValidating(true);
            ///////////factory.setNamespaceAware(true);         // Be namespace aware

            // Create the document builder using the factory
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // Use an anonymous inner class to throw SAXParseExceptions
            // on XML validation errors, as required by the JAXP standard
            // These get thrown back to the caller
            builder.setErrorHandler( new MySaxErrorHandler() ); 

            // Parse the document, if possible     
             document = builder.parse(filespec);
        } 
        catch (SAXParseException spe)
        {
            // Parser exceptions are thrown back
            // (app can extract line number etc.)
            throw spe;
        }
        catch (SAXException sxe) 
        {
           // More basic error generated during parsing - treat as fatal
            sxe.printStackTrace();
            System.exit(2);
        } 
        catch (ParserConfigurationException pce) 
        {
           // Parser with specified options can't be built - fatal
            pce.printStackTrace();
            System.exit(3);
        } 
    }
    
     /**
     * Create a DOM tree from an XML string.
     * 
     * @param xml The XML in String form
     * @throws SaxParseException if there's something wrong with the XML
     * @throws IOException if there's a problem closing the stream
     */
    public DOM(String xml)
    throws SAXParseException, IOException
    {
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));

        try
        {
            // Create the factory and configure it
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Can't use a DTD on a stream. Anyway, there will be no DTD 
            // for agent-generated SACTS. So use a non-validating parser
            ///////////factory.setValidating(true);             // Use a validating parser
            ///////////factory.setNamespaceAware(true);         // Be namespace aware

            // Create the document builder using the factory
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // Use an anonymous inner class to throw SAXParseExceptions
            // on XML validation errors, as required by the JAXP standard
            // These get thrown back to the caller
            builder.setErrorHandler( new MySaxErrorHandler() ); 

            // Parse the document, if possible     
             document = builder.parse(stream);
        } 
        catch (SAXParseException spe)
        {Log.Warning(spe.getMessage());
            // Parser exceptions are thrown back
            // (app can extract line number etc.)
            throw spe;
        }
        catch (SAXException sxe) 
        {
           // More basic error generated during parsing - treat as fatal
            sxe.printStackTrace();
            System.exit(2);
        } 
        catch (ParserConfigurationException pce) 
        {
           // Parser with specified options can't be built - fatal
            pce.printStackTrace();
            System.exit(3);
        } 
        
        stream.close();
    }
    
    
    /**
     * Return the first node under the document root node, or else return
     * null if the root node is not named as expected. After constructing a
     * DOM object, call this before starting to read top-level tags.
     * 
     * @param roottag The expected root tag (e.g. "SpeechAct"), or null if the
     * name of the root is immaterial.
     * @return The first Node below the root
     */
    public Node EnterRootNode(String roottag)
    {
        String tag = null;
        Node node = document.getFirstChild();
        do 
        {
            if (node.getNodeType()==Node.ELEMENT_NODE)  // only interested in elements
            {
                tag = node.getNodeName();               // first element node should be our root
            }
            else
            {
                node = node.getNextSibling();           // else on to the next node
            }
        } while (tag == null);                          // stop when we have the root tag
        
        // if the name of the root matters and is correct, point to the
        // first element below the root
        if ((roottag==null)||(roottag.equalsIgnoreCase(tag)))
            return node.getFirstChild();
        
        return null;                                    // root doesn't match expectation
    }
    
    
    /**
     * Convert a nodetype # to a string.
     * 
     * @param nodetype A node type from typenames
     * @return The String representation of the node type
     */
    public String Type(int nodetype)
    {
        return typenames[nodetype];
    }
    
    /**
     * Add a text element just below the root of the tree.
     * 
     * @param tag = the tage name
     * @param content = The text to go between the tags
     */
    public void AddTopLevelElement(String tag, String content)
    {
        Element root = document.getDocumentElement();   // get the SpeechAct
        Element tagElement = document.createElement(tag);        //create the <tag>
        tagElement.appendChild(document.createTextNode(content));     //create the content as child of tag
        root.appendChild(tagElement);                   //add the new element under the root
    }
    
    /**
     * Replace or set the text element inside this tag node.
     * 
     * @param node The DOM node that refers to the tag
     * @param text The text to be added
     * @throws SactException on empty element
     */
    public static void ReplaceTextNode(Node node, String text)
    throws SactException
    {
        // Attempt to replace the existing text
        try
        {
            Node child = node.getFirstChild();
            child.setNodeValue(text);            
        }
        // If fail, add a new child
        catch (DOMException de)
        {
            throw new SactException("SACT text nodes must not be empty (supply a default)");
 
        }
    }
    
    
    /**
     * Given a node that points to a tag, return the element text,
     * having first trimmed off leading and trailing white space.
     * E.g. If the XML contains "<mytag>   some text  </mytag>"
     * and node.getNodeName() is "mytag" then calling GetTextNode(node)
     * will return "some text".
     * 
     * @param node The DOM node that refers to the tag
     * @return The trimmed text belonging to that element
     */
    public static String GetTextNode(Node node)
    {
        // The text node is the child of the tag node
        return node.getFirstChild().getNodeValue().trim();
    }
    
    
    /**
     * Given a node that points to a tag, return one of its attributes.
     * E.g. If the XML contains "<mytag attr="text">
     * and node.getNodeName() is "mytag" then calling GetAttribute(node)
     * will return "text".
     * 
     * @param node The DOM node that refers to the tag
     * @param name The name of the attribute
     * @return The value of the named attribute
     * @throws SactException if no such attribute exists
     */
    public static String GetAttribute(Node node, String name)
    throws SactException
    {
        try
        {
            return node.getAttributes().getNamedItem(name).getNodeValue();
        }
        catch (Exception e)
        {
            throw new SactException("attribute " + name 
                                        + "not found in tag " 
                                        + node.getNodeName());
        }
    }
    
    /**
     * Transform the whole tree back into an XML stream.
     * This used to be possible with node.toString() but now we seem to have
     * to do it by an incredibly tortuous route!
     * 
     * @param stream 
     */
    private void ToXML(Writer stream)
    throws SactException
    {
         try
        {
            // Build a transformer
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            
            // The source is the DOM object and the dest is the stream
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(stream);
            transformer.transform(source, result);
        }
         catch (Exception e)
        {
            // Most likely to be a SACT that's faulty
            throw new SactException("Unable to reconstruct XML string from DOM tree");
        }
     }

    
     /**
     * Transform the whole tree back into XML as a String.
     * 
     * @return The String representation of the XML tree
     */
    public String ToXML()
    throws SactException
    {
        StringWriter stringwriter = new StringWriter();
        ToXML(stringwriter);
        return stringwriter.getBuffer().toString();
        
    }

    /**
     * Save the XML as a file
     * 
     * @param fsp The file to save the XML in
     */
    public void ToXML(File fsp)
    throws SactException, IOException
    {
        FileWriter file = new FileWriter(fsp);
        try
        {
            ToXML(file);
        }
        finally
        {
            file.close();
        }
    }
    
/**
 * Implementation of the error handler interface.
 * 
 * @author Steve Grand
 */
class MySaxErrorHandler implements org.xml.sax.ErrorHandler
{      
    /** ignore fatal errors (an exception is guaranteed) 
     * 
     * @param exception
     * @throws SAXException
     */
    public void fatalError(SAXParseException exception)
    throws SAXException 
    {
    }

    /** treat validation errors as fatal - throw the exception back 
     * @param e
     * @throws SAXParseException
     */
    public void error(SAXParseException e)
    throws SAXParseException
    {
        throw e;
    }

    /** dump warnings before throwing the exception
     * 
     * @param err
     * @throws SAXParseException
     */
    public void warning(SAXParseException err)
    throws SAXParseException
    {
        System.err.println("** Warning"
           + ", line " + err.getLineNumber()
           + ", uri " + err.getSystemId());
        System.err.println("   " + err.getMessage());
    }
}

    
    
    
}
