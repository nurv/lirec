
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import FAtiMA.deliberativeLayer.plan.Effect;
import FAtiMA.deliberativeLayer.plan.Step;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.Symbol;
import FAtiMA.wellFormedNames.Unifier;

import java.awt.Dimension;
import java.awt.event.*;

public class UserInterface implements ActionListener {
	JFrame _frame;
	JTextArea textArea;
	JComboBox inputList;
	JComboBox _placeOptions;
	JComboBox _timeOptions;
	WorldTest _world;
	
	private Random _r;
	private static int buffsize = 250;
	
    public UserInterface(WorldTest world_in) {
        
    	_world = world_in;
    	_r = new Random();
    	
        _frame = new JFrame("WorldTest User Interface");
        _frame.getContentPane().setLayout(new BoxLayout(_frame.getContentPane(),BoxLayout.Y_AXIS));
		_frame.setSize(650,650);
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		textArea = new JTextArea(40, 200);
	    JScrollPane scrollPane = new JScrollPane(textArea);
	    scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Create the combo box
        inputList = new JComboBox();
        
        // Create the OK button to confirm input
        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        
        _frame.getContentPane().add(scrollPane);
        
        _timeOptions = new JComboBox();
        _timeOptions.addItem("beforeDinner");
        _timeOptions.addItem("elderArrives");
		_timeOptions.addItem("dinner");
		_timeOptions.addItem("afterDinner");
		_timeOptions.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				_world.ChangeTime(_timeOptions.getSelectedItem().toString());
		    	WriteLine("=> Changing the time: " + _timeOptions.getSelectedItem().toString());
			}
		});
		
		Box timeBox = new Box(BoxLayout.X_AXIS);
        timeBox.add(new JLabel("Time: "));
        timeBox.add(_timeOptions );
		
		_placeOptions = new JComboBox();
		_placeOptions.addItem("Outdoor");
		_placeOptions.addItem("Party");
		_placeOptions.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				_world.ChangePlace(_placeOptions.getSelectedItem().toString());
				WriteLine("=> Changing the place: " + _placeOptions.getSelectedItem().toString());
			}
			
		});
        
        Box placeBox = new Box(BoxLayout.X_AXIS);
        placeBox.add(new JLabel("Place: "));
        placeBox.add(_placeOptions);
        
        _frame.getContentPane().add(timeBox);
        _frame.getContentPane().add(placeBox);
		_frame.getContentPane().add(inputList);
		_frame.getContentPane().add(okButton);
		_frame.setVisible(true);
		
		// Read user input options from a text file
		this.ParseFile(world_in.GetUserOptionsFile());
		
    }
    
    public void WriteLine(String text)
    {
    	textArea.append(text + "\n");
    	System.out.println(text);
    }
    
    public void actionPerformed(ActionEvent evt) {
		String perception = "ACTION-FINISHED " + inputList.getSelectedItem().toString();
		this.WriteLine(inputList.getSelectedItem().toString());
		_world.SendPerceptionToAll(perception);
		StringTokenizer st = new StringTokenizer(inputList.getSelectedItem().toString(), " ");
		if(st.countTokens() > 1)
		{
			String subject = st.nextToken();
			String action =  st.nextToken();
			while(st.hasMoreTokens())
				action = action + " " + st.nextToken();
			this.UpdateActionEffects(subject, ConvertToActionName(action));
		}
    }
    
    private void ParseFile(String name) {
		byte[] buffer = new byte[buffsize];
		String data = "";
		int readCharacters;
		
		try {
			FileInputStream f = new FileInputStream(name);
			while((readCharacters=f.read(buffer))>0) {
				data = data + new String(buffer,0,readCharacters);
			}
			StringTokenizer st = new StringTokenizer(data,"\r\n");
			while(st.hasMoreTokens())
				inputList.addItem(st.nextToken());
				
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
    
    private Name ConvertToActionName(String action)
	{
		StringTokenizer st = new StringTokenizer(action," ");
		String actionName = st.nextToken() + "(";
		while(st.hasMoreTokens())
		{
			actionName += st.nextToken() + ",";
		}
		if(actionName.endsWith(","))
		{
			actionName = actionName.substring(0,actionName.length()-1);
		}
		
		actionName = actionName + ")";
		return Name.ParseName(actionName);
	}
    
    private void UpdateActionEffects(String subject, Name action) 
	{
		ListIterator li = this._world.GetActions().listIterator();
		ArrayList bindings;
		Step s;
		Step gStep;
		
		while(li.hasNext()) 
		{
			s = (Step) li.next();
			bindings = new ArrayList();
			bindings.add(new Substitution(new Symbol("[SELF]"), new Symbol(subject)));
			bindings.add(new Substitution(new Symbol("[AGENT]"), new Symbol(subject)));
			if(Unifier.Unify(s.getName(),action, bindings))
			{
				gStep = (Step) s.clone();
				gStep.MakeGround(bindings);
				PropertiesChanged(gStep.getEffects());
			}
		}		
	}
    
    private void PropertiesChanged(ArrayList effects)
	{
		ListIterator li = effects.listIterator();
		Effect e;
		String msg;

		while(li.hasNext())
		{
			e = (Effect) li.next();
			String name = e.GetEffect().getName().toString();
			if(!name.startsWith("EVENT") && !name.startsWith("SpeechContext"))
			{
				if(e.GetProbability() > _r.nextFloat())
				{
					msg = "PROPERTY-CHANGED " + name + " " + e.GetEffect().GetValue();
					
					_world.GetUserInterface().WriteLine(msg);
					this._world.SendPerceptionToAll(msg);
				}
			}			
		}
	}
	
    public void update() {
        _frame.setVisible(true);
    }
    
    public void dispose() {
    	_frame.dispose();
    }
}
