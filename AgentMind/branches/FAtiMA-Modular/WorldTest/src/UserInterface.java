
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
import javax.swing.JPanel;
import javax.swing.JTextField;

import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.plans.Effect;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.Core.wellFormedNames.Unifier;

import java.awt.event.*;

/**
 * @author Meiyii Lim, Michael Kriegel
 *
 * An GUI to display the details of interaction and for the user to interact
 */

public class UserInterface implements ActionListener {
	JFrame _frame;
	JTextArea textArea;
	JComboBox inputList;
	JComboBox _caseOptions;
	JComboBox _userOptions;
	JComboBox _timeOptions;
	JComboBox _locationOptions;
	JComboBox _infoOptions;
	JComboBox _queryOptions;
	JComboBox _eventOptions;
	JComboBox _attributeOptions;
	JTextField _userSpeech;
	WorldTest _world;
	
	public static final String CASE1 = "Case1";
	public static final String CASE2 = "Case2";
	public static final String CASE3 = "Case3";
	public static final String MORNING = "Morning";
	public static final String AFTERNOON = "Afternoon";
	public static final String LIVINGROOM = "LivingRoom";
	public static final String STUDYROOM = "StudyRoom";
	public static final String KITCHEN = "Kitchen";
	public static final String RECEPTION = "Reception";
	public static final String OFFICE = "Office";
	public static final String COMMONROOM = "CommonRoom";
	public static final String AMY = "Amy";
	public static final String JOHN = "John";
	public static final String PAULIE = "Paulie";
	public static final String LUKE = "Luke";
	public static final String LUKEPAULIE = "LukePaulie";
	public static final String USER = "Amy";
	
	private Random _r;
	private static int buffsize = 250;
	private String _previousUser;
	
	
    public UserInterface(WorldTest world_in, boolean simplifiedVersion) throws IOException {
    

    	Box  caseBox = null, timeBox = null, locationBox = null, infoBox = null,queryBox = null, attributeBox = null;
    	Box  userBox = null;
    	
    	_world = world_in;
    	_r = new Random();
        _frame = new JFrame("WorldTest User Interface");
        _frame.getContentPane().setLayout(new BoxLayout(_frame.getContentPane(),BoxLayout.Y_AXIS));
		_frame.setSize(500,800);
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		textArea = new JTextArea(40, 200);
	    JScrollPane scrollPane = new JScrollPane(textArea);
	    scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    _frame.getContentPane().add(scrollPane);
	    
        // Create the combo box
        inputList = new JComboBox();
        
        
        
        /*
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
		});*/
        
        if(!simplifiedVersion){
        	_caseOptions = new JComboBox();
        	_caseOptions.addItem(UserInterface.CASE1);
    	    _caseOptions.addItem(UserInterface.CASE2);
    	    _caseOptions.addItem(UserInterface.CASE3);
    	    _caseOptions.addActionListener(new ActionListener(){
    			public void actionPerformed(ActionEvent arg0) {
    				_world.ChangeExperimentCase(_caseOptions.getSelectedItem().toString());
    				WriteLine("=> Changing the case: " + _caseOptions.getSelectedItem().toString());
    			}
    		});
    	    
    	    caseBox = new Box(BoxLayout.X_AXIS);
    	    caseBox.add(new JLabel("Case: "));
    	    caseBox.add(_caseOptions );

    	    // Meiyii 06/03/09
            _timeOptions = new JComboBox();
            GregorianCalendar gcal = new GregorianCalendar();
    		int time = gcal.get(Calendar.HOUR_OF_DAY);
    		_timeOptions.addItem(time);
            _timeOptions.addItem(UserInterface.MORNING);
            _timeOptions.addItem(UserInterface.AFTERNOON);
            
    		_timeOptions.addActionListener(new ActionListener(){
    			public void actionPerformed(ActionEvent arg0) {
    				_world.ChangeTime(_timeOptions.getSelectedItem().toString());
    		    	WriteLine("=> Changing the time: " + _timeOptions.getSelectedItem().toString());
    			}
    		});
            
    		timeBox = new Box(BoxLayout.X_AXIS);
            timeBox.add(new JLabel("Time: "));
            timeBox.add(_timeOptions );
            
            _locationOptions = new JComboBox();
            
            // Meiyii 06/04/09
            if(_world.GetScenery().equals("AmyHouse"))       
            {       				      
    	        _locationOptions.addItem(UserInterface.LIVINGROOM);
    	        _locationOptions.addItem(UserInterface.STUDYROOM);
    	        _locationOptions.addItem(UserInterface.KITCHEN);
    	      
    			//_userOptions.addItem(UserInterface.AMY);
    			//_userOptions.addItem(UserInterface.USER);
            }
            else if(_world.GetScenery().equals("Office"))
            {
    	        _locationOptions.addItem(UserInterface.RECEPTION);
    	        _locationOptions.addItem(UserInterface.COMMONROOM);
    	        _locationOptions.addItem(UserInterface.OFFICE);
    	       
    			//_userOptions.addItem(UserInterface.JOHN);
    			//_userOptions.addItem(UserInterface.LUKE);
    			//_userOptions.addItem(UserInterface.PAULIE);
    			//_userOptions.addItem(UserInterface.LUKEPAULIE);		
            }
            else
            {
            	//_userOptions.addItem("Judy");
    			//_userOptions.addItem("David");
            }

            _locationOptions.addActionListener(new ActionListener(){
    			public void actionPerformed(ActionEvent e) {
    				_world.ChangePlace(_locationOptions.getSelectedItem().toString());
    				WriteLine("=> Changing the location: " + _locationOptions.getSelectedItem().toString());
    			}
    		});             
    		
            locationBox = new Box(BoxLayout.X_AXIS);
            locationBox.add(new JLabel("Location: "));
            locationBox.add(_locationOptions);
            
            
            
            // Meiyii 19/11/09
            _infoOptions = new JComboBox();
            _infoOptions.addItem("subject SELF");
            _infoOptions.addItem("subject Amy");
            _infoOptions.addItem("target SELF");
            _infoOptions.addItem("target Amy");
            _infoOptions.addItem("time 17");
            _infoOptions.addItem("location LivingRoom");
            _infoOptions.addItem("action Greet");
            _infoOptions.addItem("action SpeechAct");
            _infoOptions.addItem("desirability 2");
            _infoOptions.addItem("desirability -2");
    		_infoOptions.addActionListener(new ActionListener(){
    			public void actionPerformed(ActionEvent arg0) {
    				_world.AddKnownInfo(_infoOptions.getSelectedItem().toString());
    		    	WriteLine("=> Add known info for SA query: " + _infoOptions.getSelectedItem().toString());
    			}
    		});
    		
    		infoBox = new Box(BoxLayout.X_AXIS);
            infoBox.add(new JLabel("Known info: "));
            infoBox.add(_infoOptions );
            
            _queryOptions = new JComboBox();
            _queryOptions.addItem("ID");
            _queryOptions.addItem("subject");
            _queryOptions.addItem("target");
            _queryOptions.addItem("location");
            _queryOptions.addItem("action");
            _queryOptions.addItem("intention");
            _queryOptions.addItem("status");
            _queryOptions.addItem("object");
            _queryOptions.addItem("events");
    		_queryOptions.addActionListener(new ActionListener(){
    			public void actionPerformed(ActionEvent arg0) {
    		    	WriteLine("=> Change the SA query: " + _queryOptions.getSelectedItem().toString());
    			}
    		});
            
    		queryBox = new Box(BoxLayout.X_AXIS);
            queryBox.add(new JLabel("Query: "));
            queryBox.add(_queryOptions );
                    
            _attributeOptions = new JComboBox();
            _attributeOptions.addItem("subject");
            _attributeOptions.addItem("target");
            _attributeOptions.addItem("location");
            _attributeOptions.addItem("action");
            _attributeOptions.addItem("intention");
            _attributeOptions.addItem("object");
            _attributeOptions.addItem("desirability");
            _attributeOptions.addItem("praiseworthiness");
            _attributeOptions.addItem("time");
    		_attributeOptions.addActionListener(new ActionListener(){
    			public void actionPerformed(ActionEvent arg0) {
    				_world.AddGAttributes(_attributeOptions.getSelectedItem().toString());
    		    	WriteLine("=> Add G attribute: " + _attributeOptions.getSelectedItem().toString());
    			}
    		});
    		attributeBox = new Box(BoxLayout.X_AXIS);
            queryBox.add(new JLabel("G Attributes: "));
            queryBox.add(_attributeOptions );
            
            _userOptions = new JComboBox();    
            _userOptions.addItem(UserInterface.USER);
            
        	_userOptions.addActionListener(new ActionListener(){
    			public void actionPerformed(ActionEvent e) {
    				_world.ChangeUser(_previousUser, _userOptions.getSelectedItem().toString());
    				WriteLine("=> Changing the user: " + _userOptions.getSelectedItem().toString());
    				String userOptionsFile = _world.GetUserOptionsFile() + _userOptions.getSelectedItem().toString() + ".txt";
    				
    				// Read user input options from a text file
    				ParseFile(userOptionsFile);
					_previousUser = _userOptions.getSelectedItem().toString();
    			}
    		});        

            userBox = new Box(BoxLayout.X_AXIS);
            userBox.add(new JLabel("User: "));
            userBox.add(_userOptions);         
            _userSpeech = new JTextField();
        }
       
    	
   
        
        // Create the OK button to confirm input
        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        
        // Button to perform the next step
        JButton stepButton = new JButton("Next Step");;
        stepButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				_world.ReadyForNextStep();
			}
		});
        
        // Button to perform spreading activation
        JButton saButton = new JButton("SpreadActivate");;
        saButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				_world.SAMemory(_queryOptions.getSelectedItem().toString());
			}
		});
        
        // Button to perform compound cue matching
        JButton ccButton = new JButton("CompoundCue");;
        ccButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				_world.CCMemory();
			}
		});
        
        // Button to perform generalisation
        JButton gButton = new JButton("Generalise");;
        gButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				_world.GMemory();
			}
		});
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel,BoxLayout.X_AXIS));
        buttonsPanel.add(okButton);
        
        if(!simplifiedVersion){
            buttonsPanel.add(stepButton);
            //buttonsPanel.add(usButton);
            buttonsPanel.add(saButton);
            buttonsPanel.add(ccButton);
            buttonsPanel.add(gButton);
        }
        
    
        if(!simplifiedVersion){
            _frame.getContentPane().add(_userSpeech);
            _frame.getContentPane().add(caseBox);
            _frame.getContentPane().add(timeBox);
            _frame.getContentPane().add(locationBox);
            _frame.getContentPane().add(userBox);  
            _frame.getContentPane().add(infoBox);
            _frame.getContentPane().add(queryBox);
		}
        
        _frame.getContentPane().add(inputList);
   
        _frame.getContentPane().add(buttonsPanel);
        
		
		//_frame.getContentPane().add(okButton);
		//_frame.getContentPane().add(stepButton);
		_frame.setVisible(true);
		
		String userOptionsFile = "";
		
		
		if(!simplifiedVersion){
			userOptionsFile = _world.GetUserOptionsFile() + _userOptions.getSelectedItem().toString() + ".txt";
			_previousUser = _userOptions.getSelectedItem().toString();
		}else{
			userOptionsFile = _world.GetUserOptionsFile() + ".txt";
		}	
		
		// Read user input options from a text file
		ParseFile(userOptionsFile);
    }
    
    public void WriteLine(String text)
    {
    	textArea.append(text + "\n");
    	System.out.println(text);
    }
    
    public void actionPerformed(ActionEvent evt) {
    	StringTokenizer st = new StringTokenizer(inputList.getSelectedItem().toString(), " ");
		if(st.countTokens() > 1)
		{
			String subject = st.nextToken();
			String action =  st.nextToken();
			while(st.hasMoreTokens())
				action = action + " " + st.nextToken();
			this.UpdateActionEffects(subject, ConvertToActionName(action));
		}
    	
		String perception = "ACTION-FINISHED " + inputList.getSelectedItem().toString();
		this.WriteLine(inputList.getSelectedItem().toString());
		_world.SendPerceptionToAll(perception);
    }
    
    private void ParseFile(String name) {
		byte[] buffer = new byte[buffsize];
		String data = "";
		int readCharacters;
		
		FileInputStream f;
		
		try {
			f = new FileInputStream(name);
			while((readCharacters=f.read(buffer))>0) {
				data = data + new String(buffer,0,readCharacters);
			}
		}catch (FileNotFoundException e) {
			System.out.println(name + "(Error, the system cannot find this options file)");
			System.exit(-1);
		}
		catch (IOException e) {
			System.out.println(name + "(Error while reading this options file)");
			System.exit(-1);
		}
		
		StringTokenizer st = new StringTokenizer(data,"\r\n");
		inputList.removeAllItems();
		while(st.hasMoreTokens())
			inputList.addItem(st.nextToken());
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
		ArrayList<Substitution> bindings;
		Step gStep;
		
		for(Step s : this._world.GetActions())
		{
			bindings = new ArrayList<Substitution>();
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
    
    
    private void PropertiesChanged(ArrayList<Effect> effects)
	{
		Condition c;
		String msg;
		
		for(Effect e : effects)
		{
			c = e.GetEffect();
			String name = c.getName().toString();
			if(!name.startsWith("EVENT") && !name.startsWith("SpeechContext"))
			{
				if(e.GetProbability(null) > _r.nextFloat())
				{
					msg = "PROPERTY-CHANGED " + c.getToM() + " " + name + " " + c.GetValue();
					
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
