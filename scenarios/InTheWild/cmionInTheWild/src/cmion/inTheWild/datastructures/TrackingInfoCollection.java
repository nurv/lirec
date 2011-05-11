package cmion.inTheWild.datastructures;

import java.util.HashMap;
import java.util.Set;

/** Tracking Information for all currently recognized users */
public class TrackingInfoCollection 
{

	private HashMap<Integer,TrackingInfo> infos;
	
	public TrackingInfoCollection()
	{
		infos = new  HashMap<Integer,TrackingInfo>();
	}
	
	public void addUserTrackingInfo(TrackingInfo info)
	{
		infos.put(info.getUserID(), info);
	}

	public void addHandTrackingInfo(TrackingInfo info)
	{
		infos.put(info.getHandID(), info);
	}	
	
	public int getNoOfObjects()
	{
		return infos.size();
	}
	
	public TrackingInfo getTrackingInfo(int id)
	{
		return infos.get(id);
	}

	public Set<Integer> getAllIDs()
	{
		return infos.keySet();
	}
	
	public boolean hasID(int id)
	{
		return infos.containsKey(id);
	}
	
}
