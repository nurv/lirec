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

package cmion.inTheWild.datastructures;

/** class encapsulating an incoming sms message (received through the SMSReceiver competency) */
public class LirecSMS 
{
 
	/** the content of the text message*/
	private String content;
		
	/** the phoneNo of the sender */
	private String phoneNo;
	
	/** create a new LirecSMS */
	public LirecSMS(String content, String phoneNo)
	{
		this.content = content;
		this.phoneNo = phoneNo;
	}
	
	/** returns the content of the message */
	public String getContent()
	{
		return content;
	}
		
	/** returns the phone number of the sender */
	public String getPhoneNo()
	{
		return phoneNo;
	}
	
	
	
	
}
