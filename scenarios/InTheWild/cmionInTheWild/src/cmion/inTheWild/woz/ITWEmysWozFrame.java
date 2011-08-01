/*	
    CMION classes for "in the wild" scenario
	Copyright(C) 2009 Heriot Watt University

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

	Authors:  Michael Kriegel 

	Revision History:
  ---
  27/11/2009      Michael Kriegel <mk95@hw.ac.uk>
  First version.
  ---  
*/

package cmion.inTheWild.woz;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.BorderFactory;

import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerListModel;

import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xml.internal.serialize.*;

import cmion.level3.MindAction;


/**
* The main frame for the Woz interface for the in the wild scenario
* 
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class ITWEmysWozFrame extends javax.swing.JFrame implements ActionListener, ListSelectionListener
{

	/** a class to associate names to phone numbers */
	public class Person 
	{
		private String phoneNo;
		
		private String name;
		
		public Person(String phoneNo)
		{
			this.phoneNo = phoneNo;
			this.name = "";
		}

		public String getPhoneNo() {
			return phoneNo;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
		
		public boolean hasName()
		{
			if (!name.trim().equals("")) 
				return true;
			else
				return false;
		}
		
		@Override
		public String toString()
		{
			if (hasName()) 
				return name;
			else
				return phoneNo;
		}	
		
	}

	/** custom table model class for the chat log table */
	private class ChatLogTableModel extends AbstractTableModel 
	{
		private static final long serialVersionUID = 1L;
		
		private LinkedList<ArrayList<Object>> data;
	    private String[] columnNames; 
		
	    public ChatLogTableModel()
	    {
	    	columnNames = new String[] { "Sender", "Message", "Time" };	
	    	data = new LinkedList<ArrayList<Object>>();
	    } 
	    
	    
	    
	    public void addRow(Person sender, String message, String time)
	    {
	    	ArrayList<Object> newRow = new ArrayList<Object>();
	    	newRow.add(sender);
	    	newRow.add(message);
	    	newRow.add(time);
	    	data.addFirst(newRow);
	    	this.fireTableRowsInserted(0, 0);
	    }
	    
	    @Override
	    public int getColumnCount() {
	        return columnNames.length;
	    }

	    @Override
	    public int getRowCount() {
	        return data.size();
	    }

	    @Override
	    public String getColumnName(int col) {
	        return columnNames[col];
	    }

	    @Override
	    public Object getValueAt(int row, int col) {
	        return data.get(row).get(col);
	    }

	}

	{
		//Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	private static final long serialVersionUID = 1L;

	private JPanel jPanel1;
	private JLabel jLabel4;
	private JLabel jLabel5;
	private JLabel jLabel11;
	private JLabel jLabel10;
	private JLabel jLabel9;
	private JComboBox comboBoxReceiver;
	private JLabel jLabel8;
	private JComboBox comboBoxCommon;
	private JLabel lblLeft;
	private JComboBox comboBoxTask;
	private JLabel jLabel20;
	private JComboBox comboBoxDontUnderstand;
	private JLabel jLabel19;
	private JComboBox comboBoxSmallTalk;
	private JLabel jLabel18;
	private JComboBox comboBoxLocations;
	private JLabel jLabel17;
	private JLabel jLabel16;
	private JLabel jLabel15;
	private JSpinner jSpinnerWhichTemplate;
	private JComboBox comboBoxGaze;
	private JLabel jLabel14;
	private JButton btnMigrate;
	private JComboBox comboBoxMigrate;
	private JLabel jLabel13;
	private JLabel lblRight;
	private JTextField txtFieldRight;
	private JTextField txtFieldLeft;
	private JTable jTable1;
	private JScrollPane jScrollPane1;
	private JMenuItem menuItemSave;
	private JMenuItem menuItemLoad;
	private JMenu menuFile;
	private JMenuBar jMenuBar1;
	private JButton btnNewTemplate;
	private JLabel jLabel12;
	private JLabel jLabel6;
	private JTextField txtFieldPhoneBookName;
	private JButton btnSetAlias;
	private JTextField txtFieldPhoneBookNo;
	private JLabel jLabel7;
	private JButton btnSendText;
	private JButton btnTalk;
	private JTextArea jTextAreaTalk;
	private JButton btnShowChoice;
	private JComboBox comboBoxEmotion;
	private JButton btnSetEmotion;
	private JLabel jLabel3;
	private JLabel jLabel2;
	private JLabel jLabel1;

	/** the cmion component that owns this window */
	private ITWEmysWoz parentMindConnector;
	
	/** the table model that stores the table data */
	private ChatLogTableModel tableModel;
	
	/** all known persons indexed by their phone no */
	private HashMap<String,Person> phonebook;
	
	/** the person representing sarah */
	private Person sarahPerson;
	
	private HashMap<String, JComboBox> templateCategories;
	
	/** file chooser for selecting files to save to and load from*/
	final JFileChooser fc;	
	
	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ITWEmysWozFrame inst = new ITWEmysWozFrame(null);
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	public ITWEmysWozFrame(ITWEmysWoz parentMindConnector) 
	{
		super();
		this.parentMindConnector = parentMindConnector;
		tableModel = new ChatLogTableModel();
		phonebook = new HashMap<String,Person>();
		sarahPerson = new Person("----");
		sarahPerson.setName("Sarah");
		fc = new JFileChooser();
 		/*
		FileFilter ff = new FileFilter() 
 		{

			@Override
			public boolean accept(File arg0) {
				if (arg0.getName().endsWith(".xml")) 
					return true;
				else
					return false;
			}

			@Override
			public String getDescription() {
				return "XML Files";
			}
		};
		fc.setFileFilter(ff); */
		
		initGUI();
		
		prepareTemplates();
		prepareTable();
		updateReceiverList();
	}
	
	private void prepareTemplates()
	{
		// associate the combo boxes for categories with the category names
		templateCategories = new HashMap<String,JComboBox>();
		templateCategories.put("Common", comboBoxCommon);
		templateCategories.put("Locations", comboBoxLocations);
		templateCategories.put("SmallTalk", comboBoxSmallTalk);
		templateCategories.put("DontUnderstand", comboBoxDontUnderstand);
		templateCategories.put("Task", comboBoxTask);
		
		// put the category names in the spinner
		SpinnerListModel jSpinnerWhichTemplateModel = 
			new SpinnerListModel(templateCategories.keySet().toArray());
		jSpinnerWhichTemplate.setModel(jSpinnerWhichTemplateModel);

	}
	
	private void prepareTable() 
	{
		// set the table model (associate table data with view)
		jTable1.setModel(tableModel);
		
		// set single item selection for table
		jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		
		// set this class as list selection listener for table
		jTable1.getSelectionModel().addListSelectionListener(this);
		
	}
	
	/** update the combo box for choosing receivers of sent sms */
	private void updateReceiverList()
	{
		ComboBoxModel comboBoxReceiverModel = 
			new DefaultComboBoxModel( phonebook.values().toArray() );
		comboBoxReceiver.setModel(comboBoxReceiverModel);
		if (phonebook.size() == 0)
			this.btnSendText.setEnabled(false);
		else
			this.btnSendText.setEnabled(true);
	}

	private void initGUI() {
		try {
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			this.setTitle("WoZ interface: in the wild");
			{
				jPanel1 = new JPanel();
				getContentPane().add(jPanel1, BorderLayout.CENTER);
				jPanel1.setLayout(null);
				jPanel1.setPreferredSize(new java.awt.Dimension(637, 445));
				jPanel1.setBackground(new java.awt.Color(128,128,128));
				jPanel1.setForeground(new java.awt.Color(0,0,0));
				{
					jLabel1 = new JLabel();
					jPanel1.add(jLabel1);
					jLabel1.setText("chat log (txt messages + Sarah talk):");
					jLabel1.setBounds(10, 7, 226, 14);
					jLabel1.setFont(new java.awt.Font("Tahoma",1,11));
					jLabel1.setForeground(new java.awt.Color(255,255,255));
				}
				{
					jLabel2 = new JLabel();
					jPanel1.add(jLabel2);
					jLabel2.setText("remote control Sarah:");
					jLabel2.setFont(new java.awt.Font("Tahoma",1,11));
					jLabel2.setBounds(10, 143, 169, 14);
					jLabel2.setForeground(new java.awt.Color(255,255,255));
				}
				{
					jLabel3 = new JLabel();
					jPanel1.add(jLabel3);
					jLabel3.setText("emotion:");
					jLabel3.setBounds(29, 166, 55, 14);
					jLabel3.setForeground(new java.awt.Color(255,255,255));
				}
				{
					btnSetEmotion = new JButton();
					jPanel1.add(btnSetEmotion);
					btnSetEmotion.setText("set");
					btnSetEmotion.setBounds(209, 160, 55, 23);
					btnSetEmotion.addActionListener(this);
				}
				{
					jLabel4 = new JLabel();
					jPanel1.add(jLabel4);
					jLabel4.setText("display binary choice");
					jLabel4.setBounds(16, 225, 182, 14);
					jLabel4.setForeground(new java.awt.Color(255,255,255));
				}
				{
					ComboBoxModel comboBoxEmotionModel = 
						new DefaultComboBoxModel(
								new String[] { "neutral", "joy", "sadness", "anger", "surprise"});
					comboBoxEmotion = new JComboBox();
					jPanel1.add(comboBoxEmotion);
					comboBoxEmotion.setModel(comboBoxEmotionModel);
					comboBoxEmotion.setBounds(83, 161, 120, 23);
				}
				{
					btnShowChoice = new JButton();
					jPanel1.add(btnShowChoice);
					btnShowChoice.setText("display");
					btnShowChoice.setBounds(418, 221, 83, 22);
					btnShowChoice.addActionListener(this);
				}
				{
					jLabel5 = new JLabel();
					jPanel1.add(jLabel5);
					jLabel5.setText("talk:");
					jLabel5.setBounds(6, 276, 29, 14);
					jLabel5.setForeground(new java.awt.Color(255,255,255));
				}
				{
					jTextAreaTalk = new JTextArea();
					jPanel1.add(jTextAreaTalk);
					jTextAreaTalk.setBounds(43, 250, 417, 68);
					jTextAreaTalk.setLineWrap(true);
					jTextAreaTalk.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
					jTextAreaTalk.getActionMap().put(jTextAreaTalk.getInputMap().get(KeyStroke.getKeyStroke("ENTER")), new TextAreaPressEnterAction());
				}
				{
					btnTalk = new JButton();
					jPanel1.add(btnTalk);
					btnTalk.setText("talk as Sarah");
					btnTalk.setBounds(514, 234, 123, 23);
					btnTalk.addActionListener(this);
				}
				{
					btnSendText = new JButton();
					jPanel1.add(btnSendText);
					btnSendText.setText("send as sms");
					btnSendText.setBounds(514, 263, 121, 23);
					btnSendText.addActionListener(this);
				}
				{
					comboBoxReceiver = new JComboBox();
					jPanel1.add(comboBoxReceiver);
					comboBoxReceiver.setBounds(514, 287, 120, 23);
				}
				{
					jLabel7 = new JLabel();
					jPanel1.add(jLabel7);
					jLabel7.setText("to");
					jLabel7.setBounds(493, 292, 21, 14);
					jLabel7.setForeground(new java.awt.Color(255,255,255));
				}
				{
					comboBoxCommon = new JComboBox();
					jPanel1.add(comboBoxCommon);
					comboBoxCommon.setBounds(6, 374, 628, 23);
					comboBoxCommon.addActionListener(this);
				}
				{
					jLabel8 = new JLabel();
					jPanel1.add(jLabel8);
					jLabel8.setText("type above or select a template below");
					jLabel8.setBounds(43, 324, 277, 14);
					jLabel8.setForeground(new java.awt.Color(255,255,255));
				}
				{
					jLabel9 = new JLabel();
					jPanel1.add(jLabel9);
					jLabel9.setText("phone book:");
					jLabel9.setFont(new java.awt.Font("Tahoma",1,11));
					jLabel9.setBounds(10, 89, 82, 14);
					jLabel9.setForeground(new java.awt.Color(255,255,255));
				}
				{
					jLabel10 = new JLabel();
					jPanel1.add(jLabel10);
					jLabel10.setText("phone no:");
					jLabel10.setBounds(54, 114, 58, 14);
					jLabel10.setForeground(new java.awt.Color(255,255,255));
				}
				{
					jLabel11 = new JLabel();
					jPanel1.add(jLabel11);
					jLabel11.setText("person name:");
					jLabel11.setBounds(296, 114, 80, 14);
					jLabel11.setForeground(new java.awt.Color(255,255,255));
				}
				{
					txtFieldPhoneBookNo = new JTextField();
					jPanel1.add(txtFieldPhoneBookNo);
					txtFieldPhoneBookNo.setBounds(112, 109, 165, 23);
					txtFieldPhoneBookNo.setEditable(false);
					txtFieldPhoneBookNo.setEnabled(false);
				}
				{
					btnSetAlias = new JButton();
					jPanel1.add(btnSetAlias);
					btnSetAlias.setText("set");
					btnSetAlias.setBounds(564, 110, 52, 23);
					btnSetAlias.addActionListener(this);
				}
				{
					txtFieldPhoneBookName = new JTextField();
					jPanel1.add(txtFieldPhoneBookName);
					txtFieldPhoneBookName.setBounds(382, 109, 176, 23);
				}
				{
					jLabel6 = new JLabel();
					jPanel1.add(jLabel6);
					jLabel6.setText("or");
					jLabel6.setBounds(493, 267, 21, 14);
					jLabel6.setForeground(new java.awt.Color(255,255,255));
				}
				{
					jLabel12 = new JLabel();
					jPanel1.add(jLabel12);
					jLabel12.setText("or");
					jLabel12.setForeground(new java.awt.Color(255,255,255));
					jLabel12.setBounds(493, 318, 21, 14);
				}
				{
					btnNewTemplate = new JButton();
					jPanel1.add(btnNewTemplate);
					btnNewTemplate.setText("add template");
					btnNewTemplate.setBounds(514, 314, 117, 24);
					btnNewTemplate.addActionListener(this);
				}
				{
					jScrollPane1 = new JScrollPane();
					jPanel1.add(jScrollPane1);
					jScrollPane1.setBounds(10, 21, 605, 63);
					{
						TableModel jTable1Model = 
							new DefaultTableModel(
									new String[][] { { "One", "Two" }, { "Three", "Four" } },
									new String[] { "Column 1", "Column 2" });
						jTable1 = new JTable();
						jScrollPane1.setViewportView(jTable1);
						jTable1.setModel(jTable1Model);
					}
				}
				{
					lblLeft = new JLabel();
					jPanel1.add(lblLeft);
					lblLeft.setText("left:");
					lblLeft.setBounds(157, 224, 19, 16);
					lblLeft.setForeground(new java.awt.Color(255,255,255));
				}
				{
					txtFieldLeft = new JTextField();
					jPanel1.add(txtFieldLeft);
					txtFieldLeft.setBounds(180, 218, 91, 28);
				}
				{
					txtFieldRight = new JTextField();
					jPanel1.add(txtFieldRight);
					txtFieldRight.setBounds(314, 218, 91, 28);
				}
				{
					lblRight = new JLabel();
					jPanel1.add(lblRight);
					lblRight.setText("right:");
					lblRight.setForeground(new java.awt.Color(255,255,255));
					lblRight.setBounds(280, 224, 28, 16);
				}
				{
					jLabel13 = new JLabel();
					jPanel1.add(jLabel13);
					jLabel13.setText("migrate to:");
					jLabel13.setForeground(new java.awt.Color(255,255,255));
					jLabel13.setBounds(322, 161, 60, 25);
				}
				{
					ComboBoxModel jComboBox1Model = 
						new DefaultComboBoxModel(
								new String[] { "Robot", "Phone" });
					comboBoxMigrate = new JComboBox();
					jPanel1.add(comboBoxMigrate);
					comboBoxMigrate.setModel(jComboBox1Model);
					comboBoxMigrate.setBounds(388, 161, 120, 23);
				}
				{
					btnMigrate = new JButton();
					jPanel1.add(btnMigrate);
					btnMigrate.setText("migrate");
					btnMigrate.setBounds(520, 161, 83, 22);
					btnMigrate.addActionListener(this);
				}
				{
					jLabel14 = new JLabel();
					jPanel1.add(jLabel14);
					jLabel14.setText("gaze at:");
					jLabel14.setForeground(new java.awt.Color(255,255,255));
					jLabel14.setBounds(34, 196, 55, 14);
				}
				{
					ComboBoxModel comboBoxGazeModel = 
						new DefaultComboBoxModel(
								new String[] { "user", "D1", "D2", "D3", "D4", "D5", "D6" });
					comboBoxGaze = new JComboBox();
					jPanel1.add(comboBoxGaze);
					comboBoxGaze.setModel(comboBoxGazeModel);
					comboBoxGaze.setBounds(86, 191, 150, 25);
					comboBoxGaze.addActionListener(this);
				}
				{
					jSpinnerWhichTemplate = new JSpinner();
					jPanel1.add(jSpinnerWhichTemplate);
					jSpinnerWhichTemplate.setBounds(465, 341, 167, 28);
				}
				{
					jLabel15 = new JLabel();
					jPanel1.add(jLabel15);
					jLabel15.setText("to");
					jLabel15.setForeground(new java.awt.Color(255,255,255));
					jLabel15.setBounds(446, 349, 21, 14);
				}
				{
					jLabel16 = new JLabel();
					jPanel1.add(jLabel16);
					jLabel16.setText("Common Utterances:");
					jLabel16.setBounds(9, 354, 143, 16);
					jLabel16.setFont(new java.awt.Font("SansSerif",1,12));
					jLabel16.setForeground(new java.awt.Color(255,255,255));
				}
				{
					jLabel17 = new JLabel();
					jPanel1.add(jLabel17);
					jLabel17.setText("Refering to Office Locations:");
					jLabel17.setFont(new java.awt.Font("SansSerif",1,12));
					jLabel17.setForeground(new java.awt.Color(255,255,255));
					jLabel17.setBounds(11, 400, 175, 16);
				}
				{
					comboBoxLocations = new JComboBox();
					jPanel1.add(comboBoxLocations);
					comboBoxLocations.setBounds(6, 421, 628, 23);
					comboBoxLocations.addActionListener(this);
				}
				{
					jLabel18 = new JLabel();
					jPanel1.add(jLabel18);
					jLabel18.setText("Small Talk:");
					jLabel18.setFont(new java.awt.Font("SansSerif",1,12));
					jLabel18.setForeground(new java.awt.Color(255,255,255));
					jLabel18.setBounds(12, 450, 175, 16);
				}
				{
					comboBoxSmallTalk = new JComboBox();
					jPanel1.add(comboBoxSmallTalk);
					comboBoxSmallTalk.setBounds(5, 472, 628, 23);
					comboBoxSmallTalk.addActionListener(this);
				}
				{
					jLabel19 = new JLabel();
					jPanel1.add(jLabel19);
					jLabel19.setText("I Don't Understand:");
					jLabel19.setFont(new java.awt.Font("SansSerif",1,12));
					jLabel19.setForeground(new java.awt.Color(255,255,255));
					jLabel19.setBounds(11, 504, 175, 16);
				}
				{
					comboBoxDontUnderstand = new JComboBox();
					jPanel1.add(comboBoxDontUnderstand);
					comboBoxDontUnderstand.setBounds(6, 525, 628, 23);
					comboBoxDontUnderstand.addActionListener(this);
				}
				{
					jLabel20 = new JLabel();
					jPanel1.add(jLabel20);
					jLabel20.setText("Task Related:");
					jLabel20.setFont(new java.awt.Font("SansSerif",1,12));
					jLabel20.setForeground(new java.awt.Color(255,255,255));
					jLabel20.setBounds(11, 554, 175, 16);
				}
				{
					comboBoxTask = new JComboBox();
					jPanel1.add(comboBoxTask);
					comboBoxTask.setBounds(7, 575, 628, 23);
					comboBoxTask.addActionListener(this);
				}
			}
			{
				jMenuBar1 = new JMenuBar();
				setJMenuBar(jMenuBar1);
				{
					menuFile = new JMenu();
					jMenuBar1.add(menuFile);
					menuFile.setText("File");
					{
						menuItemLoad = new JMenuItem();
						menuFile.add(menuItemLoad);
						menuItemLoad.setText("Load");
						menuItemLoad.addActionListener(this);
					}
					{
						menuItemSave = new JMenuItem();
						menuFile.add(menuItemSave);
						menuItemSave.setText("Save");
						menuItemSave.addActionListener(this);
					}
				}
			}
			pack();
			this.setSize(645, 680);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** new incoming sms */
	public void newLogLine(String sender, String text)
	{
		// obtain current time
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
		
    	Person person = null;
	    
	    if (sender.equals("Greta"))
	    	person = sarahPerson;
    	else
	    {
	    	// user action in the log, check if user is already in the phonebook
	    	if (phonebook.containsKey(sender))
	    		person = phonebook.get(sender);
	    	else
	    	{
	    		person = new Person(sender);
	    		phonebook.put(sender, person);
	    		updateReceiverList();
	    	}   		
	    }
	    tableModel.addRow(person, text, sdf.format(cal.getTime()));
	}
	
	
	/** action listener method */
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		ArrayList<String> parameters = new ArrayList<String>();
		if (arg0.getSource() == btnTalk)
		{
			// first parameter: text to speak
			parameters.add(jTextAreaTalk.getText());
			MindAction mindAction = new MindAction("Sarah", "wozTalk", parameters);
			parentMindConnector.newAction(mindAction);			
		}
		else if (arg0.getSource() == btnShowChoice)
		{
			parameters.add(txtFieldLeft.getText());
			parameters.add(txtFieldRight.getText());			
			MindAction mindAction = new MindAction("Sarah", "wozQuestion", parameters);
			parentMindConnector.newAction(mindAction);
		}		
		else if (arg0.getSource() == btnSetEmotion)
		{
			// first parameter: emotion to display
			parameters.add(comboBoxEmotion.getSelectedItem().toString());
			MindAction mindAction = new MindAction("Sarah", "wozEmotion", parameters);
			parentMindConnector.newAction(mindAction);		
		}	
		else if (arg0.getSource() == btnSetAlias)
		{
			// obtain selected person
			Person person = phonebook.get(this.txtFieldPhoneBookNo.getText());
			if (person!=null)
			{
				person.setName(this.txtFieldPhoneBookName.getText().trim());
				updateReceiverList();
				tableModel.fireTableDataChanged();		
			}
		}
		else if (templateCategories.values().contains(arg0.getSource()))
		{
			//quicker gui, directly talk
			JComboBox selectedBox = (JComboBox) arg0.getSource();
			String s = selectedBox.getSelectedItem().toString();
			if (! s.trim().equals(""))
			{
				parameters.add(s);
				MindAction mindAction = new MindAction("Sarah", "wozTalk", parameters);
				parentMindConnector.newAction(mindAction);			
			}
			//this.jTextAreaTalk.setText(selectedBox.getSelectedItem().toString());
		}
		else if (arg0.getSource() == btnNewTemplate)
		{
			// determine which combo box to add the template to, by looking at the spinner
			JComboBox whichBox = null;
			if (templateCategories.containsKey(jSpinnerWhichTemplate.getValue()))
					whichBox = templateCategories.get(jSpinnerWhichTemplate.getValue());
			if (whichBox!=null)
				whichBox.addItem(jTextAreaTalk.getText());
		}
		else if (arg0.getSource() == menuItemSave)
		{
			saveData();
		}
		else if (arg0.getSource() == menuItemLoad)
		{
			loadData();
		}
		else if (arg0.getSource() == btnSendText)
		{
			// check if a valid receiver was selected
			if (comboBoxReceiver.getSelectedItem() instanceof Person)
			{
				Person receiver = (Person) comboBoxReceiver.getSelectedItem();
				// first parameter: phone no of receiver
				parameters.add(receiver.getPhoneNo());
				// second parameter: text to send
				parameters.add(jTextAreaTalk.getText());
			
				MindAction mindAction = new MindAction("Sarah", "wozSendSMS", parameters);
				parentMindConnector.newAction(mindAction);		
			}
		}
		else if (arg0.getSource() == btnMigrate)
		{
			if (comboBoxMigrate.getSelectedItem() != null)
			{
				String target = comboBoxMigrate.getSelectedItem().toString();
				parameters.add(target);
				MindAction mindAction = new MindAction("Sarah", "migrate", parameters);
				parentMindConnector.newAction(mindAction);		
			}			
		}
		else if (arg0.getSource() == comboBoxGaze)
		{
			String gazeTarget = comboBoxGaze.getSelectedItem().toString();
			parameters.add(gazeTarget);
			MindAction mindAction = new MindAction("Sarah", "wozGaze", parameters);
			parentMindConnector.newAction(mindAction);		
		}
	}

	// load log, phone book, and sentence templates from a file	
	private void loadData() {
		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		{
			File file = fc.getSelectedFile();
			try
			{

				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder;
				docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(file);

				// normalize text representation
				doc.getDocumentElement().normalize();
				
				// empty phonebook
				phonebook.clear();
				
				// read phonebook entries
				NodeList phonebooklist = doc.getElementsByTagName("phonebook");		
				for (int i=0; i<phonebooklist.getLength(); i++)
				{
					NamedNodeMap attribs = phonebooklist.item(i).getAttributes();
					String phoneNo = attribs.getNamedItem("no").getNodeValue();
					String name = attribs.getNamedItem("name").getNodeValue();
					Person p = new Person(phoneNo);
					p.setName(name);
					phonebook.put(phoneNo, p);
				}
				
				// empty templates
				for (JComboBox comboBox : templateCategories.values())
					comboBox.removeAllItems();
				
				// read templates
				NodeList templatelist = doc.getElementsByTagName("template");
				for (int i=0; i<templatelist.getLength(); i++)
				{
					String template = templatelist.item(i).getTextContent();
					String catName = templatelist.item(i).getAttributes().getNamedItem("category").getNodeValue();
					JComboBox comboBox = templateCategories.get(catName);
					comboBox.addItem(template);
				}
				
				// read log lines
				NodeList loglinelist = doc.getElementsByTagName("logline");		
				for (int i=0; i<loglinelist.getLength(); i++)
				{
					NamedNodeMap attribs = loglinelist.item(i).getAttributes();
					String who = attribs.getNamedItem("who").getNodeValue();
					String what = attribs.getNamedItem("what").getNodeValue();
					String when = attribs.getNamedItem("when").getNodeValue();
					Person p;
					if (who.equals("----")) p = sarahPerson;
					else if (phonebook.containsKey(who)) p = phonebook.get(who);
					else
					{
						p = new Person("who");
						phonebook.put(who, p);
					}
					tableModel.addRow(p, what, when);			
				}
				
				// update list of sms receivers
				updateReceiverList();
			
			} 
			catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			
		}		
	}

	// save log, phone book, and sentence templates to a file
	private void saveData() 
	{
		if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
		{
			File file = fc.getSelectedFile();
			try {
				
				// create dom
				Document xmldoc= new DocumentImpl();
				// Root element.
				Element root = xmldoc.createElement("wozdata");
				
				// write phonebook entries
				for (String phoneNo : phonebook.keySet())
				{
					String name = phonebook.get(phoneNo).getName();
					Element e = xmldoc.createElement("phonebook");
					e.setAttribute("no", phoneNo);
					e.setAttribute("name", name);
					root.appendChild(e);
				}
				
				// write templates				
				for (String catName : templateCategories.keySet())
				{
					JComboBox comboBox = templateCategories.get(catName);
					for (int i=0; i<comboBox.getItemCount(); i++)
					{
						String template = comboBox.getItemAt(i).toString();
						Element e = xmldoc.createElement("template");
						Text n = xmldoc.createTextNode(template);
						e.appendChild(n);
						e.setAttribute("category", catName);
						root.appendChild(e);
					}
				}
									
				// write log lines
				for (int i=tableModel.getRowCount()-1; i >= 0; i--)
				{
					Element e = xmldoc.createElement("logline");
					String who = ((Person)tableModel.getValueAt(i, 0)).getPhoneNo();
					String what = tableModel.getValueAt(i, 1).toString();
					String when = tableModel.getValueAt(i, 2).toString();				
					e.setAttribute("who", who);
					e.setAttribute("what", what);
					e.setAttribute("when", when);					
					root.appendChild(e);
				}			
				
				xmldoc.appendChild(root);
				
				// serialize to xml
				FileOutputStream fos = new FileOutputStream(file);
				OutputFormat of = new OutputFormat("XML","ISO-8859-1",true);
				of.setIndent(1);
				of.setIndenting(true);
				XMLSerializer serializer = new XMLSerializer(fos,of);
				// As a DOM Serializer
				serializer.asDOMSerializer();
				serializer.serialize(xmldoc.getDocumentElement() );
				fos.close();				
			
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			
			
		}
		
	}

	/** invoked when user selects an item in the table */
	@Override
	public void valueChanged(ListSelectionEvent arg0) 
	{
		if (jTable1.getSelectedRow()!=-1)
		{
			int row = jTable1.getSelectedRow();
			if (tableModel.getValueAt(row, 0) instanceof Person)
			{
				Person selectedPerson = (Person) tableModel.getValueAt(row, 0);
				if (selectedPerson != sarahPerson)
				{
					this.txtFieldPhoneBookName.setText(selectedPerson.getName());
					this.txtFieldPhoneBookNo.setText(selectedPerson.getPhoneNo());
				}
			}
		} 
		
	}

	// Action for pressing enter in the text area
	private class TextAreaPressEnterAction extends AbstractAction
	{

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) 
		{
			ArrayList<String> parameters = new ArrayList<String>();			
			parameters.add(jTextAreaTalk.getText());
			MindAction mindAction = new MindAction("Sarah", "wozTalk", parameters);
			parentMindConnector.newAction(mindAction);	
			jTextAreaTalk.setText("");
		}
		
		
	}
	
}
