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

package cmion.TeamBuddy.gui;
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

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

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
public class TBWozFrame extends javax.swing.JFrame implements ActionListener, ListSelectionListener
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
	private JComboBox comboBoxPrewritten;
	private JLabel lblTarget;
	private JTextField txtFieldTarget;
	private JTable jTable1;
	private JScrollPane jScrollPane1;
	private JMenuItem menuItemSave;
	private JMenuItem menuItemLoad;
	private JMenu menuFile;
	private JMenuBar jMenuBar1;
	private JButton btnNewTemplate;
	private JLabel jLabel12;
	private JLabel jLabel13;
	private JLabel jLabel6;
	private JTextField txtFieldPhoneBookName;
	private JButton btnSetAlias;
	private JTextField txtFieldPhoneBookNo;
	private JLabel jLabel7;
	private JButton btnSendText;
	private JButton btnTalk;
	private JTextArea jTextAreaTalk;
	private JButton btnNavigate;
	private JComboBox comboBoxEmotion;
	private JComboBox comboBoxMigrationTarget;	
	private JButton btnSetEmotion;
	private JButton btnMigrate;
	private JLabel jLabel3;
	private JLabel jLabel2;
	private JLabel jLabel1;

	/** the cmion component that owns this window */
	private TBWoz parentMindConnector;
	
	/** the table model that stores the table data */
	private ChatLogTableModel tableModel;
	
	/** all known persons indexed by their phone no */
	private HashMap<String,Person> phonebook;
	
	/** the person representing sarah */
	private Person sarahPerson;
	
	/** file chooser for selecting files to save to and load from*/
	final JFileChooser fc;	
	
	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TBWozFrame inst = new TBWozFrame(null);
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	public TBWozFrame(TBWoz parentMindConnector) 
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
		prepareTable();
		updateReceiverList();
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
			this.setTitle("WoZ interface: team buddy");
			{
				jPanel1 = new JPanel();
				getContentPane().add(jPanel1, BorderLayout.CENTER);
				jPanel1.setLayout(null);
				jPanel1.setPreferredSize(new java.awt.Dimension(637, 445));
				jPanel1.setBackground(new java.awt.Color(128,128,128));
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
					jLabel2.setBounds(10, 249, 169, 14);
					jLabel2.setForeground(new java.awt.Color(255,255,255));
				}
				{
					jLabel3 = new JLabel();
					jPanel1.add(jLabel3);
					jLabel3.setText("emotion:");
					jLabel3.setBounds(29, 272, 55, 14);
					jLabel3.setForeground(new java.awt.Color(255,255,255));
				}
				{
					btnSetEmotion = new JButton();
					jPanel1.add(btnSetEmotion);
					btnSetEmotion.setText("set");
					btnSetEmotion.setBounds(209, 266, 55, 23);
					btnSetEmotion.addActionListener(this);
				}
				{
					jLabel4 = new JLabel();
					jPanel1.add(jLabel4);
					jLabel4.setText("Navigate");
					jLabel4.setBounds(16, 301, 65, 14);
					jLabel4.setForeground(new java.awt.Color(255,255,255));
				}
				{
					jLabel13 = new JLabel();
					jPanel1.add(jLabel13);
					jLabel13.setText("migrate to");
					jLabel13.setBounds(270, 301, 182, 14);
					jLabel13.setForeground(new java.awt.Color(255,255,255));
				}
				{
					ComboBoxModel comboBoxEmotionModel = 
						new DefaultComboBoxModel(
								new String[] { "neutral", "anger", "fear", "joy", "surprise", "sadness","sleeping"});
					comboBoxEmotion = new JComboBox();
					jPanel1.add(comboBoxEmotion);
					comboBoxEmotion.setModel(comboBoxEmotionModel);
					comboBoxEmotion.setBounds(83, 267, 120, 23);
				}
				{
					ComboBoxModel comboBoxMigrateModel = 
						new DefaultComboBoxModel(
								new String[] { "Screen", "Phone"});
					comboBoxMigrationTarget = new JComboBox();
					jPanel1.add(comboBoxMigrationTarget);
					comboBoxMigrationTarget.setModel(comboBoxMigrateModel);
					comboBoxMigrationTarget.setBounds(332, 297, 88, 23);
				}
				{
					btnMigrate = new JButton();
					jPanel1.add(btnMigrate);
					btnMigrate.setText("migrate");
					btnMigrate.setBounds(423, 297, 83, 22);
					btnMigrate.addActionListener(this);
				}
				{
					btnNavigate = new JButton();
					jPanel1.add(btnNavigate);
					btnNavigate.setText("go");
					btnNavigate.setBounds(180, 297, 56, 22);
					btnNavigate.addActionListener(this);
				}
				{
					jLabel5 = new JLabel();
					jPanel1.add(jLabel5);
					jLabel5.setText("talk:");
					jLabel5.setBounds(6, 351, 29, 14);
					jLabel5.setForeground(new java.awt.Color(255,255,255));
				}
				{
					jTextAreaTalk = new JTextArea();
					jPanel1.add(jTextAreaTalk);
					jTextAreaTalk.setBounds(43, 325, 417, 68);
					jTextAreaTalk.setLineWrap(true);
					jTextAreaTalk.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
				}
				{
					btnTalk = new JButton();
					jPanel1.add(btnTalk);
					btnTalk.setText("talk as Sarah");
					btnTalk.setBounds(514, 309, 123, 23);
					btnTalk.addActionListener(this);
				}
				{
					btnSendText = new JButton();
					jPanel1.add(btnSendText);
					btnSendText.setText("send as sms");
					btnSendText.setBounds(514, 338, 121, 23);
					btnSendText.addActionListener(this);
				}
				{
					comboBoxReceiver = new JComboBox();
					jPanel1.add(comboBoxReceiver);
					comboBoxReceiver.setBounds(514, 359, 120, 23);
				}
				{
					jLabel7 = new JLabel();
					jPanel1.add(jLabel7);
					jLabel7.setText("to");
					jLabel7.setBounds(493, 363, 21, 14);
					jLabel7.setForeground(new java.awt.Color(255,255,255));
				}
				{
					comboBoxPrewritten = new JComboBox();
					jPanel1.add(comboBoxPrewritten);
					comboBoxPrewritten.setBounds(6, 416, 628, 23);
					comboBoxPrewritten.addActionListener(this);
				}
				{
					jLabel8 = new JLabel();
					jPanel1.add(jLabel8);
					jLabel8.setText("type above or select a template below");
					jLabel8.setBounds(43, 399, 277, 14);
					jLabel8.setForeground(new java.awt.Color(255,255,255));
				}
				{
					jLabel9 = new JLabel();
					jPanel1.add(jLabel9);
					jLabel9.setText("phone book:");
					jLabel9.setFont(new java.awt.Font("Tahoma",1,11));
					jLabel9.setBounds(10, 195, 82, 14);
					jLabel9.setForeground(new java.awt.Color(255,255,255));
				}
				{
					jLabel10 = new JLabel();
					jPanel1.add(jLabel10);
					jLabel10.setText("phone no:");
					jLabel10.setBounds(54, 220, 58, 14);
					jLabel10.setForeground(new java.awt.Color(255,255,255));
				}
				{
					jLabel11 = new JLabel();
					jPanel1.add(jLabel11);
					jLabel11.setText("person name:");
					jLabel11.setBounds(296, 220, 80, 14);
					jLabel11.setForeground(new java.awt.Color(255,255,255));
				}
				{
					txtFieldPhoneBookNo = new JTextField();
					jPanel1.add(txtFieldPhoneBookNo);
					txtFieldPhoneBookNo.setBounds(112, 215, 165, 23);
					txtFieldPhoneBookNo.setEditable(false);
					txtFieldPhoneBookNo.setEnabled(false);
				}
				{
					btnSetAlias = new JButton();
					jPanel1.add(btnSetAlias);
					btnSetAlias.setText("set");
					btnSetAlias.setBounds(564, 216, 52, 23);
					btnSetAlias.addActionListener(this);
				}
				{
					txtFieldPhoneBookName = new JTextField();
					jPanel1.add(txtFieldPhoneBookName);
					txtFieldPhoneBookName.setBounds(382, 215, 176, 23);
				}
				{
					jLabel6 = new JLabel();
					jPanel1.add(jLabel6);
					jLabel6.setText("or");
					jLabel6.setBounds(493, 342, 21, 14);
					jLabel6.setForeground(new java.awt.Color(255,255,255));
				}
				{
					jLabel12 = new JLabel();
					jPanel1.add(jLabel12);
					jLabel12.setText("or");
					jLabel12.setForeground(new java.awt.Color(255,255,255));
					jLabel12.setBounds(493, 393, 21, 14);
				}
				{
					btnNewTemplate = new JButton();
					jPanel1.add(btnNewTemplate);
					btnNewTemplate.setText("add template");
					btnNewTemplate.setBounds(514, 389, 117, 24);
					btnNewTemplate.setSize(120, 23);
					btnNewTemplate.addActionListener(this);
				}
				{
					jScrollPane1 = new JScrollPane();
					jPanel1.add(jScrollPane1);
					jScrollPane1.setBounds(10, 21, 605, 174);
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
					lblTarget = new JLabel();
					jPanel1.add(lblTarget);
					lblTarget.setText("target:");
					lblTarget.setBounds(73, 300, 39, 16);
					lblTarget.setForeground(new java.awt.Color(255,255,255));
				}
				{
					txtFieldTarget = new JTextField();
					jPanel1.add(txtFieldTarget);
					txtFieldTarget.setBounds(114, 295, 61, 28);
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
		else if (arg0.getSource() == btnNavigate)
		{
			parameters.add(txtFieldTarget.getText());
			MindAction mindAction = new MindAction("Sarah", "wozNavigate", parameters);
			parentMindConnector.newAction(mindAction);
		}		
		else if (arg0.getSource() == btnMigrate)
		{
			Object target = comboBoxMigrationTarget.getSelectedItem();
			if (target!=null)
			{
				parameters.add(target.toString());
				MindAction mindAction = new MindAction("Sarah", "migrate", parameters);
				parentMindConnector.newAction(mindAction);
			}
			else 
			{
				JOptionPane.showMessageDialog(this, "Please specify a migration target");
			}
		}		
		else if (arg0.getSource() == btnSetEmotion)
		{
			// first parameter: emotion to display
			parameters.add(Integer.toString(comboBoxEmotion.getSelectedIndex()));
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
		else if (arg0.getSource() == comboBoxPrewritten)
		{
			this.jTextAreaTalk.setText(comboBoxPrewritten.getSelectedItem().toString());
		}
		else if (arg0.getSource() == btnNewTemplate)
		{
			this.comboBoxPrewritten.addItem(jTextAreaTalk.getText());
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
				this.comboBoxPrewritten.removeAllItems();
				
				// read templates
				NodeList templatelist = doc.getElementsByTagName("template");		
				for (int i=0; i<templatelist.getLength(); i++)
				{
					String template = templatelist.item(i).getTextContent();
					this.comboBoxPrewritten.addItem(template);
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
				for (int i=0; i<comboBoxPrewritten.getItemCount(); i++)
				{
					String template = comboBoxPrewritten.getItemAt(i).toString();
					Element e = xmldoc.createElement("template");
					Text n = xmldoc.createTextNode(template);
					e.appendChild(n);
					root.appendChild(e);
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

}
