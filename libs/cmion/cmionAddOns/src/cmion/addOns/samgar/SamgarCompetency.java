package cmion.addOns.samgar;

import yarp.Bottle;
import yarp.Network;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;

/** The abstract super class for cmion competencies that can communicate with a Samgar module.
 *  Every competency that is listed in the competency library file as SamgarCompetency should
 *  inherit from this class */
public abstract class SamgarCompetency extends Competency {
	
	/** the yarp communication port for this competency */
	private CmionPort port;
	
	private String portName;
	
	public SamgarCompetency(IArchitecture architecture) 
	{
		super(architecture);
		portName = null;
	}

	@Override
	public void setAdditionalData(Object data)
	{
		if (data instanceof String)
			portName = (String) data;
	}
	
	/** in here we initialize the yarp port */
	@Override
	public final void initialize() 
	{
		if (portName==null) portName = this.getCompetencyName();
		Network.init();
		port = new CmionPort(this,portName);		
		// after the ports have been created we are ready
		this.available = true;
	}

	/** every Samgar competency can use this function to acquire a bottle that the module 
	 *  can subsequently fill with data and that will be sent to the connected Samgar module 
	 *  upon calling sendBottle() */
	protected final Bottle prepareBottle()
	{
		Bottle b = port.prepare();
		b.clear();
		return b;
	}
	
	
	
	/** every Samgar competency can use this function to send a bottle (that has been retrieved
	 *  previously through a call of prepareBottle() to the connected Samgar module */
	protected final void sendBottle()
	{
		port.writeStrict();
	}
	
	/** every samgar competency has to implement this function in which it will 
	 *  receive bottles that the connected samgar module sends */
	public abstract void onRead(Bottle bottle_in);

}
