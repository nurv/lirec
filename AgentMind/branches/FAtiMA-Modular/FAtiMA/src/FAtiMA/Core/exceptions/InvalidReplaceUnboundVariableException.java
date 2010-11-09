/** 
 * InvalidReplaceUnboundException.java - Exception thrown you try to call the ReplaceUnboundVariables in a Ritual
 * 
 * Actions file
 *  
 * Copyright (C) 2008 GAIPS/INESC-ID 
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
 * Created: 13/03/2008 
 * @author: João Dias
 * Email to: joao.dias@gaips.inesc-id.pt
 * 
 * History: 
 * João Dias: 13/03/2008 - File created
 */

package FAtiMA.Core.exceptions;

public class InvalidReplaceUnboundVariableException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidReplaceUnboundVariableException() {
        super("You cannot call the ReplaceUnboundVariable method to Rituals!");
   }
   
   /**
    * Construct an exception passing a message back 
    * @param msg message
    */
   public InvalidReplaceUnboundVariableException(String msg) {
         super(msg);
   }

   /**
    * @param msg message
    * @param ex wrapped error/exception
    */
   public InvalidReplaceUnboundVariableException(String msg, Throwable ex) {
         super(msg, ex);
   }

   /**
    * @param ex wrapped error/exception
    */
   public InvalidReplaceUnboundVariableException(Throwable ex) {
         super(ex);
   }

}