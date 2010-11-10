/** 
 * EmotionType.java - Class that implements the Enumerable for OCC's 22 emotion types
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
 * Created: 21/12/2004 
 * @author: Jo�o Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * Jo�o Dias: 21/12/2004 - File created
 */

package FAtiMA.Core.util.enumerables;

import FAtiMA.Core.exceptions.InvalidEmotionTypeException;

/**
 * Enumerable for the 22 OCC's emotion types
 * 
 * @author Jo�o Dias
 */
public abstract class ExpectedEffectType {

	public static final short ON_SELECT = 0;
    public static final short ON_IGNORE = 1;

}