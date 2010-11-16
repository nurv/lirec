/** 
 * GretaAgent.java - The interface virtual agent
 *  
 * Copyright (C) 2006 GAIPS/INESC-ID 
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: HWU
 * Project: LIREC
 * Created: 31/03/09 
 * @author: Meiyii Lim
 * Email to: myl@macs.hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 31/03/2009 - File created
 * 
 * **/

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;


import FAtiMA.Core.util.parsers.SocketListener;


public class GretaAgent extends SocketListener{

	private WorldTest _world;
	
	public GretaAgent(WorldTest world, Socket s) {
		
		_world = world;
		
		this.socket = s;

		this.initializeSocket();
		int nBytes;
		try {
			sleep(100);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		try
		{
			nBytes = this.socket.getInputStream().available();
		}
		catch (Exception e)
		{
			nBytes = 0;
		}
	}
	
	public void processMessage(String msg) {
			
	}
	
	protected boolean Send(String msg) {
		try {
			String aux = msg + "\n";
			OutputStream out = this.socket.getOutputStream();
			out.write(aux.getBytes("UTF-8"));
			out.flush();
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			this.stopped = true;
			try {
				this.socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
		}
	}
	
	@Override
	public void handleSocketException() {
		_world.removeGreta();
	}

}
