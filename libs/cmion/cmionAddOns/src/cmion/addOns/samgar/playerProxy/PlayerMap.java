package cmion.addOns.samgar.playerProxy;

import yarp.Bottle;

public class PlayerMap {
	public enum CMD{
		SET_REQ
	}
	
	public enum DATA{
		ERROR, ACK, MAP
	}
	
	public double[][] map;
	public int width;
	public int height;
	public double resolution;
	public boolean updated;
	
	public PlayerMap(){
		map = null;
		width=0;
		height=0;
	}
	
	public void setReq(Bottle b, final boolean map){
		b.clear();
		b.addInt(PlayerProxy.Types.Map.ordinal());
		b.addInt(CMD.SET_REQ.ordinal());
		b.addInt(map?1:0); 	// MAP
	}
	
	public void update(Bottle b){
		if(b.get(1).asInt() == DATA.MAP.ordinal())
		{
			final int WIDTH_FIELD=2;
			final int HEIGHT_FIELD=3;
			final int RESOLUTION_FIELD=4;
			final int DATA_START=5;
			int newWidth = b.get(WIDTH_FIELD).asInt();
			int newHeight = b.get(HEIGHT_FIELD).asInt();
			if (newWidth != width || newHeight != height) {
				width = newWidth;
				height = newHeight;
				map = new double[height][width];
			}
			resolution = b.get(RESOLUTION_FIELD).asDouble();
			for (int y=0; y<height; y++)
				for (int x=0; x<width; x++)
					map[y][x] = b.get(DATA_START+y*height+x).asDouble();
			updated=true;
		} // WAYPOINTS
	}
}
