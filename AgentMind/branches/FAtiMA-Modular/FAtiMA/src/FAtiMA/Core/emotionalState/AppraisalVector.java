/** 
 * AppraisalVector.java - Class that represent a vector with OCC's appraisal variable
 *  
 * Copyright (C) 2009 GAIPS/INESC-ID 
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
 * Created: 2009 
 * @author: João Dias
 * Email to: joao.dias@gaips.inesc-id.pt
 * 
 * History: 
 */
package FAtiMA.Core.emotionalState;

public class AppraisalVector {
	
	public static final short LIKE = 0;
	public static final short DESIRABILITY = 1;
	public static final short DESIRABILITY_FOR_OTHER = 2;
	public static final short PRAISEWORTHINESS = 3;
	
	private float[] _vector = {0,0,0,0};
	
	public AppraisalVector()
	{
	}
	
	public float getAppraisalVariable(short variable)
	{
		return this._vector[variable];
	}
	
	public void setAppraisalVariable(short variable, float value)
	{
		this._vector[variable] = value;
	}
	
	public void sum(AppraisalVector v)
	{
		if(v != null)
		{
			_vector[0] = this._vector[0] + v._vector[0];
			_vector[1] = this._vector[1] + v._vector[1];
			_vector[2] = this._vector[2] + v._vector[2];
			_vector[3] = this._vector[3] + v._vector[3];
		}
	}
}
