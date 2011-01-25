package FAtiMA.advancedMemoryComponent;

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;

import org.znerd.xmlenc.LineBreak;
import org.znerd.xmlenc.XMLOutputter;

public class AdvancedMemoryWriter implements Serializable {

	final String encoding = "iso-8859-1";
	private Writer _writer;
	private XMLOutputter _outputter;
	private ArrayList<GER> _gers;
	
	public AdvancedMemoryWriter(ArrayList<GER> gers)
	{
		_outputter = new XMLOutputter();	
		_gers = gers;
	}
	
	public void outputGERtoXML(String file)
	{
		try{
			_writer = new FileWriter(file); // new BufferedWriter(new FileWriter(file));
			//_writer = new OutputStreamWriter(System.out, encoding);
		    _outputter = new XMLOutputter(_writer, encoding);
		    
		    _outputter.startTag("AdvancedMemory");	
		    _outputter.setLineBreak(LineBreak.DOS);
			_outputter.setIndentation("   ");
			
			_outputter.startTag("GERS");	
			for (GER ger: _gers)
			{				
				_outputter.startTag("GER");		
				if(ger.getSubject() != null && !ger.getSubject().equals(""))
					_outputter.attribute("subject", ger.getSubject().toString());	
				if(ger.getAction() != null && !ger.getAction().equals(""))
					_outputter.attribute("action", ger.getAction().toString());	
				if(ger.getIntention() != null && !ger.getIntention().equals(""))
					_outputter.attribute("intention", ger.getIntention().toString());
				if(ger.getTarget() != null && !ger.getTarget().equals(""))
					_outputter.attribute("target", ger.getTarget().toString());	
				if(ger.getObject() != null && !ger.getObject().equals(""))
					_outputter.attribute("object", ger.getObject().toString());	
				if(ger.getDesirability() != null && !ger.getDesirability().equals(""))
					_outputter.attribute("desirability", ger.getDesirability());	
				if(ger.getPraiseworthiness() != null && !ger.getPraiseworthiness().equals(""))
					_outputter.attribute("praiseworthiness", ger.getPraiseworthiness());
				if(ger.getLocation() != null && !ger.getLocation().equals(""))
					_outputter.attribute("location", ger.getLocation().toString());	
				if(ger.getTime() != null && !ger.getTime().equals(""))
					_outputter.attribute("time", ger.getTime().toString());	
				
				_outputter.attribute("coverage", Integer.toString(ger.getCoverage()));
				_outputter.endTag(); //GER
			}
			_outputter.endTag(); //GERS	
			_outputter.whitespace("\n");
			
			_outputter.endTag(); //AdvancedMemory
			_outputter.endDocument(); 
		    _outputter.getWriter().flush();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{		
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		_outputter = new XMLOutputter();	
	}
	
}
