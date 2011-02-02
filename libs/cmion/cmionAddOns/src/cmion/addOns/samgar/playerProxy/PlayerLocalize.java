package cmion.addOns.samgar.playerProxy;

import java.util.ArrayList;
import java.util.Collections;
import yarp.Bottle;
import cmion.addOns.samgar.playerProxy.PlayerProxy;

public class PlayerLocalize {
	public enum CMD{
		SET_REQ, SET_POSE
	}
	
	public enum DATA{
		ERROR, ACK, MAP_INFO, HYPOTHS
	}
	
	public int mapSizeX;
	public int mapSizeY;
	public int mapTileX;
	public int mapTileY;
	public double mapScale;
	public ArrayList<Hypoth> hypoths;
	public int hypothsCount;
	public Hypoth bestHypoth;
	public boolean updated;
	
	public PlayerLocalize(){
		hypoths = new ArrayList<Hypoth>();
		hypothsCount = 0;
		bestHypoth = new Hypoth();
	}
	
	public void setPose(Bottle b, Coordinate pose,
						final double cov_x, final double cov_y, final double cov_a){
		b.clear();
		b.addInt(PlayerProxy.Types.Localize.ordinal());
		b.addInt(CMD.SET_POSE.ordinal());
		b.addDouble(pose.x);
		b.addDouble(pose.y);
		b.addDouble(pose.a);
		b.addDouble(cov_x);
		b.addDouble(cov_y);
		b.addDouble(cov_a);	
	}
	
	public void setReq(Bottle b, final boolean map_info, final boolean hypoths){
		b.clear();
		b.addInt(PlayerProxy.Types.Localize.ordinal());
		b.addInt(CMD.SET_REQ.ordinal());
		b.addInt(map_info?1:0); 	// MAP_INFO
		b.addInt(hypoths?1:0); 	// , HYPOTHS
	}
	
	public void update(Bottle b){
		if(b.get(1).asInt() == PlayerLocalize.DATA.MAP_INFO.ordinal())
		{
			mapSizeX = b.get(2).asInt();
			mapSizeY = b.get(3).asInt();
			mapTileX = b.get(4).asInt();
			mapTileY = b.get(5).asInt();
			mapScale = b.get(6).asDouble();
			updated=true;
		}
		if(b.get(1).asInt() == PlayerLocalize.DATA.HYPOTHS.ordinal())
		{
			final int COUNT_FIELD=2;
			final int FIELDS=4;
			final int X_FIELD=1;
			final int Y_FIELD=2;
			final int A_FIELD=3;
			final int COEFF_FIELD=4;
			hypothsCount = b.get(COUNT_FIELD).asInt();
			for (int i=0; i<hypothsCount; i++)
			{
				hypoths.add(new Hypoth(new Coordinate(b.get(COUNT_FIELD+i*FIELDS+X_FIELD).asDouble(),
									   				  b.get(COUNT_FIELD+i*FIELDS+Y_FIELD).asDouble(),
									   				  b.get(COUNT_FIELD+i*FIELDS+A_FIELD).asDouble()),
									   b.get(COUNT_FIELD+i*FIELDS+COEFF_FIELD).asDouble()));
			}
			Collections.sort(hypoths,Collections.reverseOrder());
			bestHypoth = hypoths.get(0);
			updated=true;
		}
	}
}
