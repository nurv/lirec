/** 
 * SocketListener.java - Implements a socket listener
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
 * Company: GAIPS/INESC-ID
 * Project: FAtiMA
 * Created: 01/04/2002 
 * @author: Rui Prada
 * Email to: rui.prada@tagus.ist.utl.pt
 * 
 * History: 
 * Rui Prada: 01/04/2002 - File created
 */


import java.net.Socket;

public abstract class SocketListener extends Thread {
    protected int maxSize = 256;

    protected Socket socket;
    byte[] buffer = new byte[maxSize];

    protected boolean stoped = false;

    public SocketListener() {
    }
    
    /** Creates new SocketListener */
    public SocketListener(Socket socket) {
        this.socket = socket;
    }

    public void close () {
        stoped = true;
        try {
            socket.close();
        }
        catch(java.io.IOException ex) {
        }
    }

    public abstract void processData(byte[] data);

    public void run() {
        int nBytes;   

        while(!stoped) {
            try {
                sleep(100);
            }
            catch(InterruptedException ex) {
            }
            
            nBytes = receive();
            if(nBytes > 0) {
                try {
                    buffer = new byte[nBytes];
                    socket.getInputStream().read(buffer);
//                    System.out.println("Socket: " + new String(buffer));
                }
                catch (java.io.IOException ex) {
                    ex.printStackTrace();
                    stoped = true;
                }
                processData(buffer);
            }
        }
    }

    protected int receive() {
    	
    	if(!socket.isConnected())
        {
        	stoped = true;
        	return 0;
        }
        try {
            //System.out.println(socket.getInputStream().available());
            return socket.getInputStream().available();
        }
        catch (java.io.IOException ex) {
            ex.printStackTrace();
            stoped = true;
            return 0;
        }
    }
}