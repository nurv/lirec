/*	
CMION
Copyright(C) 2010 Heriot Watt University

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
21/04/2010      Michael Kriegel <mk95@hw.ac.uk>
First version.
---  
*/

package cmion.addOns.manipulators;


import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import cmion.architecture.IArchitecture;
import cmion.architecture.CmionComponent;
import cmion.architecture.CmionEvent;
import cmion.storage.CmionStorageContainer;
import cmion.storage.EventPropertyChanged;
import cmion.storage.EventPropertyRemoved;
import cmion.storage.EventSubContainerAdded;
import cmion.storage.EventSubContainerRemoved;
import cmion.storage.WorldModel;


/** this class can be used to directly manipulate and change the world model */
public class WorldModelManipulator extends CmionComponent {

	
/** the gui for the simulator */
SimulatorWindow window;

/** create a new simulator */
public WorldModelManipulator(IArchitecture architecture) 
{
	super(architecture);
    
	//Schedule a job for the event dispatch thread:
    //creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            createAndShowGUI();
        }
    });
}

/**
 * Create the GUI and show it.  For thread safety,
 * this method should be invoked from the
 * event dispatch thread.
 */
private void createAndShowGUI() {
    //Create and set up the window.
    JFrame frame = new JFrame("World Model Manipulator");
    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

    //Add contents to the window.
    window = new SimulatorWindow();
    frame.add(window);

    //Display the window.
    frame.pack();
    frame.setVisible(true);
}

/** add an entity (Agent or Object) to the world model */
public void addEntity(final CmionStorageContainer entityContainer)
{
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() 
        {
        	window.addEntity(entityContainer);
        }
    });
}

/** remove an entity (Agent or Object) from the world model */
public void removeEntity(final String entityName)
{
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() 
        {
        	window.removeEntity(entityName);
        }
    });
}


/** sets a property (Agent or Object) from the world model 
 * */
public void setProperty(final String propertyName, final String propertyValue, 
		final boolean persistent, final CmionStorageContainer parentContainer)
{
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() 
        {
        	window.setProperty(propertyName,propertyValue,persistent,parentContainer);
        }
    });
}

/** removes a property (Agent or Object) from the world model */
public void removeProperty(final String propertyName, final CmionStorageContainer entityContainer)
{
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() 
        {
        	window.removeProperty(propertyName,entityContainer);
        }
    });
}


@Override
public void registerHandlers() 
{
	// create a new handler
	HandleAnyCmionEvent handler = new HandleAnyCmionEvent();
	// listen to any world model events
	architecture.getWorldModel().getEventHandlers().add(handler);
	// and any sub container events of the world model	
	architecture.getWorldModel().registerEventHandlerWithSubContainers(handler);
}

/** internal event handler class for listening to any event */
private class HandleAnyCmionEvent extends EventHandler {

    public HandleAnyCmionEvent() {
        super(CmionEvent.class);
    }

    @Override
    public void invoke(IEvent evt) 
    {
    	if (evt instanceof EventSubContainerAdded)
    	{
    		EventSubContainerAdded evt1 = (EventSubContainerAdded) evt;
    		if (evt1.getParentContainer() == architecture.getWorldModel())
    		{	
    			addEntity(evt1.getSubContainer());
    		}
    	}
    	else if (evt instanceof EventSubContainerRemoved)
    	{
    		EventSubContainerRemoved evt1 = (EventSubContainerRemoved) evt;
    		if (evt1.getParentContainer() == architecture.getWorldModel())
    		{	
    			removeEntity(evt1.getRemovedContainerName());
    		}
    	}
    	else if (evt instanceof EventPropertyChanged)
    	{
    		EventPropertyChanged evt1 = (EventPropertyChanged) evt;
    		if (evt1.getParentContainer().getParentContainer() == architecture.getWorldModel())
    		{
    			setProperty(evt1.getPropertyName(),evt1.getPropertyValue().toString(),evt1.isPersistent(),evt1.getParentContainer());
    		}
    	}
    	else if (evt instanceof EventPropertyRemoved)
    	{
    		EventPropertyRemoved evt1 = (EventPropertyRemoved) evt;
    		if (evt1.getParentContainer().getParentContainer() == architecture.getWorldModel())
    		{
    			removeProperty(evt1.getPropertyName(),evt1.getParentContainer());
    		}
    	}
    }
}


/** frame to display events */
private class SimulatorWindow extends JPanel implements ActionListener, TreeSelectionListener 
{
	private class Property
	{
		
		public String name;
		public String value;
		public boolean persistent;
		private CmionStorageContainer parent;
		
		public Property(String name, String value, boolean persistent, CmionStorageContainer parent)
		{
			this.name = name;
			this.value = value;
			this.persistent = persistent;
			this.parent = parent;
		}
		
		public CmionStorageContainer getParent()
		{
			return parent;
		}
		
		@Override
		public String toString()
		{
			return name + " = " + value; 
		}
	}
	
	private static final long serialVersionUID = 1L;
	
	protected HashMap<String,DefaultMutableTreeNode> containers;
	
	protected DefaultMutableTreeNode rootNode;
	protected DefaultTreeModel treeModel;
    protected JTree tree;
    protected JPanel toolBar;
    
    protected JTextField txtEntity;
    protected JButton btnAddEntity;
    protected JButton btnRemoveEntity;
    protected JTextField txtPropertyName;
    protected JTextField txtPropertyValue;
    protected JButton btnSetProperty;
    protected JButton btnRemoveProperty;
    protected JComboBox agentOrObject;
    protected JCheckBox checkBoxPersistent;
    
    public SimulatorWindow() {
        super(new BorderLayout());

        containers = new HashMap<String,DefaultMutableTreeNode>();
        
        rootNode = new DefaultMutableTreeNode("WorldModel");
        treeModel = new DefaultTreeModel(rootNode);
        
        tree = new JTree(treeModel);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new CustomRenderer());

        //Listen for when the selection changes.
        tree.addTreeSelectionListener(this);

        toolBar = new JPanel();
        JScrollPane scrollPane = new JScrollPane(tree);

        // create toolbar
        JLabel lblEntity = new JLabel("entity: ");
        Font f =lblEntity.getFont();
        lblEntity.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
        toolBar.add(lblEntity);

        txtEntity = new JTextField();
        txtEntity.setPreferredSize(new Dimension(70,25));
        toolBar.add(txtEntity);

        JLabel lblType = new JLabel("type ");
        toolBar.add(lblType);

        String[] comboTypes = { WorldModel.AGENT_TYPE_NAME, WorldModel.OBJECT_TYPE_NAME};
        agentOrObject = new JComboBox(comboTypes);
        agentOrObject.setSelectedIndex(0);
        toolBar.add(agentOrObject);
        
        btnAddEntity = new JButton("add");
        btnAddEntity.addActionListener(this);
        toolBar.add(btnAddEntity);
        
        btnRemoveEntity = new JButton("remove");
        btnRemoveEntity.addActionListener(this);
        toolBar.add(btnRemoveEntity);

        JLabel lblProperty = new JLabel("     property: ");
        f =lblProperty.getFont();
        lblProperty.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
        toolBar.add(lblProperty);

        JLabel lblPropertyName = new JLabel(" name ");
        toolBar.add(lblPropertyName);
        
        txtPropertyName = new JTextField();
        txtPropertyName.setPreferredSize(new Dimension(70,25));
        toolBar.add(txtPropertyName);

        JLabel lblPropertyValue = new JLabel(" value ");
        toolBar.add(lblPropertyValue);
        
        txtPropertyValue = new JTextField();
        txtPropertyValue.setPreferredSize(new Dimension(70,25));        
        toolBar.add(txtPropertyValue);

        checkBoxPersistent = new JCheckBox(" persistent  ");
        toolBar.add(checkBoxPersistent);
        
        btnSetProperty = new JButton("set");
        btnSetProperty.addActionListener(this);
        toolBar.add(btnSetProperty);
        
        btnRemoveProperty = new JButton("remove");
        btnRemoveProperty.addActionListener(this);
        toolBar.add(btnRemoveProperty);
        
        //Add components to this panel.
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

	public DefaultMutableTreeNode addEntity(CmionStorageContainer entityContainer) 
    {
    	if (!containers.containsKey(entityContainer.getContainerName()))
    	{
    		DefaultMutableTreeNode node = new DefaultMutableTreeNode(entityContainer);
    		containers.put(entityContainer.getContainerName(), node);
    		rootNode.add(node);
    		updateTree(rootNode);
    		return node;
    	}
    	else return null;
	}

    public void removeEntity(String entityName) 
    {
    	DefaultMutableTreeNode node = containers.get(entityName);
    	if (node!=null)
    	{
    		rootNode.remove(node);
    		containers.remove(entityName);
    		updateTree(rootNode);
    	}
	}

	public void setProperty(String propertyName, String propertyValue,boolean persistent,
			CmionStorageContainer parentContainer) 
	{
    	DefaultMutableTreeNode node = containers.get(parentContainer.getContainerName());
    	if (node==null) node = addEntity(parentContainer);
    	// iterate over children, try to find one that holds the property (if property already exists)
    	DefaultMutableTreeNode propNode = null;
    	for (int i=0; i<node.getChildCount(); i++)
    	{
    		DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
    		if (childNode.getUserObject() instanceof Property)
    		{
    			Property prop = (Property) childNode.getUserObject();
    			if (prop.name.equals(propertyName))
    			{
    				prop.value = propertyValue;
    				prop.persistent = persistent;
    				propNode = childNode;
    				break;
    			}
    		}
    	}
    	if (propNode == null)
    	{
    		propNode = new DefaultMutableTreeNode(new Property(propertyName,propertyValue,persistent,parentContainer));
    		node.add(propNode); 
    	}	
		updateTree(node);
	}
    
    public void removeProperty(String propertyName,
			CmionStorageContainer entityContainer) 
    {
    	DefaultMutableTreeNode node = containers.get(entityContainer.getContainerName());
    	if (node!=null) 
    	{
        	// iterate over children, try to find one that holds the property (if property already exists)
        	DefaultMutableTreeNode propNode = null;
        	for (int i=0; i<node.getChildCount(); i++)
        	{
        		DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
        		if (childNode.getUserObject() instanceof Property)
        		{
        			Property prop = (Property) childNode.getUserObject();
        			if (prop.name.equals(propertyName))
        			{
        				propNode = childNode;
        				node.remove(propNode);
        				updateTree(node);
        				break;
        			}
        		}
        	}		
    	}	
	}

    private void updateTree(TreeNode manipulated)
    {
		TreePath path = tree.getSelectionPath();
		treeModel.reload(manipulated);
		tree.setSelectionPath(path);
    }
    
    private synchronized void updateGui()
    {
   	
    	// get currently selected Object
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
        tree.getLastSelectedPathComponent();
        if (node!=null)
        {
        	Object selectedObject = node.getUserObject();
        	if (selectedObject instanceof Property)
        	{
        		Property prop = (Property) selectedObject;
        		txtPropertyName.setText(prop.name);
        		txtPropertyValue.setText(prop.value);
        		txtEntity.setText(prop.getParent().getContainerName()); 
        		checkBoxPersistent.setSelected(prop.persistent);
        		agentOrObject.setSelectedItem(prop.getParent().getContainerType());
        	}
        	else if (selectedObject instanceof CmionStorageContainer)
        	{
        		CmionStorageContainer cont = (CmionStorageContainer) selectedObject;
        		txtEntity.setText(cont.getContainerName());  		
        		agentOrObject.setSelectedItem(cont.getContainerType());
        	}	
    	}  	
    }
    
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		if (arg0.getSource() == this.btnAddEntity)
		{
			String entityName = this.txtEntity.getText().trim();
			if ((entityName.length()>0) && (!architecture.getWorldModel().hasSubContainer(entityName)))
			{
				if (agentOrObject.getSelectedIndex()==0)
					architecture.getWorldModel().requestAddAgent(entityName);
				else
					architecture.getWorldModel().requestAddObject(entityName);
			}
		}
		else if (arg0.getSource() == this.btnRemoveEntity)
		{
			String entityName = this.txtEntity.getText().trim();
			if ((entityName.length()>0) && (architecture.getWorldModel().hasSubContainer(entityName)))
				architecture.getWorldModel().requestRemoveSubContainer(entityName);
		}
		else if (arg0.getSource() == this.btnSetProperty)
		{
			String entityName = this.txtEntity.getText().trim();
			if ((entityName.length()>0) && (architecture.getWorldModel().hasSubContainer(entityName)))
			{	
				String propertyName = this.txtPropertyName.getText().trim();
				String propertyValue = this.txtPropertyValue.getText().trim();
				if ((propertyName.length()>0))
					architecture.getWorldModel().getSubContainer(entityName).
					             requestSetProperty(propertyName, propertyValue,checkBoxPersistent.isSelected());
			}	
		}
		else if (arg0.getSource() == this.btnRemoveProperty)
		{
			String entityName = this.txtEntity.getText().trim();
			if ((entityName.length()>0) && (architecture.getWorldModel().hasSubContainer(entityName)))
			{	
				CmionStorageContainer entity = architecture.getWorldModel().getSubContainer(entityName);
				String propertyName = this.txtPropertyName.getText().trim();
				if ((propertyName.length()>0) && (entity.hasProperty(propertyName)))
					entity.requestRemoveProperty(propertyName);
			}	
		}

	}

	@Override
	public void valueChanged(TreeSelectionEvent arg0) 
	{
		updateGui();
	}

	class CustomRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTreeCellRendererComponent(
	                        JTree tree,
	                        Object value,
	                        boolean sel,
	                        boolean expanded,
	                        boolean leaf,
	                        int row,
	                        boolean hasFocus) {

	        super.getTreeCellRendererComponent(
	                        tree, value, sel,
	                        expanded, leaf, row,
	                        hasFocus);
	        if (leaf && isPersistentProperty(value)) 
	        {
	        	setFont(new Font(getFont().getName(),Font.ITALIC,getFont().getSize()));
	        } 
	        else
	        {
	        	setFont(new Font(getFont().getName(),Font.PLAIN,getFont().getSize()));
	     	}
	        return this;
	    }

	    protected boolean isPersistentProperty(Object value) {
	        if (value instanceof DefaultMutableTreeNode)
	        {
	        	Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
	        	if (userObject instanceof Property)
	        		return ((Property)userObject).persistent;
	        }
	        return false;
	    }
	}
	
}
}
