package cmion.addOns.samgar;

import yarp.Bottle;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;

/** The abstract super class for cmion competencies that can communicate with a Samgar module.
 *  Every competency that is listed in the competency library file as SamgarCompetency should
 *  inherit from this class */
public abstract class SamgarCompetency extends Competency {
	
	/** the out port for this competency */
	private CmionPort outPort;
	
	public SamgarCompetency(IArchitecture architecture) 
	{
		super(architecture);
	}

	/** in here we initialize the ports */
	@Override
	public final void initialize() 
	{
		// create the in port
		new CmionPort(CmionPort.PortType.In,this);
		// create the out port
		outPort = new CmionPort(CmionPort.PortType.Out,this);		
		// after the ports have been created we are ready
		this.available = true;
	}

	/** every Samgar competency can use this function to acquire a bottle that the module 
	 *  can subsequently fill with data and that will be sent to the connected Samgar module 
	 *  upon calling sendBottle() */
	protected final Bottle prepareBottle()
	{
		return outPort.prepare();
	}
	
	
	
	/** every Samgar competency can use this function to send a bottle (that has been retrieved
	 *  previously through a call of prepareBottle() to the connected Samgar module */
	protected final void sendBottle()
	{
		outPort.write();
	}
	
	/** every samgar competency has to implement this function in which it will 
	 *  receive bottles that the connected samgar module sends */
	public abstract void onRead(Bottle bottle_in);

}
